package server;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import clock.VectorClock;
import message.Message;
import util.Buffer;
import util.Constant;
import util.IRemoteEntity;
import server.RemoteProcess;

/* The client class that generates some of the processes and sets the initial values for all those 
 * remote processes according to the testcases provided as inputs. It also initiates the threads that 
 * are binded with the remote processes. This class is used to run processes on different JVMs.
 */

public class Client2 {
	private static int msgNum[]; // Array with the number of messages for each process
	// Map with the delays used to set the order in which each message is sent by each process
	private static Map<Integer, ArrayList<Integer>> msgOrd;  
	private static IRemoteEntity[] RMI_IDS; // Array with the remote processes
	private static int msgId; 
	private static int numProc; // total number of remote processes created
	private static int localProc; // number of local processes
	private static int local[]; // array that keeps info about which of the processes are local
	private static ArrayList<Integer> localIDS; // ids of local processes
	
    public static void main(String[] args) throws AlreadyBoundException, NotBoundException, IOException, InterruptedException {    
	    
    	// "clients" files contain the name of the remote processes used
	    BufferedReader br = new BufferedReader(new FileReader("tests/clients3.txt"));
	    String line = br.readLine();
	    numProc = Integer.parseInt(line);
        localProc = 0;
        int i = 0;
        local = new int[numProc];
        localIDS = new ArrayList<Integer>();
        while ((line = br.readLine()) != null) {
        	String[] split_line = line.split(" ");
            if(Integer.parseInt(split_line[1]) == 2){ 
            	local[i] = 1; // check if the process is local
            	localProc++;
            	localIDS.add(i);
            }
            else local[i] = 0; 
            i++; 
        }
        br.close();
        System.out.println("Press enter to continue");
        Scanner scan = new Scanner(System.in);
        scan.nextLine();
        setRegistry();
        System.out.println("Client 2 started");
    }

    public static void setRegistry() throws NotBoundException, NumberFormatException, IOException{
        Registry registry = LocateRegistry.getRegistry("145.94.233.17", Constant.RMI_PORT);
        RMI_IDS = new IRemoteEntity[numProc]; // the remote process array is instantiated
        Thread[] myThreads = new Thread[localProc]; // and localProc number of threads are created
        for(int i=0; i<numProc; i++){
        	RMI_IDS[i] = (IRemoteEntity) registry.lookup(registry.list()[i]);
        }
        
        // "messages" files contain the messages to be sent and are constructed in the following way
        // senderID messageText receiverID deliveryDelay sendingDelay
        BufferedReader br = new BufferedReader(new FileReader("tests/messages3.txt"));
		String line = "";
		Message temp;
		msgNum = new int[localProc];
		msgOrd = new HashMap<Integer, ArrayList<Integer>>();
		// initialization of the 2 structures associated with the message ordering
		for (int i=0; i<localProc; i++){
			msgNum[i] = 0;
			msgOrd.put(i, new ArrayList<Integer>());
		}
		msgId = 0;
		while ((line = br.readLine()) != null) {
			String[] split_line = line.split(" ");
			int sender = Integer.parseInt(split_line[0]); // get sender from file
			String msgText = split_line[1]; // get text from file
			int receiver = Integer.parseInt(split_line[2]); // get receiver from file
			int delay = Integer.parseInt(split_line[3]); // get message delivery delay from file
			VectorClock vt = new VectorClock(sender,numProc); // initialize the vector clock of the message
			// create the new message only for the local processes 
			if (local[sender]==1){
				temp = new Message(msgId, msgText, vt, new Buffer(), sender, receiver, delay);
				(RMI_IDS[sender]).addMessageToBeSent(temp); // and added to a list of the messages to be sent of the sender
				msgNum[localIDS.indexOf(sender)]++; // increase the number of messages of process with the sender id
				ArrayList<Integer> tmp = new ArrayList<Integer>(msgOrd.get(localIDS.indexOf(sender)));
				tmp.add(Integer.parseInt(split_line[4])); // add to the arrayList with the delays of the sender
				msgOrd.put(localIDS.indexOf(sender), tmp); // the delay associated with sending the message
				// these delays are used so that we can keep easily an ordering on the time the messages 
				// are sent by different processes
			}
			msgId++;
			
		}
		br.close();
		
        for(int i=0; i<localProc; i++){
        	// initialization of each remote process of the client
        	RMI_IDS[localIDS.get(i)].setEntities(RMI_IDS);
            RMI_IDS[localIDS.get(i)].setName(registry.list()[localIDS.get(i)]);
            RMI_IDS[localIDS.get(i)].setId(localIDS.get(i));
            RMI_IDS[localIDS.get(i)].setVectorClock(localIDS.get(i),numProc);
            // a new runnable remote process is created and is binded with the process i
            RemoteProcess p = new RemoteProcess(RMI_IDS[localIDS.get(i)], msgNum[i], msgOrd.get(i));
            myThreads[i] = new Thread(p); // and a new thread is created 
        }
        
        System.out.println("Press enter to continue");
        Scanner scan = new Scanner(System.in);
        scan.nextLine();
        for (int i = 0; i < localProc; i++) myThreads[i].start();
    }
}