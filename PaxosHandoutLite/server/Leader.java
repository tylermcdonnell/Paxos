package server;

import java.util.ArrayList;

import client.Command;
import ballot.Ballot;
import ballot.BallotGenerator;
import framework.NetController;
import message.Adopted;
import message.HeartBeat;
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
	// (NEW) Current leader ID from the view of this leader.
	private int currentLeaderId;
	
	// Heart beat generator (NEW)
	private HeartBeatGenerator hbg;
	
	// If this leader is the current leader.
	//private boolean isCurrentLeader;
	
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
	
	// This is used to implement the Master class test hook timeBombLeader.
	// After the leader has sent the specified number of qualifying messages,
	// it will immediately die.
	private Timebomb timebomb;
	
	// Is this leader currently recovering?
	public boolean isRecovering;
	
	// If a scout did not receive enough responses to send the adopted
	// or the preempted message within the its timeout, we must wait
	// until we find (by heartbeat) that a majority of servers are up,
	// and then restart the scout.
	private boolean scoutWaiting;

	// PValues of those commanders who are waiting to be re-spawned because they
	// timed out.
	private ArrayList<PValue> commandersWaiting;
	
	// Copy of the most recent list of dead processes from the HBG. 
	// Note: Debugging purposes only.
	private ArrayList<Integer> heartbeatSnapshot;
	
	public Leader(int serverId, int numServers, NetController network, boolean isRecovering)
	{
		this.scoutWaiting = false;
		this.commandersWaiting = new ArrayList<PValue>();
		
		this.isRecovering = isRecovering;
		
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
		
		// Initialize timebomb infrastructure.
		this.timebomb = new Timebomb();
		
		if (isRecovering)
		{
			// Heart beat period = .4 seconds.
			// Update system view period = 2 seconds.
			this.hbg = new HeartBeatGenerator(serverId, network, numServers, 400, 2000);
			
			ArrayList<Integer> deadProcesses = null;
			
			while (deadProcesses == null)
			{
				// Telling others our view of the current leader is 0 will
				// not adversely affect other processes.
				
				// TODO Right?
				deadProcesses = this.hbg.beatAndAnalyze(0);
			}
			
			// Loop breaks when returned value was not null.  We now have the
			// list of dead processes in the system.
			
			// We can now determine, from the heart beats, who to set as our current
			// leader.  It will not be us, because we are assuming that a server
			// can die and recover within a single update heart beat period.
			//this.currentLeaderId = ???
			
			// Let it be known that we are done recovering, so allClear can
			// stop waiting on us.
			this.isRecovering = false;
			
			return;
		}
		
		// On start up, leader with ID 0 is the current leader.
		this.currentLeaderId = 0;
		if (this.isCurrentLeader())
		{
			this.leaderInitialization();
		}
		
		// Heart beat period = .4 seconds.
		// Update system view period = 2 seconds.
		this.hbg = new HeartBeatGenerator(serverId, network, numServers, 400, 2000);
	}
	
	
	public void runTasks(Message message)
	{	
		//**********************************************************************
		//* See if we need to send another heart beat.  If so, send it.  Also
		//* check if an update period has passed.  If so, we will receive a
		//* list of the leaders we believe to be dead now.
		//**********************************************************************
		ArrayList<Integer> deadLeaderIds = this.hbg.beatAndAnalyze(this.currentLeaderId);
		
		// If the current update period has passed, we will receive a list of
		// leaders we believe to be dead now.
		if (deadLeaderIds != null)
		{
			// Update debug copy of dead list.
			this.heartbeatSnapshot = deadLeaderIds;
			
			//******************************************************************
			//* If majority of servers are up, and scout timed out, restart
			//* scout.
			//******************************************************************
			if (((double) deadLeaderIds.size()) < ((double) this.numServers / 2))
			{
				if (this.scoutWaiting)
				{
					this.scoutWaiting = false;
					
					Scout newScout = new Scout(this.currBallot, serverId, network, numServers, this.scouts.size(), this.timebomb);
					this.scouts.add(newScout);
					
					System.out.println("Leader " + this.serverId + " RESPAWNED a Scout.");
				}
				
				if (this.commandersWaiting.size() > 0)
				{
					for (int i = 0; i < this.commandersWaiting.size(); i++)
					{
						PValue newPValue = this.commandersWaiting.get(i);
						Commander newCommander = new Commander(this.serverId, this.network, this.numServers, newPValue, this.commanders.size(), this.timebomb);
						this.commanders.add(newCommander);
						
						System.out.println("Leader " + this.serverId + " created Commander for " + newPValue);
					}
					
					// Clear the list, we have revived all commanders!
					this.commandersWaiting = new ArrayList<PValue>();
				}
				
			}
			
			// Testing.
			/*
			System.out.print("Leader " + this.serverId + " dead leaders detected: ");
			for (int i = 0; i < deadLeaderIds.size(); i++)
			{
				System.out.print(deadLeaderIds.get(i) + ", ");
			}
			System.out.println();
			*/
			
			//****************************************
			// Leader Election logic for if current leader dies.
			//****************************************
			// If we observe that the process we believe to be the leader is dead,
			// we choose N + 1 as the new leader. Note that this is the raw value
			// and not the value mod N (actual process ID)
			if (deadLeaderIds.contains(this.currentLeaderId))
			{
				this.currentLeaderId += 1;
				// TSM: If we have just become leader, send out first scouts.
				if (this.isCurrentLeader())
				{
					leaderInitialization();
				}
			}
		}
		
		//**********************************************************************
		//* Leader received HeartBeat from another leader, or itself.
		//**********************************************************************
		if (message instanceof HeartBeat)
		{
			// If received a heart beat, let the generator know.
			HeartBeat hb = (HeartBeat) message;
			this.hbg.addBeat(hb);
			
			//*****************************************************************
			//* Leader election logic for if someone has a higher current ID
			//*****************************************************************
			// Here, we take the max of our own belief about the current leader
			// and everyone else's belief about the current leader. We use the
			// raw (not taken mod N) value so that we can use simple max() logic.
			int oldLeader = this.currentLeaderId;
			this.currentLeaderId = Math.max(this.currentLeaderId, hb.getCurrentLeaderId());
			if (oldLeader != this.currentLeaderId && this.isCurrentLeader())
			{
				// TSM: If we have just become leader, send out first scouts.
				leaderInitialization();
			}
		}
		
		
		//**********************************************************************
		//* Leader received a Proposal from a Replica.
		//**********************************************************************
		if (message instanceof Proposal)
		{
			Proposal proposal = (Proposal) message;
			System.out.println("Leader " + this.serverId + " received " + proposal);
			
			// Find out if we have a proposal for this slot number already.
			// If so, we can't consider this new proposal.
			boolean existsCmdForThisSlot = false;
			for (int i = 0; i < this.proposals.size(); i++)
			{
				Proposal existingProposal = this.proposals.get(i);
				
				if (existingProposal.getSlotNum() == proposal.getSlotNum())
				{
					existsCmdForThisSlot = true;
				}
			}
			
			// If there is no command for this slot number already.
			if (existsCmdForThisSlot == false)
			{
				// Take union of proposals and this new proposal.
				// This need not be a union, since we know that there is no
				// proposal in the leader's proposals set with the slot number 
				// of this new proposal.  So, just add it.
				
				// TODO: is this correct? Yes, I think so.
				
				//if (!this.proposals.contains(proposal))
				//{
				this.proposals.add(proposal);
				System.out.println("Leader " + this.serverId + " added Proposal: " + proposal);
				//}
				
				
				//**************************************************************
				//* Even if we are not current leader, rack up the proposal set
				//* so when we do become leader (if we do), we know which
				//* proposals to run through, especially in the case:
				//* Current leader dies, message is sent by client (but only
				//* the current leader can add it to their proposal set, so
				//* clearly no one does), then I become leader, and don't have
				//* it in my proposal set.  Bad news => make this fix.
				//**************************************************************
				if (!this.isCurrentLeader())
				{
					return;
				}
				
				if (this.active)
				{
					// Create a commander for this new proposal.
					PValue newPValue = new PValue(this.currBallot, proposal.getSlotNum(), proposal.getCommand());
					
					Commander newCommander = new Commander(this.serverId, this.network, this.numServers, newPValue, this.commanders.size(), this.timebomb);
					this.commanders.add(newCommander);
					
					System.out.println("Leader " + this.serverId + " created Commander for " + newPValue);
				}
			}
		}
		
		//**********************************************************************
		//* Do not run the below tasks if we are not the current leader.
		//**********************************************************************
		if (!this.isCurrentLeader())
		{
			return;
		}
		
		//**********************************************************************
		//* Leader received Adopted from a Scout.
		//**********************************************************************
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
			
			System.out.println("Proposals");
			
			
			System.out.println("proposals after \\oplus:");
			for (int i = 0; i < this.proposals.size(); i++)
			{
				System.out.println("slotNum: " + this.proposals.get(i).getSlotNum() + ", " + this.proposals.get(i));
			}
			
			
			// For all <s, p> \in proposals, spawn a Commander.
			for (int i = 0; i < this.proposals.size(); i++)
			{
				Proposal currProposal = this.proposals.get(i);
				PValue newPValue = new PValue(adopted.getBallot(), currProposal.getSlotNum(), currProposal.getCommand());
				
				Commander newCommander = new Commander(this.serverId, this.network, this.numServers, newPValue, this.commanders.size(), this.timebomb);
				this.commanders.add(newCommander);
				
				System.out.println("Leader " + this.serverId + " created Commander for " + newPValue);
			}
			
			this.active = true;
		}
		
		
		//**********************************************************************
		//* Leader received Preempted from a Scout or Commander.
		//**********************************************************************
		if (message instanceof Preempted)
		{
			Preempted preempted = (Preempted) message;
			System.out.println("Leader " + this.serverId + " received " + preempted);
			
			// The Ballot which we were preempted with.
			Ballot preemptingBallot = preempted.getBallot();
			
			// TSM: If the the process that pre-empted us has a greater leader ID, resign.
			if (preemptingBallot.getLeaderId() > this.currentLeaderId)
			{
				this.currentLeaderId = preemptingBallot.getLeaderId();
				this.active = false;
				return;
			}
			else if (preemptingBallot.greaterThan(this.currBallot))
			{
				this.active = false;
				
				// Generate new ballots until we are greater than this one.
				boolean notGreater = true;
				Ballot newBallot;
				
				while (notGreater)
				{
					newBallot = this.getNextBallot();
					
					if (newBallot.greaterThan(preemptingBallot))
					{
						notGreater = false;
					}
				}
				
				// We now have a ballot larger than the ballot we were preempted
				// with.  Spawn a Scout with this new ballot.
				Scout newScout = new Scout(this.currBallot, serverId, network, numServers, this.scouts.size(), this.timebomb);
				this.scouts.add(newScout);
			}
		}
	
		//**********************************************************************
		//* If we have any working Scouts, run their tasks.
		//**********************************************************************
		for (int i = 0; i < this.scouts.size(); i++)
		{
			Scout currScout = this.scouts.get(i);
			
			// If we haven't nulled out this Scout, it still has work to do.
			if (currScout != null)
			{
				int scoutReturnValue = currScout.runScout(message);
				
				if (scoutReturnValue == -2)
				{
					// This scout timed out.  Re-run him again once we know that a majority
					// of servers are back up.
					this.scoutWaiting = true;
					
					System.out.println("Scout " + this.serverId + " timed out.");
					
					this.scouts.add(i, null);
					this.scouts.remove(i + 1);
				}
				
				// If Scout returned its ID, null it out in our list -- it's
				// done with all its tasks and can be garbage collected.
				if (scoutReturnValue != -1)
				{
					// Testing.
					//System.out.println("Nulled out Scout of ID: " + scoutReturnValue);
					
					this.scouts.add(i, null);
					this.scouts.remove(i + 1);
				}
			}
		}
		
		//**********************************************************************
		//* If we have any working Commanders, run their tasks.
		//**********************************************************************
		for (int i = 0; i < this.commanders.size(); i++)
		{
			Commander currCommander = this.commanders.get(i);
			
			// If we haven't nulled out this Commander, it still has work to do.
			if (currCommander != null)
			{
				CommanderReturnValue commanderReturnValue = currCommander.runCommander(message);
				
				if (commanderReturnValue.getReturnValue() == -2)
				{
					// This scout timed out.  Re-run him again once we know that a majority
					// of servers are back up.
					this.commandersWaiting.add(commanderReturnValue.getPValue());
					
					System.out.println("Commander " + this.serverId + " timed out.");
					
					this.commanders.add(i, null);
					this.commanders.remove(i + 1);
				}
				
				
				// If Commander returned its ID, null it out in our list -- it's
				// done with all its tasks and can be garbage collected.
				if (commanderReturnValue.getReturnValue() != -1)
				{
					// Testing.
					//System.out.println("Leader " + this.serverId + " nulled out Commander of ID: " + commanderReturnValue);
					
					this.commanders.add(i, null);
					this.commanders.remove(i + 1);
				}
			}
		}
	}
	
	/**
	 * This should be called when a process becomes a leader for the first
	 * time, either during initialization of process 0, or when another 
	 * process is elected leader. Sends out initial scouts.
	 */
	public void leaderInitialization()
	{
		// Spawn a Scout for the initial ballot.
		Scout firstScout = new Scout(this.currBallot, serverId, network, numServers, this.scouts.size(), this.timebomb);
		this.scouts.add(firstScout);
		
		System.out.println("Leader " + this.serverId + " is now current leader -- spawned Scout.");
	}
	

	/** 
	 * Will self destruct (kill run thread) after countdown.
	 * @param countdown
	 * 			Kill self after sending this number of Paxos protocol
	 * 			messages.
	 */
	public void timebomb(int countdown)
	{
		if (this.isCurrentLeader())
		{
			this.timebomb.set(countdown);	
		}
	}
	
	/**
	 * @return True if this process believes it is the current leader.
	 */
	public boolean isLeader()
	{
		return this.isCurrentLeader();
	}
	
	/**
	 * Prints out a summary of this process' state. This should only
	 * be used for debug purposes. :)
	 */
	public void whois()
	{
		System.out.println("Process " + this.serverId + " summary.");
		System.out.println("--------------------------------------");
		System.out.println("Leader: " + this.currentLeaderId);
		for (int i = 0; i < this.numServers; i++)
		{
			if (this.heartbeatSnapshot.contains(i))
			{
				System.out.println("Server " + i + ": " + "DEAD");
			}
			else
			{
				System.out.println("Server " + i + ": " + "ALIVE");
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
		
		// Testing.
		/*
		System.out.print("SlotNums in x: ");
		for (int i = 0; i < slotNumsInX.size(); i++)
		{
			System.out.print(slotNumsInX.get(i) + ", ");
		}
		*/
		
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
		
		// Testing.
		/*
		System.out.print("SlotNums in y: ");
		for (int i = 0; i < slotNumsInY.size(); i++)
		{
			System.out.print(slotNumsInY.get(i) + ", ");
		}
		*/
		
		
		// For each slot number in x's elements, check if y has an element
		// with it.  If it doesn't add the Proposal corresponding to this
		// slot number in x's set.
		for (int i = 0; i < slotNumsInX.size(); i++)
		{
			Integer xSlotNum = slotNumsInX.get(i);
			
			if (!slotNumsInY.contains(xSlotNum))
			{
				// Testing.
				//System.out.println("Slot number NOT in y: " + xSlotNum);
				
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
	
	
	/**
	 * If the current leader ID given is equal to this leader's ID,
	 * set it as current leader.  Else, set it as not the current
	 * leader.
	 * 
	 * @param leaderId, the current leader ID.
	 */
	/*public void setCurrentLeader(int leaderId)
	{
		this.currentLeaderId = leaderId;
		
		if (leaderId == this.serverId)
		{
			this.isCurrentLeader = true;
		}
		else
		{
			this.isCurrentLeader = false;
		}
	}*/
	
	
	/**
	 * Returns true iff this leader is the current leader.
	 * 
	 * @return true iff this leader is the current leader.
	 */
	private boolean isCurrentLeader()
	{
		return ((this.currentLeaderId % this.numServers) == this.serverId);
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