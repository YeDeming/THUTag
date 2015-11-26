package org.thunlp.tagsuggest.dataset;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import org.thunlp.io.JsonUtil;
import org.thunlp.language.chinese.ForwardMaxWordSegment;
import org.thunlp.language.chinese.WordSegment;
import org.thunlp.tagsuggest.common.KeywordPost;

public class KeywordTitleDetail {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		if (args.length != 2) {
			System.out.println("Usage:\njava -jar KeywordTitleDetail.jar <input_file> <output_file>");
			return;
		}
		BufferedReader input = new BufferedReader(new InputStreamReader(
				new FileInputStream(args[0])));
		BufferedWriter out = new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream(args[1])));
		
		JsonUtil J = new JsonUtil();
		String line;
		int totalCount = 0;
		int totalTags = 0;
		WordSegment ws = new ForwardMaxWordSegment();
		while((line = input.readLine()) != null){
			KeywordPost p = J.fromJson(line, KeywordPost.class);
			String[] words = ws.segment(p.getTitle());
			totalTags += p.getTags().size();
			int count = 0;
			for(String word : words){
				if(p.getTags().contains(word)){
					count ++;
				}
			}
			out.write(p.getId()+" title words:"+words.length + " tags size:"+ p.getTags().size()
					+" keyword in the title:"+count);
			out.newLine();
			out.flush();
			
			totalCount += (p.getTags().size() - count);
		}
		out.write("total tags:"+totalTags+" keyword not in tags:"+totalCount+" percent:"+(double)totalCount/(double)totalTags);
		out.newLine();
		out.flush();
	}

}
