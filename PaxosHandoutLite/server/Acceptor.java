package server;

import message.Message;


/**
 * The acceptor on a given server.
 * @author Mike Feilbach
 *
 */
public class Acceptor
{
	// What server this acceptor is on.
	private int serverId;
	
	public Acceptor(int serverId)
	{
		this.serverId = serverId;
	}
	
	public void runTasks(Message message)
	{
		
	}
}
