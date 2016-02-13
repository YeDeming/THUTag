import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.HashSet;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.thunlp.language.chinese.ForwardMaxWordSegment;
import org.thunlp.language.chinese.WordSegment;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class CleanData {
	public static void main(String[] args) {
		try {
			String sourceFile = "/media/work/datasets(secret)/douban/raw/subject_cxx.dat";
			String targetFile = "/media/work/datasets(secret)/douban/raw/tag_subject_cxx.dat";
			String indexFile = "/home/cxx/smt/index";
			String outputFile = "/media/work/datasets(secret)/douban/raw/subject_cxx2.dat";

			BufferedReader source = new BufferedReader(new InputStreamReader(new FileInputStream(sourceFile), "UTF-8"));
			BufferedReader target = new BufferedReader(new InputStreamReader(new FileInputStream(targetFile), "UTF-8"));
			BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputFile), "UTF-8"));
			Gson g = (new GsonBuilder()).disableHtmlEscaping().create();

			BufferedWriter outIndex = new BufferedWriter(
					new OutputStreamWriter(new FileOutputStream(indexFile), "UTF-8"));
			HashSet<Integer> sourceIndex = new HashSet<Integer>();
			HashSet<Integer> targetIndex = new HashSet<Integer>();
			HashSet<Integer> index = new HashSet<Integer>();
			String sLine, tLine;
			while ((sLine = source.readLine()) != null) {
				sourceIndex.add(g.fromJson(sLine, Doc.class).id);
			}
			while ((tLine = target.readLine()) != null) {
				targetIndex.add(g.fromJson(tLine, TargetDoc.class).subject_id);
			}
			Iterator it = sourceIndex.iterator();
			while (it.hasNext()) {
				int i = (Integer) it.next();
				if (targetIndex.contains(i)) {
					index.add(i);
					outIndex.write(Integer.toString(i));
					outIndex.newLine();
					outIndex.flush();
				}
			}
			outIndex.close();
			source.close();
			target.close();
			/*
			 * Pattern toRemoveRE = Pattern.compile(
			 * "(简介|制片国家/地区|导演|影名|编剧|简体中文名|官方网站|上映日期|" +
			 * "语言|imdb编号|主演|又名|出版|编者|编着|开本|版次|原定价|定价):"); Pattern enterRE =
			 * Pattern.compile("[\t\n-\r]");
			 */
			source = new BufferedReader(new InputStreamReader(new FileInputStream(sourceFile), "UTF-8"));
			while ((sLine = source.readLine()) != null) {
				Doc doc = g.fromJson(sLine, Doc.class);
				if (index.contains(doc.id)) {
					/*
					 * doc.title = enterRE.matcher(doc.title).replaceAll("");
					 * String content = doc.description; content =
					 * toRemoveRE.matcher(content).replaceAll(""); content =
					 * enterRE.matcher(content).replaceAll(""); doc.description
					 * = content;
					 */
					out.write(g.toJson(doc));
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
