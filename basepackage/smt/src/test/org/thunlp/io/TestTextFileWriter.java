package org.thunlp.io;

import java.io.IOException;

import junit.framework.*;

public class TestTextFileWriter extends TestCase {
	public void testWriter() throws IOException {
		TextFileWriter w = new TextFileWriter("text.txt");
		String ostr = "haha,and 中文";
		w.write(ostr);
		w.close();
		TextFileReader r = new TextFileReader("text.txt");
		String str = r.readLine();
		r.close();
		Assert.assertEquals(str, ostr);
	}
}
