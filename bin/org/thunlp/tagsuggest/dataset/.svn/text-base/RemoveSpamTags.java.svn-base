package org.thunlp.tagsuggest.dataset;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.logging.Logger;

import org.thunlp.io.GzipTextFileReader;
import org.thunlp.io.IoUtils;
import org.thunlp.io.JsonUtil;
import org.thunlp.io.TextFileWriter;
import org.thunlp.io.IoUtils.LineMapper;
import org.thunlp.misc.Counter;
import org.thunlp.misc.Flags;
import org.thunlp.misc.StringPair;
import org.thunlp.tagsuggest.common.Post;
import org.thunlp.tool.GenericTool;

/**
 * Remove spam tags. Spam tags are used much more than normal by only one user.
 * We use {n(t, u) / n(u)} / {n(t, d) / n(d)} to indicate the spamness of a tag. 
 * @author sixiance
 *
 */
public class RemoveSpamTags implements GenericTool {
  private static Logger LOG = Logger.getAnonymousLogger();
  
  @Override
  public void run(String[] args) throws Exception {
    Flags flags = new Flags();
    flags.add("input");
    flags.add("output");
    flags.add("threshold");
    flags.add("quality_file");
    flags.parseAndCheck(args);
    
    File input = flags.getFile("input");
    File output = flags.getFile("output");
    File qualityFile = flags.getFile("quality_file");
    
    Map<StringPair, Double> spamrank =
      computeTagQuality(
          input,
          qualityFile);
    
    IoUtils.mapGzipLines(
        input,
        output,
        new RemoveLineMapper(spamrank, flags.getDouble("threshold")),
        1000);    
  }
  
  private Map<StringPair, Double> computeTagQuality(File input, File log) 
  throws IOException{
    Counter<StringPair> userTagCount = new Counter<StringPair>();
    Counter<String> userCount = new Counter<String>();
    Counter<String> tagCount = new Counter<String>();
    JsonUtil J = new JsonUtil();
    int numPosts = 0;
    
    LOG.info("Counting tag/user/post freq.");
    GzipTextFileReader r = new GzipTextFileReader(input);
    String line;
    while ((line = r.readLine()) != null) {
      Post p = J.fromJson(line, Post.class);
      userCount.inc(p.getUserId(), 1);
      for (String tag : p.getTags()) {
        tagCount.inc(tag, 1);
        StringPair sp = new StringPair();
        sp.first = p.getUserId();
        sp.second = tag;
        userTagCount.inc(sp, 1);
      }
      numPosts++;
      if (numPosts % 1000 == 0) {
        LOG.info(numPosts + ".");
      }
    }
    r.close();
    
    LOG.info("Computing spamrank score.");
    TextFileWriter logWriter = new TextFileWriter(log);
    Map<StringPair, Double> quality = new Hashtable<StringPair, Double>();
    for (Entry<StringPair, Long> e : userTagCount) {
      String uid = e.getKey().first;
      String tag = e.getKey().second;
      double uf = (double)e.getValue() / (double)userCount.get(uid);
      double tuf = (double)e.getValue() / (double)tagCount.get(tag);
 
      double q = 2.0 * uf * tuf / (uf + tuf); //Math.max(uf, tuf);
      if ((double)e.getValue() < 2) {
        q = 0;
      }
      if (tuf > 0.9) {
        q = tuf;
      }
      quality.put(e.getKey(), q);
      logWriter.writeLine(uid + "-" + tag + " " + e.getValue() 
          + " " +  uf + " " + tuf + " " + String.format("%.4f", q));
    }
    logWriter.close();
    return quality;
  }
  
  private static class RemoveLineMapper implements LineMapper {
    Map<StringPair, Double> spamrank = null;
    double threshold = 0;
    JsonUtil J = new JsonUtil();

    Set<String> cleanedTags = new HashSet<String>();
    StringPair keypair = new StringPair();
    
    public RemoveLineMapper(
        Map<StringPair, Double> spamrank, double threshold) {
      this.spamrank = spamrank;
      this.threshold = threshold;
    }
    
    @Override
    public String map(String line) {
      Post p = null;
      try {
        p = J.fromJson(line, Post.class);
      } catch (IOException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
      cleanedTags.clear();
      for (String tag : p.getTags()) {
        keypair.first = p.getUserId();
        keypair.second = tag;
        Double q = spamrank.get(keypair);
        if (q == null || q < threshold) {
          cleanedTags.add(tag);
        }
      }
      if (cleanedTags.size() == 0)
        return null;
      p.setTags(cleanedTags);
      try {
        return J.toJson(p);
      } catch (IOException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
        return null;
      }
    }    
  }

}
