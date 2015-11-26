package org.thunlp.tagsuggest.contentbase;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Map.Entry;
import java.util.logging.Logger;

import org.thunlp.io.RecordReader;
import org.thunlp.misc.Counter;
import org.thunlp.misc.WeightString;
import org.thunlp.tagsuggest.common.FeatureExtractor;
import org.thunlp.tagsuggest.common.GenerativeTagSuggest;
import org.thunlp.tagsuggest.common.Post;
import org.thunlp.tagsuggest.common.WordFeatureExtractor;

public class NaiveBayesTagSuggest implements GenerativeTagSuggest {
  private static Logger LOG = Logger.getAnonymousLogger();
  private Map<String, Counter<String>> counts;
  private FeatureExtractor fe = new WordFeatureExtractor();
  private Counter<String> nw = new Counter<String>();
  private Counter<String> tagfreq = new Counter<String>();
  private double alpha = 0.01;

  @Override
  public void feedback(Post p) {
    // Not supported.
  }

  @Override
  public void loadModel(String modelPath) throws IOException {
    counts = new Hashtable<String, Counter<String>>();
    RecordReader reader = new RecordReader(modelPath);
    while (reader.next()) {
      String [] cols = reader.value().split(" ");
      if (cols.length < 3 || cols.length % 2 == 0) {
        LOG.warning("wrong number of columns " + cols.length 
            + ":" + reader.value());
        continue;
      }
      Counter<String> count = counts.get(cols[0]);
      if (count == null) {
        count = new Counter<String>();
        counts.put(cols[0], count);
      }
      for (int i = 1; i < cols.length; i += 2) {
        int n = Integer.parseInt(cols[i+1]);
        count.inc(cols[i], n);
        tagfreq.inc(cols[i], n);
        nw.inc(cols[0], n);
      }
    }
    reader.close();
    LOG.info("load " + nw.size() + " words and " +
        tagfreq.size() + " tags");

  }

  @Override
  public void setConfig(Properties config) {
    fe = new WordFeatureExtractor(config);
  }

  @Override
  public List<WeightString> suggest(Post p, StringBuilder explain) {
    String [] words = fe.extract(p);
    Map<String, Double> ptds = new Hashtable<String, Double>();
    Counter<String> nwd = new Counter<String>();
    for (String w : words) {
      nwd.inc(w, 1);
    }
    for (Entry<String, Long> w : nwd) {
      Counter<String> count = counts.get(w.getKey());
      if (count == null)
        continue;
      for (Entry<String, Long> ntw : count) {
        double ptw = ntw.getValue() / (double) nw.get(w.getKey());
        double pwd = w.getValue() / (double) words.length;
        Double ptd = ptds.get(ntw.getKey());
        if (ptd == null)
          ptd = 0.0;
        ptds.put(ntw.getKey(), ptd + ptw * pwd);
      }
    }

    List<WeightString> tags = new ArrayList<WeightString>();
    for (Entry<String, Double> e : ptds.entrySet()) {
      tags.add(new WeightString(e.getKey(), e.getValue()));
    }
    Collections.sort(tags, new Comparator<WeightString>() {

      @Override
      public int compare(WeightString o1, WeightString o2) {
        return Double.compare(o2.weight, o1.weight);
      }

    }); 
    return tags;
  }

  @Override
  public void likelihood(Post p, List<Double> likelihoods) {
    Counter<String> nwd = new Counter<String>();
    String [] doc = fe.extract(p);
    for (String w : doc) {
      nwd.inc(w, 1);
    }
    for (String tag : p.getTags()) {
      double likelihood = 0;
      for (Entry<String, Long> w : nwd) {
        Counter<String> count = counts.get(w.getKey());
        double ntw = 0;
        if (count != null)
          ntw = count.get(tag);
        double ptw = (ntw + alpha) / 
          (double) (nw.get(w.getKey()) + tagfreq.size() * alpha);
        double pwd = (w.getValue() + alpha) /
          ((double) doc.length + nw.size() * alpha);
        likelihood += ptw * pwd;
      }
      likelihoods.add(likelihood);
    }  
  }
}
