import java.text.DecimalFormat;
import java.util.Random;


/**
 * This class stores the maze that we're going to try to solve. 
 * @author Will and Owen
 */
public class Maze {

	//store the possible actions as int constants
	//we're going to use these all over the place
	public static final int NORTH = 0;
	public static final int EAST = 1;
	public static final int SOUTH = 2;
	public static final int WEST = 3;

	/*keep a 4 layered array of States
	 * Layer 1: x value
	 * Layer 2: y value
	 * Layer 3: key (mapped from false/true to 0/1
	 * Layer 4: power level
	 */
	private State[][][][] states;

	//store the chance to loose the key in (0,2), the "Loose Key?" location
	private double keyLossProb;

	//Random number generator to handle probabilistic events
	private Random numGen;

	/*******************************************************************************
	 * CONSTRUCTORS
	 * Get passed various information about the maze, and create a new Maze and 
	 * all possible states that could exist in the maze.
	 *******************************************************************************/
	public Maze(int _happyReward, int _sadReward, int _stepCost, double _keyLossProb) {
		//store the variables
		keyLossProb = _keyLossProb;

		//set up the random number generator
		numGen = new Random();

		//set up the states
		initStates(_happyReward, _sadReward, _stepCost);
	}

	/**
	 * Set up the <states> array with all possible permutations of states
	 * @param happyReward the large positive reward
	 * @param sadReward the large negative reward
	 * @param stepCost the cost of taking a step
	 */
	private void initStates(int happyReward, int sadReward, int stepCost) {
		//states will be on a grid where 0 <= x <= 3, so 4 x values
		//and a grid where 0 <= y <= 2, so 3 y values
		//2 key values and 5 power levels

		//set up the states quad-array with the values above
		states = new State[4][3][2][5]; 

		//initialize each possible state
		for(int x = 0; x <=3; x++) { 
			for(int y = 0; y <=2; y++) { 
				for(int key = 0; key <= 1; key++) { 
					for(int power = 0; power <= 4; power++) {
						//the state has a key if <key> (the int) == 1
						//if we are at (3,2) and have a key, then set the happy reward and happy qvalues
						if(x==3 && y==2 && key == 1) 
							states[x][y][key][power] = new State(x, y, key==1, power, happyReward, true);
						//if we are at (2,2), then set the sad reward and sad qvalues
						else if(x==2 && y==2)
							states[x][y][key][power] = new State(x, y, key==1, power, sadReward, true);
						//if we have power level 0, and we're not in one of the other 2 terminal states,
						//set the qValues to the out of power qValues
						else if(power == 0)
							states[x][y][key][power] = new State(x, y, key==1, power, stepCost, true);
						//otherwise, just do things normally
						else 
							states[x][y][key][power] = new State(x, y, key==1, power, stepCost, false);
					}
				}
			}
		}
	}

	/**
	 * @param startState the state we're moving from
	 * @param action the action we want to take
	 * @return the state we end up in.
	 */
	public State move(State startState, int action) {
		//if we have a power level of 0, we shouldn't be able to move - return null
		if(startState.getPowerLevel() == 0) {
			System.out.println("ERROR: Trying to move with power level 0");
			return null;
		}

		//if we get passed an invalid action, flip out
		if(action != NORTH && action != SOUTH && action != EAST && action != WEST) {
			System.out.println("INVALID ACTION PASSED TO MOVE!");
			System.exit(64);
		}

		//figure out what action we actually take
		int actualAction = determineAcutalAction(action);

		//figure out the location we reach by taking the acutal action
		int newX=0, newY=0;
		switch(actualAction) {
		case NORTH: 
			newX = startState.getX();
			newY = startState.getY()+1;
			if(newY > 2) newY = 2;
			break;
		case SOUTH:
			newX = startState.getX();
			newY = startState.getY() -1;
			if(newY < 0) newY = 0;
			break;
		case EAST:
			newX = startState.getX() + 1;
			newY = startState.getY();
			if(newX > 3) newX = 3;
			break;
		case WEST:
			newX = startState.getX() - 1;
			newY = startState.getY();
			if(newX < 0) newX = 0;
			break;
		}

		//handle special locations
		if(newX == 0 && newY==0) 
			//we're at power level 4
			return states[0][0][startState.hasKey()?1:0][4];
		if(newX == 0 && newY == 2)
			//we now have a key
			return states[0][2][1][startState.getPowerLevel()-1];
		if(newX == 1 && newY == 1)
			//we now have power level 3
			return states[1][1][startState.hasKey()?1:0][3];
		if(newX == 2 && newY == 0 && startState.hasKey()) {
			//we might loose our key, if we have one
			if(numGen.nextDouble() < keyLossProb)
				return states[2][0][0][startState.getPowerLevel()-1];
		}
		if(newX == 3 && newY == 0)
			//we now have power level 3
			return states[3][0][startState.hasKey()?1:0][3];

		//if we reach one of the terminal locations, set the power level to 0 so that we guarantee that we don't move again
		if((newX == 2 && newY == 2) || (newX == 3 && newY == 2 && startState.hasKey())) {
			return states[newX][newY][startState.hasKey()?1:0][0];
		}

		//otherwise, just move and return the correct state
		return states[newX][newY][startState.hasKey()?1:0][startState.getPowerLevel()-1];
	}

	/**
	 * @param s the state in question
	 * @return the number of actions that can be taken from state <s>
	 */
	public int getNumberActionsFromState(State s) {
		//for this maze, there are 4 actions to take from all states
		return 4;
	}


	/**
	 * based on probabilstic chance, figure which action we actually take if we want to do the passed action
	 * relies on the values we assigned NORTH, SOUTH, EAST, and WEST
	 * @param action the action we wanted to take
	 * @return the action we actually should take, based on the 80%/10%/10% split
	 */
	private int determineAcutalAction(int action) {		
		//80% chance we go where we want to, 10% chance we go right, 10% chance we go left
		double randomNum = numGen.nextDouble();
		if(randomNum < 1)
			//we go where we want to
			return action;
		if(randomNum < .9)
			//actually go right
			return (action+1) % 4;
		else
			//actually go left
			return (action+3) % 4;
	}


	/**
	 * @return the start state of the maze
	 */
	public State getStartState() {
		return states[0][0][0][4];
	}

	/**
	 * @param action in int form
	 * @return a human-readable string representing that action
	 */
	public static String getStringAction(int action) {
		switch(action) {
		case NORTH:
			return "North";
		case SOUTH:
			return "South";
		case WEST:
			return "West";
		case EAST:
			return "East";
		}
		return "Unknown";
	}


	/**
	 * Print the overall policy dictated by the states, in order of location, then by if it has a key, then by the power level 
	 */
	public void printPolicy() {
		//use a DecimalFormat to limit the number of decimal places we print
		DecimalFormat df = new DecimalFormat("#,###.##");
		//just to store the state we're working with for easy reference
		State currentState;

		//go through all of the states by location first, then by if it has a key, and finally by the power level
		for(int x = 0; x <= 3; x++) {
			for (int y = 0; y <=2; y++) {
				for(int key = 0; key <= 1; key++) {
					for(int powerLevel = 0; powerLevel <= 4; powerLevel++) {
						currentState = states[x][y][key][powerLevel];
						//if the state has not been used, don't print it
						if(!currentState.hasBeenUsed()) {
							continue;
						}
						//otherwise, print it and it's best action
						System.out.println(currentState + "\t\t" + getStringAction(currentState.getBestAction()) + "\t with Q value " + df.format(currentState.getQValue(currentState.getBestAction())));
					}
				}
				//split up each location by a newline
				System.out.println("");
			}
		}
	}

}
