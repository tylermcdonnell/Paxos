package server;

import java.util.ArrayList;

import ballot.Ballot;
import message.Adopted;
import message.Message;
import message.P1a;
import message.P1b;
import message.Preempted;
import framework.NetController;

/**
 * A scout in the Paxos algorithm, as described in the paper 
 * "Paxos Made Moderately Complex."
 * @author Mike Feilbach
 *
 */
public class Scout
{
	// This scout's unique ID in the eyes of the leader who spawned him.
	private int uniqueId;
	
	// Initialized with IDs of all acceptors.
	private ArrayList<Integer> waitFor;
	
	private ArrayList<PValue> pvalues;
	
	// This server's NetController.
	private NetController network;
	
	// The number of servers in the network.
	private int numServers;
	
	private int myLeaderId;
	
	private Ballot myBallot;
	
	public Scout(Ballot ballot, int myLeaderId, NetController network, int numServers, int uniqueId)
	{
		this.uniqueId = uniqueId;
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
		P1a p1a = new P1a(this.myLeaderId, Ballot.deepCopyBallot(this.myBallot));
		for (int i = 0; i < numServers; i++)
		{
			this.network.sendMsgToServer(i, p1a);
		}
	}
	
	/**
	 * This scout's run method.  When it is done completing its tasks,
	 * the method will return the Scout's unique ID, at which point this 
	 * method should not be called again for this given Scout object.
	 * 
	 * @param message, the message to process.
	 * 
	 * @return -1, else the Scout's unique ID, at which point this 
	 * method should not be called again for this given Scout object.
	 */
	public int runScout(Message message)
	{
		// Scouts only listen for p1b messages.
		if (message instanceof P1b)
		{
			System.out.println("Scout " + this.myLeaderId + " got p1b");
			
			P1b p1b = (P1b) message;
			
			// If the ballot returned from the acceptor is the one I gave him 
			// in p1a.
			if (p1b.getBallot().equals(this.myBallot))
			{
				// If the accepted set from p1b contains any pvalues that
				// we don't have in our pvalue set currently, add them.  In other
				// words, take the union of the pvalue and accepted sets.
				this.pvalues = PValue.takeUnionOfPValueSets(this.pvalues, p1b.getAcceptedSet());
				
				// Testing.
				/*System.out.print("OLD WAIT FOR LIST: ");
				for (int i = 0; i < this.waitFor.size(); i++)
				{
					System.out.print(this.waitFor.get(i) + " ");
				}
				System.out.println();*/
				
				// Take this acceptor's ID off of the wait for list.
				// Make sure we remove the Integer object, not the element
				// at the given index (We use Integer rather than int to
				// accomplish this.)
				this.waitFor.remove(new Integer(p1b.getAcceptorId()));
				
				// Testing.
				/*System.out.print("NEW WAIT FOR LIST: ");
				for (int i = 0; i < this.waitFor.size(); i++)
				{
					System.out.print(this.waitFor.get(i) + " ");
				}
				System.out.println();*/
				
				// If |waitFor| < (|acceptors| / 2).  In other words, do we 
				// have majority of acceptors saying they like our ballot?
				// Make sure we cast to doubles first, we don't want truncation.
				// (Or does this matter?  Either way, using doubles is safe.)
				if (((double) this.waitFor.size()) < ((double) this.numServers) / 2)
				{
					// The majority of the acceptors like our ballot.
					// Send to the leader that this ballot was adopted, along 
					// with the pvalue set, so we can see if previous values 
					// were accepted already.  We must pass a deep copy of the
					// pvalues set and the Ballot, since we will nullify this 
					// scout's reference, and it will be eaten by the garbage
					// collector.
					Adopted adopted = new Adopted(Ballot.deepCopyBallot(this.myBallot), PValue.deepCopyPValueSet(this.pvalues));
					
					// send to leader: <adopted, b, pvalues>
					this.network.sendMsgToServer(this.myLeaderId, adopted);
					
					// This Scout is done with its tasks.
					// This is exit() in the Paper.
					return this.uniqueId;
				}
			}
			else
			{
				// An acceptor returned a ballot which did not match.
				Preempted preempted = new Preempted(Ballot.deepCopyBallot(this.myBallot));
				
				// send to leader: <preempted, b'>
				this.network.sendMsgToServer(this.myLeaderId, preempted);
				
				// This Scout is done with its tasks.
				// This is exit() in the Paper.
				return this.uniqueId;
			}
		}
		
		// This Scout is not done yet.
		return -1;
	}
}
