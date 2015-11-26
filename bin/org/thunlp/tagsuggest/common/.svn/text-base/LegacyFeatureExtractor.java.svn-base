package org.thunlp.tagsuggest.common;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import org.thunlp.html.HtmlReformatter;
import org.thunlp.io.TextFileReader;
import org.thunlp.language.chinese.ForwardMaxWordSegment;
import org.thunlp.language.chinese.LangUtils;
import org.thunlp.language.chinese.WordSegment;
import org.thunlp.language.english.PorterStemmer;
import org.thunlp.language.english.Stopwords;
import org.thunlp.learning.lda.LdaModel;

public class LegacyFeatureExtractor implements FeatureExtractor {
  private static Logger LOG = Logger.getAnonymousLogger();
  private static Pattern pureDigitRE = Pattern.compile("[0-9][0-9.]+");
  private static Pattern englishStopWords = 
    Pattern.compile("is|the|a|an|that|this|these|those|they|I|you|me|he|she|" +
        "which|what|why|how|when|too|either|or|and|not|but|are|were|be|to|as",
        Pattern.CASE_INSENSITIVE);
  private static Pattern bracesRE = Pattern.compile("[{}]+");
  private WordSegment ws = null;
  private Set<String> stopTags;
  private Set<String> phrases = new HashSet<String>();
  private boolean useTopicModel = false;
  private boolean usePhraseTable = false;
  private LdaModel topicModel = null;
  private double topicThreshold = 0.0;
  
  public LegacyFeatureExtractor() {
    try {
      ws = new ForwardMaxWordSegment();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    stopTags = new HashSet<String>();
    stopTags.add("imported");
    stopTags.add("public");
    stopTags.add("system:imported");
    stopTags.add("nn");
    stopTags.add("system:unfiled");
    stopTags.add("jabrefnokeywordassigned");
    stopTags.add("wismasys0809");
    stopTags.add("bibteximport");
  }
  
  public LegacyFeatureExtractor(Properties config) {
    this();
    useTopicModel = config.getProperty("topicmodel", "").length() > 0;
    usePhraseTable = config.getProperty("phrasetable", "").length() > 0;
    topicThreshold = 
      Double.parseDouble(config.getProperty("topicthreshold", "0.6"));
    
    if (useTopicModel) {
      topicModel = new LdaModel();
      try {
        LOG.info("Loading topic model from " + config.getProperty("topicmodel"));
        topicModel.loadModel(config.getProperty("topicmodel"));
        LOG.info(" done, " + topicModel.getNumTopics() + " topics.");
      } catch (IOException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }
    if (usePhraseTable) {
      try {
        loadPhrases(new File(config.getProperty("phrasetable")));
      } catch (IOException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }
  }

  public String clean(String text) {
    String cleaned = HtmlReformatter.getPlainText(text);
    cleaned = cleaned.replaceAll("\\\\[\"']", "");
    cleaned = cleaned.replaceAll("\\{\\\\ss\\}", "s");
    cleaned = cleaned.replaceAll("Proc\\.", "Proceedings");
    cleaned = cleaned.replaceAll("Int\\.|Intl\\.", "International");
    cleaned = bracesRE.matcher(cleaned).replaceAll("");
    cleaned = cleaned.replaceAll("(?<=[a-zA-Z0-9])-(?=[a-zA-Z0-9])", "");
    cleaned = LangUtils.removePunctuationMarks(cleaned);
    cleaned = LangUtils.mapFullWidthLetterToHalfWidth(cleaned);
    cleaned = LangUtils.mapFullWidthNumberToHalfWidth(cleaned);
    cleaned = LangUtils.T2S(cleaned);
    cleaned = LangUtils.removeLineEnds(cleaned);
    cleaned = LangUtils.removeExtraSpaces(cleaned);
    return cleaned.trim();
  }

  public String [] normalizeAndStem(String [] words) {
    List<String> tokens = new LinkedList<String>();
    for (String word : words) {
      if (Stopwords.isStopword(word)) {
        continue;
      }
      if (word.length() <= 1)
        continue;
      String lower = word.toLowerCase();
      String stemmed = PorterStemmer.stem(lower);
      if (stemmed.length() > 0)
        tokens.add(stemmed);
    }
    return tokens.toArray(new String[0]);
  }

  public String [] normalize(String [] words) {
    List<String> tokens = new LinkedList<String>();
    for (String word : words) {
      if (Stopwords.isStopword(word)) {
        continue;
      }
      if (word.length() <= 1)
        continue;
      String lower = word.toLowerCase();
      tokens.add(lower);
    }
    return tokens.toArray(new String[0]);
  }

  public String [] expand(String [] words) {
    List<String> tokens = new LinkedList<String>();
    for (String word : words) {
      if (englishStopWords.matcher(word).matches())
        continue;
      if (word.length() == 0)
        continue;
      tokens.add(word);
      String lower = word.toLowerCase();
      if (!lower.equals(word)) {
        tokens.add(lower);
      }

      String stemmed = PorterStemmer.stem(lower);
      if (!stemmed.equals(lower) && stemmed.length() > 0) {
        tokens.add(stemmed);
      }

      if (pureDigitRE.matcher(word).matches()) {
        tokens.add("@NUM");
      }
    }
    return tokens.toArray(new String[0]);
  }

  public String [] extractFeatures(String text) {
    text = clean(text);
    String [] words = ws.segment(text);
    words = normalize(words);
    return words;
  }

  public void loadPhrases(File phraseFile) throws IOException {
    LOG.info("Loading phrase file");
    TextFileReader r = new TextFileReader(phraseFile);
    String line;
    while ((line = r.readLine()) != null) {
      int n = 0;
      int sep = 0;
      for (int i = 0; i < line.length(); i++)
        if (line.charAt(i) == ' ') {
          n++;
          sep = i;
        }
      if (n == 1) {
        if (!Stopwords.isStopword(line.substring(0, sep)) &&
            !Stopwords.isStopword(line.substring(sep+1))) {
          phrases.add(line);
        }
      }
    }
    r.close();

    LOG.info("Total " + phrases.size() + " phrases");
  }

  public String expandPhrases(String text) {
    if (phrases.size() == 0)
      return text;
    StringBuffer sb = new StringBuffer(text);
    int pos = 0;
    int thisword = 0;
    int lastword = -1;
    while (pos < sb.length()) {
      if (sb.charAt(pos) != ' ' && pos < sb.length() - 1) 
        pos++;
      else {
        if (thisword > 0 && lastword >= 0) {
          String bigram = sb.substring(lastword, pos);
          if (phrases.contains(bigram)) {
            sb.setCharAt(thisword - 1, '-');
          }
        }
        lastword = thisword;
        thisword = pos + 1;
        pos++;
      }
    }
    return sb.toString(); 
  }
  
  public String [] getWords(String content) {
    return ws.segment(content);
  }
  
  public void getTopTopics(String [] words, List<String> features) {
    double [] dist = topicModel.inference(words);
    double max = 0.0;
    for (double d : dist) {
      if (d > max)
        max = d;
    }
    for (int i = 0; i < dist.length; i++) {
      if (dist[i] > max * topicThreshold) {
        features.add("&" + i);
      }
    }
  }

  public String [] extractFeatures(Post td) {
    List<String> features = new LinkedList<String>();
    String text = clean(td.getTitle() + " " + td.getContent());
    if (usePhraseTable) {
      text = text.toLowerCase();
      text = expandPhrases(text);
    }

    String [] words = ws.segment(text);
    words = normalize(words);
    features.addAll(Arrays.asList(words));
    features.add("#" + td.getResourceKey());
    features.add("@" + td.getUserId());
    if (useTopicModel) {
      getTopTopics(words, features);
    }
    return features.toArray(new String[0]);
  }

  public void cleanTags(Set<String> tags, Set<String> out) {
    out.clear();
    for (String tag : tags) {
      if (stopTags.contains(tag))
        continue;
      out.add(tag.replaceAll("[^0-9\\p{L}]+", "").toLowerCase());
    }
  }

  @Override
  public String [] extract(Post p) {
    return extractFeatures(p);
  }
}
