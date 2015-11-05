package server;

import java.util.ArrayList;

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
		
		// TODO
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
		}
		
		if (message instanceof Adopted)
		{
			Adopted adopted = (Adopted) message;
			System.out.println("Leader " + this.serverId + " received " + adopted);
		}
		
		if (message instanceof Preempted)
		{
			Preempted preempted = (Preempted) message;
			System.out.println("Leader " + this.serverId + " received " + preempted);
		}
	
		
		// If we have a scout out, run its tasks.
		if (this.scout != null)
		{
			boolean stillRunning = this.scout.runScout(message);
			
			if (stillRunning == false)
			{
				// Don't run anymore.
				this.scout = null;
			}
		}
		
		// TODO create classes for these message types.
		//if (message instanceof Adopted)
		//if (message instanceof Preempted)
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
