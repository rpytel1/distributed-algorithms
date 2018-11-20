package server;

import java.util.ArrayList;
import java.util.List;

import util.IRemoteEntity;

/* Class used to implement the Runnable interface so that each process is associated with a thread
 * All the sending part is included in the run() method of the class
 */
public class RemoteProcess implements Runnable{

	IRemoteEntity process; // the associated remote process
	int num; // the number of messages to be sent
	ArrayList<Integer> delays; // the list of delays associated with the ordering of sending the messages
	
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
				// every process sleeps for delays.get(i) milliseconds before sending each message
				// so that the ordering in the sending procedure can be maintained easily
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
