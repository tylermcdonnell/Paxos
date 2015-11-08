package client;

/**
 * Used to keep track of when a command was sent, when (if) its response
 * has been received, etc.
 * @author Mike Feilbach
 *
 */
public class CommandStatus 
{
	// The command we are keeping status for.
	private Command command;
	
	// The time (in milliseconds) this command was requested (sent to all
	// servers).
	private long timeSent;
	
	// If a response for this command was received yet.
	private boolean responseReceived;
	
	
	// When we should check if this command needs to be re-sent next.
	private long nextCheckTime;
	
	public CommandStatus(Command command, long timeSent, long nextCheckTime)
	{
		this.command = command;
		this.timeSent = timeSent;
		
		// This command has not been responded to yet.
		this.responseReceived = false;
		
		this.nextCheckTime = nextCheckTime;
	}
	
	public void setResponseReceived()
	{
		this.responseReceived = true;
	}
	
	public boolean getResponseReceived()
	{
		return this.responseReceived;
	}
	
	public Command getCommand()
	{
		return this.command;
	}
	
	public long getTimeSent()
	{
		return this.timeSent;
	}
	
	public void setNextCheckTime(long nextCheckTime)
	{
		this.nextCheckTime = nextCheckTime;
	}
	
	public long getNextCheckTime()
	{
		return this.nextCheckTime;
	}
}
