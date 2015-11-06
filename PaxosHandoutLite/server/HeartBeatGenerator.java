package server;

import java.util.ArrayList;

import framework.NetController;
import message.HeartBeat;

/**
 * Sends heart beats on a scheduled interval to other servers (or leaders).
 * @author Mike Feilbach
 *
 */
public class HeartBeatGenerator 
{
	// Which server, or leader, is sending these heart beats.
	private int senderId;
	
	private NetController network;
	
	// Number of servers to send the heart beats to.  Their IDs are
	// 0...(numServers - 1).
	private int numServers;
	
	// How often to send heart beats to all servers.  The update system
	// view period should be a multiple of this, in case we don't always
	// send heart beats at the heart beat period exactly from other work
	// a server may be handling at the time.  This is in ms.
	private long heartBeatPeriod;
	
	// How often we will analyze the heat beat messages that came in in the
	// previous period, and determine who is "dead" and who is "alive" at
	// this time.  This is in ms.
	private long updateSystemViewPeriod;
	
	// Time to send heat beat next.
	private long nextHeartBeatTime;
	
	// Time to update system view next.  This is in ms.
	private long nextUpdateTime;
	
	// Who we have heard from in the last update period.
	private Boolean[] heartBeatsInLastUpdatePeriod;
	
	public HeartBeatGenerator(int senderId, NetController network, int numServers, int heartBeatPeriod, int updateSystemViewPeriod)
	{
		this.senderId = senderId;
		this.network = network;
		this.numServers = numServers;
		this.heartBeatPeriod = heartBeatPeriod;
		this.updateSystemViewPeriod = updateSystemViewPeriod;
		
		// Prime the generator.
		this.nextHeartBeatTime = -1;
		this.nextUpdateTime = -1;
		
		// Set to false for all leaders.
		this.heartBeatsInLastUpdatePeriod = new Boolean[numServers];
		
		for (int i = 0; i < numServers; i++)
		{
			this.heartBeatsInLastUpdatePeriod[i] = false;
		}
	}
	
	
	/**
	 * Sends a heart beat if the time is appropriate.  Updates system view
	 * if time is appropriate.
	 * 
	 * @param currentLeader, the current leader from the view of who is
	 * calling this method.
	 * 
	 * @return A list of the leader IDs who we believe are dead (we have not
	 * gotten a heart beat from them in the last update period).
	 */
	public ArrayList<Integer> beatAndAnalyze(int currentLeader)
	{
		ArrayList<Integer> deadLeaders = new ArrayList<Integer>();
		
		HeartBeat hb = new HeartBeat(this.senderId, currentLeader);
		long currTime = System.currentTimeMillis();
		
		// If we haven't send a heart beat yet, prime it.
		if (this.nextHeartBeatTime == -1)
		{
			this.nextHeartBeatTime = currTime;
		}
		
		// If we haven't send a heart beat yet, prime it.
		if (this.nextUpdateTime == -1)
		{
			nextUpdateTime = currTime + this.updateSystemViewPeriod;
		}
		
		// If it is time to send another heart beat.
		if (currTime >= this.nextHeartBeatTime)
		{
			// Send this heart beat to all servers (including my own).
			for (int i = 0; i < numServers; i++)
			{
				this.network.sendMsgToServer(i, hb);
			}
			
			this.nextHeartBeatTime = currTime + this.heartBeatPeriod;
			
			//System.out.println("Leader " + this.senderId + " Sent heart beat: " + currTime);
		}
		
		
		// If it is time to update the system view.
		if (currTime >= this.nextUpdateTime)
		{
			//System.out.println("Leader " + this.senderId + " Updating system view: " + currTime);
			
			this.nextUpdateTime = currTime + this.updateSystemViewPeriod;
			
			// Send back list of leader IDs who we did not hear from in last
			// update period (those we think are dead).
			for (int i = 0; i < this.heartBeatsInLastUpdatePeriod.length; i++)
			{
				if (this.heartBeatsInLastUpdatePeriod[i] == false)
				{
					deadLeaders.add(i);
				}
			}
			
			// Clear data from last update period.
			this.clearHeartBeatsArray();
		}
		
		return deadLeaders;
	}
	
	
	public void addBeat(HeartBeat hb)
	{
		int leaderId = hb.getSenderId();
		this.heartBeatsInLastUpdatePeriod[leaderId] = true;
	}
	
	
	public void clearHeartBeatsArray()
	{
		for (int i = 0; i < this.numServers; i++)
		{
			this.heartBeatsInLastUpdatePeriod[i] = false;
		}
	}
}
