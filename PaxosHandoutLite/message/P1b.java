package message;

import java.util.ArrayList;

import server.PValue;
import Ballot.Ballot;


/**
 * A P1b message from Paxos, send from an acceptor to a scout.
 * @author Mike Feilbach
 *
 */
public class P1b extends Message
{
	private static final long serialVersionUID = 1L;
	
	private Ballot ballot;
	
	// This is the acceptor's accepted set of pvalues.
	private ArrayList<PValue> acceptedSet;
	
	// The acceptor who is sending this.
	private int acceptorId;
	
	public P1b(Ballot ballot, ArrayList<PValue> acceptedSet, int acceptorId)
	{
		this.ballot = ballot;
		this.acceptedSet = acceptedSet;
		this.acceptorId = acceptorId;
	}
	
	public Ballot getBallot()
	{
		return this.ballot;
	}
	
	public ArrayList<PValue> getAcceptedSet()
	{
		return this.acceptedSet;
	}
	
	public int getAcceptorId()
	{
		return this.acceptorId;
	}

	@Override
	public String toString()
	{
		String retVal = "";
		retVal += "P1b: <acceptorId: " + this.acceptorId + ", " 
				+ this.ballot 
				+ ", acceptedSet: NOT PRINTED YET (IMPLEMENT)>";
		return retVal;
	}

}
