package server;

import message.Message;
import message.MessageBuffer;
import util.Buffer;
import util.IRemoteEntity;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;

import clock.VectorClock;

/* Class with remote methods that contains all the functionalities of 
 * point-to-point communication with causal ordering according to the 
 * Schiper-Eggli-Sandoz algorithm
 */

public class RemoteEntityImpl extends UnicastRemoteObject implements IRemoteEntity {

	private static final long serialVersionUID = 1L;
	private int id; // the process id 
    private String name; // the process name
    private MessageBuffer msgBuffer; // the buffer with the messages to be delivered to the process
    public ArrayList<Message> toBeSent; // the list the messages to be sent by the process
    private IRemoteEntity[] RD; // the array with the info of all the remote processes 
    private Buffer S; // the local buffer containing the pairs (processID,VectorClock)
    private VectorClock vt; // the vector clock of the process

    protected RemoteEntityImpl() throws RemoteException {
        super();
        this.msgBuffer = new MessageBuffer();
        this.toBeSent = new ArrayList<Message>();
        this.S = new Buffer();
    }
    
    /* Method implementing the receiving procedure according to the Schiper-Eggli-Sandoz algorithm
     */
    @Override
    public synchronized void receive(Message m) throws RemoteException{
    	Buffer receivedBuffer = m.getBuffer(); // the buffer of the message
        int receiver = m.getReceiver(); 
        //m.getBuffer().printit();
        // The delivery condition ((there doesn't exist (i,V') in Sm) or (there exists (i,V') in Sm && V' <= V))
        // is checked
        if (!receivedBuffer.contains(receiver) || (receivedBuffer.contains(receiver)
                && receivedBuffer.get(receiver).smallerOrEqualThan(this.vt))) {
            deliver(m); // if it's true the message is delivered
            Message message = this.msgBuffer.peek(); 
            if(message!=null) {
            	receivedBuffer = message.getBuffer();
            	receiver = message.getReceiver();
            	// then each message in the buffer if the process is checked to see if it can be delivered
                while (!receivedBuffer.contains(receiver) || (receivedBuffer.contains(receiver)
                        && receivedBuffer.get(receiver).smallerOrEqualThan(vt))) {
                    deliver(message);
                    msgBuffer.poll();
                    message = this.msgBuffer.peek();
                    if (message == null) break;
                    receivedBuffer = message.getBuffer();
                	receiver = message.getReceiver();
                }
            }
        } 
        else this.msgBuffer.add(m); // otherwise the message is put in the buffer of the process
    }
    
    /* Method implementing the sending procedure according to the Schiper-Eggli-Sandoz algorithm
     */
    @Override
    public synchronized void sendMessage() throws RemoteException{
    	if (this.toBeSent != null){
	    	if (this.toBeSent.get(0)!= null){
		    	Message m = this.toBeSent.get(0); // the message to be sent is selected
		    	this.vt.incTimeVector(this.id); // the vector clock of the process is incremented
		    	m.setBuffer(new Buffer(this.S.clone())); // the buffer of the message is set
		    	m.setTimestamp(new VectorClock(m.getId(), this.vt.clone())); // the timestamp too
		        new java.util.Timer().schedule( 
		                new java.util.TimerTask() {
		                    @Override
		                    public void run() {
		                        try {
		                        	RD[m.getReceiver()].receive(m); // the message is sent
		                        } catch (RemoteException e) {
		                            e.printStackTrace();
		                        }
		                    }
		                },
		                m.getDelay() // and the delivery delay is set
		        );
		        System.out.println("Process "+this.id+" sent message "+m.getText()
		        	+" to process "+m.getReceiver());
		        // The pair (receiverId, VectorClock) is added to the local buffer of the process
		        // after the message is sent
		        this.S.put(m.getReceiver(), new VectorClock(m.getId(), this.vt.clone())); 
		        this.toBeSent.remove(0); // the sent message is removed from the messages to be sent list
	    	}
    	}
    }

    /* Method implementing the delivering procedure according to the Schiper-Eggli-Sandoz algorithm
     */
    @Override
    public synchronized void deliver(Message m) throws RemoteException{
    	System.out.println("Message " + m.getText() + " has been delivered to process " + m.getReceiver() 
        + " by process " + m.getSender());
    	// after the delivery of the message the VectorClock of the receiving process is updated
        this.vt = new VectorClock(this.id, this.vt.merge(m.getTimestamp())); 
        //this.vt.incTimeVector(m.getSender()); // and incremented in the entry of the sending process
        									  // so that it contains the latest known info
        this.vt.incTimeVector(this.id);
        for (int i = 0; i < RD.length; i++) {
            if (m.getBuffer().contains(i)) {
                S.putAndMerge(i, m.getBuffer().get(i)); // lastly the buffer of the receiving process 
            }											// is updated as suggested in the algorithm
        }
    }

    @Override
    public void setVectorClock(int i, int l) throws RemoteException{
        this.vt = new VectorClock(i, l);
    }

    @Override
    public void setEntities(IRemoteEntity[] entities) throws RemoteException{
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
    
    /* Method adds the message to be sent by the process
     */
    @Override
    public void addMessageToBeSent(Message m) throws RemoteException{
        this.toBeSent.add(m);
    }
}
