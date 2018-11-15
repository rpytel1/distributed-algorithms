package server;

import message.Message;
import message.MessageBuffer;
import util.Buffer;
import util.IRemoteEntity;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import clock.VectorClock;

public class RemoteEntityImpl extends UnicastRemoteObject implements IRemoteEntity {
	
	private int id;
	private String name;
	private MessageBuffer msgBuffer;
	private IRemoteEntity[] RD;
	private Buffer S;
	private VectorClock vt;
	
    protected RemoteEntityImpl() throws RemoteException {
    	super();
		this.msgBuffer = new MessageBuffer();
		this.S = new Buffer();
    }

    @Override
    public void receive(Message m) {
    	if (!m.getBuffer().contains(m.getReceiver()) || (m.getBuffer().contains(m.getReceiver()) 
    			&& m.getBuffer().get(m.getReceiver()).smallerOrEqualThan(vt))){
    		deliver(m);
    		while (!msgBuffer.peek().getBuffer().contains(msgBuffer.peek().getReceiver()) || (msgBuffer.peek().getBuffer().contains(msgBuffer.peek().getReceiver()) 
        			&& msgBuffer.peek().getBuffer().get(msgBuffer.peek().getReceiver()).smallerOrEqualThan(vt))){
    			deliver(msgBuffer.peek());
    		}
    	}
    	else{
    		msgBuffer.add(m);
    	}
    }

    @Override
    public void sendMessage(Message m) {
    	this.vt.incTimeVector(id); 
    	RD[m.getReceiver()].receive(m);
    	S.put(m.getReceiver(), vt);
    }

    @Override
    public void deliver(Message m) {
    	System.out.println("Message " + m.getText() + "has been delivered to " + m.getReceiver());
    	this.vt.incTimeVector(m.getSender());
    	msgBuffer.poll();
    	for (int i = 0; i<RD.length; i++){
    		if (m.getBuffer().contains(i)){
    			S.put(i, m.getBuffer().get(i));
    		}
    	}
    }
    
    @Override
    public void setVectorClock(int i, int l){
		this.vt = new VectorClock(i,l);
    }

    @Override
    public void setEntities(IRemoteEntity[] entities) {
    	this.RD = entities;
    }

    @Override
    public void setName(String name) {
    	this.name = name;
    }
    
    @Override
    public String getName() {
		// TODO Auto-generated method stub
		return name;
	}

    @Override
    public void setId(int id) {
    	this.id = id;
    }
    
    @Override
    public int getId() {
    	return id;
    }
    
    @Override
	public void addMessage(Message m){
		this.msgBuffer.add(m);
	}
}
