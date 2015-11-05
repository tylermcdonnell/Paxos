package server;

import java.io.Serializable;

import client.Command;

/**
 * A set of state entries comprise a state.
 * @author Mike Feilbach
 *
 */
public class StateEntry implements Serializable
{
	private static final long serialVersionUID = 1L;
	
	private Command command;
	private int slotNumber;
	
	public StateEntry(Command command, int slotNumber)
	{
		this.command = command;
		this.slotNumber = slotNumber;
	}
	
	public Command getCommand()
	{
		return this.command;
	}
	
	public int getSlotNumber()
	{
		return this.slotNumber;
	}
	
	@Override
	public String toString()
	{
		String retVal = "";
		retVal += "StateEntry: <slotNum: " + this.slotNumber + ", " + this.command + ">";
		return retVal;
	}
	
	
	@Override
	public boolean equals(Object o)
	{
		if (o == null)
		{
			return false;
		}
		
		if (o == this)
		{
			return true;
		}
		
		if (o instanceof StateEntry)
		{
			StateEntry s = (StateEntry) o;
			
			boolean slotNumEqual = this.getSlotNumber() == s.getSlotNumber();
			boolean cmdEqual = this.getCommand().equals(s.getCommand());
			
			if (slotNumEqual && cmdEqual)
			{
				return true;
			}
		}
		
		return false;
	}
	
}
