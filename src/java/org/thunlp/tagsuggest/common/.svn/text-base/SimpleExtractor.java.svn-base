package org.thunlp.tagsuggest.common;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import org.thunlp.language.chinese.ForwardMaxWordSegment;
import org.thunlp.language.chinese.LangUtils;
import org.thunlp.language.chinese.WordSegment;

public class SimpleExtractor implements FeatureExtractor {
  WordSegment ws = null;
  boolean useContent = true;
  
  public SimpleExtractor() {
    try {
      ws = new ForwardMaxWordSegment();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }
  
  public SimpleExtractor(Properties config) {
    this();
    if (!config.getProperty("usecontent", "true").equals("true")) {
      useContent = false;
    }
  }
  
  @Override
  public String[] extract(Post p) {
    String content = p.getTitle();
    if (content == null)
      content = "";
    if (useContent) {
      content += " " + p.getContent();
    }
    content = LangUtils.removePunctuationMarks(content);
    content = LangUtils.removeLineEnds(content);
    content = LangUtils.removeExtraSpaces(content);
    content = content.toLowerCase();
    String [] words = ws.segment(content);
    List<String> filtered = new LinkedList<String>();
    for (String word : words) {
      if (word.length() <= 1)
        continue;
      if (org.thunlp.language.english.Stopwords.isStopword(word))
        continue;
      if (org.thunlp.language.chinese.Stopwords.isStopword(word))
        continue;
      filtered.add(word);
    }
    return filtered.toArray(new String[filtered.size()]);
  }

}
