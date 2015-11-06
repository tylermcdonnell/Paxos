package message;

public class TimeBombLeader extends Message
{
	private static final long serialVersionUID = 1L;
	
	private int numMessagesUntilCrash;
	private int leaderId;
	
	public TimeBombLeader(int numMessages, int leaderId)
	{
		this.numMessagesUntilCrash = numMessages;
		this.leaderId = leaderId;
	}
	
	public int getNumMessagesUntilCrash()
	{
		return this.numMessagesUntilCrash;
	}
	
	public int getLeaderId()
	{
		return this.leaderId;
	}

	@Override
	public String toString() {
		String retVal = "";
		retVal += "TimeBombLeader: <numMessagesUntilCrash: " 
				+ this.numMessagesUntilCrash + ", leaderId: " 
				+ this.leaderId + ">";
		return retVal;
	}
}
