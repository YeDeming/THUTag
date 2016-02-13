package org.thunlp.hadoop;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.zip.GZIPInputStream;

import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.mapred.JobConf;

/**
 * 用于读取HDFS上的*.gz文件，文件内容应为纯文本文件
 * 
 * @author lipeng
 *
 */
public class HdfsGZIPFileReader implements HdfsReader {
	private BufferedReader reader;

	public HdfsGZIPFileReader(Path path) throws IOException {
		this(path, FileSystem.get(new JobConf()));
	}

	public HdfsGZIPFileReader(Path path, FileSystem fs) throws IOException {
		GZIPInputStream gis = new GZIPInputStream(fs.open(path));
		InputStreamReader isr = new InputStreamReader(gis);
		reader = new BufferedReader(isr);
	}

	public String readLine() throws IOException {
		if (reader == null)
			return null;
		else
			return reader.readLine();
	}

	public void close() throws IOException {
		if (reader != null) {
			reader.close();
		}
	}
}
