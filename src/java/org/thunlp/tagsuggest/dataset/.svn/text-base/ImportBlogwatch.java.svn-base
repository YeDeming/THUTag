package org.thunlp.tagsuggest.dataset;

import java.io.IOException;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.Mapper;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.Reporter;
import org.thunlp.hadoop.MapReduceHelper;
import org.thunlp.html.HtmlReformatter;
import org.thunlp.io.JsonUtil;
import org.thunlp.language.chinese.LangUtils;
import org.thunlp.misc.Flags;
import org.thunlp.tagsuggest.common.Post;
import org.thunlp.tool.GenericTool;

/**
 * Convert blogwatch crawled blog posts with tags to tagsuggest Post format.
 * @author sixiance
 *
 */
@SuppressWarnings("deprecation")
public class ImportBlogwatch implements GenericTool {
  public static enum Counter {
    NUM_TAGGED_POSTS, NUM_IO_EXCEPTIONS, NUM_SKIPPED_LONG_POSTS
  }
  @Override
  public void run(String[] args) throws Exception {
    Flags flags = new Flags();
    flags.add("input");
    flags.add("output");
    flags.parseAndCheck(args);

    MapReduceHelper.runTextSeqFileMap(
        new JobConf(this.getClass()),
        ConvertMapper.class,
        flags.getString("input"),
        flags.getString("output"));
  }

  public static class ConvertMapper implements Mapper<Text, Text, Text, Text> {
    Text outkey = new Text();
    Text outvalue = new Text();
    JsonUtil J = new JsonUtil();
    Post p = new Post();

    public void configure(JobConf job) {
    }

    public void map(Text key, Text value,
        OutputCollector<Text, Text> collector, Reporter r) throws IOException {
      if (value.getLength() > 8192) {
        r.incrCounter(Counter.NUM_SKIPPED_LONG_POSTS, 1);
        return;
      }
      WatchPost wp = J.fromTextAsJson(value, WatchPost.class);
      p.getTags().clear();
      p.setId(wp.getUrl());
      if (wp.getTags() == null)
        return;
      String [] tags = wp.getTags().split(" +");
      if (tags.length <= 0)
        return;
      r.incrCounter(Counter.NUM_TAGGED_POSTS, 1);
      for (String tag : tags) {
        p.getTags().add(tag);
      }
      p.setUserId(wp.getFeed());
      p.setExtras("");
      p.setTimestamp(wp.getTimestamp());
      p.setTitle(clean(wp.getTitle()));
      p.setContent(clean(wp.getContent()));
      try {
        J.toTextAsJson(p, outvalue);
        outkey.set(p.getId());
        collector.collect(outkey, outvalue);
      } catch (IOException e) {
        r.incrCounter(Counter.NUM_IO_EXCEPTIONS, 1);
      }
    }

    public void close() {
    }
    
    public String clean(String s) {
      s = HtmlReformatter.getPlainText(s);
      s = LangUtils.T2S(s);
      s = LangUtils.mapChineseMarksToAnsi(s);
      s = LangUtils.mapFullWidthLetterToHalfWidth(s);
      s = LangUtils.mapFullWidthNumberToHalfWidth(s);
      s = LangUtils.removeLineEnds(s);
      s = LangUtils.removeExtraSpaces(s);
      return s;
    }
  }

  public static class WatchPost {
    private String url;
    private String author;
    private String feed;

    /**
     * Seconds from 1970.1.1.
     */
    private long timestamp;
    private String title;
    private String content;

    /**
     * Space-separated tags.
     */
    private String tags;

    public void setUrl(String url) {
      this.url = url;
    }

    public String getUrl() {
      return url;
    }

    public void setAuthor(String author) {
      this.author = author;
    }

    public String getAuthor() {
      return author;
    }

    public void setFeed(String feed) {
      this.feed = feed;
    }

    public String getFeed() {
      return feed;
    }

    public void setTimestamp(long timestamp) {
      this.timestamp = timestamp;
    }

    public long getTimestamp() {
      return timestamp;
    }

    public void setTitle(String title) {
      this.title = title;
    }

    public String getTitle() {
      return title;
    }

    public void setContent(String content) {
      this.content = content;
    }

    public String getContent() {
      return content;
    }

    public void setTags(String tags) {
      this.tags = tags;
    }

    public String getTags() {
      return tags;
    }
  }

}
