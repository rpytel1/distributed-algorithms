package server;

import message.Message;
import message.MessageBuffer;
import util.Buffer;
import util.IRemoteEntity;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import clock.VectorClock;

public class RemoteEntityImpl extends UnicastRemoteObject implements IRemoteEntity {

    private int id;
    private String name;
    private MessageBuffer msgBuffer;
    private ArrayList<Message> toBeSent;
    private IRemoteEntity[] RD;
    private Buffer S;
    private VectorClock vt;
    private AtomicInteger runs;

    protected RemoteEntityImpl() throws RemoteException {
        super();
        this.msgBuffer = new MessageBuffer();
        this.toBeSent = new ArrayList<Message>();
        this.S = new Buffer();
    }

    @Override
    public void receive(Message m) throws RemoteException{
        Buffer receivedBuffer = m.getBuffer();
        int receiver = m.getReceiverID();

        if (!receivedBuffer.contains(receiver) || (m.getBuffer().contains(receiver)
                && receivedBuffer.get(receiver).smallerOrEqualThan(vt))) {
            deliver(m);

            Message message = msgBuffer.peek();
            if(message!=null) {
                while (!message.getBuffer().contains(message.getReceiverID()) || (message.getBuffer().contains(message.getReceiverID())
                        && message.getBuffer().get(message.getReceiverID()).smallerOrEqualThan(vt))) {
                    deliver(msgBuffer.peek());
                    message = msgBuffer.peek();
                }
            }
        } else {
            msgBuffer.add(m);
        }
    }

    @Override
    public void sendMessage() throws RemoteException{
        System.out.println("Sending message" +vt);
    	if (this.toBeSent.get(0)!= null){
	    	Message m = this.toBeSent.get(0);
	        this.vt.incTimeVector(id);
            new Thread(() -> {
                try {
                    Thread.sleep((int) (Math.random() * 500));
                    RD[m.getReceiverID()].receive(m);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();

	        S.put(m.getReceiverID(), vt);
	        this.toBeSent.remove(0);
    	}
    }

    @Override
    public void deliver(Message m) throws RemoteException{
        System.out.println("Message " + m.getText() + " has been delivered to " + m.getReceiverID()+" time:"+vt);
        this.vt.incTimeVector(m.getSenderID());
        msgBuffer.poll();
        this.runs.decrementAndGet();

        for (int i = 0; i < RD.length; i++) {
            if (m.getBuffer().contains(i)) {
                S.put(i, m.getBuffer().get(i));
                this.runs.decrementAndGet();
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
    public void setRuns(int i) throws RemoteException{
        this.runs = new AtomicInteger(i);
    }
    
    @Override
    public AtomicInteger getRuns() throws RemoteException{
        return this.runs;
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
    public void addMessage(Message m) throws RemoteException{
        this.msgBuffer.add(m);
    }
    
    @Override
    public void addMessageToBeSent(int i, Message m) throws RemoteException{
        this.toBeSent.add(i, m);
    }
}
