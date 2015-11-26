package org.thunlp.tagsuggest.contentbase;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Logger;

import org.thunlp.misc.WeightString;
import org.thunlp.tagsuggest.common.FeatureExtractor;
import org.thunlp.tagsuggest.common.GenerativeTagSuggest;
import org.thunlp.tagsuggest.common.KeywordPost;
import org.thunlp.tagsuggest.common.Post;
import org.thunlp.tagsuggest.common.TagSuggest;
import org.thunlp.tagsuggest.common.WordFeatureExtractor;
import org.thunlp.tagsuggest.contentbase.NoiseTagLdaModel.Document;

/**
 * This class use the Tag-LDA method proposed by Xiance Si.
 * @author sixiance
 *
 */
public class NoiseTagLdaTagSuggest implements TagSuggest, GenerativeTagSuggest {
  private static Logger LOG = Logger.getAnonymousLogger();
  private NoiseTagLdaModel model;
  private Properties config = null;
  private WordFeatureExtractor extractor = new WordFeatureExtractor();
  private int numTags = 10;
  private static String [] EMPTY_TAG_SET = new String[0];
  private static int [] EMPTY_REASON_SET = new int[0];
  private double [] pzd = null;
  private double [] ptz = null;

  @Override
  public void feedback(Post p) {}

  @Override
  public void loadModel(String modelPath) throws IOException {
    FileInputStream input = new FileInputStream(modelPath);
    model = new NoiseTagLdaModel(input);
    input.close();
    LOG.info("Load LDA model of " + model.getNumTopics()
        + " topics and " + model.tags().size() + " tags.");
    pzd = new double[model.getNumTopics()];
    ptz = new double[model.getNumTopics() + 1];
  }

  @Override
  public void setConfig(Properties config) {
    this.config = config;
    extractor = new WordFeatureExtractor(config);
    numTags = Integer.parseInt(config.getProperty("numtags", "10"));
  }

  public double computeLikelihood(double [] ptz, double [] pzd) {
    double ptd = 0;
    for (int i = 0; i < pzd.length; i++) {
      ptd += ptz[i] * pzd[i] * (1 - model.pnoise());
    }
    ptd += ptz[model.noise] * model.pnoise();
    return ptd;
  }

  @Override
  public List<WeightString> suggest(Post p, StringBuilder explain) {
    String [] features = extractor.extract(p);
	//String [] features = extractor.extractKeyword((KeywordPost)p, true, false, containContent);
    Document d = new Document(features, EMPTY_TAG_SET);
    model.inference(d, pzd);
    List<WeightString> results = new ArrayList<WeightString>();
    for (String t : model.tags()) {
      model.ptz(t, ptz);
      results.add(new WeightString(t, computeLikelihood(ptz, pzd)));
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

  @Override
  public void likelihood(Post p, List<Double> likelihoods) {
    String [] words = extractor.extract(p);
    Document d = new Document(
        words, p.getTags().toArray(new String[p.getTags().size()]));
    model.inference(d, pzd);
    for (String tag : p.getTags()) {
      if (model.tags().contains(tag)) {
        model.ptz(tag, ptz);
        likelihoods.add(computeLikelihood(ptz, pzd));
      } else {
        likelihoods.add(0.0);
      }
    }
  }
}
