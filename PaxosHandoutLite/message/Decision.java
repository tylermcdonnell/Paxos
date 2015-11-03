package message;


/**
 * A decision message.  Commanders send decision messages to replicas.
 * @author Mike Feilbach
 *
 */
public class Decision extends Message
{
	private static final long serialVersionUID = 1L;
	
	// This class wraps a Proposal, but the distinct class name
	// differentiates it from a Proposal for clarity of knowing
	// whether a message is a Proposal or Decision.
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
	
	public Proposal getProposal()
	{
		return this.proposal;
	}
	
	public int getProposalSlotNum()
	{
		return this.proposal.getSlotNum();
	}
	
	@Override
	public boolean equals(Object o)
	{
		if (o == null)
		{
			return false;
		}
		
		if (o instanceof Decision)
		{
			Decision d = (Decision) o;
			
			boolean slotNumEqual = this.proposal.getSlotNum() == d.proposal.getSlotNum();
			boolean clientIdEqual = this.proposal.getCommand().getClientId() == d.proposal.getCommand().getClientId();
			boolean cidEqual = this.proposal.getCommand().getCommandId() == d.proposal.getCommand().getCommandId();
			boolean cmdEqual = this.proposal.getCommand().getOperation().equals(d.proposal.getCommand().getOperation());
			
			if (slotNumEqual && clientIdEqual && cidEqual && cmdEqual)
			{
				return true;
			}
		}
		
		return false;
	}

}
