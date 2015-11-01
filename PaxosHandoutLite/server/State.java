package server;

import java.util.ArrayList;

/**
 * A State from the Paxos algorithm (what p.state represents, where p is 
 * a replica).
 * 
 * @author Mike Feilbach
 */
public class State {
	
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
}
