package org.thunlp.tagsuggest.dataset;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.thunlp.io.JsonUtil;
import org.thunlp.io.RecordWriter;
import org.thunlp.language.chinese.LangUtils;
import org.thunlp.misc.Flags;
import org.thunlp.misc.StringUtil;
import org.thunlp.tagsuggest.common.LegacyFeatureExtractor;
import org.thunlp.tagsuggest.common.Post;
import org.thunlp.tool.GenericTool;

public class ImportBibSonomy implements GenericTool {
  private static Logger LOG = Logger.getAnonymousLogger();
  private Connection dbconn = null;
  private Statement tagStmt = null;
  private Pattern keywordsRE = Pattern.compile("keywords = \\{(.*?)\\}");
  private Pattern accentRE = Pattern.compile("\\{[\\\"']*([a-zA-Z]+)\\}");
  private LegacyFeatureExtractor extractor = null;
  private Random random = new Random();
  private int numFolds = 5;
  private JsonUtil J = new JsonUtil();
  
  public ImportBibSonomy() {
    Properties config = new Properties();
    config.setProperty("userid", "false");
    config.setProperty("postid", "false");
    extractor = new LegacyFeatureExtractor();
  }

  public void openDbConnection(String addr, 
      String user, String pass, String dbName) throws Exception {
    Class.forName("com.mysql.jdbc.Driver").newInstance();
    String url = "jdbc:mysql://" + addr + "/" + dbName;
    dbconn = DriverManager.getConnection (url, user, pass);
    LOG.info("Database opened.");
  }
  
  private String loadTags(Post p, Set<String> tags)
  throws SQLException {
    tags.clear();
    ResultSet tagSet = 
      tagStmt.executeQuery("SELECT * from tas WHERE content_id=" +
          p.getId() + " ORDER BY user");
    String userId = null;
    Set<String> allUserIds= new TreeSet<String>();
    while (tagSet.next()) {
      userId = Long.toString(tagSet.getLong("user"));
      String tag = noNull(tagSet.getString("tag"));
      tags.add(tag);
      allUserIds.add(userId);
      p.setTimestamp(tagSet.getDate("date").getTime() / 1000);
    }
    tagSet.close();
    if (allUserIds.size() != 1) {
      throw new RuntimeException("multi-user for the same resource. "
          + allUserIds.size());
    }
    return userId;
  }
  
  public String noNull(String text) {
    return text == null ? "" : text;
  }
  
  public void dumpBibtex(String path) throws IOException, SQLException {
    if (path.length() == 0)
      return;
    RecordWriter writer = new RecordWriter(path);
    Statement stmt = dbconn.createStatement();
    tagStmt = dbconn.createStatement();
    Post doc = new Post();
    Set<String> rawTags = new HashSet<String>();
    doc.setTags(new HashSet<String>());
    ResultSet rs = null;
    int n = 0;
    
    LOG.info("Exporting bibtex.");
    rs = stmt.executeQuery("SELECT * FROM bibtex");
    List<String> parts = new LinkedList<String>();
    while (rs.next()) {
      doc.setId(Long.toString(rs.getLong("content_id")));
      doc.setResourceKey(rs.getString("simhash1"));
      doc.setTitle(clean(noNull(rs.getString("title"))));
      parts.clear();
      // parts.add(noNull(rs.getString("bibtexAbstract")));
      // parts.add(noNull(rs.getString("journal")));
      // parts.add(noNull(rs.getString("booktitle")));
      // parts.add(noNull(rs.getString("annote")));
      parts.add(noNull(rs.getString("note")));
      parts.add(noNull(rs.getString("description")));
      // parts.add(noNull(rs.getString("author")));
      parts.add(getKeywordsFromBibtex(rs.getString("misc")));
      String content = StringUtil.join(parts, " ");
      doc.setContent(clean(content));
      rawTags.clear();
      String userId = loadTags(doc, rawTags);
      doc.setUserId(userId);
      doc.setExtras(Integer.toString(random.nextInt(numFolds)));
      doc.getTags().clear();
      extractor.cleanTags(rawTags, doc.getTags());
      n++;
      if (n % 1000 == 0)
        LOG.info(Integer.toString(n));
      writer.add(J.toJson(doc));
    }
    rs.close();
    LOG.info("loaded " + n);
    writer.close();
  }
  
  private String clean(String content) {
    content = accentRE.matcher(content).replaceAll("$1");
    content = LangUtils.removePunctuationMarks(content);
    content = LangUtils.removeLineEnds(content);
    content = LangUtils.removeExtraSpaces(content);
    return content;
  }
  
  public void dumpBookmark(String path) 
  throws IOException, SQLException {
    if (path.length() == 0)
      return;
    RecordWriter writer = new RecordWriter(path);
    Statement stmt = dbconn.createStatement();
    tagStmt = dbconn.createStatement();
    Post doc = new Post();
    Set<String> rawTags = new HashSet<String>();
    doc.setTags(new HashSet<String>());
    ResultSet rs = null;
    int n = 0;
    
    LOG.info("Exporting bookmark.");
    rs = stmt.executeQuery("SELECT * FROM bookmark");
    List<String> parts = new LinkedList<String>();
    while (rs.next()) {
      doc.setId(Long.toString(rs.getLong("content_id")));
      doc.setResourceKey(rs.getString("url_hash"));
      parts.clear();
      parts.add(noNull(rs.getString("description")));
      parts.add(noNull(rs.getString("extended")));
      String content = StringUtil.join(parts, " ");
      content = LangUtils.removePunctuationMarks(content);
      content = LangUtils.removeLineEnds(content);
      content = LangUtils.removeExtraSpaces(content);
      doc.setContent(content);
      rawTags.clear();
      String userId = loadTags(doc, rawTags);
      doc.setUserId(userId);
      doc.setExtras(Integer.toString(random.nextInt(numFolds)));
      doc.getTags().clear();
      extractor.cleanTags(rawTags, doc.getTags());
      n++;
      if (n % 1000 == 0)
        LOG.info(Integer.toString(n));
      writer.add(J.toJson(doc));
    }
    rs.close();
    LOG.info(" ... " + n);
    writer.close();
  }
  
  public String getKeywordsFromBibtex(String text) {
    if (text == null) {
      return "";
    }
    Matcher m = keywordsRE.matcher(text);
    if (m.find()) {
      return m.group(1);
    }
    return "";
  }

  public void close() throws SQLException {
    dbconn.close();
  }
  
  /**
   * @param args
   */
  public void run(String[] args) throws Exception {
    Flags flags = new Flags();
    flags.addWithDefaultValue("db_addr", "localhost", "");
    flags.addWithDefaultValue("db_user", "root", "");
    flags.addWithDefaultValue("db_pass", "secret", "");
    flags.addWithDefaultValue("db_name", "rsdc_cleaned", "");
    flags.add("output_bibtex");
    flags.add("output_bookmark");
    flags.parseAndCheck(args);
    
    openDbConnection(
        flags.getString("db_addr"),
        flags.getString("db_user"),
        flags.getString("db_pass"),
        flags.getString("db_name"));
    dumpBibtex(flags.getString("output_bibtex"));
    dumpBookmark(flags.getString("output_bookmark"));
    close();
  }

}
