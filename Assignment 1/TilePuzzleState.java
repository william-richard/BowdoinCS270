/** Edited by Owen Callen and Will Richard */

/*

  Class that represents the state of an 8-puzzle 


  Stephen M. Majercik
  9/13/2007


 */


public class TilePuzzleState {

	// all we need is an array for the puzzle's current numbers
	private int[][] tileNumbers;
	// and to know where the blank is
	private int blankRow, blankCol;


	// create the array and copy the numbers into it
	public TilePuzzleState (int[][] inTileNumbers) {

		tileNumbers = new int[TilePuzzle.PUZZLE_DIMENSION][TilePuzzle.PUZZLE_DIMENSION];
		for (int r = 0 ; r < TilePuzzle.PUZZLE_DIMENSION ; r++) {
			for (int c = 0 ; c < TilePuzzle.PUZZLE_DIMENSION ; c++) {
				tileNumbers[r][c] = inTileNumbers[r][c];
				if (tileNumbers[r][c] == 0) {
					blankRow = r;
					blankCol = c;
				}
			}
		}
	}


	// getters and setters

	public int[][] getTileNumbers() {
		return tileNumbers;
	}

	public void setTileNumbers(int[][] inTileNumbers) {

		tileNumbers = new int[TilePuzzle.PUZZLE_DIMENSION][TilePuzzle.PUZZLE_DIMENSION];
		for (int r = 0 ; r < TilePuzzle.PUZZLE_DIMENSION ; r++) {
			for (int c = 0 ; c < TilePuzzle.PUZZLE_DIMENSION ; c++) {
				tileNumbers[r][c] = inTileNumbers[r][c];
				if (tileNumbers[r][c] == 0) {
					blankRow = r;
					blankCol = c;
				}
			}
		}
	}

	public int getNumber(int r, int c) {
		return tileNumbers[r][c];
	}


	public void setNumber(int r, int c, int val) {
		tileNumbers[r][c] = val;
		if (val == 0) {
			blankRow = r;
			blankCol = c;
		}
	}


	// checks for solved puzzle state
	public boolean isGoal() {

		boolean haveGoal = true;

		int numPositions = TilePuzzle.PUZZLE_DIMENSION * TilePuzzle.PUZZLE_DIMENSION - 1;
		for (int p = 1 ; p <= numPositions ; p++) {
			haveGoal = 
				haveGoal && 
				tileNumbers[(p-1)/TilePuzzle.PUZZLE_DIMENSION]
				            [(p-1)%TilePuzzle.PUZZLE_DIMENSION] == p;
		}

		return haveGoal;

	}


	// given a direction in which to move the blank, checks whether it's legal
	// and, if so, returns a new state reflecting that move; otherwise returns null
	public TilePuzzleState getSuccessor(int blankDirection) {

		int newBlankRow = blankRow;
		int newBlankCol = blankCol;

		if (blankDirection == TilePuzzle.UP)  {
			--newBlankRow;
		}
		else if (blankDirection == TilePuzzle.RIGHT)  {
			++newBlankCol;
		}
		else if (blankDirection == TilePuzzle.DOWN)  {
			++newBlankRow;
		}
		else if (blankDirection == TilePuzzle.LEFT)  {
			--newBlankCol;
		}
		else {
			System.out.println("Error in getSuccessor");
		}

		TilePuzzleState newState = null;
		if (newBlankRow >= 0 && newBlankRow < TilePuzzle.PUZZLE_DIMENSION &&
				newBlankCol >= 0 && newBlankCol < TilePuzzle.PUZZLE_DIMENSION) {
			// create the new state, copying the old state nubers to the new state
			newState = new TilePuzzleState(tileNumbers);
			// change the zero in the new state to the number that's taking its place
			newState.setNumber(blankRow, blankCol, tileNumbers[newBlankRow][newBlankCol]);
			// zero out the new blank position in the new state
			newState.setNumber(newBlankRow, newBlankCol, 0);
		}

		return newState;

	}

	/************************************************************************
	 * NEW CODE
	 ************************************************************************/
	public int intForm() {
		int sum = 0;
		int numPositions = TilePuzzle.PUZZLE_DIMENSION * TilePuzzle.PUZZLE_DIMENSION;
		for(int p = 0; p < numPositions; p++) {
			sum += tileNumbers[(p)/TilePuzzle.PUZZLE_DIMENSION]
			            [(p)%TilePuzzle.PUZZLE_DIMENSION] * Math.pow(10, numPositions-p-1);
		}
		return sum;
		
//		return (new Integer("" + getNumber(0,0) + getNumber(0,1) + getNumber(0,2) + getNumber(1,0) + getNumber(1,1) + getNumber(1,2) + getNumber(2,0) + getNumber(2,1) + getNumber(2,2))).intValue(); 
		
	}
	
	@Override
	public boolean equals(Object other){
		if(!(other instanceof TilePuzzleState)) {
			System.out.println("TilePuzzleState equals was passed a non-TilePuzzleState object");
			return false;
		} else {
			return this.intForm() == ((TilePuzzleState)other).intForm();
		}
	}
	
}