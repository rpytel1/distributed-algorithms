package util;

import java.util.HashMap;

import clock.VectorClock;
import message.Message;

public class Buffer {
		public HashMap<Integer,VectorClock> messageBuffer;
		
		public Buffer(){
			messageBuffer = new HashMap<Integer,VectorClock>();
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
			if(messageBuffer.containsKey(i)){
				messageBuffer.put(i, new VectorClock(i,messageBuffer.get(i).merge(vt)));
			}
			else{
				messageBuffer.put(i, vt);
			}
		}
}
