package org.thunlp.text;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 对文本进行去重处理，从文本中得到一组哈希值，哈希值一致的文本被认为是重复文本
 * 
 * @author adam
 *
 */
public class FuzzyHash {

	private int radius = 0;
	private double fuzziness = 0.0;
	private Pattern anchorPattern;
	private Map<String, Integer> features;
	private MessageDigest hasher;

	/**
	 * 
	 * @param radius
	 * @param fuzziness
	 * @throws NoSuchAlgorithmException
	 *             系统中没有MD5算法
	 */
	public FuzzyHash(int radius, double fuzziness) throws NoSuchAlgorithmException {
		this.radius = radius;
		this.fuzziness = fuzziness;
		this.features = new Hashtable<String, Integer>();
		this.hasher = MessageDigest.getInstance("MD5");
	}

	// public String estimateAnchor( String text ) {
	// return "的";
	// }

	public void setAnchor(String[] anchors) {
		StringBuilder sb = new StringBuilder();
		String prefix = "(";
		for (String a : anchors) {
			sb.append(prefix);
			sb.append(a);
			prefix = "|";
		}
		sb.append(")");
		String pattern = "(.{" + radius + "})" + sb + "(.{" + radius + "})";
		anchorPattern = Pattern.compile(pattern);
	}

	public String getHash(String text) {
		hasher.reset();
		features.clear();
		Matcher m = anchorPattern.matcher(text);
		while (m.find()) {
			String feature = m.group(0);
			Integer count = features.get(feature);
			if (count == null)
				count = 0;
			features.put(feature, count + 1);
		}

		Entry[] entries = new Entry[features.size()];
		int n = 0;
		for (Entry e : features.entrySet()) {
			entries[n++] = e;
		}

		Arrays.sort(entries, new Comparator<Entry>() {

			public int compare(Entry e0, Entry e1) {
				return (Integer) e1.getValue() - (Integer) e0.getValue();
			}

		});

		int nused = (int) (n * (1 - fuzziness));

		for (int i = 0; i < nused; i++) {
			hasher.update(entries[i].getKey().toString().getBytes());
		}
		String result = "";
		try {
			result = new String(hasher.digest(), "UTF-8");
		} catch (UnsupportedEncodingException e2) {
			result = new String(hasher.digest());
		}
		return result;
	}

}
