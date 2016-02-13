package org.thunlp.text.classifiers;

import org.thunlp.language.chinese.WordSegment;
import org.thunlp.language.english.EnglishWordSegment;

public class EnglishTextClassifier extends AbstractTextClassifier {

	public EnglishTextClassifier(int nclasses) {
		super(nclasses);
	}

	@Override
	protected WordSegment initWordSegment() {
		// TODO Auto-generated method stub
		return new EnglishWordSegment(); // 分词程序;

		// 如下是处理20新闻组用的分词方法
		// return new WordSegment() {
		//
		// public boolean outputPosTag() {
		// // TODO Auto-generated method stub
		// return false;
		// }
		//
		// public String[] segment(String text) {
		// return text.split(" +");
		// }
		//
		// };
	}

}
