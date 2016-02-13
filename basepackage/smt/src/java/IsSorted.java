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

public class IsSorted {
	public static void main(String[] args) {
		try {
			String sourceFile = "/media/work/datasets(secret)/douban/raw/subject.dat";
			String targetFile = "/media/work/datasets(secret)/douban/raw/tag_subject.dat";
			String indexFile = "/home/cxx/smt/index";
			BufferedReader source = new BufferedReader(new InputStreamReader(new FileInputStream(sourceFile), "UTF-8"));
			BufferedReader target = new BufferedReader(new InputStreamReader(new FileInputStream(targetFile), "UTF-8"));
			Gson g = (new GsonBuilder()).disableHtmlEscaping().create();

			BufferedWriter outIndex = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(indexFile)));

			HashSet<Integer> sourceIndex = new HashSet<Integer>();
			HashSet<Integer> targetIndex = new HashSet<Integer>();
			HashSet<Integer> index = new HashSet<Integer>();
			String sLine, tLine;
			int preId, curId;
			preId = curId = 1000001;
			boolean first = true;
			while ((sLine = source.readLine()) != null) {
				curId = g.fromJson(sLine, Doc.class).id;
				if ((curId < preId) && first) {
					System.out.println("Source File is not sorted!");
					first = false;
				}
				preId = curId;
				sourceIndex.add(curId);
			}
			if (first)
				System.out.println("Source File is sorted!");
			preId = curId = 1000001;
			first = true;
			while ((tLine = target.readLine()) != null) {
				curId = g.fromJson(tLine, TargetDoc.class).subject_id;
				if ((curId < preId) && first) {
					System.out.println("Target File is not sorted!");
					first = false;
				}
				preId = curId;
				targetIndex.add(curId);
				if (sourceIndex.contains(curId))
					index.add(curId);
			}
			if (first)
				System.out.println("Target File is sorted!");
			Iterator it = index.iterator();
			while (it.hasNext()) {
				String i = ((Integer) it.next()).toString();
				outIndex.write(i);
				outIndex.newLine();
				outIndex.flush();
			}
			outIndex.close();
			source.close();
			target.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}
}
