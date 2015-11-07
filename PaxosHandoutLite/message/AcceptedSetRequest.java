package message;

/**
 * A recovering acceptor sends this message to all other acceptors, hoping
 * to obtain their accepted sets.
 * @author Mike Feilbach
 *
 */
public class AcceptedSetRequest extends Message
{
	private static final long serialVersionUID = 1L;
	
	private int senderId;
	
	public AcceptedSetRequest(int senderId)
	{
		this.senderId = senderId;
	}
	
	public int getSenderId()
	{
		return this.senderId;
	}

	@Override
	public String toString() {
		String retVal = "";
		retVal += "AcceptedSetRequest: <senderID: " + this.senderId + ">";
		return retVal;
	}
}
