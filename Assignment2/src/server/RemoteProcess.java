package src.server;

import src.util.IComponent;

/* Class used to implement the Runnable interface so that each process is associated with a thread
 * All the requesting part is included in the run() method of the class
 */
public class RemoteProcess implements Runnable{

	IComponent process; // the associated remote process
	
	public RemoteProcess(IComponent process) {
		this.process = process;
	}
	
	@Override
	public void run() {
		while(true)
		{
			try
			{
				// every process sleeps for some random milliseconds before sending each new
				//  request 
				Thread.sleep((int)Math.random()*3000); 
				process.sendRequest();
			}
			catch (Exception e) {
				System.err.println("Client exception: " + e.toString()); 
				e.printStackTrace(); 
			} 	
		}
	}

}
