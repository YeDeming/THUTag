package org.thunlp.url;

import junit.framework.Assert;
import junit.framework.TestCase;

public class HttpUrlNormalizerTest extends TestCase {
	public void testBasic() {
		String[] original = { "http://www.google.com/", "http://www.shit.com", "http://WwW.GoOgLe.CoM/",
				"http://shit.com/main.php#" };
		String[] correct = { "http://www.google.com", "http://www.shit.com", "http://www.google.com",
				"http://shit.com/main.php" };
		for (int i = 0; i < original.length; i++) {
			String result = HttpUrlNormalizer.normalize(original[i]);
			Assert.assertEquals(correct[i], result);
		}
	}

	public void testRelative() {
		String[] original = { "http://www.google.com/./", "http://www.shit.com/./shit/../doc.php",
				"http://www.test.com/shot/me/../../" };
		String[] correct = { "http://www.google.com", "http://www.shit.com/doc.php", "http://www.test.com" };
		for (int i = 0; i < original.length; i++) {
			String result = HttpUrlNormalizer.normalize(original[i]);
			Assert.assertEquals(correct[i], result);
		}
	}

	public void testDefaultPage() {
		String[] original = { "http://www.google.com/index.html", "http://www.shit.com/default.aspx",
				"http://www.test.com/index.php", "http://www.test.com/index.htm" };
		String[] correct = { "http://www.google.com", "http://www.shit.com", "http://www.test.com",
				"http://www.test.com" };
		for (int i = 0; i < original.length; i++) {
			String result = HttpUrlNormalizer.normalize(original[i]);
			Assert.assertEquals(correct[i], result);
		}
	}

	public void testMalformedHead() {
		String[] original = { "http:/www.google.com", "www.shit.com", "http//www.test.com",
				"http:///www.test.com/index.htm" };
		String[] correct = { "http://www.google.com", "http://www.shit.com", "http://www.test.com",
				"http://www.test.com" };
		for (int i = 0; i < original.length; i++) {
			String result = HttpUrlNormalizer.normalize(original[i]);
			Assert.assertEquals(correct[i], result);
		}
	}
}
