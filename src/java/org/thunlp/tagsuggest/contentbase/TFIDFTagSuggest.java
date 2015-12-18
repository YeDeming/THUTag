package org.thunlp.tagsuggest.contentbase;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.Map.Entry;
import java.util.logging.Logger;

import org.thunlp.misc.Counter;
import org.thunlp.misc.WeightString;
import org.thunlp.tagsuggest.common.ConfigIO;
import org.thunlp.tagsuggest.common.FeatureExtractor;
import org.thunlp.tagsuggest.common.Post;
import org.thunlp.tagsuggest.common.KeywordPost;
import org.thunlp.tagsuggest.common.TagSuggest;
import org.thunlp.tagsuggest.common.WordFeatureExtractor;
import org.thunlp.tagsuggest.train.TrainTFIDF;
import org.thunlp.text.Lexicon;
import org.thunlp.text.Lexicon.Word;

public class TFIDFTagSuggest implements TagSuggest {
private static Logger LOG = Logger.getAnonymousLogger();

  Lexicon lex = null;
  private WordFeatureExtractor extractor = new WordFeatureExtractor();
  private Properties config = null;
  private int numTags = 10;
  
  @Override
  public void feedback(Post p) {

  }

  @Override
  public void loadModel(String modelPath) throws IOException {
	lex = new Lexicon();
	String input = modelPath + "/wordlex";
	File cachedWordLexFile = new File(input);
	if (cachedWordLexFile.exists()) {
		LOG.info("Use cached lexicons");
	
		lex.loadFromFile(cachedWordLexFile);
	} 

  }

  @Override
  public void setConfig(Properties config) {
    extractor = new WordFeatureExtractor(config);
    numTags = Integer.parseInt(config.getProperty("num_tags", "5"));
    this.config = config;
  }

  @Override
  public List<WeightString> suggest(Post p, StringBuilder explain) {
	  
    String [] features = extractor.extractKeyword((KeywordPost) p, true, true,true);
    
    Counter<String> featureSet = new Counter<String>();

    for (String feature : features) {
      if (feature.length() <= 1)
        continue;
      featureSet.inc(feature, 1);
    }
    List<WeightString> tags = new ArrayList<WeightString>();
    for (Entry<String, Long> e : featureSet) {
      Word w = lex.getWord(e.getKey());
      double df = 1;
      if (w != null) 
        df = w.getDocumentFrequency();
      double idf = (lex.getNumDocs() + 1.0) / df;
      double tf = (double)e.getValue() / (double)featureSet.total();
      double score = tf * Math.log(idf);
      if (explain != null) {
        explain.append("<div>tf:" + tf + " df:" + df + 
            " idf:" + idf + " final:" + score + " = " + e.getKey() + "</div>");
      }
      tags.add(new WeightString(e.getKey(), score));
    }
    Collections.sort(tags, WeightString.REVERSE_COMPARATOR);
    if (tags.size() > numTags) {
      tags = tags.subList(0, numTags);
    }
    return tags;
  }
	

}
