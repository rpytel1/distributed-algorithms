package src.server;


import src.util.Constant;
import src.util.IComponent;

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


/* Class that initializes the rmiregistry configurations so that the communication
 * of remote objects is possible and sets the initial values for all the remote processes 
 * according to the testcases provided as inputs. It also initiates the threads that are binded 
 * with the remote processes.
 */

public class Server {
	
	private static Map<Integer, ArrayList<Integer>> msgOrd;  
	private static IComponent[] RMI_IDS; // Array with the remote processes
	private static int numProc; // number of remote processes created
	
    public static void main(String[] args) throws AlreadyBoundException, NotBoundException, IOException, InterruptedException {
        Registry registry = LocateRegistry.createRegistry(Constant.RMI_PORT);
        
        // "clients" files contain the name of the remote processes used
        BufferedReader br = new BufferedReader(new FileReader("tests/clients.txt")); 
        String line = "";
        while ((line = br.readLine()) != null) {
            registry.bind(line, new RemoteComponentImpl());
        }
        br.close();
        numProc = registry.list().length;
        setRegistry();
        System.out.println("started");
    }

    public static void setRegistry() throws NotBoundException, NumberFormatException, IOException{
        Registry registry = LocateRegistry.getRegistry("localhost", Constant.RMI_PORT);
        RMI_IDS = new IComponent[numProc]; // the remote process array is instantiated
        Thread[] myThreads = new Thread[numProc]; // and numProc number of threads are created
        for(int i=0; i<numProc; i++){
            RMI_IDS[i] = (IComponent) registry.lookup(registry.list()[i]);
        }
        
        for(int i=0; i<numProc; i++){
        	// initialization of each remote process
            RMI_IDS[i].setEntities(RMI_IDS);
            RMI_IDS[i].setName(registry.list()[i]);
            RMI_IDS[i].setId(i);
            RMI_IDS[i].setNumProc(numProc);
            RMI_IDS[i].init(numProc);
            // a new runnable remote process is created and is binded with the process i
            RemoteProcess p = new RemoteProcess(RMI_IDS[i]);
            myThreads[i] = new Thread(p); // and a new thread is created 
        }
        
        for (int i = 0; i < numProc; i++)
		{
			myThreads[i].start();
		}
    }
}
