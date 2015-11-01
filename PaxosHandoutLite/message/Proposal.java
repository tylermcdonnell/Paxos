package message;

import client.Command;

/**
 * A proposal message.
 * @author Mike Feilbach
 *
 */
public class Proposal extends Message
{	
	private static final long serialVersionUID = 1L;
	
	private int slotNum;
	private Command command;
	
	public Proposal(int slotNum, Command command)
	{
		this.slotNum = slotNum;
		this.command = command;
	}
	
	public int getSlotNum()
	{
		return this.slotNum;
	}
	
	public Command getCommand()
	{
		return this.command;
	}

	@Override
	public String toString()
	{
		String retVal = "";
		retVal += "Proposal: <slotNum: " + this.slotNum + ", " + this.command.toString() + ">";
		return retVal;
	}

}
