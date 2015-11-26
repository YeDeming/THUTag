package org.thunlp.tagsuggest.common;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import org.thunlp.language.chinese.LangUtils;
import org.thunlp.text.Lexicon;

public class TagFilter {
	int minTagFreq = 0;
	Lexicon lex = null;
	Set<String> stopTags = null;

	public TagFilter(Properties config, Lexicon tagLex) {
		minTagFreq = Integer.parseInt(config.getProperty("mintagfreq", "1"));
		if (tagLex != null)
			lex = tagLex.removeLowFreqWords(minTagFreq);
		stopTags = new HashSet<String>();
		stopTags.add("imported");
		stopTags.add("public");
		stopTags.add("system:imported");
		stopTags.add("nn");
		stopTags.add("system:unfiled");
		stopTags.add("jabrefnokeywordassigned");
		stopTags.add("wismasys0809");
		stopTags.add("bibteximport");
		stopTags.add("杂谈");
	}

	public void filter(Set<String> tags, Set<String> filtered) {
		filtered.clear();
		for (String tag : tags) {
			if (lex != null && lex.getWord(tag) == null) {
				continue;
			}
			if (stopTags.contains(tag)) {
				continue;
			}
			filtered.add(tag);
		}
	}

	public void filterWithNorm(Set<String> tags, Set<String> filtered) {
		filtered.clear();
		for (String tag : tags) {
			if (lex != null && lex.getWord(tag) == null) {
				continue;
			}
			if (stopTags.contains(tag)) {
				continue;
			}
			String normed = normalize(tag);
			if (normed.length() > 0) {
				filtered.add(normed);
			}
		}
	}

	public void filterMapWithNorm(HashMap<String, Integer> tags,
			Set<String> filtered) {
		filtered.clear();
		for (Entry<String, Integer> e : tags.entrySet()) {
			String tag = e.getKey();
			if (lex != null && lex.getWord(tag) == null) {
				continue;
			}
			if (stopTags.contains(tag)) {
				continue;
			}

			if (tag.length() == 1 && !LangUtils.isChinese(tag.codePointAt(0))) {
				continue;
			}

			String normed = normalize(tag);
			if (normed.length() > 0) {
				filtered.add(normed);
			}
		}
	}

	Pattern spaceRE = Pattern.compile(" +");

	public String normalize(String tag) {
		tag = LangUtils.removePunctuationMarks(tag);
		tag = spaceRE.matcher(tag).replaceAll("");
		tag = LangUtils.T2S(tag);
		tag = tag.toLowerCase();
		return tag;
	}
}
