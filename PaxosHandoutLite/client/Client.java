package client;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import framework.NetController;
import message.Message;
import message.PlainMessage;
import message.Request;
import message.Response;

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
				
				// Send this command to all servers.
				sendRequestToAllServers(command);
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
				
				if (currMessage instanceof Response)
				{
					// Wait for <"response", cid, result> message from one of the replicas.
					// TODO
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
