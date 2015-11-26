package org.thunlp.tagsuggest.dataset;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import org.thunlp.io.JsonUtil;
import org.thunlp.io.RecordReader;
import org.thunlp.io.RecordWriter;
import org.thunlp.misc.Flags;
import org.thunlp.misc.StringUtil;
import org.thunlp.tagsuggest.common.FeatureExtractor;
import org.thunlp.tagsuggest.common.Post;
import org.thunlp.tagsuggest.common.SimpleExtractor;
import org.thunlp.text.Lexicon;
import org.thunlp.tool.GenericTool;

public class RemoveLowFreq implements GenericTool {
  private static Logger LOG = Logger.getAnonymousLogger();
  JsonUtil J = new JsonUtil();
  private int minFreq = 2;

  @Override
  public void run(String[] args) throws Exception {
    Flags flags = new Flags();
    flags.add("input");
    flags.add("output");
    flags.addWithDefaultValue("min_freq", "2");
    flags.parseAndCheck(args);

    Lexicon wordDict = new Lexicon();
    Lexicon tagDict = new Lexicon();
    minFreq = flags.getInt("min_freq");
    
    buildLexicon(flags.getString("input"), wordDict, tagDict);
    LOG.info("lexicon built. " 
        + wordDict.getSize() + " / " + tagDict.getSize());
    removeLowFreq(
        flags.getString("input"), flags.getString("output"),
        wordDict, tagDict);
  }
  
  private void removeLowFreq(
      String input, String output,
      Lexicon wordDict, Lexicon tagDict) throws IOException {
    FeatureExtractor extractor = new SimpleExtractor(); 
    // Load all docs.
    RecordReader reader = new RecordReader(input);
    RecordWriter writer = new RecordWriter(output);
    List<String> filtered = new LinkedList<String>();
    while (reader.next()) {
      Post p = J.fromJson(reader.value(), Post.class);
      String [] words = extractor.extract(p);
      filtered.clear();
      for (String w : words) {
        if (wordDict.getWord(w) != null)
          filtered.add(w);
      }
      p.setTitle(null);
      p.setContent(StringUtil.join(filtered, " "));
      filtered.clear();
      for (String t : p.getTags()) {
        if (tagDict.getWord(t) != null)
          filtered.add(t);
      }
      p.getTags().clear();
      for (String t :filtered) {
        p.getTags().add(t);
      }
      writer.add(reader.key(), J.toJson(p));
      if (reader.numRead() % 1000 == 0)
        LOG.info("  " + reader.numRead());
    }
    reader.close();
    writer.close();
  }

  private void buildLexicon(String path, Lexicon wordDict, Lexicon tagDict)
  throws IOException {
    FeatureExtractor extractor = new SimpleExtractor(); 
    // Load all docs.
    RecordReader reader = new RecordReader(path);
    while (reader.next()) {
      Post p = J.fromJson(reader.value(), Post.class);
      String [] words = extractor.extract(p);
      wordDict.addDocument(words);
      String [] tags = p.getTags().toArray(new String[p.getTags().size()]);
      tagDict.addDocument(tags);
      if (reader.numRead() % 1000 == 0)
        LOG.info("  " + reader.numRead());
    }
    reader.close();
    wordDict = wordDict.removeLowDfWords(minFreq);
    wordDict.setLock(true);
    tagDict = tagDict.removeLowDfWords(minFreq);
    tagDict.setLock(true);
  }

}
