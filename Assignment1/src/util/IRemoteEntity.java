package util;

import message.Message;

import java.rmi.Remote;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public interface IRemoteEntity extends Remote {
    public void receive(Message m);
    public void sendMessage();
    public void deliver(Message m);
    public void setVectorClock(int id, int l);
    public void setEntities(IRemoteEntity[] enities);
    public void setRuns(int i);
    public AtomicInteger getRuns();
    public void setName(String name);
    public String getName();
    public void setId(int id);
    public int getId();
    public void addMessage(Message m);
    public void addMessageToBeSent(int i, Message m);
}
