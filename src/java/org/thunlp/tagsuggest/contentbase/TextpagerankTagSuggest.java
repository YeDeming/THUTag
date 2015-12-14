package org.thunlp.tagsuggest.contentbase;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.Vector;
import java.util.Map.Entry;
import java.util.logging.Logger;
import java.util.HashSet;

import org.thunlp.io.JsonUtil;
import org.thunlp.io.RecordReader;
import org.thunlp.matrix.NormalMatrix;
import org.thunlp.matrix.pagerank.PageRank;
import org.thunlp.misc.WeightString;
import org.thunlp.tagsuggest.common.ConfigIO;
import org.thunlp.tagsuggest.common.GenerativeTagSuggest;
import org.thunlp.tagsuggest.common.Post;
import org.thunlp.tagsuggest.common.TagSuggest;
import org.thunlp.tagsuggest.common.WordFeatureExtractor;
import org.thunlp.tagsuggest.contentbase.NoiseTagLdaModel.Document;

import java.lang.Thread;

/**
 * This class use the Tag-LDA method proposed by Xiance Si.
 * 
 * @author sixiance
 * 
 */
public class TextpagerankTagSuggest implements TagSuggest {
	private static Logger LOG = Logger.getAnonymousLogger();
	private Properties config = null;
	private WordFeatureExtractor extractor = new WordFeatureExtractor();
	private int numTags = 10;
	private static String[] EMPTY_TAG_SET = new String[0];
	private static int[] EMPTY_REASON_SET = new int[0];
	private static  JsonUtil J = new JsonUtil();
	private static double[] PRresult = null;
	private static double[] rankResult = null;
	private static int num = 0;

	public static void main(String[] args) throws IOException {
		TextpagerankTagSuggest lda = new TextpagerankTagSuggest();
		lda.setConfig(ConfigIO.configFromString("numtags=10;norm=all_log;k=5;dataType=Post"));
		lda.loadModel("/home/niuyl/java/work/TagSuggestion/working_dir");
		
		RecordReader reader = new RecordReader("/home/niuyl/java/work/TagSuggestion/post/post.dat");
	    StringBuilder explain = new StringBuilder("");
	    int rightnum = 0, allnum = 0;
	    while (reader.next()) {
	    	++allnum;
	      Post p = J.fromJson(reader.value(), Post.class);
	      lda.suggest(p, explain);
	    }
	}
	
	@Override
	public void feedback(Post p) {
	}

	@Override
	public void loadModel(String modelPath) throws IOException {

	}

	@Override
	public void setConfig(Properties config) {
		this.config = config;
		extractor = new WordFeatureExtractor(config);
		numTags = Integer.parseInt(config.getProperty("numtags", "10"));
	}

	public void addEdge(NormalMatrix matrix, Vector<Integer> v, int start,
			int end) {
		for (int i = start; i < end; i++) {
			for (int j = i + 1; j <= end; j++) {
				matrix.add(v.get(i), v.get(j), 1);
				matrix.add(v.get(j), v.get(i), 1);
			}
		}
	}

	@Override
	public List<WeightString> suggest(Post p, StringBuilder explain) {

		String[] features = extractor.extract(p);
		Document d = new Document(features, EMPTY_TAG_SET);

		// for TextRank
		HashMap<String, Integer> textMap = new HashMap<String, Integer>();
		HashMap<Integer, String> textWordMap = new HashMap<Integer, String>();
		Vector<Integer> textWordId = new Vector<Integer>();
		num = 0;

		for (String word : features) {
				if (!textMap.containsKey(word)) {
					textMap.put(word, num);
					textWordMap.put(num, word);
					textWordId.add(num);
					num++;
				} else {
					textWordId.add(textMap.get(word));
				}
		}

		// calculate the TextRank value
		NormalMatrix matrix = new NormalMatrix(num, num);
		int window = 10;
		int len = textWordId.size();
		if (len < window) {
			for (int i = 1; i < len; i++) {
				addEdge(matrix, textWordId, 0, i);
			}
			for (int i = 1; i < len - 1; i++) {
				addEdge(matrix, textWordId, i, len - 1);
			}
		} else {
			for (int i = 1; i < window - 1; i++) {
				addEdge(matrix, textWordId, 0, i);
			}
			for (int i = 0; i <= len - window; i++) {
				addEdge(matrix, textWordId, i, i + window - 1);
			}
			for (int i = len - window + 1; i < len - 1; i++) {
				addEdge(matrix, textWordId, i, len - 1);
			}
		}
		PageRank.prepareMatrix(matrix);
		
		double[] rankResult = PageRank.pageRank(matrix, 100);
		
		List<WeightString> results = new ArrayList<WeightString>();
		
		for (int i = 0; i < num; ++ i)
		{
			results.add(new WeightString(textWordMap.get(i),  rankResult[i]));
		}
	
		 Collections.sort(results, new Comparator<WeightString>() {
			@Override
			public int compare(WeightString o1, WeightString o2) {
				return Double.compare(o2.weight, o1.weight);
			}
		});
		if (results.size() > numTags)
			results = results.subList(0, numTags);
		
		return results;
	}
}
