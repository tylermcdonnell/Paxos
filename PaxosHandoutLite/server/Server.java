package server;

import java.util.ArrayList;

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

	// This replicas hand-made ID.
	private final int id;
	
	// Let this replica be p. This is p.proposals.
	// A set of <slot number, command> pairs for proposals that the replica
	// has made in the past (initially empty).
	private ArrayList<Proposal> proposals;
	
	// Let this replica be p. This is p.decisions.
	// Another set of <slot number, command> pairs for decided slots
	// (initially empty).
	private ArrayList<Proposal> decisions;
	
	// Let this replica be p. This is p.slot_num.
	// The replica's current slot number (equivalent to the version
	// number of the state, and initially 1). It contains the index
	// of the next slot for which it needs to learn a decision before
	// it can update its copy of the application state.
	private int slot_num;
		
	// Let this replica be p. This is p.state.
	// The replica's copy of the application state.  All replicas start
	// with the same initial application state.
	private State state;
	
	// This server's NetController.
	private NetController network;
	
	
	/**
	 * Constructor.
	 * 
	 * @param id, the ID of this server.
	 * @param nc, the NetController for this server.
	 */
	public Server(int id, NetController nc)
	{
		this.id = id;
		this.proposals = new ArrayList<Proposal>();
		this.decisions = new ArrayList<Proposal>();
		this.slot_num = 1;
		this.state = new State();
		this.network = nc;
	}
	
	@Override
	public void run()
	{
		System.out.println("Server " + this.id + " created.");
		
		while (true)
		{
			// Receive messages from network.
			ArrayList<Message> networkMessages = (ArrayList<Message>) this.network.getReceived();
			
			for (int i = 0; i < networkMessages.size(); i++)
			{
				Message currMessage = networkMessages.get(i);
				
				performReplicaTasks(currMessage);
				performLeaderTasks(currMessage);
				performAcceptorTasks(currMessage);
				
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
	 * Analyze this message, and if it is relevant to a replica, carry out
	 * a task as a replica.
	 * 
	 * @param message, the given message.
	 */
	private void performReplicaTasks(Message message)
	{
		if (message instanceof Request)
		{
			Request request = (Request) message;
			System.out.println("Replica " + this.id + " received " + request);
			propose(request.getCommand());
			
			// TODO
		}
			
			
		if (message instanceof Decision)
		{
			Decision decision = (Decision) message;
			System.out.println("Replica " + this.id + " received " + decision);
			
			// TODO
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
	
	private void propose(Command p)
	{
		// TODO
		
		// (1) Check if there was a decision for this command yet.
		// TODO
		//if (! this.decisions contains p)
		//{
		// (2) Find lowest unused slow number s'.
		// This means finding the lowest unused slot number in the union
		// of this.decisions and this.proposals sets.
		// TODO
		// int lowestSlotNum = getLowestSlotNum();
		
		// (3) Add <s', p> to this replica's set of proposals.
		// TODO
		// proposals.add(new Proposal(lowestSlotNum, p));
		
		// (4) Send <"propose", s', p> to all leaders.
		// TODO
		//}
	}
	
	private void perform(Command p)
	{
		// TODO
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
