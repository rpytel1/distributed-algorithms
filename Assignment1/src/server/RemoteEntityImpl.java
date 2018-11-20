package server;

import message.Message;
import message.MessageBuffer;
import util.Buffer;
import util.IRemoteEntity;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import clock.VectorClock;

public class RemoteEntityImpl extends UnicastRemoteObject implements IRemoteEntity {

    private int id;
    private String name;
    private MessageBuffer msgBuffer;
    public ArrayList<Message> toBeSent;
    private IRemoteEntity[] RD;
    private Buffer S;
    private VectorClock vt;

    protected RemoteEntityImpl() throws RemoteException {
        super();
        this.msgBuffer = new MessageBuffer();
        this.toBeSent = new ArrayList<Message>();
        this.S = new Buffer();
    }

    @Override
    public synchronized void receive(Message m) throws RemoteException{
    	Buffer receivedBuffer = m.getBuffer();
        int receiver = m.getReceiver();
        //m.getBuffer().printit();
        if (!receivedBuffer.contains(receiver) || (receivedBuffer.contains(receiver)
                && receivedBuffer.get(receiver).smallerOrEqualThan(this.vt))) {
            deliver(m);
            Message message = this.msgBuffer.peek();
            if(message!=null) {
            	receivedBuffer = message.getBuffer();
            	receiver = message.getReceiver();

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
        else this.msgBuffer.add(m);
    }
    
    @Override
    public synchronized void sendMessage() throws RemoteException{
    	if (this.toBeSent != null){
	    	if (this.toBeSent.get(0)!= null){
		    	Message m = this.toBeSent.get(0);
		    	this.vt.incTimeVector(this.id);
		    	m.setBuffer(new Buffer(this.S.clone()));
		    	m.setTimestamp(new VectorClock(m.getId(), this.vt.clone()));
		    	//m.getBuffer().printit();
		        new java.util.Timer().schedule( 
		                new java.util.TimerTask() {
		                    @Override
		                    public void run() {
		                        try {
		                        	RD[m.getReceiver()].receive(m);
		                        } catch (RemoteException e) {
		                            e.printStackTrace();
		                        }
		                    }
		                },
		                m.getDelay()
		        );
		        System.out.println("Process "+this.id+" sent message "+m.getText()+" to process "+m.getReceiver());
		        this.S.put(m.getReceiver(), new VectorClock(m.getId(), this.vt.clone()));
		        this.toBeSent.remove(0);
	    	}
    	}
    }

    @Override
    public synchronized void deliver(Message m) throws RemoteException{
    	System.out.println("Message " + m.getText() + " has been delivered to process " + m.getReceiver() 
        + " by process " + m.getSender());
        this.vt = new VectorClock(this.id, this.vt.merge(m.getTimestamp()));
        this.vt.incTimeVector(m.getSender());

        for (int i = 0; i < RD.length; i++) {
            if (m.getBuffer().contains(i)) {
                S.putAndMerge(i, m.getBuffer().get(i));
            }
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
    
    @Override
    public void addMessageToBeSent(Message m) throws RemoteException{
        this.toBeSent.add(m);
    }
}
