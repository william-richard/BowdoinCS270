To run the program, run the main method in TilePuzzleShell.java.

To change the behavior of the program, see the top of TilePuzzle.java.

From there, you can control
Which searches are used by setting the boolean for a given search to “true” or “false”
for example, if you want to run A*, set AStarRun = true.  If you do not want A* to run, set AStarRun = false.
How many puzzles are run by changing NumOfPuzzles to the preferred value.
If log messages are printed by setting PRINT_MESSAGES to true or false
if and where statistic files are written by first setting WRITE_TO_STAT_FILES to true or false, and then specifying OUTPUT_FILE_DIR and the various search file names.
How random the puzzles are by setting NUMBER_BACKWARD_MOVES.  The higher this value, the longer the average shortest solution path.



