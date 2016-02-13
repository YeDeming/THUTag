import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.regex.*;

import org.thunlp.io.GzipTextFileReader;
import org.thunlp.language.chinese.ForwardMaxWordSegment;
import org.thunlp.language.chinese.WordSegment;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class MyJson {
	public static void main(String[] args) {
		try {
			WordSegment ws = new ForwardMaxWordSegment();
			String inputFile = "/media/work/datasets(secret)/douban/raw/subject.dat";
			String outputFile1 = "/home/cxx/smt/book";
			String outputFile2 = "/home/cxx/smt/movie";
			BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(inputFile), "UTF-8"));
			BufferedWriter out1 = new BufferedWriter(
					new OutputStreamWriter(new FileOutputStream(outputFile1), "UTF-8"));
			BufferedWriter out2 = new BufferedWriter(
					new OutputStreamWriter(new FileOutputStream(outputFile2), "UTF-8"));
			Gson g = (new GsonBuilder()).disableHtmlEscaping().create();
			String line;
			while ((line = in.readLine()) != null) {
				Doc doc = g.fromJson(line, Doc.class);
				String content = doc.title + "," + doc.description;
				// content.replaceAll("\\r\\n|\\n|\\r", " ");
				// content.replace('\n', ' ');
				String reg = "[\n-\r]";
				Pattern p = Pattern.compile(reg);
				Matcher m = p.matcher(content);
				String newContent = m.replaceAll("");
				String[] words = ws.segment(newContent);
				if (doc.cat_id == 1001) {
					for (int i = 0; i < words.length - 1; i++) {
						out1.write(words[i] + " ");
					}
					out1.write(words[words.length - 1]);
					out1.newLine();
					out1.flush();
				} else {
					for (int i = 0; i < words.length - 1; i++) {
						out2.write(words[i] + " ");
					}
					out2.write(words[words.length - 1]);
					out2.newLine();
					out2.flush();
				}
			}
			in.close();
			out1.close();
			out2.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}
}

class Doc {
	public int cat_id;
	public String description;
	public int id;
	public String title;
}