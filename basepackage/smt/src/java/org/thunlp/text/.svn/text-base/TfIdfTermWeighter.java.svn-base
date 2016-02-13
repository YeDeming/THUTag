package org.thunlp.text;

import org.thunlp.text.Lexicon.Word;

public class TfIdfTermWeighter extends TermWeighter {

	public TfIdfTermWeighter(Lexicon l) {
		super(l);
	}

	@Override
	public double weight(int id, double tf, int doclen) {
		// default tf* idf implementation, can be overrided
		long n = lexicon.getNumDocs();
		Word w = lexicon.getWord( id );
		return Math.log10(tf + 1) * ( Math.log10( (double) n / w.df + 1 ));
	}

}
