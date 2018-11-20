package util;

import java.io.Serializable;
import java.util.HashMap;

import clock.VectorClock;

/* The implementation of the local buffer used by both the processes and the messages
 * in which the pairs of processes' ids and timestamps are stored
 */

public class Buffer implements Serializable{

	private static final long serialVersionUID = 1L;
		public HashMap<Integer,VectorClock> messageBuffer;
		
		public Buffer(){
			messageBuffer = new HashMap<Integer,VectorClock>();
		}
		
		/* Second implementation of the constructor of the buffer used in the case that 
		 * a vector clock is given as an input
		 */		
		public Buffer(HashMap<Integer,VectorClock> t){
			messageBuffer = new HashMap<Integer,VectorClock>(t); 
		}
		
		/* Method that returns the vector clock associated with process i
		 */	
		public VectorClock get(int i){
			return messageBuffer.get(i);
		}
		
		/* Method that removes the pair associated with process i
		 */	
		public VectorClock remove(int i){
			return messageBuffer.remove(i);
		}
		
		/* Method that checks if there is an entry in the buffer for process i
		 */	
		public boolean contains(int i){
			return messageBuffer.containsKey(i);
		}
		
		/* Method that puts the pair (i,vt) in the buffer. 
		 * If there exists already a pair then it is replaced with the new one
		 */	
		public void put(int i , VectorClock vt){
			messageBuffer.put(i, vt);
		}
		
		/* Method that puts the pair (i,vt) in the buffer.  
		 * If there exists already a pair then the two vector clocks are merged in respect ot their
		 * maximum values 
		 */	
		public void putAndMerge(int i , VectorClock vt){
			if(messageBuffer.containsKey(i)){
				messageBuffer.put(i, new VectorClock(i,messageBuffer.get(i).merge(vt)));
			}
			else{
				messageBuffer.put(i, vt);
			}
		}
		
		public boolean isEmpty(){
			return messageBuffer.isEmpty();
		}
		
		/* Method that prints the content of the vector clock associated with process i in the buffer
		 * Mostly used for debugging reasons
		 */	
		public void printit(){
			for (int i:messageBuffer.keySet()) System.out.println(messageBuffer.get(i).toString());
		}
		
		/* Method that copies the content of the buffer to a new one 
		 * and returns the new buffer
		 */	
		public HashMap<Integer,VectorClock> clone(){
			HashMap<Integer,VectorClock> temp = new HashMap<Integer,VectorClock>();
			for (int i:messageBuffer.keySet()) temp.put(i,messageBuffer.get(i));
			return temp;
		}
		
}
