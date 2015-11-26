package org.thunlp.tagsuggest.dataset;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;
import java.util.Map.Entry;
import java.util.logging.Logger;

import org.thunlp.html.HtmlReformatter;
import org.thunlp.io.JsonUtil;
import org.thunlp.io.RecordReader;
import org.thunlp.io.RecordWriter;
import org.thunlp.language.chinese.LangUtils;
import org.thunlp.misc.Counter;
import org.thunlp.misc.Flags;
import org.thunlp.tagsuggest.common.ConfigIO;
import org.thunlp.tagsuggest.common.KeywordPost;
import org.thunlp.tagsuggest.common.TagFilter;
import org.thunlp.tagsuggest.common.Post;
import org.thunlp.tool.GenericTool;
//this code is wrong
public class Keywordtopost implements GenericTool{
	private static Logger LOG = Logger.getAnonymousLogger();
	JsonUtil J = new JsonUtil();

	@Override
	public void run(String[] args) throws Exception {
		// TODO Auto-generated method stub

		Flags flags = new Flags();
		flags.add("input");
		flags.add("output");
		flags.add("config");
		flags.parseAndCheck(args);
		work(flags.getString("input"), flags.getString("output"), ConfigIO.configFromString(flags.getString("config")));
	}
	
	public void work(String input, String output,  Properties config)
	{
		try{
			LOG.info(input);
			LOG.info(output);
		
			 Set<String> tags = new TreeSet<String>();
			 TagFilter tagFilter = new TagFilter(config, null);
			 int skipped = 0;
			  
			 RecordReader reader = new RecordReader(input);
		    RecordWriter writer = new RecordWriter(output);

			while (reader.next()) {
  		 		 KeywordPost pread = J.fromJson(reader.value(), KeywordPost.class);
				Post p = new Post();
  		 		 p.setContent(clean(pread.getSummary() + pread.getContent()));
			     p.setTitle(clean(pread.getTitle()));
			      tags.clear();
			      tagFilter.filterWithNorm(pread.getTags(), tags);
			      if (tags.size() == 0) {
			        skipped++;
			        continue;
			      }
			      p.setTags(tags);
			      writer.add(J.toJson(p));
			}
			writer.close();
		}catch (Exception e) {
			LOG.info("Error exec!");
		}
	}

	  private String clean(String content) {
	    /*content = HtmlReformatter.getPlainText(content);
	    content = LangUtils.mapChineseMarksToAnsi(content);
	    content = LangUtils.mapFullWidthLetterToHalfWidth(content);
	    content = LangUtils.mapFullWidthNumberToHalfWidth(content);
	    content = LangUtils.T2S(content);
	    content = LangUtils.removeExtraSpaces(content);
	    content = LangUtils.removeEmptyLines(content);*/
	    return content;
	  }

	public static void main(String[] args) throws IOException {
		new Keywordtopost().work("/home/meepo/test/sample/KeywordPost.dat",
															  "/home/meepo/test/sample/keypost.dat",ConfigIO.configFromString(""));
	}
}
