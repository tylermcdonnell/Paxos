package message;

import ballot.Ballot;


/**
 * A p1a message in Paxos, sent from a scout to an acceptor.
 * @author Mike Feilbach
 *
 */
public class P1a extends Message
{
	private static final long serialVersionUID = 1L;

	// Scouts send p1a messages, which include their leader's ID.
	// Scouts are spawned by leaders, and thus, there is a a one-to-one
	// mapping between leader ID and scout.
	private int myLeaderId;
	
	// The ballot this scout is sending.
	private Ballot ballot;
	
	public P1a(int myLeaderId, Ballot ballot)
	{
		this.myLeaderId = myLeaderId;
		this.ballot = ballot;
	}
	
	public int getMyLeaderId()
	{
		return this.myLeaderId;
	}
	
	public Ballot getBallot()
	{
		return this.ballot;
	}

	@Override
	public String toString()
	{
		String retVal = "";
		retVal += "P1a: <myLeaderId: " + this.myLeaderId + ", " + this.ballot + ">";
		return retVal;
	}
}
