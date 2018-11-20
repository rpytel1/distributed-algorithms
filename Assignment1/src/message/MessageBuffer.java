package message;
import java.util.PriorityQueue;

/* Class that implements the buffer with the received messages
 * of each process. The messages are stored in ascending order according to the 
 * MessageComparator class which compares the vector clocks of the messages
 */

public class MessageBuffer {
	public PriorityQueue<Message> msgQueue;
	
	public MessageBuffer(){
		msgQueue = new PriorityQueue<>(100, new MessageComparator());
	}
	
	/* Method that returns the head of the buffer without removing it
	 */
	public Message peek(){
		return msgQueue.peek();
	}
	
	/* Method that returns the head of the buffer while removing it
	 */
	public Message poll(){
		return msgQueue.poll();
	}
	
	/* Method that adds a message to the buffer
	 */
	public void add(Message msg){
		msgQueue.add(msg);
	}
}
