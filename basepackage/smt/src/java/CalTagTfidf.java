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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class CalTagTfidf {
	public static void main(String[] args) {
		try {
			String inputFile = "/media/work/datasets(secret)/douban/raw/tag_subject.dat";
			String indexFile = "/home/cxx/smt/index";

			String tagFile1 = "/home/cxx/smt/bookTag.vcb";
			String tagFile2 = "/home/cxx/smt/movieTag.vcb";
			String tagFile3 = "/home/cxx/smt/musicTag.vcb";

			String outputFile1 = "/home/cxx/smt/bookTagTfidf";
			String outputFile2 = "/home/cxx/smt/movieTagTfidf";
			String outputFile3 = "/home/cxx/smt/musicTagTfidf";

			BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(inputFile), "UTF-8"));
			BufferedReader index = new BufferedReader(new InputStreamReader(new FileInputStream(indexFile), "UTF-8"));

			BufferedReader tag1 = new BufferedReader(new InputStreamReader(new FileInputStream(tagFile1), "UTF-8"));
			BufferedReader tag2 = new BufferedReader(new InputStreamReader(new FileInputStream(tagFile2), "UTF-8"));
			BufferedReader tag3 = new BufferedReader(new InputStreamReader(new FileInputStream(tagFile3), "UTF-8"));

			BufferedWriter[] out = new BufferedWriter[3];
			out[0] = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputFile1), "UTF-8"));
			out[1] = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputFile2), "UTF-8"));
			out[2] = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputFile3), "UTF-8"));

			// Read index
			HashSet<Integer> indexSet = new HashSet<Integer>();
			String indexLine;
			while ((indexLine = index.readLine()) != null)
				indexSet.add(Integer.parseInt(indexLine));
			index.close();

			// Read Tag
			String tagLine;
			int D[] = { 330543, 53358, 113938 };
			Vector<HashMap<String, Integer>> df = new Vector<HashMap<String, Integer>>();
			df.add(new HashMap<String, Integer>());
			df.add(new HashMap<String, Integer>());
			df.add(new HashMap<String, Integer>());
			while ((tagLine = tag1.readLine()) != null) {
				String[] datas = tagLine.split(" ");
				df.elementAt(0).put(datas[1], Integer.parseInt(datas[2]));
			}
			tag1.close();
			while ((tagLine = tag2.readLine()) != null) {
				String[] datas = tagLine.split(" ");
				df.elementAt(1).put(datas[1], Integer.parseInt(datas[2]));
			}
			tag2.close();
			while ((tagLine = tag3.readLine()) != null) {
				String[] datas = tagLine.split(" ");
				df.elementAt(2).put(datas[1], Integer.parseInt(datas[2]));
			}
			tag3.close();

			// Cal
			Gson g = (new GsonBuilder()).disableHtmlEscaping().create();
			HashMap<String, Integer> tf = new HashMap<String, Integer>();
			Vector<String> tagStrings = new Vector<String>();
			String line;
			boolean firstBook = true;
			boolean firstMovie = true;
			boolean firstMusic = true;
			int preId = 1000001;
			int curId = 1000001;
			int id = 1000001;
			int count = 0;
			int pos = 0;
			while ((line = in.readLine()) != null) {
				TargetDoc doc = g.fromJson(line, TargetDoc.class);
				id = doc.subject_id;
				if (indexSet.contains(id)) {
					String reg = "[\n-\r]";
					Pattern p = Pattern.compile(reg);
					Matcher m = p.matcher(doc.tag);
					String content = m.replaceAll("");
					switch (doc.cat_id) {
					case 1001:
						if (firstBook) {
							firstBook = false;
							preId = curId = id;
							tagStrings.add(content);
							tf.put(content, doc.count);
							count += doc.count;
							pos = 0;
							continue;
						}
						curId = id;
						if (curId == preId) {
							preId = curId;
							tagStrings.add(content);
							tf.put(content, doc.count);
							count += doc.count;
							pos = 0;
						} else {
							out[pos].write(tagStrings.elementAt(0) + " ");
							double tfidf = df.elementAt(pos).containsKey(tagStrings.elementAt(0))
									? ((double) tf.get(tagStrings.elementAt(0)) / (double) count) * (Math.log(
											(double) D[pos] / (double) df.elementAt(pos).get(tagStrings.elementAt(0))))
									: 0.0;
							out[0].write(String.valueOf(tfidf));
							for (int i = 1; i < tagStrings.size(); i++) {
								out[pos].write(" " + tagStrings.elementAt(i) + " ");
								tfidf = df.elementAt(pos).containsKey(tagStrings.elementAt(i))
										? ((double) tf.get(tagStrings.elementAt(i)) / (double) count)
												* (Math.log((double) D[pos]
														/ (double) df.elementAt(pos).get(tagStrings.elementAt(i))))
										: 0.0;
								out[pos].write(String.valueOf(tfidf));
							}
							out[pos].newLine();
							out[pos].flush();
							tagStrings.clear();
							tf.clear();
							preId = curId;
							tagStrings.add(content);
							tf.put(content, doc.count);
							count = doc.count;
							pos = 0;
						}
						break;
					case 1002:
						if (firstMovie) {
							firstMovie = false;
							preId = curId = id;
							tagStrings.add(content);
							tf.put(content, doc.count);
							count += doc.count;
							pos = 1;
							continue;
						}
						curId = id;
						if (curId == preId) {
							preId = curId;
							tagStrings.add(content);
							tf.put(content, doc.count);
							count += doc.count;
							pos = 1;
						} else {
							out[pos].write(tagStrings.elementAt(0) + " ");
							double tfidf = df.elementAt(pos).containsKey(tagStrings.elementAt(0))
									? ((double) tf.get(tagStrings.elementAt(0)) / (double) count) * (Math.log(
											(double) D[pos] / (double) df.elementAt(pos).get(tagStrings.elementAt(0))))
									: 0.0;
							out[0].write(String.valueOf(tfidf));
							for (int i = 1; i < tagStrings.size(); i++) {
								out[pos].write(" " + tagStrings.elementAt(i) + " ");
								tfidf = df.elementAt(pos).containsKey(tagStrings.elementAt(i))
										? ((double) tf.get(tagStrings.elementAt(i)) / (double) count)
												* (Math.log((double) D[pos]
														/ (double) df.elementAt(pos).get(tagStrings.elementAt(i))))
										: 0.0;
								out[pos].write(String.valueOf(tfidf));
							}
							out[pos].newLine();
							out[pos].flush();
							tagStrings.clear();
							tf.clear();
							preId = curId;
							tagStrings.add(content);
							tf.put(content, doc.count);
							count = doc.count;
							pos = 1;
						}
						break;
					case 1003:
						if (firstMusic) {
							firstMusic = false;
							preId = curId = id;
							tagStrings.add(content);
							tf.put(content, doc.count);
							count += doc.count;
							pos = 2;
							continue;
						}
						curId = id;
						if (curId == preId) {
							preId = curId;
							tagStrings.add(content);
							tf.put(content, doc.count);
							count += doc.count;
							pos = 2;
						} else {
							out[pos].write(tagStrings.elementAt(0) + " ");
							double tfidf = df.elementAt(pos).containsKey(tagStrings.elementAt(0))
									? ((double) tf.get(tagStrings.elementAt(0)) / (double) count) * (Math.log(
											(double) D[pos] / (double) df.elementAt(pos).get(tagStrings.elementAt(0))))
									: 0.0;
							out[0].write(String.valueOf(tfidf));
							for (int i = 1; i < tagStrings.size(); i++) {
								out[pos].write(" " + tagStrings.elementAt(i) + " ");
								tfidf = df.elementAt(pos).containsKey(tagStrings.elementAt(i))
										? ((double) tf.get(tagStrings.elementAt(i)) / (double) count)
												* (Math.log((double) D[pos]
														/ (double) df.elementAt(pos).get(tagStrings.elementAt(i))))
										: 0.0;
								out[pos].write(String.valueOf(tfidf));
							}
							out[pos].newLine();
							out[pos].flush();
							tagStrings.clear();
							tf.clear();
							preId = curId;
							tagStrings.add(content);
							tf.put(content, doc.count);
							count = doc.count;
							pos = 2;
						}
						break;
					default:
						System.out.println("Error!Unknown Type!");
					}
				}
			}
			out[pos].newLine();
			out[pos].flush();
			in.close();
			out[0].close();
			out[1].close();
			out[2].close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}
}
