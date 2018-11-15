package server;

import util.Constant;
import util.IRemoteEntity;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class Server {
    public static void main(String[] args) throws AlreadyBoundException, NotBoundException, IOException {
        Registry registry = LocateRegistry.createRegistry(Constant.RMI_PORT);

        BufferedReader br = new BufferedReader(new FileReader("tests/clients.txt"));
        String line = "";
        while ((line = br.readLine()) != null) {
            registry.bind(line, new RemoteEntityImpl());
        }
        br.close();

        setRegistry();
        System.out.println("started");
    }

    public static void setRegistry() throws RemoteException, NotBoundException{
        Registry registry = LocateRegistry.getRegistry("localhost", Constant.RMI_PORT);
        IRemoteEntity[] RMI_IDS = new IRemoteEntity[registry.list().length];
        for(int i=0; i<registry.list().length; i++){
            RMI_IDS[i] = (IRemoteEntity) registry.lookup(registry.list()[i]);
        }
        for(int i=0; i<RMI_IDS.length; i++){
            RMI_IDS[i].setEntities(RMI_IDS);
            RMI_IDS[i].setName(registry.list()[i]);
            RMI_IDS[i].setId(i+1);
            RMI_IDS[i].setVectorClock(i+1,RMI_IDS.length);
        }

    }
}
