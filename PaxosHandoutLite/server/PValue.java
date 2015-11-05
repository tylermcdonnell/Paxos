package server;

import java.io.Serializable;
import java.util.ArrayList;

import ballot.Ballot;
import client.Command;


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
	
	
	/**
	 * Returns a deep copy of the given PValue.
	 * 
	 * @param pvalue, the given PValue.
	 * 
	 * @return a deep copy of the given PValue.
	 */
	public static PValue deepCopyPValue(PValue pvalue)
	{	
		// Values to deep copy:
		Ballot oldBallot = pvalue.getBallot();
		int oldSlotNumber = pvalue.getSlotNumber();
		Command oldCommand = pvalue.getCommand();
		
		Ballot newBallot = Ballot.deepCopyBallot(oldBallot);
		int newSlotNumber = oldSlotNumber;
		Command newCommand = Command.deepCopyCommand(oldCommand);
		
		PValue newPValue = new PValue(newBallot, newSlotNumber, newCommand);
		
		// Testing.  Quick check.
		//System.out.println("NEW PVALUE, should be true: " + newPValue.equals(pvalue));
		//System.out.println("NEW PVALUE, should be false: " + (newPValue == pvalue));
		
		return newPValue;
	}
	
	
	/**
	 * Returns a deep copy of the given ArrayList<PValue>.
	 * 
	 * @param pvalues, the given ArrayList<PValue>.
	 * 
	 * @return a deep copy of the given ArrayList<PValue>.
	 */
	public static ArrayList<PValue> deepCopyPValueSet(ArrayList<PValue> pvalues)
	{
		ArrayList<PValue> newPvalues = new ArrayList<PValue>();
		
		for (int i = 0; i < pvalues.size(); i++)
		{
			// Values to deep copy:
			Ballot oldBallot = pvalues.get(i).getBallot();
			int oldSlotNumber = pvalues.get(i).getSlotNumber();
			Command oldCommand = pvalues.get(i).getCommand();
			
			Ballot newBallot = Ballot.deepCopyBallot(oldBallot);
			int newSlotNumber = oldSlotNumber;
			Command newCommand = Command.deepCopyCommand(oldCommand);
			
			newPvalues.add(new PValue(newBallot, newSlotNumber, newCommand));
		}
		
		// Testing.  Quick check.
		//for (int i = 0; i < pvalues.size(); i++)
		//{
			//System.out.println("NEW PVALUE SET, element " + i + ", should be true: " + newPvalues.get(i).equals(pvalues.get(i)));
			//System.out.println("NEW PVALUE SET, element " + i + ", should be false: " + (newPvalues.get(i) == pvalues.get(i)));
		//}

		return newPvalues;
	}
	
	
	/**
	 * Returns the union of the given ArrayList<PValue> sets a and b.
	 * 
	 * @param a, set a.
	 * @param b, set b.
	 * 
	 * @return the union of the given ArrayList<PValue> sets a and b.
	 */
	public static ArrayList<PValue> takeUnionOfPValueSets(ArrayList<PValue> a, ArrayList<PValue> b)
	{
		// Traverse set b.
		for (int i = 0; i < b.size(); i++)
		{
			// If set a doesn't have this item, add it.
			PValue currVal = b.get(i);
			
			if (!a.contains(currVal))
			{
				a.add(currVal);
			}
		}
		
		return a;
	}
	
	
	@Override
	public boolean equals(Object o)
	{
		if (o == null)
		{
			return false;
		}
		
		if (o instanceof PValue)
		{
			PValue pv = (PValue) o;
			
			boolean ballotEqual = this.getBallot().equals(pv.getBallot());
			boolean slotNumEqual = this.getSlotNumber() == pv.getSlotNumber();
			boolean cmdEqual = this.getCommand().equals(pv.getCommand());
			
			if (ballotEqual && slotNumEqual && cmdEqual)
			{
				return true;
			}
		}
		
		return false;
	}
}
