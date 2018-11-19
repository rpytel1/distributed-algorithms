package message;

import java.util.Comparator;

public class MessageComparator implements Comparator<Message>{
	public int compare(Message m1, Message m2) {
		if(m1.getTimestamp().smallerOrEqualThan(m2.getTimestamp())){
			return -1;
		}
		else{
			return 1;
		}
	};
}
