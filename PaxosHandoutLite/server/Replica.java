package server;

import java.util.ArrayList;

import client.Command;
import framework.NetController;
import message.Decision;
import message.Message;
import message.Proposal;
import message.Request;
import message.Response;


/**
 * The replica on a given server.
 * @author Mike Feilbach
 *
 */
public class Replica
{
	// Let this replica be p. This is p.proposals.
	// This replica's set of <slot number, command> pairs for proposals 
	// that this replica has made in the past (initially empty).
	private ArrayList<Proposal> proposals;
	
	// Let this replica be p. This is p.decisions.
	// This replica's set of <slot number, command> pairs for decided slots
	// (initially empty).
	private ArrayList<Decision> decisions;
	
	// Let this replica be p. This is p.slot_num.
	// This replica's current slot number (equivalent to the version
	// number of the state, and initially 1). It contains the index
	// of the next slot for which it needs to learn a decision before
	// it can update its copy of the application state.
	private int slot_num;
		
	// Let this replica be p. This is p.state.
	// This replica's copy of the application state.  All replicas start
	// with the same initial application state.
	private State state;
	
	// What server this replica is on.
	private int serverId;
	
	// Number of servers in the system.
	private int numServers;
	
	// My server's NetController.
	private NetController network;
	
	// Number of clients in the system
	private int numClients;
	
	public Replica(int serverId, int numServers, NetController network, int numClients)
	{
		this.numClients = numClients;
		
		this.proposals = new ArrayList<Proposal>();
		this.decisions = new ArrayList<Decision>();
		
		// Number of servers in the system.
		this.numServers = numServers;
		
		// The next slot to fill at initialization is 1.
		// TSM: I think we want this to be 0. The project description
		// states that printChatLog should print out the zero-indexed
		// Paxos sequence number of a message in the chat log. If we
		// start with slot 1 instead of slot 0, the output sequence
		// numbers will be incorrect.
		// MIKE: changed back to 1.
		this.slot_num = 1;
		
		// All replicas start with the same initial state.
		this.state = new State();
		
		this.serverId = serverId;
		this.network = network;
	}
	
	
	/**
	 * Analyze this message, and if it is relevant to a replica, carry out
	 * a task as a replica.
	 * 
	 * @param message, the given message.
	 */
	public void runTasks(Message message)
	{
		//**********************************************************************
		//* Replica received Request from a client.
		//**********************************************************************
		if (message instanceof Request)
		{
			// IN PAPER: case <request, p>
			Request request = (Request) message;
			System.out.println("Replica " + this.serverId + " received " + request);
			
			propose(request.getCommand());
		}
			
		
		//**********************************************************************
		//* Replica received Decision from a Commander.
		//**********************************************************************
		if (message instanceof Decision)
		{
			// IN PAPER: case <decision, s, p>
			Decision decision = (Decision) message;
			System.out.println("Replica " + this.serverId + " received " + decision);
			
			// Add to the local list of decisions, only if we don't already
			// have it.  Decisions can arrive multiple times.
			if (!this.decisions.contains(decision))
			{
				this.decisions.add(decision);
			}
			
			// Keep performing decisions in slot_num as long as we can.
			// Note: this decision may have to do with a higher slot than slot_num,
			// since decisions can come from other replicas.  In fact, we may have
			// received a decision to a command we have never seen before.
			while (hasDecisionWithSlotNum(this.slot_num) != null)
			{
				// This is the decision that has slot_num (which we will
				// perform).
				Decision d = hasDecisionWithSlotNum(this.slot_num);
				
				// Check if there are any proposals in proposals which
				// have the same slot number as this decision, but are
				// not equal to this decision (the command is different).
				// If so, re-propose this proposal so it can fill a higher
				// slot in the future.
				// TODO there can only be one max, right?  I think so.
				
				// Get all the proposals for this slot number.
				ArrayList<Proposal> proposalsForSlotNum = new ArrayList<Proposal>();
				for (int i = 0; i < this.proposals.size(); i++)
				{
					Proposal currProposal = this.proposals.get(i);
					
					if (currProposal.getSlotNum() == this.slot_num)
					{
						proposalsForSlotNum.add(currProposal);
					}
				}
				
				// TODO: This loop should only execute once if ever, since
				// there should only be one proposal for any given slot in
				// proposals.  Modify this later on.
				for (int i = 0; i < proposalsForSlotNum.size(); i++)
				{
					Command currCommand = proposalsForSlotNum.get(i).getCommand();
					Command decisionCommand = decision.getProposal().getCommand();
					if (!currCommand.equals(decisionCommand))
					{
						propose(currCommand);
					}
				}
				
				// Perform the command of the decision.
				perform(d.getProposal().getCommand());
			}
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
	 * Propose the command p.  This is the propose(p) replica function
	 * from the paper.
	 * 
	 * @param p, the Command being proposed.
	 */
	private void propose(Command p)
	{	
		// (1) Check if there was a decision for this command yet.
		if (!isDecisionWithCommand(p))
		{
			// (2) Find lowest unused slow number s'.
			// This means finding the lowest unused slot number in the union
			// of this.decisions and this.proposals sets.
			int lowestSlotNum = getLowestSlotNum();
		
			//System.out.println("LOWEST SLOT NUMBER: " + lowestSlotNum + " for: " + p);
		
			// (3) Add <s', p> to this replica's set of proposals.  Since this proposal
			// has a unique slot number, we can just add it -- we don't need to take
			// the union necessarily.
			Proposal newProposal = new Proposal(lowestSlotNum, p);
			this.proposals.add(newProposal);
		
			// (4) Send <"propose", s', p> to all leaders.
			sendProposalToAllLeaders(newProposal);
		}
	}
	
	
	/**
	 * Send the given proposal to all leaders.
	 * 
	 * @param p, the given proposal.
	 */
	private void sendProposalToAllLeaders(Proposal p)
	{
		System.out.println("Replica " + this.serverId + " sending to leaders: " + p);
		for (int i = 0; i < this.numServers; i++)
		{
			this.network.sendMsgToServer(i, p);
		}
	}
	
	
	/**
	 * Returns the lowest unused slot number (in the union of this
	 * replica's proposals decisions sets).
	 * 
	 * @return the lowest unused slot number (in the union of this
	 * replica's proposals decisions sets).
	 */
	private int getLowestSlotNum()
	{
		// Convert all Decisions into proposals, and merge the proposals
		// and decisions sets (create a master set of pure proposals).
		ArrayList<Proposal> masterSet = new ArrayList<Proposal>();
		
		// Add all Proposals in the proposals set.
		masterSet.addAll(this.proposals);
		
		// Add all Decisions in the decisions set.  First, convert all
		// Decisions to Proposals.
		for (int i = 0; i < this.decisions.size(); i++)
		{
			Decision d = this.decisions.get(i);
			Proposal p = d.getProposal();
			masterSet.add(p);
		}
		
		// Find the lowest slot number and return it.
		int lowestSlotNumber = 1;
		while (true)
		{
			boolean found = false;
			
			// Does the master set contain this lowestSlotNumber?
			for (int i = 0; i < masterSet.size(); i++)
			{
				if (masterSet.get(i).getSlotNum() == lowestSlotNumber)
				{
					// We found it. Stop looking. Continue searching
					// with the next slot number.
					found = true;
					break;
				}
			}
			
			if (found)
			{
				lowestSlotNumber++;
			}
			else
			{
				break;
			}
		}
		
		return lowestSlotNumber;
	}
	
	
	/**
	 * Returns true iff there is a Decision with the given Command in
	 * this replica's decision set.
	 * 
	 * @param command, the given Command.
	 * 
	 * @return true iff there is a Decision with the given Command in
	 * this replica's decision set.
	 */
	private boolean isDecisionWithCommand(Command command)
	{
		for (int i = 0; i < this.decisions.size(); i++)
		{
			Decision currDecision = this.decisions.get(i);
			Command currCommand = currDecision.getProposal().getCommand();
			if (currCommand.equals(command))
			{
				return true;
			}
		}
		
		return false;
	}
	
	
	/**
	 * Perform the command p.  This is the perform(p) replica function
	 * from the paper.
	 * 
	 * @param p, the command being performed.
	 */
	private void perform(Command p)
	{
		// If there exists a slot we have already executed a command for, and
		// the command executed for that slot was the command p, don't execute
		// any commands.  Why can this happen?  Different replicas can propose
		// the same command for different slots, and thus the same command
		// may be decided more than once.
		for (int i = 0; i < this.decisions.size(); i++)
		{
			Decision currDecision = this.decisions.get(i);
			
			if (currDecision.getProposal().getCommand().equals(p))
			{
				if (currDecision.getProposal().getSlotNum() < this.slot_num)
				{
					// Skip this command, we've already done it.
					this.slot_num++;
					return;
				}
			}
		}
		
		// Else, we have not performed this command yet. Let's do it.
		
		// Update state.
		StateEntry result = new StateEntry(p, this.slot_num);
		this.state.addToState(result);
		
		// Update slot number to do next.
		this.slot_num++;
		
		// Send clients the update.
		Response response = new Response(p.getCommandId(), result);
		
		for (int i = 0; i < this.numClients; i++)
		{
			this.network.sendMsgToClient(i, response);
		}
	}
}
