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
	
	// My server's NetController.
	private NetController network;
	
	// Number of servers in the system.
	private int numServers;
	
	// A list of the Commanders that have been spawned.  When a Commander
	// is spawned, its unique ID will be the index of the element that
	// will contain its reference in this list.
	private ArrayList<Commander> commanders;
	
	// A list of the Scouts that have been spawned.  When a Scout
	// is spawned, its unique ID will be the index of the element that
	// will contain its reference in this list.
	private ArrayList<Scout> scouts;
	
	public Leader(int serverId, int numServers, NetController network)
	{
		this.commanders = new ArrayList<Commander>();
		this.scouts = new ArrayList<Scout>();
		
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
		// TODO redo comments above.
		if (this.serverId == 0)
		{
			this.isCurrentLeader = true;
			
			// Spawn a Scout for the initial ballot.
			Scout firstScout = new Scout(this.currBallot, serverId, network, numServers, this.scouts.size());
			this.scouts.add(firstScout);
		}
		else
		{
			this.isCurrentLeader = false;
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
			
			/*
			System.out.println("pmax_pvals:");
			for (int i = 0; i < pmax_pvals.size(); i++)
			{
				System.out.println("slotNum: " + pmax_pvals.get(i).getSlotNum() + ", " + pmax_pvals.get(i));
			}
			*/
						
			// Perform proposals = proposals (\oplus) pmax(pvals) from the Paper.
			this.proposals = oplus(this.proposals, pmax_pvals);
			
			/*
			System.out.println("proposals after \\oplus:");
			for (int i = 0; i < this.proposals.size(); i++)
			{
				System.out.println("slotNum: " + this.proposals.get(i).getSlotNum() + ", " + this.proposals.get(i));
			}
			*/
			
			// TODO
			// For all <s, p> \in proposals, spawn a Commander.
			for (int i = 0; i < this.proposals.size(); i++)
			{
				Proposal currProposal = this.proposals.get(i);
				
			}
			
			this.active = true;
		}
		
		if (message instanceof Preempted)
		{
			Preempted preempted = (Preempted) message;
			System.out.println("Leader " + this.serverId + " received " + preempted);
			
			// TODO
		}
	
		
		// If we have any Scouts, run their tasks.
		for (int i = 0; i < this.scouts.size(); i++)
		{
			Scout currScout = this.scouts.get(i);
			
			// If we haven't nulled out this Scout, it still has work to do.
			if (currScout != null)
			{
				int scoutReturnValue = currScout.runScout(message);
				
				// If Scout returned its ID, null it out in our list -- it's
				// done with all its tasks and can be garbage collected.
				if (scoutReturnValue != -1)
				{
					System.out.println("Nulled out Scout of ID: " + scoutReturnValue);
					currScout = null;
				}
			}
		}
	}
	
	
	/**
	 * Performs x \oplus y (from the Paper).
	 * 
	 * @param x, a List of Proposals.
	 * @param y, a List of Proposals.
	 * 
	 * @return x \oplus y
	 */
	private static ArrayList<Proposal> oplus(ArrayList<Proposal> x, ArrayList<Proposal> y)
	{
		ArrayList<Proposal> result = new ArrayList<Proposal>();
		
		// Step (1): Add all elements of y.
		result.addAll(y);
		
		// Informally: if there is a mapping for a slot number in y, overwrite
		// the mapping in x, if there is no mapping for a slot number in y,
		// but there is one in x, keep the one in x.
		
		// Step (2): Find all slot number mappings that are in x but not in y.
		// Add them to the result list, then we're done.
		
		// Get slot numbers in x.
		ArrayList<Integer> slotNumsInX = new ArrayList<Integer>();
		for (int i = 0; i < x.size(); i++)
		{
			Integer currSlotNum = x.get(i).getSlotNum();
			
			if (!slotNumsInX.contains(currSlotNum))
			{
				slotNumsInX.add(currSlotNum);
			}
		}
		
		System.out.print("SlotNums in x: ");
		for (int i = 0; i < slotNumsInX.size(); i++)
		{
			System.out.print(slotNumsInX.get(i) + ", ");
		}
		
		// Get slot numbers in y.
		ArrayList<Integer> slotNumsInY = new ArrayList<Integer>();
		for (int i = 0; i < y.size(); i++)
		{
			Integer currSlotNum = y.get(i).getSlotNum();
			if (!slotNumsInY.contains(currSlotNum))
			{
				slotNumsInY.add(currSlotNum);
			}
		}
		
		System.out.print("SlotNums in y: ");
		for (int i = 0; i < slotNumsInY.size(); i++)
		{
			System.out.print(slotNumsInY.get(i) + ", ");
		}
		
		
		// For each slot number in x's elements, check if y has an element
		// with it.  If it doesn't add the Proposal corresponding to this
		// slot number in x's set.
		for (int i = 0; i < slotNumsInX.size(); i++)
		{
			Integer xSlotNum = slotNumsInX.get(i);
			
			if (!slotNumsInY.contains(xSlotNum))
			{
				System.out.println("Slot number NOT in y: " + xSlotNum);
				
				// Add the elements of x with this slot number to the
				// result list.  There should only be one? // TODO //  ///  / / / / / / /// // // /  // // // // // // // // 
				for (int j = 0; j < x.size(); j++)
				{
					Proposal currProposal = x.get(j);
					if (currProposal.getSlotNum() == (int) xSlotNum)
					{
						result.add(currProposal);
					}
				}
			}
		}
		
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


//******************************************************************************
//* TESTING CODE
//******************************************************************************

/*
// Testing.
// Ballot, slotNum, Command
PValue p0 = new PValue(new Ballot(0, 0), 0, new Command(0, 0, "HEY"));
PValue p1 = new PValue(new Ballot(1, 0), 0, new Command(0, 0, "YO"));
PValue p2 = new PValue(new Ballot(0, 1), 0, new Command(0, 0, "SUP"));
PValue p6 = new PValue(new Ballot(1, 1), 0, new Command(0, 0, "MIKE"));			
PValue p3 = new PValue(new Ballot(0, 12), 2, new Command(0, 0, "UPENN"));
PValue p4 = new PValue(new Ballot(45, 1), 2, new Command(0, 0, "YALE"));
PValue p5 = new PValue(new Ballot(0, 1), 68, new Command(0, 0, "UTEXAS"));
			
pvals.add(p0);
pvals.add(p1);
pvals.add(p2);
pvals.add(p3);
pvals.add(p4);
pvals.add(p5);
pvals.add(p6);

this.proposals.add(new Proposal(0, new Command(0, 0, "MIKE_X")));
this.proposals.add(new Proposal(1, new Command(0, 0, "LONGHORN")));
this.proposals.add(new Proposal(345, new Command(0, 0, "DOYOULIFTBRO?")));
this.proposals.add(new Proposal(68, new Command(0, 0, "UTEXAS_EDITTED")));

// proposals \oplus pmax(pvals) should return:
// Slot 0: MIKE
// Slot 2: YALE
// Slot 68: UTEXAS
// Slot 1: LONGHORN
// Slot 345: DOYOULIFTBRO?

*/