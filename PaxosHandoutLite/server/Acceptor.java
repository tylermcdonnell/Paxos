package server;

import java.util.ArrayList;

import client.Command;
import ballot.Ballot;
import framework.NetController;
import message.Message;
import message.P1a;
import message.P1b;
import message.P2a;


/**
 * The acceptor on a given server.
 * @author Mike Feilbach
 *
 */
public class Acceptor
{
	// What server this acceptor is on.
	private int serverId;
	
	// This acceptor's current ballot.
	private Ballot currBallot;
	
	// This acceptor's accepted set.
	private ArrayList<PValue> accepted;
	
	// My server's NetController.
	private NetController network;
	
	public Acceptor(int serverId, NetController network)
	{
		this.serverId = serverId;
		
		// Initialize the current ballot to null.  This represents
		// "bottom."  In other words, all ballots are greater than
		// "bottom."
		this.currBallot = null;
		
		// This acceptor's accepted set is empty initially.
		this.accepted = new ArrayList<PValue>();
		
		this.network = network;
	}
	
	public void runTasks(Message message)
	{
		if (message instanceof P1a)
		{
			P1a p1a = (P1a) message;
			
			System.out.println("Acceptor got p1a from Scout: " + p1a);
			
			// Check for "bottom" case always.
			if (this.currBallot == null)
			{
				// Current ballot is "bottom," so any real ballot is larger.
				this.currBallot = p1a.getBallot();
			}
			else
			{
				// Current ballot is not "bottom."  Must actually compare it.
				if (!this.currBallot.greaterThan(p1a.getBallot()))
				{
					// The ballot from the scout is larger, so take it.
					this.currBallot = p1a.getBallot();
				}
			}
			
			// If we replaced ballot or not, send message back to the scout
			// who sent us this p1a message.  This message is of type p1b.
			
			// NOTE: WE MUST send back a deep copy of the ballot and the accepted
			// set.  Why?  Because if we send back a reference, the acceptor
			// may change the contents dynamically without notice.
			P1b p1b = new P1b(Ballot.deepCopyBallot(this.currBallot), PValue.deepCopyPValueSet(this.accepted), this.serverId);
			
			this.network.sendMsgToServer(p1a.getMyLeaderId(), p1b);
		}
		
		if (message instanceof P2a)
		{
			// TODO
		}
	}
}
