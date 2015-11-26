package org.thunlp.tagsuggest.evaluation;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.Map.Entry;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import org.apache.hadoop.mapred.RecordWriter;
import org.thunlp.io.JsonUtil;
import org.thunlp.io.RecordReader;
import org.thunlp.io.TextFileWriter;
import org.thunlp.language.chinese.LangUtils;
import org.thunlp.misc.Flags;
import org.thunlp.misc.WeightString;
import org.thunlp.tagsuggest.common.ConfigIO;
import org.thunlp.tagsuggest.common.DoubanPost;
import org.thunlp.tagsuggest.common.GenerativeTagSuggest;
import org.thunlp.tagsuggest.common.MyTag;
import org.thunlp.tagsuggest.common.Post;
import org.thunlp.tagsuggest.common.TagSuggest;
import org.thunlp.tagsuggest.common.WordFeatureExtractor;
import org.thunlp.text.Lexicon;
import org.thunlp.tool.GenericTool;

import com.sun.mail.handlers.image_gif;

import edu.stanford.nlp.util.StringUtils;

/**
 * Compute P/R/F1 at different number of tags for a tag suggester.
 * 
 * @author sixiance
 * 
 */
public class EvaluatorByActual implements GenericTool {
	private static Logger LOG = Logger.getAnonymousLogger();
	private JsonUtil J = new JsonUtil();
	private Properties config = null;
	private double minLog = -10;
	private Set<String> tagblacklist = new HashSet<String>();
	private List<Double> likelihoods = new LinkedList<Double>();

	public EvaluatorByActual() {
		// For GenericTool interface.
	}

	public EvaluatorByActual(Properties config) {
		this.config = config;

		String blacklist = config.getProperty("tagblacklist", "");
		if (blacklist.length() > 0) {
			String[] tags = blacklist.split(",");
			LOG.info("Found tag blacklist: " + StringUtils.join(tags, "/"));
			tagblacklist.addAll(Arrays.asList(tags));
		}
	}

	public static class Result {
		double[] p;
		double[] r;
		double[] f1;
		int n;
		double loglikelihood;
		double perplexity;
		double numTags;

		public Result(int atN) {
			p = new double[atN];
			r = new double[atN];
			f1 = new double[atN];
			n = 0;
			loglikelihood = 0;
			perplexity = 0;
			numTags = 0;
		}
	}
	
	public static class Result2 {
		double p;
		double r;
		double f1;
		int n;
		public Result2(int atN) {
			p = 0.0;
			r = 0.0;
			f1 = 0.0;
			n = 0;
		}
	}

	@Override
	public void run(String[] args) throws Exception {
		Flags flags = new Flags();
		flags.add("input", "test data");
		flags.add("output", "evaluation report");
		flags.add("suggester", "suggester class name");
		flags.add("model_path", "model path for suggester's loadModel");
		flags.add("config", "config string");
		flags.add("at_n", "evaluate p/r/f1 at i<=n.");
		flags.parseAndCheck(args);

		config = ConfigIO.configFromString(flags.getString("config"));
		int fold = Integer.parseInt(config.getProperty("fold", "-1"));
		File output = new File(flags.getString("output"));
		int atN = flags.getInt("at_n");

		setMinLog(Double.parseDouble(config.getProperty("minlog", "-10")));
		String suggesterClassName = flags.getString("suggester");
		if (!suggesterClassName.startsWith("org.thunlp.tagsuggest.")) {
			suggesterClassName = "org.thunlp.tagsuggest." + suggesterClassName;
		}
		TagSuggest ts = (TagSuggest) Class.forName(suggesterClassName)
				.newInstance();
		ts.setConfig(config);
		ts.loadModel(flags.getString("model_path"));

		Result result = evaluateSuggester(flags.getString("input"), ts, atN,
				fold);
		
		writeReport(result, output);
	}

	public Result evaluateSuggester(String input, TagSuggest ts, int atN,
			int fold) throws IOException {
		RecordReader reader = new RecordReader(input);
		Result result = new Result(atN);
		int n = 0;
		long duration = 0l;

		BufferedWriter out = new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream(input+fold),
				"UTF-8"));
		
		if (config.getProperty("isSample", "true").equals("true")) {
			Lexicon wordlex = new Lexicon();
			Lexicon taglex = new Lexicon();
			WordFeatureExtractor.buildLexicons(input, wordlex, taglex, config);
			int minTagFreq = Integer.parseInt(config.getProperty("mintagfreq",
					"1"));
			taglex = taglex.removeLowDfWords(minTagFreq);

			// Evaluation.
			Set<String> normedTags = new HashSet<String>();
			Pattern spaceRE = Pattern.compile(" +");
			while (reader.next()) {
				DoubanPost p = J.fromJson(reader.value(), DoubanPost.class);
				if (fold >= 0) {
					if (!p.getExtras().equals(Integer.toString(fold)))
						continue;
				}
				// p.getTags().removeAll(tagblacklist);
				if (p.getDoubanTags().size() == 0)
					continue;
				long start = System.currentTimeMillis();
				List<WeightString> tags = ts.suggest(p, null);
				duration += System.currentTimeMillis() - start;
				normedTags.clear();
				for (Entry<String, Integer> e : p.getDoubanTags().entrySet()) {
					String tag = e.getKey();
			
					if (taglex.getWord(tag) == null)
						continue; // Skip low freq tags.
					normedTags.add(tag);
				
				}
				if (normedTags.size() == 0)
					continue; // Skip documents with no valid tags.

				collectScore(tags, normedTags, result);
				

				if(fold == 3){
					// output for Demo
					MyTag myTag = new MyTag();
					myTag.setTitle(p.getTitle());
					myTag.setContent(p.getContent());
					myTag.setAnswer(normedTags);
			
					if(tags.size() < 10) myTag.setSuggestTags(tags);
					else myTag.setSuggestTags(tags.subList(0, 10));
					out.write(J.toJson(myTag));
					out.newLine();
					out.flush();
				}
				
				if (n % 100 == 0) {
					LOG.info("evaluated " + n);
					LOG.info("  @5 p:" + (result.p[4] / result.n) + " r: "
							+ (result.r[4] / result.n));
					LOG.info("  Perplexity:"
							+ Math.pow(2.0, -result.loglikelihood
									/ result.numTags));
				}
				n++;
			}
			out.close();
			reader.close();
			LOG.info("suggester speed:" + duration + "/" + n + "="
					+ (duration / (double) n) + ", "
					+ (duration / (double) n / 1000.0) + "ms.");
			// Normalize the result.
			for (int i = 0; i < atN; i++) {
				result.p[i] /= result.n;
				result.r[i] /= result.n;
				result.f1[i] /= result.n;
			}
			result.loglikelihood /= result.numTags;
			result.perplexity = Math.pow(2.0, -result.loglikelihood);
			return result;
		} else {
			// Load the word frequency info, count it if not exist.
			Lexicon wordlex = new Lexicon();
			Lexicon taglex = new Lexicon();
			WordFeatureExtractor.buildLexicons(input, wordlex, taglex, config);
			int minTagFreq = Integer.parseInt(config.getProperty("mintagfreq",
					"1"));
			taglex = taglex.removeLowDfWords(minTagFreq);

			// Check if this suggester can provide likelihood.
			boolean useLikelihood = (ts instanceof GenerativeTagSuggest);
			GenerativeTagSuggest gts = null;
			if (useLikelihood)
				gts = (GenerativeTagSuggest) ts;

			// Evaluation.
			Set<String> normedTags = new HashSet<String>();
			while (reader.next()) {
				Post p = J.fromJson(reader.value(), Post.class);
				if (fold >= 0) {
					if (!p.getExtras().equals(Integer.toString(fold)))
						continue;
				}
				p.getTags().removeAll(tagblacklist);
				if (p.getTags().size() == 0)
					continue;
				long start = System.currentTimeMillis();
				List<WeightString> tags = ts.suggest(p, null);
				duration += System.currentTimeMillis() - start;
				normedTags.clear();
				for (String tag : p.getTags()) {
					if (taglex.getWord(tag) == null)
						continue; // Skip low freq tags.
					normedTags.add(tag.toLowerCase());
				}
				if (normedTags.size() == 0)
					continue; // Skip documents with no valid tags.

				collectScore(tags, normedTags, result);

				if(fold == 3){
					// output for Demo
					MyTag myTag = new MyTag();
					myTag.setTitle(p.getTitle());
					myTag.setContent(p.getContent());
			
					myTag.setAnswer(normedTags);
					if(tags.size() < 10) myTag.setSuggestTags(tags);
					else myTag.setSuggestTags(tags.subList(0, 10));
					out.write(J.toJson(myTag));
					out.newLine();
					out.flush();
				}
				

				if (useLikelihood) {
					likelihoods.clear();
					gts.likelihood(p, likelihoods);
					for (Double l : likelihoods) {
						result.loglikelihood += takeSafeLog(l);
						result.numTags++;
					}
				}

				if (n % 100 == 0) {
					LOG.info("evaluated " + n);
					LOG.info("  @5 p:" + (result.p[4] / result.n) + " r: "
							+ (result.r[4] / result.n));
					LOG.info("  Perplexity:"
							+ Math.pow(2.0, -result.loglikelihood
									/ result.numTags));
				}
				n++;
			}
			reader.close();
			LOG.info("suggester speed:" + duration + "/" + n + "="
					+ (duration / (double) n) + ", "
					+ (duration / (double) n / 1000.0) + "ms.");
			// Normalize the result.
			for (int i = 0; i < atN; i++) {
				result.p[i] /= result.n;
				result.r[i] /= result.n;
				result.f1[i] /= result.n;
			}
			result.loglikelihood /= result.numTags;
			result.perplexity = Math.pow(2.0, -result.loglikelihood);
			return result;
		}
	}
	
	public void collectScoreByNature(List<WeightString> suggested,
			Collection<String> real, Result2 scores) {
		int count = 0;
		for(int i = 0; i < suggested.size(); i++){
			if (real.contains(suggested.get(i).text.toLowerCase())) {
				count ++;
			}
		}
		double p = (double)count / (double)suggested.size();
		double r = (double)count / (double)real.size();
		double f1;
		if (p == 0 || r == 0)
			f1 = 0;
		else
			f1 = 2 * p * r / (p + r);
		scores.p += p;
		scores.r += r;
		scores.f1 += f1;
		scores.n ++;
	}
	
	private void writeReport2(Result2 result, File output) throws IOException {
		StringBuilder sb = new StringBuilder();
		sb.append(result.p+" "+result.r+" "+result.f1);
		TextFileWriter.writeToFile(sb.toString(), output, "UTF-8");
	}

	public void collectScore(List<WeightString> suggested,
			Collection<String> real, Result scores) {
		int[] correct = new int[scores.p.length];
		for (int i = 0; i < correct.length; i++) {
			if (i >= suggested.size()) {
				correct[i] = (i == 0) ? 0 : (correct[i - 1]);
			} else {
				if (real.contains(suggested.get(i).text.toLowerCase())) {
					correct[i] = (i == 0) ? 1 : (correct[i - 1] + 1);
				} else {
					correct[i] = (i == 0) ? 0 : (correct[i - 1]);
				}
			}
		}
		// modified by cxx
		// compute P/R/F1.
		if(suggested.size() < 10 && suggested.size() != 0){
			for (int i = 0; i < suggested.size(); i++) {
				double p = (double) correct[i] / (double) (i + 1);
				double r = (double) correct[i] / (double) real.size();
				double f1;
				if (p == 0 || r == 0)
					f1 = 0;
				else
					f1 = 2 * p * r / (p + r);
	
				scores.p[i] += p;
				scores.r[i] += r;
				scores.f1[i] += f1;
			}
			for (int i = suggested.size(); i < correct.length; i++) {
				double p = (double) correct[i] / (double) suggested.size();
				double r = (double) correct[i] / (double) real.size();
				double f1;
				if (p == 0 || r == 0)
					f1 = 0;
				else
					f1 = 2 * p * r / (p + r);
	
				scores.p[i] += p;
				scores.r[i] += r;
				scores.f1[i] += f1;
			}
		}
		else{
			for (int i = 0; i < correct.length; i++) {
				double p = (double) correct[i] / (double) (i + 1);
				double r = (double) correct[i] / (double) real.size();
				double f1;
				if (p == 0 || r == 0)
					f1 = 0;
				else
					f1 = 2 * p * r / (p + r);

				scores.p[i] += p;
				scores.r[i] += r;
				scores.f1[i] += f1;
			}
		}
		scores.n++;
		
	}

	private double takeSafeLog(double v) {
		if (v == 0)
			return minLog;
		return Math.log(v) / Math.log(2.0);
	}

	private void writeReport(Result result, File output) throws IOException {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < result.p.length; i++) {
			sb.append(i + 1);
			sb.append(" ");
			sb.append(result.p[i]);
			sb.append(" ");
			sb.append(result.r[i]);
			sb.append(" ");
			sb.append(result.f1[i]);
			sb.append(" ");
			sb.append(result.loglikelihood);
			sb.append(" ");
			sb.append(result.perplexity);
			sb.append("\n");
		}
		TextFileWriter.writeToFile(sb.toString(), output, "UTF-8");
	}

	public void setMinLog(double v) {
		minLog = v;
	}
}
