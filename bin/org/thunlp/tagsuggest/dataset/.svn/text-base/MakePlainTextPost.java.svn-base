package org.thunlp.tagsuggest.dataset;

import java.io.IOException;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.Mapper;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.Reporter;
import org.apache.hadoop.mapred.lib.IdentityReducer;
import org.thunlp.hadoop.MapReduceHelper;
import org.thunlp.html.HtmlReformatter;
import org.thunlp.io.JsonUtil;
import org.thunlp.language.chinese.LangUtils;
import org.thunlp.misc.Flags;
import org.thunlp.tagsuggest.common.Post;
import org.thunlp.tool.GenericTool;

/**
 * Remove all HTML tags, unescape HTML special characters, remove unnecessary
 * spaces and line-ends. Convert traditional Chinese text to simplified. Convert
 * full-width characters to half-width equivalence.
 * @author sixiance
 *
 */
@SuppressWarnings("deprecation")
public class MakePlainTextPost implements GenericTool {
  public static enum Counters {
    NUM_JSON_ERROR
  }

  @Override
  public void run(String[] args) throws Exception {
    Flags flags = new Flags();
    flags.add("input");
    flags.add("output");
    flags.parseAndCheck(args);

    MapReduceHelper.runTextSeqFileMapReduce(
        new JobConf(this.getClass()),
        CleanMapper.class, IdentityReducer.class,
        flags.getString("input"), flags.getString("output"));
  }

  public static class CleanMapper implements Mapper<Text, Text, Text, Text> {
    Text outkey = new Text();
    Text outvalue = new Text();
    JsonUtil J = new JsonUtil();

    public void configure(JobConf job) {
    }

    public void map(Text key, Text value,
        OutputCollector<Text, Text> collector, Reporter r) throws IOException {
      Post p = null;
      try {
        p = J.fromTextAsJson(value, Post.class);
      } catch (IOException e) {
        r.incrCounter(Counters.NUM_JSON_ERROR, 1);
        return;
      }
      p.setTitle(clean(p.getTitle()));
      p.setContent(clean(p.getContent()));
      J.toTextAsJson(p, value);
      collector.collect(key, value);
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

}
