
import java.util.HashSet;
import java.util.Set;

/**
 * A post with tags.
 * 
 * @author sixiance
 *
 */
public class Post {
	private String id = "";
	private String resourceKey = "";
	private String title = "";
	private String content = "";
	private String userId = "";
	private Set<String> tags;
	private long timestamp = 0L;
	private String extras = "";

	public Post() {
		tags = new HashSet<String>();
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getId() {
		return id;
	}

	public void setResourceKey(String resourceKey) {
		this.resourceKey = resourceKey;
	}

	public String getResourceKey() {
		return resourceKey;
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

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getUserId() {
		return userId;
	}

	public void setTags(Set<String> tags) {
		this.tags = tags;
	}

	public Set<String> getTags() {
		return tags;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public void setExtras(String extras) {
		this.extras = extras;
	}

	public String getExtras() {
		return extras;
	}
}
