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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.hadoop.mapred.ID;
import org.thunlp.language.chinese.ForwardMaxWordSegment;
import org.thunlp.language.chinese.WordSegment;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class Tag {
	public static void main(String[] args) {
		try {
			String sourceFile = "/media/work/datasets(secret)/douban/raw/subject.dat";
			String targetFile = "/media/work/datasets(secret)/douban/raw/tag_subject.dat";
			String indexFile = "/home/cxx/smt/index";
			String outputFile1 = "/home/cxx/smt/bookTag";
			String outputFile2 = "/home/cxx/smt/movieTag";
			String outputFile3 = "/home/cxx/smt/musicTag";
			String outputFile4 = "/home/cxx/smt/book";
			String outputFile5 = "/home/cxx/smt/movie";
			String outputFile6 = "/home/cxx/smt/music";
			BufferedReader source = new BufferedReader(new InputStreamReader(new FileInputStream(sourceFile), "UTF-8"));
			BufferedReader target = new BufferedReader(new InputStreamReader(new FileInputStream(targetFile), "UTF-8"));
			BufferedReader index = new BufferedReader(new InputStreamReader(new FileInputStream(indexFile), "UTF-8"));

			BufferedWriter[] outTag = new BufferedWriter[3];
			outTag[0] = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputFile1), "UTF-8"));
			outTag[1] = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputFile2), "UTF-8"));
			outTag[2] = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputFile3), "UTF-8"));
			Gson g = (new GsonBuilder()).disableHtmlEscaping().create();

			HashSet<Integer> indexSet = new HashSet<Integer>();
			String indexLine;
			while ((indexLine = index.readLine()) != null)
				indexSet.add(Integer.parseInt(indexLine));
			index.close();

			String targetLine;
			boolean firstBook = true;
			boolean firstMovie = true;
			boolean firstMusic = true;
			int preId = 1000001;
			int curId = 1000001;
			int id = 1000001;
			int pos = 0;
			while ((targetLine = target.readLine()) != null) {
				TargetDoc tDoc = g.fromJson(targetLine, TargetDoc.class);
				id = tDoc.subject_id;
				if (indexSet.contains(id)) {
					String reg = "[ \n-\r]";
					Pattern p = Pattern.compile(reg);
					Matcher m = p.matcher(tDoc.tag);
					String content = m.replaceAll("");
					switch (tDoc.cat_id) {
					case 1001:
						if (firstBook) {
							firstBook = false;
							outTag[0].write(content);
							preId = curId = id;
							pos = 0;
							continue;
						}
						curId = id;
						if (curId == preId) {
							outTag[0].write(" " + content);
							preId = curId;
							pos = 0;
						} else {
							outTag[pos].newLine();
							outTag[pos].flush();
							outTag[0].write(content);
							preId = curId;
							pos = 0;
						}
						break;
					case 1002:
						if (firstMovie) {
							firstMovie = false;
							outTag[pos].newLine();
							outTag[pos].flush();
							outTag[1].write(content);
							preId = curId = id;
							pos = 1;
							continue;
						}
						curId = id;
						if (curId == preId) {
							outTag[1].write(" " + content);
							preId = curId;
							pos = 1;
						} else {
							outTag[pos].newLine();
							outTag[pos].flush();
							outTag[1].write(content);
							preId = curId;
							pos = 1;
						}
						break;
					case 1003:
						if (firstMusic) {
							firstMusic = false;
							outTag[pos].newLine();
							outTag[pos].flush();
							outTag[2].write(content);
							preId = curId = id;
							pos = 2;
							continue;
						}
						curId = id;
						if (curId == preId) {
							outTag[2].write(" " + content);
							preId = curId;
							pos = 2;
						} else {
							outTag[pos].newLine();
							outTag[pos].flush();
							outTag[2].write(content);
							preId = curId;
							pos = 2;
						}
						break;
					default:
						System.out.println("Error!Unknown Type!");
					}
				}
			}
			outTag[pos].newLine();
			outTag[pos].flush();
			outTag[0].close();
			outTag[1].close();
			outTag[2].close();
			target.close();
			/*
			 * outTag[0] = new BufferedWriter( new OutputStreamWriter(new
			 * FileOutputStream(outputFile4),"UTF-8")); outTag[1] = new
			 * BufferedWriter( new OutputStreamWriter(new
			 * FileOutputStream(outputFile5),"UTF-8")); outTag[2] = new
			 * BufferedWriter( new OutputStreamWriter(new
			 * FileOutputStream(outputFile6),"UTF-8")); WordSegment ws = new
			 * ForwardMaxWordSegment(); String sourceLine; while((sourceLine =
			 * source.readLine())!= null){ Doc doc = g.fromJson(sourceLine,
			 * Doc.class); id = doc.id; if(indexSet.contains(id)){ String
			 * content = doc.title + "," + doc.description; String reg
			 * ="[\n-\r]"; Pattern p = Pattern.compile(reg); Matcher m =
			 * p.matcher(content); String newContent = m.replaceAll(""); String
			 * [] words = ws.segment(newContent); switch (doc.cat_id) { case
			 * 1001: for(int i = 0;i < words.length - 1; i ++){
			 * outTag[0].write(words[i]+" "); }
			 * outTag[0].write(words[words.length - 1]); outTag[0].newLine();
			 * outTag[0].flush(); break; case 1002: for(int i = 0;i <
			 * words.length - 1; i ++){ outTag[1].write(words[i]+" "); }
			 * outTag[1].write(words[words.length - 1]); outTag[1].newLine();
			 * outTag[1].flush(); break; case 1003: for(int i = 0;i <
			 * words.length - 1; i ++){ outTag[2].write(words[i]+" "); }
			 * outTag[2].write(words[words.length - 1]); outTag[2].newLine();
			 * outTag[2].flush(); break; default: System.out.println(
			 * "Unknown Type!"); } } } source.close(); outTag[0].close();
			 * outTag[1].close(); outTag[2].close();
			 */
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}
}

class TargetDoc {
	public int count;
	public int cat_id;
	public String tag;
	public int subject_id;
}