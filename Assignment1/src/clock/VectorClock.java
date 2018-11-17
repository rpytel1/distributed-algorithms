package clock;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class VectorClock implements Serializable {

    List<Integer> timeVector;
    int processID;
    private static final long serialVersionUID = 1L;

    public VectorClock() {

    }

    public VectorClock(int id, int n) {
        timeVector = new ArrayList<>(Collections.nCopies(n, 0));
        processID = id;
    }

    public VectorClock(int id, List<Integer> vt) {
        timeVector = new ArrayList<>(vt);
        processID = id;
    }

    public List<Integer> getTimeVector() {
        return this.timeVector;
    }

    public void setTimeVector(List<Integer> timeVector) {
        this.timeVector = timeVector;
    }

    public int getProcessID() {
        return processID;
    }

    public void setProcessID(int processID) {
        this.processID = processID;
    }

    public int size() {
        return this.timeVector.size();
    }

    public void incTimeVector(int i) {
        this.timeVector.set(i, this.timeVector.get(i) + 1);
    }

    public List<Integer> merge(VectorClock vc2) {
        if (vc2 == null) {
            return this.timeVector;
        }

        for (int i = 0; i < this.timeVector.size(); i++) {
            Integer myTime = this.timeVector.get(i);
            Integer receivedTime = vc2.getTimeVector().get(i);

            if (myTime < receivedTime) {
                this.timeVector.add(i, receivedTime);
                this.timeVector.remove(i + 1);
            }
        }
        return this.timeVector;
    }


    public boolean smallerOrEqualThan(VectorClock vc2) {
        if (vc2 == null) {
            return true;
        }

        boolean smallerOrEqual = true;
        for (int i = 0; i < this.timeVector.size(); i++) {
            Integer myTime = this.timeVector.get(i);
            Integer receivedTime = vc2.getTimeVector().get(i);

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
        for (int i = 0; i < this.timeVector.size(); i++) {
            str = str + "Time: " + this.timeVector.get(i) + "\t Process: " + i + "\n";
        }
        return str;
    }


}
