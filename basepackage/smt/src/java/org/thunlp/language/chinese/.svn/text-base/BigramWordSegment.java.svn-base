package org.thunlp.language.chinese;

import java.util.ArrayList;
import java.util.List;

public class BigramWordSegment implements WordSegment {
	private List<String> segs = new ArrayList<String>();
	
	public boolean outputPosTag() {
		return false;
	}

	public String[] segment(String text) {
		int start, end;
		
		start = 0;
		end = 0;
		segs.clear();
		boolean precedentByChinese = false;
		while ( end < text.length() ) {
			if ( Character.isSpaceChar(text.charAt(end)) ) {
				segs.add(text.substring(start, end));
				while ( end < text.length() && Character.isSpaceChar( text.charAt(end) ) )
						end++;
				if ( end >= text.length() )
					break;
				start = end;
			}
			if ( LangUtils.isChinese(text.codePointAt(end)) ) {
				if ( end > start ) {
					segs.add(text.substring(start, end));
					precedentByChinese = false;
				} else {
					precedentByChinese = true;
				}
				start = end;
				if ( start < text.length() - 1 && 
				    LangUtils.isChinese(text.codePointAt(start + 1)) ) {					
					segs.add(text.substring(start, start+2));
					end = ++start;
				} else {
					if ( ! precedentByChinese ) {
						segs.add(text.substring(start, start + 1));
					}
					end = ++start;
				}
			} else {
				end ++;
			}
		}
		return segs.toArray(new String[segs.size()]);
	}

}
