package org.thunlp.tagsuggest.train;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Logger;

import org.thunlp.io.JsonUtil;
import org.thunlp.io.RecordReader;
import org.thunlp.misc.Flags;
import org.thunlp.tagsuggest.common.DataSource;
import org.thunlp.tagsuggest.common.ListDataSource;
import org.thunlp.tagsuggest.common.ConfigIO;
import org.thunlp.tagsuggest.common.ModelTrainer;
import org.thunlp.tagsuggest.common.Post;
import org.thunlp.tagsuggest.common.TagFilter;
import org.thunlp.tagsuggest.common.WordFeatureExtractor;
import org.thunlp.tagsuggest.contentbase.TagAllocationModel;
import org.thunlp.tagsuggest.contentbase.TagAllocationModel.Document;
import org.thunlp.text.Lexicon;
import org.thunlp.tool.GenericTool;

/**
 * Train a Tag Allocation Model.
 * @author sixiance
 *
 */
public class TrainTAM implements GenericTool, ModelTrainer {
  private static Logger LOG = Logger.getAnonymousLogger();
  JsonUtil J = new JsonUtil();
  WordFeatureExtractor fe = null;
  int minTagFreq = 1;
  TagFilter tagFilter = null;

  @Override
  public void run(String[] args) throws Exception {
    Flags flags = new Flags();
    flags.add("input");
    flags.add("output");
    flags.add("config");
    flags.parseAndCheck(args);

    Properties config = ConfigIO.configFromString(flags.getString("config"));
    train(flags.getString("input"), flags.getString("output"), config);      
  }

  @Override
  public void train(String inputPath, String modelPath, Properties config)
  throws IOException {  

	String fold = config.getProperty("fold", "-1");
    Lexicon wordlex = new Lexicon();
    Lexicon taglex = new Lexicon();
    WordFeatureExtractor.buildLexicons(
        inputPath, wordlex, taglex, config);
    fe = new WordFeatureExtractor(config);
    fe.setWordLexicon(wordlex);
    fe.setTagLexicon(taglex);
    tagFilter = new TagFilter(config, taglex);
    Set<String> filtered = new HashSet<String>();
    
    // Load all docs.
    List<Document> docs = new LinkedList<Document>();
    RecordReader reader = new RecordReader(inputPath);
    String workingDir = modelPath;
    File workingDirFile = new File(workingDir);
    if (!workingDirFile.exists()) {
    	workingDirFile.mkdir();
    }
    
     
    while (reader.next()) {
      Post p = J.fromJson(reader.value(), Post.class);
      if (!p.getExtras().equals(fold)) {        
        Document d = new Document();
        d.words = fe.extract(p);
        tagFilter.filterWithNorm(p.getTags(), filtered);
        d.tags = filtered.toArray(new String [filtered.size()]);
        d.reason = new String[d.tags.length];
        docs.add(d);
      }
      if (reader.numRead() % 5000 == 0)
        LOG.info("loading " + reader.numRead() + " docs");
    }
    reader.close();
    LOG.info("load " + docs.size() + " docs.");

    int numIter = Integer.parseInt(config.getProperty("niter", "40"));
    int numBurnIn = Integer.parseInt(config.getProperty("burnin", "30"));

    LOG.info("using config:" + config.toString());
    TagAllocationModel tam = new TagAllocationModel();
    tam.setAlpha(
        Double.parseDouble(config.getProperty("a0", "1")),
        Double.parseDouble(config.getProperty("a1", "1")));
    tam.setBeta(
        Double.parseDouble(config.getProperty("beta", "0.01")));
    tam.setGamma(
        Double.parseDouble(config.getProperty("gamma", "0.01")));
    // tam.setWordLexicon(wordlex);
    DataSource<Document> dataSource = new ListDataSource<Document>(docs);
    tam.train(dataSource, numIter, numBurnIn);

    OutputStream output = 
      new FileOutputStream(new File(modelPath+File.separator +"reason"));
    tam.saveTo(output);
    output.close();
  }

}
