import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.thunlp.language.chinese.ForwardMaxWordSegment;
import org.thunlp.language.chinese.WordSegment;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class CalTfidf {
	public static void main(String[] args) {
		try {
			String inputFile = "/home/cxx/smt/book";
			String indexFile = "/home/cxx/smt/index";
			String tagFile = "/home/cxx/smt/book.vcb";
			String outputFile = "/home/cxx/smt/bookTfidf";
			BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(inputFile), "UTF-8"));
			BufferedReader index = new BufferedReader(new InputStreamReader(new FileInputStream(indexFile), "UTF-8"));
			BufferedReader tag = new BufferedReader(new InputStreamReader(new FileInputStream(tagFile), "UTF-8"));
			BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputFile), "UTF-8"));

			// Read index
			HashSet<Integer> indexSet = new HashSet<Integer>();
			String indexLine;
			while ((indexLine = index.readLine()) != null)
				indexSet.add(Integer.parseInt(indexLine));
			index.close();

			// Read Tag
			String tagLine;
			int D = 330543;
			HashMap<String, Integer> df = new HashMap<String, Integer>();
			while ((tagLine = tag.readLine()) != null) {
				String[] datas = tagLine.split(" ");
				df.put(datas[1], Integer.parseInt(datas[2]));
			}
			tag.close();

			// Cal
			HashMap<String, Integer> tf = new HashMap<String, Integer>();
			Vector<String> tagStrings = new Vector<String>();
			String line;
			boolean first = true;
			while ((line = in.readLine()) != null) {
				String[] data = line.split(" ");
				for (int i = 0; i < data.length; i++) {
					if (tf.containsKey(data[i])) {
						int num = tf.get(data[i]);
						num++;
						tf.remove(data[i]);
						tf.put(data[i], num);
					} else {
						tf.put(data[i], 1);
						tagStrings.add(data[i]);
					}
				}
				out.write(tagStrings.elementAt(0) + " ");
				double tfidf = (tf.get(tagStrings.elementAt(0)) / data.length)
						* (Math.log((double) D / (double) df.get(tagStrings.elementAt(0))));
				out.write(String.valueOf(tfidf));
				for (int i = 1; i < tagStrings.size(); i++) {
					out.write(" " + tagStrings.elementAt(i) + " ");
					tfidf = (tf.get(tagStrings.elementAt(i)) / data.length)
							* (Math.log((double) D / (double) df.get(tagStrings.elementAt(i))));
					out.write(String.valueOf(tfidf));
				}
				out.newLine();
				out.flush();
				tagStrings.clear();
				tf.clear();
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
