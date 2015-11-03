package server;

import message.Message;


/**
 * The leader on a given server.
 * @author Mike Feilbach
 *
 */
public class Leader
{
	// What server this leader is on.
	private int serverId;
	
	public Leader(int serverId)
	{
		this.serverId = serverId;
	}
	
	public void runTasks(Message message)
	{
		
	}
}
