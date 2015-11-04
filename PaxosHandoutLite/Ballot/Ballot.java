package Ballot;

import java.io.Serializable;

import message.Decision;

/**
 * A ballot in the Paxos algorithm.
 * @author Mike Feilbach
 *
 */
public class Ballot implements Serializable
{
	private static final long serialVersionUID = 1L;
	
	// A ballot will be (ballotId, leaderId), and two ballots
	// will be compared lexicographically.
	private int leaderId;
	private int ballotId;
	
	/**
	 * Default constructor
	 * 
	 * @param leaderId, the ID of the leader this Ballot is for.
	 */
	public Ballot(int leaderId)
	{
		this.leaderId = leaderId;
		this.ballotId = 0;
	}
	
	/**
	 * Create a Ballot with the given leader ID and ballot ID.
	 */
	public Ballot(int leaderId, int ballotId)
	{
		this.leaderId = leaderId;
		this.ballotId = ballotId;
	}
	
	
	/**
	 * Returns true iff this Ballot is greater than Ballot y.
	 * 
	 * @param y, Ballot y.
	 * 
	 * @return true iff this Ballot is greater than Ballot y.
	 */
	public boolean greaterThan(Ballot y)
	{
		// Check if this is the same ballot.
		if ((this.ballotId == y.ballotId) && (this.leaderId == y.leaderId))
		{
			System.out.println("Comparing two equal ballots in greaterThan(Ballot y).");
			System.out.println("Exiting.");
			System.exit(-1);
		}
		
		// A ballot will be (ballotId, leaderId), and two ballots
		// will be compared lexicographically.
		
		// Check ballot ID first.
		if (this.ballotId > y.ballotId)
		{
			// y's ballot ID is smaller.
			return true;
		}
		else if (this.ballotId < y.ballotId)
		{
			// y's ballot ID is larger.
			return false;
		}
		else
		{
			// y's ballot ID is equal. Resort to the leader ID.
			// Leader ID's are totally ordered, so they cannot be
			// equal => this will decide for certain.
			if (this.leaderId > y.leaderId)
			{
				// y's leader ID is smaller.
				return true;
			}
			else
			{
				// y'd leader ID is larger, since total ordering of leader IDs.
				return false;
			}
		}
	}
	
	@Override
	public String toString()
	{
		String retVal = "";
		retVal += "Ballot: <ballotId = " + this.ballotId + ", leaderId = " 
				+ this.leaderId + ">";
		return retVal;
	}
	
	@Override
	public boolean equals(Object o)
	{
		if (o == null)
		{
			return false;
		}
		
		if (o instanceof Ballot)
		{
			Ballot b = (Ballot) o;
			
			boolean leaderIdEqual = this.leaderId == b.leaderId;
			boolean ballotIdEqual = this.ballotId == b.ballotId;
			
			if (leaderIdEqual && ballotIdEqual)
			{
				return true;
			}
		}
		
		return false;
	}
	
	
	public int getLeaderId()
	{
		return this.leaderId;
	}
	
	public int getBallotId()
	{
		return this.ballotId;
	}
}
