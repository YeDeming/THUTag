package org.thunlp.text.keywordextract;

import java.util.List;

import org.thunlp.misc.WeightString;

public interface KeywordExtract {
	public List<WeightString> extract(String[] doc);
}
