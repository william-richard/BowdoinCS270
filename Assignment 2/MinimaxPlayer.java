import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

// MinimaxPlayer.java


/** 
 *
 * @author  Sean Bridges
 * @version 1.0
 *
 * The MinimaxPlayer uses the minimax algorithm to determine what move it should 
 * make.  Looks ahead depth moves.  The number of moves will be on the order
 * of n^depth, where n is the number of moves possible, and depth is the number
 * of moves the engine is searching.  Because of this the minimax player periodically
 * polls the thread that calls getMove(..) to see if it was interrupted.  If the thread
 * is interrupted, the player returns null after it checks.  
 */
public class MinimaxPlayer extends DefaultPlayer
{

	//----------------------------------------------
	//instance variables

	//the number of levels minimax will look ahead
	private int depth = 1;
	private Player minPlayer;

	//----------------------------------------------
	//constructors

	/** Creates new MinimaxPlayer */
	public MinimaxPlayer(String name, int number, Player minPlayer) 
	{
		super(name, number);

		this.minPlayer = minPlayer;
	}

	//----------------------------------------------
	//instance methods

	/**
	 * Get the number of levels that the Minimax Player is currently looking 
	 * ahead.
	 */
	public int getDepth()
	{
		return depth;
	}

	/**
	 * Set the number of levels that the Minimax Player will look ahead 
	 * when getMove is next called
	 */
	public void setDepth(int anInt)
	{
		depth = anInt;
	}

	/** Passed a copy of the board, asked what move it would like to make.
	 *
	 * The MinimaxPlayer periodically polls the thread that makes this call to 
	 * see if it is interrupted.  If it is the player returns null.
	 *
	 * Looks ahead depth moves.
	 */
	public Move getMove(Board b)
	{
		MinimaxCalculator calc = new MinimaxCalculator(b,this,minPlayer);
		return calc.calculateMove(depth);
	}


}//end MinimaxPlayer



/**
 * The MinimaxCalculator does the actual work of finding the minimax move.
 * A new calculator should be created each time a move is to be made.
 * A calculator should only be used once.
 */
final class MinimaxCalculator
{

	//-------------------------------------------------------
	//instance variables

	//the number of moves we have tried
	private int moveCount = 0;
	private long startTime;

	private Player minPlayer;
	private Player maxPlayer;
	private Board board;

	private BufferedWriter dataFile;

	private final int MAX_POSSIBLE_STRENGTH;
	private final int MIN_POSSIBLE_STRENGTH;

	//Turn on and off Alpha Beta Pruning
	private static final boolean ALPHA_BETA_PRUNING = true; 

	//Allows the program to output data i.e. number of moves, level of computation, computation time
	private static final boolean OUTPUT_DATA_FILES = false;
	private static final String DATA_FILE_DIR = "/Users/Will/Documents/Schoolwork/Fall 09/AI/Homework/assign2-distributed/data_files/";


	//-------------------------------------------------------
	//constructors
	MinimaxCalculator(Board b, Player max, Player min)
	{
		board = b;
		maxPlayer = max;
		minPlayer = min;

		MAX_POSSIBLE_STRENGTH = board.getBoardStats().getMaxStrength();  // Integer.MAX_VALUE
		MIN_POSSIBLE_STRENGTH = board.getBoardStats().getMinStrength();  // Integer.MIN_VALUE

	}

	//-------------------------------------------------------
	//instance methods

	/** 
	 * Calculate the move to be made.
	 */
	public Move calculateMove(int depth)
	{

		//handle outputting running time data to files
		if(OUTPUT_DATA_FILES) {
			//make the data file location string - change depending on alpha beta pruning
			String dataFileLoc;
			if(ALPHA_BETA_PRUNING) {
				dataFileLoc = DATA_FILE_DIR + "ABP_" + depth + ".txt";
			} else {
				dataFileLoc = DATA_FILE_DIR + "MINMAX_" + depth + ".txt";
			}

			//open the data file
			try {
				dataFile = new BufferedWriter(new FileWriter(dataFileLoc, true));
			} catch (IOException e) {
				System.out.println("ERROR opening data file: " + e);
				System.exit(0);
			}
		}

		startTime = System.currentTimeMillis();

		// we have a problem, Houston...
		if(depth == 0)
		{
			System.out.println("Error, 0 depth in minumax player");
			Thread.dumpStack();
			return null;
		}

		Move[] moves = board.getPossibleMoves(maxPlayer);

		//keep track of the max value we have seen so far, the index of the move that lead to that max value and the curent value we're looking at.
		int maxIndex = -1;
		int maxValue = MIN_POSSIBLE_STRENGTH;
		int curValue;


		// explore each move in turn
		for(int curIndex = 0; curIndex < moves.length; curIndex++)
		{
			if(board.move(moves[curIndex]))    // move was legal (column was not full)
			{
				moveCount++;  // global variable

				//get the value of the current move, expanded as a min node because
				//we want to get the max value of all the moves on this level, which means we look at the min value of all the moves on the next level
				curValue = expandMinNode(depth, MIN_POSSIBLE_STRENGTH, MAX_POSSIBLE_STRENGTH);

				//if curValue is bigger than the maxValue, store it as the max value
				if(curValue > maxValue) {
					maxIndex = curIndex;
					maxValue = curValue;
				}

				board.undoLastMove();   // undo exploratory move
			}  //end if move made

			// if the thread has been interrupted, return immediately.
			if(Thread.currentThread().isInterrupted())
			{
				return null;
			}

		}//end for all moves

		long stopTime = System.currentTimeMillis();
		System.out.println("Number of moves tried = " + moveCount + 
				"  Time = " + (stopTime -  startTime) + " milliseconds");

		//Output the data for the move
		if(OUTPUT_DATA_FILES) {
			try {
				dataFile.write(depth + " " + moveCount + " " + (int)(stopTime - startTime) + "\n");
				dataFile.flush();
			} catch (IOException e) {
				System.out.println("ERROR writing to file: " + e );
			}
		}

		// maxIndex is the index of the move to be made
		return moves[maxIndex];

	}

	/**
	 * A max node returns the maximum score of its descendents.
	 */
	private int expandMaxNode(int depth, int alpha, int beta)
	{
		// test if the game is over (i.e. there are no moves to do)
		// or we have reached max depth (we don't want to do any more moves)
		if(board.isGameOver() || depth == 0)
		{
			return board.getBoardStats().getStrength(maxPlayer);
		}

		// if not
		Move[] moves = board.getPossibleMoves(maxPlayer);

		//keep track of the maxValue so far, as well as the current value for the move we're looking at
		int maxValue = MIN_POSSIBLE_STRENGTH;
		int curValue;

		// explore each move in turn
		for(int i = 0; i < moves.length; i++)
		{
			if(board.move(moves[i]))    // move was legal (column was not full)
			{
				moveCount++;  // global variable

				//expand the next move as a min node
				//since at this level we want to take the maximum of the minimums
				curValue = expandMinNode(depth - 1, alpha, beta);

				//if we have a new max, store it
				if(curValue > maxValue) {
					maxValue = curValue;
				}

				//if curValue > alpha, then we have found a better best move for the max player
				//so we should update alpha
				if(ALPHA_BETA_PRUNING && curValue > alpha) {
					alpha = curValue;
				}

				board.undoLastMove();   // undo exploratory move

				//if the curValue > beta, then we already have a better move for the min player, so it won't take this branch, so we want to prune the branch
				if(ALPHA_BETA_PRUNING && curValue > beta) {
					break;
				}

			}  //end if move made

		}//end for all moves

		return maxValue;

	}//end expandMaxNode




	/**
	 * A max node returns the minimum score of its descendents.
	 */
	private int expandMinNode(int depth, int alpha, int beta)
	{
		// test if the game is over (i.e. there are no moves to do)
		// or we have reached max depth (we don't want to do any more moves)
		if(board.isGameOver() || depth == 0)
		{
			return board.getBoardStats().getStrength(maxPlayer);
		}

		// if not
		Move[] moves = board.getPossibleMoves(minPlayer);

		//keep track of the minValue so far, as well as the current value for the move we're looking at
		int minValue = MAX_POSSIBLE_STRENGTH;
		int curValue;

		// explore each move in turn
		for(int i = 0; i < moves.length; i++)
		{
			if(board.move(moves[i]))    // move was legal (column was not full)
			{
				moveCount++;  // global variable

				//expand the next move as a max node
				//since at this level we want to take the minimums of the maximums
				curValue = expandMaxNode(depth - 1, alpha, beta);

				//if we have a new min, store it
				if(curValue < minValue) {
					minValue = curValue;
				}
				
				//if curValue < beta, then we have found a better best move for the min player
				//so we should update beta
				if(ALPHA_BETA_PRUNING && curValue < beta) {
					beta = curValue;
				}

				board.undoLastMove();   // undo exploratory move

				//if the curValue < alpha, then we already have a better move for the max player, so it won't take this branch, so we want to prune the branch
				if(ALPHA_BETA_PRUNING && curValue < alpha) {
					break;
				}

			}  //end if move made

		}//end for all moves

		return minValue;

	}//end expandMaxNode

}