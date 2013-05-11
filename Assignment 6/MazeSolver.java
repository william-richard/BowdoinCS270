import java.util.Random;

/**
 * @author Will & Owen
 * This class constructs or is passed a maze, and is able to run the requested number of trajectories on it
 * to try to find an optimal policy. 
 */
/**
 * @author Will
 *
 */
public class MazeSolver {

	//the maze we are trying to solve
	private Maze maze;
	//the learning rate for updating the q values
	private double learningRate;
	//the factor by which rewards in the future are discounted by
	private double discountFactor;
	
	//the temperature of the system.  High temperature (at early trajectories) means more randomness
	//low temp (at late trajectories) means less randomness
	private int temperature;
	//a number generator, used to figure out what action it should try next
	private Random numGen;
		
	//the current state it is at
	private State currentState;
	
	//the total number of trajectories that have been run over the lifetime of this MazeSolver.
	private int numTrajectoriesThatHaveBeenRun = 0;
	
	
	/*******************************************************************************
	 * CONSTRUCTORS
	 * Get passed various information about a maze (or the maze itself) and the solver, and creates a new MazeSolver
	 * If a number of iterations are passed in the constructor, run that many iterations after the construction is done
	 *******************************************************************************/
	public MazeSolver(Maze _maze, double _learningRate, double _discountFactor) {
		maze = _maze;
		learningRate = _learningRate;
		discountFactor = _discountFactor;
		currentState = null;
		numGen = new Random();
	}
	
	public MazeSolver(Maze _maze, double _learningRate, double _discountFactor, int _totalNumTrajtories) {
		this(_maze, _learningRate, _discountFactor);
		runTrajectories(_totalNumTrajtories);
	}
	
	public MazeSolver(int _happyReward, int _sadReward, int _stepCost, double _keyLossProb, double _learningRate, double _discountFactor) {
		this(new Maze(_happyReward, _sadReward, _stepCost, _keyLossProb), _learningRate, _discountFactor);
	}
	
	public MazeSolver(int _happyReward, int _sadReward, int _stepCost, double _keyLossProb, double _learningRate, double _discountFactor, int _totalNumTrajectories) {
		this(new Maze(_happyReward, _sadReward, _stepCost, _keyLossProb), _learningRate, _discountFactor, _totalNumTrajectories);
	}
	
	
	/**
	 * Run the passed number of trajectories on the maze
	 * Adjusts the temperature as the trajectories are run
	 * @param numTrajectories the number of trajectories to run
	 */
	public void runTrajectories(int numTrajectories) {
		//set initial temp to 3/4 of the total number of trajectories
		temperature = (numTrajectories * 3/4) + 1;
		
		//run all the trajectories, adjusting the temp
		for(int i = 0; i < numTrajectories; i++) {
			runOneTrajectory();
			numTrajectoriesThatHaveBeenRun++;
			
			//adjust the temp
			if(temperature > 1) temperature--;
			//just to make sure we don't go too low - shouldn't ever happen but we want to avoid divide by 0
			if(temperature < 1) temperature = 1;
		}
	}
	
	/**
	 * Run one trajectory on the maze.
	 * A trajectory is from the start state to a terminal state 
	 */
	private void runOneTrajectory() {
		//start by making our current state the maze's start state
		currentState = maze.getStartState();
		
		//then, until we hit a terminal state, decide where to move, tell the maze to move, update Q values, and move onto the next state
		while(! currentState.isTerminalState()) {
			//figure out what action we will take next
			int nextAction = determineNextAction();
			//tell the maze to move
			State nextState = maze.move(currentState, nextAction);
			//update the Q value for the current state
			currentState.updateQValue(nextState, nextAction, learningRate, discountFactor);
			//set the current state to the next state
			currentState = nextState;
		}
	}
	
	/**
	 * Determine the next action to take by using the Boltzman Exporation with temperature.
	 * just as a reminder, that formula is "e^(Q(s,a)/T)/sum(e^(Q(s,a)/T))
	 * where Q(s,a) is the q value of state s taking action a
	 * T is the temperature
	 * sum is the summation of all of these values over all actions
	 * and e is the constant e
	 * @return the action to take next
	 */
	private int determineNextAction() {
		//make an array to hold all of our values we are working with.  
		//The meaning of what this array holds will change as we compute
		double[] numbers = new double[maze.getNumberActionsFromState(currentState)];
		
		//first, determine all the numerators for all the actions
		for(int a = 0; a < numbers.length; a++) {
			numbers[a] = Math.pow(Math.E, currentState.getQValue(a)/temperature);
		}
		
		//add each value in the array to the previous value,
		//so that numbers[a] is the sum of all the values from numbers[0] to numbers[a-1] plus numbers[a]
		for(int a = 1; a < numbers.length; a++) {
			numbers[a] = numbers[a] + numbers[a-1];
		}
		
		//now divide each number by the maximum number to get the probability cutoff for each action
		//the maximum value is the last value in the numbers array
		for(int a = 0; a < numbers.length; a++) {
			numbers[a] = numbers[a] / numbers[numbers.length-1];
		}
		
		//all the values in <numbers> should be between 0 and 1, so get a random double and see what cutoff range it falls in
		double randomNum = numGen.nextDouble();
		
		for(int a = 0; a < numbers.length; a++) {
			if(randomNum < numbers[a])
				return a;
		}
		
		//we shouldn't get here, but in case we do, just return 0 since that should always be a valid action
		return 0;
	}

	/**
	 * Prints out statistics about the maze solver, and the final policy we get.
	 * @param args the args array from the main class
	 */
	public void printFinalInformation(String[] args) {
		//print out the stats
		System.out.println("Learning Rate: " + learningRate);
		System.out.println("Discount Factor: " + discountFactor);
		System.out.println("Large positive reward: " + args[2]);
		System.out.println("Large negative reward: " + args[3]);
		System.out.println("Step cost: " + args[4]);
		System.out.println("Key loss probability: " + args[5]);
		System.out.println("Number of trajectories that were run: " + numTrajectoriesThatHaveBeenRun);
		
		System.out.println("\n********************************\n");
		
		maze.printPolicy();
	}
	
	/**
	 * @param args should have length 7.  Each index corresponds to the following
	 * args[0] = learning rate
	 * args[1] = discount factor
	 * args[2] = the size of the large positive reward
	 * args[3] = the size of the large negative reward
	 * args[4] = the step cost
	 * args[5] = the key loss probability
	 * args[6] = the number of trajectories to run 
	 */
	public static void main(String[] args) {
		if(args.length != 7) {
			//Yell at user
			System.out.println("Incorrect number of arguments");
			System.out.println("Correct usage: java MazeSolver <learning rate> <discount factor> <the size of the large positive reward> <the size of the large negative reward> <the step cost> <the key loss probability> <the number of trajectories to run>");
			System.out.println("Try again, please.");
			System.exit(0);
		}
		
		//make a solver, and run the specified number of trajectories
		MazeSolver solver = new MazeSolver(Integer.valueOf(args[2]).intValue(), Integer.valueOf(args[3]).intValue(), Integer.valueOf(args[4]).intValue(), Double.valueOf(args[5]).doubleValue(), Double.valueOf(args[0]).doubleValue(), Double.valueOf(args[1]).doubleValue(), Integer.valueOf(args[6]).intValue());
		
		//print out stats
		solver.printFinalInformation(args);
	}
}
