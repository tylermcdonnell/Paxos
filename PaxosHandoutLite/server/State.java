package server;

import java.io.Serializable;
import java.util.ArrayList;

import client.Command;

/**
 * A State from the Paxos algorithm (what p.state represents, where p is 
 * a replica).
 * 
 * @author Mike Feilbach
 */
public class State implements Serializable
{
	private static final long serialVersionUID = 1L;
	
	private ArrayList<StateEntry> state;
	
	public State()
	{
		this.state = new ArrayList<StateEntry>();
	}
	
	public ArrayList<StateEntry> getState()
	{
		return this.state;
	}
	
	public void addToState(StateEntry stateEntry)
	{
		this.state.add(stateEntry);
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
