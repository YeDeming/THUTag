package org.thunlp.language.chinese;

import java.util.Hashtable;
import java.util.regex.Pattern;

/**
 * Some handy utility functions for text pre-process, especially for Chinese.
 * @author sixiance
 *
 */
public class LangUtils {
	protected static Pattern allMarksPat;
	protected static Hashtable<Character, Character> t2s;
	protected static Hashtable<Character, Character> s2t;
	
	/**
	 * Prevent this class to be instantiated.
	 */
	private LangUtils () {} 
	
	static {
	  // Initialize the regular expression for FilterMarks().
		String pat = "[" +
			ChineseLanguageConstants.ALL_MARKS[0] + 
			ChineseLanguageConstants.ALL_MARKS[1].
				replace("\\", "\\\\").replace("]","\\]").
				replace("[", "\\[").replace("-", "\\-") +
			"]";
		allMarksPat = Pattern.compile( pat );
		
		// Initialize the map for Traditional/Simplified Chinese conversion.
		t2s = new Hashtable<Character, Character>();
		s2t = new Hashtable<Character, Character>();
		String schars = ChineseLanguageConstants.SIMPLIFIED_CHARS;
		String tchars = ChineseLanguageConstants.TRADITIONAL_CHARS;
		for (int i = 0; i < schars.length(); i++) {
		  t2s.put(tchars.charAt(i), schars.charAt(i));
		  s2t.put(schars.charAt(i), tchars.charAt(i));
		}
	}
	
	/**
	 * Remove extra spaces, which means more than one continuous spaces will be
	 * reduced to one space. Space here is not limited to the %32 white space 
	 * character, it also includes TAB, space in Chinese full width letter 
	 * and other special characters which appear as a white space. Spaces in the 
	 * begin or end of a line are all removed.
	 * @param text
	 * @return
	 */
	public static String removeExtraSpaces( String text ) {
		text = text.replace(
				ChineseLanguageConstants.SPACE[0], 
				ChineseLanguageConstants.SPACE[1]);
		text = text.replaceAll("[ \t\u000B\u000C\u00A0\uE5F1]+", " ");
		text = text.replaceAll("(^ +)|( +$)", "");
		return text;
	}
	
	/**
	 * Remove empty lines. An empty line means a line ends in \n or \r\n and
	 * contains only white space characters, or no characters at all.
	 * @param text
	 * @return
	 */
	public static String removeEmptyLines ( String text ) {
		text = text.replaceAll("^[ " +
		    ChineseLanguageConstants.SPACE[0] +
		    "\t\u000B\u000C\u00A0\uE5F1\r\n]*\n", "");
		text = text.replaceAll("[\r\n][ " +
        ChineseLanguageConstants.SPACE[0] +
        "\t\u000B\u000C\u00A0\uE5F1\r\n]*\n", "\n");
		return text;
	}
	
	/**
	 * Remove all punctuation marks in the text, and replace them with a ' '.
	 * @param text
	 * @return
	 */
	public static String removePunctuationMarks (String text) {
		return removeExtraSpaces(allMarksPat.matcher(text).replaceAll(" "));
	}
	
	public static String removePunctuationMarksExcept(
	    String text, String exception) {
	  String pat = "[" +
    ChineseLanguageConstants.ALL_MARKS[0].replaceAll(exception, "") + 
    ChineseLanguageConstants.ALL_MARKS[1].replaceAll(exception, "").
      replace("\\", "\\\\").replace("]","\\]").
      replace("[", "\\[").replace("-", "\\-") +
    "]";
	  Pattern toRemove = Pattern.compile(pat);
	  return toRemove.matcher(text).replaceAll(" ");
	}
	
	/**
	 * Convert full-width letters in Chinese fonts to normal half-width letters
	 * in ANSI charset. Numbers are not touched.
	 * @param text
	 * @return
	 */
	public static String mapFullWidthLetterToHalfWidth(String text) {
		char [] buf = new char [text.length()];
		text.getChars(0, text.length(), buf, 0);
		
		for ( int i = 0 ; i < buf.length ; i++ ) {
			switch ( buf[i] ) {
			case 'Ａ' :
				buf[i] = 'A';
				break;
			case 'Ｂ' :
				buf[i] = 'B';
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
	
	/**
	 * Convert full-width numbers in Chinese fonts to normal half-width numbers
	 * in ANSI charset. 
	 */
	public static String mapFullWidthNumberToHalfWidth(String text){
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
	
	/**
	 * Convert Chinese full-width punctuation marks to corresponding ANSI marks.
	 * @param text
	 * @return
	 */
	public static String mapChineseMarksToAnsi( String text ) {
		char [] buf = text.toCharArray();
		
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
	
	/**
	 * Remove all line-ends like '\r\n' or '\n', make sure the returned text
	 * contains only one line.
	 * @param text
	 * @return
	 */
	public static String removeLineEnds ( String text ) {
		return text.replaceAll("[\r\n]+", " ").trim();
	}
	
  /**
   * Use code point of a character to decide if it is a Chinese character 
   * @param codePoint
   * @return
   */
  public static boolean isChinese( int codePoint ) {
    return codePoint >= ChineseLanguageConstants.CHINESE_START && 
      codePoint <= ChineseLanguageConstants.CHINESE_END;
  }
  
  /**
   * Convert traditional Chinese text to simplified Chinese text.
   * @param text
   * @return
   */
  public static String T2S(String text) {
    char [] chars = text.toCharArray();
    for (int i = 0; i < chars.length; i++) {
      Character replacement = t2s.get(chars[i]);
      if (replacement != null) {
        chars[i] = replacement;
      }
    }
    return new String(chars);
  }
  
  /**
   * Convert simplified Chinese text to traditional Chinese text.
   * @param text
   * @return
   */
  public static String S2T(String text) {
    char [] chars = text.toCharArray();
    for (int i = 0; i < chars.length; i++) {
      Character replacement = s2t.get(chars[i]);
      if (replacement != null) {
        chars[i] = replacement;
      }
    }
    return new String(chars);
  }
}
