package message;

import ballot.Ballot;


/**
 * A preempted message in the Paxos algorithm.
 * @author Mike Feilbach
 *
 */
public class Preempted extends Message
{
	private static final long serialVersionUID = 1L;
	
	private Ballot ballot;
	
	public Preempted(Ballot ballot)
	{
		this.ballot = ballot;
	}
	
	public Ballot getBallot()
	{
		return this.ballot;
	}

	@Override
	public String toString() {
		String retVal = "";
		retVal += "Preempted: <" + this.ballot + ">";
		return retVal;
	}
}
