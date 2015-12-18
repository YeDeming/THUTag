package org.thunlp.tagsuggest.contentbase;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Vector;
import java.util.Map.Entry;
import java.util.logging.Logger;

import org.apache.lucene.analysis.WhitespaceAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.MultiFieldQueryParser;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TopDocs;
import org.thunlp.matrix.NormalMatrix;
import org.thunlp.matrix.pagerank.PageRank;
import org.thunlp.misc.Counter;
import org.thunlp.misc.WeightString;
import org.thunlp.tagsuggest.common.KeywordPost;
import org.thunlp.tagsuggest.common.LegacyFeatureExtractor;
import org.thunlp.tagsuggest.common.Post;
import org.thunlp.tagsuggest.common.TagSuggest;
import org.thunlp.tagsuggest.common.WordFeatureExtractor;

public class ExpandRankKE implements TagSuggest {
	private static Logger LOG = Logger.getAnonymousLogger();
	private IndexSearcher docsSearcher = null;
	private QueryParser queryParser = null;
	//private LegacyFeatureExtractor extractor = new LegacyFeatureExtractor();
	private WordFeatureExtractor extractor = null;
	private Properties config = new Properties();
	private static List<WeightString> EMPTY_SUGGESTION = new LinkedList<WeightString>();

	private int k = 1;
	private int numKeywords = 10;

	private static List<Vector<Double>> answerTf = new ArrayList<Vector<Double>>();
	private static List<Vector<Double>> suggestTf = new ArrayList<Vector<Double>>(); 
	
	@Override
	public void feedback(Post p) {
		// Do nothing.
	}

	@Override
	public void loadModel(String modelPath) throws IOException {
		docsSearcher = new IndexSearcher((new File(modelPath, "docs"))
				.getAbsolutePath());
		String[] fields = { "doc_id", "content", "user_id", "tag" };
		queryParser = new MultiFieldQueryParser(fields,
				new WhitespaceAnalyzer());
	}

	@Override
	public void setConfig(Properties config) {
		this.config = config;
		this.extractor = new WordFeatureExtractor(config);
		this.k = Integer.parseInt(config.getProperty("k", "1"));
		this.numKeywords = Integer.parseInt(config
				.getProperty("keywords", "10"));
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
	
	public void addEdge(NormalMatrix matrix, Vector<Integer> v, int start,
			int end, double score) {
		for (int i = start; i < end; i++) {
			for (int j = i + 1; j <= end; j++) {
				matrix.add(v.get(i), v.get(j), score);
				matrix.add(v.get(j), v.get(i), score);
			}
		}
	}

	@Override
	public List<WeightString> suggest(Post p, StringBuilder explain) {
		// We first extract TF*IDF weighted keywords from post p. Then we use
		// these
		// keywords to form a query to Lucene index. Finally, we collect the
		// tags in
		// relevant documents as the suggestion.

		// String content = p.getTitle() + " " + p.getContent();
		String content = p.getTitle() + " " + ((KeywordPost) p).getSummary()
				+ " " + p.getContent();
		content = extractor.clean(content);
		List<WeightString> keywords = extractKeywords(content);
		Query q;
		try {
			q = makeQueryFromKeywords(keywords, numKeywords);
		} catch (ParseException e1) {
			// TODO Auto-generated catch block
			LOG.warning("Cannot make query from " + p.getId());
			return EMPTY_SUGGESTION;
		}
		TopDocs topDocs;
		try {
			topDocs = docsSearcher.search(q, null, k);
		} catch (IOException e1) {
			LOG.warning("IOException when search for " + p.getId());
			return EMPTY_SUGGESTION;
		}

		String[] words = extractor.getWords(content);
		Counter<String> termFreq = new Counter<String>();

		// calculate the word tf
		HashMap<String, Integer> textMap = new HashMap<String, Integer>();
		Vector<Integer> textWordId = new Vector<Integer>();
		int num = 0;

		for (String word : words) {
			termFreq.inc(word, 1);
			if (!textMap.containsKey(word)) {
				textMap.put(word, num);
				textWordId.add(num);
				num++;
			} else {
				textWordId.add(textMap.get(word));
			}
		}
		
		// Collect tags.
		//Map<String, Double> tags = new Hashtable<String, Double>();
		Vector<Vector<Integer>> otherTexts = new Vector<Vector<Integer>>();
		Vector<Double> scores = new Vector<Double>();
		for (int i = 0; i < topDocs.scoreDocs.length; i++) {
			int resultId = topDocs.scoreDocs[i].doc;
			double score = topDocs.scoreDocs[i].score;
			Document doc;
			try {
				doc = docsSearcher.doc(resultId);
			} catch (CorruptIndexException e1) {
				LOG.warning("Corrupted index when searching for " + p.getId());
				return EMPTY_SUGGESTION;
			} catch (IOException e1) {
				LOG.warning("IOException when looking up doc " + p.getId());
				return EMPTY_SUGGESTION;
			}
			
			String[] otherWords = extractor.getWords(doc.get("content"));
			Vector<Integer> otherText = new Vector<Integer>();
			for (String word : otherWords) {
				if (!textMap.containsKey(word)) {
					textMap.put(word, num);
					otherText.add(num);
					num++;
				} else {
					otherText.add(textMap.get(word));
				}
			}
			otherTexts.add(otherText);
			scores.add(score);
			
			/*
			String[] docTags = doc.get("tags").split(" ");
			for (String tagStr : docTags) {
				Double weight = tags.get(tagStr);
				if (weight == null) {
					weight = 0.0;
				}
				tags.put(tagStr, weight + score);
			}
			*/
		}
		
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
		
		for(int j = 0; j < otherTexts.size(); j ++){
			len = otherTexts.get(j).size();
			double score = scores.get(j);
			if (len < window) {
				for (int i = 1; i < len; i++) {
					addEdge(matrix, otherTexts.get(j), 0, i, score);
				}
				for (int i = 1; i < len - 1; i++) {
					addEdge(matrix, otherTexts.get(j), i, len - 1, score);
				}
			} else {
				for (int i = 1; i < window - 1; i++) {
					addEdge(matrix, otherTexts.get(j), 0, i, score);
				}
				for (int i = 0; i <= len - window; i++) {
					addEdge(matrix, otherTexts.get(j), i, i + window - 1, score);
				}
				for (int i = len - window + 1; i < len - 1; i++) {
					addEdge(matrix, otherTexts.get(j), i, len - 1, score);
				}
			}
		}
		
		PageRank.prepareMatrix(matrix);
		double rankValue[] = PageRank.pageRank(matrix, 100);

		Iterator<Entry<String, Long>> iter = termFreq.iterator();
		List<WeightString> suggested = new ArrayList<WeightString>();
		while (iter.hasNext()) {
			Entry<String, Long> e = iter.next();
			String word = e.getKey();
			int textId = textMap.get(word);
			double rank = rankValue[textId];
			suggested.add(new WeightString(word, rank));
		}

		// Weight tags.
		Collections.sort(suggested, new Comparator<WeightString>() {
			@Override
			public int compare(WeightString o1, WeightString o2) {
				return Double.compare(o2.weight, o1.weight);
			}

		});
		
		double normalTf = 0.0;
		Vector<Double> record = new Vector<Double>();
		for(int i = 0; i < suggested.size() && i < 2; i ++){
			String keyword = suggested.get(i).text;
			long tf = termFreq.get(keyword);
			if(tf != 0l && p.getTags().contains(keyword)){
				normalTf = (double) tf / (double) words.length;
				normalTf = (int)(normalTf * 1000) / 1000.0;
				record.add(normalTf);
			}
		}
		suggestTf.add(record);
		
		Vector<Double> recordAnswer = new Vector<Double>();
		for(String keyword : p.getTags()){
			long tf = termFreq.get(keyword);
			if(tf != 0l){
				normalTf = (double) tf / (double) words.length;
				normalTf = (int)(normalTf * 1000) / 1000.0;
				recordAnswer.add(normalTf);
			}
		}
		answerTf.add(recordAnswer);

		return suggested;
	}
	
	public static void outputGeneration(String filename) throws IOException{
		BufferedWriter outG = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filename+"1"),"UTF-8"));
		HashMap<Double, Integer> tfCount = new HashMap<Double, Integer>();
		for(int i = 0 ; i < answerTf.size(); i ++){
			for(int j = 0 ; j < answerTf.get(i).size(); j ++){
				double tf = answerTf.get(i).get(j);
				if(!tfCount.containsKey(tf)){
					tfCount.put(tf, 0);
				}
				tfCount.put(tf, tfCount.get(tf) + 1);
			}
		}
		Object[] ans = tfCount.entrySet().toArray();
		Comparator<Object> c = new Comparator<Object>(){
			public int compare(Object o1, Object o2) {
			    double d1 = ((Entry<Double,Integer>)o1).getKey();
			    double d2 = ((Entry<Double,Integer>)o2).getKey();
			    if(d1 > d2)return 1;
			    if(d1 == d2)return 0;
			    else return -1;
			    }
			};
		Arrays.sort(ans, c);
		
		for(Object s:ans){
			outG.write(((Entry<Integer,Integer>)s).getKey() + " " + ((Entry<Integer,Integer>)s).getValue());
			outG.newLine();
			outG.flush();
		}
		tfCount.clear();
		outG = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filename+"2"),"UTF-8"));
		for(int i = 0 ; i < suggestTf.size(); i ++){
			for(int j = 0 ; j < suggestTf.get(i).size(); j ++){
				double tf = suggestTf.get(i).get(j);
				if(!tfCount.containsKey(tf)){
					tfCount.put(tf, 0);
				}
				tfCount.put(tf, tfCount.get(tf) + 1);
			}
		}
		Object[] ans2 = tfCount.entrySet().toArray();
		Arrays.sort(ans2, c);
		
		for(Object s:ans2){
			outG.write(((Entry<Integer,Integer>)s).getKey() + " " + ((Entry<Integer,Integer>)s).getValue());
			outG.newLine();
			outG.flush();
		}
		outG.close();
	}

	public Query makeQueryFromKeywords(List<WeightString> keywords, int n)
			throws ParseException {
		StringBuilder queryString = new StringBuilder();
		for (int i = 0; i < n && i < keywords.size(); i++) {
			if (i > 0)
				queryString.append(' ');
			queryString.append(keywords.get(i).text);
			queryString.append('^');
			queryString.append(String.format("%.2f", Math
					.log(keywords.get(i).weight + 1)));
		}
		if (queryString.length() == 0)
			queryString.append("a");
		Query q = queryParser.parse(queryString.toString());
		return q;
	}

	public List<WeightString> extractKeywords(String content) {
		String[] words = extractor.getWords(content);
		Counter<String> termFreq = new Counter<String>();
		for (String word : words) {
			termFreq.inc(word, 1);
		}
		double maxDocs = 100000;
		try {
			maxDocs = (double) docsSearcher.maxDoc();
		} catch (IOException e1) {
			LOG.warning("Cannot query the total number of docs.");
			e1.printStackTrace();
		}
		Iterator<Entry<String, Long>> iter = termFreq.iterator();
		List<WeightString> keywords = new ArrayList<WeightString>();
		while (iter.hasNext()) {
			Entry<String, Long> e = iter.next();
			double tf = (double) e.getValue() / (double) words.length;
			double df = 1;
			try {
				df = docsSearcher.docFreq(new Term("content", e.getKey()));
			} catch (IOException e1) {
				LOG
						.warning("Cannot query document frequency for "
								+ e.getKey());
				e1.printStackTrace();
			}
			double idf = 0.0;
			if (df > 0.0)
				idf = maxDocs / df;
			else
				idf = 0.0;
			keywords.add(new WeightString(e.getKey(), tf * idf));
		}

		Collections.sort(keywords, new Comparator<WeightString>() {
			public int compare(WeightString o1, WeightString o2) {
				return Double.compare(o2.weight, o1.weight);
			}
		});
		// LOG.info("Keywords:" + StringUtil.join(keywords, ","));
		return keywords;
	}

}
