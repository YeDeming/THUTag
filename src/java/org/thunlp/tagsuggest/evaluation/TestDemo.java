package org.thunlp.tagsuggest.evaluation;


import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import org.thunlp.html.HtmlReformatter;
import org.thunlp.misc.Flags;
import org.thunlp.misc.WeightString;
import org.thunlp.tagsuggest.common.ConfigIO;
import org.thunlp.tagsuggest.common.Post;
import org.thunlp.tagsuggest.common.TagSuggest;
import org.thunlp.tool.GenericTool;

public class TestDemo  implements GenericTool{
	  TagSuggest suggester = null;
		private static Logger LOG = Logger.getAnonymousLogger();

	 public void run(String[] args) throws Exception {
		   
		  	Flags flags = new Flags();
		    flags.add("algorithm");
		    flags.add("model_path");
		    flags.add("config");
		    flags.add("article_path");
		    flags.add("output_path");
		    flags.parseAndCheck(args);

		    Properties config = ConfigIO.configFromString(flags.getString("config"));
		    suggester = loadTagSuggester(
		        flags.getString("algorithm"), flags.getString("model_path"));
		    suggester.setConfig(config);
		    
		    doSuggest(flags.getString("article_path"),flags.getString("output_path"));
	 }
	  private TagSuggest loadTagSuggester(String name, String modelPath) 
			  throws Exception {
			    if (!name.startsWith("org")) {
			      name = "org.thunlp.tagsuggest.contentbase." + name;
			    }
			    TagSuggest ts = (TagSuggest) Class.forName(name).newInstance();
			    ts.loadModel(modelPath);
			    return ts;
			  }
			  
	public void doSuggest(String article_path,String output_path)  throws IOException{

			BufferedReader reader = new BufferedReader(
				new InputStreamReader(
						new FileInputStream(
								article_path),
						"UTF-8"));
		
			String title = reader.readLine();
			String content = reader.readLine();
			
		    Post p = new Post();
		    p.setTitle(title);
		    p.setContent(content);

		    StringBuilder explain = new StringBuilder();
		    List<WeightString> tags = suggester.suggest(p, explain);
		    
		    BufferedWriter outTag = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(output_path),
					"UTF-8"));
			int cnt =  0;
		    for (WeightString ws : tags) {
		    	outTag.write(ws.text + " " + ws.weight);
		    	LOG.info(ws.text + " " + ws.weight);
		    	++ cnt;
		    	if (cnt>10) break;
		    	outTag.newLine();
			    outTag.flush();    
		    }
		}

}
