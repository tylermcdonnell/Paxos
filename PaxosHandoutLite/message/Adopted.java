package message;

import java.util.ArrayList;

import ballot.Ballot;
import server.PValue;


/**
 * An adopted message in Paxos.  This is sent from a scout to its leader.
 * @author Mike Feilbach
 *
 */
public class Adopted extends Message
{
	private static final long serialVersionUID = 1L;

	private Ballot ballot;
	private ArrayList<PValue> pvalues;
	
	public Adopted(Ballot ballot, ArrayList<PValue> pvalues)
	{
		this.ballot = ballot;
		this.pvalues = pvalues;
	}
	
	public Ballot getBallot()
	{
		return this.ballot;
	}
	
	public ArrayList<PValue> getPvalues()
	{
		return this.pvalues;
	}
	
	@Override
	public String toString()
	{
		String retVal = "";
		retVal += "Adopted: <" + this.ballot + ", PVALUES IS AN ARRAY LIST, NOT GOING TO SHOW HERE, TODO?>";
		return retVal;
	}
}
