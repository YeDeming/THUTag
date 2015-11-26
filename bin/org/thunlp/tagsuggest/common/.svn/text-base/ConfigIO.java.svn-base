package org.thunlp.tagsuggest.common;

import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.Map.Entry;

import org.thunlp.misc.StringUtil;

public class ConfigIO {
  public static String configToString(Properties config) {
    if (config.size() == 0)
      return "";
    List<String> entries = new LinkedList<String>();
    for (Entry<Object, Object> e : config.entrySet()) { 
      entries.add(e.getKey() + "=" + e.getValue());
    }
    return StringUtil.join(entries, ";");
  }
  
  public static Properties configFromString(String str) {
    Properties config = new Properties();
    if (str.length() == 0)
      return config;
    String [] entries = str.split(";");
    for (String entry : entries) {
      String [] cols = entry.split("=");
      if (cols.length == 2)
        config.setProperty(cols[0], cols[1]);
    }
    return config;
  }
}
