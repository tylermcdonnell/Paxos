package client;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import framework.NetController;
import message.Message;
import message.PlainMessage;
import message.Request;
import message.Response;
import server.State;
import server.StateEntry;

/**
 * A client in a chat room.  The chat room is kept consistent
 * on all replicas with Paxos.
 * @author Mike Feilbach
 *
 */
public class Client implements Runnable {

	// This client's hand-made ID.
	private final int id;
	
	// Buffered queue commands sent to client from the master.
	private LinkedList<String> clientReceiveQueue;
	
	// Client-local unique command ID.  For each command issued, use a new cid.
	private int cid;
	
	// NetController for this client.
	private NetController network;
	
	// Number of clients in the system.
	private int numClients;
	
	// Number of servers in the system.
	private int numServers;
	
	// This client's view of the chat room state.
	private State chatLog;
	
	// Commands we have issued to servers.
	private ArrayList<CommandStatus> commandStatuses;
	
	// If we haven't received a response for a given command within
	// this amount of time (in milliseconds), re-send the command.
	private static final int RESPONSE_RESEND_PERIOD = 10000;
	
	/**
	 * Constructor.
	 * 
	 * @param id, the ID of this client.
	 * @param receiveQueue, the queue used to communicate with this client.
	 * @param nc, this client's NetController.
	 * @param numClients, number of clients in the system.
	 * @param numServers, number of servers in the system.
	 */
	public Client(int id, LinkedList<String> receiveQueue, NetController nc, int numClients, int numServers)
	{
		this.id = id;
		this.clientReceiveQueue = receiveQueue;
		
		// Start client-local unique command IDs at 0.
		this.cid = 0;
		this.network = nc;
		this.numClients = numClients;
		this.numServers = numServers;
		
		// Chat room log empty initially.
		this.chatLog = new State();
		
		this.commandStatuses = new ArrayList<CommandStatus>();
	}
	
	
	/**
	 * Returns the next unique cid for this client.
	 * 
	 * @return the next unique cid for this client.
	 */
	private int getNextCid()
	{
		int returnVal = this.cid;
		
		this.cid++;
		
		return returnVal;
	}
	
	/**
	 * Send a client a command to fulfill.  The master class can use this.
	 * 
	 * @param message, the message describing this command.
	 */
	public synchronized void giveClientCommand(String message)
	{
		synchronized(this.clientReceiveQueue)
		{
			this.clientReceiveQueue.add(message);
		}
	}
	
	
	@Override
	public void run()
	{
		// Testing.
		System.out.println("Client " + this.id + " created.");
		
		while (true)
		{
			//******************************************************************
			//* Check if we need to re-request any commands (if this client has
			//* not received a response for a command within a given period of
			//* time.
			//******************************************************************
			for (int i = 0; i < this.commandStatuses.size(); i++)
			{
				CommandStatus currCommandStatus = this.commandStatuses.get(i);

				// If the command corresponding to this command status has not yet been
				// received, check if we must re-send it.
				if (!currCommandStatus.getResponseReceived())
				{
					// We have not yet gotten a response for the command
					// corresponding to this command status.  Check if we must
					// re-request it yet.
					long currentTime = System.currentTimeMillis();
					long nextCheckTime = currCommandStatus.getNextCheckTime();
					if (currentTime >= nextCheckTime)
					{
						// We must re-send this request!
						// Send this command to all servers.
						sendRequestToAllServers(currCommandStatus.getCommand());
						
						// Update this command status' next check time, based on
						// the old next check time.
						currCommandStatus.setNextCheckTime(nextCheckTime + Client.RESPONSE_RESEND_PERIOD);
						
						// Testing.
						System.out.println("Re-sending: " + currCommandStatus.getCommand());
					}
				}
			}
			
			
			
			//******************************************************************
			//* MASTER MESSAGES
			//******************************************************************
			
			// This will contain the messages received in this iteration
			// from the master.
			ArrayList<String> masterMessages = getMasterMessages();
			
			// Process messages from master.
			for (int i = 0; i < masterMessages.size(); i++)
			{
				System.out.println("Client " + this.id + " received from master: " + masterMessages.get(i));
				
				// Construct a Command with this message as the operation.
				Command command = new Command(this.id, this.getNextCid(), masterMessages.get(i));
				
				// Remember that we requested this Command.
				this.commandStatuses.add(new CommandStatus(command, System.currentTimeMillis(), System.currentTimeMillis() + Client.RESPONSE_RESEND_PERIOD));
				
				// Send this command to all servers.
				sendRequestToAllServers(command);
				
				// Testing
				System.out.println("Sending: " + command);
			}
			
			
			//******************************************************************
			//* NETWORK MESSAGES
			//******************************************************************
			
			// Receive messages on network.
			ArrayList<Message> networkMessages = getNetworkMessages();
			
			for (int i = 0; i < networkMessages.size(); i++)
			{
				Message currMessage = networkMessages.get(i);
				
				// Testing.
				//System.out.println("Client " + this.id + " got message on network: " + networkMessages.get(i));
				
				
				//**************************************************************
				//* Client received a Response from a Replica.
				//**************************************************************
				if (currMessage instanceof Response)
				{
					Response response = (Response) currMessage;
					
					System.out.println("Client " + this.id + " received: " + response);
					
					// Check if we have gotten this result yet (since all
					// replicas send a performed decision to the client
					// who issued the command).
					StateEntry result = response.getResult();
					
					synchronized(this.chatLog)
					{
						if (!this.chatLog.getState().contains(result))
						{
							// Add this result to our state.
							this.chatLog.addToState(response.getResult());
						}
					}
		
					// Testing.  Print out this client's view of chat room.
					printChatLog();
					
					// Note that we have received a response to this command.
					Command commandReceived = response.getResult().getCommand();
					
					// Find the command that we just received a response for.
					for (int j = 0; j < this.commandStatuses.size(); j++)
					{
						CommandStatus currCommandStatus = this.commandStatuses.get(j);
						Command currCommand = currCommandStatus.getCommand();
						if (currCommand.equals(commandReceived))
						{
							// Note that this command has received a response.
							currCommandStatus.setResponseReceived();
						}
					}
				}
				
				// Communication testing.
				if (currMessage instanceof PlainMessage)
				{
					PlainMessage plainMessage = (PlainMessage) currMessage;
					System.out.println("Client " + this.id + " received " + plainMessage);
				}
			}
		}
	}
	
	
	// TODO: make printChatLog() block until completion.  This just means that
	// we need to make it such that no other processes use stdout.  We need to
	// eventually funnel all System.out.print()'s to the Master for processing,
	// so this shouldn't be too hard once we finish that.  For now, this method
	// is NOT blocking.
	
	// TSM: Per the project description, I don't think our final project can
	// 		print anything at all to sdtout or even any other buffer, such as
	//		stderr. We will probably need to comment out all prints throughout
	//		the projects (even stack trace prints from errors). That also probably
	// 		means that we won't have to worry about funneling prints to the Master.
	//      That is, since the Master will be the thread calling printChatLog,
	// 		only one thing could possibly ever be printing at a time. At the same
	//		time, that means that a separate thread (other than the client) will
	// 		be accessing this printChatLog method, which means that it's possible
	// 		for more than one thread to be accessing the chatLog at a time. So,
	// 		I think we need to synchronize accesses to the chatLog.
	/**
	 * Print this client's view of the chat record in the format
	 * specified in the design doc.  Should block until completion.
	 *
	 */
	public void printChatLog()
	{
		// TODO: is sequence number the slot number?  Assume so, but ask!
		// Or is it just a logical number, like 0, 1, 2, 3... if we use the
		// slot number, it may look like 0, 2, 45, 56...
		
		// TSM: Per Benny's Piazza post, it appears that the sequence number
		//      is not equivalent to slot number. NOPs can be slotted and they
		// 		should not appear in this output OR cause gaps in the sequence
		//		numbers of the output.
		System.out.println("\nCHAT LOG of client " + this.id + ":");
		
		synchronized(this.chatLog)
		{
			ArrayList<StateEntry> entries = this.chatLog.getState();
			Collections.sort(entries);
			
			for (int i = 0; i < entries.size(); i++)
			{
				StateEntry currEntry = entries.get(i);
				
				System.out.println(i + " " 
						+ currEntry.getCommand().getClientId() + ": " 
						+ currEntry.getCommand().getOperation());
			}
			System.out.println();
		}
	}
	
	
	/**
	 * Returns an ArrayList<Message> of the messages received over the
	 * network at the time this method is called.
	 * 
	 * @return an ArrayList<Message> of the messages received over the
	 * network at the time this method is called.
	 */
	private ArrayList<Message> getNetworkMessages()
	{
		ArrayList<Message> messagesFromNet = new ArrayList<Message>();
		
		List<Message> received = this.network.getReceived();
		for (Iterator<Message> i = received.iterator(); i.hasNext();)
		{
			messagesFromNet.add(i.next());
		}
		
		return messagesFromNet;
	}
	
	
	/**
	 * Returns messages sent from the master at the time this method is
	 * called.
	 * 
	 * @return messages sent from the master at the time this method is
	 * called.
	 */
	private ArrayList<String> getMasterMessages()
	{
		ArrayList<String> messagesFromMaster = new ArrayList<String>();
		
		// Continuously listen for client commands from the master.
		synchronized(this.clientReceiveQueue)
		{	
			for (Iterator<String> i = this.clientReceiveQueue.iterator(); i.hasNext();)
			{
				String msg = i.next();
				i.remove();
				
				// Process this message outside of the iterator loop.
				messagesFromMaster.add(msg);
			}
		}
		
		return messagesFromMaster;
	}
	

	/**
	 * With the given command, send a request to all servers.
	 * 
	 * @param command, the given command.
	 */
	private void sendRequestToAllServers(Command command)
	{
		// Create request for this command.
		Request request = new Request(command);
		
		// Broadcast <"request", <K, cid, op>> to all replicas.
		for (int i = 0; i < this.numServers; i++)
		{
			this.network.sendMsgToServer(i, request);
		}
	}
	
	
	//**************************************************************************
	//* TESTING CODE
	//**************************************************************************
	
	public void testClientSend(String message, int clientIndex)
	{
		this.network.sendMsgToClient(clientIndex, new PlainMessage(message));
	}
	
	public void testServerSend(String message, int serverIndex)
	{
		this.network.sendMsgToServer(serverIndex, new PlainMessage(message));
	}
}
