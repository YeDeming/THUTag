
import java.util.HashMap;
import java.util.HashSet;

import weka.classifiers.UpdateableClassifier;

public class DoubanPost extends Post {
	private HashMap<String, Integer> doubanTags;

	public DoubanPost() {
		setTags(new HashSet<String>());
		doubanTags = new HashMap<String, Integer>();
	}

	public HashMap<String, Integer> getDoubanTags() {
		return doubanTags;
	}

	public void setDoubanTags(HashMap<String, Integer> tags) {
		doubanTags = tags;
	}
}
