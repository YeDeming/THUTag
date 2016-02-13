package org.thunlp.language.chinese;

import java.io.IOException;

import junit.framework.TestCase;

import org.thunlp.misc.StringUtil;

public class CRFWordSegmentTest extends TestCase {

	public void testSegment() throws IOException {
		String base = "/Users/sixiance/Documents/workspace/base/";
		System.setProperty("crfpp_path", base + "crfpp");
		System.setProperty("crfpp_model", base + "crfpp_model");
		System.setProperty("wordsegment.automata.file", "src/java/org/thunlp/language/chinese/lexicon.model");
		String text = "深圳市华动飞天网络技术开发有限公司  闪闪炫图地带  “MM美图之涩胆包天”是由神通Any8提供的手机图片下载wap服务。内容包括性感美女；适合作手机壁纸、屏保的风景、建筑、植物、动物、海洋卡通等。还新增了《快乐##本营》最新活动“冒险你最红”的活动事宜。";
		String text1 = "再测试一下";
		WordSegment ws = new CRFWordSegment();
		// WordSegment ws = new ForwardMaxWordSegment();
		String[] result = null;
		// String [] lines = TextFileReader.readAll(base +
		// "test.txt").split("[\r\n]+");
		System.out.println("start");
		long start = System.currentTimeMillis();
		double i = 0;
		String[] words = ws.segment(text);
		System.out.println(StringUtil.join(words, " "));
		words = ws.segment(text1);
		System.out.println(StringUtil.join(words, " "));
		/*
		 * for (String text : lines) { result = ws.segment(text); long now =
		 * System.currentTimeMillis(); if ((now - start) % 1000 == 0) {
		 * System.out.println("time:" + ((now - start) / i) + " " + i); } i++; }
		 * long end = System.currentTimeMillis(); System.out.println("time:" +
		 * (end - start) + " " + lines.length);
		 */
	}
}
