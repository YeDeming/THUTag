package org.thunlp.tagsuggest.train;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Properties;
import java.util.Random;
import java.util.Set;
import java.util.Vector;
import java.util.Map.Entry;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import org.apache.hadoop.mapred.loadhistory_jsp;
import org.thunlp.io.JsonUtil;
import org.thunlp.io.RecordReader;
import org.thunlp.language.chinese.ForwardMaxWordSegment;
import org.thunlp.language.chinese.LangUtils;
import org.thunlp.language.chinese.WordSegment;
import org.thunlp.misc.Counter;
import org.thunlp.misc.Flags;
import org.thunlp.tagsuggest.common.ConfigIO;
import org.thunlp.tagsuggest.common.KeywordPost;
import org.thunlp.tagsuggest.common.Post;
import org.thunlp.tagsuggest.common.RtuMain;
import org.thunlp.tagsuggest.common.ModelTrainer;
import org.thunlp.tagsuggest.common.TagFilter;
import org.thunlp.tagsuggest.common.WordFeatureExtractor;
import org.thunlp.tagsuggest.train.TrainWTM.StreamGobbler;
import org.thunlp.text.Lexicon;
import org.thunlp.tool.GenericTool;

public class TrainWAMWithtitleInstead implements GenericTool, ModelTrainer {
	private static Logger LOG = Logger.getAnonymousLogger();
	private Properties config = null;
	private String fold = "";
	private String giza_path = null;
	private RtuMain jar_path = new RtuMain();
	
	JsonUtil J = new JsonUtil();
	WordFeatureExtractor fe = null;
	TagFilter tagFilter = null;
	WordSegment ws = null;

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
		
		giza_path = config.getProperty("giza_path", jar_path.getProjectPath());
		LOG.info("giza_path:" + giza_path);

		buildProTable(inputPath, new File(modelPath));
	}

	public void buildProTable(String input, File modelDir) {
		int counter = 0;
		try {
			if (!modelDir.exists()) {
				modelDir.mkdir();
			}

			ws = new ForwardMaxWordSegment();
			Lexicon wordlex = new Lexicon();
			Lexicon taglex = new Lexicon();
			WordFeatureExtractor.buildLexicons(input, wordlex, taglex, config);
			fe = new WordFeatureExtractor(config);
			fe.setWordLexicon(wordlex);
			fe.setTagLexicon(taglex);
			tagFilter = new TagFilter(config, taglex);
			Set<String> filtered = new HashSet<String>();
			HashSet<String> tagSet = new HashSet<String>();

			RecordReader reader = new RecordReader(input);

			// the first time : create wordlex and taglex to store the tf and df
			// information
			Lexicon localWordlex = new Lexicon();
			Lexicon localTaglex = new Lexicon();
			File wordLexFile = new File(modelDir.getAbsolutePath() + "/wordlex");
			File tagLexFile = new File(modelDir.getAbsolutePath() + "/taglex");
			if (wordLexFile.exists() && tagLexFile.exists()) {
				LOG.info("Use cached lexicons");
				localWordlex.loadFromFile(wordLexFile);
				localTaglex.loadFromFile(tagLexFile);
			} else {
				while (reader.next()) {
					KeywordPost p = J.fromJson(reader.value(), KeywordPost.class);
					if (fold.length() > 0 && p.getExtras().equals(fold)) {
						continue;
					}
					String[] features = fe.extractKeyword(p,true,true,true);
					if (features.length <= 0) {
						continue;
					}
					tagFilter.filterWithNorm(p.getTags(), filtered);
					if (filtered == null) {
						continue;
					}
					localWordlex.addDocument(features);
					localTaglex.addDocument(filtered
							.toArray(new String[filtered.size()]));

					if (reader.numRead() % 1000 == 0)
						LOG.info(modelDir.getAbsolutePath()
								+ " building lexicons: " + reader.numRead());
				}
				localWordlex.saveToFile(wordLexFile);
				localTaglex.saveToFile(tagLexFile);
				reader.close();
				reader = new RecordReader(input);
			}

			BufferedWriter out = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(modelDir.getAbsolutePath() + "/book"),
					"UTF-8"));

			BufferedWriter outTag = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(modelDir.getAbsolutePath()
							+ "/bookTag"), "UTF-8"));
			
			double scoreLimit = Double.parseDouble(config.getProperty("scoreLimit", "0.1"));
			// the second time :
			while (reader.next()) {
				counter++;
				KeywordPost p = J.fromJson(reader.value(), KeywordPost.class);
				if (fold.length() > 0 && p.getExtras().equals(fold)) {
					continue;
				}				
				
				String content = p.getSummary() + p.getContent();
				content = content.replaceAll("\n", "");
				String [] sentences = content.split("。|！");
				
				if(sentences.length < 1){
					continue;
				}
				
				// use the first sentence to replace the title
				String[] titleWords = ws.segment(sentences[0]);
				
				for(int i = 0; i < titleWords.length; i ++){
					if(localWordlex.getWord(titleWords[i]) != null){
						if(i == 0){
							out.write(titleWords[i]);
							outTag.write(titleWords[i]);
						}else{
							out.write(" "+titleWords[i]);
							outTag.write(" "+titleWords[i]);
						}
					}
				}
				out.newLine();
				out.flush();
				outTag.newLine();
				outTag.flush();
				
				Vector<Double> wordTfidf = new Vector<Double>();
				Vector<String> wordList = new Vector<String>();
				double normalize = 0.0;
				Counter<String> termFreq = new Counter<String>();
				for (String word : titleWords) {
					termFreq.inc(word, 1);
				}
				Iterator<Entry<String, Long>> iter = termFreq.iterator();
				while (iter.hasNext()) {
					Entry<String, Long> e = iter.next();
					String word = e.getKey();
					if(localWordlex.getWord(word) == null){
						continue;
					}
					wordList.add(word);
					
					double tf = ((double) e.getValue())
							/ ((double) titleWords.length);
					double idf = Math.log(((double) localWordlex.getNumDocs())
							/ ((double) localWordlex.getWord(word)
									.getDocumentFrequency()));
					double tfidf = tf * idf;
					wordTfidf.add(tfidf);
					normalize += tfidf * tfidf;
				}
				Vector<Double> wordProb = new Vector<Double>();
				for (int i = 0; i < wordTfidf.size(); i++) {
					wordProb.add(wordTfidf.elementAt(i) / normalize);
				}
				
				
				HashMap<String, Integer> contentTf = new HashMap<String, Integer>();
				HashMap<String, Double> contentTfidf = new HashMap<String, Double>();
				for(int i = 1; i < sentences.length; i ++){
					contentTf.clear();
					contentTfidf.clear();

					double score = 0.0;
					String sentence = sentences[i];
					String[] words = ws.segment(sentence);
					for (String word : words) {
						if(contentTf.containsKey(word)){
							int tmp = contentTf.get(word);
							contentTf.put(word, tmp + 1);
						}
						else{
							contentTf.put(word, 1);
						}
					}
					normalize = 0.0;
					for(Entry<String, Integer> e : contentTf.entrySet()){
						String word = e.getKey();
						if(localWordlex.getWord(word) == null){
							continue;
						}
						double tf = ((double) e.getValue()) / ((double) words.length);
						double idf = Math.log(((double) localWordlex.getNumDocs())
								/ ((double) localWordlex.getWord(word)
										.getDocumentFrequency()));
						double tfidf = tf * idf;
						contentTfidf.put(word, tfidf);
						normalize += tfidf * tfidf;
					}
					for(Entry<String, Double> e : contentTfidf.entrySet()){
						e.setValue(e.getValue() / normalize);
					}
					for(int j = 0; j < wordList.size(); j ++){
						String word = wordList.get(j);
						if(contentTfidf.containsKey(word)){
							score += contentTfidf.get(word) * wordProb.get(j);
						}
					}
					if(score >= scoreLimit){
						for(int j = 0; j < words.length; j ++){
							if(localWordlex.getWord(words[j]) != null){
								if(j == 0 ){
									out.write(words[j]);
								}
								else{
									out.write(" "+words[j]);
								}
							}
						}
						out.newLine();
						out.flush();
						
						for(int j = 0; j < titleWords.length; j ++){
							if(localWordlex.getWord(titleWords[j]) != null){
								if(j == 0){
									outTag.write(titleWords[j]);
								}else{
									outTag.write(" "+titleWords[j]);
								}
							}
						}
						outTag.newLine();
						outTag.flush();
					}
				}
				
			}
			
			reader.close();
			out.close();
			outTag.close();

			LOG.info("source and target are prepared!");

			// training
			Runtime rn = Runtime.getRuntime();
			Process p = null;
		
			p = rn
					.exec(giza_path+File.separator + "mkcls -c80 -pbook -Vbook.vcb.classes opt",							
							null, modelDir);
			p.waitFor();
			p = rn
					.exec(giza_path+File.separator + "mkcls -c80 -pbookTag -VbookTag.vcb.classes opt",
						null, modelDir);
			p.waitFor();
			LOG.info("mkcls ok!");
			p = rn
					.exec(giza_path+File.separator +"plain2snt.out bookTag book",
							null, modelDir);
			p.waitFor();
			LOG.info("plain2snt ok!");

			// from word to tag
			p = rn.exec(giza_path+File.separator +"GIZA++ -S book.vcb -T bookTag.vcb -C book_bookTag.snt  -m1 5 -m2 0 -mh 0 -m3 0 -m4 0 -model1dumpfrequency 1"		
					,null, modelDir);
			StreamGobbler errorGobbler = new StreamGobbler(p.getErrorStream(),
					"Error");
			StreamGobbler outputGobbler = new StreamGobbler(p.getInputStream(),
					"Output");
			errorGobbler.start();
			outputGobbler.start();
			p.waitFor();
			LOG.info("GIZA++ word to tag Ok!");

			try {
				Thread.sleep(1000); 
				} catch(InterruptedException ex) {
				Thread.currentThread().interrupt();
			}
			
			// from tag to word
			p = rn.exec(giza_path+File.separator +"GIZA++ -S bookTag.vcb -T book.vcb -C bookTag_book.snt -m1 5 -m2 0 -mh 0 -m3 0 -m4 0  -model1dumpfrequency 1",		
										null, modelDir);
			errorGobbler = new StreamGobbler(p.getErrorStream(), "Error");
			outputGobbler = new StreamGobbler(p.getInputStream(), "Output");
			errorGobbler.start();
			outputGobbler.start();
			p.waitFor();
			LOG.info("GIZA++ tag to word Ok!");

		} catch (Exception e) {
			LOG.info("Error exec!");
		}
}

	public static void main(String[] args) throws IOException {
		// new TrainSMT().buildProTable("/home/cxx/smt/sample/train.dat", new
		// File("/home/cxx/smt/sample"));

	}

	class StreamGobbler extends Thread {
		InputStream is;
		String type;

		StreamGobbler(InputStream is, String type) {
			this.is = is;
			this.type = type;
		}

		public void run() {
			try {
				InputStreamReader isr = new InputStreamReader(is);
				BufferedReader br = new BufferedReader(isr);
				String line = null;
				while ((line = br.readLine()) != null) {
					LOG.info(type + ">" + line);
				}
			} catch (IOException ioe) {
				ioe.printStackTrace();
			}
		}
	}
}