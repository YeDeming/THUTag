package org.thunlp.language.chinese;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;

import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.Tokenizer;

public class WordChineseTokenizer extends Tokenizer {
	private WordSegment segmentor;
	private String[] segbuf;
	private int currentSeg;
	private int currentPos;
	private BufferedReader bufreader;

	public WordChineseTokenizer(WordSegment ws, Reader reader) {
		super(reader);
		segmentor = ws;
		segbuf = null;
		currentSeg = 0;
		currentPos = 0;
		bufreader = new BufferedReader(reader);
	}

	@Override
	public Token next() throws IOException {
		if (segbuf == null) {
			while (segbuf == null || segbuf.length == 0) {
				String line = bufreader.readLine();
				if (line == null) {
					return null;
				}
				segbuf = segmentor.segment(line);
			}
			currentSeg = 0;
		}

		Token t = new Token(segbuf[currentSeg], currentPos, currentPos + segbuf[currentSeg].length());
		currentPos += segbuf[currentSeg].length();
		currentSeg++;
		if (currentSeg >= segbuf.length)
			segbuf = null;

		return t;
	}

}
