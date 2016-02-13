package org.thunlp.misc;

public class DoublePair {
	public double first;
	public double second;

	public DoublePair() {
		first = 0;
		second = 0;
	}

	public DoublePair(double first, double second) {
		this.first = first;
		this.second = second;
	}

	public String toString() {
		return first + ":" + second;
	}

	public boolean fromString(String str) {
		String[] cols = str.split(" ");
		if (cols.length != 2)
			return false;
		first = Double.parseDouble(cols[0]);
		second = Double.parseDouble(cols[1]);
		return true;
	}
}
