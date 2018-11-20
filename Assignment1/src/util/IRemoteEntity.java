package util;

import message.Message;

import java.rmi.Remote;
import java.rmi.RemoteException;

/* The remote interface that extends the standard Remote interface with
 * all the needed methods implemented for remote communication of processes
 * according to the Schiper-Eggli-Sandoz algorithm
 */

public interface IRemoteEntity extends Remote {
    public void receive(Message m) throws RemoteException;
    public void sendMessage() throws RemoteException;
    public void deliver(Message m) throws RemoteException;
    public void setVectorClock(int id, int l) throws RemoteException;
    public void setEntities(IRemoteEntity[] enities) throws RemoteException;
    public void setName(String name) throws RemoteException;
    public String getName() throws RemoteException;
    public void setId(int id) throws RemoteException;
    public int getId() throws RemoteException;
    public void addMessageToBeSent(Message m) throws RemoteException;
}
