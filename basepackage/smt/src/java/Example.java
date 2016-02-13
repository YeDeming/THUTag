import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import org.thunlp.language.chinese.*;

public class Example {
	public static void main(String[] argv) {
		try {
			WordSegment ws = new ForwardMaxWordSegment();
			String inputFile = "/home/cxx/smt/chinese";
			BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(inputFile), "GBK"));
			BufferedWriter out = new BufferedWriter(
					new OutputStreamWriter(new FileOutputStream(inputFile + ".split"), "GBK"));
			String s;
			int counter = 0;
			while ((s = in.readLine()) != null) {
				String[] words = ws.segment(s);
				for (int i = 0; i < words.length - 1; i++) {
					out.write(words[i] + " ");
				}
				out.write(words[words.length - 1]);
				out.newLine();
				out.flush();
			}
			in.close();
			out.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}
}
