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
	
	// Operations are just messages to add to the chat for this project.
	private String operation;
	
	public Command(int clientId, int commandId, String operation)
	{
		this.clientId = clientId;
		this.commandId = commandId;
		this.operation = operation;
	}
	
	public int getClientId()
	{
		return this.clientId;
	}
	
	public int getCommandId()
	{
		return this.commandId;
	}
	
	public String getOperation()
	{
		return this.operation;
	}
	
	public String toString()
	{
		String retVal = "";
		retVal += "Command: <clientId: " + this.clientId + ", cid: " 
				+ this.commandId + ", op: " + this.operation + ">";
		
		return retVal;
	}
}
