import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.Map.Entry;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class Test {
	public static void main(String[] args) {
		try {
			Gson g = (new GsonBuilder()).disableHtmlEscaping().create();
			String outputFile = "/home/cxx/smt/source";
			BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputFile), "UTF-8"));
			BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(outputFile), "UTF-8"));
			TestJson t = new TestJson();
			t.tags.put("a", 3);
			t.tags.put("b", 3);
			t.tags.put("c", 3);
			t.tags.put("d", 3);
			out.write(g.toJson(t));
			out.newLine();
			out.flush();

			TestJson t1 = new TestJson();
			t1.tags.put("d", 3);
			t1.tags.put("c", 3);
			t1.tags.put("b", 3);
			t1.tags.put("a", 3);
			out.write(g.toJson(t1));
			out.newLine();
			out.flush();
			out.close();

			String line;
			while ((line = in.readLine()) != null) {
				TestJson tj = g.fromJson(line, TestJson.class);
				for (Entry<String, Integer> e : tj.tags.entrySet()) {
					System.out.print(e.getKey() + ":" + e.getValue() + " ");
				}
				System.out.println();
			}
			in.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}
}

class TestJson {
	HashMap<String, Integer> tags;
	String name;

	public TestJson() {
		tags = new HashMap<String, Integer>();
	}
}
