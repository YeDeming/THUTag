package org.thunlp.tagsuggest.dataset;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import javax.swing.text.html.HTML.Tag;

import org.thunlp.io.JsonUtil;
import org.thunlp.tagsuggest.common.MyKeyword;
import org.thunlp.tagsuggest.common.MyKeyword2;
import org.thunlp.tagsuggest.common.ThreeMethodKeyword;

public class MeargeResult {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		if (args.length != 1) {
			System.out
					.println("Usage:\njava -jar MeargeResult.jar <your_output_file>");
			return;
		}

		/*
		HashMap<String, Double> rightCount = new HashMap<String, Double>();
		BufferedReader input = new BufferedReader(new InputStreamReader(
				new FileInputStream("/media/disk2/private/cxx/douban/smt_test/train_keyword_contentSplit_title_inverse/cut.gzG")));
		String line;
		while((line = input.readLine()) != null){
			String [] words = line.split(" ");
			rightCount.put(words[0], Double.parseDouble(words[1]));
		}
		*/
		
		String rightDir = "/media/disk2/private/cxx/douban/smt_test/train_keyword_contentSplit_title_removeKG2/";
		String wrongDir = "/media/disk2/private/cxx/douban/smt_test/train_keyword_contentSplit_title_allwrong2/";
		String tfidfDir = "/media/disk2/private/cxx/douban/smt_test/train_both_tfidf_keyword_tfidf2/";
		String textrankDir = "/media/disk2/private/cxx/douban/smt_test/train_both_tfidf_keyword_textrank2/";

		BufferedReader inputRight = null;
		BufferedReader inputWrong = null;
		BufferedReader inputTfidf = null;
		BufferedReader inputTextrank = null;
		String line1;
		String line2;
		String line3;
		String line4;
		JsonUtil J = new JsonUtil();

		
		//double totalRight = 0;
		//int totalTag = 0;
		BufferedWriter out = new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream(args[0])));
		for (int i = 0; i < 5; i++) {
			inputRight = new BufferedReader(new InputStreamReader(
					new FileInputStream(rightDir + "cut.gz" + i)));
			inputWrong = new BufferedReader(new InputStreamReader(
					new FileInputStream(wrongDir + "cut.gz" + i)));
			inputTfidf = new BufferedReader(new InputStreamReader(
					new FileInputStream(tfidfDir + "cut.gz" + i)));
			inputTextrank = new BufferedReader(new InputStreamReader(
					new FileInputStream(textrankDir + "cut.gz" + i)));
			int counter = 0;
			while ((line1 = inputRight.readLine()) != null) {
				Set<String> suggestSet = new HashSet<String>();
				counter++;
				line2 = inputWrong.readLine();
				line3 = inputTfidf.readLine();
				line4 = inputTextrank.readLine();
				MyKeyword2 keyword1 = J.fromJson(line1, MyKeyword2.class);
				MyKeyword2 keyword2 = J.fromJson(line2, MyKeyword2.class);
				MyKeyword2 keyword3 = J.fromJson(line3, MyKeyword2.class);
				MyKeyword2 keyword4 = J.fromJson(line4, MyKeyword2.class);
				int id1 = Integer.parseInt(keyword1.getId());
				int id2 = Integer.parseInt(keyword2.getId());
				int id3 = Integer.parseInt(keyword3.getId());
				int id4 = Integer.parseInt(keyword4.getId());
				if ((id2 != id1) || (id3 != id1) || (id4 != id1)) {
					System.out.println("File " + i + "line " + counter
							+ " is not same");
				}
				ThreeMethodKeyword key = new ThreeMethodKeyword();
				key.setTitle(keyword1.getTitle());
				key.setSummary(keyword1.getSummary());
				key.setContent(keyword1.getContent());
				key.setId(keyword1.getId());
				key.setAnswer(keyword1.getAnswer());
				
				for (int j = 0; j < keyword1.getSuggestTags().size() && j < 10; j++) {
					String word = keyword1.getSuggestTags().get(j);
					key.getSmtTags().add(word);
					suggestSet.add(word);
				}
				
				for (int j = 0; j < keyword2.getSuggestTags().size() && j < 10; j++) {
					String word = keyword2.getSuggestTags().get(j);
					key.getWrongTags().add(word);
					suggestSet.add(word);
				}
				
				for (int j = 0; j < keyword3.getSuggestTags().size() && j < 10; j++) {
					String word = keyword3.getSuggestTags().get(j);
					key.getTfidfTags().add(word);
					suggestSet.add(word);
				}
				for (int j = 0; j < keyword4.getSuggestTags().size() && j < 10; j++) {
					String word = keyword4.getSuggestTags().get(j);
					key.getTextRankTags().add(word);
					suggestSet.add(word);
				}
				
				key.setSuggest(suggestSet);
				out.write(J.toJson(key));
				out.newLine();
				out.flush();
				/*
				totalTag += keyword1.getAnswer().size();
				
				if(rightCount.containsKey(keyword1.getId())){
					totalRight += rightCount.get(keyword1.getId());
				}
				*/
			}
		}
		out.close();
		/*
		BufferedWriter outDetail = new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream(args[0]+"detail")));
		outDetail.write("total tags:"+totalTag+"\n");
		outDetail.write("total right:"+totalRight+"\n");
		outDetail.write("per:"+(totalRight/(double)totalTag)+"\n");
		outDetail.flush();
		outDetail.close();
		*/
	}

}
