/** Edited by Owen Callen and Will Richard */

/*

  Class that implements the Comparator interface class for use
  as the comparator in a least-first PriorityQueue.


  Stephen M. Majercik
  9/13/2007


 */


import java.util.*;


public class ComparatorPQLeastFirst implements Comparator<SearchTreeNode> {

	public final int compare(SearchTreeNode x, SearchTreeNode y) {

		// -1 indicates that x should be closer to the head than y, 
		//    i.e. *lower* path cost nodes should be closer to
		//    the head
		// +1 indicates that y should be closer to the head than x
		//  0 indicates that they are of equal priority

		if (x.getPathCost() < y.getPathCost())
			return -1;
		else if (x.getPathCost() > y.getPathCost())
			return 1;
		else
			return 0;
	}
};

