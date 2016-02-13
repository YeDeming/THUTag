package org.thunlp.crawler;

import java.util.Hashtable;

public class InternalCrawlerListener implements CrawlerListener {

	public Hashtable<String, InternalResult> storeBox;

	public InternalCrawlerListener() {
		storeBox = new Hashtable<String, InternalResult>();
	}

	public void handleFailed(String url, String ip, int httpStatusCode, Object customData) {
		InternalResult result = new InternalResult();
		result.content = null;
		result.httpStatusCode = httpStatusCode;
		storeBox.put(url, result);
	}

	public void handleSuccess(String url, String ip, byte[] content, String[] responseHeaders, Object customData) {
		InternalResult result = new InternalResult();
		result.content = content;
		storeBox.put(url, result);
	}

	public void workerQueueAvailable(int hashcode, int capacity) {
		// TODO Auto-generated method stub
	}

	public void workersAvailable(int capacity) {
		// TODO Auto-generated method stub
	}

	public class InternalResult {
		byte[] content;
		int httpStatusCode;
	}
}
