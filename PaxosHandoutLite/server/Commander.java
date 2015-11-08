package server;

import java.util.ArrayList;

import message.Adopted;
import message.Decision;
import message.Message;
import message.P1b;
import message.P2a;
import message.P2b;
import message.Preempted;
import message.Proposal;
import ballot.Ballot;
import framework.NetController;


/**
 * A commander in the Paxos algorithm, as described in the paper 
 * "Paxos Made Moderately Complex."
 * @author Mike Feilbach
 *
 */
public class Commander
{
	// This Commander's unique ID in the eyes of the leader who spawned him.
	private int uniqueId;
	
	// Initialized with IDs of all acceptors.
	private ArrayList<Integer> waitFor;
		
	// This server's NetController.
	private NetController network;
		
	// The number of servers in the network.
	private int numServers;
	
	// This commander's pvalue.
	private PValue pvalue;
		
	private int myLeaderId;
	
	// Leader's timebomb
	private Timebomb timebomb;
		
	public Commander(int myLeaderId, NetController network, int numServers, PValue pvalue, int uniqueId, Timebomb timebomb)
	{
		this.uniqueId = uniqueId;
		this.network = network;
		this.numServers = numServers;
		this.myLeaderId = myLeaderId;
		this.pvalue = pvalue;
		
		// Add all acceptor IDs to the waitFor list.  This assumes that
		// the number of acceptors = number of servers.
		waitFor = new ArrayList<Integer>();
		for (int i = 0; i < numServers; i++)
		{
			this.waitFor.add(i);
		}
		
		// Send p2a to all acceptors.  There is an acceptor on every server,
		// including ours!
		P2a p2a = new P2a(this.myLeaderId, PValue.deepCopyPValue(pvalue));
		for (int i = 0; i < numServers; i++)
		{
			this.network.sendMsgToServer(i, p2a);
			// TSM: This is a message to another server that counts as a timebomb tick.
			timebomb.tick();
		}
	}
	
	
	/**
	 * This Commanders's run method.  When it is done completing its tasks,
	 * the method will return the Commander's unique ID, at which point this 
	 * method should not be called again for this given Commander object.
	 * 
	 * @param message, the message to process.
	 * 
	 * @return -1, else the Commander's unique ID, at which point this 
	 * method should not be called again for this given Commander object.
	 */
	public int runCommander(Message message)
	{
		// Commanders only listen for p2b messages.
		if (message instanceof P2b)
		{
			System.out.println("Commander " + this.myLeaderId + " got p2b");
			
			P2b p2b = (P2b) message;
			
			// If the ballot returned from the acceptor is the one I gave him 
			// in p2a.
			if (p2b.getBallot().equals(this.pvalue.getBallot()))
			{
				// Testing.
				/*
				System.out.print("commander: OLD WAIT FOR LIST: ");
				for (int i = 0; i < this.waitFor.size(); i++)
				{
					System.out.print(this.waitFor.get(i) + " ");
				}
				System.out.println();
				*/
				
				// Take this acceptor's ID off of the wait for list.
				// Make sure we remove the Integer object, not the element
				// at the given index (We use Integer rather than int to
				// accomplish this.)
				this.waitFor.remove(new Integer(p2b.getAcceptorId()));
				
				// Testing.
				/*
				System.out.print("commander: NEW WAIT FOR LIST: ");
				for (int i = 0; i < this.waitFor.size(); i++)
				{
					System.out.print(this.waitFor.get(i) + " ");
				}
				System.out.println();
				*/
				
				// If |waitFor| < (|acceptors| / 2).  In other words, do we 
				// have majority of acceptors saying they like our ballot?
				// Make sure we cast to doubles first, we don't want truncation.
				// (Or does this matter?  Either way, using doubles is safe.)
				if (((double) this.waitFor.size()) < ((double) this.numServers) / 2)
				{
					// The majority of the acceptors ACCEPTED our ballot.
					// (This is the time at which our proposal is for sure decided).
					// Send to all replicas that this command was taken for this slot
					// number.
					// TODO Are the above comments correct? I think yes.
					Proposal decisionProposal = new Proposal(this.pvalue.getSlotNumber(), this.pvalue.getCommand());
					Decision decision = new Decision(decisionProposal);
					
					// send to all replicas: <decision, s, p> (from the Paper).
					for (int i = 0; i < this.numServers; i++)
					{
						this.network.sendMsgToServer(i, decision);
					}
					
					// This Commander is done with its tasks.
					// This is exit() in the Paper.
					return this.uniqueId;
				}
			}
			else
			{
				// An acceptor returned a ballot which did not match.
				Preempted preempted = new Preempted(Ballot.deepCopyBallot(this.pvalue.getBallot()));
				
				// send to leader: <preempted, b'>
				this.network.sendMsgToServer(this.myLeaderId, preempted);
				
				// This Commander is done with its tasks.
				// This is exit() in the Paper.
				return this.uniqueId;
			}
		}
		
		// This Commander is not done with its tasks yet.
		return -1;
	}
}
