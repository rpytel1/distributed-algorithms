package server;

import util.Buffer;
import util.Constant;
import util.IRemoteEntity;

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

import clock.VectorClock;
import message.Message;

/* Class that initializes the rmiregistry configurations so that the communication
 * of remote objects is possible and sets the initial values for all the remote processes 
 * according to the testcases provided as inputs. It also initiates the threads that are binded 
 * with the remote processes.
 */

public class Server {
	
	private static int msgNum[]; // Array with the number of messages for each process
	// Map with the delays used to set the order in which each message is sent by each process
	private static Map<Integer, ArrayList<Integer>> msgOrd;  
	private static IRemoteEntity[] RMI_IDS; // Array with the remote processes
	private static int msgId; 
	private static int numProc; // number of remote processes created
	
    public static void main(String[] args) throws AlreadyBoundException, NotBoundException, IOException, InterruptedException {
        Registry registry = LocateRegistry.createRegistry(Constant.RMI_PORT);
        
        // "clients" files contain the name of the remote processes used
        BufferedReader br = new BufferedReader(new FileReader("tests/clients3.txt")); 
        String line = "";
        while ((line = br.readLine()) != null) {
            registry.bind(line, new RemoteEntityImpl());
        }
        br.close();
        numProc = registry.list().length;
        setRegistry();
        System.out.println("started");
    }

    public static void setRegistry() throws NotBoundException, NumberFormatException, IOException{
        Registry registry = LocateRegistry.getRegistry("localhost", Constant.RMI_PORT);
        RMI_IDS = new IRemoteEntity[numProc]; // the remote process array is instantiated
        Thread[] myThreads = new Thread[numProc]; // and numProc number of threads are created
        for(int i=0; i<numProc; i++){
            RMI_IDS[i] = (IRemoteEntity) registry.lookup(registry.list()[i]);
        }
        
        // "messages" files contain the messages to be sent and are constructed in the following way
        // senderID messageText receiverID deliveryDelay sendingDelay
        BufferedReader br = new BufferedReader(new FileReader("tests/messages3.txt"));
		String line = "";
		Message temp;
		msgNum = new int[numProc];
		msgOrd = new HashMap<Integer, ArrayList<Integer>>();
		// initialization of the 2 structures associated with the message ordering
		for (int i=0; i<numProc; i++){
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
			// create the new message 
			temp = new Message(msgId, msgText, vt, new Buffer(), sender, receiver, delay);
			(RMI_IDS[sender]).addMessageToBeSent(temp); // and added to a list of the messages to be sent of the sender
			msgNum[sender]++; // increase the number of messages of process with the sender id
			msgId++;
			ArrayList<Integer> tmp = new ArrayList<Integer>(msgOrd.get(sender));
			tmp.add(Integer.parseInt(split_line[4])); // add to the arrayList with the delays of the sender
			msgOrd.put(sender, tmp); // the delay associated with sending the message
			// these delays are used so that we can keep easily an ordering on the time the messages 
			// are sent by different processes
		}
		br.close();
		
        for(int i=0; i<numProc; i++){
        	// initialization of each remote process
            RMI_IDS[i].setEntities(RMI_IDS);
            RMI_IDS[i].setName(registry.list()[i]);
            RMI_IDS[i].setId(i);
            RMI_IDS[i].setVectorClock(i,numProc);
            // a new runnable remote process is created and is binded with the process i
            RemoteProcess p = new RemoteProcess(RMI_IDS[i], msgNum[i], msgOrd.get(i));
            myThreads[i] = new Thread(p); // and a new thread is created 
        }
        
        for (int i = 0; i < numProc; i++)
		{
			myThreads[i].start();
		}
    }
}
