package org.thunlp.text;

import java.security.NoSuchAlgorithmException;

import junit.framework.Assert;
import junit.framework.TestCase;

public class FuzzyHashTest extends TestCase {
	public void testBasic() throws NoSuchAlgorithmException {
		String doc1 = "我们的内容是不说，这里有其他的东西，也许一样，也许不一样";
		String doc2 = "我们的内容是不说，这里有其他的东西，nbsp，也许一样，也许不一样 ";
		String doc3 = "神秘的身份是不能透露的";
		FuzzyHash fh = new FuzzyHash(1, 0.1);
		String[] anchors = { "的", "是", "有", "一" };
		fh.setAnchor(anchors);
		String key1 = fh.getHash(doc1);
		String key2 = fh.getHash(doc2);
		String key3 = fh.getHash(doc3);
		Assert.assertEquals(key1, key2);
		Assert.assertFalse(key1.equals(key3));
	}
}
