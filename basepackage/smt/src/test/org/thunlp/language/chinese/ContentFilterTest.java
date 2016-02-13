package org.thunlp.language.chinese;

import junit.framework.TestCase;
import junit.framework.Assert;

public class ContentFilterTest extends TestCase {
	private ContentFilter filter;

	public void setUp() {
		filter = new ContentFilter();
	}

	public void testFilterSpace() {
		String src = "  中文的空格     其中夹杂着\u000B\u000Bsome English words但是\n下一行的  空格　　中文空格";
		String standard = "中文的空格 其中夹杂着 some English words但是\n下一行的 空格 中文空格";
		String result = filter.filterExtraSpaces(src);
		Assert.assertEquals(standard, result);
	}

	public void testFilterEmptyLines() {
		String src = "line1\nline2\r\n\r\n\nline3\n\n\r\n";
		String standard = "line1\nline2\nline3\n";
		String result = filter.filterEmptyLines(src);
		Assert.assertEquals(standard, result);
	}

	public void testFilterMarks() {
		String src = "中文,\"其中\"的各种：标点符号.。“”都出现了／～";
		String standard = "中文 其中 的各种 标点符号 都出现了";
		String result = filter.filterMarks(src);
		Assert.assertEquals(standard, result);
	}

	public void testMapMarks() {
		String src = "“这个程序”能把‘所有’的中文标点，都变换成ANSI的么？是的！";
		String standard = "\"这个程序\"能把'所有'的中文标点,都变换成ANSI的么?是的!";
		String result = filter.mapChineseMarksToAnsi(src);
		Assert.assertEquals(standard, result);
	}

	public void testFilterLineEnds() {
		String src = "line1\nline2\r\n\r\n\nline3\n\n\r\n";
		String standard = "line1 line2 line3";
		String result = filter.filterLineEnds(src);
		Assert.assertEquals(standard, result);
	}

	public void testNumbers() {
		String src = "国民生产总值达到100万!";
		String standard = "国民生产总值达到100万";
		String result = filter.filterMarks(src);
		Assert.assertEquals(standard, result);
	}
}
