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
import java.util.Map;


/* Class that initializes the rmiregistry configurations so that the communication
 * of remote objects is possible.
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
    	numProc = registry.list().length;
        // "clients" files contain the name of the remote processes used
        BufferedReader br = new BufferedReader(new FileReader("tests/clients3.txt")); 
        String line = "";
        while ((line = br.readLine()) != null) {
        	String[] split_line = line.split(" ");
        	registry.bind("//localhost:"+Constant.RMI_PORT+"/"+split_line[0], new RemoteEntityImpl());
        }
        br.close();
        
        //setRegistry();
        System.out.println("started");
    }   
}
