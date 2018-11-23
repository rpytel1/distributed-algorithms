package src.util;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface IComponent extends Remote {
	public void init(int n) throws RemoteException;
    public void setEntities(IComponent[] enities) throws RemoteException;
    public void setName(String name) throws RemoteException;
    public String getName() throws RemoteException;
    public void setId(int id) throws RemoteException;
    public int getId() throws RemoteException;
    public int[] getListOfRequests() throws RemoteException;
    public String[] getListOfStates() throws RemoteException;
    public void setListOfRequests(int[] arr ) throws RemoteException;
    public void setListOfStates(String[] arr) throws RemoteException;
    public void sendRequest() throws RemoteException;
    public void receiveRequest(int id, int numRec) throws RemoteException;
    public void receiveToken() throws RemoteException;
    public void setNumProc(int n) throws RemoteException;
}
