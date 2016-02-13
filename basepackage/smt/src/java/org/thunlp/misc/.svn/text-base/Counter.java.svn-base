package org.thunlp.misc;

import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

public class Counter<KeyType> implements Iterable<Entry<KeyType, Long>> {
	
	protected Map<KeyType, Long> hash;
	protected long total = 0;
	
	public Iterator<Entry<KeyType, Long>> iterator() {
		return hash.entrySet().iterator();
	}
	
	public void clear() {
		hash.clear();
		total = 0;
	}
	
	public int size() {
		return hash.size();
	}
	
	public long total() {
	  return total;
	}
	
	public Counter() {
		hash = new Hashtable<KeyType, Long>();
	}
	
	/**
	 * Add the count of another counter to this.
	 */
	public void inc(Counter<KeyType> another) {
	  for (Entry<KeyType, Long> e : another.hash.entrySet()) {
	    inc(e.getKey(), e.getValue());
	  }
	}
	
	/**
	 * Increase delta for each element in container.
	 */
	public void inc(Collection<KeyType> container, long delta) {
	  for (KeyType key : container) {
	    inc(key, delta);
	  }
	}
	
	 /**
   * Increase delta for each element in array.
   */
  public void inc(KeyType [] container, long delta) {
    for (KeyType key : container) {
      inc(key, delta);
    }
  }
	
	public void inc(KeyType key, long delta) {
	  Long current = hash.get(key);
    if ( current == null ) {
      current = 0l;
    }
    if (current + delta == 0) {
      hash.remove(key);
    } else {
      hash.put(key, current + delta);
    }
		total += delta;
	}
	
	public long get(KeyType key) {
		Long current = hash.get(key);
		if ( current == null ) {
			current = 0l;
		}
		return current;
	}
}
