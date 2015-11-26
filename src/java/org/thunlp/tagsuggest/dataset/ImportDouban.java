package org.thunlp.tagsuggest.dataset;

import java.io.IOException;
import java.util.Iterator;

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.JobClient;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.Mapper;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.Reducer;
import org.apache.hadoop.mapred.Reporter;
import org.apache.hadoop.mapred.SequenceFileOutputFormat;
import org.apache.hadoop.mapred.TextInputFormat;
import org.thunlp.hadoop.MapReduceHelper;
import org.thunlp.io.JsonUtil;
import org.thunlp.misc.Flags;
import org.thunlp.tagsuggest.common.Post;
import org.thunlp.tool.GenericTool;

/**
 * Import DOUBAN dataset as content-based tagging dataset.
 * 
 * DOUBAN dataset is stored as text files, in which each line is a JSON string.
 * We need two files, the subject.dat and the tag_subject.dat, which contain
 * DoubanRawSubject and DoubanRawTag respectively.
 * 
 * We convert the dataset to SequenceFile. The key is the subject ID, the value
 * is Post as JSON string. We do this by MapReducing.
 *  
 * @author sixiance
 *
 */
@SuppressWarnings("deprecation")
public class ImportDouban implements GenericTool {
  public static enum MRCounter {
    NUM_TAGS, NUM_SUBJECTS, NUM_DANGLING_TAGS, NUM_DANGLING_SUBJECTS,
    NUM_IO_EXCEPTION
  }
  
  public static final int DOUBAN_BOOK_CATID = 1001;
  
  public static class DoubanRawTag {
    private int count;
    private int cat_id;
    private String tag;
    private long subject_id;
    
    public void setSubject_id(long subject_id) {
      this.subject_id = subject_id;
    }
    
    public long getSubject_id() {
      return subject_id;
    }
    
    public void setTag(String tag) {
      this.tag = tag;
    }
    
    public String getTag() {
      return tag;
    }
    
    public void setCat_id(int cat_id) {
      this.cat_id = cat_id;
    }
    
    public int getCat_id() {
      return cat_id;
    }
    
    public void setCount(int count) {
      this.count = count;
    }
    
    public int getCount() {
      return count;
    }
  }
  
  public static class DoubanRawSubject {
    private String title;
    private String description;
    private long id;
    private int cat_id;
    
    public void setTitle(String title) {
      this.title = title;
    }
    public String getTitle() {
      return title;
    }
    public void setDescription(String description) {
      this.description = description;
    }
    public String getDescription() {
      return description;
    }
    public void setId(long id) {
      this.id = id;
    }
    public long getId() {
      return id;
    }
    public void setCat_id(int cat_id) {
      this.cat_id = cat_id;
    }
    public int getCat_id() {
      return cat_id;
    }
  }
  
  @Override
  public void run(String[] args) throws Exception {
    Flags flags = new Flags();
    flags.addWithDefaultValue(
        "tag_subject_data", "/media/work/datasets(secret)/douban/raw/tag_subject.dat", "");
    flags.addWithDefaultValue(
        "subject_data", "/media/work/datasets(secret)/douban/raw/subject.dat", "");
    flags.add("output");
    flags.parseAndCheck(args);
    
    JobConf job = new JobConf(this.getClass());
    job.setJobName("convert-douban-raw-to-posts");
    MapReduceHelper.setAllOutputTypes(job, Text.class);
    MapReduceHelper.setMR(
        job, DoubanRawMapper.class, DoubanToPostReducer.class);
    job.setInputFormat(TextInputFormat.class);
    TextInputFormat.addInputPath(
        job, new Path(flags.getString("tag_subject_data")));
    TextInputFormat.addInputPath(
        job, new Path(flags.getString("subject_data")));
    job.setOutputFormat(SequenceFileOutputFormat.class);
    SequenceFileOutputFormat.setOutputPath(
        job, new Path(flags.getString("output")));
    JobClient.runJob(job);
  }

  public static class DoubanRawMapper
  implements Mapper<LongWritable, Text, Text, Text> {
    Text outkey = new Text();
    Text outvalue = new Text();
    JsonUtil J = new JsonUtil();
    
    public void configure(JobConf job) {
    }

    public void map(LongWritable key, Text value,
        OutputCollector<Text, Text> collector, Reporter r) throws IOException {
      String json = value.toString();
      if (json.contains("\"tag\":")) {
        // This is a douban raw tag.
        DoubanRawTag tag = J.fromTextAsJson(value, DoubanRawTag.class);
        outkey.set(Long.toString(tag.getSubject_id()));
        r.incrCounter(MRCounter.NUM_TAGS, 1);
      } else {
        // This is a douban subject.
        DoubanRawSubject subject =
          J.fromTextAsJson(value, DoubanRawSubject.class);
        // We use books only.
        if (subject.getCat_id() != DOUBAN_BOOK_CATID) {
          return;
        }
        outkey.set(Long.toString(subject.getId()));
        r.incrCounter(MRCounter.NUM_SUBJECTS, 1);
      }
      collector.collect(outkey, value);
    }

    public void close() {
    }
  }

  public static class DoubanToPostReducer
  implements Reducer<Text, Text, Text, Text> {
    Text outkey = new Text();
    Text outvalue = new Text();
    JsonUtil J = new JsonUtil();
    
    public void configure(JobConf job) {
    }

    public void reduce(Text key, Iterator<Text> values,
        OutputCollector<Text, Text> collector, Reporter r) throws IOException {
      Post p = new Post();
      boolean gotTag = false;
      boolean gotSubject = false;
      while (values.hasNext()) {
        String json = values.next().toString();
        if (json.contains("\"tag\":")) {
          // This is a tag.
          DoubanRawTag tag = J.fromJson(json, DoubanRawTag.class);
          p.getTags().add(tag.getTag());
          gotTag = true;
        } else {
          // This is a subject.
          DoubanRawSubject subject = J.fromJson(json, DoubanRawSubject.class);
          p.setId(Long.toString(subject.getId()));
          p.setTimestamp(0L);
          p.setTitle(subject.getTitle());
          p.setContent(subject.getDescription());
          p.setUserId("");
          p.setExtras("");
          gotSubject = true;
        }
      }
      if (!gotTag && gotSubject) {
        r.incrCounter(MRCounter.NUM_DANGLING_SUBJECTS, 1);
        return;
      }
      if (gotTag && !gotSubject) {
        r.incrCounter(MRCounter.NUM_DANGLING_TAGS, 1);
        return;  // Ignore tags without subjects.
      }
      
      try {
        J.toTextAsJson(p, outvalue);
        collector.collect(key, outvalue);
      } catch (IOException e) {
        e.printStackTrace();
        r.incrCounter(MRCounter.NUM_IO_EXCEPTION, 1);
      }
    }

    public void close() {
    }
  }
}
