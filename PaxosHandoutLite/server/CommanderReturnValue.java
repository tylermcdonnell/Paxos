package server;

import ballot.Ballot;

public class CommanderReturnValue
{
	private PValue pvalue;
	private int commanderId;
	private int returnValue;
	
	public CommanderReturnValue(PValue pvalue, int commanderId, int returnValue)
	{
		this.pvalue = pvalue;
		this.commanderId = commanderId;
		this.returnValue = returnValue;
	}
	
	public PValue getPValue()
	{
		return this.pvalue;
	}
	
	public int getCommanderId()
	{
		return this.commanderId;
	}
	
	public int getReturnValue()
	{
		return this.returnValue;
	}
}
