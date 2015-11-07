package message;

import java.util.ArrayList;


/**
 * When a leader receives a ProposalsRequest, it responds with this message,
 * containing all of the proposals they are aware of.
 * @author Mike Feilbach
 *
 */
public class ProposalsResponse extends Message
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private ArrayList<Proposal> proposals;
	
	private int senderId;
	
	public ProposalsResponse(int senderId)
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
		retVal += "ProposalsResponse: <senderId: " + this.senderId + ", PROPOSALS TOO LONG TO PRINT. TODO?>";
		return retVal;
	}
}
