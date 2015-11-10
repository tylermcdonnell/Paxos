package server;

import java.util.ArrayList;

import client.Command;
import ballot.Ballot;
import framework.NetController;
import message.AcceptedSetRequest;
import message.AcceptedSetResponse;
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
	
	// Number of servers in the system.
	private int numServers;
	
	// If this acceptor is currently recovering from a server crash.
	// This is public so the Master class can read it (used for deciding
	// when allClear is done).
	public boolean isRecovering;
	
	// Roughly the time a recovering acceptor should stop waiting for '
	// responses from other acceptors with their accepted sets.
	private long recoveryStopWaitTime;
	
	// When recovering, set this to true after we have sent our acceptor set
	// requests, so we only do it once.
	private boolean sentAcceptorSetRequests;
	
	public Acceptor(int serverId, NetController network, boolean isRecovering, int numServers, long recoveryWaitTime)
	{
		this.isRecovering = isRecovering;
		this.sentAcceptorSetRequests = false;
		
		this.numServers = numServers;
		this.serverId = serverId;
		
		// Initialize the current ballot to null.  This represents
		// "bottom."  In other words, all ballots are greater than
		// "bottom."
		this.currBallot = null;
		
		// This acceptor's accepted set is empty initially.
		this.accepted = new ArrayList<PValue>();
		
		this.network = network;
		
		this.recoveryStopWaitTime = recoveryWaitTime + System.currentTimeMillis();
		
		// Testing.
		//if (this.isRecovering)
		//{
		//	Logger.getInstance().println("Current time:  " + System.currentTimeMillis());
		//	Logger.getInstance().println("Waiting until: " + this.recoveryStopWaitTime);
		//}
	}
	
	public void runTasks(Message message)
	{
		//**********************************************************************
		//* Recovery code (non-blocking).
		//**********************************************************************
		if (this.isRecovering)
		{
			//******************************************************************
			//* Recovering Acceptor received an AcceptedSetResponse message 
			//* from a fellow Acceptor.
			//******************************************************************
			if (message instanceof AcceptedSetResponse)
			{
				AcceptedSetResponse tempAcceptedSetMsg = (AcceptedSetResponse) message;
				ArrayList<PValue> tempAcceptedSet = tempAcceptedSetMsg.getAcceptedSet();
				
				// Testing.
				//Logger.getInstance().println("Acceptor " + this.serverId + " got accepted set from: " + tempAcceptedSetMsg.getSenderId());
				
				// Take union of my accepted set with the one I just received.
				PValue.takeUnionOfPValueSets(this.accepted, tempAcceptedSet);
				
				// Testing.
				//Logger.getInstance().println("Acceptor " + this.serverId + " new accepted set:");
				//PValue.printNicely(this.accepted);
			}
			
			if (this.sentAcceptorSetRequests == false)
			{
				// Send AcceptedSetRequest messages to all other servers.
				for (int i = 0; i < this.numServers; i++)
				{
					AcceptedSetRequest request = new AcceptedSetRequest(this.serverId);
					this.network.sendMsgToServer(i, request);
				}
			
				// Done sending acceptor set requests, make sure we don't send them
				// additional times.
				this.sentAcceptorSetRequests = true;
			}

			// We are still waiting for messages from other acceptors.
			// Check if we are done waiting yet.
			if (System.currentTimeMillis() >= this.recoveryStopWaitTime)
			{
				this.isRecovering = false;
				//Logger.getInstance().println("Acceptor " + this.serverId + " done recovering: Current time: " + System.currentTimeMillis());
			}
			
			// If still recovering, do not execute commands on messages.
			if (this.isRecovering)
			{
				return;
			}
		}
		
		
		//**********************************************************************
		//* Acceptor received AcceptorSetRequest from a recovering Acceptor.
		//**********************************************************************
		if (message instanceof AcceptedSetRequest)
		{
			AcceptedSetRequest acceptedSetRequest = (AcceptedSetRequest) message;
			
			// Find which acceptor is recovering (who sent the message?)
			int recoveringAcceptor = acceptedSetRequest.getSenderId();
			
			// Send my accepted set to the recovering acceptor.
			AcceptedSetResponse response = new AcceptedSetResponse(this.serverId, this.accepted);
			this.network.sendMsgToServer(recoveringAcceptor, response);
		}
		
		//**********************************************************************
		//* Acceptor received p1a from a Scout.
		//**********************************************************************
		if (message instanceof P1a)
		{
			P1a p1a = (P1a) message;
			
			//Logger.getInstance().println("Acceptor got p1a from Scout " + p1a.getMyLeaderId() + ": " + p1a);
			
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
		
		
		//**********************************************************************
		//* Acceptor received p2a from a Commander.
		//**********************************************************************
		if (message instanceof P2a)
		{
			P2a p2a = (P2a) message;
			
			//Logger.getInstance().println("Acceptor got p2a from Commander " + p2a.getMyLeaderId() + ": " + p2a);
			
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
				boolean commanderBallotLarger = false;
				
				// If ballots are equal, do not call greaterThan, it will return false.
				if (!ballotsEqual)
				{
					commanderBallotLarger = !this.currBallot.greaterThan(commanderBallot);
				}
				 
				
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
