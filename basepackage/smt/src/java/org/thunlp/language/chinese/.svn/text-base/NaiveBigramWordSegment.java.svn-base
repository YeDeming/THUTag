package org.thunlp.language.chinese;

import java.util.ArrayList;
import java.util.List;

public class NaiveBigramWordSegment implements WordSegment {
	private List<String> segs = new ArrayList<String>();
	
	public boolean outputPosTag() {
		return false;
	}

	public String[] segment(String text) {
		int nbigrams =  text.length() - 1;
		String [] segs = new String[nbigrams];
		for ( int i = 0 ; i < nbigrams ; i++ ) {
			segs[i] = text.substring(i, i+2);
		}
		return segs;
	}

}
