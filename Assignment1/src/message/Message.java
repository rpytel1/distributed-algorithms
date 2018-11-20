package message;

import java.io.Serializable;

import clock.VectorClock;
import util.Buffer;

/* Class that implements the structure of the messages being sent 
 * between processes
 */

public class Message implements Serializable{
    
	private static final long serialVersionUID = 1L;
	String text; // the content of the message
    VectorClock timestamp; // the vector clock associated with the message
    Buffer buffer; // the buffer that each process binds with the message that it sends
    int receiverID; // the id of the receiving process
    int senderID; // the id of the sending process
    int id; // the id of the message
    int delay; // the delay associated with the delivery of the message
    
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
