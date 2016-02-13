package org.thunlp.language.english;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;
import java.util.regex.Pattern;

public class Stopwords {
  private static Pattern stopwordRE = null;
  private static Pattern stopwordsRemovalRE = null;
  private static Set<String> stopwords = null;
  private static Logger LOG = Logger.getAnonymousLogger();
  
  static {
    // Initialize stopword list and pattern.
    stopwords = new HashSet<String>();
    String patternStr = loadStopwordsAsPattern(stopwords);
    stopwordRE = Pattern.compile(patternStr,
        Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
    stopwordsRemovalRE = Pattern.compile(" " + patternStr + " ",
        Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
  }
  
  public static boolean isStopword(String word) {
    return stopwords.contains(word);
  }
  
  public static String removeAllStopwords(String content) {
    return stopwordsRemovalRE.matcher(content).replaceAll(" ");
  }
  
  private static String loadStopwordsAsPattern(Set<String> wordset) {
    InputStream input = null;
    if (System.getProperties().containsKey("wordsegment.stopwords.en.file")) {
      try {
        input = new FileInputStream(
            System.getProperty("wordsegment.stopwords.en.file"));
      } catch (FileNotFoundException e) {
        e.printStackTrace();
        input = null;
      }
    }

    if (input == null) {
      LOG.warning("Property 'wordsegment.stopwords.en.file' is not valid, " +
      "will use default word list instead.");
      input = Stopwords.class.getClassLoader()
      .getResourceAsStream("org/thunlp/language/english/stopwords.en.txt");
    }
    StringBuilder sb = new StringBuilder();
    boolean first = true;
    try {
      BufferedReader reader = 
        new BufferedReader(new InputStreamReader(input, "UTF-8"));
      String line;
      while ((line = reader.readLine()) != null) {
        wordset.add(line);
        if (!first)
          sb.append("|");
        sb.append(line.trim());
        first = false;
      }
      reader.close();
    } catch (UnsupportedEncodingException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (IOException e) {
      // e.printStackTrace();
      LOG.warning("Cannot load stopwords, ignore stopwords.");
    }
    
    return sb.toString();
  }
}
