package message;

import server.State;
import server.StateEntry;

/**
 * A response message.  Replicas send response messages to clients to 
 * let them know an operation was done.
 * @author Mike Feilbach
 *
 */
public class Response extends Message
{
	private static final long serialVersionUID = 1L;

	// The cid of the command that was completed.
	private int cid;
	
	// The resulting state entry after the operation of this command
	// was applied.
	private StateEntry result;
	
	public Response(int cid, StateEntry stateEntry)
	{
		this.cid = cid;
		this.result = stateEntry;
	}
	
	@Override
	public String toString()
	{
		String retVal = "";
		retVal += "Response: <cid: " + this.cid + " " + this.result + ">";
		return retVal;
	}
	
	public int getCid()
	{
		return this.cid;
	}
	
	public StateEntry getResult()
	{
		return this.result;
	}
}
