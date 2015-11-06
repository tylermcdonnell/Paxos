package message;


/**
 * Heart beat messages for leaders (or servers) to send to each other to
 * keep a rough idea of attendance (who is dead or not).
 * @author Mike Feilbach
 *
 */
public class HeartBeat extends Message
{
	private static final long serialVersionUID = 1L;
	
	// Who I think the current leader is at the time I send this heart beat.
	private int currentLeaderId;
	
	// Who is sending this message;
	private int senderId;
	
	public HeartBeat(int senderId, int currentLeaderId)
	{
		this.currentLeaderId = currentLeaderId;
		this.senderId = senderId;
	}
	
	public int getCurrentLeaderId()
	{
		return this.currentLeaderId;
	}
	
	public int getSenderId()
	{
		return this.senderId;
	}

	@Override
	public String toString() {
		String retVal = "";
		retVal += "HeartBeat: <currentLeaderId: " + this.currentLeaderId + ">";
		return retVal;
	}
}
