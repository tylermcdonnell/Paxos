package message;

/**
 * When a leader recovers, it sends this request to all other leaders
 * to get all the proposals in the system.
 * @author Mike Feilbach
 *
 */
public class ProposalsRequest extends Message
{
	private static final long serialVersionUID = 1L;
	
	private int senderId;
	
	public ProposalsRequest(int senderId)
	{
		this.senderId = senderId;
	}
	
	public int getSenderId()
	{
		return this.senderId;
	}

	@Override
	public String toString() 
	{
		String retVal = "";
		retVal += "ProposalsRequest: <senderId: " + this.senderId + ">";
		return retVal;
	}

}
