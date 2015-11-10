package server;

import log.Logger;

/**
 * A timebomb for Paxos failure testing.
 * 
 * Per the project description, a timebomb will kill the specified leader after 
 * sending the specified number of "unique messages to other servers. Messages
 * within a server (scout or commander to leader), heartbeats, and client messages 
 * do not count."
 *
 */
public class Timebomb {
	
	private volatile int countdown;
	private volatile Boolean active;
	
	public Timebomb()
	{
		this.active 		= false;
		this.countdown   = 0;
	}
	
	/**
	 * Sets timebomb to active.
	 * @param countdown
	 * 			After countdown messages are received (counted by
	 * 			calls to tick), the thread calling tick will 
	 *			immediately die. All countdown values lower than
	 *			1 will be default to the same behavior as if 1 was
	 *			provided.
	 */
	public synchronized void set(int countdown)
	{
		synchronized (this.active)
		{
			this.active 	= true;
			this.countdown  = countdown;
		}
	}
	
	/**
	 * This notifies the time bomb that the associated object
	 * has sent a message. This must be called by the thread
	 * who is the subject of the timebomb (i.e., the leader).
	 * When the countdown reaches 0, this will kill the calling
	 * thread immediately.
	 */
	public synchronized void tick()
	{
		synchronized (this.active)
		{
			if (this.active)
			{				
				if (--this.countdown <= 0)
				{
					// Bomb current thread.
					Logger.getInstance().println("BOOM");
					this.active = false;
					Thread.currentThread().stop();
				}
				Logger.getInstance().println("Tick: " + this.countdown);
			}
		}
	}
}
