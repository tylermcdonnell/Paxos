package message;

import client.Command;

/**
 * A request message.  Clients send request messages to replicas to execute
 * commands in the chatroom.
 * @author Mike Feilbach
 *
 */
public class Request extends Message
{
	private static final long serialVersionUID = 1L;
	
	private Command command;
	
	public Request(Command command)
	{
		this.command = command;
	}
	
	public Command getCommand()
	{
		return this.command;
	}

	@Override
	public String toString()
	{
		String retVal = "";
		retVal += "Request: <" + this.command.toString() + ">";
		return retVal;
	}
}
