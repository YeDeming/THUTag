package org.thunlp.html;

import java.util.regex.Pattern;

import org.htmlparser.util.Translate;

/**
 * Try to fix html page design problems, especially in Chinese webpages
 * 
 * @author adam
 *
 */
public class HtmlReformatter {
	protected static Pattern commentBlockRE = Pattern.compile("<!--.*?-->", Pattern.MULTILINE | Pattern.DOTALL);
	protected static Pattern scriptBlockRE = Pattern.compile("<script.*?</script>",
			Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
	protected static Pattern styleBlockRE = Pattern.compile("<style.*?</style>",
			Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
	protected static Pattern htmlTagRE = Pattern.compile("<[^<>]+>", Pattern.MULTILINE);

	/**
	 * Convert a HTML page to corresponding plain text. This method do following
	 * things: 1) remove all <...> tags 2) remove script blocks in <_script>..
	 * <_/script> 3) remove css definitions in <_style>..<_/style> 4) convert
	 * escapes like &lt; to right character. 5) remove html comments like <!--
	 * -->
	 * 
	 * @param htmlPage
	 * @return
	 */
	public static String getPlainText(String htmlPage) {
		String content = htmlPage;
		content = commentBlockRE.matcher(content).replaceAll("");
		content = styleBlockRE.matcher(content).replaceAll("");
		content = scriptBlockRE.matcher(content).replaceAll("");
		content = htmlTagRE.matcher(content).replaceAll("");
		content = Translate.decode(content);
		content = content.replaceAll("nbsp", " ");
		return content;
	}

	public static String reformat(String htmlPage) {
		return htmlPage;
	}
}
