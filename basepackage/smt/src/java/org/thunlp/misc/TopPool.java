package org.thunlp.misc;

import java.util.Comparator;
import java.util.Iterator;
import java.util.PriorityQueue;

public class TopPool<T extends Comparable<T>> implements Iterable<T> {
	private int n;
	private PriorityQueue<T> queue;

	public TopPool(int n) {
		this.n = n;
		queue = new PriorityQueue<T>(n + 2);
	}

	public TopPool(int n, Comparator<T> cmp) {
		this.n = n;
		queue = new PriorityQueue<T>(n + 2, cmp);
	}

	public boolean add(T object) {
		queue.add(object);
		if (queue.size() > n) {
			T polled = queue.poll();
			if (polled == object) {
				return false;
			}
		}
		return true;
	}

	public int size() {
		return queue.size();
	}

	public Iterator<T> iterator() {
		return queue.iterator();
	}
}
