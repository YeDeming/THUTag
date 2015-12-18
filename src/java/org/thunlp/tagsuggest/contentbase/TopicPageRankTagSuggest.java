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

import org.thunlp.hadooplda.LdaModel;
import org.thunlp.io.JsonUtil;
import org.thunlp.io.RecordReader;
import org.thunlp.matrix.NormalMatrix;
import org.thunlp.matrix.pagerank.PageRank;
import org.thunlp.misc.WeightString;
import org.thunlp.tagsuggest.common.ConfigIO;
import org.thunlp.tagsuggest.common.GenerativeTagSuggest;
import org.thunlp.tagsuggest.common.KeywordPost;
import org.thunlp.tagsuggest.common.Post;
import org.thunlp.tagsuggest.common.TagSuggest;
import org.thunlp.tagsuggest.common.WordFeatureExtractor;

import java.lang.Thread;

/**
 * This class use the Tag-LDA method proposed by Xiance Si.
 * 
 * @author sixiance
 * 
 */
public class TopicPageRankTagSuggest implements TagSuggest {
	private static Logger LOG = Logger.getAnonymousLogger();
	private LdaModel model  = null;
	private Properties config = null;
	private WordFeatureExtractor extractor  =  null;//new WordFeatureExtractor();
	private int numTags = 10;
	private static String[] EMPTY_TAG_SET = new String[0];
	private static int[] EMPTY_REASON_SET = new int[0];
	private double[] pzd = null;
	private double[] pwz = null;
	private double[] ptz = null;
	private static  JsonUtil J = new JsonUtil();
	private static double[] PRresult = null;
	private static double[] rankResult = null;
	private static int num = 0;
	
	@Override
	public void feedback(Post p) {
	}

	@Override
	  public void loadModel(String modelPath) throws IOException {
	    FileInputStream input = new FileInputStream(modelPath);
	    model = new LdaModel(input);
	    input.close();
	    LOG.info("Load LDA model of " + model.getNumTopics()  + " topics");
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
		String[] features = extractor.extractKeyword((KeywordPost) p, true, true, true);
	    pzd = new double[model.getNumTopics()];
	    int [] topics = new int[features.length];
	    model.inference(features, topics, pzd);
	    
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

		double[] impact = new double[num];
		double[] init = new double[num];
		rankResult =  new double[num];
	
		for (int i = 0; i < num; ++i)
			rankResult[i] = 0;
		for (int i = 0; i != init.length; i++) {
			init[i] = 1.0;
		}

		for (int i = 0; i < model.getNumTopics(); i++) {
			for (int j = 0; j < num; j++) {
				impact[j] = model.pwz(textWordMap.get(j), i);
			}
			double[] rankValue = PageRank.pageRank(matrix, 100, 0.85, init, impact);
			
			for (int j = 0; j < num; ++j) 
				rankResult[j] += rankValue[j] * pzd[i];
		}

		List<WeightString> results = new ArrayList<WeightString>();
		if (num == 0)
			return results;

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