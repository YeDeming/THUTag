package org.thunlp.hadoop;

import java.io.IOException;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.Mapper;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.Reporter;

public abstract class TextMapper implements Mapper<Text, Text, Text, Text> {

	public abstract void map(Text key, Text value, OutputCollector<Text, Text> collector, Reporter reporter)
			throws IOException;

	public void configure(JobConf conf) {

	}

	public void close() throws IOException {

	}

}
