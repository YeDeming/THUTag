package org.thunlp.tagsuggest.common;

import java.io.File;
import java.io.FilenameFilter;

/**
 * 类Filter用于作为以extent为后缀名的文件的过滤器
 */
public class Filter implements FilenameFilter {
	private String extent;

	public Filter(String extent) {
		this.extent = extent;
	}

	public boolean accept(File dir, String name) {
		return name.endsWith("." + extent);
	}
}