package org.thunlp.tagsuggest.dataset;

import java.io.IOException;
import java.util.Random;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.Mapper;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.Reporter;
import org.apache.hadoop.mapred.lib.IdentityReducer;
import org.thunlp.hadoop.MapReduceHelper;
import org.thunlp.misc.Flags;
import org.thunlp.tool.GenericTool;

@SuppressWarnings("deprecation")
public class Sample implements GenericTool {
  Random random = new Random();
  double prob = 0.0;

  @Override
  public void run(String[] args) throws Exception {
    Flags flags = new Flags();
    flags.add("input");
    flags.add("output");
    flags.add("prob");
    flags.parseAndCheck(args);

    prob = flags.getDouble("prob");

    JobConf job = new JobConf(this.getClass());
    job.set("prob", flags.getString("prob"));
    MapReduceHelper.runTextSeqFileMapReduce(
        job,
        SamplingMapper.class, IdentityReducer.class,
        flags.getString("input"), flags.getString("output"));
  }

  public static class SamplingMapper implements Mapper<Text, Text, Text, Text> {
    Text outkey = new Text();
    Text outvalue = new Text();
    Random R = new Random();
    double prob = 0;
    
    public void configure(JobConf job) {
      prob = Double.parseDouble(job.get("prob"));
    }

    public void map(Text key, Text value,
        OutputCollector<Text, Text> collector, Reporter r) throws IOException {
      if (R.nextDouble() < prob) {
        collector.collect(key, value);
      }
    }

    public void close() {
    }
  }
  
}
