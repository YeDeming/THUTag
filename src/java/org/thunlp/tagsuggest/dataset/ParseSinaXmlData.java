/**
 * Created：May 20, 2013 4:39:50 PM  
 * Project：cxx  
 * @author cxx
 * @since JDK 1.6.0_13  
 * filename：ParseSinaXmlData.java  
 * description：  
 */
package org.thunlp.tagsuggest.dataset;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.thunlp.io.JsonUtil;
import org.thunlp.tagsuggest.common.Post;
import org.thunlp.tagsuggest.common.SinaPost;


public class ParseSinaXmlData {
	private final static String regxpForHtml = "<([^>]*)>([^<>]*)</([^>]*)>";
	private final static String hrefForHtml = "<a([^>]*)>([^<>]*)</a>";
	private final static String spanForHtml = "<span([^>]*)>([^<>]*)</span>";
	private final static String strongForHtml = "<strong([^>]*)>([^<>]*)</strong>";
	private final static Pattern pattern = Pattern.compile(regxpForHtml);
	private final static Pattern hrefPattern = Pattern.compile(hrefForHtml);
	private final static Pattern spanpattern = Pattern.compile(spanForHtml);
	private final static Pattern strongpattern = Pattern.compile(strongForHtml);
	
	public void cleanXmlAndHtml(String input, String output) throws IOException{
		BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(input),"UTF8"));
		String line = "";
		BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(output),"UTF8"));
		out.write("<docs>");
		out.newLine();
		out.flush();
		while((line = in.readLine()) != null){
			line = line.trim();
			line = line.replaceAll("&nbsp", "");
			
			if(line.equals("")){
				continue;
			}
			
			line = line.replaceAll("name=keywords", "name=\"keywords\"");
			line = line.replaceAll("name=description", "name=\"description\"");
			line = line.replaceAll("name=jspreload", "name=\"jspreload\"");
			
			if(line.startsWith("<meta")){
				if(!line.startsWith("<meta name=\"jspreload\"") && !line.endsWith("/>")){
					line = line.substring(0, line.length() - 1) + " />";
				}
			}else if(line.equals("</doc>")){
				out.write("</meta>");
				out.newLine();
				out.flush();
			}else if(line.startsWith("<url>")){
				
			}else{
				line = cleanHtmlStrong(line);
				line = cleanHtmlSpan(line);
				line = cleanHtmlHref(line);
				line = cleanHtmlTag(line);
				line = cleanHtmlStrong(line);
				line = cleanHtmlSpan(line);
				line = cleanHtmlHref(line);
				line = cleanHtmlTag(line);
			}
			out.write(line);
			out.newLine();
			out.flush();
		}
		out.write("</docs>");
		out.newLine();
		out.flush();
		in.close();
		out.close();
	}
	
	public void cleanXmlAndHtmlAgain(String input, String output) throws IOException{
		BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(input),"UTF8"));
		String line = "";
		BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(output),"UTF8"));
		boolean isMeta = false;
		boolean isJsPreload = false;
		while((line = in.readLine()) != null){
			line = line.trim();
			line = line.replaceAll("&nbsp", "");
			
			line = line.replaceAll("<strong>", "");
			line = line.replaceAll("<span([^>]*)>", "");
			line = line.replaceAll("</span>", "");
			line = line.replaceAll("<a([^>]*)>", "");
			line = line.replaceAll("</a>", "");
			line = line.replaceAll("<br />", "");
			
			if(line.equals("")){
				continue;
			}
			
			if(line.startsWith("<meta")){
				isMeta = true;
				if(line.equals("<meta name=\"jspreload\" content=\"jspreload\">") || 
						line.equals("<meta http-equiv=\"Content-type\" content=\"text/html; charset=gb2312\" />") ||
						line.equals("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=gb2312\" />") ||
						line.equals("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=GB2312\" />")){
					isJsPreload = true;
				}else{
					isJsPreload = false;
				}
			}else{
				if(isMeta && !isJsPreload){
					out.write("<meta name=\"jspreload\" content=\"jspreload\">");
					out.newLine();
					out.flush();
				}
				isMeta = false;
				isJsPreload = false;
			}
			out.write(line);
			out.newLine();
			out.flush();
			
		}
		in.close();
		out.close();
	}
	
	public String cleanHtmlStrong(String line){
		String result = "";
		
		boolean findTag = false;
		Matcher matcher = strongpattern.matcher(line);
		int start = 0;
		while(matcher.find()){
			findTag = true;
			result += line.substring(start, matcher.start());
			String entity = matcher.group();
			int firstRigthQuote = entity.indexOf(">");
			int lastLeftQuote = entity.lastIndexOf("<");
			result += line.substring(matcher.start() + firstRigthQuote + 1, matcher.start() + lastLeftQuote);
			start = matcher.end();
		}
		if(findTag){
			result += line.substring(start);
			result = result.replaceAll("<p>", "");
			result = result.replaceAll("</p>", "");
			result = result.trim();
			if(result.startsWith(">")){
				result = result.substring(1);
			}
			return result;
		}else{
			line = line.replaceAll("<p>", "");
			line = line.replaceAll("</p>", "");
			line = line.trim();
			if(line.startsWith(">")){
				line = line.substring(1);
			}
			return line;
		}
	}
	
	public String cleanHtmlHref(String line){
		String result = "";
		
		boolean findTag = false;
		Matcher matcher = hrefPattern.matcher(line);
		int start = 0;
		while(matcher.find()){
			findTag = true;
			result += line.substring(start, matcher.start());
			String entity = matcher.group();
			int firstRigthQuote = entity.indexOf(">");
			int lastLeftQuote = entity.lastIndexOf("<");
			result += line.substring(matcher.start() + firstRigthQuote + 1, matcher.start() + lastLeftQuote);
			start = matcher.end();
		}
		if(findTag){
			result += line.substring(start);
			result = result.replaceAll("<p>", "");
			result = result.replaceAll("</p>", "");
			result = result.trim();
			if(result.startsWith(">")){
				result = result.substring(1);
			}
			return result;
		}else{
			line = line.replaceAll("<p>", "");
			line = line.replaceAll("</p>", "");
			line = line.trim();
			if(line.startsWith(">")){
				line = line.substring(1);
			}
			return line;
		}
	}
	
	public String cleanHtmlSpan(String line){
		String result = "";
		
		boolean findTag = false;
		Matcher matcher = spanpattern.matcher(line);
		int start = 0;
		while(matcher.find()){
			findTag = true;
			result += line.substring(start, matcher.start());
			String entity = matcher.group();
			int firstRigthQuote = entity.indexOf(">");
			int lastLeftQuote = entity.lastIndexOf("<");
			result += line.substring(matcher.start() + firstRigthQuote + 1, matcher.start() + lastLeftQuote);
			start = matcher.end();
		}
		if(findTag){
			result += line.substring(start);
			result = result.replaceAll("<p>", "");
			result = result.replaceAll("</p>", "");
			result = result.trim();
			if(result.startsWith(">")){
				result = result.substring(1);
			}
			return result;
		}else{
			line = line.replaceAll("<p>", "");
			line = line.replaceAll("</p>", "");
			line = line.trim();
			if(line.startsWith(">")){
				line = line.substring(1);
			}
			return line;
		}
	}
	
	public String cleanHtmlTag(String line){
		String result = "";
		
		boolean findTag = false;
		Matcher matcher = pattern.matcher(line);
		int start = 0;
		while(matcher.find()){
			findTag = true;
			result += line.substring(start, matcher.start());
			String entity = matcher.group();
			int firstRigthQuote = entity.indexOf(">");
			int lastLeftQuote = entity.lastIndexOf("<");
			result += line.substring(matcher.start() + firstRigthQuote + 1, matcher.start() + lastLeftQuote);
			start = matcher.end();
		}
		if(findTag){
			result += line.substring(start);
			result = result.replaceAll("<p>", "");
			result = result.replaceAll("</p>", "");
			result = result.trim();
			if(result.startsWith(">")){
				result = result.substring(1);
			}
			return result;
		}else{
			line = line.replaceAll("<p>", "");
			line = line.replaceAll("</p>", "");
			line = line.trim();
			if(line.startsWith(">")){
				line = line.substring(1);
			}
			return line;
		}
	}
	
	public void cleanXml(String input, String output) throws IOException{
		BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(input),"UTF8"));
		String line = "";
		BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(output),"UTF8"));
		out.write("<docs>");
		out.newLine();
		out.flush();
		while((line = in.readLine()) != null){
			line = line.trim();
			line = line.replaceAll("&nbsp", "");
			
			if(line.equals("")){
				continue;
			}
			
			line = line.replaceAll("name=keywords", "name=\"keywords\"");
			line = line.replaceAll("name=description", "name=\"description\"");
			line = line.replaceAll("name=jspreload", "name=\"jspreload\"");
			
			if(line.startsWith("<meta")){
				if(!line.startsWith("<meta name=\"jspreload\"") && !line.endsWith("/>")){
					line = line.substring(0, line.length() - 1) + " />";
				}
			}else if(line.equals("</doc>")){
				out.write("</meta>");
				out.newLine();
				out.flush();
			}else if(line.contains("相关报道")){
				if(line.startsWith("> ")){
					line = line.substring(2) + "</p>";
				}
			}else if(line.contains("<a href")){
				line = cleanHref(line);
			}
			out.write(line);
			out.newLine();
			out.flush();
		}
		out.write("</docs>");
		out.newLine();
		out.flush();
		in.close();
		out.close();
	}
	
	public String cleanHref(String line){
		String result = line;
		String pattern1 = "href=";
		String pattern2 = " target=";
		String pattern3 = "_blank";
		int index1 = line.indexOf(pattern1);
		int index2 = line.indexOf(pattern2);
		int index3 = line.indexOf(pattern3);
		if(index1 < 0 || index2 < 0 || index3 < 0){
			return result;
		}else{
			result = line.substring(0,index1);
			if(line.charAt(index1 + pattern1.length()) == '\"'){
				System.out.println(line);
				result += line.substring(index1, index2);
			}else{
				result += pattern1 + "\"" + line.substring(index1+pattern1.length(), index2);
			}
			if(line.charAt(index2 - 1) == '\"'){
				result += "\"";
			}
			
			if(line.charAt(index2 + pattern2.length()) == '\"'){
				result += line.substring(index2, index3);
			}else{
				result += pattern2 + "\"" + line.substring(index2+pattern2.length(), index3);
			}
			if(line.charAt(index3 + pattern3.length()) == '\"'){
				result += pattern3 + "\"" + cleanHref(line.substring(index3 + pattern3.length() + 1));
			}else{
				result += pattern3 + "\"" + cleanHref(line.substring(index3+pattern3.length()));
			}
			return result;
		}
	}
	
	public void parseXmlToJson(String input, String output) throws IOException{
		//The data has so many mistake, so I decide to parse it to json from the text without parsing it to xml
		/*
		File inputXml = new File(input);
		SAXReader saxReader = new SAXReader();
		try {
			Document document = saxReader.read(inputXml);
			Element employees = document.getRootElement();
			for (Iterator i = employees.elementIterator(); i.hasNext();) {
				Element employee = (Element) i.next();
				for (Iterator j = employee.elementIterator(); j.hasNext();) {
					Element node = (Element) j.next();
					System.out.println(node.getName() + ":" + node.getText());
				}
			}
		} catch (DocumentException e) {
			System.out.println(e.getMessage());
		}
		System.out.println("dom4j parserXml");
		*/
		
		String docStart = "<doc>";
		String titleHint = "<title>";
		String keywordsHint = "keywords";
		String descriptionHint = "description";
		String contentHint = "jspreload";
		String docEnd = "</doc>";
		
		int docCounter = 0;
		boolean findDoc = false;
		boolean findContent = false;
		
		String title = "";
		String keywords = "";
		String description = "";
		String content = "";
		
		JsonUtil J = new JsonUtil();
		
		BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(input),"UTF8"));
		BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(output),"UTF8"));
		String line = "";
		int lineCounter = 0;
		while((line = in.readLine()) != null){
			lineCounter ++;
			if(!findDoc){
				if(line.equals(docStart)){
					findDoc = true;
				}else{
					continue;
				}
			}else{
				if(!findContent){
					if(line.startsWith(titleHint)){
						title = line.substring(titleHint.length(), line.length() - titleHint.length() - 1);
					}else if(line.contains(keywordsHint)){
						String keywordStart = "content=\"";
						int index = line.indexOf(keywordStart);
						int endIndex = line.lastIndexOf("\"");
						if(index < 0 || endIndex < 0 || (index + keywordStart.length() > endIndex)){
							System.out.println(lineCounter + ":" + line);
							keywords = "";
							continue;
						}
						keywords = line.substring(index + keywordStart.length(), endIndex);
					}else if(line.contains(descriptionHint)){
						String descriptionStart = "content=\"";
						int index = line.indexOf(descriptionStart);
						int endIndex = line.lastIndexOf("\"");
						description = line.substring(index + descriptionStart.length(), endIndex);
					}else if(!line.startsWith("<")){
						findContent = true;
						line = cleanHtmlStrong(line);
						line = cleanHtmlSpan(line);
						line = cleanHtmlHref(line);
						line = cleanHtmlTag(line);
						line = cleanHtmlStrong(line);
						line = cleanHtmlSpan(line);
						line = cleanHtmlHref(line);
						line = cleanHtmlTag(line);
						line = line.replaceAll("&nbsp", "");
						line = line.replaceAll("<strong>", "");
						line = line.replaceAll("<span([^>]*)>", "");
						line = line.replaceAll("</span>", "");
						line = line.replaceAll("<a([^>]*)>", "");
						line = line.replaceAll("</a>", "");
						line = line.replaceAll("<br />", "");
						line = line.trim();
						
						content = line;
					}
				}else{
					if(line.equals(docEnd)){
						findDoc = false;
						findContent = false;
						SinaPost post = new SinaPost();
						post.setTitle(title);
						post.setDescription(description);
						post.setContent(content);
						String[] datas = keywords.split(",");
						HashSet<String> tags = new HashSet<String>();
						for(String data : datas){
							if(data.equals(description)){
								continue;
							}else{
								tags.add(data);
							}
						}
						post.setTags(tags);
						if(tags.size() == 0){
							continue;
						}
						docCounter ++;
						post.setId(""+docCounter);
						
						out.write(J.toJson(post));
						out.newLine();
						out.flush();
						

					}else{
						line = cleanHtmlStrong(line);
						line = cleanHtmlSpan(line);
						line = cleanHtmlHref(line);
						line = cleanHtmlTag(line);
						line = cleanHtmlStrong(line);
						line = cleanHtmlSpan(line);
						line = cleanHtmlHref(line);
						line = cleanHtmlTag(line);
						line = line.replaceAll("&nbsp", "");
						line = line.replaceAll("<strong>", "");
						line = line.replaceAll("<span([^>]*)>", "");
						line = line.replaceAll("</span>", "");
						line = line.replaceAll("<a([^>]*)>", "");
						line = line.replaceAll("</a>", "");
						line = line.replaceAll("<br />", "");
						line = line.trim();
						
						content += line;
					}
				}
			}
		}
		in.close();
		out.close();
	}
	
	public void combineAllData(String[] files, String output) throws IOException{
		BufferedReader in = null;
		BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(output),"UTF8"));
		String line = "";
		for(String fileName : files){
			in = new BufferedReader(new InputStreamReader(new FileInputStream(fileName), "UTF8"));
			while((line = in.readLine()) != null){
				out.write(line);
				out.newLine();
				out.flush();
			}
			in.close();
		}
		out.close();
	}
	
	public void buildAllPost(String input, String output) throws IOException{
		BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(input),"UTF8"));
		BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(output),"UTF8"));
		String line = "";
		JsonUtil J = new JsonUtil();
		while((line = in.readLine()) != null){
			SinaPost sinaPost = J.fromJson(line, SinaPost.class);
			Post p = new Post();
			p.setId(sinaPost.getId());
			p.setResourceKey(sinaPost.getResourceKey());
			p.setTitle(sinaPost.getDescription());
			p.setContent(sinaPost.getContent());
			p.setUserId(sinaPost.getUserId());
			p.setTags(sinaPost.getTags());
			p.setTimestamp(sinaPost.getTimestamp());
			out.write(J.toJson(p));
			out.newLine();
			out.flush();
		}
		in.close();
		out.close();
	}
	
	public void buildRawText(String input, String output) throws IOException{
		BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(input),"UTF8"));
		BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(output),"UTF8"));
		String line = "";
		JsonUtil J = new JsonUtil();
		while((line = in.readLine()) != null){
			SinaPost sinaPost = J.fromJson(line, SinaPost.class);
			out.write(sinaPost.getTitle());
			out.newLine();
			out.write(sinaPost.getContent());
			out.newLine();
			out.flush();
		}
		in.close();
		out.close();
	}
	
	/**
	 * <p>Title:main</p>
	 * <p>Description:<p>
	 * @param args
	 */
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		ParseSinaXmlData parser = new ParseSinaXmlData();
		//parser.cleanXmlAndHtml("res/sina_data/2012.q1.txt", "res/sina_data/2012_q1_clean.txt");
		//parser.cleanXmlAndHtmlAgain("res/sina_data/2012_q1_clean.txt", "res/sina_data/2012_q1_cleanAgain.txt");
		//parser.parseXmlToJson("res/sina_data/2012_q1_cleanAgain.txt", "");
		//parser.parseXmlToJson("res/sina_data/2012.q1.txt", "res/sina_data/2012_q1_clean.txt");
		//parser.parseXmlToJson("res/sina_data/2012.q2.txt", "res/sina_data/2012_q2_clean.txt");
		//parser.parseXmlToJson("res/sina_data/2012.q3.txt", "res/sina_data/2012_q3_clean.txt");
		//parser.parseXmlToJson("res/sina_data/2012.q4.txt", "res/sina_data/2012_q4_clean.txt");
		//parser.parseXmlToJson("res/sina_data/2013.q1.txt", "res/sina_data/2013_q1_clean.txt");
		/*
		String[] files = {"res/sina_data/2012_q1_clean.txt", "res/sina_data/2012_q2_clean.txt", 
				"res/sina_data/2012_q3_clean.txt", "res/sina_data/2012_q4_clean.txt",
				"res/sina_data/2013_q1_clean.txt"};
		parser.combineAllData(files, "res/sina_data/alldata.txt");
		*/
		
		//parser.buildAllPost("res/sina_data/alldata.txt", "res/sina_data/SinaPost.dat");
		
		parser.buildRawText("res/sina_data/alldata.txt", "res/sina_data/SinaRawText.txt");
	}

}
