package org.thunlp.tagsuggest.train;

import java.io.IOException;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Map.Entry;
import java.util.logging.Logger;

import org.thunlp.io.JsonUtil;
import org.thunlp.io.RecordReader;
import org.thunlp.io.RecordWriter;
import org.thunlp.misc.Counter;
import org.thunlp.misc.Flags;
import org.thunlp.misc.SlidingWindowCounter;
import org.thunlp.tagsuggest.common.ConfigIO;
import org.thunlp.tagsuggest.common.ModelTrainer;
import org.thunlp.tagsuggest.common.Post;
import org.thunlp.tagsuggest.common.TagFilter;
import org.thunlp.tagsuggest.common.WordFeatureExtractor;
import org.thunlp.text.Lexicon;
import org.thunlp.tool.GenericTool;

/**
 * Train Naive Bayes model for tagged data. The model file is a plain text file,
 * each line is as follows:
 *   word tag1 count1 tag2 count2 ...
 * @author sixiance
 *
 */
public class TrainNaiveBayes implements GenericTool, ModelTrainer {
  private static Logger LOG = Logger.getAnonymousLogger();
  JsonUtil J = new JsonUtil();
  Lexicon wordlex = new Lexicon();
  Lexicon taglex = new Lexicon();
  TagFilter tagFilter = null;
  
  @Override
  public void run(String[] args) throws Exception {
    Flags flags = new Flags();
    flags.add("input", "training data in Post@json format.");
    flags.add("output", "the model parameters.");
    flags.add("config");
    flags.addWithDefaultValue("fold", "-1", "the fold to use, -1 for all");
    flags.parseAndCheck(args);

    Properties config = ConfigIO.configFromString(flags.getString("config"));
    train(flags.getString("input"), flags.getString("output"), config);
  }


  @Override
  public void train(String inputPath, String modelPath, Properties config)
  throws IOException {
    WordFeatureExtractor.buildLexicons(inputPath, wordlex, taglex, config);
    WordFeatureExtractor fe = new WordFeatureExtractor(config);
    taglex = taglex.removeLowDfWords(
        Integer.parseInt(config.getProperty("mintagfreq", "1")));
    fe.setTagLexicon(taglex);
    fe.setWordLexicon(wordlex);
    tagFilter = new TagFilter(config, taglex);
    Set<String> filtered = new HashSet<String>();
    String fold = config.getProperty("fold", "-1");
    
    RecordReader reader = new RecordReader(inputPath);
    Map<String, Counter<String>> counts =
      new Hashtable<String, Counter<String>>();

    while (reader.next()) {
      Post p = J.fromJson(reader.value(), Post.class);
      if (p.getExtras().equals(fold)) {
        continue;
      }

      String [] features = fe.extract(p);
      for (String f : features) {
        Counter<String> count = counts.get(f);
        if (count == null) {
          count = new SlidingWindowCounter<String>(2, 10000);
          counts.put(f, count);
        }
        tagFilter.filter(p.getTags(), filtered);
        for (String tag : filtered) {
          count.inc(tag, 1);
        }

      }

      if (reader.numRead() % 2000 == 0) {
        System.out.println(reader.numRead() + "    \r");
        System.out.flush();
        // Prune all counters.
        for (Entry<String, Counter<String>> e : counts.entrySet()) {
          e.getValue().size();
        }
      }
    }
    reader.close();

    // Output the model.
    RecordWriter writer = new RecordWriter(modelPath);
    for (Entry<String, Counter<String>> e : counts.entrySet()) {
      if (e.getValue().size() == 0)
        continue;
      StringBuilder sb = new StringBuilder();
      sb.append(e.getKey());
      for (Entry<String, Long> count : e.getValue()) {
        sb.append(" ");
        sb.append(count.getKey());
        sb.append(" ");
        sb.append(count.getValue());
      }
      writer.add(sb.toString());
    }
    writer.close();
    
  }
}
