package org.thunlp.language.chinese;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Pattern;

public class CRFWordSegment implements WordSegment {
  private ReentrantLock lock = new ReentrantLock();
  private Process crfpp = null;
  private OutputStream cmdStream = null;
  private BufferedReader resultReader = null;
  private BufferedWriter cmdWriter = null;
  private Charset defaultCharset = null;
  List<String> words = new LinkedList<String>();
  private HashSet<Character> punctuationSet;
  private static String YES = "YES";
  private static String NO = "NO";
  private static String NUMBER = "NUMBER";
  private static String ENGLISH_CHAR = "ENGLISH_CHAR";
  private static String CHINESE_TIME_CHAR = "CHINESE_TIME_CHAR";
  private static String DEFAULT = "DEFAULT";

  private static int TYPE_CHINESE = 0;
  private static int TYPE_PUNCTUATION = 1;
  private static int TYPE_ALPHANUMERIC = 2;
  private static int TYPE_NONE = 3;
  private static int TYPE_DELIMITER = 4;

  public CRFWordSegment () {
    defaultCharset = Charset.forName("GB18030");
    punctuationSet = makePunctuationSet();
    lock.lock();
    String libpath = System.getProperty("crfpp_path", "crfpp");
    String modelpath = System.getProperty("crfpp_model", "crfpp_model");
    initializeCRFPP ( libpath, modelpath );
    lock.unlock();
  }

  private HashSet<Character> makePunctuationSet() {
    HashSet<Character> set = new HashSet<Character>();
    String [] allMarks = ChineseLanguageConstants.ALL_MARKS;
    for (int i = 0; i < allMarks.length; i++) {
      for (int j = 0; j < allMarks[i].length(); j++) {
        set.add(allMarks[i].charAt(j));
      }
    }
    return set;
  }

  private boolean initializeCRFPP ( String libpath, String model) {
    boolean done = false;
    if ( crfpp == null ) {
      Runtime rt = Runtime.getRuntime();
      try {
        String runCmd = libpath + " -m " + model + " xxx";
        System.out.println(runCmd);
        crfpp = rt.exec(runCmd);

        InputStream is = crfpp.getInputStream();
        cmdStream = crfpp.getOutputStream();
        cmdWriter =
          new BufferedWriter(new OutputStreamWriter(cmdStream, defaultCharset));
        resultReader = 
          new BufferedReader(new InputStreamReader(is, defaultCharset));
        done = true;
      } catch (IOException e) {
        crfpp = null;
        System.out.println("failed to open crfpp");
        e.printStackTrace();
      }
    } else {
      done = true;
    }
    return done;
  }

  public boolean outputPosTag () {
    return false;
  }

  public void feedInputPattern(String text, int from, int to)
  throws IOException {
    for (int i = from; i < to; i++) {
      char ch = text.charAt(i);
      cmdWriter.write(ch);
      cmdWriter.write('\t');
      cmdWriter.write( punctuationSet.contains(ch) ? YES : NO );
      cmdWriter.write('\t');
      cmdWriter.write('S');
      if (LangUtils.isChinese(text.codePointAt(i))) {
        if (ch == '年' || ch == '月' || ch == '日'){
          cmdWriter.write(CHINESE_TIME_CHAR);
        } else {
          cmdWriter.write(DEFAULT);
        }
      } else if (ch > '0' && ch < '9'){
        cmdWriter.write(NUMBER);
      } else {
        cmdWriter.write(ENGLISH_CHAR);
      }
      cmdWriter.write('\n');
    }
    cmdWriter.write('\n');
    cmdWriter.flush();
  }
  
  private void segmentChinese(
      String text, int from, int to, List<String> words) {
    // Make input pattern.
    try {
      feedInputPattern(text, from, to);
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    String markers = null;
    try {
      markers = resultReader.readLine();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    if (markers.length() != to - from) {
      throw new RuntimeException("unmatched markers");
    }

    int head = 0;

    for (int j = 0; j < markers.length(); j++) {
      if (markers.charAt(j) == 'L')
        head = j;
      else if (markers.charAt(j) == 'R') {
        words.add(text.substring(from + head, from + j + 1));
        head = j + 1;
      } else if (markers.charAt(j) == 'S') {
        words.add(text.substring(from + j, from + j + 1));
        head = j + 1;
      }
    }
  }

  public String [] segment ( String text ) {
    words.clear();
    int from = 0;
    int type = TYPE_NONE;
    for (int i = 0; i < text.length(); i++) {
      int new_type = TYPE_NONE;
      char ch = text.charAt(i);
      // Whitespace rules all.
      if (ch == ' ' || ch == '\b' || ch == '\n' || ch == '\r' || ch == '\t') {
        new_type = TYPE_DELIMITER;
      } else if (LangUtils.isChinese(text.codePointAt(i))) {
        new_type = TYPE_CHINESE;
      } else if (punctuationSet.contains(ch)) {
        new_type = TYPE_PUNCTUATION;
      } else {
        new_type = TYPE_ALPHANUMERIC;
      }
      
      // See if the state changed.
      if (new_type != type) {
        if (type == TYPE_ALPHANUMERIC)
          words.add(text.substring(from, i));
        if (type == TYPE_CHINESE)
          segmentChinese(text, from, i, words);
        from = i;
        type = new_type;
      }
      
    }
    if (from < text.length()) {
      if (type == TYPE_ALPHANUMERIC)
        words.add(text.substring(from, text.length()));
      if (type == TYPE_CHINESE)
        segmentChinese(text, from, text.length(), words);
    }
    return words.toArray(new String[words.size()]);
  }
}
