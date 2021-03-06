package server;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import message.Decision;
import message.Message;
import message.P2a;
import message.PlainMessage;
import message.Proposal;
import message.Request;
import client.Command;
import framework.NetController;
import log.Logger;

/**
 * A Paxos server, which acts as a replica, leader, and acceptor.
 * @author Mike Feilbach
 * 
 */
public class Server implements Runnable {

	// This server's hand-made ID.
	public final int id;
	
	// This server's Replica.
	private Replica replica;
	
	// This server's Acceptor.
	public Acceptor acceptor;
	
	// This server's Leader.  Make public so the Master class can view
	// the leader's public variables.
	public Leader leader;
	
	// This server's NetController.
	private NetController network;
	
	// Buffered queue commands sent to client from the master.
	private LinkedList<String> serverReceiveQueue;
	
	// Number of servers in the system.
	private int numServers;
	
	
	/**
	 * Constructor.
	 * 
	 */
	public Server(int id, NetController nc, LinkedList<String> serverReceiveQueue, int numServers, int numClients, boolean isRecovering, long acceptorRecoveryWaitTime)
	{	
		this.id = id;
		this.numServers = numServers;
		this.network = nc;
		this.serverReceiveQueue = serverReceiveQueue;
		this.replica = new Replica(id, numServers, nc, numClients);
		this.leader = new Leader(id, numServers, nc, isRecovering);
		this.acceptor = new Acceptor(id, nc, isRecovering, numServers, acceptorRecoveryWaitTime);
		
		// Current leader upon start up has ID = 0;
		//this.leader.setCurrentLeader(0);
	}
	
	@Override
	public void run()
	{
		Logger.getInstance().println("Server " + this.id + " created.");
		
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
				Logger.getInstance().println("Server " + this.id + " received from master: " + masterMessages.get(i));
			}
			
			
			//******************************************************************
			//* NETWORK MESSAGES
			//******************************************************************
			
			// Receive messages from network.
			ArrayList<Message> networkMessages = getNetworkMessages();
			
			for (int i = 0; i < networkMessages.size(); i++)
			{
				Message currMessage = networkMessages.get(i);
				
				// Testing.
				//Logger.getInstance().println("Server " + this.id + " received: " + currMessage);
				
				this.replica.runTasks(currMessage);
				this.leader.runTasks(currMessage);
				this.acceptor.runTasks(currMessage);
				
				// Communication testing.
				if (currMessage instanceof PlainMessage)
				{
					PlainMessage plainMessage = (PlainMessage) currMessage;
					Logger.getInstance().println("Server " + this.id + " received " + plainMessage);
				}
			}
			
			// (NEW)
			// Leader needs to send heartbeats, so run it continuously, even
			// when no new messages are coming in.
			this.leader.runTasks(null);
			
			// Continuously run acceptor (for case when it is recovering).
			this.acceptor.runTasks(null);
		}
	}
	
	/**
	 * @return 
	 * 		True if this process believs it is leader. Note that you
	 * 		should only call timeBombLeader after an all clear, at 
	 * 		which point ALL processes should agree on the same leader.		
	 */
	public boolean isLeader()
	{
		return this.leader.isLeader();
	}
	
	/**
	 * @param countdown
	 * 		If this server believes it is the current leader, this 
	 * 		will cause it to kill itself immediately after sending
	 * 		countdown messages. Note that only P1A and P2A messages
	 * 		(i.e., those sent by Scouts and Commanders to other 
	 * 		processes qualify). If this server does not believe it
	 * 		is the leader, this will do nothing.
	 */
	public void timeBomb(int countdown)
	{
		this.leader.timebomb(countdown);
	}
	
	/**
	 * Prints a state dump of this server. For debugging purposes only.
	 */
	public void whois()
	{
		this.leader.whois();
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
