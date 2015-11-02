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
	
	// Let this replica be p. This is p.proposals.
	// A set of <slot number, command> pairs for proposals that the replica
	// has made in the past (initially empty).
	private ArrayList<Proposal> proposals;
	
	// Let this replica be p. This is p.decisions.
	// Another set of <slot number, command> pairs for decided slots
	// (initially empty).
	private ArrayList<Decision> decisions;
	
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
		this.proposals = new ArrayList<Proposal>();
		this.decisions = new ArrayList<Decision>();
		this.slot_num = 1;
		this.state = new State();
		this.network = nc;
		this.serverReceiveQueue = serverReceiveQueue;
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
		// IN PAPER: case <request, p>
		if (message instanceof Request)
		{
			Request request = (Request) message;
			System.out.println("Replica " + this.id + " received " + request);
			propose(request.getCommand());
			
			// TODO
		}
			
		// IN PAPER: case <decision, s, p>
		if (message instanceof Decision)
		{
			Decision decision = (Decision) message;
			System.out.println("Replica " + this.id + " received " + decision);
			
			// Add to the local list of decisions, only if we don't already
			// have it.
			if (!this.decisions.contains(decision))										// Do we have to implement an equals
																						// method for Decision to use contains()
																						// properly? I think yes.
			{
				this.decisions.add(decision);
			}
			
			// Keep performing decisions in slot_num as long as we can.
			// Note: this decision may have to do with a higher slot than slot_num,
			// since decisions can come from other replicas.  In fact, we may ave
			// received a decision to a command we have never seen before.
			while (hasDecisionWithSlotNum(this.slot_num) != null)
			{
				// This is the decision that has slot_num.
				Decision d = hasDecisionWithSlotNum(this.slot_num);
				
				// Check if there are any proposals in proposals which
				// have the same slot number as this decision, but are
				// not equal to this decision (the command is different).
				// If so, re-propose this proposal so it can fill a higher
				// slot in the future.
				//if ()
				//{
				//	propose();
				//}
				// TODO
				
				// Perform the command of the decision.
				perform(d.getProposal().getCommand());
			}
			
			// TODO
		}
	}
	
	
	/**
	 * Returns the decision in this server's replica's list of decisions
	 * which has slot_num equal to slotNum.  Else, if not found, returns
	 * null.
	 * 
	 * @param slotNum, the slotNum we are looking for.
	 * 
	 * @return the decision in this server's replica's list of decisions
	 * which has slot_num equal to slotNum.  Else, if not found, returns
	 * null.
	 */
	private Decision hasDecisionWithSlotNum(int slotNum)
	{
		// Traverse decisions looking for the given slot_num.
		// slot_num is unique to each decision. 
		for (int i = 0; i < this.decisions.size(); i++)
		{
			Decision tempDecision = this.decisions.get(i);
			
			if (tempDecision.getProposalSlotNum() == this.slot_num)
			{
				return tempDecision;
			}
		}
		
		return null;
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
	
	
	/**
	 * // TODO
	 * @param p
	 */
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
	
	
	/**
	 * // TODO
	 * @param p
	 */
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
