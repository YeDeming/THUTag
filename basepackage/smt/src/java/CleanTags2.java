import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.HashSet;
import java.util.regex.Pattern;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class CleanTags2 {
	public static void main(String[] args) throws Exception {
		Gson g = (new GsonBuilder()).disableHtmlEscaping().create();

		String tagFile = "/media/work/datasets(secret)/douban/raw/tag_subject_cxx.dat";
		String indexFile = "/home/cxx/smt/index";
		String outputFile = "/media/work/datasets(secret)/douban/raw/tag_subject_cxx2.dat";

		BufferedReader index = new BufferedReader(new InputStreamReader(new FileInputStream(indexFile), "UTF-8"));
		BufferedReader tagInput = new BufferedReader(new InputStreamReader(new FileInputStream(tagFile), "UTF-8"));
		BufferedWriter output = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputFile), "UTF-8"));

		// Read index
		HashSet<Integer> indexSet = new HashSet<Integer>();
		String indexLine;
		while ((indexLine = index.readLine()) != null)
			indexSet.add(Integer.parseInt(indexLine));
		index.close();

		Pattern spaceRE = Pattern.compile("[\\\\]");

		String inputLine;
		long counter = 0;
		while ((inputLine = tagInput.readLine()) != null) {
			TargetDoc tDoc = g.fromJson(inputLine, TargetDoc.class);
			if (indexSet.contains(tDoc.subject_id) && (tDoc.count != 0)) {
				String tag = spaceRE.matcher(tDoc.tag).replaceAll("");
				if ((!tag.equals("")) && tag.length() < 20) {
					tDoc.tag = tag;
					String json = g.toJson(tDoc);
					// LOG.info("jsonL: " + json);
					output.write(json);
					output.flush();
					output.newLine();
					counter++;
					if (counter % 100000 == 0)
						System.out.println(counter);
				}
			}
		}
		System.out.println(counter);
		tagInput.close();
		output.close();
	}
}
