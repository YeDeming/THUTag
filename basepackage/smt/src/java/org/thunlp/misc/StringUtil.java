package org.thunlp.misc;

import java.util.Collection;
import java.util.List;

public class StringUtil {
	public static String join(Collection strings, String delimiter) {
		return join(strings, delimiter, Integer.MAX_VALUE);
	}

	public static String join(Object[] strings, String delimiter) {
		return join(strings, delimiter, Integer.MAX_VALUE);
	}

	public static String join(String[] strings, String delimiter) {
		return join(strings, delimiter, Integer.MAX_VALUE);
	}

	public static String join(Collection strings, String delimiter, int max) {
		StringBuilder sb = new StringBuilder();
		boolean first = true;
		int n = 0;
		for (Object s : strings) {
			if (!first) {
				sb.append(delimiter);
			}
			sb.append(s.toString());
			first = false;
			n++;
			if (n >= max)
				break;
		}
		return sb.toString();
	}

	public static String join(Object[] objects, String delimiter, int max) {
		StringBuilder sb = new StringBuilder();
		boolean first = true;
		int n = 0;
		for (Object o : objects) {
			if (!first) {
				sb.append(delimiter);
			}
			sb.append(o.toString());
			first = false;
			n++;
			if (n >= max)
				break;
		}
		return sb.toString();
	}

	public static void split(String text, char delimiter, List<String> tokens) {
		int start = 0, end = 0;
		while (start < text.length() && text.charAt(start) == delimiter) {
			start++;
		}
		end = start;
		while (start < text.length()) {
			while (end < text.length() && text.charAt(end) != delimiter) {
				end++;
			}
			tokens.add(text.substring(start, end));
			while (end < text.length() && text.charAt(end) == delimiter) {
				end++;
			}
			start = end;
		}
	}
}
