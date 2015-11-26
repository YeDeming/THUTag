package org.thunlp.tagsuggest.train;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Logger;

import org.apache.lucene.analysis.WhitespaceAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;
import org.thunlp.io.JsonUtil;
import org.thunlp.io.RecordReader;
import org.thunlp.misc.Flags;
import org.thunlp.misc.StringUtil;
import org.thunlp.tagsuggest.common.ConfigIO;
import org.thunlp.tagsuggest.common.FeatureExtractor;
import org.thunlp.tagsuggest.common.KeywordPost;
import org.thunlp.tagsuggest.common.ModelTrainer;
import org.thunlp.tagsuggest.common.Post;
import org.thunlp.tagsuggest.common.TagFilter;
import org.thunlp.tagsuggest.common.WordFeatureExtractor;
import org.thunlp.text.Lexicon;
import org.thunlp.text.Lexicon.Word;
import org.thunlp.tool.GenericTool;

public class TrainExpandRank implements GenericTool, ModelTrainer {
  private static Logger LOG = Logger.getAnonymousLogger();
  private WordFeatureExtractor extractor;
  private TagFilter tagFilter = null;
  private Properties config = null;
  private Lexicon wordlex = null;
  private Lexicon taglex = null;
  private String fold = "";
  private int minTagFreq = 1;
  private Set<String> filtered = new HashSet<String>();
  
  @Override
  public void run(String[] args) throws Exception {
    Flags flags = new Flags();
    flags.add("input");
    flags.add("output");
    flags.add("config");
    flags.parseAndCheck(args);
    
    Properties config = ConfigIO.configFromString(flags.getString("config"));
    
    train(flags.getString("input"), flags.getString("output"), config);
  }

  @Override
  public void train(String input, String modelPath, Properties config)
    throws IOException {
    this.config = config;
    this.fold = config.getProperty("fold", "");
    minTagFreq = Integer.parseInt(config.getProperty("mintagfreq", "1"));
    wordlex = new Lexicon();
    taglex = new Lexicon();
    WordFeatureExtractor.buildLexicons(
        input, wordlex, taglex, config);
    WordFeatureExtractor e = new WordFeatureExtractor(config);
    e.setTagLexicon(taglex);
    e.setWordLexicon(wordlex);
    extractor = e;
    tagFilter = new TagFilter(config, taglex);
    buildIndexes(input, new File(modelPath));
  }
  
  
  public void buildIndexes(String input, File modelDir)
  throws IOException {
    if (!modelDir.exists()) {
      modelDir.mkdir();
    }
    
    Set<String> whitelist = new HashSet<String>();
    Set<String> blacklist = new HashSet<String>();
    
    if (config.getProperty("whitelist", "").length() > 0) {
      whitelist.addAll(
          Arrays.asList(config.getProperty("whitelist", "").split(",")));
    }
    if (config.getProperty("blacklist", "").length() > 0) {
      blacklist.addAll(
          Arrays.asList(config.getProperty("blacklist", "").split(",")));
    }
    
    WhitespaceAnalyzer analyzer = new WhitespaceAnalyzer();
    JsonUtil J = new JsonUtil();    
    IndexWriter docsIndex =
      new IndexWriter(new File(modelDir, "docs"), analyzer);
    
    RecordReader reader = new RecordReader(input);
    while (reader.next()) {
      //Post p = J.fromJson(reader.value(), Post.class);
    	KeywordPost p = J.fromJson(reader.value(), KeywordPost.class);
      if (blacklist.contains(p.getUserId())) {
        continue;
      }
      if (whitelist.size() > 0 && !whitelist.contains(p.getUserId())) {
        continue;
      }
      if (fold.length() > 0 && p.getExtras().equals(fold)) {
        continue;
      }
      Document contentDoc = makeContentDoc(p);
      docsIndex.addDocument(contentDoc);
      if (reader.numRead() % 5000 == 0) {
        LOG.info("Added " + reader.numRead() + " documents.");
      }
    }
    reader.close();
    
    LOG.info("Optimizing docs index...");
    docsIndex.optimize();
    docsIndex.close();
  }
  
  public Document makeContentDoc(KeywordPost p) {
    //String [] words = extractor.extract(p);
	  String[] words = extractor.extractKeyword(p, true, true, true);
    String docString = StringUtil.join(words, " ");
    Document d = new Document();
    d.add(new Field("doc_id", p.getId(),
        Field.Store.YES, Field.Index.UN_TOKENIZED));
    d.add(new Field("content", docString,
        Field.Store.YES, Field.Index.TOKENIZED));
    tagFilter.filter(p.getTags(), filtered);
    d.add(new Field("tags", StringUtil.join(filtered, " "),
        Field.Store.YES, Field.Index.TOKENIZED));
    d.add(new Field("user_id", p.getUserId(),
        Field.Store.YES, Field.Index.UN_TOKENIZED));
    return d;
  }
  
}
