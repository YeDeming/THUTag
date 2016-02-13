package org.thunlp.io;

import java.io.IOException;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.apache.hadoop.io.Text;

public class JsonUtilTest extends TestCase {
	public static class PlainObject {
		private String line;
		private int value;

		public void setLine(String line) {
			this.line = line;
		}

		public String getLine() {
			return line;
		}

		public void setValue(int value) {
			this.value = value;
		}

		public int getValue() {
			return value;
		}
	}

	public void testToAndFromString() throws IOException {
		JsonUtil j = new JsonUtil();

		PlainObject po = new PlainObject();
		po.setLine("shie<a href=\"ddd\">alsidhfw</a>");
		po.setValue(1213);

		String json = j.toJson(po);
		System.out.println(json);
		PlainObject po1 = j.fromJson(json, PlainObject.class);
		Assert.assertEquals(po1.line, po.line);
		Assert.assertEquals(po1.value, po.value);
	}

	public void testToAndFromText() throws IOException {
		JsonUtil j = new JsonUtil();
		PlainObject po = new PlainObject();
		po.setLine("shie");
		po.setValue(1213);

		Text text = new Text();
		j.toTextAsJson(po, text);
		PlainObject po1 = j.fromTextAsJson(text, PlainObject.class);
		Assert.assertEquals(po1.line, po.line);
		Assert.assertEquals(po1.value, po.value);
	}

	public void testToAndFromTextWithLargeData() throws IOException {
		JsonUtil j = new JsonUtil();
		PlainObject po = new PlainObject();
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < 1024; i++)
			sb.append("orz");
		po.setLine(sb.toString());
		po.setValue(1213);

		Text text = new Text();
		j.toTextAsJson(po, text);
		PlainObject po1 = j.fromTextAsJson(text, PlainObject.class);
		Assert.assertEquals(po1.line, po.line);
		Assert.assertEquals(po1.value, po.value);
	}
}
