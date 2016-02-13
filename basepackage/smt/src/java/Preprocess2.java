import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.HashSet;
import java.util.regex.Pattern;

import org.thunlp.io.JsonUtil;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class Preprocess2 {
	public static void main(String[] args) {
		int counter = 0;
		int id = 0;
		try {
			Gson g = (new GsonBuilder()).disableHtmlEscaping().create();
			// JsonUtil J = new JsonUtil();

			String inputFile = "/media/work/datasets(secret)/douban/raw/subject_cxx.dat";
			String indexFile = "/home/cxx/smt/index";
			String tagFile = "/media/work/datasets(secret)/douban/raw/tag_subject_cxx.dat";
			String outputFile = "/home/cxx/smt/sample/bookPost.dat";

			BufferedReader index = new BufferedReader(new InputStreamReader(new FileInputStream(indexFile), "UTF-8"));

			BufferedReader input = new BufferedReader(new InputStreamReader(new FileInputStream(inputFile), "UTF-8"));

			BufferedReader tagInput = new BufferedReader(new InputStreamReader(new FileInputStream(tagFile), "UTF-8"));

			BufferedWriter output = new BufferedWriter(
					new OutputStreamWriter(new FileOutputStream(outputFile), "UTF-8"));

			// Read index
			HashSet<Integer> indexSet = new HashSet<Integer>();
			String indexLine;
			while ((indexLine = index.readLine()) != null)
				indexSet.add(Integer.parseInt(indexLine));
			index.close();

			String inputLine, tagLine;
			tagLine = tagInput.readLine();

			while ((inputLine = input.readLine()) != null) {
				Doc subject = g.fromJson(inputLine, Doc.class);
				id = subject.id;
				if ((subject.cat_id == 1001) && indexSet.contains(id)) {
					// LOG.info("content: " + content);
					DoubanPost p = new DoubanPost();
					p.setId(Long.toString(id));
					p.setTimestamp(0L);
					p.setTitle(subject.title);
					p.setContent(subject.description);
					p.setUserId("");
					p.setExtras("");

					boolean readTag = false;
					do {
						TargetDoc tDoc = g.fromJson(tagLine, TargetDoc.class);
						if (tDoc.subject_id == id) {
							// p.getTags().add(tag);
							// tDoc.tag = re.matcher(tDoc.tag).replaceAll("");
							p.getDoubanTags().put(tDoc.tag, tDoc.count);
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
					output.flush();
					output.newLine();
					counter++;
					if (counter % 10000 == 0)
						System.out.println("lines:" + counter);
				}
			}
			input.close();
			tagInput.close();
			output.close();
		} catch (Exception e) {
			System.out.println(counter + " " + id);
			e.printStackTrace();
		}
	}
}
