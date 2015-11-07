package message;

import java.util.ArrayList;

import server.PValue;

/**
 * An alive acceptor who receives this message from a recovering acceptor will
 * forward the recovering acceptor their accepted set.
 * 
 * @author Mike Feilbach
 *
 */
public class AcceptedSetResponse extends Message
{
	private static final long serialVersionUID = 1L;
	
	private int senderId;
	private ArrayList<PValue> acceptedSet;
	
	public AcceptedSetResponse(int senderId, ArrayList<PValue> acceptedSet)
	{
		this.senderId = senderId;
		this.acceptedSet = acceptedSet;
	}
	
	public int getSenderId()
	{
		return this.senderId;
	}
	
	public ArrayList<PValue> getAcceptedSet()
	{
		return this.acceptedSet;
	}

	@Override
	public String toString() {
		String retVal = "";
		retVal += "AcceptedSetResponse: <senderID: " + this.senderId + ", Accepted Set: TOO LONG, TODO?>";
		return retVal;
	}
}
