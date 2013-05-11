import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;


public class MaxSatSolver {

	//Variables to deal with output statistics
	private static final boolean WRITE_STATS_TO_FILES = true;
	private static final String OUTPUT_FILE_LOCATION = "/Users/Will/Documents/Schoolwork/Fall 09/AI/Homework/assign3-distributed/data_files/";
	private static BufferedWriter outputFile;

	//variables for each MaxSatSover
	private int[][] SatFormula;
	private double[] probVector;
	private int nIterations; 
	private int nIndividualsPerIteration; 
	private double posLearningRate;
	private double negLearningRate;
	private double mutProb;
	private double mutAmt;

	//boolean to decide if we are using negative learning
	private static boolean useNegativeLearning = true;

	//store the best solution that we have seen, and the number of clauses that it satisfied
	private SolutionVector overallBestSolutionVector;	

	public MaxSatSolver(String[] args) {
		if(args.length != 7) {
			//the args array is not correct - complain
			System.out.println("Incorrect number of arguments passed\nCorrect usage: java MaxSatSolver <filename> <num iterations> <num individuals per iteration> <positive learning rate> <negative learning rate> <mutation probablitily> <mutation amount>");
			System.exit(0);
		}
		
		//As we assign arguments to variables, the assignments and conversions will throw errors if the argument passed is not of the correct type

		//get the SAT Formula from the file
		SatFormula = SATFileReader.getFormulaArray(args[0]);

		//set up the probability vector
		//so that variable 1 corresponds to index 1 in the probability vector,
		//we will ignore probVector[0]
		probVector = new double[SATFileReader.nVariables + 1];
		//set all initial probabilities to .5
		for(int i = 1; i < probVector.length; i++) {
			probVector[i] = .5;
		}

		//get the number of iterations
		try {
			nIterations = (new Integer(args[1])).intValue();
		} catch (NumberFormatException e) {
			System.out.println("Number of interations passed is not valid:\n" + e);
			System.exit(0);
		}

		//get the number of individuals per iteration
		try {
			nIndividualsPerIteration = (new Integer(args[2])).intValue();
		} catch (NumberFormatException e) {
			System.out.println("Number of individuals to generate per iteration passed is not valid.\n" + e);
			System.exit(0);
		}

		//get the positive learning rate
		try {
			posLearningRate = (new Double(args[3])).doubleValue();
		} catch (NumberFormatException e) {
			System.out.println("Positive learning rate passed is not valid.\n" + e);
			System.exit(0);
		}
		if(posLearningRate <= 0.0 || posLearningRate > 1.0) {
			System.out.println("Incorrect value passed for positive learning rate");
			System.exit(0);
		}

		//get the negative learning rate
		try {
			negLearningRate = (new Double(args[4])).doubleValue();
		} catch (NumberFormatException e) {
			System.out.println("Negative learning rate passed is not valid.\n" + e);
			System.exit(0);
		}
		if(negLearningRate <= 0.0 || negLearningRate > 1.0) {
			System.out.println("Incorrect value passed for negative learning rate");
			System.exit(0);
		}

		//get the mutation probability
		try {
			mutProb = (new Double(args[5])).doubleValue();
		} catch (NumberFormatException e) {
			System.out.println("Mutation probablitiy passed is not valid.\n" + e);
			System.exit(0);
		}
		if(mutProb <= 0.0 || mutProb > 1.0) {
			System.out.println("Incorrect value passed for mutation probability");
			System.exit(0);
		}

		//get mutation amount
		try {
			mutAmt = (new Double(args[6])).doubleValue();
		} catch (NumberFormatException e) {
			System.out.println("Mutation amount passed is not valid.\n" + e);
			System.exit(0);
		}
		if(mutAmt <= 0.0 || mutAmt > 1.0) {
			System.out.println("Incorrect value passed for mutation amount");
			System.exit(0);
		}

	}

	/** Another constructor
	 * Allows for all parameters to be set without putting them into a String[]
	 * @param _filename
	 * @param _nIteration
	 * @param _nIndividualsPerIteration
	 * @param _posLearningRate
	 * @param _negLearningRate
	 * @param _mutProb
	 * @param _mutAmt
	 */
	public MaxSatSolver(String _filename, int _nIteration, int _nIndividualsPerIteration, double _posLearningRate, double _negLearningRate, double _mutProb, double _mutAmt) {
		//get the SAT Formula from the file
		SatFormula = SATFileReader.getFormulaArray(_filename);

		//set up the probability vector
		//so that variable 1 corresponds to index 1 in the probability vector,
		//we will ignore probVector[0]
		probVector = new double[SATFileReader.nVariables + 1];
		//set all initial probabilities to .5
		for(int i = 1; i < probVector.length; i++) {
			probVector[i] = .5;
		}

		//assign the rest of the variables
		nIterations = _nIteration;
		nIndividualsPerIteration = _nIndividualsPerIteration;
		posLearningRate = _posLearningRate;
		negLearningRate = _negLearningRate;
		mutProb = _mutProb;
		mutAmt = _mutAmt;
	}

	public void solve() {

		//make the random number generator that we will use
		Random randomGen = new Random();

		//make one solution that we will ignore and initialize the localBest, localWorst, bestSolutionVector
		SolutionVector dummySolution = new SolutionVector(probVector, randomGen);
		//if we happen to come up with a perfect solution randomly, return
		//this means we have a constant best case running time - SWEET!
		if(dummySolution.evaluate(SatFormula)) {
			overallBestSolutionVector = dummySolution;
			System.out.println("CONSTANT RUNNING TIME FOR THE WIN!!!!!!!");
			return;
		}

		SolutionVector localBest = dummySolution;
		SolutionVector localWorst = dummySolution;
		overallBestSolutionVector = dummySolution;

		//go through the correct number of times
		for(int iteration = 0; iteration < nIterations; iteration++) {

			//populate the array and evaluate the solutions in it.  Pick out the local best and the worst
			for(int solutionIndex = 0; solutionIndex < nIndividualsPerIteration; solutionIndex++) {
				//generate a solution
				SolutionVector curSolution = new SolutionVector(probVector, randomGen);	

				//evaluate it.  If it satisfies all of the clauses, set it as the best solution and stop looking
				if(curSolution.evaluate(SatFormula)) {
					overallBestSolutionVector = curSolution;
					return;
				}

				//update the overall bestSolutionVector if the curSolution is better
				if(curSolution.getNumSatisfied() > overallBestSolutionVector.getNumSatisfied()) {
					overallBestSolutionVector = curSolution;
				}

				//if this is the first time through (i.e. solutionIndex = 0) then just set localBest and localWorst
				//otherwise, compare the curSolution to the localBest and localWorst, and update as needed
				if(curSolution.getNumSatisfied() > localBest.getNumSatisfied() || solutionIndex == 0) {
					localBest = curSolution;
				}
				if(curSolution.getNumSatisfied() < localWorst.getNumSatisfied() || solutionIndex == 0) {
					localWorst = curSolution;
				}
			}

			//using the local best and worst, change each variable in the probability vector 
			//<1-the learning rate>% of the probability is determined by the old probability, 
			//while <the learning rate>% of the new probability is determined by the value of the local solution 
			for(int curVariableIndex = 1; curVariableIndex < probVector.length; curVariableIndex++) {

				//update for the local best
				probVector[curVariableIndex] = probVector[curVariableIndex] * (1-posLearningRate) + 
				(localBest.getAssignment(curVariableIndex)?1:0) * posLearningRate;

				//update for the local worst
				if(useNegativeLearning) 
					probVector[curVariableIndex] = probVector[curVariableIndex] * (1-negLearningRate) +
					(localWorst.getAssignment(curVariableIndex)?0:1) * negLearningRate;
			}

			//mutate the probabilty vector
			for(int curVariableIndex = 1; curVariableIndex < probVector.length; curVariableIndex++) {
				//see if we should mutate or not based on a random number
				if(randomGen.nextDouble() < mutProb) {
					//we should mutate - see if we should increment or decrement
					if(randomGen.nextBoolean()) {
						probVector[curVariableIndex] += mutAmt;
					} else {
						probVector[curVariableIndex] -= mutAmt;
					}

					//if we accidentally moved the probability vector above 1.0 or below 0.0, fix it
					if(probVector[curVariableIndex] > .999) {
						probVector[curVariableIndex] = .999;
					}
					if(probVector[curVariableIndex] < .001) {
						probVector[curVariableIndex] = .001;
					}
				}
			}
		}

		//see if the probability vector gives us a better answer than the overall best that we found.
		SolutionVector probSolution = new SolutionVector(probVector);
		probSolution.evaluate(SatFormula);

		if(probSolution.getNumSatisfied() > overallBestSolutionVector.getNumSatisfied()) {
			System.out.println("Used solution from probability vector");
			overallBestSolutionVector = probSolution;
		}

	}

	/** Print out the best solution and other stats
	 */
	public void printBestSolution() {
		if(useNegativeLearning) {
			System.out.println("Using Negative Learning");
		} else {
			System.out.println("NOT using Negative Learning");
		}

		System.out.println("\nBEST SOLUTION:\n" + overallBestSolutionVector);

		System.out.println("\nSatisfied all but " + (SatFormula.length - overallBestSolutionVector.getNumSatisfied()) + " clauses");

		double optimalSolution = 1;

		System.out.println("assuming optimal solution of " + optimalSolution + " then we achieved " + (int)((overallBestSolutionVector.getNumSatisfied() / (SatFormula.length - optimalSolution))*100) + "% sucess rate.");
	}

	/**GETTERS!!!!!!*/
	public int getNumIterations() { return nIterations; }
	public int getNumIndividualsPerIteration() { return nIndividualsPerIteration; }
	public double getPosLearningRate() { return posLearningRate; }
	public double getNegLearingRate() { return negLearningRate; }
	public double getMutProb() { return mutProb; }
	public double getMutAmt() { return mutAmt; }

	/** Maine function
	 * Solved the MaxSAT problem based on the passed arguments
	 * @param args
	 */
	public static void main(String[] args) {

		//if we are not writing to the stat files, solve the solution passed.
		if(! WRITE_STATS_TO_FILES) {
			//make a MaxSAT solver
			MaxSatSolver maxSat = new MaxSatSolver(args);

			//keep track of the start time
			long startTime = System.currentTimeMillis();

			//solve the problem
			maxSat.solve();

			//store the stop time
			long stopTime = System.currentTimeMillis();

			//print out the result
			maxSat.printBestSolution();

			//print out the time
			System.out.println("Solve time: " + ((stopTime - startTime) / 1000.0) + " seconds");
		} else {
			//if we are writing to stat files, ignore command line args and just cycle through all posibilities.
			runAllVariableCombinations();
		}		
	}

	public static void runAllVariableCombinations() {

		final int defaultNumIteration = 2000;
		final int defaultNumIndividuals = 100;
		final double defaultPosLearningRate = 0.1;
		@SuppressWarnings("unused")
		final double defaultNegLearningRate = 0.075;
		final double defaultMutProb = 0.02;
		final double defaultMutAmt = 0.05;

		String filename = "";
		
		for(int i = 0; i < 3; i++) {
			
			for(int fileNumber = 0; fileNumber < 3; fileNumber ++) {

				//			filename = "/Users/Will/Documents/Schoolwork/Fall 09/AI/Homework/assign3-distributed/maxsat-problems/practice_problems/practice_3_5_A.cnf";

				switch(fileNumber) {
				case 0:
					filename = "/Users/Will/Documents/Schoolwork/Fall 09/AI/Homework/assign3-distributed/maxsat-problems/maxsat-random/highgirth/4SAT/HG-4SAT-V100-C900-1.cnf";
					break;
				case 1:
					filename = "/Users/Will/Documents/Schoolwork/Fall 09/AI/Homework/assign3-distributed/maxsat-problems/maxsat-random/highgirth/4SAT/HG-4SAT-V100-C900-3.cnf";
					break;
				case 2:
					filename = "/Users/Will/Documents/Schoolwork/Fall 09/AI/Homework/assign3-distributed/maxsat-problems/maxsat-random/highgirth/4SAT/HG-4SAT-V100-C900-5.cnf";
					break;
				}

				useNegativeLearning = true;

				//			for(int negLearning = 0; negLearning <=0; negLearning++) {
				//
				//				useNegativeLearning = negLearning == 0;
				//
				//				for(int numIteration = 20; numIteration <= 20000; numIteration *= 2) {
				//					System.out.println("num iterations = " + numIteration);
				//
				//					MaxSatSolver maxSat = new MaxSatSolver(filename, numIteration, defaultNumIndividuals, defaultPosLearningRate, defaultNegLearningRate, defaultMutProb, defaultMutAmt);
				//
				//					long startTime = System.currentTimeMillis();
				//
				//					maxSat.solve();
				//
				//					long stopTime = System.currentTimeMillis();
				//
				//					//if we are outputting stats, do it
				//					if(WRITE_STATS_TO_FILES) {
				//						//make the stat file filename
				//						String statFileLoc;
				//						if(useNegativeLearning) {
				//							statFileLoc = OUTPUT_FILE_LOCATION + filename.substring(filename.lastIndexOf('/')) + "_num_iterations.txt";
				//						} else {
				//							statFileLoc = OUTPUT_FILE_LOCATION + filename.substring(filename.lastIndexOf('/')) + "_num_iterations_no_neg_learning.txt";
				//						}
				//
				//						try {
				//							outputFile = new BufferedWriter(new FileWriter(statFileLoc, true));
				//							String outputMessage = (stopTime - startTime) + " " + maxSat.overallBestSolutionVector.getNumSatisfied() + " " + maxSat.nIterations + " " + maxSat.nIndividualsPerIteration + " " + maxSat.posLearningRate + " " + maxSat.negLearningRate + " " + maxSat.mutProb + " " + maxSat.mutAmt + "\n";
				//							outputFile.write(outputMessage);
				//							outputFile.flush();
				//						} catch (IOException e) {
				//							System.out.println("ERROR opening file:" + e);
				//							System.exit(0);
				//						}
				//					}
				//
				//					maxSat.printBestSolution();
				//
				//					System.out.println("Solve time: " + ((stopTime - startTime) / 1000.0) + " seconds");
				//					System.out.println("****************************\n");
				//
				//				}
				//				for(int numIndividualsPerIteration = 10; numIndividualsPerIteration <= 5000; numIndividualsPerIteration *= 2) {
				//					System.out.println("num individuals per iteration = "+ numIndividualsPerIteration);
				//
				//					MaxSatSolver maxSat = new MaxSatSolver(filename, defaultNumIteration, numIndividualsPerIteration, defaultPosLearningRate, defaultNegLearningRate, defaultMutProb, defaultMutAmt);
				//
				//					long startTime = System.currentTimeMillis();
				//
				//					maxSat.solve();
				//
				//					long stopTime = System.currentTimeMillis();
				//
				//					//if we are outputting stats, do it
				//					if(WRITE_STATS_TO_FILES) {
				//						//make the stat file filename
				//						String statFileLoc;
				//						if(useNegativeLearning) {
				//							statFileLoc = OUTPUT_FILE_LOCATION + filename.substring(filename.lastIndexOf('/')) + "_num_individuals.txt";
				//						} else {
				//							statFileLoc = OUTPUT_FILE_LOCATION + filename.substring(filename.lastIndexOf('/')) + "_num_individuals_no_neg_learning.txt";
				//						}
				//
				//						try {
				//							outputFile = new BufferedWriter(new FileWriter(statFileLoc, true));
				//							String outputMessage = (stopTime - startTime) + " " + maxSat.overallBestSolutionVector.getNumSatisfied() + " " + maxSat.nIterations + " " + maxSat.nIndividualsPerIteration + " " + maxSat.posLearningRate + " " + maxSat.negLearningRate + " " + maxSat.mutProb + " " + maxSat.mutAmt + "\n";
				//							outputFile.write(outputMessage);
				//							outputFile.flush();
				//						} catch (IOException e) {
				//							System.out.println("ERROR opening file:" + e);
				//							System.exit(0);
				//						}
				//					}
				//
				//					maxSat.printBestSolution();
				//
				//					System.out.println("Solve time: " + ((stopTime - startTime) / 1000.0) + " seconds");
				//					System.out.println("****************************\n");
				//
				//				}
				//				//for rates/probabilities, get more data for values below .1
				//				for(double positiveLearningRate = 0.0; positiveLearningRate <= .5; positiveLearningRate += .05) {
				//					System.out.println("pos learn rate = " + positiveLearningRate);
				//
				//					MaxSatSolver maxSat = new MaxSatSolver(filename, defaultNumIteration, defaultNumIndividuals, positiveLearningRate, defaultNegLearningRate, defaultMutProb, defaultMutAmt);
				//
				//					long startTime = System.currentTimeMillis();
				//
				//					maxSat.solve();
				//
				//					long stopTime = System.currentTimeMillis();
				//
				//					//if we are outputting stats, do it
				//					if(WRITE_STATS_TO_FILES) {
				//						//make the stat file filename
				//						String statFileLoc;
				//						if(useNegativeLearning) {
				//							statFileLoc = OUTPUT_FILE_LOCATION + filename.substring(filename.lastIndexOf('/')) + "_pos_learn_rate.txt";
				//						} else {
				//							statFileLoc = OUTPUT_FILE_LOCATION + filename.substring(filename.lastIndexOf('/')) + "_pos_learn_rate_no_neg_learning.txt";
				//						}
				//
				//						try {
				//							outputFile = new BufferedWriter(new FileWriter(statFileLoc, true));
				//							String outputMessage = (stopTime - startTime) + " " + maxSat.overallBestSolutionVector.getNumSatisfied() + " " + maxSat.nIterations + " " + maxSat.nIndividualsPerIteration + " " + maxSat.posLearningRate + " " + maxSat.negLearningRate + " " + maxSat.mutProb + " " + maxSat.mutAmt + "\n";
				//							outputFile.write(outputMessage);
				//							outputFile.flush();
				//						} catch (IOException e) {
				//							System.out.println("ERROR opening file:" + e);
				//							System.exit(0);
				//						}
				//					}
				//
				//					maxSat.printBestSolution();
				//
				//					System.out.println("Solve time: " + ((stopTime - startTime) / 1000.0) + " seconds");
				//					System.out.println("****************************\n");
				//
				//				}

				if(useNegativeLearning) {
					for(double negativeLearningRate = .001; negativeLearningRate < .1; negativeLearningRate += .002) {
						System.out.println("neg learn rate = "+ negativeLearningRate);

						MaxSatSolver maxSat = new MaxSatSolver(filename, defaultNumIteration, defaultNumIndividuals, defaultPosLearningRate, negativeLearningRate, defaultMutProb, defaultMutAmt);

						long startTime = System.currentTimeMillis();

						maxSat.solve();

						long stopTime = System.currentTimeMillis();

						//if we are outputting stats, do it
						if(WRITE_STATS_TO_FILES) {
							//make the stat file filename
							String statFileLoc;
							if(useNegativeLearning) {
								statFileLoc = OUTPUT_FILE_LOCATION + filename.substring(filename.lastIndexOf('/')) + "_neg_learn_rate.txt";
							} else {
								statFileLoc = OUTPUT_FILE_LOCATION + filename.substring(filename.lastIndexOf('/')) + "_neg_learn_rate_no_neg_learning.txt";
							}

							try {
								outputFile = new BufferedWriter(new FileWriter(statFileLoc, true));
								String outputMessage = (stopTime - startTime) + " " + maxSat.overallBestSolutionVector.getNumSatisfied() + " " + maxSat.nIterations + " " + maxSat.nIndividualsPerIteration + " " + maxSat.posLearningRate + " " + maxSat.negLearningRate + " " + maxSat.mutProb + " " + maxSat.mutAmt + "\n";
								outputFile.write(outputMessage);
								outputFile.flush();
							} catch (IOException e) {
								System.out.println("ERROR opening file:" + e);
								System.exit(0);
							}
						}

						maxSat.printBestSolution();

						System.out.println("Solve time: " + ((stopTime - startTime) / 1000.0) + " seconds");
						System.out.println("****************************\n");

					}
				}
				//				for(double mutationProb = 0.0; mutationProb <= .5; mutationProb += .05) {
				//					System.out.println("mutation prob = " + mutationProb);
				//
				//					MaxSatSolver maxSat = new MaxSatSolver(filename, defaultNumIteration, defaultNumIndividuals, defaultPosLearningRate, defaultNegLearningRate, mutationProb, defaultMutAmt);
				//
				//					long startTime = System.currentTimeMillis();
				//
				//					maxSat.solve();
				//
				//					long stopTime = System.currentTimeMillis();
				//
				//					//if we are outputting stats, do it
				//					if(WRITE_STATS_TO_FILES) {
				//						//make the stat file filename
				//						String statFileLoc;
				//						if(useNegativeLearning) {
				//							statFileLoc = OUTPUT_FILE_LOCATION + filename.substring(filename.lastIndexOf('/')) + "_mut_prob.txt";
				//						} else {
				//							statFileLoc = OUTPUT_FILE_LOCATION + filename.substring(filename.lastIndexOf('/')) + "_mut_prob_no_neg_learning.txt";
				//						}
				//
				//						try {
				//							outputFile = new BufferedWriter(new FileWriter(statFileLoc, true));
				//							String outputMessage = (stopTime - startTime) + " " + maxSat.overallBestSolutionVector.getNumSatisfied() + " " + maxSat.nIterations + " " + maxSat.nIndividualsPerIteration + " " + maxSat.posLearningRate + " " + maxSat.negLearningRate + " " + maxSat.mutProb + " " + maxSat.mutAmt + "\n";
				//							outputFile.write(outputMessage);
				//							outputFile.flush();
				//						} catch (IOException e) {
				//							System.out.println("ERROR opening file:" + e);
				//							System.exit(0);
				//						}
				//					}
				//
				//					maxSat.printBestSolution();
				//
				//					System.out.println("Solve time: " + ((stopTime - startTime) / 1000.0) + " seconds");
				//					System.out.println("****************************\n");
				//
				//				}
				//				for(double mutationAmt = 0.0; mutationAmt <= .1; mutationAmt += .01) {
				//					System.out.println("mutation amt = " + mutationAmt);
				//
				//					MaxSatSolver maxSat = new MaxSatSolver(filename, defaultNumIteration, defaultNumIndividuals, defaultPosLearningRate, defaultNegLearningRate, defaultMutProb, mutationAmt);
				//
				//					long startTime = System.currentTimeMillis();
				//
				//					maxSat.solve();
				//
				//					long stopTime = System.currentTimeMillis();
				//
				//					//if we are outputting stats, do it
				//					if(WRITE_STATS_TO_FILES) {
				//						//make the stat file filename
				//						String statFileLoc;
				//						if(useNegativeLearning) {
				//							statFileLoc = OUTPUT_FILE_LOCATION + filename.substring(filename.lastIndexOf('/')) + "_mut_amt.txt";
				//						} else {
				//							statFileLoc = OUTPUT_FILE_LOCATION + filename.substring(filename.lastIndexOf('/')) + "_mut_amt_no_neg_learning.txt";
				//						}
				//
				//						try {
				//							outputFile = new BufferedWriter(new FileWriter(statFileLoc, true));
				//							String outputMessage = (stopTime - startTime) + " " + maxSat.overallBestSolutionVector.getNumSatisfied() + " " + maxSat.nIterations + " " + maxSat.nIndividualsPerIteration + " " + maxSat.posLearningRate + " " + maxSat.negLearningRate + " " + maxSat.mutProb + " " + maxSat.mutAmt + "\n";
				//							outputFile.write(outputMessage);
				//							outputFile.flush();
				//						} catch (IOException e) {
				//							System.out.println("ERROR opening file:" + e);
				//							System.exit(0);
				//						}
				//					}
				//
				//					maxSat.printBestSolution();
				//
				//					System.out.println("Solve time: " + ((stopTime - startTime) / 1000.0) + " seconds");
				//
				//					System.out.println("****************************\n");
				//				}
				//
				//				try {
				//					outputFile.close();
				//				} catch (IOException e) {
				//					System.out.println("ERROR closing file: " + e);
				//				}
			}
		}
	}
}
