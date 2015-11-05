package server;

import java.util.ArrayList;

import client.Command;
import ballot.Ballot;
import ballot.BallotGenerator;
import framework.NetController;
import message.Adopted;
import message.Message;
import message.Preempted;
import message.Proposal;


/**
 * The leader on a given server.
 * @author Mike Feilbach
 *
 */
public class Leader
{
	// If this leader is the current leader.
	// TODO							// TODO					// TODO						// TODO					// TODO
	private boolean isCurrentLeader;
	
	// What server this leader is on.
	private int serverId;
	
	// The current ballot this leader has.
	private Ballot currBallot;
	
	// This will give a leader its next ballot whenever it needs one.
	private BallotGenerator ballotGenerator;
	
	private boolean active;
	private ArrayList<Proposal> proposals;
	
	// Not sure if there will need to be more than one scout, but for now, just have one.
	private Scout scout;
	
	// My server's NetController.
	private NetController network;
	
	// Number of servers in the system.
	private int numServers;
	
	public Leader(int serverId, int numServers, NetController network)
	{
		this.serverId = serverId;
		
		// Set up initial ballot and ballot generator.
		// Pass in the leader ID (the same thing as the server ID, since
		// leaders are located on servers).
		this.ballotGenerator = new BallotGenerator(serverId);
		this.currBallot = this.ballotGenerator.getCurrentBallot();
		
		// Set active to false.
		this.active = false;
		
		// Create an empty set of proposals.
		this.proposals = new ArrayList<Proposal>();
		
		// My server's network.
		this.network = network;
		
		// Number of servers in the system.
		this.numServers = numServers;
		
		// TODO Instead, keep an scouts = ArrayList<Scout>.  For each scout created,
		// give it an ID equal to the index of the element in scouts that the Scout 
		// is located at.  When a scout of ID n returns false, null it out in the list.
		// Keep giving scouts increasing numbers, never need to delete scouts for
		// our purposes.  But, for now, just use one scout.
		if (this.serverId == 0)
		{
			this.isCurrentLeader = true;
			
			// Spawn a Scout for the initial ballot.
			this.scout = new Scout(this.currBallot, serverId, network, numServers);
		}
		else
		{
			this.isCurrentLeader = false;
			this.scout = null;
		}
	}
	
	public void runTasks(Message message)
	{	
		if (message instanceof Proposal)
		{
			Proposal proposal = (Proposal) message;
			System.out.println("Leader " + this.serverId + " received " + proposal);
			
			// TODO
		}
		
		if (message instanceof Adopted)
		{
			Adopted adopted = (Adopted) message;
			System.out.println("Leader " + this.serverId + " received " + adopted);
			
			// pvals from the Paper.
			ArrayList<PValue> pvals = adopted.getPvalues();

			// Perform pmax(pvals) from the Paper.
			// Roughly speaking, for each slot: determine the maximum ballot
			// for this slot, and only include this proposal in the result.
			// "it determines, for each slot, the proposal corresponding to
			// the maximum ballot number in pvals by invoking pmax."
			ArrayList<Proposal> pmax_pvals = pmax(pvals);
			
			// Testing.
			/*
			for (int i = 0; i < pmax_pvals.size(); i++)
			{
				System.out.println("slotNum: " + pmax_pvals.get(i).getSlotNum() + ", " + pmax_pvals.get(i));
			}
			*/
			
			// proposals = proposals (\oplus) pmax(pvals) from the Paper.
			this.proposals = oplus(this.proposals, pmax_pvals);
			
			// TODO
			// For all <s, p> \in proposals, spawn a Commander 
			
			this.active = true;
		}
		
		if (message instanceof Preempted)
		{
			Preempted preempted = (Preempted) message;
			System.out.println("Leader " + this.serverId + " received " + preempted);
			
			// TODO
		}
	
		
		// If we have a scout out, run its tasks.
		if (this.scout != null)
		{
			boolean stillRunning = this.scout.runScout(message);
			
			if (stillRunning == false)
			{
				// Don't run anymore.
				// TODO use list instead. See comments in an above TODO.
				this.scout = null;
			}
		}
	}
	
	
	/**
	 * Performs x \oplus y (from the Paper).
	 * 
	 * @param x
	 * @param y
	 * 
	 * @return x \oplus y
	 */
	private static ArrayList<Proposal> oplus(ArrayList<Proposal> x, ArrayList<Proposal> y)
	{
		ArrayList<Proposal> result = new ArrayList<Proposal>();
		
		// Informally: if there is a mapping for a slot number in y, overwrite
		// the mapping in x, if there is no mapping for a slot number in y,
		// but there is one in x, keep the one in x.
		
		// Step (1): Find all slot number mappings that are in x but not in y.
		// Add them to the result list.
		
		//for (int i = 0; )
		
		// TODO
		return result;
	}
	
	
	/**
	 * Does the pmax function as described in the Paper.  Returns a list
	 * of Proposals, which include, for each slot in pvals, the proposal
	 * corresponding to the highest ballot.
	 * 
	 * @param pvals
	 * 
	 * @return a list of Proposals, which include, for each slot in pvals,
	 * the proposal corresponding to the highest ballot.
	 */
	private ArrayList<Proposal> pmax(ArrayList<PValue> pvals)
	{
		ArrayList<Proposal> pmax_pvals = new ArrayList<Proposal>();
		
		// Find out all the slots in pvals.
		ArrayList<Integer> slots = new ArrayList<Integer>();
		for (int i = 0; i < pvals.size(); i++)
		{
			if (!slots.contains(pvals.get(i).getSlotNumber()))
			{
				slots.add(pvals.get(i).getSlotNumber());
			}
		}
		
		// For each slot, find the max proposal (max ballot).
		for (int i = 0; i < slots.size(); i++)
		{
			// The best pvalue for this slot number.
			PValue bestYet = null;
			
			// The slot number we will look at this iteration.
			int slotNumber = slots.get(i);
			
			// Testing.
			//System.out.println("Considering slot number: " + slotNumber);
			
			for (int j = 0; j < pvals.size(); j++)
			{
				// The proposal we are looking at.
				PValue pval = pvals.get(j);
				
				// If this pvalue has to do with the current slot number.
				if (pval.getSlotNumber() == slotNumber)
				{
					// It does, consider it.
					if (bestYet == null)
					{
						// This is the first one we've seen -- take it.
						bestYet = pval;
					}
					else
					{
						// This isn't the first one we've seen.
						if (pval.getBallot().greaterThan(bestYet.getBallot()))
						{
							bestYet = pval;
						}
					}
				}
			} // End inner for loop.
			
			// We found our best pvalue for this slot number.
			// Add it to the list.
			pmax_pvals.add(new Proposal(bestYet.getSlotNumber(), bestYet.getCommand()));
			
		} // End outer for loop.
		
		return pmax_pvals;
	}
	
	
	
	/**
	 * Returns this leader's next ballot.  Also updates this leader's 
	 * current ballot variable.
	 * 
	 * @return this leader's next ballot.
	 */
	private Ballot getNextBallot()
	{
		Ballot nextBallot = this.ballotGenerator.getNextBallot();
		this.currBallot = nextBallot;
		
		return nextBallot;
	}
	
}
