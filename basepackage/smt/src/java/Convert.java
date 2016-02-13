import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

public class Convert {
	public static void main(String[] args) {
		try {
			String sourceFile = "/home/cxx/workspace/copy/2010-12-13.212638.cxx.actual.ti.final";
			String inputFile = "/home/cxx/workspace/copy/2010-12-13.212638.cxx.t1.5";
			String outputFile = "/home/cxx/workspace/copy/2010-12-13.212638.cxx.actual.t1.5";
			BufferedReader source = new BufferedReader(new InputStreamReader(new FileInputStream(sourceFile), "UTF-8"));
			BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(inputFile), "UTF-8"));
			BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputFile), "UTF-8"));
			String line;
			String pro;
			while ((line = source.readLine()) != null) {
				pro = in.readLine();
				String[] data1 = line.split(" ");
				String[] data2 = pro.split(" ");
				out.write(data1[1] + " " + data1[0] + " " + data2[2]);
				out.newLine();
				out.flush();
			}
			source.close();
			in.close();
			out.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}
}
