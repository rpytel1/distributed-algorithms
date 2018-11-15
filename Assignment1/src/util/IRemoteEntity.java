package util;

import message.Message;

import java.rmi.Remote;
import java.util.List;

public interface IRemoteEntity extends Remote {
    public void receive(Message m);
    public void sendMessage(Message m);
    public void deliver(Message m);
    public void setVectorClock(int id, int l);
    public void setEntities(IRemoteEntity[] enities);
    public void setName(String name);
    public String getName();
    public void setId(int id);
    public int getId();
    public void addMessage(Message m);
}
