package assignment3.node;

import assignment3.link.Link;
import assignment3.link.LinkComparator;
import assignment3.link.LinkState;
import assignment3.message.Message;
import assignment3.message.MessageType;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public class Node extends UnicastRemoteObject implements IComponent {

    private Node[] nodes;

    private int id;
    private NodeState state;
    private int level;
    private double fragmentName;
    private Queue<Link> links; // the adjacent edges 	

    private double weightBestAdjacent;
    private Link bestEdge; //the adjacent edge leading towards the best candidate for the MOE it knows about
    private Link testEdge;
    private Link inBranch; //the adjacent edge leading to the core of the fragment
    public int findCount; // maybe atomic implementation

    protected Node(int id, Queue<Link> links) throws RemoteException {
        super();
        this.id = id;
        state = NodeState.SLEEPING;
        level = 0;
        this.links = new PriorityQueue<Link>(links);
        fragmentName = this.links.peek().getWeight();
    }

    public void wakeUp() throws RemoteException {
        Link edge = links.peek();
        edge.setState(LinkState.IN_MST); // TODO: to be checked
        level = 0;
        state = NodeState.FOUND;
        findCount = 0;
        Message msg = new Message(MessageType.CONNECT, 0);
        nodes[edge.getReceiver(id)].receive(msg, edge);
    }


    public void receive(Message message, Link link) throws RemoteException {
        updateLinks(link);
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
            case REPORT:
                receiveReport(message, link);
                break;
            case REJECT:
                receiveReject(message, link);
                break;
            case CHANGE_ROOT:
                receiveChangeRoot(message, link);
                break;
        }
    }

    public void updateLinks(Link link) {
        List<Link> linkLists = new ArrayList<>(links);
        Link linkToUpdate = new Link();
        for (Link linkEntry : linkLists) {
            if (linkEntry.compareTo(link) == 0) {
                linkToUpdate = linkEntry;
            }
        }
        linkLists.remove(linkToUpdate);
        linkLists.add(link);
        this.links = new PriorityQueue<>(linkLists);
    }

    public void send(Message message, Link link) {

    }

    private void receiveConnect(Message message, Link link) throws RemoteException {
        if (state == NodeState.SLEEPING)
            wakeUp();
        // the case that l < l' and fragment F is absorbed by F'
        if (message.getLevel() < this.level) {
            link.setState(LinkState.IN_MST); // TODO: to be checked
            Message msg = new Message(MessageType.INITIATE, level, fragmentName, state);
            nodes[link.getReceiver(id)].receive(msg, link);
            if (state == NodeState.FIND)
                findCount++;
        } else {
            if (link.getState() == LinkState.CANDIDATE_IN_MST) {
                // TODO: append message to the message queue
            	// probably receiveConnect is invoked again with a delay 
            	// and when the condition is met the message is received
            } else {
            	// the merging case: l == l' and MOE == MOE'
                Message msg = new Message(MessageType.INITIATE, level + 1, link.getWeight(), NodeState.FIND);
                nodes[link.getReceiver(id)].receive(msg, link);
            }
        }
    }

    private void receiveInitiate(Message message, Link link) throws RemoteException {
        level = message.getLevel();
        fragmentName = message.getfName();
        state = message.getSenderState();
        inBranch = link; // the adjacent edge leading to the core of the fragment
        bestEdge = null;
        weightBestAdjacent = Double.POSITIVE_INFINITY;

        for (Link adjescentLink : this.links) {//TODO: Not sure if this is the correct one list of links
            if (adjescentLink != link && adjescentLink.getState() == LinkState.IN_MST) {
                Message msg = new Message(MessageType.INITIATE, level, fragmentName, state);
                nodes[adjescentLink.getReceiver(id)].receive(msg, adjescentLink);
                if (state == NodeState.FIND) {
                    findCount = findCount + 1; // the messages sent
                }
            }
        }
        if (state == NodeState.FIND) {
            test();
        }
    }

    private void test() throws RemoteException {
        if (links.stream().anyMatch(p -> p.getState() == LinkState.CANDIDATE_IN_MST)) {
            testEdge = links.stream().filter(p->p.getState()== LinkState.CANDIDATE_IN_MST).min(new LinkComparator()).get();
            Message msg = new Message(MessageType.TEST, level, fragmentName);
            nodes[testEdge.getReceiver(id)].receive(msg, testEdge);
        } else {
            testEdge = null;
            report();
        }
    }

    private void receiveTest(Message message, Link link) throws RemoteException {
        if (state == NodeState.SLEEPING) {
            wakeUp();
        }
        if (message.getLevel() > level) {
            //TODO: append message to the queue
        	// receiving again with a delay
        } else {
        	// if the name of the fragment is different then a possible MOE is identified
            if (message.getfName() != fragmentName) { 
                Message msg = new Message(MessageType.ACCEPT);
                nodes[link.getReceiver(id)].receive(msg, link);
            } else {
                if (link.getState() == LinkState.CANDIDATE_IN_MST) {
                    link.setState(LinkState.NOT_IN_MST); //TODO: think about local copies
                }
                // if the node hasn't set this edge as testEdge then it sends a reject 
                // because they are in the same fragment with the sender
                if (link.compareTo(testEdge)==0) {
                    Message msg = new Message(MessageType.REJECT);
                    nodes[link.getReceiver(id)].receive(msg, link);
                } else {
                    test();
                }
            }
        }
    }


    private void receiveReject(Message message, Link link) throws RemoteException {
        if (link.getState() == LinkState.CANDIDATE_IN_MST) {
            link.setState(LinkState.NOT_IN_MST);
        }
        test(); // to find another possible MOE
    }

    private void receiveAccept(Message message, Link link) throws RemoteException {
    	testEdge = null;
    	if (link.getWeight() < weightBestAdjacent){
    		bestEdge = link; 
    		weightBestAdjacent = link.getWeight();
    	}
    	report();
    }

    private void report() throws RemoteException {
    	if (findCount==0 && testEdge.equals(null)){
    		this.state = NodeState.FOUND;
    		Message msg = new Message(MessageType.REPORT, weightBestAdjacent);
    		nodes[inBranch.getReceiver(id)].receive(msg, inBranch);
    	}
    }

    private void receiveReport(Message message, Link link) throws RemoteException {
    	if (!link.equals(inBranch)){
    		findCount -=1;
    		if (message.getWeight() < weightBestAdjacent){
    			weightBestAdjacent = message.getWeight();
    			bestEdge = link;
        	}
    		report();
    	}
    	else{
    		if (this.state == NodeState.FIND){
    			// TODO: append message to the message queue
    		}
    		else{
    			if (message.getWeight() > weightBestAdjacent)
    				changeRoot();
    			else{
    				if (message.getWeight() == weightBestAdjacent &&
    						weightBestAdjacent == Double.POSITIVE_INFINITY){
    					// TODO: HALT
    				}
    			}
    		}
    	}
    }

    private void changeRoot() {
    	if (bestEdge.getState() == LinkState.IN_MST){
    		Message msg = new Message(MessageType.CHANGE_ROOT);
    		// TODO: check if bestEdge is adjacent to the node
    		nodes[bestEdge.getReceiver(id)].receiveChangeRoot(msg, bestEdge);
    	}
    	else{
    		Message msg = new Message(MessageType.CONNECT, level);
    		// TODO: check if bestEdge is adjacent to the node
    		nodes[bestEdge.getReceiver(id)].receiveChangeRoot(msg, bestEdge);
    		bestEdge.setState(LinkState.IN_MST);
    	}
    }

    private void receiveChangeRoot(Message message, Link link) {
    	changeRoot();
    }
}
