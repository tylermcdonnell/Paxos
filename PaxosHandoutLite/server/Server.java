package server;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;

import message.Decision;
import message.Message;
import message.PlainMessage;
import message.Proposal;
import message.Request;
import client.Command;
import framework.NetController;

/**
 * A Paxos server, which acts as a replica, leader, and acceptor.
 * @author Mike Feilbach
 * 
 */
public class Server implements Runnable {

	// This server's hand-made ID.
	private final int id;
	
	// This server's Replica.
	private Replica replica;
	
	// This server's Acceptor.
	private Acceptor acceptor;
	
	// This server's Leader.
	private Leader leader;
	
	// This server's NetController.
	private NetController network;
	
	// Buffered queue commands sent to client from the master.
	private LinkedList<String> serverReceiveQueue;
	
	
	/**
	 * Constructor.
	 * 
	 * @param id, the ID of this server.
	 * @param nc, the NetController for this server.
	 */
	public Server(int id, NetController nc, LinkedList<String> serverReceiveQueue)
	{
		this.id = id;
		this.network = nc;
		this.serverReceiveQueue = serverReceiveQueue;
		this.replica = new Replica(this.id);
		this.leader = new Leader(this.id);
		this.acceptor = new Acceptor(this.id);
	}
	
	@Override
	public void run()
	{
		System.out.println("Server " + this.id + " created.");
		
		while (true)
		{
			//******************************************************************
			//* MASTER MESSAGES
			//******************************************************************
			
			// This will contain the messages received in this iteration
			// from the master.
			ArrayList<String> masterMessages = getMessagesFromMaster();
			
			// Process messages from master.
			for (int i = 0; i < masterMessages.size(); i++)
			{
				System.out.println("Server " + this.id + " received from master: " + masterMessages.get(i));
			}
			
			
			//******************************************************************
			//* NETWORK MESSAGES
			//******************************************************************
			
			// Receive messages from network.
			ArrayList<Message> networkMessages = (ArrayList<Message>) this.network.getReceived();
			
			for (int i = 0; i < networkMessages.size(); i++)
			{
				Message currMessage = networkMessages.get(i);
				
				this.replica.runTasks(currMessage);
				this.leader.runTasks(currMessage);
				this.acceptor.runTasks(currMessage);
				
				// Communication testing.
				if (currMessage instanceof PlainMessage)
				{
					PlainMessage plainMessage = (PlainMessage) currMessage;
					System.out.println("Server " + this.id + " received " + plainMessage);
				}
			}
		}
	}
	
	
	/**
	 * Analyze this message, and if it is relevant to a leader, carry out
	 * a task as a leader.
	 * 
	 * @param message, the given message.
	 */
	private void performLeaderTasks(Message message)
	{
		// TODO
	}
	
	
	/**
	 * Analyze this message, and if it is relevant to an acceptor, carry out
	 * a task as an acceptor.
	 * 
	 * @param message, the given message.
	 */
	private void performAcceptorTasks(Message message)
	{
		// TODO
	}
	
	
	/**
	 * Returns messages sent from the master.
	 * @return messages sent from the master.
	 */
	private ArrayList<String> getMessagesFromMaster()
	{
		ArrayList<String> messagesFromMaster = new ArrayList<String>();
		
		// Continuously listen for client commands from the master.
		synchronized(this.serverReceiveQueue)
		{	
			for (Iterator<String> i = this.serverReceiveQueue.iterator(); i.hasNext();)
			{
				String msg = i.next();
				i.remove();
				
				// Process this message outside of the iterator loop.
				messagesFromMaster.add(msg);
			}
		}
		
		return messagesFromMaster;
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
