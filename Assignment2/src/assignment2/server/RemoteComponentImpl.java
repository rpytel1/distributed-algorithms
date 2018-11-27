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
        this.N = new int[n];
        this.States = new String[n];
        this.tk = new Token(n);
        for (int i = 0; i < n; i++) N[i] = 0;
        // different type of initialization for process 0 and the others
        if (this.id == 0) {
            this.States[0] = "H";
            for (int i = 1; i < n; i++) this.States[i] = "O";
        } else {
            for (int i = 0; i < this.id; i++) this.States[i] = "R";
            for (int i = this.id; i < n; i++) this.States[i] = "O";
        }
        index = 100 * id;
    }

    /**
     * Broadcast request to the components that it is suspected to possess the tokken
     */
    @Override
    public void sendRequest() throws RemoteException {
        System.out.println(id + " " + "Started sending request");

        if (this.States[this.id].equals("H")) {
            System.out.println(getStateStamp() + this.id + ":Sending request to itself");

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
        this.States[this.id] = "R";
        this.N[this.id] += 1;
        int i = 0;
        while (i < this.numProc) {
            if (i != this.id) {
                if (this.States[i].equals("R")) {
                    System.out.println(getStateStamp() + this.id + ": Sending request to process " + i);
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
                            200 // and the delivery delay is set
                    );

                }
            }
            i += 1;
        }
        System.out.println(id + " " + "Finished sending request");

    }


    /* Method for receiving a token request
     */
    @Override
    public void receiveRequest(int reqId, int numReq) throws RemoteException {
        System.out.println(id + " " + index + "Started receiving request" + reqId);
        // Update requests
        System.out.println(getStateStamp() + id + ":Receiving request from process " + reqId);
        this.N[reqId] = numReq;

        if (id == reqId) {
            States[id] = "H";
        }

        if (this.States[this.id].equals("O") || this.States[this.id].equals("E"))
            this.States[reqId] = "R";
        else if (this.States[this.id].equals("R")) {
            if (!this.States[reqId].equals("R")) {
                this.States[reqId] = "R";
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
            this.States[reqId] = "R";
            this.States[this.id] = "O";
            this.tk.setTS(reqId, "R");
            this.tk.setTN(reqId, numReq);
            System.out.println(getStateStamp() + this.id + ": Sending token to process " + reqId);


            RD[reqId].receiveToken();


        }
        System.out.println(id + " " + index + "Finished receiving request" + reqId);
        index++;

    }

    /* Method for receiving the token
     */
    @Override
    public void receiveToken() throws RemoteException {
        System.out.println(id + "Started receiving tokken");
        System.out.println(getStateStamp() + id + ":Token received ");
        this.States[this.id] = "E";
        criticalSection();
        this.States[this.id] = "O";
        this.tk.setTS(this.id, "O");
        for (int i = 0; i < this.numProc; i++) {
            System.out.println("Executions" + N[i] + " " + tk.getTN(i));
            System.out.println("States" + States[i] + " " + tk.getTS(i));
            if (this.N[i] > this.tk.getTN(i)) {
                this.tk.setTN(i, this.N[i]);
                this.tk.setTS(i, this.States[i]);
            } else {
                this.N[i] = this.tk.getTN(i);
                this.States[i] = this.tk.getTS(i);
            }
        }
        System.out.println(getStateStamp());
        boolean flag = true;
        for (int i = 0; i < this.numProc; i++) {
            if (!this.States[i].equals("O")) {
                flag = false;
            }
        }
        if (flag) {
            this.States[this.id] = "H";
        } else {
            for (int i = 1; i < this.numProc + 1; i++) {
                // Cyclic check
                int reqID = (this.id + i) % this.numProc;
                // Send to first process with outstanding request
                if (this.States[reqID].equals("R")) {
                    System.out.println(getStateStamp() + this.id + " sending token to process " + reqID);
                    new java.util.Timer().schedule(
                            new java.util.TimerTask() {
                                @Override
                                public void run() {
                                    try {
                                        RD[reqID].receiveToken();
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
        System.out.println(id + "Finished receiving tokken");

    }

    /* Method implementing the critical section of each process
     */
    private void criticalSection() {
        //this.States[this.id] = "E";
        System.out.println(getStateStamp() + this.id + " Entering critical section");
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            System.out.println("Critical section interrupted");
        }
        System.out.println(getStateStamp() + this.id + " leaving critical section");
        //this.States[this.id] = "O";
        //this.tk.setTS(this.id, "O");
    }

    @Override
    public int[] getListOfRequests() throws RemoteException {
        return this.N;
    }

    @Override
    public String[] getListOfStates() throws RemoteException {
        return this.States;
    }

    @Override
    public void setListOfRequests(int[] arr) throws RemoteException {
        this.N = Arrays.copyOf(arr, this.numProc);
    }

    @Override
    public void setListOfStates(String[] arr) throws RemoteException {
        this.States = Arrays.copyOf(arr, this.numProc);
    }

    @Override
    public void setEntities(IComponent[] entities) throws RemoteException {
        this.RD = entities;
    }

    @Override
    public void setName(String name) throws RemoteException {
        this.name = name;
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
        this.numProc = n;
    }

    private String getStateStamp() {
        String stateStamp = "[";
        for (int i = 0; i < States.length; i++) {
            stateStamp += this.States[i];
        }
        stateStamp += "]";
        return stateStamp;
    }

}
