import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.HashSet;
import java.util.Iterator;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class Count {
	public static void main(String[] args) {
		try {
			String sourceFile = "/media/work/datasets(secret)/douban/raw/subject_cxx.dat";
			String outputFile = "/home/cxx/smt/detail";

			BufferedReader source = new BufferedReader(new InputStreamReader(new FileInputStream(sourceFile), "UTF-8"));
			BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputFile), "UTF-8"));
			Gson g = (new GsonBuilder()).disableHtmlEscaping().create();

			String line;
			int total = 0;
			int book = 0;
			int movie = 0;
			int music = 0;
			while ((line = source.readLine()) != null) {
				Doc doc = g.fromJson(line, Doc.class);
				switch (doc.cat_id) {
				case 1001:
					book++;
					break;
				case 1002:
					movie++;
					break;
				case 1003:
					music++;
					break;
				default:
					System.out.println("Error Type");
				}
				total++;
			}
			source.close();
			out.write("total:" + total);
			out.newLine();
			out.write("book num:" + book);
			out.newLine();
			out.write("movie num:" + movie);
			out.newLine();
			out.write("music num:" + music);
			out.newLine();
			out.flush();
			out.close();
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}
}
