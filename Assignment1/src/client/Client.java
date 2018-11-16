package client;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.concurrent.atomic.AtomicInteger;

import clock.VectorClock;
import message.Message;
import util.Buffer;
import util.Constant;
import util.IRemoteEntity;

public class Client {
	static int msgNum = 0;
	public static void main(String[] args) throws NotBoundException, IOException, InterruptedException{
		Registry registry = LocateRegistry.getRegistry("localhost", Constant.RMI_PORT);
		BufferedReader br = new BufferedReader(new FileReader("tests/messages.txt"));
		String line = "";
		Message temp;
		while ((line = br.readLine()) != null) {
			msgNum++;
			String[] split_line = line.split(" ");
			String msgText = split_line[1];
			VectorClock vt = new VectorClock(registry.list().length,Integer.parseInt(split_line[0]));
			int sender = Integer.parseInt(split_line[0]);
			int receiver = Integer.parseInt(split_line[2]);
			temp = new Message(msgText,vt,new Buffer(), sender, receiver);
			((IRemoteEntity) registry.lookup(registry.list()[sender])).addMessage(temp);
		}
		br.close();
		
		SchiperEggliSandoz();
	}
	
	public static void SchiperEggliSandoz() throws NotBoundException, InterruptedException, IOException{
		Registry registry = LocateRegistry.getRegistry("localhost", Constant.RMI_PORT);
		IRemoteEntity[] RMI_IDS = new IRemoteEntity[registry.list().length];
		for(int i=0; i<registry.list().length; i++){
			RMI_IDS[i] = (IRemoteEntity) registry.lookup(registry.list()[i]);
		}
		
		for(int j=0; j<msgNum; j++){
			AtomicInteger runs = new AtomicInteger(RMI_IDS.length);
			for(int i=0; i<RMI_IDS.length; i++){
				IRemoteEntity RDi = RMI_IDS[i];
				new Thread ( () -> {					
					try {
						Thread.sleep((int)(Math.random()*500));
						RDi.broadcast();
						runs.getAndDecrement();

					} catch (Exception e) {
						e.printStackTrace();
		 	 		}
				
				
				}).start();
			}
			while(runs.get()!=0){
				Thread.sleep(1);
			}
		}
	}
}