package assignment3.node;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;

import assignment3.link.Link;
import assignment3.message.Message;

public interface IComponent extends Remote {
	
	public void receive(Message message, Link link) throws RemoteException ;
	public void receiveConnect(Message message, Link link) throws RemoteException ;
	public void receiveTest(Message message, Link link) throws RemoteException ;
	public void receiveReport(Message message, Link link) throws RemoteException ;
	public void receiveChangeRoot(Message message, Link link) throws RemoteException ;
	public void setEntities(IComponent[] entities) throws RemoteException ;
	public void wakeUp() throws RemoteException;
	public IComponent[] getEntities() throws RemoteException;
	public int getID() throws RemoteException;
	public Map<String,Integer> getMetrics() throws RemoteException;
	public Map<Double,Link> getCores() throws RemoteException;
	public Map<Double,Link> getMST() throws RemoteException;
	public Map<Integer,List<Link>> getLevels() throws RemoteException;
}
