package org.thunlp.text.classifiers;

import java.io.File;
import java.io.IOException;
import java.util.Hashtable;

import org.thunlp.io.TextFileReader;

public class EnglishTextClassifierTester {

	public static void main(String[] args) throws IOException {
		if (args.length != 3) {
			System.out.println("Usage : <featureSize> <TrainingPlainFile> <TestPlainFile>");
			return;
		}
		TextFileReader reader = new TextFileReader(args[1]);
		String temp;
		Hashtable<String, Integer> categoryToInt = new Hashtable<String, Integer>();
		String[] categoryList = { "alt.atheism", "comp.graphics", "comp.os.ms-windows.misc", "comp.sys.ibm.pc.hardware",
				"comp.sys.mac.hardware", "comp.windows.x", "misc.forsale", "rec.autos", "rec.motorcycles",
				"rec.sport.baseball", "rec.sport.hockey", "sci.crypt", "sci.electronics", "sci.med", "sci.space",
				"soc.religion.christian", "talk.politics.guns", "talk.politics.mideast", "talk.politics.misc",
				"talk.religion.misc" };

		EnglishTextClassifier etc = new EnglishTextClassifier(categoryList.length);
		etc.setMaxFeatures(Integer.parseInt(args[0]));
		for (int i = 0; i < categoryList.length; i++) {
			categoryToInt.put(categoryList[i], i);
		}

		int count = 0;
		int index;
		int label;
		String content;
		while ((temp = reader.readLine()) != null) {
			count++;
			index = temp.indexOf("\t");
			if (!categoryToInt.containsKey(temp.subSequence(0, index))) {
				System.err.println("Line " + count + " wrong, please check");
				continue;
			} else {
				label = categoryToInt.get(temp.subSequence(0, index));
				content = temp.substring(index);
				etc.addTrainingText(content, label);
			}
			if (count % 1000 == 0)
				System.err.println(count);
		}
		reader.close();

		// 这段是用来检验lexicon和李景阳的程序是否一致的临时代码
		// etc.lexicon.saveToFile(new File("tmplex"));
		// if ( true )
		// return;

		etc.train();

		// etc.saveModel("model");

		count = 0;
		int right = 0;
		reader = new TextFileReader(args[2]);
		while ((temp = reader.readLine()) != null) {
			count++;
			index = temp.indexOf("\t");
			if (!categoryToInt.containsKey(temp.subSequence(0, index))) {
				System.err.println("Line " + count + " wrong, please check");
				continue;
			} else {
				label = categoryToInt.get(temp.subSequence(0, index));
				content = temp.substring(index);
				ClassifyResult cr = etc.classify(content);
				if (cr.label == label)
					right++;
			}
			if (count % 1000 == 0)
				System.err.println(count);
		}
		reader.close();

		System.out.println("Classification Result : " + (double) right / count);

	}

}
