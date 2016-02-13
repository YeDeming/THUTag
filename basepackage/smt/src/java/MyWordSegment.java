import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.thunlp.language.chinese.ForwardMaxWordSegment;
import org.thunlp.language.chinese.WordSegment;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class MyWordSegment {
	public static void main(String[] args) {
		try {
			String sourceFile = "/media/work/datasets(secret)/douban/raw/subject.dat";
			String indexFile = "/home/cxx/smt/index";
			String outputFile = "/home/cxx/smt/music";
			BufferedReader source = new BufferedReader(new InputStreamReader(new FileInputStream(sourceFile), "UTF-8"));
			BufferedReader index = new BufferedReader(new InputStreamReader(new FileInputStream(indexFile), "UTF-8"));

			BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputFile), "UTF-8"));

			Gson g = (new GsonBuilder()).disableHtmlEscaping().create();

			HashSet<Integer> indexSet = new HashSet<Integer>();
			String indexLine;
			while ((indexLine = index.readLine()) != null)
				indexSet.add(Integer.parseInt(indexLine));
			index.close();

			int id;
			WordSegment ws = new ForwardMaxWordSegment();
			String sourceLine;
			while ((sourceLine = source.readLine()) != null) {
				Doc doc = g.fromJson(sourceLine, Doc.class);
				id = doc.id;
				if ((doc.cat_id == 1003) && (indexSet.contains(id))) {
					String content = doc.title + "," + doc.description;
					String reg = "[\n-\r]";
					Pattern p = Pattern.compile(reg);
					Matcher m = p.matcher(content);
					String newContent = m.replaceAll("");
					String[] words = ws.segment(newContent);
					for (int i = 0; i < words.length - 1; i++) {
						out.write(words[i] + " ");
					}
					out.write(words[words.length - 1]);
					out.newLine();
					out.flush();
				}
			}
			source.close();
			out.close();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}
}
