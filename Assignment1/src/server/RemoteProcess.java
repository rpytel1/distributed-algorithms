package server;

import java.rmi.RemoteException;

import util.IRemoteEntity;

public class RemoteProcess implements Runnable{

	IRemoteEntity process;
	int num;
	
	public RemoteProcess(IRemoteEntity process, int num) {
		this.process = process;
		this.num = num;
	}
	
	@Override
	public void run() {
		for (int i = 0; i < this.num; i++)
		{
			try
			{
				Thread.sleep(process.getId()*500);
				process.sendMessage();
			}
			catch (Exception e) {
				System.err.println("Client exception: " + e.toString()); 
				e.printStackTrace(); 
			} 	
		}
	}

}
