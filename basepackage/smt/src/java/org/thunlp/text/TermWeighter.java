package org.thunlp.text;

import org.thunlp.text.Lexicon.Word;

public abstract class TermWeighter {
	protected Lexicon lexicon;

	public TermWeighter(Lexicon l) {
		lexicon = l;
	}

	abstract public double weight(int id, double tf, int doclen);
}
