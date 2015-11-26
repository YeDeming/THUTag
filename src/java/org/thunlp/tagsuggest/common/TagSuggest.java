package org.thunlp.tagsuggest.common;

import java.io.IOException;
import java.util.List;
import java.util.Properties;

import org.thunlp.misc.WeightString;

/**
 * This interface describes what an abstract tagger does.
 * @author sixiance
 *
 */
public interface TagSuggest {
  public void loadModel(String modelPath) throws IOException;
  public void setConfig(Properties config);
  public List<WeightString> suggest (Post p, StringBuilder explain);
  public void feedback(Post p);
}
