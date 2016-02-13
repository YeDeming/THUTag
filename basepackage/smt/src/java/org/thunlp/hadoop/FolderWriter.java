package org.thunlp.hadoop;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.logging.Logger;

import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.mapred.JobConf;

public class FolderWriter {
	Logger LOG = Logger.getAnonymousLogger();

	private SequenceFile.Writer currentWriter;

	public FolderWriter(Path path, Class key, Class value) throws IOException {
		JobConf conf = new JobConf();
		init(path, FileSystem.get(conf), conf, key, value, -1);
	}

	public FolderWriter(Path path, FileSystem fs, JobConf conf, Class key, Class value) throws IOException {
		init(path, fs, conf, key, value, -1);
	}

	public FolderWriter(Path path, FileSystem fs, JobConf conf, Class key, Class value, int part) throws IOException {
		init(path, fs, conf, key, value, part);
	}

	public void init(Path path, FileSystem fs, JobConf conf, Class key, Class value, int part) throws IOException {
		Path outputPart;
		DecimalFormat partFormat = new DecimalFormat("00000");
		if (part >= 0) {
			// Use caller specified part number.
			outputPart = new Path(path, "part-" + partFormat.format(part));
		} else if (fs.exists(path)) {
			Path[] root = { path };
			FileStatus[] parts = fs.listStatus(root);
			String partnum = partFormat.format(parts.length);
			outputPart = new Path(path, "part-" + partnum);
			LOG.info("folder exists, write to a new part:" + outputPart.toString());
		} else {
			outputPart = new Path(path, "part-00000");
			LOG.info("create first part");
		}

		currentWriter = SequenceFile.createWriter(fs, conf, outputPart, key, value);
	}

	public void append(Writable key, Writable value) throws IOException {
		currentWriter.append(key, value);
	}

	public void close() throws IOException {
		currentWriter.close();
	}
}
