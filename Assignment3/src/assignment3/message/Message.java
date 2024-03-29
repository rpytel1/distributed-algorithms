package assignment3.message;

import java.io.Serializable;

import assignment3.node.NodeState;

public class Message implements Serializable {

    private MessageType type;
    private int senderId;
    private int receiverID;
    private int level;
    

	private double fName;
	private double bestWeight;
    private NodeState senderState;
    
    public Message() {
    }
    
 // constructor for ACCEPT, REJECT and CHANGE_ROOT messages
    public Message(MessageType type) {
    	this.type = type;
    }
    
    // constructor for CONNECT messages
    public Message(MessageType type, int level) {
    	this.type = type;
    	this.level = level;
    }
    
    // constructor for REPORT messages
    public Message(MessageType type, double weight) {
    	this.type = type;
    	this.bestWeight = weight;
    }
    
    // constructor for TEST messages
    public Message(MessageType type, int level, double name) {
    	this.type = type;
    	this.level = level;
    	fName = name;
    }
    
    //constructor for INITIATE messages 
    public Message(MessageType type, int level, double name, NodeState s) {
    	this.type = type;
    	this.level = level;
    	fName = name;
    	senderState = s;
    }
    
    public MessageType getType() {
        return type;
    }

    public void setType(MessageType type) {
        this.type = type;
    }

    public int getSenderId() {
        return senderId;
    }

    public void setSenderId(int senderId) {
        this.senderId = senderId;
    }

    public int getReceiverID() {
        return receiverID;
    }

    public void setReceiverID(int receiverID) {
        this.receiverID = receiverID;
    }
    
    public int getLevel() {
		return level;
	}
    
	public void setLevel(int level) {
		this.level = level;
	}
	
	public double getfName() {
		return fName;
	}
	
	public void setfName(double fName) {
		this.fName = fName;
	}
	
	public double getWeight() {
		return bestWeight;
	}
	
	public void setWeight(double weight) {
		this.bestWeight = weight;
	}
	
	public NodeState getSenderState() {
		return senderState;
	}
	
	public void setSenderState(NodeState senderState) {
		this.senderState = senderState;
	}
}
