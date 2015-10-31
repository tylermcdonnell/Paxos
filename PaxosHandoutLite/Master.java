import java.util.Scanner;

public class Master {

  public static void main(String [] args) {
    Scanner scan = new Scanner(System.in);
    int numNodes, numClients;
    
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
      }
    }
  }
}
