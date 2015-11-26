package org.thunlp.tagsuggest.evaluation;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.Map.Entry;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import org.thunlp.io.JsonUtil;
import org.thunlp.io.RecordReader;
import org.thunlp.io.TextFileWriter;
import org.thunlp.misc.Flags;
import org.thunlp.misc.WeightString;
import org.thunlp.tagsuggest.common.ConfigIO;
import org.thunlp.tagsuggest.common.DoubanPost;
import org.thunlp.tagsuggest.common.GenerativeTagSuggest;
import org.thunlp.tagsuggest.common.KeywordPost;
import org.thunlp.tagsuggest.common.MyKeyword2;
import org.thunlp.tagsuggest.common.MyTag;
import org.thunlp.tagsuggest.common.Post;
import org.thunlp.tagsuggest.common.TagFilter;
import org.thunlp.tagsuggest.common.TagSuggest;
import org.thunlp.tagsuggest.common.WordFeatureExtractor;
import org.thunlp.text.Lexicon;
import org.thunlp.tool.GenericTool;

import edu.stanford.nlp.util.StringUtils;

/**
 * Compute P/R/F1 at different number of tags for a tag suggester.
 * 
 * @author sixiance
 * 
 */
public class Evaluator implements GenericTool {
	private static Logger LOG = Logger.getAnonymousLogger();
	private JsonUtil J = new JsonUtil();
	private Properties config = null;
	private double minLog = -10;
	private Set<String> tagblacklist = new HashSet<String>();
	private List<Double> likelihoods = new LinkedList<Double>();

	public Evaluator() {
		// For GenericTool interface.
	}

	public Evaluator(Properties config) {
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
		
		long num;
		long suggestnum;
		long answernum;

		long[] record;
		
		public Result(int atN) {
			p = new double[atN];
			r = new double[atN];
			f1 = new double[atN];
			n = 0;
			loglikelihood = 0;
			perplexity = 0;
			numTags = 0;
			
			record = new long[atN];
			num = 0;
			suggestnum = 0;
			answernum = 0;
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

		String outputFile = config.getProperty("outputFile",input);
		BufferedWriter out = new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream(outputFile+fold),
				"UTF-8"));
		
		boolean outputF = config.getProperty("outputF", "false").equals("true");
		BufferedWriter outF = new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream(outputFile+"F"+fold),"UTF-8"));
		List<WeightString> fmeasure = new ArrayList<WeightString>();
		
		boolean outputWrong = config.getProperty("outputWrong", "false").equals("true");
		BufferedWriter outWrong = new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream(outputFile+fold+"wrong"),
				"UTF-8"));
		
		
		
		String dataType = config.getProperty("dataType", "Post");
		if(dataType.equals("DoubanPost")){
				Lexicon wordlex = new Lexicon();
				Lexicon taglex = new Lexicon();
				WordFeatureExtractor.buildLexicons(input, wordlex, taglex, config);
				int minTagFreq = Integer.parseInt(config.getProperty("mintagfreq",
						"1"));
				
				TagFilter tagFilter = new TagFilter(config, taglex);
			
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
					
					tagFilter.filterMapWithNorm(p.getDoubanTags(), normedTags);
					
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
		} 
		else if (dataType.equals("Post")){
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
	
				TagFilter tagFilter = new TagFilter(config, taglex);
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
			
					tagFilter.filterWithNorm(p.getTags(), normedTags);
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
		else if (dataType.equals("KeywordPost")){
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

			
			TagFilter tagFilter = new TagFilter(config, taglex);
			// Evaluation.
			Set<String> normedTags = new HashSet<String>();
			while (reader.next()) {
				KeywordPost p = J.fromJson(reader.value(), KeywordPost.class);
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
				
				tagFilter.filterWithNorm(p.getTags(), normedTags);
				if (normedTags.size() == 0)
					continue; // Skip documents with no valid tags.
				
				collectScore(tags, normedTags, result);

				if(outputF){
					int count = 0;
					for(int i = 0; i < tags.size() && i < 10; i++){
						if (normedTags.contains(tags.get(i).text.toLowerCase())) {
							count ++;
						}
					}
					double pp = 0.0;
					if(tags.size() < 10){
						pp = (tags.size() == 0) ? 0.0 : (double)count / (double)tags.size() ;
					}
					else{
						pp = (double)count / 10.0;
					}
					double r = (normedTags.size() == 0) ? 0.0 : (double)count / (double)normedTags.size() ;
					double f = 0.0;
					if (pp == 0 || r == 0)
						f = 0;
					else
						f = 2 * pp * r / (pp + r);
					fmeasure.add(new WeightString(p.getId(), f));
					
					
				}
		
				MyKeyword2 myKeyword = new MyKeyword2();
				myKeyword.setTitle(p.getTitle());
				myKeyword.setSummary(p.getSummary());
				myKeyword.setContent(p.getContent());
				myKeyword.setId(p.getId());
				myKeyword.setAnswer(normedTags);
		
				for(int i = 0 ; i < tags.size() && i < 10; i ++){
					myKeyword.getSuggestTags().add(tags.get(i).text);
				}
				out.write(J.toJson(myKeyword));
				out.newLine();
				out.flush();
				
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
			out.close();
			outWrong.close();
			
			if(outputF){
				Collections.sort(fmeasure, new Comparator<WeightString>() {
					@Override
					public int compare(WeightString o1, WeightString o2) {
						return Double.compare(o2.weight, o1.weight);
					}
				});
				for(int i = 0; i < fmeasure.size(); i ++){
					outF.write(fmeasure.get(i).text + " " + fmeasure.get(i).weight);
					outF.newLine();
					outF.flush();
				}
				outF.close();
			}
			
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
		return result;
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
			scores.record[i] += correct[i];
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
		
		int count = 0;
		for(int i = 0; i < suggested.size() && i < 10; i++){
			if (real.contains(suggested.get(i).text.toLowerCase())) {
				count ++;
			}
		}
		scores.num += count;
		scores.suggestnum += suggested.size();
		scores.answernum += real.size();
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
