package org.thunlp.text;

import java.util.Comparator;

public class Term {
	public int id;
	public double weight;

	public static class TermIdComparator implements Comparator<Term> {
		public int compare(Term o1, Term o2) {
			return o1.id - o2.id;
		}

	}

	public static class TermWeightComparator implements Comparator<Term> {
		public int compare(Term o1, Term o2) {
			return Double.compare(o2.weight, o1.weight);
		}
	}
}
