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
		for (int i=0; i<5; i++)
		{
			try
			{
				// every process sleeps for some random milliseconds before sending each new
				//  request 
				Thread.sleep((int)(Math.random()*3000)); 
				System.out.println("process "+ process.getId() +" working for time "+ i);
				process.sendRequest();
			}
			catch (Exception e) {
				System.err.println("Client exception: " + e.toString()); 
				e.printStackTrace(); 
			} 	
		}
	}

}
