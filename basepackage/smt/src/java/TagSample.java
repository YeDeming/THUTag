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
import java.util.Random;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.crypto.Data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class TagSample {
	public static void main(String[] args) {
		try {
			String sourceFile = "/media/work/datasets(secret)/douban/raw/subject.dat";
			String targetFile = "/media/work/datasets(secret)/douban/raw/tag_subject.dat";
			String indexFile = "/home/cxx/smt/indexWithLength";
			String outputFile1 = "/home/cxx/smt/bookTagSample";
			String outputFile2 = "/home/cxx/smt/movieTagSample";
			String outputFile3 = "/home/cxx/smt/musicTagSample";
			BufferedReader source = new BufferedReader(new InputStreamReader(new FileInputStream(sourceFile), "UTF-8"));
			BufferedReader target = new BufferedReader(new InputStreamReader(new FileInputStream(targetFile), "UTF-8"));
			BufferedReader index = new BufferedReader(new InputStreamReader(new FileInputStream(indexFile), "UTF-8"));

			BufferedWriter[] outTag = new BufferedWriter[3];
			outTag[0] = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputFile1), "UTF-8"));
			outTag[1] = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputFile2), "UTF-8"));
			outTag[2] = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputFile3), "UTF-8"));
			Gson g = (new GsonBuilder()).disableHtmlEscaping().create();

			HashMap<Integer, Integer> indexMap = new HashMap<Integer, Integer>();
			String indexLine;
			while ((indexLine = index.readLine()) != null) {
				String[] data = indexLine.split(" ");
				indexMap.put(Integer.parseInt(data[0]), Integer.parseInt(data[1]));
			}
			index.close();

			String targetLine;
			Vector<String> words = new Vector<String>();
			Vector<Integer> pro = new Vector<Integer>();
			int preId = 1000001;
			int curId = 1000001;
			int id = 1000001;
			int pos = 0;
			int count = 0;
			Random random = new Random();
			while ((targetLine = target.readLine()) != null) {
				TargetDoc tDoc = g.fromJson(targetLine, TargetDoc.class);
				id = tDoc.subject_id;
				if (indexMap.containsKey(id)) {
					String reg = "[ \n-\r]";
					Pattern p = Pattern.compile(reg);
					Matcher m = p.matcher(tDoc.tag);
					String content = m.replaceAll("");
					switch (tDoc.cat_id) {
					case 1001:
						curId = id;
						if (curId == preId) {
							words.add(content);
							pro.add(tDoc.count);
							count += tDoc.count;
							preId = curId;
							pos = 0;
						} else {
							int loop = indexMap.get(preId);
							for (int i = 0; i < loop; i++) {
								int num = random.nextInt(count);
								int sum = 0;
								int j = 0;
								for (j = 0; j < pro.size(); j++) {
									sum += pro.elementAt(j);
									if (sum > num)
										break;
								}
								if (i == 0) {
									outTag[pos].write(words.elementAt(j));
								} else {
									outTag[pos].write(" " + words.elementAt(j));
								}
							}
							outTag[pos].newLine();
							outTag[pos].flush();
							words.clear();
							pro.clear();
							words.add(content);
							pro.add(tDoc.count);
							count = tDoc.count;
							preId = curId;
							pos = 0;
						}
						break;
					case 1002:
						curId = id;
						if (curId == preId) {
							words.add(content);
							pro.add(tDoc.count);
							count += tDoc.count;
							preId = curId;
							pos = 1;
						} else {
							int loop = indexMap.get(preId);
							for (int i = 0; i < loop; i++) {
								int num = random.nextInt(count);
								int sum = 0;
								int j = 0;
								for (j = 0; j < pro.size(); j++) {
									sum += pro.elementAt(j);
									if (sum > num)
										break;
								}
								if (i == 0) {
									outTag[pos].write(words.elementAt(j));
								} else {
									outTag[pos].write(" " + words.elementAt(j));
								}
							}
							outTag[pos].newLine();
							outTag[pos].flush();
							words.clear();
							pro.clear();
							words.add(content);
							pro.add(tDoc.count);
							count = tDoc.count;
							preId = curId;
							pos = 1;
						}
						break;
					case 1003:
						curId = id;
						if (curId == preId) {
							words.add(content);
							pro.add(tDoc.count);
							count += tDoc.count;
							preId = curId;
							pos = 2;
						} else {
							int loop = indexMap.get(preId);
							for (int i = 0; i < loop; i++) {
								int num = random.nextInt(count);
								int sum = 0;
								int j = 0;
								for (j = 0; j < pro.size(); j++) {
									sum += pro.elementAt(j);
									if (sum > num)
										break;
								}
								if (i == 0) {
									outTag[pos].write(words.elementAt(j));
								} else {
									outTag[pos].write(" " + words.elementAt(j));
								}
							}
							outTag[pos].newLine();
							outTag[pos].flush();
							words.clear();
							pro.clear();
							words.add(content);
							pro.add(tDoc.count);
							count = tDoc.count;
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
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}
}