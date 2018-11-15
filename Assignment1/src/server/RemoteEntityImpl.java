package server;

import message.Message;
import util.IRemoteEntity;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class RemoteEntityImpl extends UnicastRemoteObject implements IRemoteEntity {
    protected RemoteEntityImpl() throws RemoteException {
    }

    @Override
    public void receive(Message m) {

    }

    @Override
    public void sendMessage(Message m) {

    }

    @Override
    public void deliver(Message m) {

    }

    @Override
    public void setEnities(IRemoteEntity[] enities) {

    }

    @Override
    public void setName(String name) {

    }

    @Override
    public void setId(int id) {

    }
}
