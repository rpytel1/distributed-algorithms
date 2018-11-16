package message;
import java.util.PriorityQueue;

public class MessageBuffer {
	public PriorityQueue<Message> msgQueue;
	
	public MessageBuffer(){
		msgQueue = new PriorityQueue<>(100, new MessageComparator());
	}
	
	public Message peek(){
		return msgQueue.peek();
	}
	
	public Message poll(){
		return msgQueue.poll();
	}
	
	public void add(Message msg){
		msgQueue.add(msg);
	}
}
