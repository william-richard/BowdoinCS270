
public class State {

	//location
	private int x;
	private int y;
	//whether it has a key
	private boolean hasKey;
	//its power level
	private int powerLevel;
	//the reward this state gives
	private int reward;
	//it's Q values in an array
	//they are indexed by action value - if NORTH=0, then qValue[0] is the Q value of taking action NORTH from this state
	private double[] qValue = {0.0, 0.0, 0.0, 0.0};
	//store whether this state is a terminal state
	private boolean isTerminalState;
	//store whether this state has been used i.e. the q values have been adjusted
	private boolean hasBeenUsed = false;
	
	/*******************************************************************************
	 * CONSTRUCTORS
	 * Pass all the information about a state - location, whether we have a key, the power level, the reward and if it is a terminal state
	 *******************************************************************************/
	public State(int _x, int _y, boolean _hasKey, int _powerLevel, int _reward, boolean _isTerminalState) {
		x = _x;
		y = _y;
		hasKey = _hasKey;
		powerLevel = _powerLevel;
		reward = _reward;
		isTerminalState = _isTerminalState;
	}

	/******************************************************************************
	 * GETTERS
	 ******************************************************************************/	
	/**
	 * @return the x
	 */
	public int getX() {
		return x;
	}

	/**
	 * @return the y
	 */
	public int getY() {
		return y;
	}

	/**
	 * @return the hasKey
	 */
	public boolean hasKey() {
		return hasKey;
	}

	/**
	 * @return the powerLevel
	 */
	public int getPowerLevel() {
		return powerLevel;
	}

	/**
	 * @return the reward
	 */
	public int getReward() {
		return reward;
	}

	/**
	 * @return the qValue of index <index>
	 */
	public double getQValue(int index) {
		return qValue[index];
	}
	
	/**
	 * @return the isTerminalState
	 */
	public boolean isTerminalState() {
		return isTerminalState;
	}

	/**
	 * @return the hasBeenUsed
	 */
	public boolean hasBeenUsed() {
		return hasBeenUsed;
	}

	/**
	 * @return the utility of this state.  In other words, the maximum Q value
	 */
	public double getUtility() {
		double maxValue = qValue[0];
		for(int i = 1; i < qValue.length; i++) {
			if(qValue[i] > maxValue) 
				maxValue = qValue[i];
		}
		return maxValue;
	}
	
	/**
	 * @return the best action to take i.e. the action with the highest Q value
	 */
	public int getBestAction() {
		//start by assuming that the best action is at index 0
		int bestAction = 0;
		double bestActionQValue = qValue[bestAction];
		
		//go through the rest of the q values, and find the max one
		for(int i = 0; i < qValue.length; i++) {
			if(qValue[i] > bestActionQValue) {
				bestAction = i;
				bestActionQValue = qValue[i];
			}
		}
		
		//return the action corresponding to the highest Q value
		return bestAction;
	}
	/**
	 * setter for a Q value - just for adjusting Q values
	 * @param qValue the qValue to set
	 * @param index the index of which to set the qValue
	 */
	private void setQValue(double newQValue, int index) {
		qValue[index] = newQValue;
	}
	
	/**
	 * @param action the action we're taking
	 * @param learningRate the learning rate 
	 * @param discountFactor the discount factor
	 * @param nextState the next state that we moved to by taking <action>
	 */
	public void updateQValue(State nextState, int action, double learningRate, double discountFactor) {
		//store that this state has been used
		hasBeenUsed = true;
		
		//calculate the new Q value
		double newQValue;
		//if the state is a terminal state, instead of using the Utility, use the reward
		if(nextState.isTerminalState()) {
			newQValue = qValue[action] + learningRate*(getReward() + discountFactor*nextState.getReward() - qValue[action]);
		} else { //otherwise, use the utility in the calculation
			newQValue = qValue[action] + learningRate*(getReward() + discountFactor*nextState.getUtility() - qValue[action]);
		}

		//set the new Q value
		setQValue(newQValue, action);
	}
	
	/** (non-Javadoc)
	 * @see java.lang.Object#toString()
	 * In this case though, print out all of the stored variables, except for hasBeenUsed
	 */
	@Override
	public String toString() {
		return "(" + x + ", " + y+ ")" + "" +"\tHas key? " + hasKey + "\tPower Level= " + powerLevel + "\tTerminal? " + isTerminalState;  
	}
	
	/**
	 * Print out all the Q values for this state
	 */
	public void printQValues() {
		System.out.println("Q values for '" + this + "':");
		for(int i = 0; i < qValue.length; i++) {
			System.out.println("\t" + i + ": " + qValue[i]);
		}
	}
}
