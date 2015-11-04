package server;

import java.io.Serializable;

import client.Command;
import Ballot.Ballot;


/**
 * A PValue in the Paxos algorithm.  Contains: <b, s, p> where
 * b = ballot, s = slot number, p = command.
 * @author Mike Feilbach
 *
 */
public class PValue implements Serializable
{
	private static final long serialVersionUID = 1L;
	
	private Ballot ballot;
	private int slotNumber;
	private Command command;
	
	public PValue(Ballot ballot, int slotNumber, Command command)
	{
		this.ballot = ballot;
		this.slotNumber = slotNumber;
		this.command = command;
	}
	
	public Ballot getBallot()
	{
		return this.ballot;
	}
	
	public int getSlotNumber()
	{
		return this.slotNumber;
	}
	
	public Command getCommand()
	{
		return this.command;
	}
	
	@Override
	public String toString()
	{
		String retVal = "";
		retVal += "PValue: <" + this.ballot + ", slotNumber: " 
				+ this.slotNumber + ", " + this.command + ">";
		return retVal;
	}

}
