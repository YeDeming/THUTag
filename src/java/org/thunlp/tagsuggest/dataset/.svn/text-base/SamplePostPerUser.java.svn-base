package org.thunlp.tagsuggest.dataset;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Map.Entry;
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
import org.thunlp.io.JsonUtil;
import org.thunlp.io.RecordReader;
import org.thunlp.io.RecordWriter;
import org.thunlp.misc.Flags;
import org.thunlp.tagsuggest.common.Post;
import org.thunlp.tool.GenericTool;

/**
 * For each user, sample given number of posts.
 * @author sixiance
 *
 */
@SuppressWarnings("deprecation")
public class SamplePostPerUser implements GenericTool {
  private static Logger LOG = Logger.getAnonymousLogger();
  
  @Override
  public void run(String[] args) throws Exception {
    Flags flags = new Flags();
    flags.add("input");
    flags.add("output");
    flags.add("num_posts");
    flags.parseAndCheck(args);
    
    sample(
        flags.getString("input"),
        flags.getString("output"),
        flags.getInt("num_posts"));
  }
  
  /**
   * Load all data into the memory, then sample numPosts posts for each user.
   * @param flags
   * @throws IOException
   */
  public void sample(String input, String output, int numPosts)
  throws IOException {
    Random random = new Random();
    JsonUtil J = new JsonUtil();
    
    Map<String, List<String>> userposts = new Hashtable<String, List<String>>();
    RecordReader reader = new RecordReader(input);
    while (reader.next()) {
      Post p = J.fromJson(reader.value(), Post.class);
      String userid = p.getUserId();
      List<String> posts = userposts.get(userid);
      if (posts == null) {
        posts = new ArrayList<String>();
        userposts.put(userid, posts);
      }
      posts.add(reader.value());
      if (reader.numRead() % 1000 == 0) {
        LOG.info("read: " + reader.numRead());
      }
    }
    LOG.info("total " + reader.numRead() + " posts and " + userposts.size() +
        "users");
    reader.close();
    
    RecordWriter writer = new RecordWriter(output);
    // Sampling.
    int total = 0;
    for (Entry<String, List<String>> e : userposts.entrySet()) {
      List<String> posts = e.getValue();
      int n = 0;
      while (n < numPosts && posts.size() > 0) {
        writer.add(posts.remove(random.nextInt(posts.size())));
        n++;
      }
      total += n;
    }
    LOG.info("write " + total + " posts.");
    writer.close();
  }
  
  public void sampleMapReduce(Flags flags) throws IOException {
    JobConf job = new JobConf();
    job.setJarByClass(this.getClass());
    job.setJobName("sample-post-per-user");
    MapReduceHelper.setMR(job, SampleMapper.class, SampleReducer.class);
    MapReduceHelper.setAllOutputTypes(job, Text.class);
    MapReduceHelper.setSeqFileInputOutput(
        job,
        new Path(flags.getString("input")),
        new Path(flags.getString("output")));
    job.set("num_posts", flags.getString("num_posts"));
    JobClient.runJob(job);
  }
  
  public static class SampleMapper
  implements Mapper<Text, Text, Text, Text> {
    JsonUtil J = new JsonUtil();
    Text outkey = new Text();
    Text outvalue = new Text();

    public void configure(JobConf job) {
    }

    public void map(Text key, Text value,
        OutputCollector<Text, Text> collector, Reporter r) throws IOException {
      Post p = J.fromJson(value.toString(), Post.class);
      outkey.set(p.getUserId());
      collector.collect(outkey, value);
    }

    public void close() {
    }
  }
  
  public static class SampleReducer
  implements Reducer<Text, Text, Text, Text> {
    Text outkey = new Text();
    Text outvalue = new Text();
    int numPosts = 0;
    Random random = new Random();
    
    public void configure(JobConf job) {
      numPosts = Integer.parseInt(job.get("num_posts")); 
    }

    public void reduce(Text key, Iterator<Text> values,
        OutputCollector<Text, Text> collector, Reporter r) throws IOException {
      List<String> posts = new ArrayList<String>();
      while (values.hasNext()) {
        Text value = values.next();
        posts.add(value.toString());
      }
      // Sample.
      int sampled = 0;
      while (sampled < numPosts && posts.size() > 0) {
        int idx = random.nextInt(posts.size());
        String post = posts.remove(idx);
        outvalue.set(post);
        collector.collect(key, outvalue);
      }
    }

    public void close() {
    }
  }
}
