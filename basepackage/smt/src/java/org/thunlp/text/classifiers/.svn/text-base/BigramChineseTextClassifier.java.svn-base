package org.thunlp.text.classifiers;

import org.thunlp.language.chinese.BigramWordSegment;
import org.thunlp.language.chinese.WordSegment;

public class BigramChineseTextClassifier extends AbstractTextClassifier {

  public BigramChineseTextClassifier(int nclasses) {
    super(nclasses);
  }
  
  public BigramChineseTextClassifier(int nclasses, WordSegment seg ) {
    super(nclasses, seg);
  }

  @Override
  protected WordSegment initWordSegment() {
    return new BigramWordSegment();
  }
  
}