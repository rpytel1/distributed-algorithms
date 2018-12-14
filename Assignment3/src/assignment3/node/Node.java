package assignment3.node;

import assignment3.link.Link;
import assignment3.link.LinkComparator;
import assignment3.link.LinkState;
import assignment3.message.Message;
import assignment3.message.MessageType;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public class Node extends UnicastRemoteObject implements IComponent {

    private IComponent[] nodes;

    private int id;
    private NodeState state;
    private int level;
    private double fragmentName;
    private Queue<Link> links; // the adjacent edges 	

    private double weightBestAdjacent;
    private Link bestEdge; //the adjacent edge leading towards the best candidate for the MOE it knows about
    private Link testEdge;
    private Link inBranch; //the adjacent edge leading to the core of the fragment
    public AtomicInteger findCount; // maybe atomic implementation

    public Node(int id, Queue<Link> links) throws RemoteException {
        super();
        this.id = id;
        state = NodeState.SLEEPING;
        level = 0;
        this.links = new PriorityQueue<Link>(links);
        fragmentName = this.links.peek().getWeight();
    }

    @Override
    public void wakeUp() throws RemoteException {
        System.out.println(id + ":Wakeup");

        Link edge = links.peek();
        System.out.println(id + ": changing link to " + edge.getReceiver(id) + " to IN_MST");
        edge.setState(LinkState.IN_MST);
        level = 0;
        state = NodeState.FOUND;
        findCount = new AtomicInteger(0);
        Message msg = new Message(MessageType.CONNECT, 0);
        System.out.println(id + ":Sending Connect to " + edge.getReceiver(id));
        new java.util.Timer().schedule(
                new java.util.TimerTask() {
                    @Override
                    public void run() {
                        try {
                            nodes[edge.getReceiver(id)].receive(msg, edge);
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                    }
                },
                10
        );
    }

    @Override
    public void receive(Message message, Link link) throws RemoteException {
        Link myLink = getMyLink(link);
        switch (message.getType()) {
            case TEST:
                receiveTest(message, myLink);
                break;
            case ACCEPT:
                receiveAccept(message, myLink);
                break;
            case CONNECT:
                receiveConnect(message, myLink);
                break;
            case INITIATE:
                receiveInitiate(message, myLink);
                break;
            case REPORT:
                receiveReport(message, myLink);
                break;
            case REJECT:
                receiveReject(message, myLink);
                break;
            case CHANGE_ROOT:
                receiveChangeRoot(message, myLink);
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

    public void send(Message message, Link link) throws RemoteException {

    }

    private Link getMyLink(Link link) {
        List<Link> linkLists = new ArrayList<>(links);
        Link linkToUpdate = new Link();
        for (Link linkEntry : linkLists) {
            if (linkEntry.compareTo(link) == 0) {
                linkToUpdate = linkEntry;
            }
        }
        return linkToUpdate;
    }

    @Override
    public void receiveConnect(Message message, Link link) throws RemoteException {
        System.out.println(id + ":Receive Connect from " + link.getReceiver(id));
        if (state == NodeState.SLEEPING)
            wakeUp();
        // the case that l < l' and fragment F is absorbed by F'
        if (message.getLevel() < this.level) {
            System.out.println(id + ": changing link to " + link.getReceiver(id) + " to IN_MST");
            link.setState(LinkState.IN_MST); // TODO: to be checked
            Message msg = new Message(MessageType.INITIATE, level, fragmentName, state);
            new java.util.Timer().schedule(
                    new java.util.TimerTask() {
                        @Override
                        public void run() {
                            try {
                                System.out.println(id + "Sending Receive Initiate to " + link.getReceiver(id));
                                nodes[link.getReceiver(id)].receive(msg, link);
                            } catch (RemoteException e) {
                                e.printStackTrace();
                            }
                        }
                    },
                    10
            );
            if (state == NodeState.FIND) {
                findCount.getAndIncrement();
            }
        } else {
            if (link.getState() == LinkState.CANDIDATE_IN_MST) {
                System.out.println(id + "Connect Appended to queue");
                // TODO: append message to the message queue
                new java.util.Timer().schedule(
                        new java.util.TimerTask() {
                            @Override
                            public void run() {
                                try {
                                    nodes[id].receiveConnect(message, link);
                                } catch (RemoteException e) {
                                    e.printStackTrace();
                                }
                            }
                        },
                        300);
            } else {
                // the merging case: l == l' and MOE == MOE'
                Message msg = new Message(MessageType.INITIATE, level + 1, link.getWeight(), NodeState.FIND);
                System.out.println(id + ":Sending Initiate to " + link.getReceiver(id));
                new java.util.Timer().schedule(
                        new java.util.TimerTask() {
                            @Override
                            public void run() {
                                try {
                                    nodes[link.getReceiver(id)].receive(msg, link);
                                } catch (RemoteException e) {
                                    e.printStackTrace();
                                }
                            }
                        },
                        10);
            }
        }
    }

    private void receiveInitiate(Message message, Link link) throws RemoteException {
        System.out.println(id + ":Receive Initiate from " + link.getReceiver(id));
        level = message.getLevel();
        fragmentName = message.getfName();
        state = message.getSenderState();
        inBranch = link; // the adjacent edge leading to the core of the fragment
        bestEdge = null;
        weightBestAdjacent = Double.POSITIVE_INFINITY;

        for (Link adjescentLink : this.links) {//TODO: Not sure if this is the correct one list of links
            if (adjescentLink.compareTo(link) != 0 && adjescentLink.getState() == LinkState.IN_MST) {
                Message msg = new Message(MessageType.INITIATE, level, fragmentName, state);
                System.out.println(id + ": Sending Initiate to " + adjescentLink.getReceiver(id));
                Link copy = new Link(adjescentLink);
                new java.util.Timer().schedule(
                        new java.util.TimerTask() {
                            @Override
                            public void run() {
                                try {
                                    nodes[copy.getReceiver(id)].receive(msg, copy);
                                } catch (RemoteException e) {
                                    e.printStackTrace();
                                }
                            }
                        },
                        10
                );
                if (state == NodeState.FIND) {
                    findCount.getAndIncrement();
                    // the messages sent
                }
            }
        }
        if (state == NodeState.FIND) {
            test();
        }
    }

    private void test() throws RemoteException {
        System.out.println(id + ":Test");
        if (links.stream().anyMatch(p -> p.getState() == LinkState.CANDIDATE_IN_MST)) {
            testEdge = links.stream().filter(p -> p.getState() == LinkState.CANDIDATE_IN_MST).min(new LinkComparator()).get();
            Message msg = new Message(MessageType.TEST, level, fragmentName);
            Link copy = new Link(testEdge);
            System.out.println(id + ":Sending Receive test to " + testEdge.getReceiver(id));
            new java.util.Timer().schedule(
                    new java.util.TimerTask() {
                        @Override
                        public void run() {
                            try {
                                nodes[copy.getReceiver(id)].receive(msg, testEdge);
                            } catch (RemoteException e) {
                                e.printStackTrace();
                            }
                        }
                    },
                    10
            );
        } else {
            if (testEdge != null) {
                System.out.println(id + "Changing test edge " + testEdge.getReceiver(id) + "to null");
            } else {
                System.out.println(id + "Changing test edge to null");
            }
            testEdge = null;
            report();
        }
    }

    @Override
    public void receiveTest(Message message, Link link) throws RemoteException {
        System.out.println(id + ":Receive Test from " + link.getReceiver(id));
        if (state == NodeState.SLEEPING) {
            wakeUp();
        }
        if (message.getLevel() > level) {
            //TODO: append message to the queue
            System.out.println(id + ": Test - Appended to queue");
            new java.util.Timer().schedule(
                    new java.util.TimerTask() {
                        @Override
                        public void run() {
                            try {
                                nodes[id].receiveTest(message, link);
                            } catch (RemoteException e) {
                                e.printStackTrace();
                            }
                        }
                    },
                    300);
            // receiving again with a delay
        } else {
            // if the name of the fragment is different then a possible MOE is identified
            if (message.getfName() != fragmentName) {
                Message msg = new Message(MessageType.ACCEPT);
                System.out.println(id + "Sending Accept to " + link.getReceiver(id));
                new java.util.Timer().schedule(
                        new java.util.TimerTask() {
                            @Override
                            public void run() {
                                try {
                                    nodes[link.getReceiver(id)].receive(msg, link);
                                } catch (RemoteException e) {
                                    e.printStackTrace();
                                }
                            }
                        },
                        10
                );
            } else {
                if (link.getState() == LinkState.CANDIDATE_IN_MST) {
                    System.out.println(id + ": changing link to " + link.getReceiver(id) + " to NOT_IN_MST");
                    link.setState(LinkState.NOT_IN_MST); //TODO: think about local copies
                }
                // if the node hasn't set this edge as testEdge then it sends a reject 
                // because they are in the same fragment with the sender
                if (link.compareTo(testEdge) != 0) {
                    System.out.println(id + ":Sending Reject to " + link.getReceiver(id));
                    Message msg = new Message(MessageType.REJECT);
                    new java.util.Timer().schedule(
                            new java.util.TimerTask() {
                                @Override
                                public void run() {
                                    try {
                                        nodes[link.getReceiver(id)].receive(msg, link);
                                    } catch (RemoteException e) {
                                        e.printStackTrace();
                                    }
                                }
                            },
                            10
                    );
                } else {
                    test();
                }
            }
        }
    }


    private void receiveReject(Message message, Link link) throws RemoteException {
        System.out.println(id + ":Receive Reject from " + link.getReceiver(id));
        if (link.getState() == LinkState.CANDIDATE_IN_MST) {
            System.out.println(id + ": changing link to " + link.getReceiver(id) + " to NOT_IN_MST");
            link.setState(LinkState.NOT_IN_MST);
        }
        test(); // to find another possible MOE
    }

    private void receiveAccept(Message message, Link link) throws RemoteException {
        System.out.println(id + ":Receive Accept from " + link.getReceiver(id));
        testEdge = null;
        if (link.getWeight() < weightBestAdjacent) {
            bestEdge = link;
            weightBestAdjacent = link.getWeight();
        }
        report();
    }

    private void report() throws RemoteException {
        System.out.println(id + ":Report find_count:" + findCount.get() + " testEdge:" + testEdge);
        if (findCount.get() == 0 && testEdge == null) {
            this.state = NodeState.FOUND;
            Message msg = new Message(MessageType.REPORT, weightBestAdjacent);
            System.out.println(id + ":Sending Report to " + inBranch.getReceiver(id));
            new java.util.Timer().schedule(
                    new java.util.TimerTask() {
                        @Override
                        public void run() {
                            try {
                                nodes[inBranch.getReceiver(id)].receive(msg, inBranch);
                            } catch (RemoteException e) {
                                e.printStackTrace();
                            }
                        }
                    },
                    10
            );
        }
    }

    @Override
    public void receiveReport(Message message, Link link) throws RemoteException {
        System.out.println(id + ":Receive Report from " + link.getReceiver(id));
        if (link.compareTo(inBranch) != 0) {
            findCount.getAndDecrement();
            if (message.getWeight() < weightBestAdjacent) {
                weightBestAdjacent = message.getWeight();
                bestEdge = link;
            }
            report();
        } else {
            if (this.state == NodeState.FIND) {
                // TODO: append message to the message queue
                new java.util.Timer().schedule(
                        new java.util.TimerTask() {
                            @Override
                            public void run() {
                                try {
                                    nodes[id].receiveTest(message, link);
                                } catch (RemoteException e) {
                                    e.printStackTrace();
                                }
                            }
                        },
                        500);
            } else {
                if (message.getWeight() > weightBestAdjacent)
                    changeRoot();
                else {
                    if (message.getWeight() == weightBestAdjacent &&
                            weightBestAdjacent == Double.POSITIVE_INFINITY) {
                        System.out.println(id+":HAAAAAAAAAAAAAAAAAAAAAAAAAALT");
                        // TODO: HALT
                    }
                }
            }
        }
    }

    private void changeRoot() throws RemoteException {
        System.out.println(id + ":Change Root");

        if (bestEdge.getState() == LinkState.IN_MST) {
            Message msg = new Message(MessageType.CHANGE_ROOT);
            // TODO: check if bestEdge is adjacent to the node
            nodes[bestEdge.getReceiver(id)].receiveChangeRoot(msg, bestEdge);
        } else {
            Message msg = new Message(MessageType.CONNECT, level);
            // TODO: check if bestEdge is adjacent to the node
            new java.util.Timer().schedule(
                    new java.util.TimerTask() {
                        @Override
                        public void run() {
                            try {
                                nodes[bestEdge.getReceiver(id)].receiveChangeRoot(msg, bestEdge);
                                System.out.println(id + ": changing bestEdge to " + bestEdge.getReceiver(id) + " to IN_MST");
                                bestEdge.setState(LinkState.IN_MST);
                            } catch (RemoteException e) {
                                e.printStackTrace();
                            }
                        }
                    },
                    10
            );

        }
    }

    @Override
    public void receiveChangeRoot(Message message, Link link) throws RemoteException {
        System.out.println(id + ":Receive Change Root");
        changeRoot();
    }

    @Override
    public void setEntities(IComponent[] entities) throws RemoteException {
        nodes = entities;
    }
}
