package org.thunlp.text;

import java.util.Arrays;
import java.util.Enumeration;
import java.util.Hashtable;

import org.thunlp.text.Lexicon.Word;

public class DocumentVector {
	private TermWeighter weighter;
	private Term.TermIdComparator idcomp;

	public DocumentVector(Lexicon l) {
		weighter = new TfIdfTermWeighter(l);
		idcomp = new Term.TermIdComparator();
	}

	public DocumentVector(Lexicon l, TermWeighter w) {
		weighter = w;
		idcomp = new Term.TermIdComparator();
	}

	public Term[] build(Word[] doc, boolean normalized) {
		Hashtable<Integer, Term> terms = new Hashtable<Integer, Term>();
		for (Word w : doc) {
			Term t = terms.get(w.id);
			if (t == null) {
				t = new Term();
				t.id = w.id;
				t.weight = 0;
				terms.put(t.id, t);
			}
			t.weight += 1;
		}

		Term[] vec = new Term[terms.size()];
		Enumeration<Term> en = terms.elements();
		int i = 0;
		double normalizer = 0;
		while (en.hasMoreElements()) {
			vec[i] = en.nextElement();
			vec[i].weight = weighter.weight(vec[i].id, vec[i].weight, doc.length);
			normalizer += vec[i].weight * vec[i].weight;
			i++;
		}

		if (normalized) {
			normalizer = Math.sqrt(normalizer);
			for (Term t : vec) {
				t.weight /= normalizer;
			}
		}

		Arrays.sort(vec, idcomp);

		return vec;
	}

	public double dotProduct(Term[] v1, Term[] v2) {
		int p1 = 0, p2 = 0;
		double product = 0.0;
		while (p1 < v1.length && p2 < v2.length) {
			if (v1[p1].id < v2[p2].id) {
				p1++;
			} else if (v2[p2].id < v1[p1].id) {
				p2++;
			} else {
				product += v1[p1].weight * v2[p2].weight;
				p1++;
				p2++;
			}
		}
		return product;
	}

}
