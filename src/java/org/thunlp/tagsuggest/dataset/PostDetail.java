package org.thunlp.tagsuggest.dataset;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;
import java.util.Vector;
import java.util.Map.Entry;
import java.util.logging.Logger;

import org.thunlp.io.JsonUtil;
import org.thunlp.io.RecordReader;
import org.thunlp.misc.Counter;
import org.thunlp.misc.Flags;
import org.thunlp.tagsuggest.common.ConfigIO;
import org.thunlp.tagsuggest.common.DoubanPost;
import org.thunlp.tagsuggest.common.Post;
import org.thunlp.tagsuggest.common.TagFilter;
import org.thunlp.tagsuggest.common.WordFeatureExtractor;
import org.thunlp.text.Lexicon;
import org.thunlp.tool.GenericTool;

import com.mysql.jdbc.log.Log;

public class PostDetail implements GenericTool {
	private static Logger LOG = Logger.getAnonymousLogger();
	private Properties config = null;
	JsonUtil J = new JsonUtil();
	WordFeatureExtractor fe = null;
	TagFilter tagFilter = null;
	
	@Override
	public void run(String[] args) throws Exception {
		// TODO Auto-generated method stub
		Flags flags = new Flags();
		flags.add("input");
		flags.add("output");
		flags.add("config");
		flags.parseAndCheck(args);
		
		Properties config = ConfigIO.configFromString(flags.getString("config"));
		buildVcb(flags.getString("input"), flags.getString("output") , config);
	}
	
	public void buildVcb(String input, String output,Properties config)
	{
		this.config = config;
		
		int counter = 0;
		try{
			File modelDir = new File(output);
			if (!modelDir.exists()) {
		        modelDir.mkdir();
		    }
			
		    Lexicon wordlex = new Lexicon();
		    Lexicon taglex = new Lexicon();
		    WordFeatureExtractor.buildLexicons(
		        input, wordlex, taglex, config);
		    fe = new WordFeatureExtractor(config);
		    fe.setWordLexicon(wordlex);
		    fe.setTagLexicon(taglex);
		    tagFilter = new TagFilter(config, taglex);
		    
		    // output for debug
		    Lexicon wlex = new Lexicon();
		    wlex = wordlex.removeLowFreqWords(Integer.parseInt(config.getProperty("minwordfreq","10")));
		    Lexicon tlex = new Lexicon();
		    tlex = taglex.removeLowFreqWords(Integer.parseInt(config.getProperty("mintagfreq","10")));
		    wlex.saveToFile(new File(output+File.separator+"wordlex"));
		    tlex.saveToFile(new File(output+File.separator+"taglex"));
		    
		    Set<String> tagSet = new HashSet<String>();
		    Set<String> filtered = new HashSet<String>();
			RecordReader reader = new RecordReader(input);
			
			BufferedWriter out = new BufferedWriter(
					new OutputStreamWriter(new FileOutputStream(output+"/tagCount.vcb"),"UTF-8"));
			
		    Counter<String> termFreq = new Counter<String>();

		    long totalLen = 0;
		    double avergeLen = 0;
		    long overMaxLen = 0;
		    long numdocs = 0;
		    
		    long tagnum = 0;
		    long tagnumori = 0;
		    while (reader.next()) {
		    	counter ++;	
		    	Post p = J.fromJson(reader.value(), Post.class);			
		    	String [] words = fe.extract(p);
				if(words.length <= 0){
					continue;
				}
				boolean exist = false;
				tagFilter.filterWithNorm(p.getTags(), filtered);
				for(String s:p.getTags()){
					if(!tagSet.contains(s)){
						tagSet.add(s);
						tagnumori ++;
					}
					if(filtered.contains(s)){
						termFreq.inc(s,1);
						tagnum ++;
						exist = true;
					}
				}
				int tmpLen = 0;
				if(exist){
					numdocs += 1;
					for(String word:words){
						totalLen += word.length();
						tmpLen += word.length();
					}
					if(tmpLen > 101) overMaxLen += 1;
				}
		    }
		    avergeLen = (double)totalLen /(double) numdocs;
		    
			BufferedWriter detail = new BufferedWriter(
					new OutputStreamWriter(new FileOutputStream(output+"/detail.txt"),"UTF-8"));
		    detail.write("totalLen:"+totalLen);
		    detail.newLine();
		    detail.write("avergeLen:"+avergeLen);
		    detail.newLine();
		    detail.write("overMaxLen:"+overMaxLen);
		    detail.newLine();
		    detail.write("numdocs:"+numdocs);
		    detail.newLine();
		    detail.flush();
		    detail.close();
		    
		    out.write("tagNum:"+tagnum);
		    out.newLine();
		    double aveTag = (double)tagnum / (double)numdocs;
		    out.write("aveNum:"+aveTag);
		    out.newLine();
		    out.write("aveNumOri:"+((double)tagnumori / (double)numdocs));
		    out.newLine();
		    out.flush();
		    Iterator<Entry<String, Long>> iter = termFreq.iterator();
		    while (iter.hasNext()) {
			      Entry<String, Long> e = iter.next();
			      out.write(e.getKey()+" "+e.getValue());
			      out.newLine();
			      out.flush();
		    }
		    reader.close();
		    out.close();
		}catch (IOException e) {
			// TODO: handle exception
			System.out.println(counter);
			e.printStackTrace();
		}
	}
}
