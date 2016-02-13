import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.HashSet;
import java.util.regex.Pattern;

import org.thunlp.io.JsonUtil;
import org.thunlp.io.RecordReader;
import org.thunlp.io.RecordWriter;
import org.thunlp.misc.Flags;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class Preprocess {
	public static void main(String[] args) throws Exception {
		Gson g = (new GsonBuilder()).disableHtmlEscaping().create();

		String inputFile = "/media/work/datasets(secret)/douban/raw/subject_cxx.dat";
		String indexFile = "/home/cxx/smt/index";
		String tagFile = "/media/work/datasets(secret)/douban/raw/tag_subject_cxx.dat";
		String outputFile = "/home/cxx/smt/100000/post.dat";

		BufferedReader index = new BufferedReader(new InputStreamReader(new FileInputStream(indexFile), "UTF-8"));

		BufferedReader input = new BufferedReader(new InputStreamReader(new FileInputStream(inputFile), "UTF-8"));

		BufferedReader tagInput = new BufferedReader(new InputStreamReader(new FileInputStream(tagFile), "UTF-8"));

		BufferedWriter output = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputFile), "UTF-8"));

		// Read index
		HashSet<Integer> indexSet = new HashSet<Integer>();
		String indexLine;
		while ((indexLine = index.readLine()) != null)
			indexSet.add(Integer.parseInt(indexLine));
		index.close();

		/*
		 * Pattern toRemoveRE = Pattern.compile(
		 * "(简介|制片国家/地区|导演|影名|编剧|简体中文名|官方网站|上映日期|" +
		 * "语言|imdb编号|主演|又名|出版|编者|编着|开本|版次|原定价|定价):"); Pattern enterRE =
		 * Pattern.compile("[\n-\r]"); Pattern spaceRE = Pattern.compile(
		 * "[ \n-\r]");
		 */

		String inputLine, tagLine;
		tagLine = tagInput.readLine();
		int counter = 0;
		while ((inputLine = input.readLine()) != null) {
			Doc subject = g.fromJson(inputLine, Doc.class);
			int id = subject.id;
			if (indexSet.contains(id) && (subject.cat_id == 1001)) {
				String content = subject.description;
				// LOG.info("content: " + content);
				/*
				 * content = toRemoveRE.matcher(content).replaceAll(""); content
				 * = enterRE.matcher(content).replaceAll("");
				 */

				Post p = new Post();
				p.setId(Long.toString(id));
				p.setTimestamp(0L);
				p.setTitle(subject.title);
				p.setContent(content);
				p.setUserId("");
				p.setExtras("");

				boolean readTag = false;
				do {
					TargetDoc tDoc = g.fromJson(tagLine, TargetDoc.class);
					if (tDoc.subject_id == id) {
						// String tag =
						// spaceRE.matcher(tDoc.tag).replaceAll("");
						p.getTags().add(tDoc.tag);
						readTag = true;
					} else {
						if (readTag == true) {
							break;
						}
					}
				} while ((tagLine = tagInput.readLine()) != null);

				// LOG.info("after: " + content);
				String json = g.toJson(p);
				// LOG.info("jsonL: " + json);
				output.write(json);
				output.newLine();
				output.flush();
				counter++;
				if (counter == 100000)
					break;
			}
		}
		input.close();
		tagInput.close();
		output.close();
	}
}
