import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Vector;


public class DigitReader {

	private final static int INPUT_ARRAY_SIZE = 64;
	private final static int OUTPUT_ARRAY_SIZE = 10;

	//store the input data
	//the first index is the example number, the 2nd is the array of values for that example
	private Vector<double[]> inputData;
	//store the ouput data
	//the first index is the example number, the 2nd is the array of output values for that example
	private Vector<double[]> outputData;
	//input reader
	BufferedReader digitFileReader;

	//reads the data from the passed fileName and fills the input and output arrays accordingly
	public DigitReader(String fileName) {
		//set up the vectors
		inputData = new Vector<double[]>();
		outputData = new Vector<double[]>();

		//open the file
		try {
			digitFileReader = new BufferedReader(new FileReader(fileName));
		} catch (FileNotFoundException e) {
			System.out.println("Invalid file passed to DigitReader: " + e);
			System.exit(0);
		}

		//keep reading until file is empty
		String nextInputLine;
		String nextOutputLine;
		try {
			while((nextInputLine = digitFileReader.readLine()) !=null) {
				//we have a valid input line - if we get a valid output line, read them
				if((nextOutputLine = digitFileReader.readLine()) != null) {
					readNextPair(nextInputLine, nextOutputLine);
				} else {
					//we've reached the end of the file trying te get an output line
					//this shouldn't happen, but whatever
					break;
				}
			}
		} catch (IOException e) {
			System.out.println("Error reading file " + fileName);
			System.out.println(e);
		}

		//done reading stuff - close up the file
		try {
			digitFileReader.close();
		} catch (IOException e) {
			System.out.println("ERROR closing file: " + e);
		}
		
	}

	//given a file, this reads an input-output pair and fills the next entry of the Vectors
	private void readNextPair(String nextInputLine, String nextOutputLine) {
		//make the double arrays that will hold everything
		double[] inputArray = new double[INPUT_ARRAY_SIZE];
		double[] outputArray = new double [OUTPUT_ARRAY_SIZE];
		
		//read through the input line and fill the array
		//make sure the first character is a '('
		if(nextInputLine.charAt(0) != '(') {
			System.out.println("Input Line does not start with a '('");
			System.exit(0);
		}
		
		int startIndex = 1; //*SHOULD* be the first space
		int endIndex = nextInputLine.indexOf(' ', startIndex+1);
		String nextSubstring;
		for(int i = 0; i < INPUT_ARRAY_SIZE; i++) {
			nextSubstring = nextInputLine.substring(startIndex, endIndex);
			inputArray[i] = Double.valueOf(nextSubstring);
			startIndex = endIndex;
			endIndex = nextInputLine.indexOf(' ', startIndex+1);
		}
		
		//do the same for the output line
		//make sure the first character is a '('
		if(nextOutputLine.charAt(0) != '(') {
			System.out.println("Output Line does not start with a '('");
			System.exit(0);
		}
		
		startIndex = 1; //*SHOULD* be the first space
		endIndex = nextOutputLine.indexOf(' ', startIndex+1);
		for(int i = 0; i < OUTPUT_ARRAY_SIZE; i++) {
			nextSubstring = nextOutputLine.substring(startIndex, endIndex);
			outputArray[i] = Double.valueOf(nextSubstring);
			startIndex = endIndex;
			endIndex = nextOutputLine.indexOf(' ', startIndex+1);
		}
		
		//add the input and output arrays to the Vectors
		inputData.add(inputArray);
		outputData.add(outputArray);
		
		
	}


	//returns the array of input values of the passed example number
	public double[] getInput(int exampleNum) {
		//check if it is a valid example number
		if(exampleNum <0 || exampleNum > inputData.size()) {
			throw new IndexOutOfBoundsException("Invalid input value passed: " + exampleNum);
		}
		return inputData.get(exampleNum);
	}
	
	//returns the array of output values of the passed example number
	public double[] getOutput(int exampleNum) {
		//check if it is a valid example number
		if(exampleNum <0 || exampleNum > outputData.size()) {
			throw new IndexOutOfBoundsException("Invalid output value passed: " + exampleNum);
		}
		return outputData.get(exampleNum);
	}
	
	public int getNumExamples() {
		return inputData.size();
	}

	/*******************
	 * JUST FOR TESTING
	 * @param args
	 */
	public static void main(String args[]) {
		DigitReader r = new DigitReader("/Users/Will/Documents/Schoolwork/Fall 09/AI/Homework/assign5-distributed/digit-recognition-examples/reader_example.txt");
		
		double[] input1 = r.getInput(0);
		double[] output1 = r.getOutput(0);
		double[] input2 = r.getInput(1);
		double[] output2 = r.getOutput(1);
		
		for(int i = 0; i < input1.length; i++)
			System.out.print(input1[i] + " ");
		System.out.println("");
		for(int i = 0; i < output1.length; i++)
			System.out.print(output1[i] + " ");
		System.out.println("\n******");
		for(int i = 0; i < input2.length; i++)
			System.out.print(input2[i] + " ");
		System.out.println("");
		for(int i = 0; i < output2.length; i++)
			System.out.print(output2[i] + " ");

		
	}
}
