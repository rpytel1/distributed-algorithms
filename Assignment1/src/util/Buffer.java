package util;

import java.io.Serializable;
import java.util.HashMap;

import clock.VectorClock;
import message.Message;

public class Buffer implements Serializable{
		public HashMap<Integer,VectorClock> messageBuffer;
	private static final long serialVersionUID = 1L;


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

	public HashMap<Integer, VectorClock> getMessageBuffer() {
		return messageBuffer;
	}

	public void setMessageBuffer(HashMap<Integer, VectorClock> messageBuffer) {
		this.messageBuffer = messageBuffer;
	}
}
