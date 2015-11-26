package org.thunlp.tagsuggest.dataset;

import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Logger;

import org.thunlp.html.HtmlReformatter;
import org.thunlp.io.JsonUtil;
import org.thunlp.io.RecordReader;
import org.thunlp.io.RecordWriter;
import org.thunlp.language.chinese.LangUtils;
import org.thunlp.misc.Flags;
import org.thunlp.tagsuggest.common.ConfigIO;
import org.thunlp.tagsuggest.common.Post;
import org.thunlp.tagsuggest.common.TagFilter;
import org.thunlp.tool.GenericTool;

/**
 * Clean Posts, remove HTML tags in the content, perform T2S, full-width to
 * half-width conversion, normalize tags.
 * @author sixiance
 *
 */
public class PostCleaner implements GenericTool {
  private static Logger LOG = Logger.getAnonymousLogger();

  @Override
  public void run(String[] args) throws Exception {
    Flags flags = new Flags();
    flags.add("input");
    flags.add("output");
    flags.add("config");
    flags.parseAndCheck(args);
    
    RecordReader reader = new RecordReader(flags.getString("input"));
    RecordWriter writer = new RecordWriter(flags.getString("output"));
    JsonUtil ju = new JsonUtil();
    Properties config = ConfigIO.configFromString(flags.getString("config"));
    Set<String> tags = new TreeSet<String>();
    TagFilter tagFilter = new TagFilter(config, null);
    int skipped = 0;
    while (reader.next()) {
      Post p = ju.fromJson(reader.value(), Post.class);
      p.setContent(clean(p.getContent()));
      p.setTitle(clean(p.getTitle()));
      tags.clear();
      tagFilter.filterWithNorm(p.getTags(), tags);
      if (tags.size() == 0) {
        skipped++;
        continue;
      }
      p.setTags(tags);
      writer.add(ju.toJson(p));
    }
    LOG.info("Processed " + reader.numRead() + " docs, skipped " + skipped);
    reader.close();
    writer.close();
  }
  
  private String clean(String content) {
    content = HtmlReformatter.getPlainText(content);
    content = LangUtils.mapChineseMarksToAnsi(content);
    content = LangUtils.mapFullWidthLetterToHalfWidth(content);
    content = LangUtils.mapFullWidthNumberToHalfWidth(content);
    content = LangUtils.T2S(content);
    content = LangUtils.removeExtraSpaces(content);
    content = LangUtils.removeEmptyLines(content);
    return content;
  }

}
