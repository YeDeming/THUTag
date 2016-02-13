package org.thunlp.io;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import junit.framework.*;

public class TestTextFileReader extends TestCase {

	public static String TESTFILE = "src/test/org/thunlp/io/textfile.txt";
	public static String BIGTESTFILE = "src/test/org/thunlp/io/bigtextfile.txt";

	public void testFileReader() throws IOException {
		String[] strs = { "blabla", "中文的话" };
		int i = 0;
		TextFileReader r = new TextFileReader(TESTFILE);
		String l;
		while ((l = r.readLine()) != null) {
			Assert.assertEquals(strs[i++], l);
		}
		r.close();
	}

	public void testReadAll() throws IOException {
		String content = "blabla\n中文的话";
		String read = TextFileReader.readAll(TESTFILE, "UTF-8");
		System.out.println(read);
		Assert.assertEquals(content, read);
	}

	public void testBigFile() throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(BIGTESTFILE), "UTF-8"));
		String l;
		long start = System.currentTimeMillis();
		StringBuilder sb = new StringBuilder();
		while ((l = br.readLine()) != null) {
			sb.append(l + "\n");
		}
		br.close();
		String standard = sb.toString();
		long brTime = System.currentTimeMillis() - start;
		start = System.currentTimeMillis();
		String result = TextFileReader.readAll(BIGTESTFILE, "UTF-8");
		long tfrTime = System.currentTimeMillis() - start;
		Assert.assertEquals(standard, result);
		// System.out.println(tfrTime + " " + brTime);
		Assert.assertTrue(tfrTime < brTime);
	}
}
