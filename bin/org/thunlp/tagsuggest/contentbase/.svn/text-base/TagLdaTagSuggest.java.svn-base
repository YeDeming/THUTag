package org.thunlp.tagsuggest.contentbase;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Logger;

import org.thunlp.hadooplda.LdaModel;
import org.thunlp.misc.WeightString;
import org.thunlp.tagsuggest.common.FeatureExtractor;
import org.thunlp.tagsuggest.common.GenerativeTagSuggest;
import org.thunlp.tagsuggest.common.Post;
import org.thunlp.tagsuggest.common.TagSuggest;
import org.thunlp.tagsuggest.common.WordFeatureExtractor;

/**
 * This class use the Tag-LDA method proposed by Xiance Si.
 * @author sixiance
 *
 */
public class TagLdaTagSuggest implements TagSuggest, GenerativeTagSuggest {
  private static Logger LOG = Logger.getAnonymousLogger();
  private LdaModel model;
  private Set<String> knownTags = new HashSet<String>();
  private double [] norms = null;
  private Properties config = null;
  private FeatureExtractor extractor = new WordFeatureExtractor();
  private int numTags = 10;
  private int maxNumTags = 3000;

  @Override
  public void feedback(Post p) {}

  @Override
  public void loadModel(String modelPath) throws IOException {
    FileInputStream input = new FileInputStream(modelPath);
    model = new LdaModel(input);
    input.close();
    double [] ptz = new double[model.getNumTopics()];
    norms = new double[model.getNumTopics()];
    Arrays.fill(norms, 0);
    for (String token : model.getAllWords()) {
      if (token.charAt(0) == '_') {
        knownTags.add(token.substring(1));
        model.pwz(token, ptz);
        for (int k = 0; k < model.getNumTopics(); k++)
          norms[k] += ptz[k];
      }
    }
    LOG.info("Load LDA model of " + model.getNumTopics()
        + " topics and " + knownTags.size() + " tags.");
  }

  @Override
  public void setConfig(Properties config) {
    this.config = config;
    extractor = new WordFeatureExtractor(config);
    numTags = Integer.parseInt(config.getProperty("numtags", "10"));
  }

  @Override
  public List<WeightString> suggest(Post p, StringBuilder explain) {
    double [] pzd = new double[model.getNumTopics()];
    double [] ptz = new double[model.getNumTopics()];
    String [] features = extractor.extract(p);
    int [] topics = new int[features.length];
    model.inference(features, topics, pzd);
    List<WeightString> results = new ArrayList<WeightString>();
    for (String t : knownTags) {
      double ptd = 0;
      model.pwz("_" + t, ptz);
      for (int i = 0; i < pzd.length; i++) {
        ptd += ptz[i] / norms[i] * pzd[i];
      }
      results.add(new WeightString(t, ptd));
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
    String [] doc = extractor.extract(p);
    for (String tag : p.getTags()) {
      double likelihood = 0;
      if (knownTags.contains(tag)) {
        double [] pzd = new double[model.getNumTopics()];
        int [] topics = new int[doc.length];
        model.inference(doc, topics, pzd);
        for (int z = 0; z < model.getNumTopics(); z++) {
          likelihood += model.pwz("_" + tag, z) / norms[z] * pzd[z];
        }
      }
      likelihoods.add(likelihood);
    }
  }
}
