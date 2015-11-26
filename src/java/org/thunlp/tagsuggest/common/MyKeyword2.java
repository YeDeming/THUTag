package org.thunlp.tagsuggest.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.thunlp.misc.WeightString;

public class MyKeyword2 {
	String title;
	String summary;
	String content;
	String id;
	//HashMap<String, Integer> doubanTags;
	Set<String> answerSet;
	//HashMap<String, Double> suggestTags;
	List<String> suggestTags;
	
	public MyKeyword2() {
		//doubanTags = new HashMap<String, Integer>();
		answerSet = new HashSet<String>();
		//suggestTags = new HashMap<String, Double>();
		suggestTags = new ArrayList<String>();
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
	
	/*
	public void setDoubanTags(HashMap<String, Integer> tags) {
		doubanTags = tags;
	}
	public HashMap<String, Integer> getDoubanTags() {
		return doubanTags;
	}*/
	public void setAnswer(Set<String> tags){
		answerSet = tags;
	}
	public Set<String> getAnswer(){
		return answerSet;
	}
	public void setSuggestTags(List<String> tags) {
		suggestTags = tags;
	}
	public List<String> getSuggestTags() {
		return suggestTags;
	}
}
