package assignment3.node;

import assignment3.link.Link;
import assignment3.message.Message;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;

public class Node extends UnicastRemoteObject implements IComponent{

    Node[] nodes;

    int id;
    NodeState state;
    int level;
    String fragmentName;

    List<Link> coreLinks;
    Link testEdgeAdjacent;
    int weightBestAdjecent;
    List<Link> links;
    List<Link> moeCandidatesList;

    protected Node() throws RemoteException {
        super();
    }

    public void wakeUp() {

    }

    public void receive(Message message, Link link) {
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

    private void receiveConnect(Message message, Link link) {

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
