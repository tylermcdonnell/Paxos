package server;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * A State from the Paxos algorithm (what p.state represents, where p is 
 * a replica).
 * 
 * @author Mike Feilbach
 */
public class State implements Serializable
{
	private static final long serialVersionUID = 1L;
	
	private ArrayList<String> state;
	
	public State()
	{
		this.state = new ArrayList<String>();
	}
	
	public ArrayList<String> getState()
	{
		return this.state;
	}
	
	public void addToState(String s)
	{
		this.state.add(s);
	}
	
	@Override
	public String toString()
	{
		String retVal = "State: ";
		
		for (int i = 0; i < this.state.size(); i++)
		{
			retVal += this.state.get(i) + ", ";
		}
		
		return retVal;
	}
}
