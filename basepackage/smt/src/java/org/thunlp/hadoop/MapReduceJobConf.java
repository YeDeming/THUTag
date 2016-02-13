package org.thunlp.hadoop;

import org.apache.hadoop.io.Writable;
import org.apache.hadoop.io.WritableComparable;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.Mapper;
import org.apache.hadoop.mapred.Reducer;
import org.apache.hadoop.mapred.SequenceFileInputFormat;
import org.apache.hadoop.mapred.SequenceFileOutputFormat;

public class MapReduceJobConf extends JobConf {
	public MapReduceJobConf() {
		super();
		this.setInputFormat(SequenceFileInputFormat.class);
		this.setOutputFormat(SequenceFileOutputFormat.class);
	}

	public MapReduceJobConf(Class jobClass) {
		super(jobClass);
		this.setInputFormat(SequenceFileInputFormat.class);
		this.setOutputFormat(SequenceFileOutputFormat.class);
	}

	public void setKeyValueClass(Class<? extends WritableComparable> keyMerge, Class<? extends Writable> valueMerge,
			Class<? extends WritableComparable> keyOut, Class<? extends Writable> valueOut) {
		this.setMapOutputKeyClass(keyMerge);
		this.setMapOutputValueClass(valueMerge);
		this.setOutputKeyClass(keyOut);
		this.setOutputValueClass(valueOut);
	}

	public void setMapReduce(Class<? extends Mapper> mapper, Class<? extends Reducer> reducer) {
		this.setMapperClass(mapper);
		this.setReducerClass(reducer);
	}
}
