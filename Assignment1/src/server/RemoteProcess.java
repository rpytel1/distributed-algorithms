package server;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import util.IRemoteEntity;

public class RemoteProcess implements Runnable{

	IRemoteEntity process;
	int num;
	ArrayList<Integer> delays;
	
	public RemoteProcess(IRemoteEntity process, int num, List<Integer> d) {
		this.process = process;
		this.num = num;
		this.delays = new ArrayList<Integer>(d);
	}
	
	@Override
	public void run() {
		for (int i = 0; i < this.num; i++)
		{
			try
			{
				//Thread.sleep(process.getId()*500);
				//System.out.println(delays.get(i));
				Thread.sleep(delays.get(i));
				process.sendMessage();
			}
			catch (Exception e) {
				System.err.println("Client exception: " + e.toString()); 
				e.printStackTrace(); 
			} 	
		}
	}

}
