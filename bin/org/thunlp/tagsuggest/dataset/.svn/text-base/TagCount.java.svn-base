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
import org.thunlp.tagsuggest.common.TagFilter;
import org.thunlp.tagsuggest.common.WordFeatureExtractor;
import org.thunlp.text.Lexicon;
import org.thunlp.tool.GenericTool;

import com.mysql.jdbc.log.Log;

public class TagCount implements GenericTool {
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
		    
		    Set<String> filtered = new HashSet<String>();
			
			RecordReader reader = new RecordReader(input);
			
			BufferedWriter out = new BufferedWriter(
					new OutputStreamWriter(new FileOutputStream(modelDir.getAbsolutePath()+"/tagCount.vcb"),"UTF-8"));
			
		    Counter<String> termFreq = new Counter<String>();

		    while (reader.next()) {
		    	counter ++;	
		    	DoubanPost p = J.fromJson(reader.value(), DoubanPost.class);			
		    	String [] words = fe.extract(p);
				if(words.length <= 0){
					continue;
				}
				tagFilter.filterMapWithNorm(p.getDoubanTags(), filtered);
				for(Entry<String, Integer> e:p.getDoubanTags().entrySet()){
					if(filtered.contains(e.getKey())){
						termFreq.inc(e.getKey(),1);
					}
				}
		    }
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
