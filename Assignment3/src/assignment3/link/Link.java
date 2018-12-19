package assignment3.link;

import assignment3.node.Node;

import java.io.Serializable;

public class Link implements Serializable, Comparable{
    private LinkState state;
    private int from;
    private int to;
    private double weight;
    private int delay;

    public Link() {
    }
    
    public Link(LinkState s, int id1, int id2, double w, int d) {
    	state = s;
    	from = id1;
    	to = id2;
    	weight = w;
    	delay = d;
    }
    public Link(Link link){
        state = link.getState();
        from = link.getFrom();
        to = link.getTo();
        weight = link.getWeight();
        delay = link.getDelay();
    }

    public LinkState getState() {
        return state;
    }

    public void setState(LinkState state) {
        this.state = state;
    }

    public int getFrom() {
        return from;
    }

    public void setFrom(int from) {
        this.from = from;
    }

    public int getTo() {
        return to;
    }

    public void setTo(int to) {
        this.to = to;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public int getDelay() {
        return delay;
    }

    public void setDelay(int delay) {
        this.delay = delay;
    }
    
    public synchronized int getReceiver(int id) {
        if (id == this.to)
        	return this.from;
        return this.to;
    }

	@Override
	public int compareTo(Object arg0) {
        if(arg0 == null){
            return -1;
        }
		double otherWeight = ((Link) arg0).getWeight();
		if (weight < otherWeight)
			return -1;
		if (weight > otherWeight)
			return 1;
		return 0;
	}
}
