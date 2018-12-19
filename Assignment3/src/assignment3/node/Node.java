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

    private IComponent[] nodes = null;

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
        fragmentName = id;
//        bestEdge = links.peek();
    }

    @Override
    public void wakeUp() throws RemoteException {
        Link edge = links.peek();
        edge.setState(LinkState.IN_MST);
        System.out.println(id + ":Wakeup");

        System.out.println(id + ": changing link to " + edge.getReceiver(id) + " to IN_MST");
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
                            int receiverId = edge.getReceiver(id);
                            Thread.sleep(10);
                            nodes[receiverId].receive(msg, edge);
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                },
                0
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
    public synchronized void receiveConnect(Message message, Link link) throws RemoteException {
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
                System.out.println(id + ": Connect Incrementing findcount");
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
                                    Thread.sleep(300);
                                    nodes[id].receive(message, link);
                                } catch (RemoteException e) {
                                    e.printStackTrace();
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                        },
                        0);
            } else {
                // the merging case: l == l' and MOE == MOE'
                Message msg = new Message(MessageType.INITIATE, level + 1, link.getWeight(), NodeState.FIND);
                System.out.println(id + ":Sending Initiate to " + link.getReceiver(id));
                new java.util.Timer().schedule(
                        new java.util.TimerTask() {
                            @Override
                            public void run() {
                                try {
                                    int receiverId = link.getReceiver(id);
                                    Thread.sleep(40);//It is added to preserve order of messages sended from certain node
                                    nodes[receiverId].receive(msg, link);
                                } catch (RemoteException e) {
                                    e.printStackTrace();
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                        },
                        0);
            }
        }
    }

    private synchronized void receiveInitiate(Message message, Link link) throws RemoteException {
        System.out.println(id + ":Receive Initiate from " + link.getReceiver(id));
        level = message.getLevel();
        fragmentName = message.getfName();
        state = message.getSenderState();
        inBranch = link; // the adjacent edge leading to the core of the fragment
        bestEdge = null;
        weightBestAdjacent = Double.POSITIVE_INFINITY;

        for (Link adjescentLink : this.links) {
            if (adjescentLink.compareTo(link) != 0 && adjescentLink.getState() == LinkState.IN_MST) {
                Message msg = new Message(MessageType.INITIATE, level, fragmentName, state);
                System.out.println(id + ": Sending Initiate to " + adjescentLink.getReceiver(id));
                new java.util.Timer().schedule(
                        new java.util.TimerTask() {
                            @Override
                            public void run() {
                                try {
                                    Link copy = new Link(adjescentLink);
                                    Thread.sleep(10);
                                    nodes[copy.getReceiver(id)].receive(msg, copy);
                                } catch (RemoteException e) {
                                    e.printStackTrace();
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                        },
                        0
                );
                if (state == NodeState.FIND) {
                    System.out.println(id + ": Initiate Incrementing findcount");
                    findCount.getAndIncrement();
                    // the messages sent
                }
            }
        }
        if (state == NodeState.FIND) {
            test();
        }
    }

    private synchronized void test() throws RemoteException {
        System.out.println(id + ":Test");
        if (links.stream().anyMatch(p -> p.getState() == LinkState.CANDIDATE_IN_MST)) {
            testEdge = links.stream().filter(p -> p.getState() == LinkState.CANDIDATE_IN_MST).min(new LinkComparator()).get();
            Message msg = new Message(MessageType.TEST, level, fragmentName);
            System.out.println(id + ":Sending Receive test to " + testEdge.getReceiver(id));
            int reciverID = testEdge.getReceiver(id);
            new java.util.Timer().schedule(
                    new java.util.TimerTask() {
                        @Override
                        public void run() {
                            try {
                                Thread.sleep(10);
                                nodes[reciverID].receive(msg, testEdge);
                            } catch (RemoteException e) {
                                e.printStackTrace();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    },
                    0
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
    public synchronized void receiveTest(Message message, Link link) throws RemoteException {
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
                                Thread.sleep(300);
                                nodes[id].receiveTest(message, link);
                            } catch (RemoteException e) {
                                e.printStackTrace();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    },
                    0);
            // receiving again with a delay
        } else {
            // if the name of the fragment is different then a possible MOE is identified
            System.out.println(id + ":Receive Test" + message.getfName() + " " + fragmentName);
            if (message.getfName() != fragmentName) {
                Message msg = new Message(MessageType.ACCEPT);
                System.out.println(id + "Sending Accept to " + link.getReceiver(id));
                new java.util.Timer().schedule(
                        new java.util.TimerTask() {
                            @Override
                            public void run() {
                                try {
                                    Thread.sleep(10);
                                    nodes[link.getReceiver(id)].receive(msg, link);
                                } catch (RemoteException e) {
                                    e.printStackTrace();
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                        },
                        0
                );
            } else {
                if (link.getState() == LinkState.CANDIDATE_IN_MST) {
                    System.out.println(id + ": changing link to " + link.getReceiver(id) + " to NOT_IN_MST");
                    link.setState(LinkState.NOT_IN_MST); //TODO: think about local copies
                }
                // if the node hasn't set this edge as testEdge then it sends a reject 
                // because they are in the same fragment with the sender
                if (link.compareTo(testEdge) != 0) {

                    new java.util.Timer().schedule(
                            new java.util.TimerTask() {
                                @Override
                                public void run() {
                                    try {
                                        System.out.println(id + ":Sending Reject to " + link.getReceiver(id));
                                        Message msg = new Message(MessageType.REJECT);
                                        Thread.sleep(10);
                                        nodes[link.getReceiver(id)].receive(msg, link);
                                    } catch (RemoteException e) {
                                        e.printStackTrace();
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                }
                            },
                            0
                    );
                } else {
                    test();
                }
            }
        }
    }


    private synchronized void receiveReject(Message message, Link link) throws RemoteException {
        System.out.println(id + ":Receive Reject from " + link.getReceiver(id));
        if (link.getState() == LinkState.CANDIDATE_IN_MST) {
            System.out.println(id + ": changing link to " + link.getReceiver(id) + " to NOT_IN_MST");
            link.setState(LinkState.NOT_IN_MST);
        }
        test(); // to find another possible MOE
    }

    private synchronized void receiveAccept(Message message, Link link) throws RemoteException {
        System.out.println(id + ":Receive Accept from " + link.getReceiver(id));
        System.out.println(id + ": Changing test edge from" + link.getReceiver(id) + " to null");
        testEdge = null;
        System.out.println(id + ": Accept " + link.getWeight() + " " + weightBestAdjacent);
        if (link.getWeight() < weightBestAdjacent) {
            bestEdge = link;
            weightBestAdjacent = link.getWeight();
        }
        report();
    }

    private synchronized void report() throws RemoteException {
        System.out.println(id + ":Report find_count:" + findCount.get() + " testEdge:" + testEdge);
        if (testEdge != null) {
            System.out.println(id + ": " + testEdge.getReceiver(id) + " " + testEdge.getWeight());
        }
        if (findCount.get() == 0 && testEdge == null) {
            this.state = NodeState.FOUND;
            Message msg = new Message(MessageType.REPORT, weightBestAdjacent);
            System.out.println(id + ":Sending Report to " + inBranch.getReceiver(id));
            new java.util.Timer().schedule(
                    new java.util.TimerTask() {
                        @Override
                        public void run() {
                            try {
                                int receiverId = inBranch.getReceiver(id);
                                Thread.sleep(10);
                                nodes[receiverId].receive(msg, inBranch);
                            } catch (RemoteException e) {
                                e.printStackTrace();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    },
                    0
            );
        }
    }

    @Override
    public synchronized void receiveReport(Message message, Link link) throws RemoteException {
        System.out.println(id + ":Receive Report from " + link.getReceiver(id) + " state:" + state + " find_count:" + findCount);
        System.out.println(id + " " + link.getWeight() + " " + inBranch.getWeight());
        if (link.compareTo(inBranch) != 0) {
            System.out.println(id + ": Decrementing findcount");
            findCount.getAndDecrement();
            System.out.println(id + ": Report " + message.getWeight() + " " + weightBestAdjacent);
            if (message.getWeight() < weightBestAdjacent) {
                weightBestAdjacent = message.getWeight();
                System.out.println(id + ": Report BestEdge changed from " + bestEdge.getReceiver(id) + "to  " + bestEdge.getReceiver(id));
                bestEdge = link;
            }
            report();
        } else {
            if (this.state == NodeState.FIND) {
                // TODO: append message to the message queue
                System.out.println("Adding report to the queue");
                new java.util.Timer().schedule(
                        new java.util.TimerTask() {
                            @Override
                            public void run() {
                                try {
                                    Thread.sleep(500);
                                    nodes[id].receive(message, link);
                                } catch (RemoteException e) {
                                    e.printStackTrace();
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                        },
                        0);
            } else {
                if (message.getWeight() > weightBestAdjacent)
                    changeRoot();
                else {
                    if (message.getWeight() == weightBestAdjacent &&
                            weightBestAdjacent == Double.POSITIVE_INFINITY) {
                        System.out.println(id + ":HAAAAAAAAAAAAAAAAAAAAAAAAAALT");
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
            new java.util.Timer().schedule(
                    new java.util.TimerTask() {
                        @Override
                        public void run() {
                            try {
                                int receiverId = bestEdge.getReceiver(id);
                                Thread.sleep(10);
                                System.out.println(id + ":Sending change root to " + receiverId);
                                nodes[receiverId].receive(msg, bestEdge);

                            } catch (RemoteException e) {
                                e.printStackTrace();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    },
                    0
            );
        } else {
            Message msg = new Message(MessageType.CONNECT, level);
            // TODO: check if bestEdge is adjacent to the node
            System.out.println(id + ":Sending connect message " + bestEdge.getReceiver(id));
            new java.util.Timer().schedule(
                    new java.util.TimerTask() {
                        @Override
                        public void run() {
                            try {
                                Link copy = new Link(bestEdge);
                                int receiverId = copy.getReceiver(id);
                                Thread.sleep(10);
                                nodes[receiverId].receive(msg, copy);
                            } catch (RemoteException e) {
                                e.printStackTrace();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    },
                    0
            );
            System.out.println(id + ": changing bestEdge to " + bestEdge.getReceiver(id) + " to IN_MST");
            bestEdge.setState(LinkState.IN_MST);

        }
    }

    @Override
    public synchronized void receiveChangeRoot(Message message, Link link) throws RemoteException {
        System.out.println(id + ":Receive Change Root");
        changeRoot();
    }

    @Override
    public void setEntities(IComponent[] entities) throws RemoteException {
        nodes = entities;
    }
    
    @Override
    public IComponent[] getEntities() throws RemoteException{
    	return nodes;
    }
}
