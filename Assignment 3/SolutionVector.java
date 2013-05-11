import java.util.Random;


public class SolutionVector {

	//the assignments of each of the variables
	private boolean[] assignments;
	//the number of clauses it satisfied
	private int numSatisfied;
	
	/**
	 * Basic Constructor
	 * @param probVector: the probability vector
	 * @param randomGen: the random number generator
	 */
	public SolutionVector(double[] probVector, Random randomGen) {
		//set up the assignments array
		assignments = new boolean[probVector.length];
		
		//go through each variable in the assignment and based on the probability vector, come up with an assignment for that variable 
		for(int i = 1; i < assignments.length; i++) {
			assignments[i] = randomGen.nextDouble() < probVector[i];
		}
	}
	
	/**
	 * Constructor that just uses the probability vector.
	 * If the probability of an assignment being true is > 50%, then set it true.
	 * Otherwise, set it false.
	 * @param probVector: the probability vector
	 */
	public SolutionVector(double[] probVector) {
		//set up the assignments array
		assignments = new boolean[probVector.length];

		//go through all the variables and set them according to the probability vector
		for(int i = 1; i < assignments.length; i++) {
			if(probVector[i] > .5) assignments[i] = true;
			else assignments[i] = false;
		}
	}
		
	/**evaluates this solution based on the passed SAT Formula.
	 * @param SatFormula
	 * @return true if this solution satisfies all of the clauses
	 */
	public boolean evaluate(int[][] SatFormula) {
		//start with numSatisfed = 0
		numSatisfied = 0;
		
		//go through all the clauses and evaluate each one
		for(int clauseIndex = 0; clauseIndex < SatFormula.length; clauseIndex++) {
			for(int variableIndex = 0; variableIndex < SatFormula[clauseIndex].length; variableIndex++) {
				int curVariable = SatFormula[clauseIndex][variableIndex];
				//if the current variable is negative, check if our assignment of that variable is false
				if(curVariable < 0) {
					if(! assignments[-1 * curVariable]) {
						numSatisfied++;
						break;
					}
				//otherwise, check if our assignment of the current variable is true
				} else {
					if( assignments[curVariable]) {
						numSatisfied++;
						break;
					}
				}

			}
		}
		
		//return if we have satisfied all of the clauses
		return numSatisfied == SatFormula.length;
		
	}
	
	/** getter for numSatisfied
	 * @return numSatisfied
	 */
	public int getNumSatisfied() {
		return numSatisfied;
	}
	
	/** getter for assignments
	 * @param i: the variable index
	 * @return the ith variable's value
	 */
	public boolean getAssignment(int i) {
		return assignments[i];
	}

	/** Equals method
	 * compares the assignments array.
	 */
	@Override
	public boolean equals(Object other) {
		//make "other" into a SolutionVector
		SolutionVector otherSolu;
		if(other instanceof SolutionVector)
			otherSolu = (SolutionVector) other;
		else return false;
		
		//return if the assignments are the same
		return this.assignments.equals(otherSolu.assignments);
	}
	
	/** toString
	 * print out each variable number and its assignment
	 */
	@Override
	public String toString() {
		String returnString = "";
	
		for(int i = 1; i < assignments.length; i++) {
			returnString += (assignments[i]?"":"-") + i + " ";
		}
		
		return returnString;
	}
	
}
