package org.thunlp.tagsuggest.contentbase;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.logging.Logger;
import java.util.Properties;

import org.thunlp.misc.Counter;
import org.thunlp.misc.WeightString;
import org.thunlp.tagsuggest.common.Post;
import org.thunlp.tagsuggest.common.TagSuggest;
import org.thunlp.tagsuggest.common.WordFeatureExtractor;
import org.thunlp.text.Lexicon;
import org.thunlp.text.Lexicon.Word;

public class PMITagSuggest implements TagSuggest {
	private static Logger LOG = Logger.getAnonymousLogger();
	private Lexicon wordLex = null;
	private Lexicon tagLex = null;
	private WordFeatureExtractor fe = null;
	private Properties config = null;
	private int numTags = 5;

	private HashMap<Integer, HashMap<Integer, Double>> pmiMap = new HashMap<Integer, HashMap<Integer, Double>>();
	
	@Override
	public void feedback(Post p) {

	}

	@Override
	public void loadModel(String modelPath) throws IOException {
		
		
		BufferedReader pro = new BufferedReader(new InputStreamReader(
				new FileInputStream(modelPath + File.separator +  "pmi.txt"),
				"UTF-8"));
		String proLine;
		while ((proLine = pro.readLine()) != null) {
			String[] data = proLine.split(" ");
			if (data.length != 3){
				continue;
			}
			int first = Integer.parseInt(data[0]);
			int second = Integer.parseInt(data[1]);
			double probability = Double.parseDouble(data[2]);
			/*
			if(probability<0.01){
				continue;
			}
			*/
			if(!pmiMap.containsKey(first)){
				pmiMap.put(first, new HashMap<Integer, Double>());
			}
			pmiMap.get(first).put(second, probability);
		}
		pro.close();
		
		wordLex = new Lexicon();
		String input = modelPath+"/wordlex";
		File cachedWordLexFile = new File(input);
		if (cachedWordLexFile.exists()) {
			LOG.info("Use cached lexicons");
			wordLex.loadFromFile(cachedWordLexFile);
		}
		
		tagLex = new Lexicon();
		String inputTag = modelPath+"/taglex";
		File cachedTagLexFile = new File(inputTag);
		if (cachedTagLexFile.exists()) {
			LOG.info("Use cached lexicons");
			tagLex.loadFromFile(cachedTagLexFile);
		}
		
	}

	@Override
	public void setConfig(Properties config) {
		fe = new WordFeatureExtractor(config);
		numTags = Integer.parseInt(config.getProperty("num_tags", "5"));
		this.config = config;
	}

	@Override
	public List<WeightString> suggest(Post p, StringBuilder explain) {
		HashMap<Integer, Double> wordTfidf = new HashMap<Integer, Double>();
		
		String[] words = fe.extract(p);
		Counter<String> termFreq = new Counter<String>();
		// calculate the word tfidf
		for (String word : words) {
			if (wordLex.getWord(word) != null)
				termFreq.inc(word, 1);
		}
		Iterator<Entry<String, Long>> iter = termFreq.iterator();
		HashMap<Integer, Double> proMap = new HashMap<Integer, Double>();
		while (iter.hasNext()) {
			Entry<String, Long> e = iter.next();
			String word = e.getKey();
			double tf = (double) e.getValue() / (double) words.length;
			// double idf = (double)D/(double)df.get(word);
			double idf = 0.0;
			if(wordLex.getWord(word) != null){
				idf = Math.log((double) wordLex.getNumDocs()
					/ (double) wordLex.getWord(word).getDocumentFrequency());
			}
			else{
				continue;
			}
			double tfidf = tf * idf;
			int id = wordLex.getWord(word).getId();
			if (pmiMap.containsKey(id)) {
				wordTfidf.put(id, tfidf);
				
				// to suggest the tags
				for (Entry<Integer, Double> ee : pmiMap.get(id).entrySet()) {
					int tagId = ee.getKey();
					if(tagLex.getWord(tagId) != null){
						double pro = ee.getValue();
						if(!proMap.containsKey(tagId)){
							proMap.put(tagId, 0.0);
						}
						proMap.put(tagId, proMap.get(tagId) + tfidf * pro);
					}
				}
			}
		}
		
		// ranking
		List<WeightString> tags = new ArrayList<WeightString>();
		for (Entry<Integer, Double> e : proMap.entrySet()) {
			tags.add(new WeightString(tagLex.getWord(e.getKey()).getName(), e
							.getValue()));
		}
		Collections.sort(tags, new Comparator<WeightString>() {

			@Override
			public int compare(WeightString o1, WeightString o2) {
				return Double.compare(o2.weight, o1.weight);
			}

		});
		return tags;
	}
}
