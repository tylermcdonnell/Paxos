import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Scanner;

import ballot.Ballot;
import ballot.BallotGenerator;
import client.Client;
import client.Command;
import framework.Config;
import framework.NetController;
import message.Decision;
import message.Message;
import message.Proposal;
import message.Request;
import message.TimeBombLeader;
import server.Server;

public class Master {

	// How long ago can the last message be sent on any NetController before
	// we say the system is idle (in milliseconds)?
	public final static long ALL_CLEAR_WAIT_TIME_MS = 2000;
	
	// How long a recovering acceptor should wait to receive acceptor sets
	// from other acceptors, before it stops recovering and uses the acceptor
	// sets it has received up to this point.
	public final static long ACCEPTOR_SET_RECEIVE_WAIT_TIME = 5000;
	
	// A list of queues we can talk to clients with. The queue at index i is the
	// queue that client i is continuously listening on.
	public static ArrayList<LinkedList<String>> clientQueues = new ArrayList<LinkedList<String>>();

	// A list of queues we can talk to servers with. The queue at index i is the
	// queue that server i is continuously listening on.
	public static ArrayList<LinkedList<String>> serverQueues = new ArrayList<LinkedList<String>>();

	// A list of client thread handles.
	public static ArrayList<Thread> clientThreads = new ArrayList<Thread>();

	// A list of the process objects underlying the client thread handles.
	public static ArrayList<Client> clientProcesses = new ArrayList<Client>();

	// A list of server thread handles.
	public static ArrayList<Thread> serverThreads = new ArrayList<Thread>();

	// A list of the process objects underlying the server thread handles.
	public static ArrayList<Server> serverProcesses = new ArrayList<Server>();

	// A list of all NetControllers. Each server has a NetController, and all
	// servers
	// can talk to ever other server through their own NetController.
	public static ArrayList<NetController> netControllers = new ArrayList<NetController>();

	// Number of clients.
	public static int numberClients = -1;

	// Number of servers.
	public static int numberServers = -1;

	public static void main(String[] args) {
		Scanner scan = new Scanner(System.in);
		int numNodes, numClients;

		// Added by Mike.
		int serverIndex;

		while (scan.hasNextLine()) {
			String[] inputLine = scan.nextLine().split(" ");
			int clientIndex, nodeIndex;
			System.out.println(inputLine[0]);
			switch (inputLine[0]) {

			case "start":
				numNodes = Integer.parseInt(inputLine[1]);
				numClients = Integer.parseInt(inputLine[2]);
				/*
				 * start up the right number of nodes and clients, and store the
				 * connections to them for sending further commands
				 */

				start(numClients, numNodes);

				break;

			case "sendMessage":
				clientIndex = Integer.parseInt(inputLine[1]);
				String message = "";
				for (int i = 2; i < inputLine.length; i++) {
					message += inputLine[i];
					if (i != inputLine.length - 1) {
						message += " ";
					}
				}
				/*
				 * Instruct the client specified by clientIndex to send the
				 * message to the proper paxos node
				 */

				// Testing.
				System.out.println("Sending message to Client " + clientIndex + ": " + message);

				// Send the client the message.
				clientProcesses.get(clientIndex).giveClientCommand(message);

				break;

			case "printChatLog":
				clientIndex = Integer.parseInt(inputLine[1]);
				/*
				 * Print out the client specified by clientIndex's chat history
				 * in the format described on the handout.
				 */
				Master.clientProcesses.get(clientIndex).printChatLog();

				break;

			case "allClear":
				/*
				 * Ensure that this blocks until all messages that are going to
				 * come to consensus in PAXOS do, and that all clients have
				 * heard of them
				 */
				
				// Wait for:
				// (1) all leaders to finish recovering (if any are recovering currently)
				// (2) the last message sent on any NetController is more than one second ago
				allClear();
				
				System.out.println("All clear returned!");
				
				break;

			case "crashServer":
				nodeIndex = Integer.parseInt(inputLine[1]);

				/*
				 * Immediately crash the server specified by nodeIndex
				 */
				Master.serverThreads.get(nodeIndex).stop();

				break;

			case "restartServer":
				nodeIndex = Integer.parseInt(inputLine[1]);
				/*
				 * Restart the server specified by nodeIndex
				 */
				
				// TODO
				// Clear net controller messages before giving it to this
				// restarting server.
				// TODO
				Server server = new Server(nodeIndex, getServerNetController(nodeIndex), 
						Master.serverQueues.get(nodeIndex), Master.numberServers, 
						Master.numberClients, true, ACCEPTOR_SET_RECEIVE_WAIT_TIME);
				
				Thread serverThread = new Thread(server);
				serverThread.start();
				
				break;

			case "timeBombLeader":
				int numMessages = Integer.parseInt(inputLine[1]);
				/*
				 * Instruct the leader to crash after sending the number of
				 * paxos related messages specified by numMessages
				 */			
				// TSM: Per instructor Piazza comments, the specified number
				//		of messages should include:
				//
				//		"only unique messages to other serves. Messages within
				//		 a server (scout or commander to their own leader),
				//		 heartbeats, and client messages, do not count."
				//
				// 		Though they explicitly used the word "other" servers,
				//		they also give an example which indicates that this
				// 		includes messages sent to its own acceptor (i.e., itself).
								

				// Need to find out who current leader is. This will be called
				// after allClear is called, so when a single leader is elected.
				// How to ensure this?
				// -- Have heart beat messages include who the server thinks the
				// current leader is. Let Master access a Server public variable
				// which specifies who each process thinks the current Leader
				// is.
				// When all are in agreement, allClear is true, and we can also
				// find out who currrent leader is at that point, as well.
				// TimeBombLeader tbMessage = new TimeBombLeader();
				
				// TSM: I don't think it's enough for allClear to simply block 
				//		until all processes agree on a leader. allClear has to
				// 		block until all activity in the chat room has come to
				// 		a stand still. It seems like each process also needs to
				// 		have some sort of status indicating whether or not there
				// 		are any outstanding proposals. 
				//      
				// 		*** Unclear: is it valid to crash more than half of the 
				//		servers using the crashServer command, then send a message,
				//		and then call allClear? What would the expected behavior be?
				// 		Clearly, if more than half of the servers are dead, the
				//		protocol can not be progress. Would allClear block forever
				//		or block until processes were waiting for a majority to 
				// 		come back up?
				break;

			// Added by Mike.
			case "commTest":

				// Make sure NetControllers between servers and clients are
				// working nicely.
				commTest();

				break;

			// Added by Mike.
			case "ServQueueTest":

				start(2, 2);

				// Make sure server queues are working nicely.
				serverIndex = Integer.parseInt(inputLine[1]);
				serverQueues.get(serverIndex).add("Hello server " + serverIndex);

				break;

			// Added by Mike.
			case "sendServerDecisionTest":

				sendServerDecisionTest();

				break;

			// Added by Mike.
			case "ballotTest":

				ballotTest();

				break;

			// Added by Mike.
			case "test":

				test();

				break;

			} // End switch
		} // End while
	} // End main

	
	/**
	 * See description in main loop.
	 */
	private static void allClear()
	{
		// Wait for:
		// (1) all leaders to finish recovering (if any are recovering currently)
		// (2) the last message sent on any NetController is more than one second ago
		while (true)
		{
			boolean noLeaderRecovering = true;
			boolean noMessagesSentLastSecond = true;
			
			// Check if any leader is recovering.
			for (int i = 0; i < Master.serverProcesses.size(); i++)
			{
				Server currServer = Master.serverProcesses.get(i);
				
				// Make sure leader was created, since Server thread may have
				// not created its leader yet.
				if (currServer.leader == null)
				{
					noLeaderRecovering = false;
				}
				else
				{
					// Leader is not null, we can check its field.
					if (currServer.leader.isRecovering == true)
					{
						noLeaderRecovering = false;
					}
				}
			}
			
			// Check if any NetController has sent a message in the last second.
			long currTime = System.currentTimeMillis();
			
			// Testing.
			System.out.println("Current Time:                      " + currTime);
			System.out.println("Checking against currTime - 2000:  " + (currTime - 2000));
			
			for (int i = 0; i < Master.netControllers.size(); i++)
			{
				NetController currNetController = Master.netControllers.get(i);
				
				System.out.println("NetController last send time:      " + currNetController.lastMessageTime());
				
				if (currNetController.lastMessageTime() >= (currTime - Master.ALL_CLEAR_WAIT_TIME_MS))
				{
					// Testing.
					//System.out.println("Not a second since last send event.");
					
					noMessagesSentLastSecond = false;
				}
			}
			
			if (noLeaderRecovering && noMessagesSentLastSecond)
			{
				// Mission accomplished!
				return;
			}
		}
	}
	
	private static void test() {
		start(1, 1);

		// Send the client the message.
		clientProcesses.get(0).giveClientCommand("hi");
	}

	private static void start(int numClients, int numNodes) {
		// Store global values.
		Master.numberClients = numClients;
		Master.numberServers = numNodes;

		// Create NetControllers for all clients and servers. This leaves
		// it very general, such that all clients and servers can all talk
		// to each other, if needed.
		int numNetControllers = numNodes + numClients;
		for (int i = 0; i < numNetControllers; i++) {
			createNetController(i, numNetControllers, numClients);
		}

		// Create servers.
		for (int i = 0; i < numNodes; i++) {
			// A queue for give this client commands.
			LinkedList<String> serverQueue = new LinkedList<String>();
			serverQueues.add(serverQueue);

			// Pass in ID of this server.
			Server server = new Server(i, getServerNetController(i), 
					serverQueue, Master.numberServers, Master.numberClients, 
					false, ACCEPTOR_SET_RECEIVE_WAIT_TIME);
			
			Thread serverThread = new Thread(server);
			serverThread.start();

			// Store the thread and the underlying object.
			Master.serverThreads.add(serverThread);
			Master.serverProcesses.add(server);
		}

		// Create clients.
		for (int i = 0; i < numClients; i++) {
			// A queue for give this client commands.
			LinkedList<String> clientQueue = new LinkedList<String>();
			clientQueues.add(clientQueue);

			// Pass in ID of this client.
			Client client = new Client(i, clientQueue, getClientNetController(i), numClients, numNodes);
			Thread clientThread = new Thread(client);
			clientThread.start();

			// Store the thread and the underlying object.
			Master.clientThreads.add(clientThread);
			Master.clientProcesses.add(client);
		}
	}

	/**
	 * Returns the NetController for this client.
	 * 
	 * @param clientNum,
	 *            this client's index.
	 */
	public static NetController getClientNetController(int clientNum) {
		return Master.netControllers.get(clientNum);
	}

	/**
	 * Returns the NetController for this server.
	 * 
	 * @param serverNum,
	 *            this server's index.
	 */
	public static NetController getServerNetController(int serverNum) {
		return Master.netControllers.get(Master.numberClients + serverNum);
	}

	/**
	 * Creates a NetController for the given process, described by its process
	 * number.
	 * 
	 * @param processNumber,
	 *            this process' process number.
	 * 
	 * @param numProcesses,
	 *            the number of processes this process can talk to.
	 * 
	 * @param numClients,
	 *            the number of clients in the network.
	 * 
	 * @return a NetController for the given process, described by its process
	 *         number.
	 */
	private static NetController createNetController(int processNumber, int numProcesses, int numClients) {

		// **********************************************************************
		// * Setup communication for this process with all other processes.
		// **********************************************************************
		NetController nc = null;

		// Dynamically create a config file for this process.
		// Reuse the same file for all processes.
		String fileName = "config.txt";
		File file = new File(fileName);

		PrintWriter out = null;
		try {
			out = new PrintWriter(file);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		// NumProcesses field.
		out.println("NumProcesses=" + numProcesses);

		// ProcNum field.
		out.println("ProcNum=" + processNumber);

		// host fields.
		for (int i = 0; i < numProcesses; i++) {
			out.println("host" + i + "=localhost");
		}

		// port fields.
		for (int i = 0; i < numProcesses; i++) {
			out.println("port" + i + "=" + (6100 + i));
		}

		out.flush();
		out.close();

		try {

			Config config = new Config(fileName);
			nc = new NetController(config, numClients);

		} catch (Exception e) {
			e.printStackTrace();
		}

		// Add this to the list of net controllers in the entire system.
		netControllers.add(nc);

		return nc;
	}

	/**
	 * Tests NetController-based communication network. Requires that clients
	 * and servers are listening for messages and printing out messages they
	 * receive.
	 */
	private static void commTest() {
		start(2, 2);

		// Client 0 sends msg to everyone.
		Master.clientProcesses.get(0).testClientSend("Hello client 0 from client 0", 0);
		Master.clientProcesses.get(0).testClientSend("Hello client 1 from client 0", 1);
		Master.clientProcesses.get(0).testServerSend("Hello server 0 from client 0", 0);
		Master.clientProcesses.get(0).testServerSend("Hello server 1 from client 0", 1);

		// Client 1 sends msg to everyone.
		Master.clientProcesses.get(1).testClientSend("Hello client 0 from client 1", 0);
		Master.clientProcesses.get(1).testClientSend("Hello client 1 from client 1", 1);
		Master.clientProcesses.get(1).testServerSend("Hello server 0 from client 1", 0);
		Master.clientProcesses.get(1).testServerSend("Hello server 1 from client 1", 1);

		// Server 0 sends msg to everyone.
		Master.serverProcesses.get(0).testClientSend("Hello client 0 from server 0", 0);
		Master.serverProcesses.get(0).testClientSend("Hello client 1 from server 0", 1);
		Master.serverProcesses.get(0).testServerSend("Hello server 0 from server 0", 0);
		Master.serverProcesses.get(0).testServerSend("Hello server 1 from server 0", 1);

		// Server 1 sends msg to everyone.
		Master.serverProcesses.get(1).testClientSend("Hello client 0 from server 1", 0);
		Master.serverProcesses.get(1).testClientSend("Hello client 1 from server 1", 1);
		Master.serverProcesses.get(1).testServerSend("Hello server 0 from server 1", 0);
		Master.serverProcesses.get(1).testServerSend("Hello server 1 from server 1", 1);
	}

	private static void sendServerDecisionTest() {
		start(2, 2);

		// Send the server a fake decision.
		int serverIndex = 1;

		// Send from client 0 WLOG (doesn't matter who sends it).
		// Should be added to decisions.
		Message m = new Decision(new Proposal(1, new Command(0, 0, "HEY LOL")));
		Master.netControllers.get(0).sendMsgToServer(serverIndex, m);

		// Should be added to decisions.
		Message m2 = new Decision(new Proposal(1, new Command(0, 0, "HEY LAWL")));
		Master.netControllers.get(0).sendMsgToServer(serverIndex, m2);

		// Should not be added to decisions.
		Master.netControllers.get(0).sendMsgToServer(serverIndex, m);
	}

	private static void ballotTest() {
		BallotGenerator bg0 = new BallotGenerator(0);
		BallotGenerator bg1 = new BallotGenerator(1);

		Ballot firstBallot0 = bg0.getCurrentBallot();
		Ballot firstBallot1 = bg1.getCurrentBallot();

		System.out.println("x.equals(x) (true): " + firstBallot0.equals(firstBallot0));

		System.out.println("x = First ballot leader 0: " + firstBallot0);
		System.out.println("y = First ballot leader 1: " + firstBallot1);

		System.out.println("x > y (false): " + firstBallot0.greaterThan(firstBallot1));
		System.out.println("x.equals(y) (false): " + firstBallot0.equals(firstBallot1));

		Ballot secondBallot0 = bg0.getNextBallot();
		Ballot secondBallot1 = bg1.getNextBallot();
		System.out.println("x = Second ballot leader 0: " + secondBallot0);
		System.out.println("y = Second ballot leader 1: " + secondBallot1);

		System.out.println("x > y (false): " + secondBallot0.greaterThan(secondBallot1));

		Ballot thirdBallot0 = bg0.getNextBallot();
		System.out.println("x = Third ballot leader 0: " + thirdBallot0);
		System.out.println("y = Second ballot leader 1: " + secondBallot1);

		System.out.println("x > y (true): " + thirdBallot0.greaterThan(secondBallot1));

		System.out.flush();
		System.exit(-1);
	}
}
