package assignment2.server;

import assignment2.util.IComponent;
import assignment2.util.Token;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Arrays;

/* Class with remote methods that contains all the functionalities of
 * request and token sending and needed in implementing Singhal�s algorithm
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

    private int index;

    protected RemoteComponentImpl() throws RemoteException {
        super();
    }

    @Override
    public void init(int n) {
        N = new int[n];
        States = new String[n];
        tk = new Token(n);
        for (int i = 0; i < n; i++) N[i] = 0;
        // different type of initialization for process 0 and the others
        if (id == 0) {
            States[0] = "H";
            for (int i = 1; i < n; i++) States[i] = "O";
        } else {
            for (int i = 0; i < id; i++) States[i] = "R";
            for (int i = id; i < n; i++) States[i] = "O";
        }
        index = 100 * id;
    }

    /**
     * Broadcast request to the components that it is suspected to possess the tokken
     */
    @Override
    public void sendRequest() {

        if (States[id].equals("H")) {
            System.out.println(getStateStamp() + id + ":Sending request to itself");

            new java.util.Timer().schedule(
                    new java.util.TimerTask() {
                        @Override
                        public void run() {
                            try {
                                RD[id].receiveRequest(id, N[id]);
                            } catch (RemoteException e) {
                                e.printStackTrace();
                            }
                        }
                    },
                    200 // and the delivery delay is set
            );
        }
        States[id] = "R";
        N[id] += 1;
        int i = 0;
        while (i < numProc) {
            if (i != id) {
                if (States[i].equals("R")) {
                    System.out.println(getStateStamp() + id + ": Sending request to process " + i);
                    int finalI = i;
                    new java.util.Timer().schedule(
                            new java.util.TimerTask() {
                                @Override
                                public void run() {
                                    try {
                                        RD[finalI].receiveRequest(id, N[id]);
                                    } catch (RemoteException e) {
                                        e.printStackTrace();
                                    }
                                }
                            },
                            200
                    );

                }
            }
            i += 1;
        }
    }


    /* Method for receiving a token request
     */
    @Override
    public void receiveRequest(int reqId, int numReq){
        // Update requests
        System.out.println(getStateStamp() + id + ":Receiving request from process " + reqId);
        N[reqId] = numReq;

        if (id == reqId) {
            States[id] = "H";
        }

        if (States[id].equals("O") || States[id].equals("E"))
            States[reqId] = "R";
        else if (States[id].equals("R")) {
            if (!States[reqId].equals("R")) {
                States[reqId] = "R";
                new java.util.Timer().schedule(
                        new java.util.TimerTask() {
                            @Override
                            public void run() {
                                try {
                                    RD[reqId].receiveRequest(id, N[id]);
                                } catch (RemoteException e) {
                                    e.printStackTrace();
                                }
                            }
                        },
                        200); // and the delivery delay is set

            }
        } else {
            States[reqId] = "R";
            States[id] = "O";
            tk.setTS(reqId, "R");
            tk.setTN(reqId, numReq);
            System.out.println(getStateStamp() + id + ": Sending token to process " + reqId);

            new java.util.Timer().schedule(
                    new java.util.TimerTask() {
                        @Override
                        public void run() {
                            try {
                                RD[reqId].receiveToken(tk);
                            } catch (RemoteException e) {
                                e.printStackTrace();
                            }
                        }
                    },
                    200); // and the delivery delay is set


        }
        index++;

    }

    /* Method for receiving the token
     */
    @Override
    public void receiveToken(Token tk){
        this.tk = tk;
        System.out.println(getStateStamp() + id + ":Token received ");
        States[id] = "E";
        criticalSection();
        States[id] = "O";
        tk.setTS(id, "O");
        for (int i = 0; i < numProc; i++) {
            if (N[i] > tk.getTN(i)) {
                tk.setTN(i, N[i]);
                tk.setTS(i, States[i]);
            } else {
                N[i] = tk.getTN(i);
                States[i] = tk.getTS(i);
            }
        }
        boolean flag = true;
        for (int i = 0; i < numProc; i++) {
            if (!States[i].equals("O")) {
                flag = false;
            }
        }
        if (flag) {
            States[id] = "H";
            System.out.println(getStateStamp() +id + ":Holding the token");
        } else {
            for (int i = 1; i < numProc + 1; i++) {
                // Cyclic check
                int reqID = (id + i) % numProc;
                // Send to first process with outstanding request
                if (States[reqID].equals("R")) {
                    System.out.println(getStateStamp() + id + " sending token to process " + reqID);
                    new java.util.Timer().schedule(
                            new java.util.TimerTask() {
                                @Override
                                public void run() {
                                    try {
                                        RD[reqID].receiveToken(tk);
                                    } catch (RemoteException e) {
                                        e.printStackTrace();
                                    }
                                }
                            },
                            200);


                    break;
                }
            }
        }
    }

    /* Method implementing the critical section of each process
     */
    private void criticalSection() {
        //States[id] = "E";
        System.out.println(getStateStamp() + id + " Entering critical section");
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            System.out.println("Critical section interrupted");
        }
        System.out.println(getStateStamp() + id + " leaving critical section");
        //States[id] = "O";
        //tk.setTS(id, "O");
    }

    @Override
    public int[] getListOfRequests() throws RemoteException {
        return N;
    }

    @Override
    public String[] getListOfStates() throws RemoteException {
        return States;
    }

    @Override
    public void setListOfRequests(int[] arr) throws RemoteException {
        N = Arrays.copyOf(arr, numProc);
    }

    @Override
    public void setListOfStates(String[] arr) throws RemoteException {
        States = Arrays.copyOf(arr, numProc);
    }

    @Override
    public void setEntities(IComponent[] entities) throws RemoteException {
        RD = entities;
    }

    @Override
    public void setName(String name) throws RemoteException {
        name = name;
    }

    @Override
    public String getName() throws RemoteException {
        // TODO Auto-generated method stub
        return name;
    }

    @Override
    public void setId(int id) throws RemoteException {
        this.id = id;
    }

    @Override
    public int getId() throws RemoteException {
        return id;
    }

    @Override
    public void setNumProc(int n) throws RemoteException {
        numProc = n;
    }

    private String getStateStamp() {
        String stateStamp = "[";
        for (int i = 0; i < States.length; i++) {
            stateStamp += States[i];
        }
        stateStamp += "]";
        return stateStamp;
    }

}
