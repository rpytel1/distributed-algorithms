package clock;

import java.io.Serializable;
import java.util.Arrays;

/* Class implementing the vector clock used in the Schiper-Eggli-Sandoz algorithm
 */

public class VectorClock implements Serializable{

	private static final long serialVersionUID = 1L;
	int[] timeVector; // the timestamp for all processes
    int processID; // the process id with which the current vector clock is associated

    /* Constructor of the class that sets all the initial values of the timestamp to zero
     */
    public VectorClock(int id, int n) {
    	this.timeVector = new int[n];
        for (int i=0; i<n; i++) this.timeVector[i] = 0;
        this.processID = id;
    }

    /* Constructor of the class that sets the timestamp to the value of vt
     */
    public VectorClock(int id, int[] vt) {
        this.timeVector = vt;
        this.processID = id;
    }
    
    /* Method that returns a copy of the timestamp of the current vector clock
     */
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

    /* Method that increases the value of the time of process i in the time vector
     */
    public void incTimeVector(int i) {
        this.timeVector[i]+=1;
    }

    /* Method that merges the current vector clock with vc2
     * in respect to their max values
     */
    public int[] merge(VectorClock vc2) {
        if (vc2 == null) {
            return this.timeVector;
        }
        for (int i = 0; i < this.timeVector.length; i++) {
            int myTime = this.timeVector[i];
            int receivedTime = vc2.getVector()[i];
            // if the time of vector clock vc2 for process i is greater than the time 
            // stored in the current vector clock then the value is updated
            if (myTime < receivedTime) this.timeVector[i] = receivedTime;
        }
        return this.timeVector;
    }

    /* Method that checks if the current vector clock is smaller or equal to the
     * vector clock vc2, meaning that vc1<=vc2 <=> vc1[i]<=vc2[i] for i = 1...k
     */
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
