package server;

import util.Buffer;
import util.Constant;
import util.IRemoteEntity;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.concurrent.atomic.AtomicInteger;

import clock.VectorClock;
import message.Message;

public class Server {
	
	private static int msgNum[];
	private static IRemoteEntity[] RMI_IDS;
	private static int msgId;
	private static int numProc;
	
    public static void main(String[] args) throws AlreadyBoundException, NotBoundException, IOException, InterruptedException {
        Registry registry = LocateRegistry.createRegistry(Constant.RMI_PORT);

        BufferedReader br = new BufferedReader(new FileReader("tests/clients.txt"));
        String line = "";
        while ((line = br.readLine()) != null) {
            registry.bind(line, new RemoteEntityImpl());
        }
        br.close();
        numProc = registry.list().length;
        setRegistry();
        System.out.println("started");
    }

    public static void setRegistry() throws NotBoundException, NumberFormatException, IOException{
        Registry registry = LocateRegistry.getRegistry("localhost", Constant.RMI_PORT);
        RMI_IDS = new IRemoteEntity[numProc];
        Thread[] myThreads = new Thread[numProc];
        for(int i=0; i<numProc; i++){
            RMI_IDS[i] = (IRemoteEntity) registry.lookup(registry.list()[i]);
        }
        BufferedReader br = new BufferedReader(new FileReader("tests/messages.txt"));
		String line = "";
		Message temp;
		msgNum = new int[numProc];
		for (int i=0; i<numProc; i++) msgNum[i] = 0;
		msgId = 0;
		while ((line = br.readLine()) != null) {
			String[] split_line = line.split(" ");
			int sender = Integer.parseInt(split_line[0]);
			String msgText = split_line[1];
			int receiver = Integer.parseInt(split_line[2]);
			int delay = Integer.parseInt(split_line[3]);
			VectorClock vt = new VectorClock(sender,numProc);	
			temp = new Message(msgId, msgText, vt, new Buffer(), sender, receiver, delay);(RMI_IDS[sender]).addMessageToBeSent(temp);
			msgNum[sender]++;
			msgId++;
		}
		br.close();
		
        for(int i=0; i<numProc; i++){
            RMI_IDS[i].setEntities(RMI_IDS);
            RMI_IDS[i].setName(registry.list()[i]);
            RMI_IDS[i].setId(i);
            RMI_IDS[i].setVectorClock(i,numProc);
            RemoteProcess p = new RemoteProcess(RMI_IDS[i], msgNum[i]);
            myThreads[i] = new Thread(p);
        }
        
        for (int i = 0; i < numProc; i++)
		{
			myThreads[i].start();
		}
    }
}
