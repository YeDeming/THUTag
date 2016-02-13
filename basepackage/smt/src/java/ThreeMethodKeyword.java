
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.thunlp.misc.WeightString;

public class ThreeMethodKeyword {
	String title;
	String summary;
	String content;
	String id;
	Set<String> answerSet;
	List<String> smtTags;
	List<String> tfidfTags;
	List<String> textRankTags;
	List<String> wrongTags;
	Set<String> suggestSet;

	public ThreeMethodKeyword() {
		// TODO Auto-generated constructor stub

		answerSet = new HashSet<String>();
		smtTags = new ArrayList<String>();
		tfidfTags = new ArrayList<String>();
		textRankTags = new ArrayList<String>();
		wrongTags = new ArrayList<String>();
		suggestSet = new HashSet<String>();
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getTitle() {
		return title;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getContent() {
		return content;
	}

	public void setSummary(String summary) {
		this.summary = summary;
	}

	public String getSummary() {
		return summary;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getId() {
		return id;
	}

	public void setAnswer(Set<String> tags) {
		answerSet = tags;
	}

	public Set<String> getAnswer() {
		return answerSet;
	}

	public void setSmtTags(List<String> tags) {
		smtTags = tags;
	}

	public List<String> getSmtTags() {
		return smtTags;
	}

	public void setTfidfTags(List<String> tags) {
		tfidfTags = tags;
	}

	public List<String> getTfidfTags() {
		return tfidfTags;
	}

	public void setTextRankTags(List<String> tags) {
		textRankTags = tags;
	}

	public List<String> getTextRankTags() {
		return textRankTags;
	}

	public void setWrongTags(List<String> tags) {
		wrongTags = tags;
	}

	public List<String> getWrongTags() {
		return wrongTags;
	}

	public void setSuggest(Set<String> tags) {
		suggestSet = tags;
	}

	public Set<String> getSuggest() {
		return suggestSet;
	}
}
