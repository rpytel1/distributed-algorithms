package assignment3.node;

import java.rmi.Remote;
import java.rmi.RemoteException;

import assignment3.link.Link;
import assignment3.message.Message;

public interface IComponent extends Remote {
	
	public void receive(Message message, Link link) throws RemoteException ;
	public void receiveConnect(Message message, Link link) throws RemoteException ;
	public void receiveTest(Message message, Link link) throws RemoteException ;
	public void receiveReport(Message message, Link link) throws RemoteException ;
	public void receiveChangeRoot(Message message, Link link) ;
	public void setEntities(IComponent[] entities) throws RemoteException ;
	public void wakeUp() throws RemoteException ;
	
}
