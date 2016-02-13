import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Vector;

public class TagSuggest {
	public static void main(String[] args) {
		try {
			String vcbFile = "/home/cxx/smt/book.vcb";
			String proFile = "/media/work/result_forbooks/2010-09-15.132201.cxx.actual.t3.fianl";
			String inputFile = "/media/work/datasets(secret)/douban/raw/tag_subject.dat";
			String outputFile = "home/cxx/smt/bookSuggest";
			BufferedReader vcb = new BufferedReader(new InputStreamReader(new FileInputStream(vcbFile), "UTF-8"));
			BufferedReader pro = new BufferedReader(new InputStreamReader(new FileInputStream(proFile), "UTF-8"));
			BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(inputFile), "UTF-8"));
			BufferedReader out = new BufferedReader(new InputStreamReader(new FileInputStream(outputFile), "UTF-8"));

			Vector<HashMap<String, Double>> proVector = new Vector<HashMap<String, Double>>();
			HashMap<String, Integer> words = new HashMap<String, Integer>();
			int id = 0;
			String proLine;
			while ((proLine = pro.readLine()) != null) {
				String[] data = proLine.split(" ");
				if (words.size() != 3)
					continue;
				if (words.containsKey(data[0])) {
					proVector.elementAt(words.get(data[0])).put(data[1], Double.parseDouble(data[2]));
				} else {
					words.put(data[0], id);
					id++;
					proVector.add(new HashMap<String, Double>());
					proVector.lastElement().put(data[1], Double.parseDouble(data[2]));
				}
			}
			pro.close();

			HashMap<String, Integer> df = new HashMap<String, Integer>();
			String vcbLine;
			while ((vcbLine = vcb.readLine()) != null) {
				String[] data = vcbLine.split(" ");
				if (data.length != 2)
					continue;
				df.put(data[0], Integer.parseInt(data[1]));
			}
			vcb.close();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}
}
