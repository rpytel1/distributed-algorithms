package assignment3.link;

import java.util.Comparator;

public class LinkComparator implements Comparator<Link> {
    @Override
    public int compare(Link o1, Link o2) {
        return o1.compareTo(o2);
    }

}
