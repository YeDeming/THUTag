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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
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
import org.thunlp.misc.WeightString;
import org.thunlp.tagsuggest.common.RtuMain;
import org.thunlp.tagsuggest.common.ConfigIO;
import org.thunlp.tagsuggest.common.Post;
import org.thunlp.tagsuggest.common.ModelTrainer;
import org.thunlp.tagsuggest.common.TagFilter;
import org.thunlp.tagsuggest.common.WordFeatureExtractor;
import org.thunlp.tagsuggest.train.TrainWAM.StreamGobbler;
import org.thunlp.text.Lexicon;
import org.thunlp.tool.GenericTool;

public class TrainWAMsample implements GenericTool, ModelTrainer {
	private static Logger LOG = Logger.getAnonymousLogger();
	private Properties config = null;
	private String fold = "";
	private String giza_path;
	private RtuMain jar_path = new RtuMain();
	
	JsonUtil J = new JsonUtil();
	WordFeatureExtractor extractor = null;
	
	@Override
	public void run(String[] args) throws Exception {
		LOG.info("All start");
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

		try {
			if (!modelDir.exists()) {
				modelDir.mkdir();
			}

			Lexicon wordlex = new Lexicon();
			Lexicon taglex = new Lexicon();
			LOG.info("Start building");

			WordFeatureExtractor.buildLexicons(input, wordlex, taglex, config);

			extractor = new WordFeatureExtractor(config);
			extractor.setWordLexicon(wordlex);
			extractor.setTagLexicon(taglex);
			
			RecordReader reader = new RecordReader(input);

			// information
			Lexicon localWordlex = new Lexicon();
			
			File wordLexFile = new File(modelDir.getAbsolutePath() + "/wordlex");

			if (wordLexFile.exists()) {
				LOG.info("Use cached lexicons");
				localWordlex.loadFromFile(wordLexFile);
			} else {
				while (reader.next()) {
					Post p = J.fromJson(reader.value(), Post.class);
					if (fold.length() > 0 && p.getExtras().equals(fold)) {
						continue;
					}
					String[] features = extractor.extract(p);
					if (features.length <= 0) {
						continue;
					}

					localWordlex.addDocument(features);

					if (reader.numRead() % 1000 == 0)
					{
						LOG.info(modelDir.getAbsolutePath()
								+ " building lexicons: " + reader.numRead());
					}
				}
				localWordlex.saveToFile(wordLexFile);
				reader.close();
				reader = new RecordReader(input);
			}

			BufferedWriter outTitle = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(modelDir.getAbsolutePath() 
							+ "/book"), "UTF-8"));

			BufferedWriter outContentbag = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(modelDir.getAbsolutePath()
							+ "/bookTag"), "UTF-8"));

			// the second time :
			while (reader.next()) {
				Post p = J.fromJson(reader.value(), Post.class);
				if (fold.length() > 0 && p.getExtras().equals(fold)) {
					continue;
				}
				
				String title = p.getTitle();
				String content = p.getContent();
				
				title = extractor.clean(title);
			    String[] titlewords = extractor.getWords(title);
			    
			    String[] words = extractor.extract(p);
			    
				if (titlewords.length <= 0 || words.length <= 0) {
					continue;
				}

				// sample the words
			    List<WeightString> wordList = new ArrayList<WeightString>();
				Counter<String> termFreq = new Counter<String>();
				
				for (String word : words) {
					termFreq.inc(word, 1);
				}
				Iterator<Entry<String, Long>> iter = termFreq.iterator();

				while (iter.hasNext()) {
					Entry<String, Long> e = iter.next();
					String word = e.getKey();

					double tf = ((double) e.getValue())
							/ ((double) words.length);
					double idf = Math.log(((double) localWordlex.getNumDocs())
							/ ((double) localWordlex.getWord(word)
									.getDocumentFrequency()));
					
					wordList.add(new WeightString(e.getKey(), tf * idf));
				}
				
			    Collections.sort(wordList, WeightString.REVERSE_COMPARATOR);

				int wordnum = (titlewords.length > 100) ? 100 : titlewords.length;
				if (wordList.size() < wordnum)  wordnum = wordList.size();

				//print the title
				for (int i = 0; i < wordnum; i++) {
					String word = titlewords[i];
					if (i == 0) {
						outTitle.write(word);
					} else {
						outTitle.write(" " + word);
					}
				}
				outTitle.newLine();
				outTitle.flush();

				// print the content bag

				for (int i = 0; i < wordnum; i++) {
					String word = wordList.get(i).text;

					if (i == 0) {
						outContentbag.write(word);
					} else {
						outContentbag.write(" " + word);
					}
				}
				outContentbag.newLine();
				outContentbag.flush();

			}
			
			reader.close();
			outTitle.close();
			outContentbag.close();

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
		TrainWAMsample Test = new TrainWAMsample();
		Test.config = ConfigIO.configFromString("num_tags=10;norm=all_log;isSample=true;model=/home/meepo/test/sample/book.model;size=70000;fromDouban=true;minwordfreq=10;mintagfreq=10;selfTrans=0.2;commonLimit=2");
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