package client;

import java.io.Serializable;

/**
 * A command has the form <k, cid, op> where k = client ID,
 * cid = client-local unique command ID, op = operation.
 * 
 * @author Mike Feilbach
 */
public class Command implements Serializable
{
	private static final long serialVersionUID = 1L;
	
	private int clientId;
	private int commandId;
	
	// Operations are just messages to add to the chat.
	private String message;
	
	public Command(int clientId, int commandId, String message)
	{
		this.clientId = clientId;
		this.commandId = commandId;
		this.message = message;
	}
	
	public int getClientId()
	{
		return this.clientId;
	}
	
	public int getCommandId()
	{
		return this.commandId;
	}
	
	public String getMessage()
	{
		return this.message;
	}
	
	public String toString()
	{
		String retVal = "";
		retVal += "Command: <clientId: " + this.clientId + ", cid: " 
				+ this.commandId + ", op: " + this.message + ">";
		
		return retVal;
	}
}
