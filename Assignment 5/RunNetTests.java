import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class RunNetTests {


	public static final double ALPHA_START = .02;
	public static final double ALPHA_END = 1.0;
	public static final double ALPHA_INC = .02;
	public static final int LAYER1NODES_START = 20;
	public static final int LAYER1NODES_END = 200;
	public static final int LAYER1NODES_INC = 20;
	public static final int NUM_EPOCH = 100;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if(args.length != 3) {
			System.out.println("USAGE:");
			System.out.println("java runNetTests <train file location> <test file location> <output files location>");
			System.exit(0);
		}

		for(double alpha = ALPHA_START; alpha <= ALPHA_END; alpha+= ALPHA_INC) {
			for(int numNodes = LAYER1NODES_START; numNodes <=LAYER1NODES_END; numNodes += LAYER1NODES_END) {
				NeuralNet net = new NeuralNet(alpha, numNodes, 0, true, args[0], args[1]);

				try {
					BufferedWriter outputFile = new BufferedWriter(new FileWriter(args[2]+"A"+alpha+"_"+"N"+numNodes+".txt"));
					
					long startTime = System.currentTimeMillis();
					
					for(int epochNum = 0; epochNum < 100; epochNum++) {
						net.train();
						String outputMessage = epochNum + " " + net.test() + " " + (System.currentTimeMillis() - startTime) + "\n";
						System.out.print(outputMessage);
						outputFile.write(outputMessage);
						outputFile.flush();
					}
					outputFile.close();
				} catch (IOException e) {
					System.out.println("ERROR opening file: " + e);
					System.exit(0);
				}
			}
		}
	}

}
