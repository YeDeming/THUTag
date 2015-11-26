package org.thunlp.tagsuggest.dataset;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.HashMap;

import org.thunlp.io.JsonUtil;
import org.thunlp.language.chinese.ForwardMaxWordSegment;
import org.thunlp.language.chinese.WordSegment;
import org.thunlp.misc.WeightString;
import org.thunlp.tagsuggest.common.KeywordPost;

public class KeywordDetail {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		if (args.length != 2) {
			System.out.println("Usage:\njava -jar KeywordDetail.jar <input_file> <output_file>");
			return;
		}
		BufferedReader input = new BufferedReader(new InputStreamReader(
				new FileInputStream(args[0])));
		BufferedWriter outLine = new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream(args[1]+"line")));
		List<WeightString> list = new ArrayList<WeightString>();
		JsonUtil J = new JsonUtil();
		String line;
		WordSegment ws = new ForwardMaxWordSegment();
		int totalLen = 0;
		int totalWord = 0;
		int keyNum = 0;
		int counter = 0;
		int titleLen = 0;
		int summaryLen = 0;
		int titleWord = 0;
		int summaryWord = 0;
		while((line = input.readLine()) != null){
			KeywordPost p = J.fromJson(line, KeywordPost.class);
			totalLen += p.getTitle().length() + p.getSummary().length() + p.getContent().length();
			
			titleLen += p.getTitle().length();
			summaryLen += p.getSummary().length();
			String allContent = p.getTitle()+" " + p.getSummary()+" "+p.getContent();
			String[] words = ws.segment(allContent);
			totalWord += words.length;
			String[] titleWords = ws.segment(p.getTitle());
			titleWord += titleWords.length;
			
			String[] summaryWords = ws.segment(p.getSummary());
			summaryWord += summaryWords.length;
			
			keyNum += p.getTags().size();
			counter ++;
			list.add(new WeightString(p.getId(), p.getTitle().length()
					+ p.getSummary().length() + p.getContent().length()));
		}
		BufferedWriter out = new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream(args[1])));
		double avgLen = (double)totalLen / (double) counter;
		double avgWord = (double)totalWord / (double) counter;
		double avgNum = (double)keyNum / (double) counter;
		out.write("total line"+counter);
		out.newLine();
		out.write("average length:"+avgLen);
		out.newLine();
		out.write("average word length:"+avgWord);
		out.newLine();
		out.write("average title length:"+((double)titleLen / (double) counter));
		out.newLine();
		out.write("average title word length:"+((double)titleWord / (double) counter));
		out.newLine();
		out.write("average summary length:"+((double)summaryLen / (double) counter));
		out.newLine();
		out.write("average summary word length:"+((double)summaryWord / (double) counter));
		out.newLine();
		out.write("average num:"+avgNum);
		out.newLine();
		out.flush();
		out.close();
		Collections.sort(list, new Comparator<WeightString>() {
			@Override
			public int compare(WeightString o1, WeightString o2) {
				return Double.compare(o1.weight, o2.weight);
			}
		});
		for(int i = 0; i < list.size(); i ++){
			outLine.write(list.get(i).text + ":"+list.get(i).weight);
			outLine.newLine();
			outLine.flush();
		}
		outLine.close();
	}

}
