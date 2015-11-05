package server;

import java.util.ArrayList;

import client.Command;
import ballot.Ballot;
import framework.NetController;
import message.Message;
import message.P1a;
import message.P1b;
import message.P2a;
import message.P2b;


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
			
			System.out.println("Acceptor got p1a from Scout " + p1a.getMyLeaderId() + ": " + p1a);
			
			// If the scout's ballot is larger than this acceptor's.
			boolean scoutBallotLarger = false;
			
			// Check for "bottom" case always.
			if (this.currBallot == null)
			{
				// Current ballot is "bottom," so any real ballot is larger.
				scoutBallotLarger = true;
			}
			else
			{
				// Current ballot is not "bottom."  Must actually compare it.
				if (!this.currBallot.greaterThan(p1a.getBallot()))
				{
					// The ballot from the scout is larger, so take it.
					scoutBallotLarger = true;
				}
			}
			
			if (scoutBallotLarger)
			{
				this.currBallot = p1a.getBallot();
			}
			
			// If we replaced ballot or not, send message back to the scout
			// who sent us this p1a message.  This message is of type p1b.
			
			// NOTE: WE MUST send back a deep copy of the ballot and the accepted
			// set.  Why?  Because if we send back a reference, the acceptor
			// may change the contents dynamically without notice.
			
			// NOTE: all the classes in the package messages are serializable,
			// and are serialized when sent over the network.  Thus, deep
			// copying is not required, since it is done implicitly by the
			// serialization when sending across the network.
			P1b p1b = new P1b(Ballot.deepCopyBallot(this.currBallot), PValue.deepCopyPValueSet(this.accepted), this.serverId);
			
			this.network.sendMsgToServer(p1a.getMyLeaderId(), p1b);
		}
		
		if (message instanceof P2a)
		{
			P2a p2a = (P2a) message;
			
			System.out.println("Acceptor got p2a from Commander " + p2a.getMyLeaderId() + ": " + p2a);
			
			// If commander ballot is larger than or equal to this acceptor's.
			boolean commanderBallotIsLargerOrEqual = false;
			Ballot commanderBallot = p2a.getMyPValue().getBallot();
			
			// Check for "bottom" case always.
			if (this.currBallot == null)
			{
				// Current ballot is "bottom," so any real ballot is larger.
				commanderBallotIsLargerOrEqual = true;
			}
			else
			{
				// Current ballot is not "bottom."  Must actually compare it.
				boolean ballotsEqual = this.currBallot.equals(commanderBallot);
				boolean commanderBallotLarger = !this.currBallot.greaterThan(commanderBallot);
				
				if (ballotsEqual || commanderBallotLarger)
				{
					// The ballot from the commander is larger or equal, so take it.
					commanderBallotIsLargerOrEqual = true;
				}
			}
			
			// if b >= ballot_num (from the Paper).
			if (commanderBallotIsLargerOrEqual)
			{
				// Replace this acceptor's ballot.
				this.currBallot = p2a.getMyPValue().getBallot();
				
				// accepted = accepted (union) {<b, s, p>} (from the Paper).
				// In other words, if the PValue from p2a is not in accepted,
				// add it, else don't.
				if (!this.accepted.contains(p2a.getMyPValue()))
				{
					this.accepted.add(p2a.getMyPValue());
				}
			}
			
			// Send to the commander a p2b message.
			P2b p2b = new P2b(this.serverId, Ballot.deepCopyBallot(this.currBallot));
			
			this.network.sendMsgToServer(p2a.getMyLeaderId(), p2b);
		}
	}
}
