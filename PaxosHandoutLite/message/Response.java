package message;

import server.State;

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
	
	// The resulting state after the operation of this command
	// was applied.
	private State result;
	
	public Response(int cid, State resultingState)
	{
		this.cid = cid;
		this.result = resultingState;
	}
	
	@Override
	public String toString()
	{
		String retVal = "";
		retVal += "Response: <cid: " + this.cid + " " + this.result + ">";
		return retVal;
	}
}
