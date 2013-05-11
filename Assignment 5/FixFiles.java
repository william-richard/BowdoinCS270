import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Vector;

public class FixFiles {
	public static void main(String[] args) {
		//args[0] is the directory to fix
		String dirname = args[0];

		File file = new File(dirname); 

		if(file.isDirectory()){

			System.out.println("Directory is  " + dirname);

			String str[] = file.list();

			Vector<String> fileStorage;
			
			for( int i = 0; i<str.length; i++){
				fileStorage = new Vector<String>();
				File f=new File(dirname + str[i]);
				if(f.isDirectory()){
					System.out.println(str[i] + " is a directory");
				}

				else{
					System.out.println(str[i] + " is a file");
					if(str[i].equals(".DS_Store")) continue;
					//get the alpha and N values from the filename
					Double alpha = new Double(str[i].substring(1, 5));
					Integer numNodes = new Integer(str[i].substring(str[i].indexOf('N')+1, str[i].lastIndexOf('0')+1));
					System.out.println("alpha = "+ alpha + " numNodes = " + numNodes);
					
					//get all the lines from the file
					
					try {
						//read everything from the file, and make the new lines that we want with the alpha and numNodes
						BufferedReader br = new BufferedReader(new FileReader(f.getPath()));
						while(br.ready()) {
							String nextLine = br.readLine();
							nextLine.trim();
							fileStorage.add(nextLine + " " + alpha + " " + numNodes);
						}
						//write it to the file
						BufferedWriter bw = new BufferedWriter(new FileWriter(f.getPath()));
						for(String s : fileStorage) {
							bw.write(s);
							bw.newLine();
						}
						bw.close();
					} catch(FileNotFoundException e) {
						System.out.println("File Not Found!");
						e.printStackTrace();
					} catch(IOException e) {
						System.out.println("IOException!");
						e.printStackTrace();
					}
				}
			}
		}
		else{
			System.out.println(dirname  + " is not a directory");
		}
	}
}
