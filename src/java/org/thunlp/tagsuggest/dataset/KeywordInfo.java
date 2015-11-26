package org.thunlp.tagsuggest.dataset;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Vector;

import org.thunlp.io.JsonUtil;
import org.thunlp.io.RecordReader;
import org.thunlp.language.chinese.LangUtils;
import org.thunlp.misc.WeightString;
import org.thunlp.tagsuggest.common.KeywordPost;
import org.thunlp.tagsuggest.common.Post;

import weka.classifiers.trees.j48.EntropyBasedSplitCrit;

import edu.stanford.nlp.io.EncodingPrintWriter.out;

public class KeywordInfo {

	private static JsonUtil J = new JsonUtil();
	
	public void getSinaInfo(String input, String output) throws IOException{
		BufferedReader in = new BufferedReader(new InputStreamReader(
				new FileInputStream(input)));
		BufferedWriter outLine = new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream(output+"line")));
		List<WeightString> list = new ArrayList<WeightString>();
		String line;
		int totalLen = 0;
		int totalWord = 0;
		int keyNum = 0;
		int counter = 0;
		int titleLen = 0;
		int titleWord = 0;
		while((line = in.readLine()) != null){
			Post p = J.fromJson(line, Post.class);
			totalLen += p.getTitle().length() + p.getContent().length();
			
			titleLen += p.getTitle().length();
			String allContent = p.getTitle()+" " +p.getContent();
			String[] words = allContent.split(" |。");
			totalWord += words.length;
			String[] titleWords = p.getTitle().split(" ");
			titleWord += titleWords.length;
			
			keyNum += p.getTags().size();
			counter ++;
			list.add(new WeightString(p.getId(), p.getTitle().length() + p.getContent().length()));
		}
		BufferedWriter out = new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream(output)));
		double avgLen = (double)totalLen / (double) counter;
		double avgWord = (double)totalWord / (double) counter;
		double avgNum = (double)keyNum / (double) counter;
		out.write("total line"+counter);
		out.newLine();
		out.write("total word"+totalWord);
		out.newLine();
		out.write("average length:"+avgLen);
		out.newLine();
		out.write("average word length:"+avgWord);
		out.newLine();
		out.write("average title length:"+((double)titleLen / (double) counter));
		out.newLine();
		out.write("average title word length:"+((double)titleWord / (double) counter));
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
	
	public void getKeywordInfo(String input, String output) throws IOException{
		RecordReader reader = new RecordReader(input);
		BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(output)));
		int artitleNums = 0;
		int inTitle = 0;
		int inSummary = 0;
		int inContent = 0;
		int notInAny = 0;
		int keywordNums = 0;
		int keywordCount = 0;
		int inPF = 0;
		int inPM = 0;
		int inPL = 0;
//		int inCF = 0;
//		int inCM = 0;
//		int inCL = 0;
		
		while(reader.next()){
			KeywordPost p = J.fromJson(reader.value(), KeywordPost.class);
			String title = p.getTitle();
			String summary = p.getSummary();
			String content = p.getContent();
			String[] paragraph = content.split("\n");
			if(p.getTags() != null){
				artitleNums ++;
				out.write("id:"+p.getId());
				for(String keyword : p.getTags()){
					keywordNums ++;
					out.write(" "+keyword+":");
					boolean isInTitle = false;
					if(title.indexOf(keyword) >= 0){
						isInTitle = true;
						inTitle ++;
						out.write(" inT");
					}
					boolean isInSummary = false;
					if(summary.indexOf(keyword) >= 0){
						isInSummary = true;
						inSummary ++;
						out.write(" inS");
					}
					int start = 0;
					int pos = 0;
					for(int i = 0 ; i < paragraph.length; i ++){
						while( (pos = paragraph[i].indexOf(keyword ,start)) != -1){
							out.write(" p"+i+":"+pos);
							double dpos = (double) pos / (double)paragraph[i].length();
							if(dpos <= 0.33) inPF ++;
							else if(dpos > 0.33 && dpos <= 0.66) inPM ++;
							else inPL ++;
							start = pos + 1;
						}
					}
					start = 0;
					pos = 0;
					out.write(" c:");
					
					boolean isInContent = false;
					while((pos = content.indexOf(keyword, start )) >= 0){
						isInContent = true;
						keywordCount ++;
						out.write(pos+" ");
						start = pos + 1;
					}
					if(isInContent){
						inContent ++;
					}
					
					if(!isInTitle && !isInSummary && !isInContent){
						notInAny ++;
					}
				}
			}
			out.newLine();
			out.flush();
		}
		out.write("artitleNums:"+artitleNums);
		out.newLine();
		out.write("inTitle:"+inTitle);
		out.newLine();
		out.write("inSummary:"+inSummary);
		out.newLine();
		out.write("inContent:"+inContent);
		out.newLine();
		out.write("notInAny:"+notInAny);
		out.newLine();
		out.write("keywordNums:"+keywordNums);
		out.newLine();
		out.write("keywordCount:"+keywordCount);
		out.newLine();
		out.write("inPF:"+inPF);
		out.newLine();
		out.write("inPM:"+inPM);
		out.newLine();
		out.write("inPL:"+inPL);
		out.newLine();
		out.flush();
		out.close();
	}
	
	public void checkSegPerform(String input, String output) throws IOException{
		RecordReader reader = new RecordReader(input);
		BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(output),"UTF8"));
		
		int artitleNums = 0;
		int inTitle = 0;
		int inSummary = 0;
		int inContent = 0;
		int notInAny = 0;
		int keywordNums = 0;
		
		HashMap<String, String> titleWordMap = new HashMap<String, String>();
		HashMap<String, String> summaryWordMap = new HashMap<String, String>();
		HashMap<String, String> contentWordMap = new HashMap<String, String>();
		
		HashMap<String, Integer> titlePosMap = new HashMap<String, Integer>();
		HashMap<String, Integer> summaryPosMap = new HashMap<String, Integer>();
		HashMap<String, Integer> contentPosMap = new HashMap<String, Integer>();
		
		while(reader.next()){
			artitleNums ++;
			titleWordMap.clear();
			summaryWordMap.clear();
			contentWordMap.clear();
			
			KeywordPost p = J.fromJson(reader.value(), KeywordPost.class);
			String[] datas = p.getTitle().split(" ");
			for(String data : datas){
				String[] wordAndTag = data.split("_");
				if(wordAndTag.length != 2){
					System.out.println(reader.numRead()+":"+data);
					continue;
				}
				titleWordMap.put(wordAndTag[0], wordAndTag[1]);
			}
			
			String[] sentences = p.getSummary().split("。");
			for(String sentence : sentences){
				datas = sentence.split(" ");
				for(String data : datas){
					if(data.equals("")){
						continue;
					}
					String[] wordAndTag = data.split("_");
					if(wordAndTag.length != 2){
						System.out.println(reader.numRead()+":"+data);
						continue;
					}
					summaryWordMap.put(wordAndTag[0], wordAndTag[1]);
				}
			}
			
			sentences = p.getContent().split("。");
			for(String sentence : sentences){
				datas = sentence.split(" ");
				for(String data : datas){
					if(data.equals("")){
						continue;
					}
					String[] wordAndTag = data.split("_");
					if(wordAndTag.length != 2){
						System.out.println(reader.numRead()+":"+data);
						continue;
					}
					contentWordMap.put(wordAndTag[0], wordAndTag[1]);
				}
			}
			
			for(String tag : p.getTags()){
				keywordNums ++;
				boolean isTitle = false;
				boolean isSummary = false;
				boolean isContent = false;
				
				if(titleWordMap.containsKey(tag)){
					inTitle ++;
					isTitle = true;
					String label = titleWordMap.get(tag);
					Integer tmpInt = titlePosMap.get(label);
					if(tmpInt != null){
						titlePosMap.put(label, tmpInt + 1);
					}else{
						titlePosMap.put(label, 1);
					}
				}
				if(summaryWordMap.containsKey(tag)){
					inSummary ++;
					isSummary = true;
					String label = summaryWordMap.get(tag);
					Integer tmpInt = summaryPosMap.get(label);
					if(tmpInt != null){
						summaryPosMap.put(label, tmpInt + 1);
					}else{
						summaryPosMap.put(label, 1);
					}
				}
				if(contentWordMap.containsKey(tag)){
					inContent ++;
					isContent = true;
					String label = contentWordMap.get(tag);
					Integer tmpInt = contentPosMap.get(label);
					if(tmpInt != null){
						contentPosMap.put(label, tmpInt + 1);
					}else{
						contentPosMap.put(label, 1);
					}
				}
				
				if(!isTitle && !isSummary && !isContent){
					notInAny ++;
				}
				
			}
			
		}
		reader.close();
		
		out.write("artitleNums:"+artitleNums);
		out.newLine();
		out.write("inTitle:"+inTitle);
		out.newLine();
		for(Entry<String, Integer> e : titlePosMap.entrySet()){
			out.write(e.getKey()+":"+e.getValue());
			out.newLine();
		}
		out.newLine();
		out.write("inSummary:"+inSummary);
		out.newLine();
		for(Entry<String, Integer> e : summaryPosMap.entrySet()){
			out.write(e.getKey()+":"+e.getValue());
			out.newLine();
		}
		out.newLine();
		out.write("inContent:"+inContent);
		out.newLine();
		for(Entry<String, Integer> e : contentPosMap.entrySet()){
			out.write(e.getKey()+":"+e.getValue());
			out.newLine();
		}
		out.newLine();
		out.write("keywordNums:"+keywordNums);
		out.newLine();
		out.write("notInAny:"+notInAny);
		out.newLine();
		out.close();
	}
	
	public String cleanText(String content){
		content = LangUtils.removePunctuationMarks(content);
		content = LangUtils.removeLineEnds(content);
		content = LangUtils.removeExtraSpaces(content);
		content = content.toLowerCase();
		return content;
	}
	
	public void getText(String input, String output) throws IOException{
		RecordReader reader = new RecordReader(input);
		BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(output)));
		int lineCounter = 0;
		while(reader.next()){
			KeywordPost p = J.fromJson(reader.value(), KeywordPost.class);
			
			out.write(p.getTitle());
			out.newLine();
			out.flush();
			
			out.write(p.getSummary());
			out.newLine();
			out.flush();
			
			out.write(p.getContent());
			out.newLine();
			out.flush();
			
			lineCounter ++;
			if(lineCounter % 100 == 0){
				System.out.println("Done "+lineCounter+" lines.");
			}
		}
		out.close();
	}
	
	public void getSentence(String input, String output) throws IOException{
		RecordReader reader = new RecordReader(input);
		BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(output)));
		int lineCounter = 0;
		while(reader.next()){
			KeywordPost p = J.fromJson(reader.value(), KeywordPost.class);
			
			String paragraph = p.getTitle();
			paragraph = paragraph.replaceAll("\n", "");
			String[] sentences = paragraph.split("。|！");
			for(String sentence : sentences){
				sentence = cleanText(sentence);
				if(sentence.equals("")){
					continue;
				}
				out.write(sentence);
				out.newLine();
				out.flush();
			}
			out.newLine();
			out.flush();
			
			paragraph = p.getSummary();
			paragraph = paragraph.replaceAll("\n", "");
			sentences = paragraph.split("。|！");
			for(String sentence : sentences){
				sentence = cleanText(sentence);
				if(sentence.equals("")){
					continue;
				}
				out.write(sentence);
				out.newLine();
				out.flush();
			}
			out.newLine();
			out.flush();
			
			paragraph = p.getContent();
			paragraph = paragraph.replaceAll("\n", "");
			sentences = paragraph.split("。|！");
			for(String sentence : sentences){
				sentence = cleanText(sentence);
				if(sentence.equals("")){
					continue;
				}
				out.write(sentence);
				out.newLine();
				out.flush();
			}
			out.newLine();
			out.flush();
			
			lineCounter ++;
			if(lineCounter % 100 == 0){
				System.out.println("Done "+lineCounter+" lines.");
			}
		}
		out.close();
	}
	
	public void checkNoLatticePerform(String keywordJson, String input, String output) throws IOException{
		RecordReader reader = new RecordReader(keywordJson);
		BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(input),"UTF8"));
		BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(output),"UTF8"));
		
		int wordCount = 0;
		int artitleNums = 0;
		int inTitle = 0;
		int inSummary = 0;
		int inContent = 0;
		int inDocument = 0;
		int notInAny = 0;
		int keywordNums = 0;
		
		HashMap<String, String> titleWordMap = new HashMap<String, String>();
		HashMap<String, String> summaryWordMap = new HashMap<String, String>();
		HashMap<String, String> contentWordMap = new HashMap<String, String>();
		
		HashMap<String, Integer> titlePosMap = new HashMap<String, Integer>();
		HashMap<String, Integer> summaryPosMap = new HashMap<String, Integer>();
		HashMap<String, Integer> contentPosMap = new HashMap<String, Integer>();
		
		HashSet<String> allTitleWordSet = new HashSet<String>();
		HashSet<String> allDocumentWordSet = new HashSet<String>();
		HashSet<String> notInAnySet = new HashSet<String>();
		
		HashMap<Integer, Integer> tagLengthMap = new HashMap<Integer, Integer>();
		
		int notInTitle = 0;
		int notInAllTitle = 0;
		
		Vector<String> titleVec = new Vector<String>();
		
		long totalLength = 0; 
		
		String line = "";
		while(reader.next()){
			artitleNums ++;
			titleWordMap.clear();
			summaryWordMap.clear();
			contentWordMap.clear();
			
			KeywordPost p = J.fromJson(reader.value(), KeywordPost.class);
			titleVec.add(p.getTitle());
			
			line = in.readLine();
			String[] datas = line.split(" ");
			for(String data : datas){
				String[] wordAndTag = data.split("_");
				if(wordAndTag.length != 2){
					System.out.println(reader.numRead()+":"+data);
					continue;
				}
				titleWordMap.put(wordAndTag[0], wordAndTag[1]);
				allTitleWordSet.add(wordAndTag[0]);
				wordCount ++;
				totalLength += wordAndTag[0].length();
				allDocumentWordSet.add(wordAndTag[0]);
			}
			
			line = in.readLine();
			datas = line.split(" ");
			for(String data : datas){
				String[] wordAndTag = data.split("_");
				if(wordAndTag.length != 2){
					System.out.println(reader.numRead()+":"+data);
					continue;
				}
				summaryWordMap.put(wordAndTag[0], wordAndTag[1]);
				wordCount ++;
				totalLength += wordAndTag[0].length();
				allDocumentWordSet.add(wordAndTag[0]);
			}
			
			line = in.readLine();
			datas = line.split(" ");
			for(String data : datas){
				String[] wordAndTag = data.split("_");
				if(wordAndTag.length != 2){
					System.out.println(reader.numRead()+":"+data);
					continue;
				}
				contentWordMap.put(wordAndTag[0], wordAndTag[1]);
				wordCount ++;
				totalLength += wordAndTag[0].length();
				allDocumentWordSet.add(wordAndTag[0]);
			}
			
			for(String tag : p.getTags()){
				
				int tagLength = tag.length();
				if(!tagLengthMap.containsKey(tagLength)){
					tagLengthMap.put(tagLength, 0);
				}
				tagLengthMap.put(tagLength, tagLengthMap.get(tagLength) + 1);
				
				keywordNums ++;
				boolean isTitle = false;
				boolean isSummary = false;
				boolean isContent = false;
				
				if(titleWordMap.containsKey(tag)){
					inTitle ++;
					isTitle = true;
					String label = titleWordMap.get(tag);
					Integer tmpInt = titlePosMap.get(label);
					if(tmpInt != null){
						titlePosMap.put(label, tmpInt + 1);
					}else{
						titlePosMap.put(label, 1);
					}
				}else{
					notInTitle ++;
				}
				if(summaryWordMap.containsKey(tag)){
					inSummary ++;
					isSummary = true;
					String label = summaryWordMap.get(tag);
					Integer tmpInt = summaryPosMap.get(label);
					if(tmpInt != null){
						summaryPosMap.put(label, tmpInt + 1);
					}else{
						summaryPosMap.put(label, 1);
					}
				}
				if(contentWordMap.containsKey(tag)){
					inContent ++;
					isContent = true;
					String label = contentWordMap.get(tag);
					Integer tmpInt = contentPosMap.get(label);
					if(tmpInt != null){
						contentPosMap.put(label, tmpInt + 1);
					}else{
						contentPosMap.put(label, 1);
					}
				}
				
				if(!isTitle && !isSummary && !isContent){
					notInAny ++;
					notInAnySet.add(tag);
				}else{
					inDocument ++;
				}

			}
		}
		
		int notInAllOriTitle = 0;
		int notInAllDocument = 0;
		for(String tag : notInAnySet){
			if(!allTitleWordSet.contains(tag)){
				notInAllTitle ++;
			}
			
			if(!allDocumentWordSet.contains(tag)){
				notInAllDocument ++;
			}
			
			boolean inOriTitle = false;
			for(String title : titleVec){
				if(title.contains(tag)){
					inOriTitle = true;
					break;
				}
			}
			if(!inOriTitle){
				notInAllOriTitle ++;
			}
		}
		
		
		
		out.write("artitleNums:"+artitleNums);
		out.newLine();
		out.write("totalLength:"+totalLength);
		out.newLine();
		out.write("averageLength:"+((double)totalLength) / (double)artitleNums);
		out.newLine();
		
		out.write("inTitle:"+inTitle);
		out.newLine();
		for(Entry<String, Integer> e : titlePosMap.entrySet()){
			out.write(e.getKey()+":"+e.getValue());
			out.newLine();
		}
		out.newLine();
		out.write("inSummary:"+inSummary);
		out.newLine();
		for(Entry<String, Integer> e : summaryPosMap.entrySet()){
			out.write(e.getKey()+":"+e.getValue());
			out.newLine();
		}
		out.newLine();
		out.write("inContent:"+inContent);
		out.newLine();
		for(Entry<String, Integer> e : contentPosMap.entrySet()){
			out.write(e.getKey()+":"+e.getValue());
			out.newLine();
		}
		out.newLine();
		out.write("keywordNums:"+keywordNums);
		out.newLine();
		out.write("notInAny:"+notInAny);
		out.newLine();
		out.write("wordCount:"+wordCount);
		out.newLine();
		out.write("notInTitle:"+notInTitle);
		out.newLine();
		out.write("notInAllTitle:"+notInAllTitle);
		out.newLine();
		out.write("notInAllDocument:"+notInAllDocument);
		out.newLine();
		out.write("notInAllOriTitle:"+notInAllOriTitle);
		out.newLine();
		out.write("inTitle:"+(keywordNums - notInTitle));
		out.newLine();
		out.write("inDocument:"+inDocument);
		out.newLine();
		out.write("inAllTitle:"+(keywordNums - notInAllTitle));
		out.newLine();
		out.write("inAllDocument:"+(keywordNums - notInAllDocument));
		out.newLine();
		
		for(Entry<Integer, Integer> e : tagLengthMap.entrySet()){
			out.write(e.getKey() + ":"+e.getValue());
			out.newLine();
		}
		
		out.close();
	}
	
	public void checkLatticePerform(String keywordJson, String input, String latticeFile, String output) throws IOException{
		RecordReader reader = new RecordReader(keywordJson);
		BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(input),"UTF8"));
		BufferedReader inLattice = new BufferedReader(new InputStreamReader(new FileInputStream(latticeFile),"UTF8"));
		BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(output),"UTF8"));
		
		int wordCount = 0;
		int latticeWordCount = 0;
		
		int artitleNums = 0;
		int inTitle = 0;
		int inSummary = 0;
		int inContent = 0;
		int notInAny = 0;
		int keywordNums = 0;
		double wordAverageLength = 0.0;
		double latticeAverageLength = 0.0;
		double totalAverageLength = 0.0;
		double keywordAverageLength = 0.0;
		
		int latticeCorrespondWordCount = 0;
		double latticeCorrespondAverageLength = 0.0;
		
		
		HashMap<String, String> titleWordMap = new HashMap<String, String>();
		HashMap<String, String> summaryWordMap = new HashMap<String, String>();
		HashMap<String, String> contentWordMap = new HashMap<String, String>();
		
		HashMap<String, String> titleLatticeMap = new HashMap<String, String>();
		HashMap<String, String> summaryLatticeMap = new HashMap<String, String>();
		HashMap<String, String> contentLatticeMap = new HashMap<String, String>();
		int titleLatticeWordCount = 0;
		double titleLatticeAverageLength = 0.0;
		int summaryLatticeWordCount = 0;
		double summaryLatticeAverageLength = 0.0;
		int contentLatticeWordCount = 0;
		double contentLatticeAverageLength = 0.0;
		
		
		HashMap<String, Integer> titlePosMap = new HashMap<String, Integer>();
		HashMap<String, Integer> summaryPosMap = new HashMap<String, Integer>();
		HashMap<String, Integer> contentPosMap = new HashMap<String, Integer>();
		
		HashSet<String> allTitleSet = new HashSet<String>();
		HashSet<String> allDocumentSet = new HashSet<String>();
		
		String line = "";
		String latticeLine = "";
		
		HashSet<String> notInAnySet = new HashSet<String>();
		reader = new RecordReader(keywordJson);
		while(reader.next()){
			artitleNums ++;
			titleWordMap.clear();
			summaryWordMap.clear();
			contentWordMap.clear();
			
			titleLatticeMap.clear();
			summaryLatticeMap.clear();
			contentLatticeMap.clear();
			
			KeywordPost p = J.fromJson(reader.value(), KeywordPost.class);
			
			line = in.readLine();
			latticeLine = inLattice.readLine();
			String[] lineSentences = line.split(" ");
			String[] latticeLineSentences = latticeLine.split("  ");
			if(lineSentences.length != latticeLineSentences.length){
				System.out.println("Error1:"+reader.numRead()+":"+lineSentences.length +":"+latticeLineSentences.length);
			}else{
				
				for(int i = 0; i < latticeLineSentences.length; i ++){
					boolean[] isLattice = new boolean[lineSentences[i].length()];
					for(int j = 0 ; j < lineSentences[i].length(); j ++){
						isLattice[j] = false;
					}
					
					String[] datas = latticeLineSentences[i].split(" ");
					
					for(String data : datas){
						String[] wordAndTag = data.split(",");
						if(wordAndTag.length != 4){
							System.out.println(reader.numRead()+":"+data);
							continue;
						}
						int startIndex = Integer.parseInt(wordAndTag[0]);
						int endIndex = Integer.parseInt(wordAndTag[1]);
						String word = lineSentences[i].substring(startIndex, endIndex);
						
						totalAverageLength += word.length();
						
						titleWordMap.put(word, wordAndTag[2]);
						if(wordAndTag[3].equals("0")){
							wordAverageLength += word.length();
							wordCount ++;
						}else{
							latticeAverageLength += word.length();
							latticeWordCount ++;
							titleLatticeMap.put(word, wordAndTag[2]);
							for(int j = startIndex; j < endIndex; j ++){
								isLattice[j] = true;
							}
						}
						
						allTitleSet.add(word);
						allDocumentSet.add(word);
					}
					
					for(String data : datas){
						String[] wordAndTag = data.split(",");
						if(wordAndTag.length != 4){
							System.out.println(reader.numRead()+":"+data);
							continue;
						}
						int startIndex = Integer.parseInt(wordAndTag[0]);
						int endIndex = Integer.parseInt(wordAndTag[1]);
						String word = lineSentences[i].substring(startIndex, endIndex);
						
						if(wordAndTag[3].equals("0")){
							for(int j = startIndex; j < endIndex; j ++){
								if(isLattice[j]){
									latticeCorrespondWordCount ++;
									latticeCorrespondAverageLength += word.length();
									break;
								}
							}
						}
					}
					
				}
			}
			
			line = in.readLine();
			latticeLine = inLattice.readLine();
			lineSentences = line.split(" ");
			latticeLineSentences = latticeLine.split("  ");
			if(lineSentences.length != latticeLineSentences.length){
				System.out.println("Error1:"+reader.numRead()+":"+lineSentences.length +":"+latticeLineSentences.length);
			}else{
				for(int i = 0; i < latticeLineSentences.length; i ++){
					boolean[] isLattice = new boolean[lineSentences[i].length()];
					for(int j = 0 ; j < lineSentences[i].length(); j ++){
						isLattice[j] = false;
					}
					
					String[] datas = latticeLineSentences[i].split(" ");
					
					for(String data : datas){
						String[] wordAndTag = data.split(",");
						if(wordAndTag.length != 4){
							System.out.println(reader.numRead()+":"+data);
							continue;
						}
						int startIndex = Integer.parseInt(wordAndTag[0]);
						int endIndex = Integer.parseInt(wordAndTag[1]);
						String word = lineSentences[i].substring(startIndex, endIndex);
						
						totalAverageLength += word.length();
						
						summaryWordMap.put(word, wordAndTag[2]);
						if(wordAndTag[3].equals("0")){
							wordAverageLength += word.length();
							wordCount ++;
						}else{
							latticeAverageLength += word.length();
							latticeWordCount ++;
							summaryLatticeMap.put(word, wordAndTag[2]);
							for(int j = startIndex; j < endIndex; j ++){
								isLattice[j] = true;
							}
						}
						allDocumentSet.add(word);
					}
					
					for(String data : datas){
						String[] wordAndTag = data.split(",");
						if(wordAndTag.length != 4){
							System.out.println(reader.numRead()+":"+data);
							continue;
						}
						int startIndex = Integer.parseInt(wordAndTag[0]);
						int endIndex = Integer.parseInt(wordAndTag[1]);
						String word = lineSentences[i].substring(startIndex, endIndex);
						
						if(wordAndTag[3].equals("0")){
							for(int j = startIndex; j < endIndex; j ++){
								if(isLattice[j]){
									latticeCorrespondWordCount ++;
									latticeCorrespondAverageLength += word.length();
									break;
								}
							}
						}
					}
					
				}
			}
			
			line = in.readLine();
			latticeLine = inLattice.readLine();
			lineSentences = line.split(" ");
			latticeLineSentences = latticeLine.split("  ");
			if(lineSentences.length != latticeLineSentences.length){
				System.out.println("Error1:"+reader.numRead()+":"+lineSentences.length +":"+latticeLineSentences.length);
			}else{
				for(int i = 0; i < latticeLineSentences.length; i ++){
					boolean[] isLattice = new boolean[lineSentences[i].length()];
					for(int j = 0 ; j < lineSentences[i].length(); j ++){
						isLattice[j] = false;
					}
					
					String[] datas = latticeLineSentences[i].split(" ");
					
					for(String data : datas){
						String[] wordAndTag = data.split(",");
						if(wordAndTag.length != 4){
							System.out.println(reader.numRead()+":"+data);
							continue;
						}
						int startIndex = Integer.parseInt(wordAndTag[0]);
						int endIndex = Integer.parseInt(wordAndTag[1]);
						String word = lineSentences[i].substring(startIndex, endIndex);
						
						totalAverageLength += word.length();
						
						contentWordMap.put(word, wordAndTag[2]);
						if(wordAndTag[3].equals("0")){
							wordAverageLength += word.length();
							wordCount ++;
						}else{
							latticeAverageLength += word.length();
							latticeWordCount ++;
							contentLatticeMap.put(word, wordAndTag[2]);
							for(int j = startIndex; j < endIndex; j ++){
								isLattice[j] = true;
							}
						}
						allDocumentSet.add(word);
					}
					
					for(String data : datas){
						String[] wordAndTag = data.split(",");
						if(wordAndTag.length != 4){
							System.out.println(reader.numRead()+":"+data);
							continue;
						}
						int startIndex = Integer.parseInt(wordAndTag[0]);
						int endIndex = Integer.parseInt(wordAndTag[1]);
						String word = lineSentences[i].substring(startIndex, endIndex);
						
						if(wordAndTag[3].equals("0")){
							for(int j = startIndex; j < endIndex; j ++){
								if(isLattice[j]){
									latticeCorrespondWordCount ++;
									latticeCorrespondAverageLength += word.length();
									break;
								}
							}
						}
					}
					
				}
			}
			
			for(String tag : p.getTags()){
				keywordNums ++;
				keywordAverageLength += tag.length();
				boolean isTitle = false;
				boolean isSummary = false;
				boolean isContent = false;
				
				if(titleWordMap.containsKey(tag)){
					inTitle ++;
					isTitle = true;
					String label = titleWordMap.get(tag);
					Integer tmpInt = titlePosMap.get(label);
					if(tmpInt != null){
						titlePosMap.put(label, tmpInt + 1);
					}else{
						titlePosMap.put(label, 1);
					}
				}
				if(summaryWordMap.containsKey(tag)){
					inSummary ++;
					isSummary = true;
					String label = summaryWordMap.get(tag);
					Integer tmpInt = summaryPosMap.get(label);
					if(tmpInt != null){
						summaryPosMap.put(label, tmpInt + 1);
					}else{
						summaryPosMap.put(label, 1);
					}
				}
				if(contentWordMap.containsKey(tag)){
					inContent ++;
					isContent = true;
					String label = contentWordMap.get(tag);
					Integer tmpInt = contentPosMap.get(label);
					if(tmpInt != null){
						contentPosMap.put(label, tmpInt + 1);
					}else{
						contentPosMap.put(label, 1);
					}
				}
				
				if(titleLatticeMap.containsKey(tag)){
					titleLatticeWordCount ++;
					titleLatticeAverageLength += tag.length();
				}
				if(summaryLatticeMap.containsKey(tag)){
					summaryLatticeWordCount ++;
					summaryLatticeAverageLength += tag.length();
				}
				if(contentLatticeMap.containsKey(tag)){
					contentLatticeWordCount ++;
					contentLatticeAverageLength += tag.length();
				}
				
				if(!isTitle && !isSummary && !isContent){
					notInAnySet.add(tag);
					notInAny ++;
				}
			}
		}
		
		out.write("artitleNums:"+artitleNums);
		out.newLine();
		out.write("inTitle:"+inTitle);
		out.newLine();
		for(Entry<String, Integer> e : titlePosMap.entrySet()){
			out.write(e.getKey()+":"+e.getValue());
			out.newLine();
		}
		out.newLine();
		out.write("inSummary:"+inSummary);
		out.newLine();
		for(Entry<String, Integer> e : summaryPosMap.entrySet()){
			out.write(e.getKey()+":"+e.getValue());
			out.newLine();
		}
		out.newLine();
		out.write("inContent:"+inContent);
		out.newLine();
		for(Entry<String, Integer> e : contentPosMap.entrySet()){
			out.write(e.getKey()+":"+e.getValue());
			out.newLine();
		}
		out.newLine();
		out.write("keywordNums:"+keywordNums);
		out.newLine();
		
		int notInAllTitle = 0;
		int notInAllDocument = 0;
		for(String tag : notInAnySet){
			if(!allTitleSet.contains(tag)){
				notInAllTitle ++;
			}
			
			if(!allDocumentSet.contains(tag)){
				notInAllDocument ++;
			}
		}
		
		out.write("inTitle:"+inTitle);
		out.newLine();
		out.write("inDocument:"+(keywordNums - notInAny));
		out.newLine();
		out.write("inAllTitle:"+(keywordNums - notInAllTitle));
		out.newLine();
		out.write("inAllDocument:"+(keywordNums - notInAllDocument));
		out.newLine();
		
		out.write("keywordAverageLength:"+(keywordAverageLength / ((double)keywordNums)));
		out.newLine();
		out.write("notInAny:"+notInAny);
		out.newLine();
		out.write("wordCount:"+wordCount);
		out.newLine();
		out.write("latticeWordCount:"+latticeWordCount);
		out.newLine();
		out.write("wordAverageLength:"+(wordAverageLength / ((double)wordCount)));
		out.newLine();
		out.write("latticeAverageLength:"+(latticeAverageLength / ((double)latticeWordCount)));
		out.newLine();
		out.write("totalAverageLength:"+(totalAverageLength / ((double)(wordCount + latticeWordCount))));
		out.newLine();
		out.write("latticeCorrespondWordCount:"+latticeCorrespondWordCount);
		out.newLine();
		out.write("latticeCorrespondAverageLength:"+(latticeCorrespondAverageLength / ((double)latticeCorrespondWordCount)));
		out.newLine();
		
		out.write("titleLatticeWordCount:"+titleLatticeWordCount);
		out.newLine();
		out.write("titleLatticeAverageLength:"+(titleLatticeAverageLength / ((double)titleLatticeWordCount)));
		out.newLine();
		
		out.write("summaryLatticeWordCount:"+summaryLatticeWordCount);
		out.newLine();
		out.write("summaryLatticeAverageLength:"+(summaryLatticeAverageLength / ((double)summaryLatticeWordCount)));
		out.newLine();
		
		out.write("contentLatticeWordCount:"+contentLatticeWordCount);
		out.newLine();
		out.write("contentLatticeAverageLength:"+(contentLatticeAverageLength / ((double)contentLatticeWordCount)));
		out.newLine();
		out.close();
	}
	
	
	
	
	public void getSegmentTextFromLattice(String input, String lattice, String output) throws IOException{
		BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(input),"UTF8"));
		BufferedReader inLattice = new BufferedReader(new InputStreamReader(new FileInputStream(lattice),"UTF8"));
		BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(output),"UTF8"));
		
		String line = "";
		String latticeLine = "";
		int lineCounter = 0;
		while((line = in.readLine()) != null){
			lineCounter ++;
			latticeLine = inLattice.readLine();
			String[] lineSentences = line.split(" ");
			String[] latticeLineSentences = latticeLine.split("  ");
			if(lineSentences.length != latticeLineSentences.length){
				System.out.println("Error1:"+ lineCounter +":"+lineSentences.length +":"+latticeLineSentences.length);
			}else{
				StringBuffer tmp = new StringBuffer();
				for(int i = 0; i < latticeLineSentences.length; i ++){
					String[] datas = latticeLineSentences[i].split(" ");
					for(String data : datas){
						String[] wordAndTag = data.split(",");
						if(wordAndTag.length != 4){
							System.out.println(lineCounter +":"+data);
							continue;
						}
						
						if(wordAndTag[3].equals("0")){
							tmp.append(lineSentences[i].substring(Integer.parseInt(wordAndTag[0]), Integer.parseInt(wordAndTag[1]))
									+ "_" + wordAndTag[2] + " ");
						}
						
					}
				}
				out.write(tmp.toString().trim());
				out.newLine();
				out.flush();
			}
			
			lineCounter ++;
			line = in.readLine();
			latticeLine = inLattice.readLine();
			lineSentences = line.split(" ");
			latticeLineSentences = latticeLine.split("  ");
			if(lineSentences.length != latticeLineSentences.length){
				System.out.println("Error1:"+lineCounter+":"+lineSentences.length +":"+latticeLineSentences.length);
			}else{
				StringBuffer tmp = new StringBuffer();
				for(int i = 0; i < latticeLineSentences.length; i ++){
					String[] datas = latticeLineSentences[i].split(" ");
					for(String data : datas){
						String[] wordAndTag = data.split(",");
						if(wordAndTag.length != 4){
							System.out.println(lineCounter +":"+data);
							continue;
						}
						
						if(wordAndTag[3].equals("0")){
							tmp.append(lineSentences[i].substring(Integer.parseInt(wordAndTag[0]), Integer.parseInt(wordAndTag[1]))
									+ "_" + wordAndTag[2] + " ");
						}
						
					}
				}
				out.write(tmp.toString().trim());
				out.newLine();
				out.flush();
			}
			
			line = in.readLine();
			latticeLine = inLattice.readLine();
			lineSentences = line.split(" ");
			latticeLineSentences = latticeLine.split("  ");
			if(lineSentences.length != latticeLineSentences.length){
				System.out.println("Error1:"+lineCounter+":"+lineSentences.length +":"+latticeLineSentences.length);
			}else{
				StringBuffer tmp = new StringBuffer();
				for(int i = 0; i < latticeLineSentences.length; i ++){
					String[] datas = latticeLineSentences[i].split(" ");
					for(String data : datas){
						String[] wordAndTag = data.split(",");
						if(wordAndTag.length != 4){
							System.out.println(lineCounter +":"+data);
							continue;
						}
						
						if(wordAndTag[3].equals("0")){
							tmp.append(lineSentences[i].substring(Integer.parseInt(wordAndTag[0]), Integer.parseInt(wordAndTag[1]))
									+ "_" + wordAndTag[2] + " ");
						}
						
					}
				}
				out.write(tmp.toString().trim());
				out.newLine();
				out.flush();
			}
		}
		in.close();
		inLattice.close();
		out.close();
	}
	
	public void compareTwoFile(String first, String second, String output) throws IOException{
		BufferedReader inFirst = new BufferedReader(new InputStreamReader(new FileInputStream(first),"UTF8"));
		BufferedReader inSecond = new BufferedReader(new InputStreamReader(new FileInputStream(second),"UTF8"));
		BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(output),"UTF8"));
		String firstLine = "";
		String secondLine = "";
		int lineCounter = 0;
		while((firstLine = inFirst.readLine()) != null){
			secondLine = inSecond.readLine();
			lineCounter ++;
			if(!firstLine.equals(secondLine)){
				out.write(""+lineCounter);
				out.newLine();
				out.flush();
				out.write(""+firstLine);
				out.newLine();
				out.flush();
				out.write(""+secondLine);
				out.newLine();
				out.flush();
			}
		}
		inFirst.close();
		inSecond.close();
	}
	
	public void createSegmentdJson(String keywordJson, String input, String output)throws IOException{
		RecordReader reader = new RecordReader(keywordJson);
		BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(input),"UTF8"));
		BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(output),"UTF8"));
		
		String line = "";
		int lineCounter = 0;
		while(reader.next()){
			KeywordPost p = J.fromJson(reader.value(), KeywordPost.class);
			lineCounter ++;
			
			StringBuffer tmp = new StringBuffer();
			line = in.readLine();
			while(!line.equals("")){
				tmp.append(line+" ");
				line = in.readLine();
			}
			p.setTitle(tmp.toString().trim());
			
			tmp = new StringBuffer();
			line = in.readLine();
			while(!line.equals("")){
				tmp.append(line+"。");
				line = in.readLine();
			}
			p.setSummary(tmp.toString().trim());
			
			tmp = new StringBuffer();
			line = in.readLine();
			while(!line.equals("")){
				tmp.append(line+"。");
				line = in.readLine();
			}
			p.setContent(tmp.toString().trim());
			
			out.write(J.toJson(p));
			out.newLine();
			out.flush();
		}
		in.close();
		out.close();
	}
	
	public void createSegmentdLatticeAsNormal(String keywordJson, String input, String latticeFile, String output)throws IOException{
		RecordReader reader = new RecordReader(keywordJson);
		BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(input),"UTF8"));
		BufferedReader inLattice = new BufferedReader(new InputStreamReader(new FileInputStream(latticeFile),"UTF8"));
		BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(output),"UTF8"));
		
		String line = "";
		String latticeLine = "";
		int lineCounter = 0;
		while(reader.next()){
			KeywordPost p = J.fromJson(reader.value(), KeywordPost.class);
			lineCounter ++;
			
			line = in.readLine();
			latticeLine = inLattice.readLine();
			StringBuffer tmp = new StringBuffer();
			while(!line.equals("") && !latticeLine.equals("")){
				String[] lineSentences = line.split(" ");
				String[] latticeLineSentences = latticeLine.split("  ");
				if(lineSentences.length != latticeLineSentences.length){
					System.out.println("Error1:"+ lineCounter +":" +line+":"+lineSentences.length +":"+latticeLine+":"+latticeLineSentences.length);
				}else{
					for(int i = 0; i < latticeLineSentences.length; i ++){
						String[] datas = latticeLineSentences[i].split(" ");
						for(String data : datas){
							String[] wordAndTag = data.split(",");
							if(wordAndTag.length != 4){
								System.out.println(lineCounter +":"+data);
								continue;
							}
							
							//if(wordAndTag[3].equals("0")){
								tmp.append(lineSentences[i].substring(Integer.parseInt(wordAndTag[0]), Integer.parseInt(wordAndTag[1]))
										+ "_" + wordAndTag[2] + " ");
							//}
						}
					}
					//tmp.append("。");
				}
				line = in.readLine();
				latticeLine = inLattice.readLine();
			}
			p.setTitle(tmp.toString().trim());
			
			
			line = in.readLine();
			latticeLine = inLattice.readLine();
			tmp = new StringBuffer();
			while(!line.equals("") && !latticeLine.equals("")){
				String[] lineSentences = line.split(" ");
				String[] latticeLineSentences = latticeLine.split("  ");
				if(lineSentences.length != latticeLineSentences.length){
					System.out.println("Error1:"+ lineCounter +":"+lineSentences.length +":"+latticeLineSentences.length);
				}else{
					for(int i = 0; i < latticeLineSentences.length; i ++){
						String[] datas = latticeLineSentences[i].split(" ");
						for(String data : datas){
							String[] wordAndTag = data.split(",");
							if(wordAndTag.length != 4){
								System.out.println(lineCounter +":"+data);
								continue;
							}
							
							//if(wordAndTag[3].equals("0")){
								tmp.append(lineSentences[i].substring(Integer.parseInt(wordAndTag[0]), Integer.parseInt(wordAndTag[1]))
										+ "_" + wordAndTag[2] + " ");
							//}
						}
					}
					tmp.append("。");
				}
				line = in.readLine();
				latticeLine = inLattice.readLine();
			}
			p.setSummary(tmp.toString().trim());
			
			line = in.readLine();
			latticeLine = inLattice.readLine();
			tmp = new StringBuffer();
			while(!line.equals("") && !latticeLine.equals("")){
				String[] lineSentences = line.split(" ");
				String[] latticeLineSentences = latticeLine.split("  ");
				if(lineSentences.length != latticeLineSentences.length){
					System.out.println("Error1:"+ lineCounter +":"+lineSentences.length +":"+latticeLineSentences.length);
				}else{
					for(int i = 0; i < latticeLineSentences.length; i ++){
						String[] datas = latticeLineSentences[i].split(" ");
						for(String data : datas){
							String[] wordAndTag = data.split(",");
							if(wordAndTag.length != 4){
								System.out.println(lineCounter +":"+data);
								continue;
							}
							
							//if(wordAndTag[3].equals("0")){
								tmp.append(lineSentences[i].substring(Integer.parseInt(wordAndTag[0]), Integer.parseInt(wordAndTag[1]))
										+ "_" + wordAndTag[2] + " ");
							//}
						}
					}
					tmp.append("。");
				}
				line = in.readLine();
				latticeLine = inLattice.readLine();
			}
			p.setContent(tmp.toString().trim());
			
			out.write(J.toJson(p));
			out.newLine();
			out.flush();
		}
		in.close();
		inLattice.close();
		out.close();
	}
	
	public void createSegmentdLatticeJson(String keywordJson, String input, String latticeFile, String output)throws IOException{
		RecordReader reader = new RecordReader(keywordJson);
		BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(input),"UTF8"));
		BufferedReader inLattice = new BufferedReader(new InputStreamReader(new FileInputStream(latticeFile),"UTF8"));
		BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(output),"UTF8"));
		
		String line = "";
		String latticeLine = "";
		int lineCounter = 0;
		while(reader.next()){
			KeywordPost p = J.fromJson(reader.value(), KeywordPost.class);
			lineCounter ++;
			
			line = in.readLine();
			latticeLine = inLattice.readLine();
			String[] lineSentences = line.split(" ");
			String[] latticeLineSentences = latticeLine.split("  ");
			if(lineSentences.length != latticeLineSentences.length){
				System.out.println("Error1:"+ lineCounter +":"+lineSentences.length +":"+latticeLineSentences.length);
			}else{
				StringBuffer tmp = new StringBuffer();
				for(int i = 0; i < latticeLineSentences.length; i ++){
					String[] datas = latticeLineSentences[i].split(" ");
					for(String data : datas){
						String[] wordAndTag = data.split(",");
						if(wordAndTag.length != 4){
							System.out.println(lineCounter +":"+data);
							continue;
						}
						
						if(wordAndTag[3].equals("0")){
							tmp.append(lineSentences[i].substring(Integer.parseInt(wordAndTag[0]), Integer.parseInt(wordAndTag[1]))
									+ "_" + wordAndTag[2] + " ");
						}
						
					}
				}
				p.setTitle(tmp.toString().trim());
			}
			
			lineCounter ++;
			line = in.readLine();
			latticeLine = inLattice.readLine();
			lineSentences = line.split(" ");
			latticeLineSentences = latticeLine.split("  ");
			if(lineSentences.length != latticeLineSentences.length){
				System.out.println("Error1:"+lineCounter+":"+lineSentences.length +":"+latticeLineSentences.length);
			}else{
				StringBuffer tmp = new StringBuffer();
				for(int i = 0; i < latticeLineSentences.length; i ++){
					String[] datas = latticeLineSentences[i].split(" ");
					for(String data : datas){
						String[] wordAndTag = data.split(",");
						if(wordAndTag.length != 4){
							System.out.println(lineCounter +":"+data);
							continue;
						}
						
						if(wordAndTag[3].equals("0")){
							tmp.append(lineSentences[i].substring(Integer.parseInt(wordAndTag[0]), Integer.parseInt(wordAndTag[1]))
									+ "_" + wordAndTag[2] + " ");
						}
						
					}
					tmp.append("。");
				}
				p.setSummary(tmp.toString().trim());
			}
			
			line = in.readLine();
			latticeLine = inLattice.readLine();
			lineSentences = line.split(" ");
			latticeLineSentences = latticeLine.split("  ");
			if(lineSentences.length != latticeLineSentences.length){
				System.out.println("Error1:"+lineCounter+":"+lineSentences.length +":"+latticeLineSentences.length);
			}else{
				StringBuffer tmp = new StringBuffer();
				for(int i = 0; i < latticeLineSentences.length; i ++){
					String[] datas = latticeLineSentences[i].split(" ");
					for(String data : datas){
						String[] wordAndTag = data.split(",");
						if(wordAndTag.length != 4){
							System.out.println(lineCounter +":"+data);
							continue;
						}
						
						if(wordAndTag[3].equals("0")){
							tmp.append(lineSentences[i].substring(Integer.parseInt(wordAndTag[0]), Integer.parseInt(wordAndTag[1]))
									+ "_" + wordAndTag[2] + " ");
						}
					}
					tmp.append("。");
				}
				p.setContent(tmp.toString().trim());
			}
			
			out.write(J.toJson(p));
			out.newLine();
			out.flush();
		}
		in.close();
		inLattice.close();
		out.close();
	}
	
	public void getSinaSentence(String input, String output) throws IOException{
		RecordReader reader = new RecordReader(input);
		BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(output)));
		int lineCounter = 0;
		while(reader.next()){
			Post p = J.fromJson(reader.value(), Post.class);
			
			String paragraph = p.getTitle();
			paragraph = paragraph.replaceAll("\n", "");
			String[] sentences = paragraph.split("。|！");
			for(String sentence : sentences){
				sentence = cleanText(sentence);
				if(sentence.equals("")){
					continue;
				}
				out.write(sentence);
				out.newLine();
				out.flush();
			}
			out.newLine();
			out.flush();
			
			paragraph = p.getContent();
			paragraph = paragraph.replaceAll("\n", "");
			sentences = paragraph.split("。|！");
			for(String sentence : sentences){
				sentence = cleanText(sentence);
				if(sentence.equals("")){
					continue;
				}
				out.write(sentence);
				out.newLine();
				out.flush();
			}
			out.newLine();
			out.flush();
			
			lineCounter ++;
			if(lineCounter % 100 == 0){
				System.out.println("Done "+lineCounter+" lines.");
			}
		}
		out.close();
	}
	
	public void createSinaSegmentdJson(String keywordJson, String input, String latticeFile, String output)throws IOException{
		RecordReader reader = new RecordReader(keywordJson);
		BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(input),"UTF8"));
		BufferedReader inLattice = new BufferedReader(new InputStreamReader(new FileInputStream(latticeFile),"UTF8"));
		BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(output),"UTF8"));
		
		String line = "";
		String latticeLine = "";
		int lineCounter = 0;
		while(reader.next()){
			Post p = J.fromJson(reader.value(), Post.class);
			lineCounter ++;
			
			line = in.readLine();
			latticeLine = inLattice.readLine();
			StringBuffer tmp = new StringBuffer();
			while(!line.equals("") && !latticeLine.equals("")){
				String[] lineSentences = line.split(" ");
				String[] latticeLineSentences = latticeLine.split("  ");
				if(lineSentences.length != latticeLineSentences.length){
					System.out.println("Error1:"+ lineCounter +":" +line+":"+lineSentences.length +":"+latticeLine+":"+latticeLineSentences.length);
				}else{
					for(int i = 0; i < latticeLineSentences.length; i ++){
						String[] datas = latticeLineSentences[i].split(" ");
						for(String data : datas){
							String[] wordAndTag = data.split(",");
							if(wordAndTag.length != 4){
								System.out.println(lineCounter +":"+data);
								continue;
							}
							
							if(wordAndTag[3].equals("0")){
								tmp.append(lineSentences[i].substring(Integer.parseInt(wordAndTag[0]), Integer.parseInt(wordAndTag[1]))
										+ "_" + wordAndTag[2] + " ");
							}
						}
					}
					//tmp.append("。");
				}
				line = in.readLine();
				latticeLine = inLattice.readLine();
				lineCounter++;
			}
			p.setTitle(tmp.toString().trim());
			
			line = in.readLine();
			latticeLine = inLattice.readLine();
			tmp = new StringBuffer();
			while(!line.equals("") && !latticeLine.equals("")){
				String[] lineSentences = line.split(" ");
				String[] latticeLineSentences = latticeLine.split("  ");
				if(lineSentences.length != latticeLineSentences.length){
					System.out.println("Error1:"+ lineCounter +":"+lineSentences.length +":"+latticeLineSentences.length);
				}else{
					for(int i = 0; i < latticeLineSentences.length; i ++){
						String[] datas = latticeLineSentences[i].split(" ");
						for(String data : datas){
							String[] wordAndTag = data.split(",");
							if(wordAndTag.length != 4){
								System.out.println(lineCounter +":"+data);
								continue;
							}
							
							if(wordAndTag[3].equals("0")){
								tmp.append(lineSentences[i].substring(Integer.parseInt(wordAndTag[0]), Integer.parseInt(wordAndTag[1]))
										+ "_" + wordAndTag[2] + " ");
							}
						}
					}
					tmp.append("。");
				}
				line = in.readLine();
				latticeLine = inLattice.readLine();
				lineCounter++;
			}
			p.setContent(tmp.toString().trim());
			
			out.write(J.toJson(p));
			out.newLine();
			out.flush();
		}
		in.close();
		inLattice.close();
		out.close();
	}
	
	public void createSinaSegmentdLatticeAsNormal(String keywordJson, String input, String latticeFile, String output)throws IOException{
		RecordReader reader = new RecordReader(keywordJson);
		BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(input),"UTF8"));
		BufferedReader inLattice = new BufferedReader(new InputStreamReader(new FileInputStream(latticeFile),"UTF8"));
		BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(output),"UTF8"));
		
		String line = "";
		String latticeLine = "";
		int lineCounter = 0;
		while(reader.next()){
			Post p = J.fromJson(reader.value(), Post.class);
			lineCounter ++;
			
			line = in.readLine();
			latticeLine = inLattice.readLine();
			StringBuffer tmp = new StringBuffer();
			while(!line.equals("") && !latticeLine.equals("")){
				String[] lineSentences = line.split(" ");
				String[] latticeLineSentences = latticeLine.split("  ");
				if(lineSentences.length != latticeLineSentences.length){
					System.out.println("Error1:"+ lineCounter +":" +line+":"+lineSentences.length +":"+latticeLine+":"+latticeLineSentences.length);
				}else{
					for(int i = 0; i < latticeLineSentences.length; i ++){
						String[] datas = latticeLineSentences[i].split(" ");
						for(String data : datas){
							String[] wordAndTag = data.split(",");
							if(wordAndTag.length != 4){
								System.out.println(lineCounter +":"+data);
								continue;
							}
							
							//if(wordAndTag[3].equals("0")){
								tmp.append(lineSentences[i].substring(Integer.parseInt(wordAndTag[0]), Integer.parseInt(wordAndTag[1]))
										+ "_" + wordAndTag[2] + " ");
							//}
						}
					}
					//tmp.append("。");
				}
				line = in.readLine();
				latticeLine = inLattice.readLine();
				lineCounter++;
			}
			p.setTitle(tmp.toString().trim());
			
			line = in.readLine();
			latticeLine = inLattice.readLine();
			tmp = new StringBuffer();
			while(!line.equals("") && !latticeLine.equals("")){
				String[] lineSentences = line.split(" ");
				String[] latticeLineSentences = latticeLine.split("  ");
				if(lineSentences.length != latticeLineSentences.length){
					System.out.println("Error1:"+ lineCounter +":"+lineSentences.length +":"+latticeLineSentences.length);
				}else{
					for(int i = 0; i < latticeLineSentences.length; i ++){
						String[] datas = latticeLineSentences[i].split(" ");
						for(String data : datas){
							String[] wordAndTag = data.split(",");
							if(wordAndTag.length != 4){
								System.out.println(lineCounter +":"+data);
								continue;
							}
							
							//if(wordAndTag[3].equals("0")){
							try{
								tmp.append(lineSentences[i].substring(Integer.parseInt(wordAndTag[0]), Integer.parseInt(wordAndTag[1]))
										+ "_" + wordAndTag[2] + " ");
							}catch (NumberFormatException e) {
								// TODO: handle exception
								System.out.println(lineCounter+":"+wordAndTag);
								e.printStackTrace();
								
							}
							//}
						}
					}
					tmp.append("。");
				}
				line = in.readLine();
				latticeLine = inLattice.readLine();
				lineCounter++;
			}
			p.setContent(tmp.toString().trim());
			
			out.write(J.toJson(p));
			out.newLine();
			out.flush();
		}
		in.close();
		inLattice.close();
		out.close();
	}
	
	public void checkSinaNoLatticeDetail(String keywordJson, String input, String output) throws IOException{
		RecordReader reader = new RecordReader(keywordJson);
		BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(input),"UTF8"));
		BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(output),"UTF8"));
		
		int wordCount = 0;
		int artitleNums = 0;
		int inTitle = 0;
		int inContent = 0;
		int notInAny = 0;
		int keywordNums = 0;
		
		HashMap<String, String> titleWordMap = new HashMap<String, String>();
		HashMap<String, String> contentWordMap = new HashMap<String, String>();
		
		HashMap<String, Integer> titlePosMap = new HashMap<String, Integer>();
		HashMap<String, Integer> contentPosMap = new HashMap<String, Integer>();
		
		HashSet<String> allTitleWordSet = new HashSet<String>();
		HashSet<String> notInAnySet = new HashSet<String>();
		
		int notInTitle = 0;
		int notInAllTitle = 0;
		
		Vector<String> titleVec = new Vector<String>();
		long totalLength = 0;
		
		String line = "";
		while(reader.next()){
			artitleNums ++;
			titleWordMap.clear();
			contentWordMap.clear();
			
			Post p = J.fromJson(reader.value(), Post.class);
			titleVec.add(p.getTitle());
			
			line = in.readLine();
			while(!line.equals("")){
				String[] datas = line.split(" ");
				for(String data : datas){
					String[] wordAndTag = data.split("_");
					if(wordAndTag.length != 2){
						System.out.println(reader.numRead()+":"+data);
						continue;
					}
					titleWordMap.put(wordAndTag[0], wordAndTag[1]);
					allTitleWordSet.add(wordAndTag[0]);
					wordCount ++;
					
					totalLength += wordAndTag[0].length();
					
				}
				line = in.readLine();
			}
			
			line = in.readLine();
			while(!line.equals("")){
				String[] datas = line.split(" ");
				for(String data : datas){
					String[] wordAndTag = data.split("_");
					if(wordAndTag.length != 2){
						System.out.println(reader.numRead()+":"+data);
						continue;
					}
					contentWordMap.put(wordAndTag[0], wordAndTag[1]);
					wordCount ++;
					totalLength += wordAndTag[0].length();
				}
				line = in.readLine();
			}
			
			for(String tag : p.getTags()){
				keywordNums ++;
				boolean isTitle = false;
				boolean isContent = false;
				
				if(titleWordMap.containsKey(tag)){
					inTitle ++;
					isTitle = true;
					String label = titleWordMap.get(tag);
					Integer tmpInt = titlePosMap.get(label);
					if(tmpInt != null){
						titlePosMap.put(label, tmpInt + 1);
					}else{
						titlePosMap.put(label, 1);
					}
				}else{
					notInTitle ++;
				}
				if(contentWordMap.containsKey(tag)){
					inContent ++;
					isContent = true;
					String label = contentWordMap.get(tag);
					Integer tmpInt = contentPosMap.get(label);
					if(tmpInt != null){
						contentPosMap.put(label, tmpInt + 1);
					}else{
						contentPosMap.put(label, 1);
					}
				}
				
				if(!isTitle && !isContent){
					notInAny ++;
					notInAnySet.add(tag);
				}

			}
			
			if(reader.numRead() % 1000 == 0){
				System.out.println("Done " + reader.numRead() + " lines!");
			}
		}
		
		out.write("artitleNums:"+artitleNums);
		out.newLine();
		out.write("totalLength:"+totalLength);
		out.newLine();
		out.write("averageLength:"+((double)totalLength) / (double)artitleNums);
		out.newLine();
		out.write("inTitle:"+inTitle);
		out.newLine();
		for(Entry<String, Integer> e : titlePosMap.entrySet()){
			out.write(e.getKey()+":"+e.getValue());
			out.newLine();
		}
		out.newLine();
		out.write("inContent:"+inContent);
		out.newLine();
		for(Entry<String, Integer> e : contentPosMap.entrySet()){
			out.write(e.getKey()+":"+e.getValue());
			out.newLine();
		}
		out.newLine();
		out.write("keywordNums:"+keywordNums);
		out.newLine();
		out.write("notInAny:"+notInAny);
		out.newLine();
		out.write("wordCount:"+wordCount);
		out.newLine();
		out.write("notInTitle:"+notInTitle);
		out.newLine();
		out.write("notInAllTitle:"+notInAllTitle);
		out.newLine();
		out.close();
	}

	
	public void checkSinaNoLatticePerform(String keywordJson, String input, String output) throws IOException{
		RecordReader reader = new RecordReader(keywordJson);
		BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(input),"UTF8"));
		BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(output),"UTF8"));
		
		int wordCount = 0;
		int artitleNums = 0;
		int inTitle = 0;
		int inContent = 0;
		int notInAny = 0;
		int keywordNums = 0;
		
		HashMap<String, String> titleWordMap = new HashMap<String, String>();
		HashMap<String, String> contentWordMap = new HashMap<String, String>();
		
		HashMap<String, Integer> titlePosMap = new HashMap<String, Integer>();
		HashMap<String, Integer> contentPosMap = new HashMap<String, Integer>();
		
		HashSet<String> allTitleWordSet = new HashSet<String>();
		HashSet<String> allDocumentWordSet = new HashSet<String>();
		HashSet<String> notInAnySet = new HashSet<String>();
		
		HashMap<Integer, Integer> tagLengthMap = new HashMap<Integer, Integer>();
		
		int notInTitle = 0;
		int notInAllTitle = 0;
		
		Vector<String> titleVec = new Vector<String>();
		
		String line = "";
		while(reader.next()){
			artitleNums ++;
			titleWordMap.clear();
			contentWordMap.clear();
			
			Post p = J.fromJson(reader.value(), Post.class);
			titleVec.add(p.getTitle());
			
			line = in.readLine();
			while(!line.equals("")){
				String[] datas = line.split(" ");
				for(String data : datas){
					String[] wordAndTag = data.split("_");
					if(wordAndTag.length != 2){
						System.out.println(reader.numRead()+":"+data);
						continue;
					}
					titleWordMap.put(wordAndTag[0], wordAndTag[1]);
					allTitleWordSet.add(wordAndTag[0]);
					allDocumentWordSet.add(wordAndTag[0]);
					wordCount ++;
					
				}
				line = in.readLine();
			}
			
			line = in.readLine();
			while(!line.equals("")){
				String[] datas = line.split(" ");
				for(String data : datas){
					String[] wordAndTag = data.split("_");
					if(wordAndTag.length != 2){
						System.out.println(reader.numRead()+":"+data);
						continue;
					}
					contentWordMap.put(wordAndTag[0], wordAndTag[1]);
					allDocumentWordSet.add(wordAndTag[0]);
					wordCount ++;
				}
				line = in.readLine();
			}
			
			for(String tag : p.getTags()){
				
				int tagLength = tag.length();
				if(!tagLengthMap.containsKey(tagLength)){
					tagLengthMap.put(tagLength, 0);
				}
				tagLengthMap.put(tagLength, tagLengthMap.get(tagLength) + 1);
				
				keywordNums ++;
				boolean isTitle = false;
				boolean isContent = false;
				
				if(titleWordMap.containsKey(tag)){
					inTitle ++;
					isTitle = true;
					String label = titleWordMap.get(tag);
					Integer tmpInt = titlePosMap.get(label);
					if(tmpInt != null){
						titlePosMap.put(label, tmpInt + 1);
					}else{
						titlePosMap.put(label, 1);
					}
				}else{
					notInTitle ++;
				}
				if(contentWordMap.containsKey(tag)){
					inContent ++;
					isContent = true;
					String label = contentWordMap.get(tag);
					Integer tmpInt = contentPosMap.get(label);
					if(tmpInt != null){
						contentPosMap.put(label, tmpInt + 1);
					}else{
						contentPosMap.put(label, 1);
					}
				}
				
				if(!isTitle && !isContent){
					notInAny ++;
					notInAnySet.add(tag);
				}

			}
			
			if(reader.numRead() % 1000 == 0){
				System.out.println("Done " + reader.numRead() + " lines!");
			}
		}
		
		System.out.println("notInAnySet.size():"+notInAnySet.size());
		int notInAnySetCounter = 0;
		//int notInAllOriTitle = 0;
		int notInAllDocument = 0;
		for(String tag : notInAnySet){
			if(!allTitleWordSet.contains(tag)){
				notInAllTitle ++;
			}
			if(!allDocumentWordSet.contains(tag)){
				notInAllDocument ++;
			}
			
			/*
			boolean inOriTitle = false;
			for(String title : titleVec){
				if(title.contains(tag)){
					inOriTitle = true;
					break;
				}
			}
			if(!inOriTitle){
				notInAllOriTitle ++;
			}
			*/
			
			notInAnySetCounter ++;
			if(notInAnySetCounter % 1000 == 0){
				System.out.println("Done "+notInAnySetCounter+ " tags");
			}
		}
		
		
		
		out.write("artitleNums:"+artitleNums);
		out.newLine();
		out.write("inTitle:"+inTitle);
		out.newLine();
		for(Entry<String, Integer> e : titlePosMap.entrySet()){
			out.write(e.getKey()+":"+e.getValue());
			out.newLine();
		}
		out.newLine();
		out.write("inContent:"+inContent);
		out.newLine();
		for(Entry<String, Integer> e : contentPosMap.entrySet()){
			out.write(e.getKey()+":"+e.getValue());
			out.newLine();
		}
		out.newLine();
		out.write("keywordNums:"+keywordNums);
		out.newLine();
		
		out.write("inTitle:"+(keywordNums - notInTitle));
		out.newLine();
		out.write("inDocument:"+(keywordNums - notInAny));
		out.newLine();
		out.write("inAllTitle:"+(keywordNums - notInAllTitle));
		out.newLine();
		out.write("inAllDocument:"+(keywordNums - notInAllDocument));
		out.newLine();
		
		out.write("notInAny:"+notInAny);
		out.newLine();
		out.write("wordCount:"+wordCount);
		out.newLine();
		out.write("notInTitle:"+notInTitle);
		out.newLine();
		out.write("notInAllTitle:"+notInAllTitle);
		out.newLine();
		
		for(Entry<Integer, Integer> e : tagLengthMap.entrySet()){
			out.write(e.getKey() + ":"+e.getValue());
			out.newLine();
		}
		
		/*
		out.write("notInAllOriTitle:"+notInAllOriTitle);
		out.newLine();
		*/
		out.close();
	}
	
	public void checkSinaLatticePerform(String keywordJson, String input, String latticeFile, String output) throws IOException{
		RecordReader reader = new RecordReader(keywordJson);
		BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(input),"UTF8"));
		BufferedReader inLattice = new BufferedReader(new InputStreamReader(new FileInputStream(latticeFile),"UTF8"));
		BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(output),"UTF8"));
		
		HashMap<String, Integer> tagCountMap = new HashMap<String, Integer>();
		while(reader.next()){
			KeywordPost p = J.fromJson(reader.value(), KeywordPost.class);
			for(String tag : p.getTags()){
				if(!tagCountMap.containsKey(tag)){
					tagCountMap.put(tag, 0);
				}
				tagCountMap.put(tag, tagCountMap.get(tag)+1);
			}
		}
		reader.close();
		
		int wordCount = 0;
		int latticeWordCount = 0;
		
		int artitleNums = 0;
		int inTitle = 0;
		int inContent = 0;
		int notInAny = 0;
		int keywordNums = 0;
		double wordAverageLength = 0.0;
		double latticeAverageLength = 0.0;
		double totalAverageLength = 0.0;
		double keywordAverageLength = 0.0;
		
		HashMap<String, String> titleWordMap = new HashMap<String, String>();
		HashMap<String, String> contentWordMap = new HashMap<String, String>();
		
		HashMap<String, Integer> titlePosMap = new HashMap<String, Integer>();
		HashMap<String, Integer> contentPosMap = new HashMap<String, Integer>();
		
		HashSet<String> allTitleWordSet = new HashSet<String>();
		HashSet<String> allDocumentWordSet = new HashSet<String>();
		
		String line = "";
		String latticeLine = "";
		
		HashSet<String> notInAnySet = new HashSet<String>();
		reader = new RecordReader(keywordJson);
		
		int lineCounter = 0;
		
		while(reader.next()){
			artitleNums ++;
			titleWordMap.clear();
			contentWordMap.clear();
			
			Post p = J.fromJson(reader.value(), Post.class);
			lineCounter ++;
			
			line = in.readLine();
			latticeLine = inLattice.readLine();
			while(!line.equals("") && !latticeLine.equals("")){
				String[] lineSentences = line.split(" ");
				String[] latticeLineSentences = latticeLine.split("  ");
				if(lineSentences.length != latticeLineSentences.length){
					System.out.println("Error1:"+ lineCounter +":" +line+":"+lineSentences.length +":"+latticeLine+":"+latticeLineSentences.length);
				}else{
					for(int i = 0; i < latticeLineSentences.length; i ++){
						String[] datas = latticeLineSentences[i].split(" ");
						for(String data : datas){
							String[] wordAndTag = data.split(",");
							if(wordAndTag.length != 4){
								System.out.println(reader.numRead()+":"+data);
								continue;
							}
							String word = null;
							try{
								word = lineSentences[i].substring(Integer.parseInt(wordAndTag[0]), Integer.parseInt(wordAndTag[1]));
							}catch (NumberFormatException e) {
								// TODO: handle exception
								System.out.println(lineCounter+":"+wordAndTag);
								e.printStackTrace();
								continue;
							}
							totalAverageLength += word.length();
							
							titleWordMap.put(word, wordAndTag[2]);
							if(wordAndTag[3].equals("0")){
								wordAverageLength += word.length();
								wordCount ++;
							}else{
								latticeAverageLength += word.length();
								latticeWordCount ++;
							}
							allTitleWordSet.add(word);
							allDocumentWordSet.add(word);
						}
					}
				}
				line = in.readLine();
				latticeLine = inLattice.readLine();
				lineCounter++;
			}
			
			line = in.readLine();
			latticeLine = inLattice.readLine();
			while(!line.equals("") && !latticeLine.equals("")){
				String[] lineSentences = line.split(" ");
				String[] latticeLineSentences = latticeLine.split("  ");
				if(lineSentences.length != latticeLineSentences.length){
					System.out.println("Error1:"+ lineCounter +":"+lineSentences.length +":"+latticeLineSentences.length);
				}else{
					for(int i = 0; i < latticeLineSentences.length; i ++){
						String[] datas = latticeLineSentences[i].split(" ");
						for(String data : datas){
							String[] wordAndTag = data.split(",");
							if(wordAndTag.length != 4){
								System.out.println(lineCounter +":"+data);
								continue;
							}
							
							String word = null;
							try{
								word = lineSentences[i].substring(Integer.parseInt(wordAndTag[0]), Integer.parseInt(wordAndTag[1]));
							}catch (NumberFormatException e) {
								// TODO: handle exception
								System.out.println(lineCounter+":"+wordAndTag);
								e.printStackTrace();
								continue;
							}
							totalAverageLength += word.length();
							
							contentWordMap.put(word, wordAndTag[2]);
							if(wordAndTag[3].equals("0")){
								wordAverageLength += word.length();
								wordCount ++;
							}else{
								latticeAverageLength += word.length();
								latticeWordCount ++;
							}
							allDocumentWordSet.add(word);
						}
					}
					
				}
				line = in.readLine();
				latticeLine = inLattice.readLine();
				lineCounter++;
			}
			
			for(String tag : p.getTags()){
				keywordNums ++;
				keywordAverageLength += tag.length();
				boolean isTitle = false;
				boolean isContent = false;
				
				if(titleWordMap.containsKey(tag)){
					inTitle ++;
					isTitle = true;
					String label = titleWordMap.get(tag);
					Integer tmpInt = titlePosMap.get(label);
					if(tmpInt != null){
						titlePosMap.put(label, tmpInt + 1);
					}else{
						titlePosMap.put(label, 1);
					}
				}
				
				if(contentWordMap.containsKey(tag)){
					inContent ++;
					isContent = true;
					String label = contentWordMap.get(tag);
					Integer tmpInt = contentPosMap.get(label);
					if(tmpInt != null){
						contentPosMap.put(label, tmpInt + 1);
					}else{
						contentPosMap.put(label, 1);
					}
				}
				
				if(!isTitle && !isContent){
					notInAnySet.add(tag);
					notInAny ++;
				}
			}
		}
		
		System.out.println("notInAnySet.size():"+notInAnySet.size());
		int notInAnySetCounter = 0;
		int notInAllTitle = 0;
		int notInAllDocument = 0;
		for(String tag : notInAnySet){
			if(!allTitleWordSet.contains(tag)){
				notInAllTitle ++;
			}
			if(!allDocumentWordSet.contains(tag)){
				notInAllDocument ++;
			}
			
			/*
			boolean inOriTitle = false;
			for(String title : titleVec){
				if(title.contains(tag)){
					inOriTitle = true;
					break;
				}
			}
			*/
			
			notInAnySetCounter ++;
			if(notInAnySetCounter % 1000 == 0){
				System.out.println("Done "+notInAnySetCounter+ " tags");
			}
		}
		
		out.write("artitleNums:"+artitleNums);
		out.newLine();
		out.write("inTitle:"+inTitle);
		out.newLine();
		
		for(Entry<String, Integer> e : titlePosMap.entrySet()){
			out.write(e.getKey()+":"+e.getValue());
			out.newLine();
		}
		out.newLine();
		out.write("inContent:"+inContent);
		out.newLine();
		for(Entry<String, Integer> e : contentPosMap.entrySet()){
			out.write(e.getKey()+":"+e.getValue());
			out.newLine();
		}
		out.newLine();
		out.write("keywordNums:"+keywordNums);
		out.newLine();
		
		out.write("inTitle:"+inTitle);
		out.newLine();
		out.write("inDocument:"+(keywordNums - notInAny));
		out.newLine();
		out.write("inAllTitle:"+(keywordNums - notInAllTitle));
		out.newLine();
		out.write("inAllDocument:"+(keywordNums - notInAllDocument));
		out.newLine();
		
		out.write("keywordAverageLength:"+(keywordAverageLength / ((double)keywordNums)));
		out.newLine();
		out.write("notInAny:"+notInAny);
		out.newLine();
		out.write("wordCount:"+wordCount);
		out.newLine();
		out.write("latticeWordCount:"+latticeWordCount);
		out.newLine();
		out.write("wordAverageLength:"+(wordAverageLength / ((double)wordCount)));
		out.newLine();
		out.write("latticeAverageLength:"+(latticeAverageLength / ((double)latticeWordCount)));
		out.newLine();
		out.write("totalAverageLength:"+(totalAverageLength / ((double)(wordCount + latticeWordCount))));
		out.newLine();
		out.close();
	}
	
	public void createLDATrainData(String keywordJson, String input, String latticeFile, String output)throws IOException{
		RecordReader reader = new RecordReader(keywordJson);
		BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(input),"UTF8"));
		BufferedReader inLattice = new BufferedReader(new InputStreamReader(new FileInputStream(latticeFile),"UTF8"));
		BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(output),"UTF8"));
		
		String line = "";
		String latticeLine = "";
		int lineCounter = 0;
		out.write("13702");
		out.newLine();
		out.flush();
		while(reader.next()){
			//KeywordPost p = J.fromJson(reader.value(), KeywordPost.class);
			lineCounter ++;
			
			line = in.readLine();
			latticeLine = inLattice.readLine();
			StringBuffer tmp = new StringBuffer();
			while(!line.equals("") && !latticeLine.equals("")){
				String[] lineSentences = line.split(" ");
				String[] latticeLineSentences = latticeLine.split("  ");
				if(lineSentences.length != latticeLineSentences.length){
					System.out.println("Error1:"+ lineCounter +":" +line+":"+lineSentences.length +":"+latticeLine+":"+latticeLineSentences.length);
				}else{
					for(int i = 0; i < latticeLineSentences.length; i ++){
						String[] datas = latticeLineSentences[i].split(" ");
						for(String data : datas){
							String[] wordAndTag = data.split(",");
							if(wordAndTag.length != 4){
								System.out.println(lineCounter +":"+data);
								continue;
							}
							/*
							tmp.append(lineSentences[i].substring(Integer.parseInt(wordAndTag[0]), Integer.parseInt(wordAndTag[1]))
									+ "_" + wordAndTag[2] + " ");
							*/
							
							String word =lineSentences[i].substring(Integer.parseInt(wordAndTag[0]), Integer.parseInt(wordAndTag[1]));
							tmp.append(word+" ");
						}
					}
				}
				line = in.readLine();
				latticeLine = inLattice.readLine();
			}
			//p.setTitle(tmp.toString().trim());
			out.write(tmp.toString());
			out.flush();
			
			
			line = in.readLine();
			latticeLine = inLattice.readLine();
			tmp = new StringBuffer();
			while(!line.equals("") && !latticeLine.equals("")){
				String[] lineSentences = line.split(" ");
				String[] latticeLineSentences = latticeLine.split("  ");
				if(lineSentences.length != latticeLineSentences.length){
					System.out.println("Error1:"+ lineCounter +":"+lineSentences.length +":"+latticeLineSentences.length);
				}else{
					for(int i = 0; i < latticeLineSentences.length; i ++){
						String[] datas = latticeLineSentences[i].split(" ");
						for(String data : datas){
							String[] wordAndTag = data.split(",");
							if(wordAndTag.length != 4){
								System.out.println(lineCounter +":"+data);
								continue;
							}
							String word =lineSentences[i].substring(Integer.parseInt(wordAndTag[0]), Integer.parseInt(wordAndTag[1]));
							tmp.append(word+" ");
						}
					}
				}
				line = in.readLine();
				latticeLine = inLattice.readLine();
			}
			out.write(tmp.toString());
			out.flush();
			
			line = in.readLine();
			latticeLine = inLattice.readLine();
			tmp = new StringBuffer();
			while(!line.equals("") && !latticeLine.equals("")){
				String[] lineSentences = line.split(" ");
				String[] latticeLineSentences = latticeLine.split("  ");
				if(lineSentences.length != latticeLineSentences.length){
					System.out.println("Error1:"+ lineCounter +":"+lineSentences.length +":"+latticeLineSentences.length);
				}else{
					for(int i = 0; i < latticeLineSentences.length; i ++){
						String[] datas = latticeLineSentences[i].split(" ");
						for(String data : datas){
							String[] wordAndTag = data.split(",");
							if(wordAndTag.length != 4){
								System.out.println(lineCounter +":"+data);
								continue;
							}
							String word =lineSentences[i].substring(Integer.parseInt(wordAndTag[0]), Integer.parseInt(wordAndTag[1]));
							tmp.append(word+" ");
						}
					}
				}
				line = in.readLine();
				latticeLine = inLattice.readLine();
			}
			out.write(tmp.toString());
			out.newLine();
			out.flush();
		}
		in.close();
		inLattice.close();
		out.close();
	}
	
	public void createLDATrainDataThulac(String keywordJson, String input, String latticeFile, String output)throws IOException{
		RecordReader reader = new RecordReader(keywordJson);
		BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(input),"UTF8"));
		BufferedReader inLattice = new BufferedReader(new InputStreamReader(new FileInputStream(latticeFile),"UTF8"));
		BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(output),"UTF8"));
		
		String line = "";
		String latticeLine = "";
		int lineCounter = 0;
		out.write("13702");
		out.newLine();
		out.flush();
		while(reader.next()){
			//KeywordPost p = J.fromJson(reader.value(), KeywordPost.class);
			lineCounter ++;
			
			line = in.readLine();
			latticeLine = inLattice.readLine();
			StringBuffer tmp = new StringBuffer();
			while(!line.equals("") && !latticeLine.equals("")){
				String[] lineSentences = line.split(" ");
				String[] latticeLineSentences = latticeLine.split("  ");
				if(lineSentences.length != latticeLineSentences.length){
					System.out.println("Error1:"+ lineCounter +":" +line+":"+lineSentences.length +":"+latticeLine+":"+latticeLineSentences.length);
				}else{
					for(int i = 0; i < latticeLineSentences.length; i ++){
						String[] datas = latticeLineSentences[i].split(" ");
						for(String data : datas){
							String[] wordAndTag = data.split(",");
							if(wordAndTag.length != 4){
								System.out.println(lineCounter +":"+data);
								continue;
							}
							/*
							tmp.append(lineSentences[i].substring(Integer.parseInt(wordAndTag[0]), Integer.parseInt(wordAndTag[1]))
									+ "_" + wordAndTag[2] + " ");
							*/
							
							if(!wordAndTag[3].equals("0")){
								continue;
							}
							String word =lineSentences[i].substring(Integer.parseInt(wordAndTag[0]), Integer.parseInt(wordAndTag[1]));
							if(word.length() < 2){
								continue;
							}
							tmp.append(word+" ");
						}
					}
				}
				line = in.readLine();
				latticeLine = inLattice.readLine();
			}
			//p.setTitle(tmp.toString().trim());
			out.write(tmp.toString());
			out.flush();
			
			
			line = in.readLine();
			latticeLine = inLattice.readLine();
			tmp = new StringBuffer();
			while(!line.equals("") && !latticeLine.equals("")){
				String[] lineSentences = line.split(" ");
				String[] latticeLineSentences = latticeLine.split("  ");
				if(lineSentences.length != latticeLineSentences.length){
					System.out.println("Error1:"+ lineCounter +":"+lineSentences.length +":"+latticeLineSentences.length);
				}else{
					for(int i = 0; i < latticeLineSentences.length; i ++){
						String[] datas = latticeLineSentences[i].split(" ");
						for(String data : datas){
							String[] wordAndTag = data.split(",");
							if(wordAndTag.length != 4){
								System.out.println(lineCounter +":"+data);
								continue;
							}
							
							if(!wordAndTag[3].equals("0")){
								continue;
							}
							String word =lineSentences[i].substring(Integer.parseInt(wordAndTag[0]), Integer.parseInt(wordAndTag[1]));
							if(word.length() < 2){
								continue;
							}
							tmp.append(word+" ");
						}
					}
				}
				line = in.readLine();
				latticeLine = inLattice.readLine();
			}
			out.write(tmp.toString());
			out.flush();
			
			line = in.readLine();
			latticeLine = inLattice.readLine();
			tmp = new StringBuffer();
			while(!line.equals("") && !latticeLine.equals("")){
				String[] lineSentences = line.split(" ");
				String[] latticeLineSentences = latticeLine.split("  ");
				if(lineSentences.length != latticeLineSentences.length){
					System.out.println("Error1:"+ lineCounter +":"+lineSentences.length +":"+latticeLineSentences.length);
				}else{
					for(int i = 0; i < latticeLineSentences.length; i ++){
						String[] datas = latticeLineSentences[i].split(" ");
						for(String data : datas){
							String[] wordAndTag = data.split(",");
							if(wordAndTag.length != 4){
								System.out.println(lineCounter +":"+data);
								continue;
							}
							if(!wordAndTag[3].equals("0")){
								continue;
							}
							String word =lineSentences[i].substring(Integer.parseInt(wordAndTag[0]), Integer.parseInt(wordAndTag[1]));
							if(word.length() < 2){
								continue;
							}
							tmp.append(word+" ");
						}
					}
				}
				line = in.readLine();
				latticeLine = inLattice.readLine();
			}
			out.write(tmp.toString());
			out.newLine();
			out.flush();
		}
		in.close();
		inLattice.close();
		out.close();
	}
	
	public void createSinaLDATrainData(String sinaJson, String input, String latticeFile, String output)throws IOException{
		RecordReader reader = new RecordReader(sinaJson);
		BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(input),"UTF8"));
		BufferedReader inLattice = new BufferedReader(new InputStreamReader(new FileInputStream(latticeFile),"UTF8"));
		BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(output),"UTF8"));
		
		String line = "";
		String latticeLine = "";
		int lineCounter = 0;
		out.write("175271");
		out.newLine();
		out.flush();
		while(reader.next()){
			//KeywordPost p = J.fromJson(reader.value(), KeywordPost.class);
			lineCounter ++;
			
			line = in.readLine();
			latticeLine = inLattice.readLine();
			StringBuffer tmp = new StringBuffer();
			while(!line.equals("") && !latticeLine.equals("")){
				String[] lineSentences = line.split(" ");
				String[] latticeLineSentences = latticeLine.split("  ");
				if(lineSentences.length != latticeLineSentences.length){
					System.out.println("Error1:"+ lineCounter +":" +line+":"+lineSentences.length +":"+latticeLine+":"+latticeLineSentences.length);
				}else{
					for(int i = 0; i < latticeLineSentences.length; i ++){
						String[] datas = latticeLineSentences[i].split(" ");
						for(String data : datas){
							String[] wordAndTag = data.split(",");
							if(wordAndTag.length != 4){
								System.out.println(lineCounter +":"+data);
								continue;
							}
							/*
							tmp.append(lineSentences[i].substring(Integer.parseInt(wordAndTag[0]), Integer.parseInt(wordAndTag[1]))
									+ "_" + wordAndTag[2] + " ");
							*/
							
							if(!wordAndTag[3].equals("0")){
								continue;
							}
							String word =lineSentences[i].substring(Integer.parseInt(wordAndTag[0]), Integer.parseInt(wordAndTag[1]));
							if(word.length() < 2){
								continue;
							}
							tmp.append(word+" ");
						}
					}
				}
				line = in.readLine();
				latticeLine = inLattice.readLine();
			}
			//p.setTitle(tmp.toString().trim());
			out.write(tmp.toString());
			out.flush();
			
			line = in.readLine();
			latticeLine = inLattice.readLine();
			tmp = new StringBuffer();
			while(!line.equals("") && !latticeLine.equals("")){
				String[] lineSentences = line.split(" ");
				String[] latticeLineSentences = latticeLine.split("  ");
				if(lineSentences.length != latticeLineSentences.length){
					System.out.println("Error1:"+ lineCounter +":"+lineSentences.length +":"+latticeLineSentences.length);
				}else{
					for(int i = 0; i < latticeLineSentences.length; i ++){
						String[] datas = latticeLineSentences[i].split(" ");
						for(String data : datas){
							String[] wordAndTag = data.split(",");
							if(wordAndTag.length != 4){
								System.out.println(lineCounter +":"+data);
								continue;
							}
							if(!wordAndTag[3].equals("0")){
								continue;
							}
							String word =lineSentences[i].substring(Integer.parseInt(wordAndTag[0]), Integer.parseInt(wordAndTag[1]));
							if(word.length() < 2){
								continue;
							}
							tmp.append(word+" ");
						}
					}
				}
				line = in.readLine();
				latticeLine = inLattice.readLine();
			}
			out.write(tmp.toString());
			out.newLine();
			out.flush();
		}
		in.close();
		inLattice.close();
		out.close();
	}
	
	public void createLDALatticeAsNormal(String keywordJson, String input, String latticeFile, String ldaFile, String output)throws IOException{
		RecordReader reader = new RecordReader(keywordJson);
		BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(input),"UTF8"));
		BufferedReader inLattice = new BufferedReader(new InputStreamReader(new FileInputStream(latticeFile),"UTF8"));
		BufferedReader inLDA = new BufferedReader(new InputStreamReader(new FileInputStream(ldaFile),"UTF8"));
		BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(output),"UTF8"));
		
		String line = "";
		String latticeLine = "";
		String ldaLine = "";
		int ldaCounter = 0;
		int lineCounter = 0;
		while(reader.next()){
			KeywordPost p = J.fromJson(reader.value(), KeywordPost.class);
			lineCounter ++;
			ldaLine = inLDA.readLine();
			String[] ldaDatas = ldaLine.split(" ");
			ldaCounter = 0;
			
			line = in.readLine();
			latticeLine = inLattice.readLine();
			
			StringBuffer tmp = new StringBuffer();
			while(!line.equals("") && !latticeLine.equals("")){
				String[] lineSentences = line.split(" ");
				String[] latticeLineSentences = latticeLine.split("  ");
				
				if(lineSentences.length != latticeLineSentences.length){
					System.out.println("Error1:"+ lineCounter +":" +line+":"+lineSentences.length +":"+latticeLine+":"+latticeLineSentences.length);
				}else{
					for(int i = 0; i < latticeLineSentences.length; i ++){
						String[] datas = latticeLineSentences[i].split(" ");
						for(String data : datas){
							String[] wordAndTag = data.split(",");
							if(wordAndTag.length != 4){
								System.out.println(lineCounter +":"+data);
								continue;
							}
							
							tmp.append(lineSentences[i].substring(Integer.parseInt(wordAndTag[0]), Integer.parseInt(wordAndTag[1]))
										+ "_" + wordAndTag[2] + "_" + ldaDatas[ldaCounter].split(":")[1] + " ");
							ldaCounter ++;
						}
					}
				}
				line = in.readLine();
				latticeLine = inLattice.readLine();
			}
			p.setTitle(tmp.toString().trim());
			
			
			line = in.readLine();
			latticeLine = inLattice.readLine();
			tmp = new StringBuffer();
			while(!line.equals("") && !latticeLine.equals("")){
				String[] lineSentences = line.split(" ");
				String[] latticeLineSentences = latticeLine.split("  ");
				if(lineSentences.length != latticeLineSentences.length){
					System.out.println("Error1:"+ lineCounter +":"+lineSentences.length +":"+latticeLineSentences.length);
				}else{
					for(int i = 0; i < latticeLineSentences.length; i ++){
						String[] datas = latticeLineSentences[i].split(" ");
						for(String data : datas){
							String[] wordAndTag = data.split(",");
							if(wordAndTag.length != 4){
								System.out.println(lineCounter +":"+data);
								continue;
							}
							
							tmp.append(lineSentences[i].substring(Integer.parseInt(wordAndTag[0]), Integer.parseInt(wordAndTag[1]))
										+ "_" + wordAndTag[2] + "_" + ldaDatas[ldaCounter].split(":")[1] + " ");
							ldaCounter ++;
						}
					}
					tmp.append("。");
				}
				line = in.readLine();
				latticeLine = inLattice.readLine();
			}
			p.setSummary(tmp.toString().trim());
			
			line = in.readLine();
			latticeLine = inLattice.readLine();
			tmp = new StringBuffer();
			while(!line.equals("") && !latticeLine.equals("")){
				String[] lineSentences = line.split(" ");
				String[] latticeLineSentences = latticeLine.split("  ");
				if(lineSentences.length != latticeLineSentences.length){
					System.out.println("Error1:"+ lineCounter +":"+lineSentences.length +":"+latticeLineSentences.length);
				}else{
					for(int i = 0; i < latticeLineSentences.length; i ++){
						String[] datas = latticeLineSentences[i].split(" ");
						for(String data : datas){
							String[] wordAndTag = data.split(",");
							if(wordAndTag.length != 4){
								System.out.println(lineCounter +":"+data);
								continue;
							}
							
							tmp.append(lineSentences[i].substring(Integer.parseInt(wordAndTag[0]), Integer.parseInt(wordAndTag[1]))
									+ "_" + wordAndTag[2] + "_" + ldaDatas[ldaCounter].split(":")[1] + " ");
							ldaCounter ++;
						}
					}
					tmp.append("。");
				}
				line = in.readLine();
				latticeLine = inLattice.readLine();
			}
			p.setContent(tmp.toString().trim());
			
			if(ldaCounter != ldaDatas.length){
				System.out.println("Error 11!"+lineCounter);
			}
			
			out.write(J.toJson(p));
			out.newLine();
			out.flush();
		}
		in.close();
		inLattice.close();
		out.close();
	}
	
	public void createLDAThulacAsNormal(String keywordJson, String input, String latticeFile, String ldaFile, String output)throws IOException{
		RecordReader reader = new RecordReader(keywordJson);
		BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(input),"UTF8"));
		BufferedReader inLattice = new BufferedReader(new InputStreamReader(new FileInputStream(latticeFile),"UTF8"));
		BufferedReader inLDA = new BufferedReader(new InputStreamReader(new FileInputStream(ldaFile),"UTF8"));
		BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(output),"UTF8"));
		
		String line = "";
		String latticeLine = "";
		String ldaLine = "";
		int ldaCounter = 0;
		int lineCounter = 0;
		while(reader.next()){
			KeywordPost p = J.fromJson(reader.value(), KeywordPost.class);
			lineCounter ++;
			ldaLine = inLDA.readLine();
			String[] ldaDatas = ldaLine.split(" ");
			ldaCounter = 0;
			
			line = in.readLine();
			latticeLine = inLattice.readLine();
			
			StringBuffer tmp = new StringBuffer();
			while(!line.equals("") && !latticeLine.equals("")){
				String[] lineSentences = line.split(" ");
				String[] latticeLineSentences = latticeLine.split("  ");
				
				if(lineSentences.length != latticeLineSentences.length){
					System.out.println("Error1:"+ lineCounter +":" +line+":"+lineSentences.length +":"+latticeLine+":"+latticeLineSentences.length);
				}else{
					for(int i = 0; i < latticeLineSentences.length; i ++){
						String[] datas = latticeLineSentences[i].split(" ");
						for(String data : datas){
							String[] wordAndTag = data.split(",");
							if(wordAndTag.length != 4){
								System.out.println(lineCounter +":"+data);
								continue;
							}
							
							if(!wordAndTag[3].equals("0")){
								continue;
							}
							String word =lineSentences[i].substring(Integer.parseInt(wordAndTag[0]), Integer.parseInt(wordAndTag[1]));
							if(word.length() < 2){
								continue;
							}
							
							tmp.append(word + "_" + wordAndTag[2] + "_" + ldaDatas[ldaCounter].split(":")[1] + " ");
							ldaCounter ++;
						}
					}
				}
				line = in.readLine();
				latticeLine = inLattice.readLine();
			}
			p.setTitle(tmp.toString().trim());
			
			
			line = in.readLine();
			latticeLine = inLattice.readLine();
			tmp = new StringBuffer();
			while(!line.equals("") && !latticeLine.equals("")){
				String[] lineSentences = line.split(" ");
				String[] latticeLineSentences = latticeLine.split("  ");
				if(lineSentences.length != latticeLineSentences.length){
					System.out.println("Error1:"+ lineCounter +":"+lineSentences.length +":"+latticeLineSentences.length);
				}else{
					for(int i = 0; i < latticeLineSentences.length; i ++){
						String[] datas = latticeLineSentences[i].split(" ");
						for(String data : datas){
							String[] wordAndTag = data.split(",");
							if(wordAndTag.length != 4){
								System.out.println(lineCounter +":"+data);
								continue;
							}
							
							if(!wordAndTag[3].equals("0")){
								continue;
							}
							String word =lineSentences[i].substring(Integer.parseInt(wordAndTag[0]), Integer.parseInt(wordAndTag[1]));
							if(word.length() < 2){
								continue;
							}
							
							tmp.append(word + "_" + wordAndTag[2] + "_" + ldaDatas[ldaCounter].split(":")[1] + " ");
							
							ldaCounter ++;
						}
					}
					tmp.append("。");
				}
				line = in.readLine();
				latticeLine = inLattice.readLine();
			}
			p.setSummary(tmp.toString().trim());
			
			line = in.readLine();
			latticeLine = inLattice.readLine();
			tmp = new StringBuffer();
			while(!line.equals("") && !latticeLine.equals("")){
				String[] lineSentences = line.split(" ");
				String[] latticeLineSentences = latticeLine.split("  ");
				if(lineSentences.length != latticeLineSentences.length){
					System.out.println("Error1:"+ lineCounter +":"+lineSentences.length +":"+latticeLineSentences.length);
				}else{
					for(int i = 0; i < latticeLineSentences.length; i ++){
						String[] datas = latticeLineSentences[i].split(" ");
						for(String data : datas){
							String[] wordAndTag = data.split(",");
							if(wordAndTag.length != 4){
								System.out.println(lineCounter +":"+data);
								continue;
							}
							
							if(!wordAndTag[3].equals("0")){
								continue;
							}
							String word =lineSentences[i].substring(Integer.parseInt(wordAndTag[0]), Integer.parseInt(wordAndTag[1]));
							if(word.length() < 2){
								continue;
							}
							
							tmp.append(word + "_" + wordAndTag[2] + "_" + ldaDatas[ldaCounter].split(":")[1] + " ");
							
							ldaCounter ++;
						}
					}
					tmp.append("。");
				}
				line = in.readLine();
				latticeLine = inLattice.readLine();
			}
			p.setContent(tmp.toString().trim());
			
			if(ldaCounter != ldaDatas.length){
				System.out.println("Error 11!"+lineCounter);
			}
			
			out.write(J.toJson(p));
			out.newLine();
			out.flush();
		}
		in.close();
		inLattice.close();
		out.close();
	}
	
	public void checkTagTopic(String jsonFile, String ldaTrainFile, String ldaModelFile, String output) throws IOException{
		
		BufferedReader inTrain = new BufferedReader(new InputStreamReader(new FileInputStream(ldaTrainFile),"UTF8"));
		BufferedReader inModel = new BufferedReader(new InputStreamReader(new FileInputStream(ldaModelFile),"UTF8"));
		
		
		inTrain.readLine();
		String trainLine = "";
		String modelLine = "";
		
		HashMap<String, HashMap<Integer, Integer>> topicMap = new HashMap<String, HashMap<Integer,Integer>>();
		
		int lineCounter = 0;
		while((trainLine = inTrain.readLine()) != null){
			modelLine = inModel.readLine();
			lineCounter ++;
			
			String[] words = trainLine.split(" ");
			String[] ldaDatas = modelLine.split(" ");
			if(words.length != ldaDatas.length){
				//System.out.println(words.length + "" + ldaDatas.length);
				System.out.println("Error at line:"+lineCounter);
				continue;
			}
			
			for(int i = 0; i < words.length; i ++){
				String word = words[i];
				int topic = Integer.parseInt(ldaDatas[i].split(":")[1]);
				HashMap<Integer, Integer> countMap = topicMap.get(word);
				if(countMap != null){
					Integer count = countMap.get(topic);
					if(count != null){
						countMap.put(topic, count + 1);
					}else{
						countMap.put(topic, 1);
					}
				}else{
					countMap = new HashMap<Integer, Integer>();
					countMap.put(topic, 1);
				}
				topicMap.put(word, countMap);
			}
		}
		inTrain.close();
		inModel.close();
		
		Comparator<Object> c = new Comparator<Object>() {
			@Override
			public int compare(Object o1, Object o2) {
				// TODO Auto-generated method stub
				Integer d1 = ((Entry<Integer, Integer>)o1).getValue();
				Integer d2 = ((Entry<Integer, Integer>)o2).getValue();
				return d2.compareTo(d1);
			}
		};
		
		HashMap<String, Integer> wordTopicMap = new HashMap<String, Integer>();
		for(Entry<String, HashMap<Integer, Integer>> e : topicMap.entrySet()){
			Object[] ans = e.getValue().entrySet().toArray();
			Arrays.sort(ans,c);
			/*
			for(int i = 0; i < ans.length; i ++){
				System.out.print(((Entry<Integer, Integer>)ans[i]).getValue()+" ");
			}
			System.out.println();
			*/
			wordTopicMap.put(e.getKey(), ((Entry<Integer, Integer>)ans[0]).getKey());
		}
		
		RecordReader reader = new RecordReader(jsonFile);
		BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(output),"UTF8"));
		HashMap<Integer, Integer> topicNumMap = new HashMap<Integer, Integer>();
		HashSet<Integer> topicSet = new HashSet<Integer>();
		while(reader.next()){
			
			KeywordPost p = J.fromJson(reader.value(), KeywordPost.class);
			for(String tag : p.getTags()){
				if(wordTopicMap.containsKey(tag)){
					topicSet.add(wordTopicMap.get(tag));
				}
			}
			
			int size = topicSet.size();
			Integer num = topicNumMap.get(size);
			if(num != null){
				topicNumMap.put(size, num + 1);
			}else{
				topicNumMap.put(size, 1);
			}
			
			topicSet.clear();
		}
		for(Entry<Integer, Integer> e : topicNumMap.entrySet()){
			out.write(e.getKey() + ":" + e.getValue());
			out.newLine();
			out.flush();
		}
		reader.close();
		out.close();
	}
	
	public void checkSinaTagTopic(String jsonFile, String ldaTrainFile, String ldaModelFile, String output) throws IOException{
		
		BufferedReader inTrain = new BufferedReader(new InputStreamReader(new FileInputStream(ldaTrainFile),"UTF8"));
		BufferedReader inModel = new BufferedReader(new InputStreamReader(new FileInputStream(ldaModelFile),"UTF8"));
		
		
		inTrain.readLine();
		String trainLine = "";
		String modelLine = "";
		
		HashMap<String, HashMap<Integer, Integer>> topicMap = new HashMap<String, HashMap<Integer,Integer>>();
		
		int lineCounter = 0;
		while((trainLine = inTrain.readLine()) != null){
			modelLine = inModel.readLine();
			lineCounter ++;
			
			String[] words = trainLine.split(" ");
			String[] ldaDatas = modelLine.split(" ");
			if(words.length != ldaDatas.length){
				//System.out.println(words.length + "" + ldaDatas.length);
				System.out.println("Error at line:"+lineCounter);
				continue;
			}
			
			for(int i = 0; i < words.length; i ++){
				String word = words[i];
				int topic = Integer.parseInt(ldaDatas[i].split(":")[1]);
				HashMap<Integer, Integer> countMap = topicMap.get(word);
				if(countMap != null){
					Integer count = countMap.get(topic);
					if(count != null){
						countMap.put(topic, count + 1);
					}else{
						countMap.put(topic, 1);
					}
				}else{
					countMap = new HashMap<Integer, Integer>();
					countMap.put(topic, 1);
				}
				topicMap.put(word, countMap);
			}
		}
		inTrain.close();
		inModel.close();
		
		Comparator<Object> c = new Comparator<Object>() {
			@Override
			public int compare(Object o1, Object o2) {
				// TODO Auto-generated method stub
				Integer d1 = ((Entry<Integer, Integer>)o1).getValue();
				Integer d2 = ((Entry<Integer, Integer>)o2).getValue();
				return d2.compareTo(d1);
			}
		};
		
		HashMap<String, Integer> wordTopicMap = new HashMap<String, Integer>();
		for(Entry<String, HashMap<Integer, Integer>> e : topicMap.entrySet()){
			Object[] ans = e.getValue().entrySet().toArray();
			Arrays.sort(ans,c);
			/*
			for(int i = 0; i < ans.length; i ++){
				System.out.print(((Entry<Integer, Integer>)ans[i]).getValue()+" ");
			}
			System.out.println();
			*/
			wordTopicMap.put(e.getKey(), ((Entry<Integer, Integer>)ans[0]).getKey());
		}
		
		RecordReader reader = new RecordReader(jsonFile);
		BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(output),"UTF8"));
		HashMap<Integer, Integer> topicNumMap = new HashMap<Integer, Integer>();
		HashSet<Integer> topicSet = new HashSet<Integer>();
		while(reader.next()){
			
			Post p = J.fromJson(reader.value(), Post.class);
			for(String tag : p.getTags()){
				if(wordTopicMap.containsKey(tag)){
					topicSet.add(wordTopicMap.get(tag));
				}
			}
			
			int size = topicSet.size();
			Integer num = topicNumMap.get(size);
			if(num != null){
				topicNumMap.put(size, num + 1);
			}else{
				topicNumMap.put(size, 1);
			}
			
			topicSet.clear();
		}
		for(Entry<Integer, Integer> e : topicNumMap.entrySet()){
			out.write(e.getKey() + ":" + e.getValue());
			out.newLine();
			out.flush();
		}
		reader.close();
		out.close();
	}
	
	public void createSinaLDAThulacAsNormal(String keywordJson, String input, String latticeFile, String ldaFile, String output)throws IOException{
		RecordReader reader = new RecordReader(keywordJson);
		BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(input),"UTF8"));
		BufferedReader inLattice = new BufferedReader(new InputStreamReader(new FileInputStream(latticeFile),"UTF8"));
		BufferedReader inLDA = new BufferedReader(new InputStreamReader(new FileInputStream(ldaFile),"UTF8"));
		BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(output),"UTF8"));
		
		String line = "";
		String latticeLine = "";
		String ldaLine = "";
		int ldaCounter = 0;
		int lineCounter = 0;
		int latticeLineCounter = 0;
		while(reader.next()){
				Post p = J.fromJson(reader.value(), Post.class);
				lineCounter ++;
				ldaLine = inLDA.readLine();
				String[] ldaDatas = ldaLine.split(" ");
				ldaCounter = 0;
				
				line = in.readLine();
				latticeLine = inLattice.readLine();
				latticeLineCounter++;
				
				StringBuffer tmp = new StringBuffer();
				while(!line.equals("") && !latticeLine.equals("")){
					String[] lineSentences = line.split(" ");
					String[] latticeLineSentences = latticeLine.split("  ");
					
					if(lineSentences.length != latticeLineSentences.length){
						System.out.println("Error1:"+ lineCounter +":" +line+":"+lineSentences.length +":"+latticeLine+":"+latticeLineSentences.length);
					}else{
						for(int i = 0; i < latticeLineSentences.length; i ++){
							String[] datas = latticeLineSentences[i].split(" ");
							for(String data : datas){
								String[] wordAndTag = data.split(",");
								if(wordAndTag.length != 4){
									System.out.println(lineCounter +":"+data);
									continue;
								}
								if(!wordAndTag[3].equals("0")){
									continue;
								}
								String word =lineSentences[i].substring(Integer.parseInt(wordAndTag[0]), Integer.parseInt(wordAndTag[1]));
								if(word.length() < 2){
									continue;
								}
								
								tmp.append(word + "_" + wordAndTag[2] + "_" + ldaDatas[ldaCounter].split(":")[1] + " ");
								ldaCounter ++;
							}
						}
					}
					line = in.readLine();
					latticeLine = inLattice.readLine();
					latticeLineCounter++;
				}
				p.setTitle(tmp.toString().trim());
				
				line = in.readLine();
				latticeLine = inLattice.readLine();
				latticeLineCounter++;
				tmp = new StringBuffer();
				while(!line.equals("") && !latticeLine.equals("")){
					String[] lineSentences = line.split(" ");
					String[] latticeLineSentences = latticeLine.split("  ");
					if(lineSentences.length != latticeLineSentences.length){
						System.out.println("Error1:"+ lineCounter +":"+lineSentences.length +":"+latticeLineSentences.length);
					}else{
						for(int i = 0; i < latticeLineSentences.length; i ++){
							String[] datas = latticeLineSentences[i].split(" ");
							for(String data : datas){
								String[] wordAndTag = data.split(",");
								if(wordAndTag.length != 4){
									System.out.println(lineCounter +":"+data);
									continue;
								}
								
								if(!wordAndTag[3].equals("0")){
									continue;
								}
								String word =lineSentences[i].substring(Integer.parseInt(wordAndTag[0]), Integer.parseInt(wordAndTag[1]));
								if(word.length() < 2){
									continue;
								}
								
								tmp.append(word + "_" + wordAndTag[2] + "_" + ldaDatas[ldaCounter].split(":")[1] + " ");
								
								ldaCounter ++;
							}
						}
						tmp.append("。");
					}
					line = in.readLine();
					latticeLine = inLattice.readLine();
					latticeLineCounter++;
				}
				p.setContent(tmp.toString().trim());
				
				if(ldaCounter != ldaDatas.length){
					System.out.println("Error 11!"+lineCounter);
				}
				
				out.write(J.toJson(p));
				out.newLine();
				out.flush();
		}
		in.close();
		inLattice.close();
		out.close();
	}
	
	public void buildWord2VecTrainFile(String input, String output) throws IOException{
		BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(input),"UTF8"));
		BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(output),"UTF8"));
		String line = "";
		String[] POSs = {"n","i","ns","np","nz","ni","a","v","id","j","b"};
		//String[] POSs = {"n","i","ns","np","nz","ni","id","j"};
		HashSet<String> posSet = new HashSet<String>();
		for(String pos : POSs){
			posSet.add(pos);
		}
		while((line = in.readLine()) != null){
			KeywordPost post = J.fromJson(line, KeywordPost.class);
			
			String[] words = post.getTitle().split(" ");
			for (int i = 0; i < words.length; i++) {
				int index = words[i].lastIndexOf("_");
				if (index == -1) {
					continue;
				}
				String word = words[i].substring(0, index);
				String label = words[i].substring(index + 1);
				
				if (word.length() < 2) {
					continue;
				}
				
				if(!posSet.contains(label)){
					continue;
				}
				
				out.write(word+" ");
			}
			
			words = post.getSummary().split(" |。");
			for (int i = 0; i < words.length; i++) {
				if(words[i].equals("")){
					continue;
				}
				int index = words[i].lastIndexOf("_");
				if (index == -1) {
					continue;
				}
				String word = words[i].substring(0, index);
				String label = words[i].substring(index + 1);
				
				if (word.length() < 2) {
					continue;
				}
				
				if(!posSet.contains(label)){
					continue;
				}
				
				out.write(word+" ");
			}
			
			words = post.getContent().split(" |。");
			for (int i = 0; i < words.length; i++) {
				int index = words[i].lastIndexOf("_");
				if (index == -1) {
					continue;
				}
				String word = words[i].substring(0, index);
				String label = words[i].substring(index + 1);
				
				if (word.length() < 2) {
					continue;
				}
				
				if(!posSet.contains(label)){
					continue;
				}
				
				out.write(word+" ");
			}
		}
		out.newLine();
		out.flush();
		in.close();
		out.close();
	}
	
	void creatTopicWord2Vec(String trainFile, String modelFile, String output, String logFile) throws IOException{
		BufferedReader inTrain = new BufferedReader(new InputStreamReader(new FileInputStream(trainFile),"UTF8"));
		BufferedReader inModel = new BufferedReader(new InputStreamReader(new FileInputStream(modelFile),"UTF8"));
		String trainLine = "";
		String modelLine = "";
		
		BufferedWriter outLog = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(logFile), "UTF8"));
		
		HashMap<String, HashMap<Integer, Integer>> topicMap = new HashMap<String, HashMap<Integer,Integer>>();
		HashMap<String, HashMap<Integer, Integer>> topicCountMap = new HashMap<String, HashMap<Integer,Integer>>();
		int lineCounter = 0;
		trainLine = inTrain.readLine();
		while((trainLine = inTrain.readLine()) != null){
			modelLine = inModel.readLine();
			lineCounter ++;
			
			String[] trainDatas = trainLine.trim().split(" ");
			String[] modelDatas = modelLine.trim().split(" ");
			if(trainDatas.length != modelDatas.length){
				System.out.println("Error:" + lineCounter);
				continue;
			}
			
			for(int i = 0; i < trainDatas.length; i ++){
				String[] datas = modelDatas[i].split(":");
				if(datas.length != 2){
					System.out.println("Error1:"+lineCounter);
					continue;
				}
				
				String word = trainDatas[i];
				int topic = Integer.parseInt(datas[1]);
				
				HashMap<Integer, Integer> tmpMap = topicMap.get(word);
				if(tmpMap != null){
					Integer num = tmpMap.get(topic);
					if(num == null){
						int size = tmpMap.size();
						tmpMap.put(topic, size);
						topicMap.put(word, tmpMap);
					}
				}else{
					tmpMap = new HashMap<Integer, Integer>();
					tmpMap.put(topic, 0);
					topicMap.put(word, tmpMap);
				}
				
				HashMap<Integer, Integer> tmpCountMap = topicCountMap.get(word);
				if(tmpCountMap != null){
					Integer num = tmpCountMap.get(topic);
					if(num == null){
						tmpCountMap.put(topic, 1);
					}else{
						tmpCountMap.put(topic, num + 1);
					}
					topicCountMap.put(word, tmpCountMap);
				}else{
					tmpCountMap = new HashMap<Integer, Integer>();
					tmpCountMap.put(topic, 1);
					topicCountMap.put(word, tmpCountMap);
				}
			}
			
			if(lineCounter % 10000 == 0){
				System.out.println("Done " + lineCounter + " lines.");
			}
		}
		inTrain.close();
		inModel.close();
		
		for(Entry<String, HashMap<Integer, Integer>> e : topicMap.entrySet()){
			outLog.write(e.getKey());
			HashMap<Integer, Integer> tmpMap = topicCountMap.get(e.getKey());
			for(Entry<Integer, Integer> ee : e.getValue().entrySet()){
				outLog.write(" "+ee.getKey()+":"+ee.getValue()+":"+tmpMap.get(ee.getKey()));
			}
			outLog.newLine();
			outLog.flush();
		}
		outLog.close();
		
		inTrain = new BufferedReader(new InputStreamReader(new FileInputStream(trainFile),"UTF8"));
		inModel = new BufferedReader(new InputStreamReader(new FileInputStream(modelFile),"UTF8"));
		BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(output), "UTF8"));
		
		lineCounter = 0;
		trainLine = inTrain.readLine();
		while((trainLine = inTrain.readLine()) != null){
			modelLine = inModel.readLine();
			lineCounter ++;
			
			String[] trainDatas = trainLine.trim().split(" ");
			String[] modelDatas = modelLine.trim().split(" ");
			if(trainDatas.length != modelDatas.length){
				System.out.println("Error:" + lineCounter);
				continue;
			}
			
			for(int i = 0; i < trainDatas.length; i ++){
				String[] datas = modelDatas[i].split(":");
				if(datas.length != 2){
					System.out.println("Error1:"+lineCounter);
					continue;
				}
				
				String word = trainDatas[i];
				int topic = Integer.parseInt(datas[1]);
				
				HashMap<Integer, Integer> tmpMap = topicMap.get(word);
				int size = tmpMap.size();
				if(size == 1){
					out.write(word + " ");
				}else{
					int num = tmpMap.get(topic);
					out.write(word + "_" + num + " ");
				}
				out.flush();
			}
			if(lineCounter % 10000 == 0){
				System.out.println("Second. Done " + lineCounter + " lines.");
			}
		}
		out.newLine();
		out.flush();
		out.close();
		inTrain.close();
		inModel.close();
		
		topicMap.clear();
		
	}
	
	
	
	/**
	 * @param args
	 */
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		KeywordInfo info = new KeywordInfo();
		//info.getKeywordInfo("/home/cxx/keyword/KeywordPost.dat", "/home/cxx/keyword/keywordInfo.txt");
		//info.getKeywordInfo("res/pmi/KeywordPost.dat", "res/pmi/keywordInfo.txt");
		//info.checkSegPerform("res/pmi/KeywordPostSeg.dat", "res/pmi/KeywordPostSegInfo.txt");
		//info.getText("res/pmi/KeywordPost.dat", "res/pmi/KeywordToSeg.txt");
		
		/*
		info.checkNoLatticePerform("res/pmi/KeywordPostSeg.dat", 
				"res/pmi/KeywordSegResult.txt", "res/pmi/KeywordNoLatticeInfo.txt");
		*/
		
		/*
		info.checkLatticePerform("res/pmi/KeywordPost.dat", "res/pmi/KeywordToSeg.txt", 
				"res/pmi/KeywordLattice", "res/pmi/KeywordPostLatticeInfo.txt");
		
		info.checkLatticePerform("res/pmi/KeywordPost.dat", "res/pmi/KeywordToSeg.txt", 
				"res/pmi/KeywordLattice10", "res/pmi/KeywordPostLattice10Info.txt");
		*/
		
		//info.getSegmentTextFromLattice("res/pmi/KeywordToSeg.txt", "res/pmi/KeywordLattice10", "res/pmi/KeywordSegResultFromLattice.txt");
		//info.compareTwoFile("res/pmi/KeywordSegResult.txt", "res/pmi/KeywordSegResultFromLattice.txt", "res/pmi/KeywordSegDifference.txt");
		/*
		info.createSegmentdJson("res/pmi/KeywordPostSeg.dat", "res/pmi/KeywordToSeg.txt", 
				"res/pmi/KeywordLattice10", "res/pmi/KeywordPostSegmented.dat");
		*/
		//info.getSentence("res/pmi/KeywordPost.dat", "res/pmi/KeywordPostSentenceToSeg.txt");
		/*
		info.createSegmentdJson("res/pmi/KeywordPostSeg.dat",  
				"res/pmi/KeywordPostSentenceSeg.txt", "res/pmi/KeywordPostSegmented.dat");
		*/
		/*
		info.createSegmentdLatticeAsNormal("res/pmi/KeywordPost.dat", "res/pmi/KeywordPostSentenceToSeg.txt", 
				"res/pmi/KeywordPostSentenceLattice.txt", "res/pmi/KeywordPostLatticeAsNormal.dat");
		*/
		//info.getSinaSentence("res/pmi/SinaPost.dat", "res/pmi/SinaPostSentenceToSeg.txt");
		
		/*
		info.checkSinaNoLatticeDetail("res/pmi/SinaPostSegmented.dat", 
				"res/pmi/SinaPostSegResult.txt", "res/pmi/SinaPostNoLatticeDetail.txt");
		*/
		
		/*
		info.checkSinaNoLatticePerform("res/pmi/SinaPostSegmented.dat", 
				"res/pmi/SinaPostSegResult.txt", "res/pmi/SinaPostNoLatticeInfo.txt");
		*/
		
		/*
		info.createSinaSegmentdJson("res/pmi/SinaPost.dat",  
				"res/pmi/SinaPostSegResult.txt", "res/pmi/SinaPostSegmented.dat");
		*/
		/*
		info.createSinaSegmentdJson("res/pmi/SinaPost.dat", "res/pmi/SinaPostSentenceToSeg.txt", 
				"res/pmi/SinaPostSentenceLattice10.txt", "res/pmi/SinaPostSegmented.dat");
		*/
		/*
		info.createSinaSegmentdLatticeAsNormal("res/pmi/SinaPost.dat", "res/pmi/SinaPostSentenceToSeg.txt", 
				"res/pmi/SinaPostSentenceLattice.txt", "res/pmi/SinaPostLatticeAsNormal.dat");
		*/
		
		
		
		/*
		info.checkSinaLatticePerform("res/pmi/SinaPost.dat", "res/pmi/SinaPostSentenceToSeg.txt", 
				"res/pmi/SinaPostSentenceLattice5.txt", "res/pmi/SinaPostLattice5Info.txt");
		
		info.checkSinaLatticePerform("res/pmi/SinaPost.dat", "res/pmi/SinaPostSentenceToSeg.txt", 
				"res/pmi/SinaPostSentenceLattice10.txt", "res/pmi/SinaPostLattice10Info.txt");
		*/
		
		/*
		info.createLDATrainData("res/pmi/KeywordPost.dat", "res/pmi/KeywordPostSentenceToSeg.txt", 
				"res/pmi/KeywordPostSentenceLattice.txt", "res/pmi/KeywordPostLDATrain.dat");
		*/
		
		/*
		info.createLDALatticeAsNormal("res/pmi/KeywordPost.dat", "res/pmi/KeywordPostSentenceToSeg.txt", 
				"res/pmi/KeywordPostSentenceLattice.txt", "res/pmi/model-final.tassign", "res/pmi/KeywordPostLDA.dat");
		*/
		
		//info.getSinaInfo("res/pmi/SinaPost.dat", "res/pmi/SinaInfoDetail.txt");
		
		/*
		info.createSinaLDATrainData("res/pmi/SinaPost.dat", "res/pmi/SinaPostSentenceToSeg.txt", 
				"res/pmi/SinaPostSentenceLattice10.txt", "res/pmi/SinaPostLDAThulacTrain.dat");
		*/
		
		//info.checkTagTopic("res/pmi/KeywordPost.dat", "res/pmi/KeywordPostLDATrain.dat", "res/pmi/Keyword-model-final.tassign", "res/pmi/KeywordPostTagLDA.txt");
		//info.checkSinaTagTopic("res/pmi/SinaPost.dat", "res/pmi/SinaPostLDATrain.dat", "res/pmi/model-final.tassign", "res/pmi/SinaPostTagLDA.txt");
		
		/*
		info.createLDATrainDataThulac("res/pmi/KeywordPost.dat", "res/pmi/KeywordPostSentenceToSeg.txt", 
				"res/pmi/KeywordPostSentenceLattice.txt", "res/pmi/KeywordPostLDAThulacTrain.dat");
		*/
		/*
		info.createLDAThulacAsNormal("res/pmi/KeywordPost.dat", "res/pmi/KeywordPostSentenceToSeg.txt", 
				"res/pmi/KeywordPostSentenceLattice.txt", "res/pmi/model-final.tassign", "res/pmi/KeywordPostLDAThulac.dat");
		*/
		/*
		info.createSinaLDAThulacAsNormal("res/pmi/SinaPost.dat", "res/pmi/SinaPostSentenceToSeg.txt", 
				"res/pmi/SinaPostSentenceLattice10.txt", "res/pmi/model-final.tassign", "res/pmi/SinaPostLDAThulac.dat");
		*/
		
		//info.buildWord2VecTrainFile("res/pmi/KeywordPostSeg.dat", "res/pmi/KeywordPostWord2Vec.txt");
		
		info.creatTopicWord2Vec(args[0], args[1], args[2], args[3]);
		
	}
}
