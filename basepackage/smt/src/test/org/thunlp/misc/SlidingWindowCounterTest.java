package org.thunlp.misc;

import java.util.Map.Entry;

import junit.framework.Assert;
import junit.framework.TestCase;

public class SlidingWindowCounterTest extends TestCase {
	public void testMinCount() {
		SlidingWindowCounter<String> c = new SlidingWindowCounter<String>(2, 3);
		c.inc("a", 1);
		c.inc("a", 1);
		c.inc("a", 1);
		c.inc("b", 1);
		c.inc("c", 1);
		c.inc("d", 2);

		Assert.assertEquals(2, c.size());
		Assert.assertEquals(3, c.get("a"));
		Assert.assertEquals(2, c.get("d"));
		Assert.assertEquals(0, c.get("b"));
		Assert.assertEquals(0, c.get("c"));

		int n = 0;
		for (Entry<String, Long> e : c) {
			System.out.println(e.getKey() + " " + e.getValue());
			n++;
		}
		Assert.assertEquals(2, n);
	}
}