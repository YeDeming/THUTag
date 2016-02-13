package org.thunlp.text;

public class BinaryTermWeighter extends TermWeighter {

	public BinaryTermWeighter() {
		super(null);
	}

	@Override
	public double weight(int id, double tf, int doclen) {
		return 1;
	}

}
