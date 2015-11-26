package org.thunlp.tagsuggest.common;

import java.util.Comparator;
import java.util.Map.Entry;

public class PairComparator<T> implements Comparator<Entry<T, Double>> {
	@Override
	public int compare(Entry<T, Double> o1, Entry<T, Double> o2) {
		if (o1.getValue() < o2.getValue()) {
			return 1;
		} else {
			return 0;
		}
	}
}