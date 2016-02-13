package org.thunlp.language.chinese;

import junit.framework.Assert;
import junit.framework.TestCase;

public class IctclasWordSegmentTest extends TestCase {
	public void testBasicSegment() {
		String text = "清华大学人工智能实验室自然语言处理小组";
		String[] standard = { "清华大学", "人工智能", "实验室", "自然", "语言", "处理", "小组" };
		WordSegment ws = new IctclasWordSegment("lib/ictclas");
		System.out.println(System.getProperty("user.dir"));
		String[] result = ws.segment(text);
		Assert.assertTrue(ws.outputPosTag());
		Assert.assertNotNull(result);
		Assert.assertEquals(standard.length, result.length);
		for (int i = 0; i < standard.length; i++) {
			Assert.assertEquals(standard[i], result[i]);
		}
	}

	public void testPostagSegment() {
		String text = "清华大学人工智能实验室自然语言处理小组";
		String[] standard = { "清华大学/nt", "人工智能/n", "实验室/n", "自然/a", "语言/n", "处理/v", "小组/n" };
		IctclasWordSegment ws = new IctclasWordSegment("lib/ictclas");
		System.out.println(System.getProperty("user.dir"));
		String[] result = ws.segmentWithPostag(text);
		Assert.assertTrue(ws.outputPosTag());
		Assert.assertNotNull(result);
		Assert.assertEquals(standard.length, result.length);
		for (int i = 0; i < standard.length; i++) {
			Assert.assertEquals(standard[i], result[i]);
		}
	}

	public void testPostagFilterSegment() {
		String text = "清华大学人工智能实验室自然语言处理小组";
		String[] standard = { "人工智能", "实验室", "语言", "处理", "小组" };
		String filter = "n|v";
		IctclasWordSegment ws = new IctclasWordSegment("lib/ictclas");
		System.out.println(System.getProperty("user.dir"));
		String[] result = ws.segmentWithPostagFilter(text, filter);
		Assert.assertTrue(ws.outputPosTag());
		Assert.assertNotNull(result);
		Assert.assertEquals(standard.length, result.length);
		for (int i = 0; i < standard.length; i++) {
			Assert.assertEquals(standard[i], result[i]);
		}
	}

}
