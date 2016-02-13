package org.thunlp.language.chinese;

import java.io.Reader;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;

public class WordChineseAnalyzer extends Analyzer {
	private WordSegment segmentor;

	public WordChineseAnalyzer(WordSegment seg) {
		segmentor = seg;
	}

	@Override
	public TokenStream tokenStream(String fieldName, Reader reader) {
		// TODO Auto-generated method stub
		return new WordChineseTokenizer(segmentor, reader);
	}

}
