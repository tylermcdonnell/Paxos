import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Scanner;

import Ballot.Ballot;
import Ballot.BallotGenerator;
import client.Client;
import client.Command;
import framework.Config;
import framework.NetController;
import message.Decision;
import message.Message;
import message.Proposal;
import message.Request;
import server.Server;

public class Master {

    // A list of queues we can talk to clients with.  The queue at index i is the
	// queue that client i is continuously listening on.
    public static ArrayList<LinkedList<String>> clientQueues = new ArrayList<LinkedList<String>>();
    
    // A list of queues we can talk to servers with.  The queue at index i is the
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
  	
  	// A list of all NetControllers.  Each server has a NetController, and all servers
  	// can talk to ever other server through their own NetController.
  	public static ArrayList<NetController> netControllers = new ArrayList<NetController>();
  	
  	// Number of clients.
  	public static int numberClients = -1;
  	
  	// Number of servers.
  	public static int numberServers = -1;
	
  public static void main(String [] args) {
    Scanner scan = new Scanner(System.in);
    int numNodes, numClients;
    
    // Added by Mike.
    int serverIndex;
    
    while (scan.hasNextLine()) {
      String [] inputLine = scan.nextLine().split(" ");
      int clientIndex, nodeIndex;
      System.out.println(inputLine[0]);
      switch (inputLine[0]) {
        
      	case "start":
      		numNodes = Integer.parseInt(inputLine[1]);
            numClients = Integer.parseInt(inputLine[2]);
            /*
             * start up the right number of nodes and clients, and store the 
             *  connections to them for sending further commands
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
             * Instruct the client specified by clientIndex to send the message
             * to the proper paxos node
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
            break;
            
        case "allClear":
            /*
             * Ensure that this blocks until all messages that are going to 
             * come to consensus in PAXOS do, and that all clients have heard
             * of them 
             */
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
            break;
            
        case "timeBombLeader":
            int numMessages = Integer.parseInt(inputLine[1]);
            /*
             * Instruct the leader to crash after sending the number of paxos
             * related messages specified by numMessages
             */ 
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
      }
    }
  }
  
  
  private static void start(int numClients, int numNodes)
  {
	  // Store global values.
      Master.numberClients = numClients;
      Master.numberServers = numNodes;
      
	  // Create NetControllers for all clients and servers.  This leaves
      // it very general, such that all clients and servers can all talk
      // to each other, if needed.
      int numNetControllers = numNodes + numClients;
      for (int i = 0; i < numNetControllers; i++)
      {
    	  createNetController(i, numNetControllers, numClients);
      }
      
      // Create servers.
      for (int i = 0; i < numNodes; i++)
      {
    	  // A queue for give this client commands.
    	  LinkedList<String> serverQueue = new LinkedList<String>();
    	  serverQueues.add(serverQueue);
    	  
    	  // Pass in ID of this server.
    	  Server server = new Server(i, getServerNetController(i), serverQueue);
    	  Thread serverThread = new Thread(server);
    	  serverThread.start();
      	
    	  // Store the thread and the underlying object.
    	  Master.serverThreads.add(serverThread);
    	  Master.serverProcesses.add(server);
      }
      
      // Create clients.
      for (int i = 0; i < numClients; i++)
      {
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
   * @param clientNum, this client's index.
   */
  public static NetController getClientNetController(int clientNum)
  {
	  return Master.netControllers.get(clientNum);
  }
  
  
  /**
   * Returns the NetController for this server.
   * 
   * @param serverNum, this server's index.
   */
  public static NetController getServerNetController(int serverNum)
  {
	  return Master.netControllers.get(Master.numberClients + serverNum);
  }
  
  
  /**
	 * Creates a NetController for the given process, described by its process
	 * number.
	 * 
	 * @param processNumber, this process' process number.
	 * 
	 * @param numProcesses, the number of processes this process can talk to.
	 * 
	 * @param numClients, the number of clients in the network.
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
	 * Tests NetController-based communication network.  Requires that clients
	 * and servers are listening for messages and printing out messages they
	 * receive.
	 */
	private static void commTest()
	{
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
	
	
	private static void sendServerDecisionTest()
	{
		start (2, 2);
    	
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
	
	
	private static void ballotTest()
	{
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
