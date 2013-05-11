import java.util.Random;


public class NeuralNet {
	public final int NUM_INPUT_NODES = 64;
	public final int NUM_OUTPUT_NODES = 10;

	/*holds all the weights of all the layers.  
	 * If you consider the amount that j weights the activation passed to it from node i
	 * The first array specifies the layer number of node i number (assuming input nodes are at layer 0
	 * The 2nd array specifies the index of node i in its layer
	 * The 3rd array specifies the index of node j in its layer
	 * The vale is the weight that we are considering
	 */
	double[][][] weights;

	// the learning rate
	double alpha;

	//use or don't use bias node
	boolean useBiasNode;
	
	//the files to use
	private DigitReader testFile;
	private DigitReader trainFile;

	//Using the Sigmoid
	public static double transitionFunction(double input) {
		return 1/(1+Math.exp(-1*input));
	}
	public static double derivitiveOfTheTransitionFunction(double input) {
		return transitionFunction(input)*(1-transitionFunction(input));
	}

	/** Builds a 4 layer neural net.
	 * @param _alpha: the learning rate
	 * @param numHiddenNodesInFirstLayer: the number of nodes in the first hidden layer.
	 * @param numHiddenNodesInSecondLayer: the number of nodes in the second hidden layer.  If 0, do not make a second hidden layer, and make a 3 layer neural net.
	 */
	public NeuralNet(double _alpha, int numHiddenNodesInFirstLayer, int numHiddenNodesInSecondLayer, boolean _useBiasNode, String trainFilenname, String testFilename) {
		//test variables passed and complain if they are incorrect
		if(_alpha <= 0.0 || _alpha > 1.0 || numHiddenNodesInFirstLayer <= 0) {
			System.out.println("Invalid arguments");
			System.out.println("alpha = "+ _alpha);
			System.out.println("numNodesInFirstLayer = "+ numHiddenNodesInFirstLayer);
			System.exit(0);
		}
		
		alpha = _alpha;
		useBiasNode = _useBiasNode;
		
		//instantiate the weights array - there are 2 sets of weights for a 3 layer net and 3 sets of weights for a 4 layer net
		if(numHiddenNodesInSecondLayer <= 0 ) weights = new double[2][][];
		else weights = new double [3][][];
		
		int numBiasNodes = 0;
		if(useBiasNode) numBiasNodes = 1;
		
		weights[0] = new double[NUM_INPUT_NODES][numHiddenNodesInFirstLayer+numBiasNodes];
		
		if(numHiddenNodesInSecondLayer <= 0) {
			//do a 3 layer neural net
			weights[1] = new double[numHiddenNodesInFirstLayer+numBiasNodes][NUM_OUTPUT_NODES];
		} else {
			weights[1] = new double[numHiddenNodesInFirstLayer+numBiasNodes][numHiddenNodesInSecondLayer+numBiasNodes];
			weights[2] = new double[numHiddenNodesInSecondLayer+numBiasNodes][NUM_OUTPUT_NODES];
		}
		//initialize weights to .5
		Random randomGen = new Random(1234567);
		for(int layerNum = 0; layerNum < weights.length; layerNum++) {
			for(int i = 0; i < weights[layerNum].length; i++) {
				for(int j = 0; j < weights[layerNum][i].length; j++) {
					weights[layerNum][i][j] = randomGen.nextDouble();
				}
			}
		}
		
		//open the files with the digit reader
		trainFile = new DigitReader(trainFilenname);
		testFile = new DigitReader(testFilename);
	}

	public void train() {
		//this array will store all the activations from each layer to the next
		double[][] activations = new double[weights.length+1][];
		//this array will store the errors for each node
		double[][] errors = new double[weights.length+1][];
		for(int i = 0; i < weights.length; i++) {
			activations[i] = new double[weights[i].length];
			errors[i] = new double[weights[i].length];
		}
		activations[weights.length] = new double[NUM_OUTPUT_NODES];
		errors[weights.length] = new double[NUM_OUTPUT_NODES];

		for(int testNumber = 0; testNumber < trainFile.getNumExamples(); testNumber++) {
			//get the input nodes' activations from the file
			activations[0] = trainFile.getInput(testNumber);

			//go through all the layers, passing the input in and storing the output
			for(int layerNumber = 1; layerNumber < activations.length; layerNumber++) {
				//go through all the nodes in the layer
				for(int nodeNum = 0; nodeNum < activations[layerNumber].length; nodeNum++) {
					//calculate the weighted sum of the activations from the previous
					double sum = 0.0;
					for(int inputNum = 0; inputNum < activations[layerNumber-1].length; inputNum++) {
						sum += activations[layerNumber-1][inputNum] * weights[layerNumber-1][inputNum][nodeNum];
					}
					//store the result from the transition function evaluated on the sum
					activations[layerNumber][nodeNum] = transitionFunction(sum);
				}
				//if we are use bias nodes, and we are not in an output layer
				//set the first node's activation to 1, so that it is the bias node 
				if(useBiasNode && layerNumber < activations.length-1) {
					activations[layerNumber][0] = 1.0;
				}
			}
			//we have the all the activations stored at all of the layers
			//use the output value and back propagation to adjust the weights
			//get the output values from the file
			double[] outputs = trainFile.getOutput(testNumber);
			//calculate the errors for the output nodes
			for(int nodeNum = 0; nodeNum < activations[activations.length-1].length; nodeNum++) {
				errors[activations.length-1][nodeNum] = outputs[nodeNum] - activations[activations.length-1][nodeNum];
			}
			//calculate all the rest of the errors based on the errors of the output nodes
			for(int layerNum = activations.length-2; layerNum >=0; layerNum--) {
				for(int nodeNum = 0; nodeNum < activations[layerNum].length; nodeNum++) {
					double sum = 0;
					for(int errorNodeNum = 0; errorNodeNum < activations[layerNum+1].length; errorNodeNum++) {
						double weight = weights[layerNum][nodeNum][errorNodeNum];
						double error = errors[layerNum+1][errorNodeNum];
						sum += weight*error*derivitiveOfTheTransitionFunction(activations[layerNum+1][errorNodeNum]);
					}
					errors[layerNum][nodeNum] = sum;
				}
			}

			//adjust the weights
			for(int layerNum = 0; layerNum < activations.length-1; layerNum++) {
				for(int givingNode = 0; givingNode < activations[layerNum].length; givingNode++) {
					for(int takingNode = 0; takingNode < activations[layerNum+1].length; takingNode++) {
						double oldWeight = weights[layerNum][givingNode][takingNode];
						double givenValue = activations[layerNum][givingNode];
						double error = errors[layerNum+1][takingNode];
						weights[layerNum][givingNode][takingNode] = oldWeight + alpha*givenValue*error*derivitiveOfTheTransitionFunction(activations[layerNum+1][takingNode]);
					}
				}
			}
		}		
	}

	//returns the average error on the test set where the error on a test item is the Euclidean distance between the 
	//target vector and the actual output vector
	public double test() {
		//start the sum of all the errors
		double overallErrorSum = 0;

		//this array will store all the activations from each layer to the next
		double[][] activations = new double[weights.length+1][];
		for(int i = 0; i < weights.length; i++) {
			activations[i] = new double[weights[i].length];
		}
		activations[weights.length] = new double[NUM_OUTPUT_NODES];

		//go though all the tests in the test file
		for(int testNumber = 0; testNumber < testFile.getNumExamples(); testNumber++) {
			//get the input nodes' activations from the file
			activations[0] = testFile.getInput(testNumber);

			//go through all the layers, passing the input in and storing the output
			for(int layerNumber = 1; layerNumber < activations.length; layerNumber++) {
				//go through all the nodes in the layer
				for(int nodeNum = 0; nodeNum < activations[layerNumber].length; nodeNum++) {
					//calculate the weighted sum of the activations from the previous
					double sum = 0.0;
					for(int inputNum = 0; inputNum < activations[layerNumber-1].length; inputNum++) {
						sum += activations[layerNumber-1][inputNum] * weights[layerNumber-1][inputNum][nodeNum];
					}
					//store the result from the transition function evaluated on the sum
					activations[layerNumber][nodeNum] = transitionFunction(sum);
				}
				//if we are use bias nodes, and we are not in an output layer
				//set the first node's activation to 1, so that it is the bias node 
				if(useBiasNode && layerNumber < activations.length-1) {
					activations[layerNumber][0] = 1.0;
				}
			}

			//compute the squared error for this test
			//get the output values from the file
			double[] outputs = testFile.getOutput(testNumber);
			//calculate the errors for the output nodes
			double testErrorSum = 0;
			for(int nodeNum = 0; nodeNum < activations[activations.length-1].length; nodeNum++) {
				//add the difference of the errors squared to the sum
				double errorDiff = outputs[nodeNum] - activations[activations.length-1][nodeNum];
				testErrorSum += Math.pow(errorDiff, 2);
			}
			overallErrorSum += Math.sqrt(testErrorSum);			
		}

		return overallErrorSum / testFile.getNumExamples();
	}

	public static void main (String[] args) {
		/*args will hold, in this order, the learning rate, the number of nodes in the first hidden layer,
		* the number of nodes in the 2nd hidden layer, the training Filenname and the testing filename, and the number of epochs to run
		*/
		if(args.length != 7) {
			System.out.println("USAGE:");
			System.out.println("NeuralNet <learning rate> <# of nodes in the first hidden layer> <number of nodes in the 2nd hidden layer or 0 for a 3 layer net> <true to use bias nodes, false to not use bias nodes> <the location of the training file> <the location of the testing file> <the number of epochs to run>");
			System.exit(0);
		}
		
		//make the neural net
		NeuralNet net = new NeuralNet(Double.valueOf(args[0]), Integer.valueOf(args[1]), Integer.valueOf(args[2]), Boolean.valueOf(args[3]), args[4], args[5]);
		
		//start training and testing
		int numberOfEpochs = Integer.valueOf(args[6]);
		for(int epochNum = 0; epochNum < numberOfEpochs; epochNum++) {
			System.out.println("starting epoch " + epochNum);
			net.train();
			System.out.println("Done training");
			System.out.println("Error for epoch " + epochNum + " = " + net.test());
		}
	}
}

