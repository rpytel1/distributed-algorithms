package message;

import java.util.Comparator;

public class MessageComparator implements Comparator<Message>{
	public int compare(Message o1, Message o2) {
		if(o1.timestamp.smallerOrEqualThan(o2.timestamp)){
			return -1;
		}
		else{
			return 1;
		}
	};
}
