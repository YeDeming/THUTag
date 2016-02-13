package org.thunlp.misc;

import junit.framework.Assert;
import junit.framework.TestCase;

public class TopPoolTest extends TestCase {
	public void testTopPool() {
		TopPool<Integer> tp = new TopPool<Integer>(5);
		int[] values = { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 };
		for (int v : values) {
			tp.add(v);
		}
		Assert.assertEquals(5, tp.size());
		for (Integer i : tp) {
			Assert.assertTrue(i > 5);
		}
	}
}
