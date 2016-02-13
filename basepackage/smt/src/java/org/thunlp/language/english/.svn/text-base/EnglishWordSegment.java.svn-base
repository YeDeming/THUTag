package org.thunlp.language.english;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.logging.Logger;

import org.tartarus.snowball.ext.porterStemmer;
import org.thunlp.language.chinese.WordSegment;

public class EnglishWordSegment implements WordSegment {
  private static Logger LOG = Logger.getAnonymousLogger();
  private static Set<String> stopwordsSet = null;
  private porterStemmer stemmer = null;
  
  static {
    stopwordsSet = loadStopwords();
  }
  
  public EnglishWordSegment() {
    stemmer = new porterStemmer();
  }

  private static Set<String> loadStopwords() {
    Set<String> stopwords = new HashSet<String>();
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
      input = EnglishWordSegment.class.getClassLoader()
      .getResourceAsStream("org/thunlp/language/english/stopwords.en.txt");
    }
    
    try {
      BufferedReader reader = 
        new BufferedReader(new InputStreamReader(input, "UTF-8"));
      String line;
      while ((line = reader.readLine()) != null) {
        stopwords.add(line.trim());
      }
      reader.close();
    } catch (UnsupportedEncodingException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (IOException e) {
      // e.printStackTrace();
      LOG.warning("Cannot load stopwords, ignore stopwords.");
    }
    
    return stopwords;
  }

  public boolean outputPosTag() {
    return false;
  }

  /**
   * 使用porterStemmer和停用词表来切分处理英文
   */
  public String[] segment(String text) {
    String [] tokens = text.split("[^0-9\\p{L}]+");
    LinkedList<String> results = new LinkedList<String>(); 
    for ( int i = 0 ; i < tokens.length ; i++ ) {
      if ( ! stopwordsSet.contains( tokens[i] ) ) {
        stemmer.setCurrent(tokens[i]);
        if ( stemmer.stem() ) {
          results.add(stemmer.getCurrent());
        } else {
          results.add(tokens[i]);
        }
      }
    }
    return results.toArray(new String[results.size()]);
  }

}
