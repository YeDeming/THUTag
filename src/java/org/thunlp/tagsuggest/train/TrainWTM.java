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

import org.thunlp.io.JsonUtil;
import org.thunlp.io.RecordReader;
import org.thunlp.language.chinese.LangUtils;
import org.thunlp.misc.Counter;
import org.thunlp.misc.Flags;
import org.thunlp.tagsuggest.common.ConfigIO;
import org.thunlp.tagsuggest.common.DoubanPost;
import org.thunlp.tagsuggest.common.ModelTrainer;
import org.thunlp.tagsuggest.common.RtuMain;
import org.thunlp.tagsuggest.common.TagFilter;
import org.thunlp.tagsuggest.common.WordFeatureExtractor;
import org.thunlp.tagsuggest.train.TrainWAMsample.StreamGobbler;
import org.thunlp.text.Lexicon;
import org.thunlp.tool.GenericTool;

public class TrainWTM implements GenericTool, ModelTrainer {
	private static Logger LOG = Logger.getAnonymousLogger();
	private Properties config = null;
	private String fold = "";
	private String giza_path = null;
	private RtuMain jar_path = new RtuMain();

	JsonUtil J = new JsonUtil();
	WordFeatureExtractor fe = null;
	TagFilter tagFilter = null;

	@Override
	public void run(String[] args) throws Exception {
		// TODO Auto-generated method stub
		
		LOG.info("All start");
		
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

		try {
			if (!modelDir.exists()) {
				modelDir.mkdir();
			}

			Lexicon wordlex = new Lexicon();
			Lexicon taglex = new Lexicon();
			LOG.info("Start building");

			WordFeatureExtractor.buildLexicons(input, wordlex, taglex, config);

			fe = new WordFeatureExtractor(config);
			fe.setWordLexicon(wordlex);
			fe.setTagLexicon(taglex);
			tagFilter = new TagFilter(config, taglex);
			Set<String> filtered = new HashSet<String>();
			HashSet<String> tagSet = new HashSet<String>();

			RecordReader reader = new RecordReader(input);
			// the first time : create wordlex and taglex to store the tf and df
			// information.
			Lexicon localWordlex = new Lexicon();
			Lexicon localTaglex = new Lexicon();
			File wordLexFile = new File(modelDir.getAbsolutePath() + "/wordlex");
			File tagLexFile = new File(modelDir.getAbsolutePath() + "/taglex");
			if (wordLexFile.exists() && tagLexFile.exists()) {
				LOG.info("Use cached lexicons");
				localWordlex.loadFromFile(wordLexFile);
				localTaglex.loadFromFile(tagLexFile);
			} else {
				LOG.info("Create lexicons");
				while (reader.next()) {
					DoubanPost p = J.fromJson(reader.value(), DoubanPost.class);
					if (fold.length() > 0 && p.getExtras().equals(fold)) {
						continue;
					}
					String[] features = fe.extract(p);
					if (features.length <= 0) {
						continue;
					}
					tagFilter.filterMapWithNorm(p.getDoubanTags(), filtered);
					if (filtered == null) {
						continue;
					}
					localWordlex.addDocument(features);
					localTaglex.addDocument(filtered
							.toArray(new String[filtered.size()]));

					if (reader.numRead() % 1000 == 0)
					{
						LOG.info(modelDir.getAbsolutePath()
								+ " building lexicons: " + reader.numRead());
					//	break;
					}
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

			TagFilter localTagFilter = new TagFilter(config, localTaglex);
			Set<String> localFiltered = new HashSet<String>();

			Random random = new Random();
			Pattern spaceRE = Pattern.compile(" +");
			// the second time :
			while (reader.next()) {
				DoubanPost p = J.fromJson(reader.value(), DoubanPost.class);
				if (fold.length() > 0 && p.getExtras().equals(fold)) {
					continue;
				}
				localTagFilter.filterMapWithNorm(p.getDoubanTags(),
						localFiltered);
				double total = 0.0;
				Vector<Double> tagTfidf = new Vector<Double>();
				Vector<String> tagList = new Vector<String>();
				// calculate the tfidf of tag
				for (Entry<String, Integer> e : p.getDoubanTags().entrySet()) {
					String tag = e.getKey();
					tag = LangUtils.removePunctuationMarks(tag);
					tag = spaceRE.matcher(tag).replaceAll("");
					tag = LangUtils.T2S(tag);
					tag = tag.toLowerCase();
					if (localFiltered.contains(tag)) {
						double idf = Math.log(((double) localTaglex
								.getNumDocs())
								/ ((double) localTaglex.getWord(tag)
										.getDocumentFrequency()));
						tagTfidf.add(((double) e.getValue()) * idf);
						total += ((double) e.getValue()) * idf;
						tagList.add(tag);
						tagSet.add(tag);
					}
				}
				if (total == 0.0)
					continue;
				Vector<Double> tagProb = new Vector<Double>();
				for (int i = 0; i < tagTfidf.size(); i++) {
					tagProb.add(tagTfidf.elementAt(i) / total);
				}
				String[] words = fe.extract(p);
				if (words.length <= 0) {
					continue;
				}

				
				int wordnum = (words.length > 100) ? 100 : words.length;
				int wordCount = Integer.parseInt(config.getProperty("wordCount", "1"));
				int tagCount = Integer.parseInt(config.getProperty("tagCount", "1"));
				int tagLength = wordnum * tagCount / wordCount;
				if(tagLength <= 0){
					continue;
				}
				if(tagLength > 100){
					tagLength = 100;
				}

				// sample the words
				Vector<Double> wordTfidf = new Vector<Double>();
				Vector<String> wordList = new Vector<String>();
				Counter<String> termFreq = new Counter<String>();
				for (String word : words) {
					termFreq.inc(word, 1);
				}
				Iterator<Entry<String, Long>> iter = termFreq.iterator();
				double totalTfidf = 0.0;
				while (iter.hasNext()) {
					Entry<String, Long> e = iter.next();
					String word = e.getKey();
					wordList.add(word);
					double tf = ((double) e.getValue())
							/ ((double) words.length);
					double idf = Math.log(((double) localWordlex.getNumDocs())
							/ ((double) localWordlex.getWord(word)
									.getDocumentFrequency()));
					wordTfidf.add(tf * idf);
					totalTfidf += tf * idf;
				}
				Vector<Double> wordProb = new Vector<Double>();
				for (int i = 0; i < wordTfidf.size(); i++) {
					wordProb.add(wordTfidf.elementAt(i) / totalTfidf);
				}

				for (int i = 0; i < words.length && i < 100; i++) {
					double select = random.nextDouble();
					double sum = 0.0;
					int j = 0;
					for (j = 0; j < wordProb.size(); j++) {
						sum += wordProb.elementAt(j);
						if (sum >= select)
							break;
					}
					String word = wordList.elementAt(j);
					if (i == 0) {
						out.write(word);
					} else {
						out.write(" " + word);
					}
				}
				out.newLine();
				out.flush();

				// sample the tags
				for (int i = 0; i < tagLength; i++) {
					double num = random.nextDouble();
					double sum = 0.0;
					int j = 0;
					for (j = 0; j < tagProb.size(); j++) {
						sum += tagProb.elementAt(j);
						if (sum >= num)
							break;
					}
					if (i == 0) {
						outTag.write(tagList.elementAt(j));
					} else {
						outTag.write(" " + tagList.elementAt(j));
					}
				}
				outTag.newLine();
				outTag.flush();
			/*	if (reader.numRead() % 1000 == 0)
				{
						break;
				}*/
			}

			for(String tag:tagSet){
				out.write(tag);
				out.newLine();
				out.flush();
				outTag.write(tag);
				outTag.newLine();
				outTag.flush();
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
		TrainWTM Test = new TrainWTM();
		Test.config = ConfigIO.configFromString("num_tags=10;norm=all_log;isSample=true;model=/home/meepo/test/sample/book.model;size=70000;minwordfreq=10;mintagfreq=10;selfTrans=0.2;commonLimit=2");
		Test.buildProTable("/home/meepo/test/sample/bookPost70000.dat", new
			File("/home/meepo/test/sample"));
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