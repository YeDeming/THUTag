package org.thunlp.tagsuggest.dataset;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.logging.Logger;

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.JobClient;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.Mapper;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.Reducer;
import org.apache.hadoop.mapred.Reporter;
import org.thunlp.hadoop.MapReduceHelper;
import org.thunlp.html.HtmlReformatter;
import org.thunlp.io.JsonUtil;
import org.thunlp.language.chinese.ForwardMaxWordSegment;
import org.thunlp.language.chinese.LangUtils;
import org.thunlp.language.chinese.WordSegment;
import org.thunlp.misc.Flags;
import org.thunlp.misc.StringUtil;
import org.thunlp.tagsuggest.common.Post;
import org.thunlp.text.Lexicon;
import org.thunlp.tool.GenericTool;

/**
 * Make training data for tag LDA model. 
 * This tool runs as a Hadoop MapReduce program.
 * @author sixiance
 *
 */
public class MakeDistTagLDAInput implements GenericTool {
  private static Logger LOG = Logger.getAnonymousLogger();
  
  Flags flags = new Flags();

  @Override
  public void run(String[] args) throws Exception {  
    parseFlags(args); 
    convert();
  }

  
  private void parseFlags(String[] args) {
    flags.add("input");
    flags.add("output");
    flags.add("lexicon_dir", "This should on a shared volume, such as /global");
    flags.addWithDefaultValue("min_freq", "100");
    flags.addWithDefaultValue("sample_rate", "0.2");
    flags.parseAndCheck(args);
  }
  
  private void convert() throws IOException {
    JobConf job = new JobConf(this.getClass());
    job.setJobName("make-dist-taglda-input");
    MapReduceHelper.setAllOutputTypes(job, Text.class);
    MapReduceHelper.SetSeqFileInputOutput(
        job, flags.getString("input"), new Path(flags.getString("output")));
    MapReduceHelper.setMR(job, MakeInputMapper.class, MakeInputReducer.class);
    flags.saveToJobConf(job);
    JobClient.runJob(job);
  }
  
  public static class MakeInputMapper
  implements Mapper<Text, Text, Text, Text> {
    Text outkey = new Text();
    Text outvalue = new Text();
    Lexicon l = null;
    Flags flags = new Flags();
    WordSegment seg = null;
    JsonUtil J = new JsonUtil();
    Random random = new Random();
    double sampleRate = 0;
    
    public void configure(JobConf job) {
      flags.loadFromJobConf(job);
      l = new Lexicon();
      try {
        seg = new ForwardMaxWordSegment();
      } catch (IOException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
      sampleRate = flags.getDouble("sample_rate");
    }

    private String clean(String text) {
      text = HtmlReformatter.getPlainText(text);
      text = LangUtils.removePunctuationMarks(text);
      text = LangUtils.removeLineEnds(text);
      text = LangUtils.removeExtraSpaces(text);
      text = LangUtils.T2S(text);
      return text;
    }
    
    public void map(Text key, Text value,
        OutputCollector<Text, Text> collector, Reporter r) throws IOException {
      Post p = J.fromTextAsJson(value, Post.class);
      List<String> tokens = new ArrayList<String>();
      String [] words = null;
      
      words = seg.segment(clean(p.getTitle()));
      for (String word : words) {
        if (org.thunlp.language.chinese.Stopwords.isStopword(word))
          continue;
        tokens.add(word);
      }
      words = seg.segment(clean(p.getContent()));
      for (String word : words) {
        if (org.thunlp.language.chinese.Stopwords.isStopword(word))
          continue;
        tokens.add(word);
      }
      
      for (String tag : p.getTags()) {
        tokens.add("_" + tag);
      }
      l.addDocument(tokens.toArray(new String[tokens.size()]));
      value.set(StringUtil.join(tokens, " "));
      collector.collect(key, value);
    }

    public void close() {
      l.saveToFile(new File(
          flags.getString("lexicon_dir") + "/" + random.nextInt()));
      LOG.info("Lexicon saved");
    }
  }
  
  public static class MakeInputReducer 
  implements Reducer<Text, Text, Text, Text> {
    Text outkey = new Text();
    Text outvalue = new Text();
    Flags flags = new Flags();
    Lexicon l = null;

    public void configure(JobConf job) {
      flags.loadFromJobConf(job);
      loadAndCombineLexicons(flags.getString("lexicon_dir"));
    }
    
    private void loadAndCombineLexicons(String lexiconDirPath) {
      File lexiconDir = new File(lexiconDirPath);
      if (!lexiconDir.isDirectory()) {
        throw new RuntimeException(
            "lexicon dir " + lexiconDirPath + " is not a directory");
      }
      File [] files = lexiconDir.listFiles();
      l = new Lexicon();
      for (File f : files) {
        Lexicon tl = new Lexicon();
        tl.loadFromFile(f);
        l.mergeFrom(tl);
        LOG.info("Merging from " + f);
      }
      LOG.info("Merged lexicon from " + files.length + " files");
      LOG.info("BEFORE num_doc:" + l.getNumDocs() + " words:" + l.getSize());
      l = l.removeLowDfWords(flags.getInt("min_freq"));
      LOG.info("AFTER num_doc:" + l.getNumDocs() + " words:" + l.getSize());
    }

    public void reduce(Text key, Iterator<Text> values,
        OutputCollector<Text, Text> collector, Reporter r) throws IOException {
      List<String> selected = new ArrayList<String>();
      while (values.hasNext()) {
        Text value = values.next();
        String [] tokens = value.toString().split(" ");
        boolean hasTag = false;
        selected.clear();
        for (String token : tokens) {
          if (l.getWord(token) != null) {
            selected.add(token);
            if (token.startsWith("_"))
              hasTag = true;
          }
        }
        if (hasTag) {
          value.set(StringUtil.join(selected, " "));
          collector.collect(key, value);
        }
      }
    }

    public void close() {
    }
  }
}
