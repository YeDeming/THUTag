package org.thunlp.html;

import junit.framework.Assert;
import junit.framework.TestCase;

public class ReformatterTest extends TestCase {
	public void testGetPlainText() {
		String htmlpage = "<p>核心提示 ：采取措外，铲除&ldquo;潜规则&rdquo;，最好待不再是空中楼阁！<br />";
		String answer = "核心提示 ：采取措外，铲除“潜规则”，最好待不再是空中楼阁！";
		String plainText = HtmlReformatter.getPlainText(htmlpage);
		Assert.assertEquals(plainText, answer);
	}

	public void testGetPlainText1() {
		String htmlpage = "<Script type='javascript'> int a = 0</script>最好待不再是空中楼阁！<br />";
		String answer = "最好待不再是空中楼阁！";
		String plainText = HtmlReformatter.getPlainText(htmlpage);
		Assert.assertEquals(plainText, answer);
	}
}
