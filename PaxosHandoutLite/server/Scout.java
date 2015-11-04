package server;

import java.util.ArrayList;

import Ballot.Ballot;
import message.Message;
import message.P1a;
import message.P1b;
import framework.NetController;

/**
 * A scout in the Paxos algorithm, as described in the paper 
 * "Paxos Made Moderately Complex."
 * @author Mike Feilbach
 *
 */
public class Scout
{
	// Initialized with IDs of all acceptors.
	private ArrayList<Integer> waitFor;
	
	private ArrayList<PValue> pvalues;
	
	// This server's NetController.
	private NetController network;
	
	// The number of servers in the network.
	private int numServers;
	
	private int myLeaderId;
	
	private Ballot myBallot;
	
	public Scout(Ballot ballot, int myLeaderId, NetController network, int numServers)
	{
		this.network = network;
		this.numServers = numServers;
		this.myLeaderId = myLeaderId;
		this.myBallot = ballot;
		
		// Add all acceptor IDs to the waitFor list.  This assumes that
		// the number of acceptors = number of servers.
		waitFor = new ArrayList<Integer>();
		for (int i = 0; i < numServers; i++)
		{
			this.waitFor.add(i);
		}
		
		// pvalues is empty initially.
		pvalues = new ArrayList<PValue>();
		
		// Send p1a to all acceptors.  There is an acceptor on every server,
		// including ours!
		P1a p1a = new P1a(this.myLeaderId, this.myBallot);
		for (int i = 0; i < numServers; i++)
		{
			this.network.sendMsgToServer(i, p1a);
		}
	}
	
	public void runCommander(Message m)
	{
		if (m instanceof P1b)
		{
			P1b p1b = (P1b) m;
			
			// TODO -- This is the only case where Scout does anything!
			if (p1b.getBallot().equals(this.myBallot))
			{
				
			}
		}
	}
}
