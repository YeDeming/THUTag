package org.thunlp.tagsuggest.contentbase;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Map.Entry;
import java.util.logging.Logger;

import org.apache.lucene.analysis.WhitespaceAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.MultiFieldQueryParser;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TopDocs;
import org.thunlp.misc.Counter;
import org.thunlp.misc.WeightString;
import org.thunlp.tagsuggest.common.LegacyFeatureExtractor;
import org.thunlp.tagsuggest.common.Post;
import org.thunlp.tagsuggest.common.TagSuggest;

public class KnnTagSuggest implements TagSuggest {
  private static Logger LOG = Logger.getAnonymousLogger();
  private IndexSearcher docsSearcher = null;
  private QueryParser queryParser = null;
  private LegacyFeatureExtractor extractor = new LegacyFeatureExtractor();
  private Properties config = new Properties();
  private static List<WeightString> EMPTY_SUGGESTION =  new LinkedList<WeightString>();
  
  private int k = 1;
  private int numKeywords = 10;
  
  @Override
  public void feedback(Post p) {
    // Do nothing.
  }

  @Override
  public void loadModel(String modelPath) throws IOException {
    docsSearcher =  new IndexSearcher((new File(modelPath, "docs")).getAbsolutePath());
    String [] fields = {"doc_id", "content", "user_id", "tag"};
    queryParser = new MultiFieldQueryParser(fields, new WhitespaceAnalyzer());
  }


  @Override
  public void setConfig(Properties config) {
    this.config = config;
    this.k = Integer.parseInt(config.getProperty("k", "1"));
    this.numKeywords = Integer.parseInt(config.getProperty("keywords", "10"));
  }

  @Override
  public List<WeightString> suggest(Post p, StringBuilder explain) {
    // We first extract TF*IDF weighted keywords from post p. Then we use these
    // keywords to form a query to Lucene index. Finally, we collect the tags in 
    // relevant documents as the suggestion.
    
    String content = p.getTitle() + " " + p.getContent();
    content = extractor.clean(content);
    List<WeightString> keywords = extractKeywords(content);
    Query q;
    try {
    	q = makeQueryFromKeywords(keywords, numKeywords);
    } catch (ParseException e1) {
      // TODO Auto-generated catch block
      LOG.warning("Cannot make query from " + p.getId());
      return EMPTY_SUGGESTION;
    }
    TopDocs topDocs;
    try {
      topDocs = docsSearcher.search(q, null, k);
    } catch (IOException e1) {
      LOG.warning("IOException when search for " + p.getId());
      return EMPTY_SUGGESTION;
    }

    // Collect tags.
    Map<String, Double> tags = new Hashtable<String, Double>();
    for (int i = 0; i < topDocs.scoreDocs.length; i++) {
      int resultId = topDocs.scoreDocs[i].doc;
      double score = topDocs.scoreDocs[i].score;
      Document doc;
      try {
        doc = docsSearcher.doc(resultId);
      } catch (CorruptIndexException e1) {
        LOG.warning("Corrupted index when searching for " + p.getId());
        return EMPTY_SUGGESTION;
      } catch (IOException e1) {
        LOG.warning("IOException when looking up doc " + p.getId());
        return EMPTY_SUGGESTION;
      }
      String [] docTags = doc.get("tags").split(" ");
      for (String tagStr : docTags) {
        Double weight = tags.get(tagStr);
        if (weight == null) {
          weight = 0.0;
        }
        tags.put(tagStr, weight + score);
      }
    }

    // Weight tags.
    List<WeightString> suggested = new ArrayList<WeightString>();
    for (Entry<String, Double> e : tags.entrySet()) {
      suggested.add(new WeightString(e.getKey(), e.getValue()));
    }
    Collections.sort(suggested, new Comparator<WeightString>() {

      @Override
      public int compare(WeightString o1, WeightString o2) {
        return Double.compare(o2.weight, o1.weight);
      }
      
    });
    
    return suggested;
  }

  public Query makeQueryFromKeywords(List<WeightString> keywords, int n)
  throws ParseException {
    StringBuilder queryString = new StringBuilder();
    for (int i = 0; i < n && i < keywords.size(); i++) {
      if (i > 0)
        queryString.append(' ');
      queryString.append(keywords.get(i).text);
      queryString.append('^');
      queryString.append(String.format("%.2f",
          Math.log(keywords.get(i).weight + 1)));
    }
    if (queryString.length() == 0)
      queryString.append("a");
    Query q = queryParser.parse(queryString.toString());
    return q;
  }


  public List<WeightString> extractKeywords(String content) {
    String [] words = extractor.getWords(content);
    Counter<String> termFreq = new Counter<String>();
    for (String word : words) {
      termFreq.inc(word, 1);
    }
    double maxDocs = 100000;
    try {
      maxDocs = (double) docsSearcher.maxDoc();
    } catch (IOException e1) {
      LOG.warning("Cannot query the total number of docs.");
      e1.printStackTrace();
    }
    Iterator<Entry<String, Long>> iter = termFreq.iterator();
    List<WeightString> keywords = new ArrayList<WeightString>();
    while (iter.hasNext()) {
      Entry<String, Long> e = iter.next();
      double tf = (double) e.getValue() / (double) words.length;
      double df = 1;
      try {
        df = docsSearcher.docFreq(new Term("content", e.getKey()));
      } catch (IOException e1) {
        LOG.warning("Cannot query document frequency for " + e.getKey());
        e1.printStackTrace();
      }
      double idf = 0.0;
      if (df > 0.0) 
        idf = maxDocs / df;
      else
        idf = 0.0;
      keywords.add(new WeightString(e.getKey(), tf * idf));
    }
    
    Collections.sort(keywords, new Comparator<WeightString>() {
      public int compare(WeightString o1, WeightString o2) {
        return Double.compare(o2.weight, o1.weight);
      }
    });
    // LOG.info("Keywords:" + StringUtil.join(keywords, ","));
    return keywords;
  }
  
}
