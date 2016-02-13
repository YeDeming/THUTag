package org.thunlp.text;

public class TfOnlyTermWeighter extends TermWeighter {

	public TfOnlyTermWeighter() {
		super(null);
	}

	@Override
	public double weight(int id, double tf, int doclen) {
		return tf;
	}

}
