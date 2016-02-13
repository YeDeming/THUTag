import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

public class InsertTag {
	public static void main(String[] args) {
		try {
			String outputFile = "/home/cxx/smt/DoubanWordList/musicWordList.txt";
			// String inputFile = "/home/cxx/smt/musicTag.vcb";
			String inputFile = "/home/cxx/smt/DoubanWordList/musicWordList";
			BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(inputFile), "UTF-8"));
			BufferedWriter out = new BufferedWriter(
					new OutputStreamWriter(new FileOutputStream(outputFile, true), "UTF-8"));
			String line;
			while ((line = in.readLine()) != null) {
				// String [] datas = line.split(" ");
				// out.write(datas[1]);
				out.write(line);
				out.newLine();
				out.flush();
			}
			out.flush();
			out.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}
}
