package server;

import java.util.ArrayList;

import client.Command;
import message.Decision;
import message.Message;
import message.Proposal;
import message.Request;


/**
 * The replica on a given server.
 * @author Mike Feilbach
 *
 */
public class Replica
{
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
	
	// What server this replica is on.
	private int serverId;
	
	public Replica(int serverId)
	{
		this.proposals = new ArrayList<Proposal>();
		this.decisions = new ArrayList<Decision>();
		this.slot_num = 1;
		this.state = new State();
		this.serverId = serverId;
	}
	
	
	/**
	 * Analyze this message, and if it is relevant to a replica, carry out
	 * a task as a replica.
	 * 
	 * @param message, the given message.
	 */
	public void runTasks(Message message)
	{
		// IN PAPER: case <request, p>
		if (message instanceof Request)
		{
			Request request = (Request) message;
			System.out.println("Replica " + this.serverId + " received " + request);
			propose(request.getCommand());
			
			// TODO
		}
			
		// IN PAPER: case <decision, s, p>
		if (message instanceof Decision)
		{
			Decision decision = (Decision) message;
			System.out.println("Replica " + this.serverId + " received " + decision);
			
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
}
