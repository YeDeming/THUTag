package org.thunlp.language.chinese;

import java.util.Hashtable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class is deprecated. Use LangUtils instead.
 * @author sixiance
 *
 */
@Deprecated
public class ContentFilter {
	protected Pattern allMarksPat;
	
	public ContentFilter () {
		String pat = "[" +
			ChineseLanguageConstants.ALL_MARKS[0] + 
			ChineseLanguageConstants.ALL_MARKS[1].
				replace("\\", "\\\\").replace("]","\\]").
				replace("[", "\\[").replace("-", "\\-") +
			"]";
		allMarksPat = Pattern.compile( pat );
		//System.out.println(pat);
	}
	public String filterExtraSpaces( String text ) {
		text = text.replace(
				ChineseLanguageConstants.SPACE[0], 
				ChineseLanguageConstants.SPACE[1]);
		text = text.replaceAll("[ \t\u000B\u000C\u00A0\uE5F1]+", " ");
		text = text.replaceAll("(^ +)|( +$)", "");
		return text;
	}
	
	public String filterEmptyLines ( String text ) {
		return text.replaceAll("[\r\n]+", "\n");
	}
	
	public String filterMarks ( String text ) {
		return filterExtraSpaces(allMarksPat.matcher(text).replaceAll(" "));
	}
	
	public String mapFullWidthLetterToHalfWidth(String text) {
		char [] buf = new char [text.length()];
		text.getChars(0, text.length(), buf, 0);
		
		for ( int i = 0 ; i < buf.length ; i++ ) {
			switch ( buf[i] ) {
			case 'Ａ' :
				buf[i] = 'A';
				break;
			case 'Ｂ' :
				buf[i] = 'B';
				break;
			case 'Ｃ' :
				buf[i]= 'C';
				break;
			case 'Ｄ':
				buf[i] = 'D';
				break;
			case 'Ｅ':
				buf[i] = 'E';
				break;
			case 'Ｆ':
				buf[i] = 'F';
				break;
			case 'Ｇ':
				buf[i] = 'G';
				break;
			case 'Ｈ':
				buf[i] = 'H';
				break;
			case 'Ｉ':
				buf[i] = 'I';
				break;
			case 'Ｊ':
				buf[i] = 'J';
				break;
			case 'Ｋ':
				buf[i] = 'K';
				break;
			case 'Ｌ':
				buf[i] = 'L';
				break;
			case 'Ｍ':
				buf[i] = 'M';
				break;
			case 'Ｎ':
				buf[i] = 'N';
				break;
			case 'Ｏ':
				buf[i] = 'O';
				break;
			case 'Ｐ':
				buf[i] = 'P';
				break;
			case 'Ｑ':
				buf[i] = 'Q';
				break;
			case 'Ｒ':
				buf[i] = 'R';
				break;
			case 'Ｓ':
				buf[i] = 'S';
				break;
			case 'Ｔ':
				buf[i] = 'T';
				break;
			case 'Ｕ':
				buf[i] = 'U';
				break;
			case 'Ｖ':
				buf[i] = 'V';
				break;
			case 'Ｗ':
				buf[i] = 'W';
				break;
			case 'Ｘ' :
				buf[i] = 'X';
				break;
			case 'Ｙ' :
				buf[i]= 'Y';
				break;
			case 'Ｚ':
				buf[i] = 'Z';
				break;
			case 'ａ':
				buf[i] = 'a';
				break;
			case 'ｂ':
				buf[i] = 'b';
				break;
			case 'ｃ':
				buf[i] = 'c';
				break;
			case 'ｄ':
				buf[i] = 'd';
				break;
			case 'ｅ':
				buf[i] = 'e';
				break;
			case 'ｆ':
				buf[i] = 'f';
				break;
			case 'ｇ':
				buf[i] = 'g';
				break;
			case 'ｈ':
				buf[i] = 'h';
				break;
			case 'ｉ':
				buf[i] = 'i';
				break;
			case 'ｊ':
				buf[i] = 'j';
				break;
			case 'ｋ':
				buf[i] = 'k';
				break;
			case 'ｌ':
				buf[i] = 'l';
				break;
			case 'ｍ':
				buf[i] = 'm';
				break;
			case 'ｎ':
				buf[i] = 'n';
				break;
			case 'ｏ':
				buf[i] = 'o';
				break;
			case 'ｐ':
				buf[i] = 'p';
				break;
			case 'ｑ':
				buf[i] = 'q';
				break;
			case 'ｒ':
				buf[i] = 'r';
				break;
			case 'ｓ':
				buf[i] = 's';
				break;
			case 'ｔ':
				buf[i] = 't';
				break;
			case 'ｕ':
				buf[i] = 'u';
				break;
			case 'ｖ':
				buf[i] = 'v';
				break;
			case 'ｗ':
				buf[i] = 'w';
				break;
			case 'ｘ':
				buf[i] = 'x';
				break;
			case 'ｙ':
				buf[i] = 'y';
				break;
			case 'ｚ':
				buf[i] = 'z';
				break;
			
			default :
				
			}
		}
		text = new String ( buf );
		return text;		
	}
	
	public String mapFullWidthNumberToHalfWidth(String text){
		char [] buf = new char [text.length()];
		text.getChars(0, text.length(), buf, 0);
		
		for ( int i = 0 ; i < buf.length ; i++ ) {
			switch ( buf[i] ) {
			case '０':
				buf[i] = '0';
				break;
			case '１':
				buf[i] = '1';
				break;
			case '２':
				buf[i] = '2';
				break;
			case '３':
				buf[i] = '3';
				break;
			case '４':
				buf[i] = '4';
				break;
			case '５':
				buf[i] = '5';
				break;
			case '６':
				buf[i] = '6';
				break;
			case '７':
				buf[i] = '7';
				break;
			case '８':
				buf[i] = '8';
				break;
			case '９':
				buf[i] = '9';
				break;
			default :
				
			}
		}
		text = new String ( buf );
		return text;		
	}
	
	public String mapChineseMarksToAnsi( String text ) {
		char [] buf = new char [text.length()];
		text.getChars(0, text.length(), buf, 0);
		
		for ( int i = 0 ; i < buf.length ; i++ ) {
			switch ( buf[i] ) {
			case '“' :
			case '”' :
				buf[i] = '"';
				break;
			case '‘' :
			case '’' :
				buf[i]= '\'';
				break;
			case '（':
				buf[i] = '(';
				break;
			case '）':
				buf[i] = ')';
				break;
			case '～':
				buf[i] = '~';
				break;
			case '｀':
				buf[i] = '`';
				break;
			case '！':
				buf[i] = '!';
				break;
			case '＠':
				buf[i] = '@';
				break;
			case '＃':
				buf[i] = '#';
				break;
			case '￥':
				buf[i] = '$';
				break;
			case '％':
				buf[i] = '%';
				break;
			case '＆':
				buf[i] = '&';
				break;
			case '＊':
				buf[i] = '*';
				break;
			case '＋':
				buf[i] = '+';
				break;
			case '－':
				buf[i] = '-';
				break;
			case '＝':
				buf[i] = '=';
				break;
			case '；':
				buf[i] = ';';
				break;
			case '：':
				buf[i] = ':';
				break;
			case '，':
				buf[i] = ',';
				break;
			case '／':
				buf[i] = '/';
				break;
			case '？':
				buf[i] = '?';
				break;
			case '｜':
				buf[i] = '|';
				break;
			case '　':
				buf[i] = ' ';
				break;
			default :
				
			}
		}
		text = new String ( buf );
		return text;
	}
	
	public String filterLineEnds ( String text ) {
		return text.replaceAll("[\r\n]+", " ").trim();
	}
}
