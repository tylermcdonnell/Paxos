package message;


/**
 * A decision message.  Commanders send decision messages to replicas.
 * @author Mike Feilbach
 *
 */
public class Decision extends Message
{
	private static final long serialVersionUID = 1L;
	
	private Proposal proposal;
	
	public Decision(Proposal p)
	{
		this.proposal = p;
	}

	@Override
	public String toString()
	{
		String retVal = "";
		retVal += "Decision: <" + this.proposal.toString() + ">";
		return retVal;
	}

}
