package assignment3.node;

import assignment3.link.Link;
import assignment3.link.LinkState;
import assignment3.message.Message;
import assignment3.message.MessageType;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
import java.util.TreeSet;

public class Node extends UnicastRemoteObject implements IComponent{

    private Node[] nodes;

    private int id;
    private NodeState state;
    private int level;
    private double fragmentName;

    private Queue<Link> coreLinks;
    private int weightBestAdjecent;
    private Queue<Link> links;
    private Queue<Link> moeCandidatesList;
    public int findCount;

    protected Node(int id, Set<Link> links) throws RemoteException {
        super();
        this.id = id;
        state = NodeState.SLEEPING;
        level = 0;
        this.links = new PriorityQueue<Link>(links);
        moeCandidatesList = new PriorityQueue<Link>(links);
    }

    public void wakeUp() throws RemoteException {
    	Link edge = links.peek();
    	edge.setState(LinkState.IN_MST); // TODO: to be checked 
    	level = 0;
    	state = NodeState.FOUND;
        findCount = 0;
        Message msg = new Message(MessageType.CONNECT,0);
        nodes[edge.getReceiver(id)].receive(msg, edge);
    }


    public void receive(Message message, Link link)  throws RemoteException {
        switch (message.getType()) {
            case TEST:
                receiveTest(message, link);
                break;
            case ACCEPT:
                receiveAccept(message, link);
                break;
            case CONNECT:
                receiveConnect(message, link);
                break;
            case INITIATE:
                receiveInitiate(message, link);
                break;
            case CHANGE_ROOT:
                receiveChangeRoot(message, link);
                break;
        }
    }

    public void send(Message message, Link link) {

    }

    private void receiveConnect(Message message, Link link) throws RemoteException {
    	if (state == NodeState.SLEEPING)
    		wakeUp();
    	if (message.getLevel()<this.level){
    		link.setState(LinkState.IN_MST); // TODO: to be checked
    		Message msg = new Message(MessageType.INITIATE,level,fragmentName,state);
    		nodes[link.getReceiver(id)].receive(msg, link);
    		if (state == NodeState.FIND)
    			findCount++;
    	}
		else{
			if (link.getState() == LinkState.CANDIDATE_IN_MST){
				// TODO: append message to the message queue
			}
			else{
				Message msg = new Message(MessageType.INITIATE,level+1,link.getWeight(),NodeState.FIND);
	    		nodes[link.getReceiver(id)].receive(msg, link);
			}
		}
    }

    private void receiveInitiate(Message message, Link link) {

    }

    private void test() {

    }

    private void receiveTest(Message message, Link link) {

    }

    private void reject(Link link) {

    }

    private void receiveReject(Message message, Link link) {

    }

    private void receiveAccept(Message message, Link link) {

    }

    private void report() {

    }

    private void receiveReport(Message message, Link link) {

    }

    private void changeRoot() {

    }

    private void receiveChangeRoot(Message message, Link link) {

    }
}
