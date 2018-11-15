package clock;

import java.util.ArrayList;
import java.util.Collections;

public class VectorClock {
	
	ArrayList<Integer> timeVector;
	int processID;
	
	public VectorClock(int n, int id){
		timeVector = new ArrayList<Integer>(Collections.nCopies(n, 0));
		processID = id;
	}
	
	public VectorClock(int id, ArrayList<Integer> vt){
		timeVector = new ArrayList<Integer>(vt);
		processID = id;
	}
	
	public ArrayList<Integer> getVector(){
		return this.timeVector;
	}
		
	public int getID(){
		return this.processID;
	}
	
	public int size(){
		return this.timeVector.size();
	}
	
	public void incTimeVector(int i){
		this.timeVector.set(i, this.timeVector.get(i)+1);
	}
	
	public ArrayList<Integer> merge(VectorClock vc2){
		if(vc2==null){
			return this.timeVector;
		}
		else{
			for (int i = 0; i < this.timeVector.size(); i++) {
				if (this.timeVector.get(i)<vc2.getVector().get(i)){
					this.timeVector.add(i, vc2.getVector().get(i));
					this.timeVector.remove(i+1);	
				}
			}
			return this.timeVector;
		}
	}
	
	public boolean smallerOrEqualThan(VectorClock vc2){
		if(vc2==null){
			return true;
		}
		else{
			boolean flag = true;
			for (int i = 0; i < this.timeVector.size(); i++) {
				if (this.timeVector.get(i)>vc2.getVector().get(i)){
					flag = false;
				}
				if (!flag) break;
			}
			return flag;
		}
	}
	
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		String str = "Time vector of process " + this.processID +":\n";
		for (int i = 0; i < this.timeVector.size(); i++) {
			str = str + "Time: " + this.timeVector.get(i) + "\t Process: " + i + "\n";
		}
		return str;
	}
}
