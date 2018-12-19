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
    public AtomicInteger ConnectSent;
    public AtomicInteger TestSent;
    public AtomicInteger InitiateSent;
    public AtomicInteger ReportSent;
    public AtomicInteger AcceptSent;
    public AtomicInteger RejectSent;
    public AtomicInteger ChangeRootSent;
    public AtomicInteger ConnectReceived;
    public AtomicInteger TestReceived;
    public AtomicInteger InitiateReceived;
    public AtomicInteger ReportReceived;
    public AtomicInteger AcceptReceived;
    public AtomicInteger RejectReceived;
    public AtomicInteger ChangeRootReceived;
    public AtomicInteger merges;
    public AtomicInteger absorbs;
    public Map<Double,Link> cores;
    public Map<Double,Link> mstEdges;
    public Map<Integer,List<Link>> levels;

    public Node(int id, Queue<Link> links) throws RemoteException {
        super();
        this.id = id;
        state = NodeState.SLEEPING;
        level = 0;
        this.links = new PriorityQueue<Link>(links);
        fragmentName = id;
        initializeMetrics();
        cores = new HashMap<Double,Link>();
        mstEdges = new HashMap<Double,Link>();
        levels = new HashMap<Integer,List<Link>>();
    }

    private void initializeMetrics(){
		ConnectSent = new AtomicInteger(0);
		TestSent = new AtomicInteger(0);
		InitiateSent = new AtomicInteger(0);
		ReportSent = new AtomicInteger(0);
		AcceptSent = new AtomicInteger(0);
		RejectSent = new AtomicInteger(0);
		ChangeRootSent = new AtomicInteger(0);
		ConnectReceived = new AtomicInteger(0);
		TestReceived = new AtomicInteger(0);
		InitiateReceived = new AtomicInteger(0);
		ReportReceived = new AtomicInteger(0);
		AcceptReceived = new AtomicInteger(0);
		RejectReceived = new AtomicInteger(0);
		ChangeRootReceived = new AtomicInteger(0);
		merges = new AtomicInteger(0);
		absorbs = new AtomicInteger(0);
    }
    
    @Override
    public void wakeUp() throws RemoteException {
        Link edge = links.peek();
        edge.setState(LinkState.IN_MST);
        mstEdges.put(edge.getWeight(), edge);
        System.out.println(id + ":Wakeup");

        System.out.println(id + ": changing link to " + edge.getReceiver(id) + " to IN_MST");
        level = 0;
        state = NodeState.FOUND;
        findCount = new AtomicInteger(0);
        Message msg = new Message(MessageType.CONNECT, 0);
        System.out.println(id + ":Sending Connect to " + edge.getReceiver(id));
        ConnectSent.getAndIncrement();
        new java.util.Timer().schedule(
                new java.util.TimerTask() {
                    @Override
                    public void run() {
                        try {
                            int receiverId = edge.getReceiver(id);
                            Thread.sleep(100);
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
    public synchronized void receive(Message message, Link link) throws RemoteException {
        try {
            Thread.sleep((long)(new Random().nextDouble()*10));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Link myLink = getMyLink(link);
        switch (message.getType()) {
            case TEST:
            	TestReceived.getAndIncrement();
                receiveTest(message, myLink);
                break;
            case ACCEPT:
            	AcceptReceived.getAndIncrement();
                receiveAccept(message, myLink);
                break;
            case CONNECT:
            	ConnectReceived.getAndIncrement();
                receiveConnect(message, myLink);
                break;
            case INITIATE:
            	InitiateReceived.getAndIncrement();
                receiveInitiate(message, myLink);
                break;
            case REPORT:
            	ReportReceived.getAndIncrement();
                receiveReport(message, myLink);
                break;
            case REJECT:
            	RejectReceived.getAndIncrement();
                receiveReject(message, myLink);
                break;
            case CHANGE_ROOT:
            	ChangeRootReceived.getAndIncrement();
                receiveChangeRoot(message, myLink);
                break;
        }
        if (testEdge != null) {
            System.out.println(id + ": test edge " + id + " to " + testEdge.getReceiver(id) + " fragment " + fragmentName);
        }
    }

    public synchronized void updateLinks(Link link) {
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

    private synchronized Link getMyLink(Link link) {
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
            link.setState(LinkState.IN_MST);
            mstEdges.put(link.getWeight(), link);
            Message msg = new Message(MessageType.INITIATE, level, fragmentName, state);
            InitiateSent.getAndIncrement();
            absorbs.getAndIncrement();
            new java.util.Timer().schedule(
                    new java.util.TimerTask() {
                        @Override
                        public void run() {
                            try {
                                System.out.println(id + "Sending Receive Initiate to " + link.getReceiver(id));
                                Thread.sleep(100);
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
                                    Thread.sleep(3000);
                                    nodes[id].receiveConnect(message, link);
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
                InitiateSent.getAndIncrement();
                merges.getAndIncrement();
                cores.put(link.getWeight(), link);
                List<Link> tt;
                if (levels.containsKey(level+1))
                	tt = levels.get(level+1);
                else
                	tt = new ArrayList<Link>();
                tt.add(link);
                levels.put(level+1, tt);
                System.out.println(id + ":Sending Initiate to " + link.getReceiver(id));
                new java.util.Timer().schedule(
                        new java.util.TimerTask() {
                            @Override
                            public void run() {
                                try {
                                    int receiverId = link.getReceiver(id);
                                    Thread.sleep(400);//It is added to preserve order of messages sended from certain node
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
                InitiateSent.getAndIncrement();
                new java.util.Timer().schedule(
                        new java.util.TimerTask() {
                            @Override
                            public void run() {
                                try {
                                    Link copy = new Link(adjescentLink);
                                    Thread.sleep(100);
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
            System.out.println(id + ":Sending Receive test to " + testEdge.getReceiver(id) + " fragment" + fragmentName);
            TestSent.getAndIncrement();
            int reciverID = testEdge.getReceiver(id);
            new java.util.Timer().schedule(
                    new java.util.TimerTask() {
                        @Override
                        public void run() {
                            try {
                                Thread.sleep(100);
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
                                Thread.sleep(3000);
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
                AcceptSent.getAndIncrement();
                new java.util.Timer().schedule(
                        new java.util.TimerTask() {
                            @Override
                            public void run() {
                                try {
                                    Thread.sleep(100);
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
                    link.setState(LinkState.NOT_IN_MST); //TODO: maybe should change test edge to null?
                }
                // if the node hasn't set this edge as testEdge then it sends a reject 
                // because they are in the same fragment with the sender
                if (testEdge != null) {
                    System.out.println(id + ":" + testEdge.getWeight() + " " + link.getWeight());
                    System.out.println(id + ":" + link.compareTo(testEdge));

                } else {

                    System.out.println(id + ": test edge null");
                }
                if (link.compareTo(testEdge) != 0) {

                    new java.util.Timer().schedule(
                            new java.util.TimerTask() {
                                @Override
                                public void run() {
                                    try {
                                        System.out.println(id + ":Sending Reject to " + link.getReceiver(id));
                                        RejectSent.getAndIncrement();
                                        Message msg = new Message(MessageType.REJECT);
                                        Thread.sleep(100);
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
        testEdge = null;
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
            ReportSent.getAndIncrement();
            new java.util.Timer().schedule(
                    new java.util.TimerTask() {
                        @Override
                        public void run() {
                            try {
                                int receiverId = inBranch.getReceiver(id);
                                Thread.sleep(100);
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
//                System.out.println(id + ": Report BestEdge changed from " + bestEdge.getReceiver(id) + "to  " + bestEdge.getReceiver(id));
                bestEdge = link;
            }
            report();
        } else {
            if (this.state == NodeState.FIND) {
                System.out.println("Adding report to the queue");
                new java.util.Timer().schedule(
                        new java.util.TimerTask() {
                            @Override
                            public void run() {
                                try {
                                    Thread.sleep(5000);
                                    nodes[id].receiveReport(message, link);
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
                        Map<String,Integer> stats = null;
                        Map<String,Integer> temp;
                        int t;
                        for (IComponent node:nodes){
                        	if (node.getID()==id){
                        		temp = getMetrics();
                        	}
                        	else{
                        		temp = node.getMetrics();
                        		for (Double w:node.getMST().keySet()){
                        			if (!mstEdges.containsKey(w))
                        				mstEdges.put(w, node.getMST().get(w));
                        		}
                        		for (Double w:node.getCores().keySet()){
                        			if (!cores.containsKey(w))
                        				cores.put(w, node.getCores().get(w));
                        		}
                        		for(int i:node.getLevels().keySet()){
                        			if (!levels.containsKey(i)){
                        				levels.put(i, node.getLevels().get(i));
                        			}
                        			else{
                        				List<Link> tt;
	                        			for (Link l:node.getLevels().get(i)){
	                        				if (!levels.get(i).stream().anyMatch(p -> p.getWeight() == l.getWeight())){
	                        					tt = levels.get(i);
	                        					tt.add(l);
	                        					levels.put(i, tt);
	                        				}				
	                        			}
                        			}
                        		}
                        	}
                        	if (stats==null)
                        		stats = new HashMap<String,Integer>(temp);
                        	else{
                        		for (String k: stats.keySet()){
                        			t = stats.get(k);
                        			stats.put(k, t+temp.get(k));
                        		}
                        	}	
                        }
                        t = stats.get("merges");
                        stats.put("merges", t/2);
                        presentStats(stats);
                    }
                }
            }
        }
    }

    private void changeRoot() throws RemoteException {
        System.out.println(id + ":Change Root");

        if (bestEdge.getState() == LinkState.IN_MST) {
            Message msg = new Message(MessageType.CHANGE_ROOT);
            ChangeRootSent.getAndIncrement();
            new java.util.Timer().schedule(
                    new java.util.TimerTask() {
                        @Override
                        public void run() {
                            try {
                                int receiverId = bestEdge.getReceiver(id);
                                Thread.sleep(100);
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
            System.out.println(id + ":Sending connect message " + bestEdge.getReceiver(id));
            ConnectSent.getAndIncrement();
            new java.util.Timer().schedule(
                    new java.util.TimerTask() {
                        @Override
                        public void run() {
                            try {
                                Link copy = new Link(bestEdge);
                                int receiverId = copy.getReceiver(id);
                                Thread.sleep(100);
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
            mstEdges.put(bestEdge.getWeight(), bestEdge);

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

	@Override
	public int getID() throws RemoteException {
		return id;
	}

	@Override
	public Map<String,Integer> getMetrics() throws RemoteException {
		// TODO Auto-generated method stub
		Map<String,Integer> stat = new HashMap<String, Integer>();
		stat.put("ConnectSent", ConnectSent.get());
		stat.put("InitiateSent", InitiateSent.get());
		stat.put("AcceptSent", AcceptSent.get());
		stat.put("RejectSent", RejectSent.get());
		stat.put("TestSent", TestSent.get());
		stat.put("ReportSent", ReportSent.get());
		stat.put("ChangeRootSent", ChangeRootSent.get());
		stat.put("ConnectReceived", ConnectReceived.get());
		stat.put("InitiateReceived", InitiateReceived.get());
		stat.put("AcceptReceived", AcceptReceived.get());
		stat.put("RejectReceived", RejectReceived.get());
		stat.put("TestReceived", TestReceived.get());
		stat.put("ReportReceived", ReportReceived.get());
		stat.put("ChangeRootReceived", ChangeRootReceived.get());
		stat.put("merges", merges.get());
		stat.put("absorbs", absorbs.get());
		return stat;
	}
	
	
	private void presentStats(Map<String, Integer> stats){
		for (String k:stats.keySet()){
			System.out.println(k+": "+stats.get(k));
		}
		System.out.println("---------- MST ----------");
		for (Double w:mstEdges.keySet()){
			System.out.println("("+mstEdges.get(w).getFrom()+ " - "+mstEdges.get(w).getTo()+")");
		}
		System.out.println("---------- Core-Level Values ----------");
		for (int l:levels.keySet()){
			System.out.println("In level "+ l);
			for (Link e: levels.get(l)){
				System.out.println("("+e.getFrom()+ " - "+e.getTo()+")");
			}
		}
	}

	@Override
	public Map<Double, Link> getCores() throws RemoteException {
		// TODO Auto-generated method stub
		return cores;
	}

	@Override
	public Map<Double, Link> getMST() throws RemoteException {
		// TODO Auto-generated method stub
		return mstEdges;
	}

	@Override
	public Map<Integer, List<Link>> getLevels() throws RemoteException {
		// TODO Auto-generated method stub
		return levels;
	}
}
