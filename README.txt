README - P2

Mike Feilbach (mfeilbach@utexas.edu), mjf2628, feilbach
Tyler McDonnell (tyler@cs.utexas.edu), tsm563, tyler
University of Texas at Austin
CS 380D -- Distributed Computing I
Professor Lorenzo Alvisi
Fall 2015

MIKE:
Slip days used (this project): 2
Slip days used (total)       : Homework: 4, Projects: 2

TYLER:
Slip days used (this project): 2
Slip days used (total):        Homework: 0, Projects: 2

--------------------------------------------------------------------------------
- Notes:
--------------------------------------------------------------------------------

This implementation of Paxos is strongly based on Robbert van Renesse's 
paper, "Paxos Made Moderately Complex." 

Project assumptions:

1. All servers cannot be crashed at the same time. In other words, there is
   at least one process (leader, replica, acceptor) alive which contains the
   entire application state. Other processes, when recovering, retrieve this
   state via recovery. Due to this assumption, no processes implement stable
   storage.
   
2. After restarting a server, allClear will be called before any additional
   commands. See the API below for more informationregarding  allClear. 
   allClear ensures (a) that the process has had to time to retrieve 
   application state, a component of recovery; and (b) that a new leader has 
   been decided upon by the leader election protocol.


Here is a list of implementation-specific design decisions:

1. Each server is a single process that acts as a leader, replica, and acceptor. 
   Paxos itself only requires f + 1 leaders and acceptors, while it requires 
   2f + 1 acceptors. Also, it does not specify anything about co-location of
   these entities within the same process. 
   
2. Server 0 is the initial primary leader (the only leader who may propose).

3. We use a keep-alive based system as a failure detector. That is, processes
   may determine a process is dead if they fail to receive a keep-alive message
   from that system in some configurable time.
   
3. The system uses a view change model for leader election. In particular, all
   leaders maintain an integer expressing who they think the current leader is.
   If they detect, via the keep-alive failure detection mechanism, that the 
   current leader is dead, they increment the index. This index corresponds to
   who they now think is the new leader.  In addition, if a leader X sees that 
   some other leader Y has a higher index, X will update its index to the
   higher value. (e.g., if leader X has index = 3, and it finds that leader Y
   has index = 5, leader X will change its index to 5).  Note that index is
   a running count, it can take on values past (n - 1).  In other words, if
   the index is 5 and there are 5 servers in the system, the current leader
   is on server (5 mod n) = (5 mod 5) = 0.
   
4. When a process first becomes leader, it immediately spawns scouts (i.e., 
   sends out p1a messages) as described in "Paxos Made Moderately Complex."
   This means that the timeBombLeader command cannot be used to deterministically
   limit p1a messages. For this reason, we have added an additional timeBomb
   command, which can be used to specify a non-leader process. To limit p1a
   messages of a leader, you simply call timeBomb on them immediately prior
   to them becoming leader.
   
5. This implementation is the "inefficient" implementation of Paxos described
   in Renesse's paper. In particular, a leader obtains the entire history of 
   all accepted pvalues when it sends out a scout, rather than only the most
   recently accepted pvalue for a slot.
   
6. On the client side, we have no concept of a primary server. A client issues
   each request to ALL servers, and might receive responses from one or more
   servers.
  
--------------------------------------------------------------------------------
- Interface Provided: 
--------------------------------------------------------------------------------

start <numberOfServers> <numberOfClients> 

	Starts a chat room with the specified number of clients and servers.
	
sendMessage <index> <message> 
		
	Tells a client to send a message to the chat room. The message will be
	every argument after the specified index. This call is asynchronous to
	the running of both the clients and servers.
	
crashServer <index>

	Immediately kills the specified server.
	
restartServer <index>

	Restarts a server that was previously crashed.
	
allClear

	Blocks (from the perspective of the test runner) until the state of
	participants (clients + servers) has stabilized. If a system has
	reached a stable point as a result of allClear, this means that the
	system state will not change until some action is taken, for instance
	crashing or restarting a process or sending a new message.
	
timeBombLeader <numberOfMessages>

	Tells the current leader of the system to crash itself after sending
	the specified number of server-side Paxos messages. The messages 
	counted as "server-side Paxos messages" are the p1a and p2a messages
	outlined in the literature: see Leslie Lamport's "Paxos Made Simple" 
	and Robbert van Renesse's "Paxos Made Moderately Complex".
	
timeBomb <index> <numberOfMessages>

	Tells a specified process to crash itself after sending the specified
	number of P1A and P2A messages. See timeBombLeader for more information
	on these messages. For more information on how this command is distinct
	from timeBombLeader, see our Design Description.
	
printChatLog <index>

	Prints the client's view of the chat record in the following format:
	
	[sequenceNumber] [senderIndex]: message
	
	where sequenceNumber is the index in a total ordering of slot decisions
	excluding NOPs (Olive Days).
	
whois <index>

	(Debugging Only). For the server specified by index, prints a state
	dump of that server's view of whether each other server in the system
	is alive or dead and who is the leader. You may also specify "a" rather
	than an index, which will print the state for all live processes.