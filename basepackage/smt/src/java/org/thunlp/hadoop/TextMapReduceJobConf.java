package org.thunlp.hadoop;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.SequenceFileInputFormat;
import org.apache.hadoop.mapred.SequenceFileOutputFormat;

public class TextMapReduceJobConf extends JobConf {
	public TextMapReduceJobConf() {
		super();
		this.setInputFormat(SequenceFileInputFormat.class);
		this.setOutputFormat(SequenceFileOutputFormat.class);
		this.setMapOutputKeyClass(Text.class);
		this.setMapOutputValueClass(Text.class);
		this.setOutputKeyClass(Text.class);
		this.setOutputValueClass(Text.class);
	}

	public TextMapReduceJobConf(Class jobClass) {
		super(jobClass);
		this.setInputFormat(SequenceFileInputFormat.class);
		this.setOutputFormat(SequenceFileOutputFormat.class);
		this.setMapOutputKeyClass(Text.class);
		this.setMapOutputValueClass(Text.class);
		this.setOutputKeyClass(Text.class);
		this.setOutputValueClass(Text.class);
	}

	public void setMapReduce(Class<? extends TextMapper> mapper, Class<? extends TextReducer> reducer) {
		this.setMapperClass(mapper);
		this.setReducerClass(reducer);
	}
}
