package org.thunlp.hadoop;

import org.apache.hadoop.io.Writable;
import org.apache.hadoop.io.WritableComparable;
import org.apache.hadoop.mapred.lib.IdentityMapper;

public class ReduceOnlyJobConf extends MapReduceJobConf {
	public ReduceOnlyJobConf() {
		super();
		this.setMapperClass(IdentityMapper.class);
	}

	public ReduceOnlyJobConf(Class jobClass) {
		super(jobClass);
		this.setMapperClass(IdentityMapper.class);
	}

	public void setKeyValueClass(Class<? extends WritableComparable> keyMerge, Class<? extends Writable> valueMerge,
			Class<? extends WritableComparable> keyOut, Class<? extends Writable> valueOut) {
		this.setMapOutputKeyClass(keyMerge);
		this.setMapOutputValueClass(valueMerge);
		this.setOutputKeyClass(keyOut);
		this.setOutputValueClass(valueOut);
	}
}
