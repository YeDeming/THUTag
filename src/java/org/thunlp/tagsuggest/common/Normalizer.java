package org.thunlp.tagsuggest.common;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

public class Normalizer {

	public static void l1Normalization(double[] values) {
		double sum = 0.0;
		for (int i = 0; i < values.length; ++i) {
			sum += values[i];
		}
		for (int i = 0; i < values.length; ++i) {
			values[i] /= sum;
		}
	}

	public static void l1Normalization(List<Double> values) {
		double sum = 0.0;
		for (int i = 0; i < values.size(); ++i) {
			sum += values.get(i).doubleValue();
		}
		for (int i = 0; i < values.size(); ++i) {
			values.set(i, values.get(i) / sum);
		}
	}

	public static <T> void l1Normalization(HashMap<T, Double> values) {
		double sum = 0.0;
		for (Iterator<Entry<T, Double>> iter = values.entrySet().iterator(); iter
				.hasNext();) {
			sum += iter.next().getValue().doubleValue();
		}
		for (Iterator<Entry<T, Double>> iter = values.entrySet().iterator(); iter
				.hasNext();) {
			Entry<T, Double> entry = iter.next();
			entry.setValue(entry.getValue().doubleValue() / sum);
		}
	}

	public static void l2Normalization(List<Double> values) {
		double sum = 0.0;
		for (int i = 0; i < values.size(); ++i) {
			sum += values.get(i).doubleValue() * values.get(i).doubleValue();
		}
		sum = Math.sqrt(sum);
		for (int i = 0; i < values.size(); ++i) {
			values.set(i, values.get(i) / sum);
		}
	}

	public static <T> void l2Normalization(HashMap<T, Double> values) {
		double sum = 0.0;
		for (Iterator<Entry<T, Double>> iter = values.entrySet().iterator(); iter
				.hasNext();) {
			double value = iter.next().getValue().doubleValue();
			sum += value * value;
		}
		sum = Math.sqrt(sum);
		for (Iterator<Entry<T, Double>> iter = values.entrySet().iterator(); iter
				.hasNext();) {
			Entry<T, Double> entry = iter.next();
			entry.setValue(entry.getValue().doubleValue() / sum);
		}
	}
}
