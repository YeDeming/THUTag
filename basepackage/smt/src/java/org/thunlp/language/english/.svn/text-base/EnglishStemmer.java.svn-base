package org.thunlp.language.english;

import org.tartarus.snowball.ext.englishStemmer;

/**
 * This is just a proxy to Snowball Porter stemmer.
 * @author sixiance
 *
 */
public class EnglishStemmer {
  private static englishStemmer stemmer = new englishStemmer(); 
  
  private EnglishStemmer() {}
  
  synchronized public static String stem(String word) {
    stemmer.setCurrent(word);
    stemmer.stem();
    return stemmer.getCurrent();
  }
}
