package org.thunlp.hadoop;

import java.io.IOException;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.JobClient;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.SequenceFileInputFormat;
import org.apache.hadoop.mapred.SequenceFileOutputFormat;
import org.apache.hadoop.mapred.lib.IdentityReducer;

public class MapReduceHelper {
	private static Logger LOG = Logger.getAnonymousLogger();

	public static void setAllOutputTypes(JobConf job, Class typeClass) {
		job.setOutputKeyClass(typeClass);
		job.setOutputValueClass(typeClass);
		job.setMapOutputKeyClass(typeClass);
		job.setMapOutputValueClass(typeClass);
	}

	public static void setSeqFileInputOutput(JobConf job, Path input, Path output) {
		job.setInputFormat(SequenceFileInputFormat.class);
		job.setOutputFormat(SequenceFileOutputFormat.class);
		SequenceFileInputFormat.addInputPath(job, input);
		SequenceFileOutputFormat.setOutputPath(job, output);
	}

	public static void SetSeqFileInputOutput(JobConf job, String inputPaths, Path output) throws IOException {
		job.setInputFormat(SequenceFileInputFormat.class);
		job.setOutputFormat(SequenceFileOutputFormat.class);
		SequenceFileOutputFormat.setOutputPath(job, output);

		// Expand input pattern.
		FileSystem fs = FileSystem.get(job);
		String[] paths = inputPaths.split(",");
		for (String p : paths) {
			int lastslash = p.lastIndexOf("/");
			if (lastslash < 0) {
				p = "./" + p;
				lastslash = 1;
			}
			String parent = p.substring(0, lastslash);
			p = p.substring(lastslash + 1);
			// Each path is treated as a pattern.
			p = p.replace("\\", "\\\\");
			p = p.replace(".", "\\.");
			p = p.replace("*", ".*");
			p = p.replace("?", ".");
			LOG.info("Use pattern:" + p);
			Pattern re = Pattern.compile(p);
			// List all files.
			FileStatus[] files = fs.listStatus(new Path(parent));
			for (FileStatus f : files) {
				if (re.matcher(f.getPath().getName()).matches()) {
					SequenceFileInputFormat.addInputPath(job, f.getPath());
					LOG.info("Adding input:" + f.getPath());
				}
			}
		}
	}

	public static void setMR(JobConf job, Class mapper, Class reducer) {
		job.setMapperClass(mapper);
		job.setReducerClass(reducer);
		job.setNumMapTasks(48);
		job.setNumReduceTasks(12);
	}

	public static void setMapOnly(JobConf job, Class mapper) {
		job.setMapperClass(mapper);
		job.setReducerClass(IdentityReducer.class);
		job.setNumReduceTasks(0);
	}

	public static void runTextSeqFileMapReduce(JobConf job, Class mapper, Class reducer, String input, String output)
			throws IOException {
		job.setJobName(mapper.getSimpleName() + "-" + reducer.getSimpleName() + System.currentTimeMillis());
		MapReduceHelper.setAllOutputTypes(job, Text.class);
		MapReduceHelper.setMR(job, mapper, reducer);
		MapReduceHelper.SetSeqFileInputOutput(job, input, new Path(output));
		JobClient.runJob(job);
	}

	public static void runTextSeqFileMap(JobConf job, Class mapper, String input, String output) throws IOException {
		job.setJobName(mapper.getSimpleName() + "-maponly" + System.currentTimeMillis());
		MapReduceHelper.setAllOutputTypes(job, Text.class);
		MapReduceHelper.setMapOnly(job, mapper);
		MapReduceHelper.SetSeqFileInputOutput(job, input, new Path(output));
		JobClient.runJob(job);
	}
}