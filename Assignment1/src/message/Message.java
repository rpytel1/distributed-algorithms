package message;

import clock.VectorClock;
import util.Buffer;

import java.io.Serializable;

public class Message implements Serializable {
    String text;
    VectorClock timestamp;
    Buffer buffer;
    int receiverID;
    int senderID;
    private static final long serialVersionUID = 1L;

    public Message() {

    }

    public Message(String m, VectorClock vt, Buffer S, int sID, int rID) {
        this.text = m;
        this.timestamp = vt;
        this.buffer = S;
        this.receiverID = rID;
        this.senderID = sID;
    }

    public String getText() {
        return this.text;
    }

    public VectorClock getTimestamp() {
        return this.timestamp;
    }

    public Buffer getBuffer() {
        return this.buffer;
    }

    public int getReceiverID() {
        return this.receiverID;
    }

    public int getSenderID() {
        return this.senderID;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setTimestamp(VectorClock timestamp) {
        this.timestamp = timestamp;
    }

    public void setBuffer(Buffer buffer) {
        this.buffer = buffer;
    }


    public void setReceiverID(int receiverID) {
        this.receiverID = receiverID;
    }


    public void setSenderID(int senderID) {
        this.senderID = senderID;
    }


}
