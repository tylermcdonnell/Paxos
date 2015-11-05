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
	
}
