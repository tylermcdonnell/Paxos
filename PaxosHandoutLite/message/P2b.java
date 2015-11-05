package message;

import ballot.Ballot;


/**
 * A p2b message from Paxos.  Acceptors send p2b messages to commanders.
 * @author Mike Feilbach
 *
 */
public class P2b extends Message
{
	private static final long serialVersionUID = 1L;

	// The acceptor ID (which acceptor sent this message).
	private int acceptorId;
	
	private Ballot ballot;
	
	public P2b(int acceptorId, Ballot ballot)
	{
		this.acceptorId = acceptorId;
		this.ballot = ballot;
	}
	
	public int getAcceptorId()
	{
		return this.acceptorId;
	}
	
	public Ballot getBallot()
	{
		return this.ballot;
	}
	
	@Override
	public String toString()
	{
		String retVal = "";
		retVal += "P2b: <acceptorId: " + this.acceptorId + ", " + this.ballot + ">";
		return retVal;
	}

}
