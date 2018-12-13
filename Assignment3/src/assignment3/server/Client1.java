package assignment3.server;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Scanner;

import assignment3.link.Link;
import assignment3.link.LinkState;
import assignment3.node.IComponent;
import assignment3.node.Node;

public class Client1 {
	private static IComponent[] RMI_IDS; // Array with the remote processes
	private static int numProc; // number of remote processes created
	private static int localProc; // number of local processes
	private static int local[]; // array that keeps info about which of the processes are local
	private static List<Integer> localIDS; // ids of local processes
	private static Map<Integer, List<Link>> links;
	
    public static void main(String[] args) throws AlreadyBoundException, NotBoundException, IOException, InterruptedException {
    	Registry registry = LocateRegistry.createRegistry(Constant.RMI_PORT);
    	links = new HashMap<Integer, List<Link>>();
    	initializeEdges();
        // "clients" files contain the name of the remote processes used
        BufferedReader br = new BufferedReader(new FileReader("tests/nodes.txt")); 
        String line = br.readLine();
        numProc = Integer.parseInt(line);
        localProc = 0;
        int i = 0;
        local = new int[numProc];
        localIDS = new ArrayList<Integer>();
        while ((line = br.readLine()) != null) {
        	String[] split_line = line.split(" ");
        	if(Integer.parseInt(split_line[1]) == 1){
        		registry.bind("//localhost:"+Constant.RMI_PORT+"/"+split_line[0], 
        				new Node(i, new PriorityQueue<Link>(links.get(i))));
        		local[i] = 1; // check if the process is local
            	localProc++;
            	localIDS.add(i);
        	}
        	else{
        		registry.bind("//145.94.233.58:"+Constant.RMI_PORT+"/"+split_line[0], new Node(i, new PriorityQueue<Link>(links.get(i))));
        		local[i] = 0;

        	}
        	i++;
        }
        br.close();
        System.out.println("Press enter to continue");
        Scanner scan = new Scanner(System.in);
        scan.nextLine();
        setRegistry();
        System.out.println("Client 1 started");
    }
    
    public static void initializeEdges() throws IOException{
    	BufferedReader br = new BufferedReader(new FileReader("tests/edges.txt")); 
        String line;
        int node1;
        int node2;
        double weight;
        int delay;
        while ((line = br.readLine()) != null) {
        	String[] split_line = line.split(" ");
        	node1 = Integer.parseInt(split_line[0]);
        	node2 = Integer.parseInt(split_line[1]);
        	weight = Double.parseDouble(split_line[2]);
        	delay = Integer.parseInt(split_line[3]);
        	if(links.get(node1)!=null){
        		List<Link> temp = links.get(node1);
        		temp.add(new Link(LinkState.CANDIDATE_IN_MST, node1, node2, weight, delay));
        		links.put(node1, temp);
        	}
        	else{
        		List<Link> temp = new ArrayList<>();
        		temp.add(new Link(LinkState.CANDIDATE_IN_MST, node1, node2, weight, delay));
        		links.put(node1, temp);
        	}
        	if(links.get(node2)!=null){
        		List<Link> temp = links.get(node2);
        		temp.add(new Link(LinkState.CANDIDATE_IN_MST, node1, node2, weight, delay));
        		links.put(node2, temp);
        	}
        	else{
        		List<Link> temp = new ArrayList<>();
        		temp.add(new Link(LinkState.CANDIDATE_IN_MST, node1, node2, weight, delay));
        		links.put(node2, temp);
        	}
        }
        br.close();
    }

    public static void setRegistry() throws NotBoundException, NumberFormatException, IOException{
    	Registry registry = LocateRegistry.getRegistry("localhost", Constant.RMI_PORT);
        RMI_IDS = new IComponent[numProc]; // the remote process array is instantiated
        Thread[] myThreads = new Thread[numProc]; // and numProc number of threads are created
        for(int i=0; i<numProc; i++){
            RMI_IDS[i] = (IComponent) registry.lookup(registry.list()[i]);
        }
        
        for(int i=0; i<localProc; i++){
        	// initialization of each remote process of the client
            RMI_IDS[localIDS.get(i)].setEntities(RMI_IDS);
            // a new runnable remote process is created and is binded with the process i
            RemoteProcess p = new RemoteProcess(RMI_IDS[localIDS.get(i)]);
            myThreads[i] = new Thread(p); // and a new thread is created 
        }
        
        System.out.println("Press enter to continue");
        Scanner scan = new Scanner(System.in);
        scan.nextLine();
         myThreads[0].start();
    }
}
