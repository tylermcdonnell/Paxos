package ballot;


/**
 * Generate ballots for any given leader in the Paxos algorithm.
 * @author Mike Feilbach
 *
 */
public class BallotGenerator
{

	private int leaderId;
	private Ballot currBallot;
	
	public BallotGenerator(int leaderId)
	{
		// Store the leader ID. This leader will generate ballots with this
		// class for its lifetime.
		this.leaderId = leaderId;
		
		// Create an initial ballot for this leader ID.  This will be the first
		// Ballot, with a ballot number of 0.
		this.currBallot = new Ballot(this.leaderId);
	}
	
	
	/**
	 * Get this leader's next ballot.
	 * 
	 * @return this leader's next ballot.
	 */
	public Ballot getNextBallot()
	{
		// This is the current ballot.
		Ballot curr = this.getCurrentBallot();
		
		// Increase the ballot ID.
		int currBallotId = curr.getBallotId();
		
		// Create a ballot with the same leader ID, but the next ballot ID.
		Ballot newBallot = new Ballot(currBallotId + 1, this.leaderId);
		
		// Store this ballot as our current ballot!
		this.currBallot = newBallot;
		
		return this.currBallot; 
	}
	
	
	/**
	 * Get this leader's current ballot.
	 * 
	 * @return this leader's current ballot.
	 */
	public Ballot getCurrentBallot()
	{
		return this.currBallot;
	}
}
