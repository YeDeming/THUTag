package org.thunlp.hadoop;

import java.io.IOException;

/**
 * 用于从HDFS上读取纯文本文件
 * 
 * @author lipeng
 *
 */
public interface HdfsReader {
	String readLine() throws IOException;

	void close() throws IOException;
}
