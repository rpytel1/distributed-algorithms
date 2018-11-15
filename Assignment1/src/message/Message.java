package message;

import clock.VectorClock;
import util.Buffer;

public class Message {
    String text;
    VectorClock timestamp;
    Buffer buffer;
    int receiverID;
    int senderID;
    
    public Message(String m, VectorClock vt, Buffer S, int sID, int rID){
		this.text = m;
		this.timestamp = vt;
		this.buffer = S;
		this.receiverID = rID;
		this.senderID = sID;
	}
    
    public String getText(){
    	return this.text;
    }
    
    public VectorClock getTimestamp(){
    	return this.timestamp;
    }
    
    public Buffer getBuffer(){
    	return this.buffer;
    }
    
    public int getReceiver(){
    	return this.receiverID;
    }
    
    public int getSender(){
    	return this.senderID;
    }

}
