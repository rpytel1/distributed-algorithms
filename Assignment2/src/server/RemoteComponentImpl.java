package src.server;

import src.util.IComponent;
import src.util.Token;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Arrays;

/* Class with remote methods that contains all the functionalities of 
 * request and token sending and needed in implementing Singhal’s algorithm 
 * for token-based mutual exclusion
 */

public class RemoteComponentImpl extends UnicastRemoteObject implements IComponent {

	private static final long serialVersionUID = 1L;
	private int id; // the process id 
    private String name; // the process name
    private IComponent[] RD; // the array with the info of all the remote processes 
    private int[] N; // the local array of the number of requests of all the processes
    private String[] States; // the local array of the state of all the processes
    private int numProc;
    private Token tk;

    protected RemoteComponentImpl() throws RemoteException {
        super();
    }
    
    @Override
    public void init(int n){
    	this.N = new int[n];
        this.States = new String[n];
        this.tk = new Token(n);
        for (int i=0; i<n; i++)	N[i] = 0;
        // different type of initialization for process 0 and the others
        if(this.id == 0){
        	this.States[0] = "H";
        	for (int i=1; i<n; i++) this.States[i] = "O";
        }
        else{
        	for (int i=0; i<this.id; i++) this.States[i] = "R";
        	for (int i=this.id; i<n; i++) this.States[i] = "O";
        }
    }
     
    /**
     * Broadcast request to the components that it is suspected to possess the token
     */
    @Override
    public void sendRequest() throws RemoteException{
    	if (this.States[this.id].equals("H")){	
    		System.out.println("Process " + this.id +" Sending request to itself");
    		RD[this.id].receiveRequest(this.id,this.N[this.id]);
    	}	
    	else{
	    	this.States[this.id] = "R";
	    	this.N[this.id] += 1;
	    	int i = 0;
	    	while(i<this.numProc){
	    		if (i!=this.id){
	    			if (this.States[i].equals("R")){
	    				System.out.println("Process " + this.id +" Sending request to process "+ i);
	    				RD[i].receiveRequest(this.id,this.N[this.id]);
	    			}
	    		}
	    		i+=1;
	    	}       
    	}
    }

    /* Method for receiving a token request 
     */
    @Override
    public synchronized void receiveRequest(int reqId, int numReq) throws RemoteException {
        // Update requests
        this.N[reqId] = numReq;
        if(this.States[this.id].equals("O") || this.States[this.id].equals("E"))
        	this.States[reqId] = "R";
        else if (this.States[this.id].equals("R")){
        	if (!this.States[reqId].equals("R")){
        		this.States[reqId] = "R";
        		RD[reqId].receiveRequest(this.id,this.N[this.id]);
        	}	
        }
        else{
        	this.States[reqId] = "R";
        	this.States[this.id] = "O";
        	this.tk.setTS(reqId, "R");
        	this.tk.setTN(reqId, numReq);
        	System.out.println("Process " + this.id +" sending token to process "+ reqId);
        	RD[reqId].receiveToken();
        }
	        
	}
	
    /* Method for receiving the token 
     */
    @Override
    public void receiveToken() throws RemoteException {
    	
        System.out.println("Token received by process " + this.id);
        this.States[this.id] = "E";
        criticalSection();
        this.States[this.id] = "O";
        this.tk.setTS(this.id, "O");
        for (int i=0; i<this.numProc; i++){
        	if (this.N[i]>this.tk.getTN(i)){
        		this.tk.setTN(i, this.N[i]);
        		this.tk.setTS(i, this.States[i]);
        	}
        	else{
        		this.N[i] = this.tk.getTN(i);
        		this.States[i] = this.tk.getTS(i);
        	}
        }
        boolean flag = true;
        for (int i=0; i<this.numProc; i++){
        	if(!this.States[i].equals("O")){
        		flag = false;
        	}
        }
        if (flag) this.States[this.id] = "H";
        else{
        	for(int i = 1; i < this.numProc+1; i++) {               
                // Cyclic check
                int reqID = (this.id + i)%this.numProc;   
                // Send to first process with outstanding request
                if (this.States[reqID].equals("R")){
                	System.out.println("Process " + this.id +" sending token to process "+ reqID);
                	RD[reqID].receiveToken();
                	break;
                }
        	}
        }
	}
    
    /* Method implementing the critical section of each process
     */
    private void criticalSection() {
        //this.States[this.id] = "E";
        System.out.println("Process "+ this.id + " entering critical section");
        try {
            Thread.sleep((int) (Math.random()*1000));
        } catch (InterruptedException e) {
        	System.out.println("Critical section interrupted");
        }
        System.out.println("Process "+ this.id + " leaving critical section");
        //this.States[this.id] = "O";
        //this.tk.setTS(this.id, "O");
    }
    
    @Override
    public int[] getListOfRequests() throws RemoteException{
    	return this.N;
    }
    
    @Override
    public String[] getListOfStates() throws RemoteException{
    	return this.States;
    }
    
    @Override
    public void setListOfRequests(int[] arr) throws RemoteException{
    	this.N = Arrays.copyOf(arr, this.numProc);
    }
    
    @Override
    public void setListOfStates(String[] arr) throws RemoteException{
    	this.States = Arrays.copyOf(arr, this.numProc);
    }

    @Override
    public void setEntities(IComponent[] entities) throws RemoteException{
        this.RD = entities;
    }

    @Override
    public void setName(String name) throws RemoteException{
        this.name = name;
    }

    @Override
    public String getName() throws RemoteException{
        // TODO Auto-generated method stub
        return name;
    }

    @Override
    public void setId(int id) throws RemoteException{
        this.id = id;
    }

    @Override
    public int getId() throws RemoteException{
        return id;
    }
    
    @Override
    public void setNumProc(int n) throws RemoteException{
    	this.numProc = n;
    }
    
    private void printStates(){
    	System.out.print("Process "+id+" ");
    	for (int i=0; i<States.length; i++)
    		System.out.print(States[i]);
    	System.out.println();
    }
    
}
