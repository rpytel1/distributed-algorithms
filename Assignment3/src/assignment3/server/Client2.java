package assignment3.server;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.PriorityQueue;
import java.util.Scanner;

import assignment3.link.Link;
import assignment3.node.IComponent;
import assignment3.node.Node;

public class Client2 {
	private static IComponent[] RMI_IDS; // Array with the remote processes
	private static int numProc; // number of remote processes created
	private static int localProc; // number of local processes
	private static int local[]; // array that keeps info about which of the processes are local
	private static ArrayList<Integer> localIDS; // ids of local processes
	
    public static void main(String[] args) throws AlreadyBoundException, NotBoundException, IOException, InterruptedException {
    	
        // "clients" files contain the name of the remote processes used
        BufferedReader br = new BufferedReader(new FileReader("tests/nodes.txt"));
        String line = br.readLine();
        numProc = Integer.parseInt(line);
        localProc = 0;
        int i = 0;
        local = new int[numProc];
        localIDS = new ArrayList<>();
        while ((line = br.readLine()) != null) {
        	String[] split_line = line.split(" ");
        	if(Integer.parseInt(split_line[1]) == 2){
        		local[i] = 1; // check if the process is local
            	localProc++;
            	localIDS.add(i);
        	}
        	else{
        		local[i] = 0; 
        	}
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
    	boolean success = false;
    	Registry registry = null;
		while (!success){
    		try{
    			registry = LocateRegistry.getRegistry("145.94.186.211", Constant.RMI_PORT);
    			success = true;
    		}
    		catch (RemoteException e) {
                e.printStackTrace();
            }
		}
    	//Registry registry = LocateRegistry.getRegistry("145.94.186.211", Constant.RMI_PORT);
        RMI_IDS = new IComponent[numProc]; // the remote process array is instantiated
        Thread[] myThreads = new Thread[numProc]; // and numProc number of threads are created
        for(int i=0; i<numProc; i++){
            RMI_IDS[i] = (IComponent) registry.lookup(registry.list()[i]);
        }
        
        for(int i=0; i<localProc; i++){
        	// initialization of each remote process of the client
            RMI_IDS[localIDS.get(i)].setEntities(RMI_IDS);
            RemoteProcess p = new RemoteProcess(RMI_IDS[localIDS.get(i)]);
            myThreads[i] = new Thread(p); // and a new thread is created 
        }
        
        System.out.println("Press enter to continue");
        Scanner scan = new Scanner(System.in);
        scan.nextLine();
        for (int i = 0; i < localProc; i+=2) myThreads[i].start();
    }
}