package org.thunlp.language.english;

import org.tartarus.snowball.ext.porterStemmer;

/**
 * This is just a proxy to Snowball Porter stemmer.
 * @author sixiance
 *
 */
public class PorterStemmer {
  private static porterStemmer stemmer = new porterStemmer(); 
  
  private PorterStemmer() {}
  
  synchronized public static String stem(String word) {
    stemmer.setCurrent(word);
    stemmer.stem();
    return stemmer.getCurrent();
  }
}
