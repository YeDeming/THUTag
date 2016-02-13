import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Map.Entry;

import org.thunlp.io.JsonUtil;

public class OutputForDemo {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		JsonUtil J = new JsonUtil();
		BufferedReader input = new BufferedReader(
				new InputStreamReader(new FileInputStream("/home/cxx/smt/sample/bookPost.dat"), "UTF-8"));
		BufferedWriter output = new BufferedWriter(
				new OutputStreamWriter(new FileOutputStream("/home/cxx/result/smt/bookPost.dat"), "UTF-8"));
		String line;
		int count = 0;
		while ((line = input.readLine()) != null) {
			count++;
			if (count % 5 != 1)
				continue;
			DoubanPost p = J.fromJson(line, DoubanPost.class);
			MyTag myTag = new MyTag();
			myTag.setTitle(p.getTitle());
			myTag.setContent(p.getContent());
			myTag.setDoubanTags(p.getDoubanTags());
			output.write(J.toJson(myTag));
			output.newLine();
			output.flush();
		}
		input.close();
		output.close();
	}

}
