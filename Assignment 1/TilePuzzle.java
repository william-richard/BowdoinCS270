/** Edited by Owen Callen and Will Richard */

    /*

     Class that implements graphics and some basic functions for the 8-puzzle.

     NOTE:  A "position" number refers to the following positions on the puzzle grid:

        |-----|-----|-----|
        |  1  |  2  |  3  |
        |-----|-----|-----|
        |  4  |  5  |  6  |
        |-----|-----|-----|
        |  7  |  8  |  9  |
        |-----|-----|-----|


     Stephen M. Majercik

     9/13/2007

     */


    import objectdraw.*;
    import java.awt.*;
    import java.io.BufferedWriter;
    import java.io.FileWriter;
    import java.io.IOException;
    import java.util.*;


    public class TilePuzzle extends ActiveObject {

           /**********************************
            * BEHAVIOR VARIABLES
            **********************************/

           //public vars that allow the user to control which searches are done
           public static final boolean AStarRun = false;
           public static final boolean BFSRun = true;
           public static final boolean IterativeDeepeningRun = true;

           public static final boolean ShowSolutions = false;

           //public var that allow the user to control the number of puzzles solved
           public static final int NumOfPuzzles = 50;

           //DEBUG BOOLEAN
           public static final boolean PRINT_MESSAGES = true;

           //Allows you to turn on and off writing to files
           public static final boolean WRITE_TO_STAT_FILES = true;

           //output file names.
           private static final String OUTPUT_FILE_DIR = "/Users/Will/Documents/Schoolwork/Fall 09/AI/Homework/assign1-distributed/data_files/";
           private static final String BFS_OUTPUT_FILE_LOC = OUTPUT_FILE_DIR + "bfs_hash_output.txt";
           private static final String IDF_OUTPUT_FILE_LOC = OUTPUT_FILE_DIR + "idf_hash_output.txt";
           private static final String ASTAR_OUTPUT_FILE_LOC = OUTPUT_FILE_DIR + "astar_hash_output.txt";


           // the number of backward moves from the solved state to take in order
           // to generate a random, solvable puzzle
           private static final int NUMBER_BACKWARD_MOVES = 150;


           // time delay and move increment for sliding tiles
           private static final int MOVE_DELAY = 15;
           private static final int MOVE_PIXELS = 2;

           // puzzle dimensions
           private static final int PUZZLE_UPPER_LEFT_X = 50;
           private static final int PUZZLE_UPPER_LEFT_Y = 30;
           private static final int PUZZLE_SIDE_LENGTH = 300;
           public static final int PUZZLE_DIMENSION = 3;
           private static final int NUM_PUZZLE_BORDER_PIXELS = 8;
           private static final int TILE_SIZE = PUZZLE_SIDE_LENGTH/PUZZLE_DIMENSION;

           // pixel offset for number from upper-left corner of tile
           private static final int NUMBER_X_OFFSET = 28;
           private static final int NUMBER_Y_OFFSET = 12;
           private static final int NUMBER_FONT_SIZE = 64;

           // arrays of pieces of the puzzle
           private FilledRect[][] tiles;
           private FramedRect[][] tileFrames;
           private double[][] tileUpperLeftX;
           private double[][] tileUpperLeftY;
           private int[][] tileNumbers;
           private int[][] initialTileNumbers;
           private Text[][] tileNumberText;

           private static final Color tileColor = Color.GREEN;

           private int blankCurrentRow;
           private int blankInitialRow;
           private int blankCurrentCol;
           private int blankInitialCol;

           // Using the position p of the blank as an index into this array yields an
           // array of legal positions the blank can be moved to from p.
           private static final int[][] legalBlankMoves = {{ },             // there is no position 0
                   { 2, 4 },        // position 1
                   { 1, 3, 5 },     // position 2
                   { 2, 6 },        // position 3
                   { 1, 5, 7 },     // position 4
                   { 2, 4, 6, 8 },  // position 5
                   { 3, 5, 9 },     // position 6
                   { 4, 8, },       // position 7
                   { 5, 7, 9 },     // position 8
                   { 6, 8 } };      // position 9

           // Use the least common multiple of the number of possible legal moves for each
           // position so that we can use this random number generator for any position by
           // MODing by the number of legal moves at that position and still get equal
           // probabilities for each possible move  :-)
           private final int MAX_RANDOM_NUM = 12;
           private RandomIntGenerator randomBlankMoveIndex = new RandomIntGenerator(1, MAX_RANDOM_NUM);

           // possible moves of the blank tile;
           // NO_ACTION is only used for the node containing the initial state
           public static final int NO_ACTION = 0;
           public static final int UP = 1;
           public static final int RIGHT = 2;
           public static final int DOWN = 3;
           public static final int LEFT = 4;

           // canvas to draw on
           private DrawingCanvas canvas;

           // declarations for a least-first priority queue and a greatest-first
           // priority queue.  these can be used to hold the nodes in the "fringe"
           // of a search.  it's up to you to figure out how to use them.

           private ComparatorPQLeastFirst costComparatorLeastFirst = new ComparatorPQLeastFirst();
           // initial capacity is 10000; queue grows automatically, when necessary
           private PriorityQueue<SearchTreeNode> nodePQLeastFirst =
                   new PriorityQueue<SearchTreeNode>(100000, costComparatorLeastFirst);

           // initial capacity is 10000; queue grows automatically, when necessary
           //      private PriorityQueue<SearchTreeNode> nodePQGreatestFirst =
           //              new PriorityQueue<SearchTreeNode>(10000, costComparatorGreatestFirst);
           //
           //      // can be used to hold the states in the "closed" list
           //      // (if those states are Strings; see the stateString method)
           //      private LinkedList<String> closed = new LinkedList<String>();

           //Hash map storing nodes.  Must be reset before starting new seaches.
           private HashMap<SearchTreeNode, SearchTreeNode> hash = new HashMap<SearchTreeNode, SearchTreeNode>(100000);

           private static final boolean USE_HASH = true;

           //a linked list that will be used as a queue in BFS and a stack in itteritive deepening search.
           private LinkedList<SearchTreeNode> linkedList = new LinkedList<SearchTreeNode>();

           private long numNodesCreated;
           private int maxStructSize;
           private int curPuzzleSolutionLength;

           //      private BufferedWriter bfsOut;
           //      private BufferedWriter idfOut;
           //      private BufferedWriter astarOut;


           public TilePuzzle (DrawingCanvas canvas) {

                   this.canvas = canvas;

                   Text holdOn = new Text("Working.....", 50, 50, canvas);
                   holdOn.setFontSize(40);

                   // all we need at this point is a barebones representation of the puzzle, i.e.
                   // an array of the tile numbers; we also need an array in which to save the
                   // initial numbers so we can reset the puzzle when necessary
                   tileNumbers = new int[PUZZLE_DIMENSION][PUZZLE_DIMENSION];
                   initialTileNumbers = new int[PUZZLE_DIMENSION][PUZZLE_DIMENSION];

                   // start with the puzzle in solved order; note that the blank is 0
                   int tileNumber = 1;
                   for (int r = 0 ; r < PUZZLE_DIMENSION ; r++) {
                           for (int c = 0 ; c < PUZZLE_DIMENSION ; c++) {
                                   tileNumbers[r][c] = tileNumber % (PUZZLE_DIMENSION * PUZZLE_DIMENSION);
                                   ++tileNumber;
                           }
                   }
                   // need to keep track of where the blank is
                   blankCurrentRow = PUZZLE_DIMENSION - 1;
                   blankCurrentCol = PUZZLE_DIMENSION - 1;

                   // randomize it, but keep it solvable
                   generateRandomPuzzle(NUMBER_BACKWARD_MOVES);

                   // save the initial configuration and blank location
                   for (int r = 0 ; r < PUZZLE_DIMENSION ; r++) {
                           for (int c = 0 ; c < PUZZLE_DIMENSION ; c++) {
                                   initialTileNumbers[r][c] = tileNumbers[r][c];
                           }
                   }
                   blankInitialRow = blankCurrentRow;
                   blankInitialCol = blankCurrentCol;

                   // execute the "run" method
                   start();

           }





           // the method that's executed right after the constructor is finished
           public void run() {


                   /* *********************************************************************
                    *********************************************************************

              This is where all your search methods should be called from.

              You can run searches multiple times or do different searches on
              the same puzzle by using the resetToPreviousPuzzle method.

              You can change the puzzle to solve by using the resetToNewPuzzle
              method.


                    *********************************************************************
                    ********************************************************************* */

                   // sample code to run BFS five times and show the solutions graphically;
                   // of course, you need to write the breadthFirstSearch method

                   SearchTreeNode solutionPathRootNode;

                   long startTime;

                   //open up output files
                   BufferedWriter bfsOut;
                   BufferedWriter idfOut;
                   BufferedWriter astarOut;
                   if(WRITE_TO_STAT_FILES) {
                           try {
                                   bfsOut = new BufferedWriter(new FileWriter(BFS_OUTPUT_FILE_LOC, true));
                                   idfOut = new BufferedWriter(new FileWriter(IDF_OUTPUT_FILE_LOC, true));
                                   astarOut = new BufferedWriter(new FileWriter(ASTAR_OUTPUT_FILE_LOC, true));
                           } catch (IOException e ) {
                                   System.out.println("ERROR opening ouput file: " + e);
                                   return;
                           }
                   }

                   for (int i = 1 ; i <= NumOfPuzzles; i++) {

                           // search method should return the root node in the solution path
                           // to send to showSolution, which shows the solution graphically
                           // NOTE:  showSolution assumes that the solutionChild fields in
                           // the nodes comprising the solution path have been set so that
                           // they can be followed from the root to the goal; see the line
                           // of code marked by lines of asterisks in that method

                           //                      if(PRINT_MESSAGES) System.out.println("Trying to solve: " + new TilePuzzleState(tileNumbers).intForm());

                           solutionPathRootNode = null;
                           if(AStarRun){
                                   if(PRINT_MESSAGES) System.out.println("*********************************\nSTARTING A*\n*******************************");
                                   numNodesCreated = 0;
                                   maxStructSize = 0;
                                   curPuzzleSolutionLength = 0;

                                   startTime = System.currentTimeMillis();
                                   solutionPathRootNode = AStar();
                                   if(WRITE_TO_STAT_FILES) {
                                           String outputMessage = "" + curPuzzleSolutionLength + " " + numNodesCreated + " " + maxStructSize + " " + (System.currentTimeMillis() - startTime) + "\n";
                                           System.out.println("writing '" + outputMessage +"' to file");
                                           try {
                                                   astarOut.write(outputMessage);
                                                   astarOut.flush();
                                           } catch (IOException e ) {
                                                   System.out.println("ERROR writing to file: " + e);
                                                   return;
                                           }
                                   }

                                   if(PRINT_MESSAGES) System.out.println("Search took " + (System.currentTimeMillis() - startTime)/1000.0 + " seconds");
                                   if(PRINT_MESSAGES) System.out.println("A* created " + numNodesCreated + " nodes");
                                   if(ShowSolutions) showSolution(solutionPathRootNode);
                                   if(PRINT_MESSAGES) System.out.println("********************************\nDONE WITH A*\n*******************************");

                                   nodePQLeastFirst = new PriorityQueue<SearchTreeNode>(10000, costComparatorLeastFirst);
                                   resetToPreviousPuzzle();
                           }


                           if(BFSRun){
                                   if(PRINT_MESSAGES) System.out.println("********************************\nSTARTING BFS\n**********************");
                                   numNodesCreated = 0;
                                   maxStructSize = 0;
                                   curPuzzleSolutionLength = 0;

                                   startTime = System.currentTimeMillis();

                                   solutionPathRootNode = breadthFirstSearch();
                                   //write data to file
                                   if(WRITE_TO_STAT_FILES) {
                                           String outputMessage = "" + curPuzzleSolutionLength + " " + numNodesCreated + " " + maxStructSize + " " + (System.currentTimeMillis() - startTime) + "\n";
                                           System.out.println("writing '" + outputMessage +"' to file");
                                           try {
                                                   bfsOut.write(outputMessage);
                                                   bfsOut.flush();
                                           } catch (IOException e ) {
                                                   System.out.println("ERROR writing to file: " + e);
                                                   return;
                                           }
                                   }

                                   if(PRINT_MESSAGES) System.out.println("Search took " + (System.currentTimeMillis() - startTime)/1000.0 + " seconds");
                                   if(PRINT_MESSAGES) System.out.println("BFS created " + numNodesCreated + " nodes");

                                   if(ShowSolutions) showSolution(solutionPathRootNode);

                                   if(PRINT_MESSAGES) System.out.println("********************************\nDONE WITH BFS\n**********************");

                                   resetToPreviousPuzzle();

                                   linkedList = new LinkedList<SearchTreeNode>();
                           }

                           if(IterativeDeepeningRun){
                                   if(PRINT_MESSAGES) System.out.println("********************************\nSTARTING ITERATIVE DEEPENING SEARCH\n****************");
                                   numNodesCreated = 0;
                                   maxStructSize = 0;
                                   curPuzzleSolutionLength = 0;

                                   startTime = System.currentTimeMillis();

                                   solutionPathRootNode = iterativeDeepeningDFS();
                                   if(solutionPathRootNode == null) {
                                           System.out.println("NO SOLUTION FOUND");
                                           System.exit(0);
                                   }
                                   if(WRITE_TO_STAT_FILES) {
                                           String outputMessage = "" + curPuzzleSolutionLength + " " + numNodesCreated + " " + maxStructSize + " " + (System.currentTimeMillis() - startTime) + "\n";                              System.out.println("writing '" + outputMessage +"' to file");
                                           try {
                                                   idfOut.write(outputMessage);
                                                   idfOut.flush();
                                           } catch (IOException e ) {
                                                   System.out.println("ERROR writing to file: " + e);
                                                   return;
                                           }
                                   }
                                   if(PRINT_MESSAGES) System.out.println("Search took " + (System.currentTimeMillis() - startTime)/1000.0 + " seconds");
                                   if(PRINT_MESSAGES) System.out.println("Iterative deepening created " + numNodesCreated + " nodes");

                                   if(ShowSolutions) showSolution(solutionPathRootNode);
                                   if(PRINT_MESSAGES) System.out.println("********************************\nDONE WITH ITERATIVE DEEPENING SEARCH\n*******************************");

                                   resetToPreviousPuzzle();
                                   linkedList = new LinkedList<SearchTreeNode>();
                           }

                           resetToNewPuzzle();;
                   }

                   //close up all the files
                   try {
                           bfsOut.close();
                           idfOut.close();
                           astarOut.close();
                   } catch (IOException e ) {
                           System.out.println("ERROR closing output files: " + e);
                           return;
                   }



                   if(PRINT_MESSAGES) System.out.println("*********************DOOOOOOOOOONE!!!!!*********************");

                   System.exit(1);
           }

           public SearchTreeNode breadthFirstSearch() {

                   if(USE_HASH)
                           //set up a Hashtable to store all of the nodes we create
                           hash.clear();

                   //set up the queue to store the nodes we want to look at
                   //              linkedList.clear();
                   linkedList = new LinkedList<SearchTreeNode>();

                   //make the initial node
                   SearchTreeNode startNode = new SearchTreeNode(new TilePuzzleState(tileNumbers), null, NO_ACTION, 0);

                   numNodesCreated++;

                   if(USE_HASH)
                           //add the initial node to the table
                           hash.put(startNode, startNode);

                   //add the initial node to the queue
                   linkedList.add(startNode);

                   //node that will eventually be the goal node
                   SearchTreeNode goalNode = null;

                   //current node we're looking at
                   SearchTreeNode curNode = null;

                   //              int count = 0;

                   //keep going while the goal has not been found
                   while (goalNode == null) {
                           //                      if(PRINT_MESSAGES) System.out.println("BFS Queue size = " + linkedList.size());
                           //get the node on the top of the queue
                           try {
                                   curNode = linkedList.removeFirst();
                           } catch (NoSuchElementException e) {
                                   System.out.println("Queue is empty.  No solution found.");
                                   System.exit(0);
                           }

                           //debug line
                           //                      if(PRINT_MESSAGES) {
                           //                              count ++;
                           //                              if(count % 50 == 0)
                           //                                      System.out.println("Looked at " + count + " nodes.");
                           //                      }

                           //expand it, and if the goal is found, assign to goalNode
                           goalNode = bfsExpandNode(curNode);
                   }

                   return findSolutionPath(goalNode);

           }


           /**Expands a parent node, creating all of its children and adding them to the queue and table if they have not been created before.
            * Returns a child if it is a goal state, otherwise returns null.
            * @param parent
            */
           private SearchTreeNode bfsExpandNode(SearchTreeNode parent) {

                   //make the up child
                   TilePuzzleState upState = parent.getState().getSuccessor(UP);
                   if(upState != null) {
                           //the node's path cost and depth are one great than that of its parent
                           SearchTreeNode upNode = new SearchTreeNode(upState, parent, UP, parent.getDepth() + 1);

                           numNodesCreated++;

                           //check if the node is a goal state, and if it is, return it
                           if(upState.isGoal()) {
                                   return upNode;
                           }

                           if(USE_HASH) {
                                   //check if the node is already in the Hashtable
                                   if(!(hash.containsValue(upNode))) {
                                           //the hashtable does not have it already, so add it to the table
                                           hash.put(upNode, upNode);
                                           //the table does not have the upNode in it, so we should put it on the queue so it will get expanded later
                                           linkedList.add(upNode);
                                           if(maxStructSize < linkedList.size()) maxStructSize = linkedList.size();
                                   }
                           } else {
                                   //add the node to the list
                                   linkedList.add(upNode);
                                   if(maxStructSize < linkedList.size()) maxStructSize = linkedList.size();

                           }
                   }

                   //make the right child
                   TilePuzzleState rightState = parent.getState().getSuccessor(RIGHT);
                   if(rightState != null) {
                           //the node's path cost and depth are one great than that of its parent
                           SearchTreeNode rightNode = new SearchTreeNode(rightState, parent, RIGHT, parent.getDepth() + 1);

                           numNodesCreated++;

                           //check if the node is a goal state, and if it is, return it
                           if(rightState.isGoal()) {
                                   return rightNode;
                           }

                           if(USE_HASH) {
                                   //check if the node is already in the Hashtable
                                   if(!(hash.containsValue(rightNode))) {
                                           //the hash does not have it already, so add it to the table
                                           hash.put(rightNode, rightNode);
                                           //the hash does not have the rightNode in it, so we should put it on the queue so it will get expanded later
                                           linkedList.add(rightNode);
                                           if(maxStructSize < linkedList.size()) maxStructSize = linkedList.size();
                                   }
                           } else {
                                   //add the node to the queue
                                   linkedList.add(rightNode);
                                   if(maxStructSize < linkedList.size()) maxStructSize = linkedList.size();
                           }
                   }

                   //make the down child
                   TilePuzzleState downState = parent.getState().getSuccessor(DOWN);
                   if(downState != null) {
                           //the node's path cost and depth are one great than that of its parent
                           SearchTreeNode downNode = new SearchTreeNode(downState, parent, DOWN, parent.getDepth() + 1);

                           numNodesCreated++;

                           //check if the node is a goal state, and if it is, return it
                           if(downState.isGoal()) {
                                   return downNode;
                           }
                           if(USE_HASH) {
                                   //check if the node is already in the Hashtable
                                   if(!(hash.containsValue(downNode))) {
                                           //the hash does not have it already, so add it to the table
                                           hash.put(downNode, downNode);
                                           //the hash does not have the downNode in it, so we should put it on the queue so it will get expanded later
                                           linkedList.add(downNode);
                                           if(maxStructSize < linkedList.size()) maxStructSize = linkedList.size();
                                   }
                           } else {
                                   //add the node to the queue
                                   linkedList.add(downNode);
                                   if(maxStructSize < linkedList.size()) maxStructSize = linkedList.size();
                           }

                   }

                   //make the left child
                   TilePuzzleState leftState = parent.getState().getSuccessor(LEFT);
                   if(leftState != null) {
                           //the node's path cost and depth are one great than that of its parent
                           SearchTreeNode leftNode = new SearchTreeNode(leftState, parent, LEFT, parent.getDepth() + 1);

                           numNodesCreated++;

                           //check if the node is a goal state, and if it is, return it
                           if(leftState.isGoal()) {
                                   return leftNode;
                           }

                           if(USE_HASH) {
                                   //check if the node is already in the Hashtable
                                   if(!(hash.containsValue(leftNode))) {
                                           //the hash does not have it already, so add it to the table
                                           hash.put(leftNode, leftNode);
                                           //the hash does not have the leftNode in it, so we should put it on the queue so it will get expanded later
                                           linkedList.add(leftNode);
                                           if(maxStructSize < linkedList.size()) maxStructSize = linkedList.size();
                                   }
                           } else {
                                   //add the node to the queue
                                   linkedList.add(leftNode);
                                   if(maxStructSize < linkedList.size()) maxStructSize = linkedList.size();
                           }

                   }

                   //we did not find any solutions, so return null
                   return null;
           }

           /** iterativeDeepeningDFS
            * Performs limited depth first searches with greater and greater depth until it finds a solution.
            * @return
            */
           public SearchTreeNode iterativeDeepeningDFS() {
                   //start with the goal node = null
                   SearchTreeNode goalNode = null;

                   for(int depth = 0; goalNode == null; depth++) {
                           //limitedDFS will return the solution node if it finds it, or null if it doesn't.
                           goalNode = limitedDFS(depth);
                   }

                   return goalNode;
           }



           /** Limited Depth First Search
            * Does a depth first search to a limited depth.
            * Explores nodes at that depth to see if they are goals, but not passed that depth.
            * @param depth
            * The depth to explore to
            * @return
            * A solution node, if it finds one, or null.
            */
           private SearchTreeNode limitedDFS(int depth) {

                   if(USE_HASH)
                           //set up a Hashtable to store all of the nodes we create
                           hash.clear();

                   //set up the stack to store the nodes we want to look at
                   //              linkedList.clear();
                   linkedList = new LinkedList<SearchTreeNode>();

                   //make sure that tileNumbers is set correctly
                   resetToPreviousPuzzle();

                   //make the initial node
                   SearchTreeNode startNode = new SearchTreeNode(new TilePuzzleState(tileNumbers), null, NO_ACTION, 0);

                   numNodesCreated++;

                   if(USE_HASH)
                           //add the initial node to the table
                           hash.put(startNode, startNode);

                   //add the initial node to the stack
                   linkedList.add(startNode);

                   //node that will eventually be the goal node
                   SearchTreeNode goalNode = null;

                   //current node we're looking at
                   SearchTreeNode curNode = null;

                   //              if(PRINT_MESSAGES) System.out.println("Depth = " + depth);

                   //keep going while the goal has not been found or the stack is empty, meaning we have explored all nodes with depth less than or equal to target depth
                   while (goalNode == null && !linkedList.isEmpty()) {
                           //get the node on the top of the queue
                           try {
                                   curNode = linkedList.removeFirst();
                           } catch (NoSuchElementException e) {
                                   //This should never happen - we should exit the while loop before this is the case, but we're just being careful.
                                   System.out.println("Stack is empty.  No solution found.");
                                   System.exit(0);
                           }

                           //expand it, and if the goal is found, assign to goalNode
                           goalNode = limitedDfsExpandNode(curNode, depth);
                   }

                   return findSolutionPath(goalNode);
           }

           /** Expand node used in limited DFS.  It expands the parent node in each direction.
            * If it creates a goal node, it returns it.
            * It checks the hash to see if the new node has already been created, and only adds it to the hash if it is not in the hash
            * already or if the value is the hash is at a greater depth.
            * Then, it adds it to the stack of nodes to expand only if the new node's depth is less than the depth we want to explore to.
            *
            * @param parent
            * The parent node.
            * @param depth
            * The depth we want to explore to, but not passed.
            * @return a goal node, if it finds one, or null.
            */
           private SearchTreeNode limitedDfsExpandNode(SearchTreeNode parent, int depth) {

                   //make the up child
                   TilePuzzleState upState = parent.getState().getSuccessor(UP);
                   if(upState != null) {
                           //the node's path cost and depth are one great than that of its parent
                           SearchTreeNode upNode = new SearchTreeNode(upState, parent, UP, parent.getDepth() + 1);

                           numNodesCreated++;

                           //check if the node is a goal state, and if it is, return it
                           if(upState.isGoal()) {
                                   return upNode;
                           }

                           if(USE_HASH) {
                                   //check if the node is already in the Hashtable or if it is in the hashtable with a greater depth
                                   if(!(hash.containsValue(upNode)) || upNode.getDepth() < hash.get(upNode).getDepth()) {
                                           //the hashtable does not have it already, so add it to the table
                                           hash.put(upNode, upNode);

                                           //check if it is at the maximum depth.  If it is, do not add it to the stack/linkedList
                                           if(upNode.getDepth() < depth) {
                                                   //the table does not have the upNode in it, so we should put it on the queue so it will get expanded later
                                                   linkedList.addFirst(upNode);
                                                   if(maxStructSize < linkedList.size()) maxStructSize = linkedList.size();
                                           }
                                   }
                           } else {
                                   if(upNode.getDepth() < depth) {
                                           linkedList.addFirst(upNode);
                                           if(maxStructSize < linkedList.size()) maxStructSize = linkedList.size();
                                   }
                           }
                   }

                   //make the right child
                   TilePuzzleState rightState = parent.getState().getSuccessor(RIGHT);
                   if(rightState != null) {
                           //the node's path cost and depth are one great than that of its parent
                           SearchTreeNode rightNode = new SearchTreeNode(rightState, parent, RIGHT, parent.getDepth() + 1);

                           numNodesCreated++;

                           //check if the node is a goal state, and if it is, return it
                           if(rightState.isGoal()) {
                                   return rightNode;
                           }

                           if(USE_HASH) {
                                   //check if the node is already in the Hashtable or if it is in the hashtable with a greater depth
                                   if(!(hash.containsValue(rightNode)) || rightNode.getDepth() < hash.get(rightNode).getDepth()) {
                                           //the hash does not have it already, so add it to the table
                                           hash.put(rightNode, rightNode);

                                           //check if it is at the maximum depth.  If it is, do not add it to the stack/linkedList
                                           if(rightNode.getDepth() < depth) {
                                                   //the table does not have the upNode in it, so we should put it on the queue so it will get expanded later
                                                   linkedList.addFirst(rightNode);
                                                   if(maxStructSize < linkedList.size()) maxStructSize = linkedList.size();
                                           }
                                   }
                           } else {
                                   if(rightNode.getDepth() < depth) {
                                           linkedList.addFirst(rightNode);
                                           if(maxStructSize < linkedList.size()) maxStructSize = linkedList.size();
                                   }
                           }
                   }

                   //make the down child
                   TilePuzzleState downState = parent.getState().getSuccessor(DOWN);
                   if(downState != null) {
                           //the node's path cost and depth are one great than that of its parent
                           SearchTreeNode downNode = new SearchTreeNode(downState, parent, DOWN, parent.getDepth() + 1);

                           numNodesCreated++;

                           //check if the node is a goal state, and if it is, return it
                           if(downState.isGoal()) {
                                   return downNode;
                           }

                           if(USE_HASH) {
                                   //check if the node is already in the Hashtable or if it is in the hashtable with a greater depth
                                   if(!(hash.containsValue(downNode)) || downNode.getDepth() < hash.get(downNode).getDepth()) {
                                           //the hash does not have it already, so add it to the table
                                           hash.put(downNode, downNode);

                                           //check if it is at the maximum depth.  If it is, do not add it to the stack/linkedList
                                           if(downNode.getDepth() < depth) {
                                                   //the table does not have the upNode in it, so we should put it on the queue so it will get expanded later
                                                   linkedList.addFirst(downNode);
                                                   if(maxStructSize < linkedList.size()) maxStructSize = linkedList.size();
                                           }
                                   }
                           } else {
                                   if(downNode.getDepth() < depth) {
                                           linkedList.addFirst(downNode);
                                           if(maxStructSize < linkedList.size()) maxStructSize = linkedList.size();
                                   }
                           }
                   }

                   //make the left child
                   TilePuzzleState leftState = parent.getState().getSuccessor(LEFT);
                   if(leftState != null) {
                           //the node's path cost and depth are one great than that of its parent
                           SearchTreeNode leftNode = new SearchTreeNode(leftState, parent, LEFT, parent.getDepth() + 1);

                           numNodesCreated++;

                           //check if the node is a goal state, and if it is, return it
                           if(leftState.isGoal()) {
                                   return leftNode;
                           }

                           if(USE_HASH) {
                                   //check if the node is already in the Hashtable or if it is in the hashtable with a greater depth
                                   if(!(hash.containsValue(leftNode)) || leftNode.getDepth() < hash.get(leftNode).getDepth()) {
                                           //the hash does not have it already, so add it to the table
                                           hash.put(leftNode, leftNode);

                                           //check if it is at the maximum depth.  If it is, do not add it to the stack/linkedList
                                           if(leftNode.getDepth() < depth) {
                                                   //the table does not have the upNode in it, so we should put it on the queue so it will get expanded later
                                                   linkedList.addFirst(leftNode);
                                                   if(maxStructSize < linkedList.size()) maxStructSize = linkedList.size();
                                           }
                                   }
                           } else {
                                   if(leftNode.getDepth() < depth) {
                                           linkedList.addFirst(leftNode);
                                           if(maxStructSize < linkedList.size()) maxStructSize = linkedList.size();
                                   }
                           }
                   }

                   //we did not find any solutions, so return null
                   return null;
           }


           public SearchTreeNode AStar() {

                   if(USE_HASH)
                           //reset the hash
                           hash.clear();

                   //reset the PQ
                   //              nodePQLeastFirst.clear();
                   nodePQLeastFirst = new PriorityQueue<SearchTreeNode>(10000, costComparatorLeastFirst);

                   //make the initial node
                   SearchTreeNode startNode = new SearchTreeNode(new TilePuzzleState(tileNumbers), null, NO_ACTION, 0);

                   numNodesCreated++;

                   if(USE_HASH)
                           //add the initial node to the table
                           hash.put(startNode, startNode);

                   //add the initial node to the PQ
                   nodePQLeastFirst.add(startNode);

                   //set up the goal node and current node
                   SearchTreeNode goalNode = null;
                   SearchTreeNode curNode = null;

                   //keep running until we have a solution
                   while(goalNode == null) {
                           curNode = nodePQLeastFirst.poll();
                           if(curNode == null) {
                                   //if curNode is null, the queue is empty, so no solution can be found
                                   System.out.println("Queue is empty.  No solution found.");
                                   System.exit(0);
                           }
                           //expand the current node
                           goalNode = aStarExpandNode(curNode);
                   }


                   return findSolutionPath(goalNode);
           }


           private SearchTreeNode aStarExpandNode(SearchTreeNode parent) {

                   //make the up child
                   TilePuzzleState upState = parent.getState().getSuccessor(UP);
                   if(upState != null) {
                           //the node's path cost and depth are one great than that of its parent
                           SearchTreeNode upNode = new SearchTreeNode(upState, parent, UP, parent.getDepth() + 1);

                           numNodesCreated++;

                           //check if the node is a goal state, and if it is, return it
                           if(upState.isGoal()) {
                                   return upNode;
                           }

                           if(USE_HASH) {
                                   //check if the node is already in the Hashtable
                                   if(!(hash.containsValue(upNode)) || upNode.getPathCost() < hash.get(upNode).getPathCost()) {
                                           //the hashtable does not have it already or our new node has a shorter path cost, so add it to the table
                                           hash.put(upNode, upNode);
                                           //the table does not have the upNode in it, so we should put it on the queue so it will get expanded later
                                           nodePQLeastFirst.add(upNode);
                                           if(maxStructSize < nodePQLeastFirst.size()) maxStructSize = nodePQLeastFirst.size();
                                   }
                           } else {
                                   nodePQLeastFirst.add(upNode);
                                   if(maxStructSize < nodePQLeastFirst.size()) maxStructSize = nodePQLeastFirst.size();
                           }
                   }

                   //make the right child
                   TilePuzzleState rightState = parent.getState().getSuccessor(RIGHT);
                   if(rightState != null) {
                           //the node's path cost and depth are one great than that of its parent
                           SearchTreeNode rightNode = new SearchTreeNode(rightState, parent, RIGHT, parent.getDepth() + 1);

                           numNodesCreated++;

                           //check if the node is a goal state, and if it is, return it
                           if(rightState.isGoal()) {
                                   return rightNode;
                           }

                           if(USE_HASH) {
                                   //check if the node is already in the Hashtable
                                   if(!(hash.containsValue(rightNode)) || rightNode.getPathCost() < hash.get(rightNode).getPathCost()) {
                                           //the hashtable does not have it already or our new node has a shorter path cost, so add it to the table
                                           hash.put(rightNode, rightNode);
                                           //the table does not have the rightNode in it, so we should put it on the queue so it will get expanded later
                                           nodePQLeastFirst.add(rightNode);
                                           if(maxStructSize < nodePQLeastFirst.size()) maxStructSize = nodePQLeastFirst.size();

                                   }
                           } else {
                                   nodePQLeastFirst.add(rightNode);
                                   if(maxStructSize < nodePQLeastFirst.size()) maxStructSize = nodePQLeastFirst.size();
                           }

                   }

                   //make the down child
                   TilePuzzleState downState = parent.getState().getSuccessor(DOWN);
                   if(downState != null) {
                           //the node's path cost and depth are one great than that of its parent
                           SearchTreeNode downNode = new SearchTreeNode(downState, parent, DOWN, parent.getDepth() + 1);

                           numNodesCreated++;

                           //check if the node is a goal state, and if it is, return it
                           if(downState.isGoal()) {
                                   return downNode;
                           }

                           if(USE_HASH) {
                                   //check if the node is already in the Hashtable
                                   if(!(hash.containsValue(downNode)) || downNode.getPathCost() < hash.get(downNode).getPathCost()) {
                                           //the hashtable does not have it already or our new node has a shorter path cost, so add it to the table
                                           hash.put(downNode, downNode);
                                           //the table does not have the downNode in it, so we should put it on the queue so it will get expanded later
                                           nodePQLeastFirst.add(downNode);
                                           if(maxStructSize < nodePQLeastFirst.size()) maxStructSize = nodePQLeastFirst.size();
                                   }
                           } else {
                                   nodePQLeastFirst.add(downNode);
                                   if(maxStructSize < nodePQLeastFirst.size()) maxStructSize = nodePQLeastFirst.size();
                           }

                   }

                   //make the left child
                   TilePuzzleState leftState = parent.getState().getSuccessor(LEFT);
                   if(leftState != null) {
                           //the node's path cost and depth are one great than that of its parent
                           SearchTreeNode leftNode = new SearchTreeNode(leftState, parent, LEFT, parent.getDepth() + 1);

                           numNodesCreated++;

                           //check if the node is a goal state, and if it is, return it
                           if(leftState.isGoal()) {
                                   return leftNode;
                           }

                           if(USE_HASH) {
                                   //check if the node is already in the Hashtable
                                   if(!(hash.containsValue(leftNode)) || leftNode.getPathCost() < hash.get(leftNode).getPathCost()) {
                                           //the hashtable does not have it already or our new node has a shorter path cost, so add it to the table
                                           hash.put(leftNode, leftNode);
                                           //the table does not have the leftNode in it, so we should put it on the queue so it will get expanded later
                                           nodePQLeastFirst.add(leftNode);
                                           if(maxStructSize < nodePQLeastFirst.size()) maxStructSize = nodePQLeastFirst.size();
                                   }
                           } else {
                                   nodePQLeastFirst.add(leftNode);
                                   if(maxStructSize < nodePQLeastFirst.size()) maxStructSize = nodePQLeastFirst.size();
                           }

                   }

                   //we did not find any solutions, so return null
                   return null;
           }


           /** Makes a solution node with all solutionChild nodes filled in the solution path
            */
           private SearchTreeNode findSolutionPath(SearchTreeNode goalNode) {
                   if(goalNode == null) {
                           return null;
                   }

                   int solutionLength = 0;

                   SearchTreeNode currentNode = goalNode;
                   SearchTreeNode parentNode = goalNode.getParent();

                   while(currentNode.getParent() != null) {
                           solutionLength++;
                           //set the parent's solution child
                           parentNode.setSolutionChild(currentNode);

                           //move the current node and parent node up one
                           currentNode = parentNode;
                           parentNode = currentNode.getParent();
                   }

                   if(PRINT_MESSAGES) System.out.println("Solution Length = " + solutionLength);
                   curPuzzleSolutionLength = solutionLength;
                   return currentNode;
           }





           // generate a random puzzle by moving the blank randomly in a legal
           // direction the specified number of times
           public void generateRandomPuzzle(int numberMoves) {

                   for (int m = 1 ; m <= numberMoves ; m++) {
                           blankRandomMove();
                   }

           }





           // generates and makes a random legal move for the blank
           public void blankRandomMove() {

                   // tranlsate the row and column of the blank into a position number
                   // (see file header comment in case the obvious position numbers aren't obvious)
                   int currentBlankPosition = (blankCurrentRow * PUZZLE_DIMENSION) + (blankCurrentCol + 1);

                   // index into the legalBlankMoves array to get an array of positions that can be
                   // moved to from the current position
                   int numLegalMoves = legalBlankMoves[currentBlankPosition].length;

                   // generate a random number and MOD it by the appropriate amount to get an index
                   //  into the array of legal moves and use it to get the position to move to
                   int legalBlankMoveIndex = randomBlankMoveIndex.nextValue() % numLegalMoves;
                   int newBlankPosition = legalBlankMoves[currentBlankPosition][legalBlankMoveIndex];

                   // translate the new position to [row][col] coordinates
                   int newBlankRow = (newBlankPosition-1) / PUZZLE_DIMENSION;
                   int newBlankCol = (newBlankPosition-1) % PUZZLE_DIMENSION;

                   // copy the non-0 number in the the new blank position to the old blank
                   // position and zero out the new blank position
                   tileNumbers[blankCurrentRow][blankCurrentCol] = tileNumbers[newBlankRow][newBlankCol];
                   tileNumbers[newBlankRow][newBlankCol] = 0;

                   // update the current blank coordinates
                   blankCurrentRow = newBlankRow;
                   blankCurrentCol = newBlankCol;

           }




           // reset the puzzle to its previous numbers; need to be careful so that references to
           // tile graphical objects don't get screwed up.....
           public void resetToPreviousPuzzle() {

                   // now that each initially non-blank position actually has tile graphical objects, go through the
                   // initialTileNumbers array and reset the Text object at each position to reflect these previous
                   // numbers; also reset the tileNumbers array
                   for (int r = 0 ; r < PUZZLE_DIMENSION ; r++) {
                           for (int c = 0 ; c < PUZZLE_DIMENSION ; c++) {
                                   tileNumbers[r][c] = initialTileNumbers[r][c];
                           }
                   }

                   // reset the blank coordinates
                   blankCurrentRow = blankInitialRow;
                   blankCurrentCol = blankInitialCol;


           }




           // reset the puzzle to new numbers
           public void resetToNewPuzzle() {

                   //set the tileNumebers array so that it is solved
                   int tileNumber = 1;
                   for (int r = 0 ; r < PUZZLE_DIMENSION ; r++) {
                           for (int c = 0 ; c < PUZZLE_DIMENSION ; c++) {
                                   tileNumbers[r][c] = tileNumber % (PUZZLE_DIMENSION * PUZZLE_DIMENSION);
                                   ++tileNumber;
                           }
                   }

                   // reset the blank coordinates
                   // (they should have this value anyway, but just to make sure...)
                   blankCurrentRow = PUZZLE_DIMENSION-1;
                   blankCurrentCol = PUZZLE_DIMENSION-1;

                   generateRandomPuzzle(NUMBER_BACKWARD_MOVES);

                   // save the initial configuration and blank location
                   for (int r = 0 ; r < PUZZLE_DIMENSION ; r++) {
                           for (int c = 0 ; c < PUZZLE_DIMENSION ; c++) {
                                   initialTileNumbers[r][c] = tileNumbers[r][c];
                           }
                   }
                   blankInitialRow = blankCurrentRow;
                   blankInitialCol = blankCurrentCol;

           }





           // returns a String representing a TilePuzzleState; the String is just the concatenation
           // of the tile numbers reading left-to-right and top-down (the blank is 0)
           public String stateString(TilePuzzleState s) {

                   int[][] tempTiles = s.getTileNumbers();
                   String stateStr = "";
                   for (int r = 0 ; r < PUZZLE_DIMENSION ; r++) {
                           for (int c = 0 ; c < PUZZLE_DIMENSION ; c++) {
                                   stateStr += tempTiles[r][c];
                           }
                   }

                   return stateStr;
           }




           // show the numbers in one of the numbers arrays
           public void showNumbers (int[][] tileNums) {

                   for (int r = 0 ; r < PUZZLE_DIMENSION ; r++) {
                           for (int c = 0 ; c < PUZZLE_DIMENSION ; c++) {
                                   int number = tileNums[r][c];
                                   if (number != 0)
                                           System.out.print(number + "  ");
                                   else
                                           System.out.print("   ");
                           }
                           System.out.println();
                   }
                   System.out.println();
                   System.out.println();
                   System.out.println();

           }



           // show the solution graphically
           public void showSolution (SearchTreeNode currSolutionPathNode) {

                   // create all the other arrays we need to "tile-ize" the array,
                   // i.e. make and manipulate the graphical representation
                   tiles = new FilledRect[PUZZLE_DIMENSION][PUZZLE_DIMENSION];
                   tileFrames = new FramedRect[PUZZLE_DIMENSION][PUZZLE_DIMENSION];
                   tileUpperLeftX = new double[PUZZLE_DIMENSION][PUZZLE_DIMENSION];
                   tileUpperLeftY = new double[PUZZLE_DIMENSION][PUZZLE_DIMENSION];
                   tileNumberText = new Text[PUZZLE_DIMENSION][PUZZLE_DIMENSION];

                   // the black border and background against which the tiles get moved; note
                   // that there is no blank tile -- it's just the background showing through
                   // where there's no green tile
                   new FilledRect(PUZZLE_UPPER_LEFT_X - NUM_PUZZLE_BORDER_PIXELS,
                                   PUZZLE_UPPER_LEFT_Y - NUM_PUZZLE_BORDER_PIXELS,
                                   PUZZLE_SIDE_LENGTH + NUM_PUZZLE_BORDER_PIXELS * 2,
                                   PUZZLE_SIDE_LENGTH + NUM_PUZZLE_BORDER_PIXELS * 2, canvas);

                   // create the graphical puzzle with tile numbers corresponding to the
                   // randomized tile numbers in the tilenumbers array
                   for (int r = 0 ; r < PUZZLE_DIMENSION ; r++) {
                           for (int c = 0 ; c < PUZZLE_DIMENSION ; c++) {

                                   // only create graphics objects for locations where there's a tile;
                                   // 0 is the blank
                                   if (initialTileNumbers[r][c] != 0) {
                                           tiles[r][c] = new FilledRect(PUZZLE_UPPER_LEFT_X + c * TILE_SIZE,
                                                           PUZZLE_UPPER_LEFT_Y + r * TILE_SIZE,
                                                           TILE_SIZE, TILE_SIZE, canvas);
                                           tiles[r][c].setColor(tileColor);

                                           tileFrames[r][c] = new FramedRect(PUZZLE_UPPER_LEFT_X + c * TILE_SIZE,
                                                           PUZZLE_UPPER_LEFT_Y + r * TILE_SIZE,
                                                           TILE_SIZE, TILE_SIZE, canvas);

                                           tileUpperLeftX[r][c] = tiles[r][c].getX();
                                           tileUpperLeftY[r][c] = tiles[r][c].getY();

                                           tileNumberText[r][c] = new Text(initialTileNumbers[r][c],
                                                           PUZZLE_UPPER_LEFT_X + c * TILE_SIZE + NUMBER_X_OFFSET,
                                                           PUZZLE_UPPER_LEFT_Y + r * TILE_SIZE + NUMBER_Y_OFFSET, canvas);
                                           tileNumberText[r][c].setFontSize(NUMBER_FONT_SIZE);

                                   }
                                   else {
                                           // make sure the references to graphical objects where the blank is
                                           // are all null
                                           tiles[blankCurrentRow][blankCurrentCol] = null;
                                           tileFrames[blankCurrentRow][blankCurrentCol] = null;
                                           tileNumberText[blankCurrentRow][blankCurrentCol] = null;

                                           // we're still going to need the upper-left coordinates for this
                                           // square, so calculate and store them
                                           tileUpperLeftX[r][c] = PUZZLE_UPPER_LEFT_X + c * TILE_SIZE;
                                           tileUpperLeftY[r][c] = PUZZLE_UPPER_LEFT_Y + r * TILE_SIZE;

                                   }
                           }
                   }


                   pause(2000);


                   // take care of the special case when the puzzle starts out solved
                   if (currSolutionPathNode.getState().isGoal())
                           return;

                   // used to save the new row and col of the blank after a move
                   int newBlankRow = 0;
                   int newBlankCol = 0;

                   // follow the path down to the goal state node
                   while (!currSolutionPathNode.getState().isGoal()) {

                           // *******************************************************************************
                           // follow the solutionChild "pointers" to the goal
                           SearchTreeNode currSolutionPathNodeChild = currSolutionPathNode.getSolutionChild();
                           // *******************************************************************************

                           int action = currSolutionPathNodeChild.getAction();

                           // note that the action refers to the movement of the *blank*, so
                           // this needs to be translated into the movement of an actual tile,
                           // e.g. moving the blank UP corresponds to moving the tile above
                           // blank DOWN

                           // save the new row and col of the blank and move the actual tile
                           if (action == UP)  {
                                   newBlankRow = blankCurrentRow-1;
                                   newBlankCol = blankCurrentCol;
                                   moveTileGraphical(blankCurrentRow-1, blankCurrentCol, DOWN);
                           }
                           else if (action == RIGHT)  {
                                   newBlankRow = blankCurrentRow;
                                   newBlankCol = blankCurrentCol+1;
                                   moveTileGraphical(blankCurrentRow, blankCurrentCol+1, LEFT);
                           }
                           else if (action == DOWN)  {
                                   newBlankRow = blankCurrentRow+1;
                                   newBlankCol = blankCurrentCol;
                                   moveTileGraphical(blankCurrentRow+1, blankCurrentCol, UP);
                           }
                           else if (action == LEFT)  {
                                   newBlankRow = blankCurrentRow;
                                   newBlankCol = blankCurrentCol-1;
                                   moveTileGraphical(blankCurrentRow, blankCurrentCol-1, RIGHT);
                           }
                           else {
                                   System.out.println("Error in move");
                           }

                           currSolutionPathNode = currSolutionPathNodeChild;

                   }

                   // make sure the object pointers in the new blank spot are all null
                   // and that the number in the numbers array at that spot is 0
                   tiles[newBlankRow][newBlankCol] = null;
                   tileFrames[newBlankRow][newBlankCol] = null;
                   tileNumberText[newBlankRow][newBlankCol] = null;
                   tileNumbers[newBlankRow][newBlankCol] = 0;

           }




           // moves a tile at [r][c] in the indicated direction; assumes both that there is a
           // tile at [r][c] and that the move is legal
           private void moveTileGraphical(int r, int c, int dir) {

                   if (dir == UP)  {
                           // gradually move the tile
                           while (tiles[r][c].getY() > tileUpperLeftY[r-1][c]) {
                                   tiles[r][c].move(0, -MOVE_PIXELS);
                                   tileFrames[r][c].move(0, -MOVE_PIXELS);
                                   tileNumberText[r][c].move(0, -MOVE_PIXELS);
                                   pause(MOVE_DELAY);
                           }
                           // reset object references and the affected blank coordinate to reflect the move
                           tiles[r-1][c] = tiles[r][c];
                           tileFrames[r-1][c] = tileFrames[r][c];
                           tileNumbers[r-1][c] = tileNumbers[r][c];
                           tileNumberText[r-1][c] = tileNumberText[r][c];
                           ++blankCurrentRow;
                   }

                   else if (dir == RIGHT)  {
                           // gradually move the tile
                           while (tiles[r][c].getX() < tileUpperLeftX[r][c+1]) {
                                   tiles[r][c].move(MOVE_PIXELS, 0);
                                   tileFrames[r][c].move(MOVE_PIXELS, 0);
                                   tileNumberText[r][c].move(MOVE_PIXELS, 0);
                                   pause(MOVE_DELAY);
                           }
                           // reset object references and the affected blank coordinate to reflect the move
                           tiles[r][c+1] = tiles[r][c];
                           tileFrames[r][c+1] = tileFrames[r][c];
                           tileNumbers[r][c+1] = tileNumbers[r][c];
                           tileNumberText[r][c+1] = tileNumberText[r][c];
                           --blankCurrentCol;
                   }

                   else if (dir == DOWN)  {
                           // gradually move the tile
                           while (tiles[r][c].getY() < tileUpperLeftY[r+1][c]) {
                                   tiles[r][c].move(0, MOVE_PIXELS);
                                   tileFrames[r][c].move(0, MOVE_PIXELS);
                                   tileNumberText[r][c].move(0, MOVE_PIXELS);
                                   pause(MOVE_DELAY);
                           }
                           // reset object references and the affected blank coordinate to reflect the move
                           tiles[r+1][c] = tiles[r][c];
                           tileFrames[r+1][c] = tileFrames[r][c];
                           tileNumbers[r+1][c] = tileNumbers[r][c];
                           tileNumberText[r+1][c] = tileNumberText[r][c];
                           --blankCurrentRow;
                   }

                   else if (dir == LEFT)  {
                           // gradually move the tile
                           while (tiles[r][c].getX() > tileUpperLeftX[r][c-1]) {
                                   tiles[r][c].move(-MOVE_PIXELS, 0);
                                   tileFrames[r][c].move(-MOVE_PIXELS, 0);
                                   tileNumberText[r][c].move(-MOVE_PIXELS, 0);
                                   pause(MOVE_DELAY);
                           }
                           // reset object references and the affected blank coordinate to reflect the move
                           tiles[r][c-1] = tiles[r][c];
                           tileFrames[r][c-1] = tileFrames[r][c];
                           tileNumbers[r][c-1] = tileNumbers[r][c];
                           tileNumberText[r][c-1] = tileNumberText[r][c];
                           ++blankCurrentCol;
                   }

                   else {
                           System.out.println("Error:; unknown move");
                   }

                   // reset the object references at the newly blank position to refer to nothing
                   tiles[r][c] = null;
                   tileFrames[r][c] = null;
                   tileNumberText[r][c] = null;

           }



    }
