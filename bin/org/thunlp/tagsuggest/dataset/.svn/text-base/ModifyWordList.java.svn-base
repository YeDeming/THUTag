package org.thunlp.tagsuggest.dataset;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import org.thunlp.io.JsonUtil;
import org.thunlp.io.RecordReader;
import org.thunlp.language.chinese.LangUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import weka.filters.unsupervised.attribute.Normalize;

public class ModifyWordList {
	Set<String> stopwords = null;
	
	public void buildWordList() throws Exception{
	    stopwords = new HashSet<String>();
	    stopwords.add("简介");
	    stopwords.add("装帧");
	    stopwords.add("简体");
	    stopwords.add("imdb");
	    stopwords.add("定价");
	    stopwords.add("踏上");
	    stopwords.add("介质");
	    stopwords.add("编号");
	    stopwords.add("译者");
	    stopwords.add("制片");
	    int counter = 0;
	    try{
	    	Gson g = (new GsonBuilder()).disableHtmlEscaping().create();
		    String input = "/media/work/datasets(secret)/douban/raw/tag_subject_cxx.dat";
			BufferedReader tagInput = new BufferedReader(
					new InputStreamReader(new FileInputStream(input),"UTF-8"));
		    HashMap<String, Integer> bookWord = new HashMap<String, Integer>();
		    HashMap<String, Integer> movieWord = new HashMap<String, Integer>();
		    HashMap<String, Integer> musicWord = new HashMap<String, Integer>();
		    
		    String tagLine;
		    while((tagLine = tagInput.readLine()) != null){
		    	counter ++;
		    	TargetDoc doc = g.fromJson(tagLine, TargetDoc.class);
		    	int id = doc.cat_id;
		    	String tag = normalize(doc.tag);
		    	if(tag.length() <= 1){
		    		continue;
		    	}
		        if (tag.matches("^[a-zA-Z0-9]+$"))
		            continue;
		        if (org.thunlp.language.chinese.Stopwords.isStopword(tag))
		              continue;
		        if (stopwords.contains(tag))
		            continue;
		    	switch(id){
		    	case 1001:
		    		if(bookWord.containsKey(tag)){
		    			int count = bookWord.get(tag) + doc.count;
		    			bookWord.remove(tag);
		    			bookWord.put(tag, count);
		    		}
		    		else{
		    			bookWord.put(tag, doc.count);
		    		}
		    		break;
		    	case 1002:
		    		if(movieWord.containsKey(tag)){
		    			int count = movieWord.get(tag) + doc.count;
		    			movieWord.remove(tag);
		    			movieWord.put(tag, count);
		    		}
		    		else{
		    			movieWord.put(tag, doc.count);
		    		}
		    		break;
		    	case 1003:
		    		if(musicWord.containsKey(tag)){
		    			int count = musicWord.get(tag) + doc.count;
		    			musicWord.remove(tag);
		    			musicWord.put(tag, count);
		    		}
		    		else{
		    			musicWord.put(tag, doc.count);
		    		}
		    		break;
		    	default:
		    		System.out.println("Wrong Type!");	
		    	}
		    }
		    tagInput.close();
		    
		    String outputFile = "/home/cxx/smt/DoubanWordList/bookWordList";
			BufferedWriter out = new BufferedWriter(
					new OutputStreamWriter(new FileOutputStream(outputFile,true),"UTF-8"));
		    for(Entry<String, Integer> e:bookWord.entrySet()){
		    	if(e.getValue() > 10){
		    		out.write(e.getKey());
		    		out.newLine();
		    		out.flush();
		    	}
		    }
		    out.close();
		    outputFile = "/home/cxx/smt/DoubanWordList/movieWordList";
			out = new BufferedWriter(
					new OutputStreamWriter(new FileOutputStream(outputFile,true),"UTF-8"));
		    for(Entry<String, Integer> e:movieWord.entrySet()){
		    	if(e.getValue() > 10){
		    		out.write(e.getKey());
		    		out.newLine();
		    		out.flush();
		    	}
		    }
		    out.close();
		    outputFile = "/home/cxx/smt/DoubanWordList/musicWordList";
			out = new BufferedWriter(
					new OutputStreamWriter(new FileOutputStream(outputFile,true),"UTF-8"));
		    for(Entry<String, Integer> e:musicWord.entrySet()){
		    	if(e.getValue() > 10){
		    		out.write(e.getKey());
		    		out.newLine();
		    		out.flush();
		    	}
		    }
		    out.close();
	    }catch (IOException e) {
			// TODO: handle exception
	    	System.out.println(counter);
	    	e.printStackTrace();
		}
	}
	
	Pattern spaceRE = Pattern.compile(" +");
	  
	public String normalize(String tag) {
		tag = LangUtils.removePunctuationMarks(tag);
		tag = spaceRE.matcher(tag).replaceAll("");
		tag = LangUtils.T2S(tag);
		tag = tag.toLowerCase();
		return tag;
	}
	
	public static void main(String args[]) throws Exception{
	    new ModifyWordList().buildWordList();
	}
}

class TargetDoc{
	public int count;
	public int cat_id;
	public String tag;
	public int subject_id;
}
