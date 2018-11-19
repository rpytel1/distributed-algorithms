package util;

import java.io.Serializable;
import java.util.HashMap;

import clock.VectorClock;

public class Buffer implements Serializable{
		/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
		public HashMap<Integer,VectorClock> messageBuffer;
		
		public Buffer(){
			messageBuffer = new HashMap<Integer,VectorClock>();
		}
		
		public Buffer(HashMap<Integer,VectorClock> t){
			messageBuffer = new HashMap<Integer,VectorClock>(t);
		}
		
		public VectorClock get(int i){
			return messageBuffer.get(i);
		}
		
		public VectorClock remove(int i){
			return messageBuffer.remove(i);
		}
		
		public boolean contains(int i){
			return messageBuffer.containsKey(i);
		}
		
		public void put(int i , VectorClock vt){
			messageBuffer.put(i, vt);
		}
		
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
		
		public void printit(){
			for (int i:messageBuffer.keySet()) System.out.println(messageBuffer.get(i).toString());
		}
		
		public HashMap<Integer,VectorClock> clone(){
			HashMap<Integer,VectorClock> temp = new HashMap<Integer,VectorClock>();
			for (int i:messageBuffer.keySet()) temp.put(i,messageBuffer.get(i));
			return temp;
		}
		
}
