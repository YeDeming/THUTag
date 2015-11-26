package org.thunlp.tagsuggest.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.thunlp.misc.WeightString;

public class ThreeMethodTest {
	String title;
	String summary;
	String content;
	String id;
	Set<String> suggestSet;
	
	public ThreeMethodTest() {
		// TODO Auto-generated constructor stub
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
	public void setSuggest(Set<String> tags){
		suggestSet = tags;
	}
	public Set<String> getSuggest(){
		return suggestSet;
	}
}
