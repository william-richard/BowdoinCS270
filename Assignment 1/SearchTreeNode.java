/** Edited by Owen Callen and Will Richard */

/*

  Class that represents a node in the puzzle search tree


  Stephen M. Majercik
  9/13/2007


 */


public class SearchTreeNode {

	private TilePuzzleState state;
	private SearchTreeNode parent;
	private int action;           // what action led from the parent to this node
//	private int pathCost;
	private int depth;            // in search tree
	private SearchTreeNode solutionChild;
	
	//stores the heuristic value, if it has been calculated
	//negative unless it has been set
	private int heuristicValue = -1;
	@SuppressWarnings("unused")
	private final static int HEURISTIC_VALUE_OF_BECOMING_SKYNET = 999999999;


	public SearchTreeNode (TilePuzzleState state, SearchTreeNode parent, int action, int depth) {

		this.state = state;
		this.parent = parent;
		this.action = action;
		this.depth = depth;

	}


	// getters and setters

	public TilePuzzleState getState() {
		return state;
	}

	public void setState(TilePuzzleState state) {
		this.state = state;
	}

	public SearchTreeNode getParent() {
		return parent;
	}

	public void setParent(SearchTreeNode parent) {
		this.parent = parent;
	}

	public int getAction() {
		return action;
	}

	public void setAction(int action) {
		this.action = action;
	}

	//path cost is the depth + the heuristic value
	public int getPathCost() {
		return getDepth() + getHeuristicValue();
	}

	public int getDepth() {
		return depth;
	}

	public void setDepth(int depth) {
		this.depth = depth;
	}

	public SearchTreeNode getSolutionChild() {
		return solutionChild;
	}

	public void setSolutionChild (SearchTreeNode n) {
		solutionChild = n;
	}
	
	
	/************************************************************************
	 * NEW CODE
	 ************************************************************************/
	@Override
	public int hashCode() {
		return state.intForm();
	}

	@Override
	public boolean equals(Object other) {
		if(!(other instanceof SearchTreeNode)) {
			System.out.println("SearchTreeNode equals was passed a non-SearchTreeNode object");
			return false;
		}
		else {
			return state.equals(((SearchTreeNode)other).getState());
		}
	}
	
	/** return this node's heuristic value
	 * calculate it if it has not been set
	 * @return the heuristic value for this node.
	 */
	public int getHeuristicValue() {
		//if the heuristic value has not been set, set it
		if(heuristicValue == -1) {
			//calculate the heuristic value
//			heuristicValue = Math.max(manhattanHeuristic(), GashningHeuristic());
			heuristicValue = manhattanHeuristic();
			//			heuristicValue = GashningHeuristic();
			
		}
		//return the value
		return heuristicValue;	
	}
	
	
	private int manhattanHeuristic() {
		//the sum of the offsets of all tiles.
		int totNumMoves = 0;
		
		//the current value we're looking at
		int curValue;
		//the row and col where we WANT the current tile to be
		int goalRow, goalCol;
		
		//go through each tile in the puzzle
		for(int r = 0; r < TilePuzzle.PUZZLE_DIMENSION; r++) {
			for(int c = 0; c < TilePuzzle.PUZZLE_DIMENSION; c++) {
				//figure out what value is at that tile
				curValue = getState().getNumber(r, c);
				//figure where that tile should be
				goalRow = (curValue-1)/TilePuzzle.PUZZLE_DIMENSION;
				goalCol = (curValue-1)%TilePuzzle.PUZZLE_DIMENSION;
				
				//add the offset to the total
//				totNumMoves += Math.abs(r - goalRow) + Math.abs(c - goalCol);
				totNumMoves += ourAbsoluteValue(r - goalRow);
				totNumMoves += ourAbsoluteValue(c - goalCol);
			}
		}
		
		return totNumMoves;
	}
	
	private int ourAbsoluteValue(int value) {
		if(value > 0) return value;
		return value * -1;
	}
	
	@SuppressWarnings("unused")
	private int GashningHeuristic() {
		//get a copy of the state array
		int[] tiles = new int[TilePuzzle.PUZZLE_DIMENSION * TilePuzzle.PUZZLE_DIMENSION];
		int index = 0;
		int blankIndex = 0;
		for(int row = 0; row < TilePuzzle.PUZZLE_DIMENSION; row++) {
			for(int col = 0; col < TilePuzzle.PUZZLE_DIMENSION; col++) {
				tiles[index] = getState().getTileNumbers()[row][col];
				//record where the blank is
				if(tiles[index] == 0)
					blankIndex = index;
				index++;
			}
		}
		
		//the total number of moves we need to do to solve the puzzle using this heuristic
		int totNumMoves = 0;
		
		//will record if there are any out of place tiles if we need to switch something with the blank if the blank is already in it's final position
		boolean tileOutOfPlace;

		//repeat the loop tiles.length-1 times so we have placed all tiles.length-1 tiles
		for(int j = 0; j < tiles.length-1; j++) {
			//if the blank is where it should be, switch it with the first out-of-place tile and switch it with the blank
			if(blankIndex == tiles.length-1) {
				//assume there are no tiles out of place, until we find one that is misplaced
				tileOutOfPlace = false;
				//the blank is where we want it - make sure everything else is too, and if there is something that needs to be moved, move it
				for(index = 0; index < tiles.length-1; index++) {
					if(tiles[index] != index-1) {
						//the tile is not where we want it to be
						tileOutOfPlace = true;
						//switch the out of place tile with the blank
						//this is really bad form, but it will work and we're not making any assumptions that haven't already been made
						tiles[blankIndex] = tiles[index];
						tiles[index] = 0;
						blankIndex = index;
						totNumMoves++;
						//break so that we don't keep on switching things
						break;
					}
				}
				//we did not find any misplaced tiles, so we're done
				if(!tileOutOfPlace)
					return totNumMoves;
			}
			
			//figure out what number belongs where the blank is now
			int numWeWant = blankIndex + 1;
			int numWeWantIndex = 0;
			//find the index of the tile we want to switch with the blank
			for(index = 0; index < tiles.length; index++) {
				if(tiles[index] == numWeWant) {
					numWeWantIndex = index;
					break;
				}
			}
			//switch the blank with the number we want
			//again, we're making a bad assumption, but it's already been made
			tiles[blankIndex] = tiles[numWeWantIndex];
			tiles[numWeWantIndex] = 0;
			blankIndex = numWeWantIndex;
			totNumMoves++;
		}
		return totNumMoves;
	}
}