
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class MyTag {
	String title;
	String content;
	HashMap<String, Integer> doubanTags;
	// HashMap<String, Double> suggestTags;

	public MyTag() {
		doubanTags = new HashMap<String, Integer>();
		// suggestTags = new HashMap<String, Double>();
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

	public void setDoubanTags(HashMap<String, Integer> tags) {
		doubanTags = tags;
	}

	public HashMap<String, Integer> getDoubanTags() {
		return doubanTags;
	}
	// public void setSuggestTags(HashMap<String, Double> tags) {
	// suggestTags = tags;
	// }
	// public HashMap<String, Double> getSuggestTags() {
	// return suggestTags;
	// }
}
