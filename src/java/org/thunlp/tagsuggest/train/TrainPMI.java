package org.thunlp.tagsuggest.train;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Vector;
import java.util.logging.Logger;

import javax.swing.text.Position;

import org.thunlp.io.JsonUtil;
import org.thunlp.io.RecordReader;
import org.thunlp.misc.Flags;
import org.thunlp.tagsuggest.common.ConfigIO;
import org.thunlp.tagsuggest.common.DoubanPost;
import org.thunlp.tagsuggest.common.ModelTrainer;
import org.thunlp.tagsuggest.common.TagFilter;
import org.thunlp.tagsuggest.common.WordFeatureExtractor;
import org.thunlp.text.Lexicon;
import org.thunlp.text.Lexicon.Word;
import org.thunlp.tool.GenericTool;

public class TrainPMI implements GenericTool, ModelTrainer {
	private static Logger LOG = Logger.getAnonymousLogger();
	private Properties config = null;
	private String fold = "";
	JsonUtil J = new JsonUtil();
	WordFeatureExtractor fe = null;
	TagFilter tagFilter = null;

	private Comparator<Object> c = new Comparator<Object>() {
		public int compare(Object o1, Object o2) {
			int d1 = ((Vector<Integer>) o1).size();
			int d2 = ((Vector<Integer>) o2).size();
			if (d1 > d2)
				return 1;
			if (d1 == d2)
				return 0;
			else
				return -1;
		}
	};

	private Comparator<Object> cDouble = new Comparator<Object>() {
		public int compare(Object o1, Object o2) {
			double d1 = ((Entry<Integer, Double>) o1).getValue();
			double d2 = ((Entry<Integer, Double>) o2).getValue();
			if (d1 < d2)
				return 1;
			if (d1 == d2)
				return 0;
			else
				return -1;
		}
	};

	@Override
	public void run(String[] args) throws Exception {
		// TODO Auto-generated method stub
		Flags flags = new Flags();
		flags.add("input");
		flags.add("output");
		flags.add("config");
		flags.parseAndCheck(args);

		Properties config = ConfigIO
				.configFromString(flags.getString("config"));
		train(flags.getString("input"), flags.getString("output"), config);
	}

	@Override
	public void train(String inputPath, String modelPath, Properties config)
			throws IOException {
		// TODO Auto-generated method stub
		this.config = config;
		this.fold = config.getProperty("fold", "");

		buildProTable(inputPath, new File(modelPath));
	}

	public void buildProTable(String input, File modelDir) {
		try {
			if (!modelDir.exists()) {
				modelDir.mkdir();
			}

			Lexicon wordlex = new Lexicon();
			Lexicon taglex = new Lexicon();
			WordFeatureExtractor.buildLexicons(input, wordlex, taglex, config);
			fe = new WordFeatureExtractor(config);
			fe.setWordLexicon(wordlex);
			fe.setTagLexicon(taglex);
			tagFilter = new TagFilter(config, taglex);
			HashSet<String> filtered = new HashSet<String>();

			RecordReader reader = new RecordReader(input);

			// the first time : create wordlex and taglex to store the tf and df
			// information
			Lexicon localWordlex = new Lexicon();
			Lexicon localTaglex = new Lexicon();
			File wordLexFile = new File(modelDir.getAbsolutePath() + "/wordlex");
			File tagLexFile = new File(modelDir.getAbsolutePath() + "/taglex");

			int lineCouter = 0;
			HashMap<String, Vector<Integer>> wordInverted = new HashMap<String, Vector<Integer>>();
			HashMap<String, Vector<Integer>> tagInverted = new HashMap<String, Vector<Integer>>();

			HashSet<String> wordSet = new HashSet<String>();
			HashSet<String> tagSet = new HashSet<String>();

			
			
			BufferedWriter out = null;

			if (wordLexFile.exists() && tagLexFile.exists()) {
				LOG.info("Use cached lexicons");
				localWordlex.loadFromFile(wordLexFile);
				localTaglex.loadFromFile(tagLexFile);

				BufferedReader in = new BufferedReader(new InputStreamReader(
						new FileInputStream(modelDir.getAbsolutePath()
								+ "/wordInverted.txt"), "UTF8"));
				String line = "";
				while ((line = in.readLine()) != null) {
					String[] words = line.split(" ");
					wordInverted.put(words[0], new Vector<Integer>());
					for (int i = 1; i < words.length; i++) {
						wordInverted.get(words[0]).add(
								Integer.parseInt(words[i]));
					}
				}
				in.close();

				in = new BufferedReader(new InputStreamReader(
						new FileInputStream(modelDir.getAbsolutePath()
								+ "/tagInverted.txt"), "UTF8"));
				while ((line = in.readLine()) != null) {
					String[] words = line.split(" ");
					tagInverted.put(words[0], new Vector<Integer>());
					for (int i = 1; i < words.length; i++) {
						tagInverted.get(words[0]).add(
								Integer.parseInt(words[i]));
					}
				}
				in.close();
			} else {
				while (reader.next()) {

					lineCouter++;

					wordSet.clear();
					tagSet.clear();

					DoubanPost p = J.fromJson(reader.value(), DoubanPost.class);
					if (fold.length() > 0 && p.getExtras().equals(fold)) {
						continue;
					}
					String[] features = fe.extract(p);
					if (features.length <= 0) {
						continue;
					}

					for (String word : features) {
						if (wordSet.contains(word)) {
							continue;
						} else {
							wordSet.add(word);
						}
						if (!wordInverted.containsKey(word)) {
							wordInverted.put(word, new Vector<Integer>());
						}
						wordInverted.get(word).add(lineCouter);
					}

					tagFilter.filterMapWithNorm(p.getDoubanTags(), filtered);
					if (filtered == null) {
						continue;
					}

					for (String tag : filtered) {
						if (tagSet.contains(tag)) {
							continue;
						} else {
							tagSet.add(tag);
						}
						if (!tagInverted.containsKey(tag)) {
							tagInverted.put(tag, new Vector<Integer>());
						}
						tagInverted.get(tag).add(lineCouter);
					}

					localWordlex.addDocument(features);
					localTaglex.addDocument(filtered
							.toArray(new String[filtered.size()]));

					if (reader.numRead() % 1000 == 0)
						LOG.info(modelDir.getAbsolutePath()
								+ " building lexicons: " + reader.numRead());
				}

				out = new BufferedWriter(new OutputStreamWriter(
						new FileOutputStream(modelDir.getAbsolutePath()
								+ "/wordInverted.txt"), "UTF-8"));
				for (Entry<String, Vector<Integer>> e : wordInverted.entrySet()) {
					out.write(e.getKey());
					for (int num : e.getValue()) {
						out.write(" " + num);
					}
					out.newLine();
					out.flush();
				}
				out.close();

				out = new BufferedWriter(new OutputStreamWriter(
						new FileOutputStream(modelDir.getAbsolutePath()
								+ "/tagInverted.txt"), "UTF-8"));
				for (Entry<String, Vector<Integer>> e : tagInverted.entrySet()) {
					out.write(e.getKey());
					for (int num : e.getValue()) {
						out.write(" " + num);
					}
					out.newLine();
					out.flush();
				}
				out.close();

				localWordlex.saveToFile(wordLexFile);
				localTaglex.saveToFile(tagLexFile);

				reader.close();
				reader = new RecordReader(input);
			}

			LOG.info("First Round Done!" + wordInverted.size() + ":"
					+ tagInverted.size());

			TagFilter localTagFilter = new TagFilter(config, localTaglex);
			HashSet<String> localFiltered = new HashSet<String>();
			HashMap<Integer, HashMap<Integer, Double>> pmiMap = new HashMap<Integer, HashMap<Integer, Double>>();

			Vector<Word> wordVec = new Vector<Word>();
			Vector<Word> tagVec = new Vector<Word>();
			double N = localWordlex.getNumDocs();

			lineCouter = 0;
			int pmiCounter = 0;

			int wordNullCounter = 0;
			int tagNullCounter = 0;
			int positionNullCounter = 0;
			int commonNullCounter = 0;
			int calBeforeCounter = 0;

			int commonLimit = Integer.parseInt(config.getProperty("commonLimit", "5"));
			//BufferedWriter outLog = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("res/pmi/log.txt"), "UTF8"));
			double selfTrans = Double.parseDouble(config.getProperty("selfTrans", "0.0"));
			LOG.info("selfTrans:"+selfTrans);
			
			while (reader.next()) {

				wordSet.clear();
				tagSet.clear();
				wordVec.clear();
				tagVec.clear();

				if (reader.numRead() % 100 == 0)
					LOG.info("Second Round!" + reader.numRead() + ":"
							+ lineCouter + ":" + pmiMap.size() + ":"
							+ pmiCounter + ":" + wordNullCounter + ":"
							+ tagNullCounter + ":" + positionNullCounter + ":"
							+ commonNullCounter + ":" + calBeforeCounter);

				DoubanPost p = J.fromJson(reader.value(), DoubanPost.class);
				if (fold.length() > 0 && p.getExtras().equals(fold)) {
					continue;
				}
				localTagFilter.filterMapWithNorm(p.getDoubanTags(),
						localFiltered);
				if (localFiltered == null || localFiltered.size() == 0) {
					tagNullCounter++;
					continue;
				}

				String[] features = fe.extract(p);
				if (features.length <= 0) {
					wordNullCounter++;
					continue;
				}

				for (String word : features) {
					if (wordSet.contains(word)) {
						continue;
					} else {
						wordSet.add(word);

						Word wordStruct = localWordlex.getWord(word);
						if (wordStruct == null) {
							continue;
						}
						wordVec.add(wordStruct);
					}
				}

				for (String tag : localFiltered) {
					if (tagSet.contains(tag)) {
						continue;
					} else {
						tagSet.add(tag);

						Word tagStruct = localTaglex.getWord(tag);
						if (tagStruct == null) {
							continue;
						}
						tagVec.add(tagStruct);
					}
				}

				if (wordVec.size() == 0 || tagVec.size() == 0) {
					continue;
				}

				lineCouter++;

				for (int i = 0; i < wordVec.size(); i++) {
					int wordId = wordVec.get(i).getId();
					String word = wordVec.get(i).getName();
					Vector<Integer> wordPosition = wordInverted.get(word);
					if (!pmiMap.containsKey(wordId)) {
						pmiMap.put(wordId, new HashMap<Integer, Double>());
					}
					HashMap<Integer, Double> tmpMap = pmiMap.get(wordId);

					for (int j = 0; j < tagVec.size(); j++) {
						int tagId = tagVec.get(j).getId();
						String tag = tagVec.get(j).getName();

						if (tmpMap.containsKey(tagId)) {
							calBeforeCounter++;
							continue;
						} else {
							Vector<Integer> tagPosition = tagInverted.get(tag);
							if (wordPosition == null || tagPosition == null) {
								positionNullCounter++;
								continue;
							}
							
							Vector<Integer> common = interTwoVector(
									wordPosition, tagPosition);
							int commonCounter = common.size();

							if (commonCounter < commonLimit) {
							
								continue;
							}

							double Pw1 = ((double) wordPosition.size()) / N;
							double Pw0 = 1.0 - Pw1;
							double Pt1 = ((double) tagPosition.size()) / N;
							double Pt0 = 1.0 - Pt1;

							double P11 = ((double) commonCounter) / N;
							double P10 = (double) (wordPosition.size() - commonCounter)
									/ N;
							double P01 = (double) (tagPosition.size() - commonCounter)
									/ N;
							double P00 = 1.0 - P11 - P10 - P01;
							
							double pmi10 = (wordPosition.size() - commonCounter == 0) ? 0.0 : P10 * Math.log(P10 / Pw1 / Pt0);
							double pmi01 = (tagPosition.size() - commonCounter == 0) ? 0.0 : P01 * Math.log(P01 / Pw0 / Pt1);
									
							double pmi = P11 * Math.log(P11 / Pw1 / Pt1) + pmi10 + pmi01 + P00 * Math.log(P00 / Pw0 / Pt0);
							
							tmpMap.put(tagId, pmi);
							pmiCounter++;
						}
					}
					pmiMap.put(wordId, tmpMap);
				
				}

			}
			reader.close();
		
			LOG.info("PMI size!" + pmiMap.size());
					
			out = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(modelDir.getAbsolutePath()
							+ "/pmi.txt"), "UTF-8"));
			
			BufferedWriter outCheck = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(modelDir.getAbsolutePath()
							+ "/check.txt"), "UTF-8"));
			
			for (Entry<Integer, HashMap<Integer, Double>> e : pmiMap.entrySet()) {
				double total = 0.0;
				for(Entry<Integer, Double> ee : e.getValue().entrySet()){
					total += ee.getValue();
				}
				
				if(total == 0){
					continue;
				}
				
				for(Entry<Integer, Double> ee : e.getValue().entrySet()){
					ee.setValue(ee.getValue() / total);
				}
				
				int wordId = e.getKey();
				Word wordStruct = localWordlex.getWord(wordId);
				if(wordStruct != null){
					String word = wordStruct.getName();
					Word tagStruct = localTaglex.getWord(word);
					if(tagStruct != null){
						int tagId = tagStruct.getId();
						for(Entry<Integer, Double> ee : e.getValue().entrySet()){
							ee.setValue((1.0 - selfTrans) * ee.getValue());
						}
						if(!e.getValue().containsKey(tagId)){
							e.getValue().put(tagId, 0.0);
						}
						e.getValue().put(tagId, e.getValue().get(tagId) + selfTrans);
					}
				}
				
				Object[] ans = e.getValue().entrySet().toArray();
				Arrays.sort(ans, cDouble);

				outCheck.write(wordStruct.getName()+":");
				outCheck.newLine();
				outCheck.flush();
				
				for (Object s : ans) {
					int tagId = ((Entry<Integer, Double>) s).getKey();
					double score = ((Entry<Integer, Double>) s).getValue();
					out.write(e.getKey() + " " + tagId + " " + score );
					out.newLine();
					out.flush();
					
					outCheck.write("\t"+localTaglex.getWord(tagId).getName()+":"+score);
					outCheck.newLine();
					outCheck.flush();
				}
			}
			out.close();
			outCheck.close();

		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			LOG.info("Error exec!");
		}
	}

	public void checkPmi(String input, String wordLexFile, String tagLexFile,
			String output) throws IOException {
		Lexicon wordLex = new Lexicon();
		Lexicon tagLex = new Lexicon();
		wordLex.loadFromFile(new File(wordLexFile));
		tagLex.loadFromFile(new File(tagLexFile));

		BufferedReader in = new BufferedReader(new InputStreamReader(
				new FileInputStream(input), "UTF8"));
		BufferedWriter out = new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream(output), "UTF8"));
		String line = "";
		while ((line = in.readLine()) != null) {
			String[] words = line.split(" ");
			if(words.length != 3){
				continue;
			}
			int wordId = Integer.parseInt(words[0]);
			int tagId = Integer.parseInt(words[1]);
			double pro = Double.parseDouble(words[2]);
			
			if(pro < 0.01){
				continue;
			}
			
			out.write(wordLex.getWord(wordId).getName() + " "
					+ tagLex.getWord(tagId).getName() + " " + pro);
			out.newLine();
			out.flush();
		}
		in.close();
		out.close();
	}

	public Vector<Integer> interTwoVector(Vector<Integer> a, Vector<Integer> b) {
		Vector<Integer> ans = new Vector<Integer>();
		int aLen = a.size();
		int bLen = b.size();
		int i = 0;
		int j = 0;
		while (i < aLen && j < bLen) {
			int aValue = a.get(i).intValue();
			int bValue = b.get(j).intValue();
			if (aValue == bValue) {
				ans.add(aValue);
				i++;
				j++;
			} else if (aValue < bValue) {
				i++;
			} else {
				j++;
			}
		}

		return ans;
	}

	public static void main(String[] args) throws IOException {
		TrainPMI pmi = new TrainPMI();
	
		pmi.checkPmi("res/pmi/train/70000selfTrans0_2/model.0.gz/pmi.txt", "res/pmi/train/70000selfTrans0_2/model.0.gz/wordlex",
				"res/pmi/train/70000selfTrans0_2/model.0.gz/taglex", "res/pmi/train/70000selfTrans0_2/model.0.gz/output.txt");
	}
}
