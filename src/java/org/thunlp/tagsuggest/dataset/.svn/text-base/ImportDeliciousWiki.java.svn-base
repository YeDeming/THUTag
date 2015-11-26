package org.thunlp.tagsuggest.dataset;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.thunlp.html.HtmlReformatter;
import org.thunlp.io.JsonUtil;
import org.thunlp.io.RecordWriter;
import org.thunlp.io.TextFileReader;
import org.thunlp.language.chinese.LangUtils;
import org.thunlp.misc.Flags;
import org.thunlp.tagsuggest.common.Post;
import org.thunlp.tool.GenericTool;

public class ImportDeliciousWiki implements GenericTool {
  private static Logger LOG = Logger.getAnonymousLogger();

  enum States {START, IN_ARTICLE, IN_TAG}
  Pattern hashRE = Pattern.compile("<hash>(.*?)</hash>");
  Pattern nameRE = Pattern.compile("<name>(.*?)</name>");

  @Override
  public void run(String[] args) throws Exception {
    Flags flags = new Flags();
    flags.add("tagxml");
    flags.add("pagedir");
    flags.add("output");
    flags.parseAndCheck(args);

    TextFileReader reader = new TextFileReader(flags.getString("tagxml"));
    RecordWriter writer = new RecordWriter(flags.getString("output"));
    JsonUtil ju = new JsonUtil();
    States state = States.START;
    String line = null;
    int n = 0;
    Post p = null;
    while ((line = reader.readLine()) != null) {
      if (state == States.START) {
        if (line.contains("<article>")) {
          state = States.IN_ARTICLE;
          p = new Post();
        }
      } else if (state == States.IN_ARTICLE) {
        if (line.contains("<tag>")) {
          state = States.IN_TAG;
        } else if (line.contains("<hash>")) {
          Matcher m = hashRE.matcher(line);
          if (m.find()) {
            String id = m.group(1);
            p.setId(id);
            try {
              String content = TextFileReader.readAll(
                  flags.getString("pagedir") + File.separator + id);
              content = HtmlReformatter.getPlainText(content);
              p.setContent(content);
            } catch (IOException e) {
              LOG.info("Cannot read " + id);
              p.setContent("");
            }
          } else {
            throw new RuntimeException("<hash>:" + line);
          }
        } else if (line.contains("</article>")) {
          if (p.getContent().length() > 0) {
            writer.add(p.getId(), ju.toJson(p));
            p = null;
            n++;
            if (n % 100 == 0) {
              LOG.info("Converted " + n);
            }
          }
          state = States.START;
        }
      } else if (state == States.IN_TAG) {
        if (line.contains("<name>")) {
          Matcher m = nameRE.matcher(line);
          if (m.find()) {
            String tag = m.group(1);
            tag = tag.toLowerCase();
            tag = LangUtils.removePunctuationMarks(tag);
            if (tag.length() > 0)
              p.getTags().add(tag);
          } else {
            throw new RuntimeException("<name>:" + line);
          }
        } else if (line.contains("</tag>")) {
          state = States.IN_ARTICLE;
        }
      }

    }
    reader.close();
    writer.close();
  }

}
