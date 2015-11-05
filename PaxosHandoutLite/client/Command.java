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
	
	
	/**
	 * Returns a deep copy of the given Command.
	 * 
	 * @param command, the given Command.
	 * 
	 * @return a deep copy of the given Command.
	 */
	public static Command deepCopyCommand(Command command)
	{
		int newClientId = command.getClientId();
		int newCommandId = command.getCommandId();
		String newOperation = new String(command.getOperation());
		
		Command newCommand = new Command(newClientId, newCommandId, newOperation);
		
		// Testing.  Quick check.
		//System.out.println("NEW COMMANAD, should be true: " + newCommand.equals(command));
		//System.out.println("NEW COMMAND, should be false: " + (newCommand == command));
		
		return newCommand;
	}
	
	@Override
	public boolean equals(Object o)
	{
		if (o == null)
		{
			return false;
		}
		
		if (o instanceof Command)
		{
			Command c = (Command) o;
			
			boolean clientIdEqual = this.getClientId() == c.getClientId();
			boolean cidEqual = this.getCommandId() == c.getCommandId();
			boolean opEqual = this.getOperation().equals(c.getOperation());
			
			if (clientIdEqual && cidEqual && opEqual)
			{
				return true;
			}
		}
		
		return false;
	}
}
