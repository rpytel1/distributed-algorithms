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
    
    public int getReceiver(int id) {
        if (id == this.to)
        	return this.from;
        return this.to;
    }

	@Override
	public int compareTo(Object arg0) {
		double otherWeight = ((Link) arg0).getWeight();
		if (weight < otherWeight)
			return -1;
		if (weight > otherWeight)
			return 1;
		return 0;
	}
}
