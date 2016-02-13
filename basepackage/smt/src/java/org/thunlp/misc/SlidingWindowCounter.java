package org.thunlp.misc;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

public class SlidingWindowCounter<KeyType> extends Counter<KeyType> {
	private int minCount = 0;
	private int maxSize = 0;
	private boolean pruned = false;

	public SlidingWindowCounter(int minCount, int maxSize) {
		super();
		this.minCount = minCount;
		this.maxSize = maxSize;
	}

	@Override
	public void inc(KeyType key, long delta) {
		super.inc(key, delta);
		if (hash.size() > maxSize) {
			pruneHash();
		} else {
			pruned = false;
		}
	}

	@Override
	public long get(KeyType key) {
		if (!pruned) {
			pruneHash();
		}
		return super.get(key);
	}

	@Override
	public Iterator<Entry<KeyType, Long>> iterator() {
		if (!pruned) {
			pruneHash();
		}
		return hash.entrySet().iterator();
	}

	public int size() {
		if (!pruned) {
			pruneHash();
		}
		return hash.size();
	}

	/**
	 * Remove small keys to keep the hash size under toKeep.
	 */
	private void pruneHash() {
		Map<KeyType, Long> prunedHash = new Hashtable<KeyType, Long>();
		for (Entry<KeyType, Long> e : hash.entrySet()) {
			if (e.getValue() >= minCount) {
				prunedHash.put(e.getKey(), e.getValue());
			}
		}
		hash = null;
		hash = prunedHash;
		pruned = true;
	}
}
