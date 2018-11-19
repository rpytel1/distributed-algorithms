package message;

import java.io.Serializable;

import clock.VectorClock;
import util.Buffer;

public class Message implements Serializable{
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	String text;
    VectorClock timestamp;
    Buffer buffer;
    int receiverID;
    int senderID;
    int id;
    int delay;
    
    public Message(int id, String m, VectorClock vt, Buffer S, int sID, int rID, int delay){
    	this.id = id;
		this.text = m;
		this.timestamp = vt;
		this.buffer = S;
		this.receiverID = rID;
		this.senderID = sID;
		this.delay = delay;
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
    
    public int getDelay(){
    	return this.delay;
    }
    
    public void setBuffer(Buffer b){
    	this.buffer = b;
    }
    
    public int getReceiver(){
    	return this.receiverID;
    }
    
    public int getSender(){
    	return this.senderID;
    }
    
    public int getId(){
    	return this.id;
    }
    
    @Override
    public String toString() {
        return "Message{" +
                "id=" + id +
                ", srcId=" + senderID +
                ", destId=" + receiverID +
                ", content=" + text +
                ", time=" + timestamp.getVector()[senderID] +
                '}';
}

	public void setTimestamp(VectorClock vt) {
		this.timestamp = vt;
	}

}
