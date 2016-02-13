package org.thunlp.keywordextract;

import java.io.File;
import java.util.List;

import junit.framework.TestCase;

import org.thunlp.misc.StringUtil;
import org.thunlp.misc.WeightString;
import org.thunlp.text.Lexicon;
import org.thunlp.text.keywordextract.TfIdfKeywordExtract;

public class TfIdfKeywordExtractTest extends TestCase {
	public void testExtract() {
		Lexicon l = new Lexicon(new File(
				"/Users/sixiance/Documents/workspace/rsdc09/newdata/models/fdt/bibtex.bigall.taguser.working/word.lex"));
		TfIdfKeywordExtract ke = new TfIdfKeywordExtract(l);
		String doc = "foo bar";

		List<WeightString> results = ke.extract(doc.toLowerCase().split(" "));
		System.out.println(StringUtil.join(results, ",\n"));
	}
}
