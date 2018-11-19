package clock;

import java.io.Serializable;
import java.util.Arrays;

public class VectorClock implements Serializable{

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	int[] timeVector;
    int processID;

    public VectorClock(int id, int n) {
    	this.timeVector = new int[n];
        for (int i=0; i<n; i++) this.timeVector[i] = 0;
        this.processID = id;
    }

    public VectorClock(int id, int[] vt) {
        this.timeVector = vt;
        this.processID = id;
    }
    
    public int[] clone() {
        return Arrays.copyOf(this.timeVector, this.timeVector.length);
    }

    public int[] getVector() {
        return this.timeVector;
    }

    public int getID() {
        return this.processID;
    }

    public int size() {
        return this.timeVector.length;
    }

    public void incTimeVector(int i) {
        this.timeVector[i]+=1;
    }

    public int[] merge(VectorClock vc2) {
        if (vc2 == null) {
            return this.timeVector;
        }
        for (int i = 0; i < this.timeVector.length; i++) {
            int myTime = this.timeVector[i];
            int receivedTime = vc2.getVector()[i];
            if (myTime < receivedTime) this.timeVector[i] = receivedTime;
        }
        return this.timeVector;
    }


    public boolean smallerOrEqualThan(VectorClock vc2) {
        if (vc2 == null) {
            return true;
        }

        boolean smallerOrEqual = true;
        for (int i = 0; i < this.timeVector.length; i++) {
            int myTime = this.timeVector[i];
            int receivedTime = vc2.getVector()[i];
            if (myTime > receivedTime) {
                smallerOrEqual = false;
            }
            if (!smallerOrEqual) break;
        }
        return smallerOrEqual;

    }
    
    public boolean smallerThan(VectorClock vc2) {
        if (vc2 == null) {
            return true;
        }

        boolean smaller = true;
        for (int i = 0; i < this.timeVector.length; i++) {
            int myTime = this.timeVector[i];
            int receivedTime = vc2.getVector()[i];
            if (myTime >= receivedTime) {
                smaller = false;
            }
            if (!smaller) break;
        }
        return smaller;

    }

    @Override
    public String toString() {
        // TODO Auto-generated method stub
        String str = "Time vector of process " + this.processID + ":\n";
        for (int i = 0; i < this.timeVector.length; i++) {
            str = str + "Time: " + this.timeVector[i] + "\t Process: " + i + "\n";
        }
        return str;
    }
}
