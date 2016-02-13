package org.thunlp.text.classifiers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.Hashtable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.thunlp.io.TextFileReader;
import org.thunlp.language.chinese.NaiveBigramWordSegment;

import junit.framework.Assert;
import junit.framework.TestCase;

public class ChineseTextClassifierTester {

	static BigramChineseTextClassifier tc;

	static String categoryList[] = { "财政税收金融价格", "大气海洋水文科学", "地理学", "地质学", "电工", "电影", "电子学与计算机", "法学", "纺织",
			"固体地球物理学测绘学空间科学", "航空航天", "化工", "化学", "环境科学", "机械工程", "建筑园林城市规划", "交通", "教育", "经济学", "军事", "考古学", "矿冶",
			"力学", "美术", "民族", "农业", "轻工", "社会学", "生物学", "世界地理", "数学", "水利", "体育", "天文学", "图书馆情报档案", "土木工程", "外国历史",
			"外国文学", "文物博物馆", "物理学", "戏剧", "戏曲曲艺", "现代医学", "新闻出版", "心理学", "音乐舞蹈", "语言文字", "哲学", "政治学", "中国传统医学", "中国地理",
			"中国历史", "中国文学", "自动控制与系统工程", "宗教" };

	static int index = 0;

	static String trainingPath;

	static String testingPath;

	static double predict[] = new double[55];

	static double answer[] = new double[55];

	static double correct[] = new double[55];

	static PrintStream out = System.out;

	static Hashtable<String, Integer> categoryToInt;

	static int trainingPathIndex = -1;

	static int testingPathIndex = -1;

	public static double average(double array[]) {
		double sum = 0;
		for (int i = 0; i < array.length; i++)
			sum += array[i];
		return sum / array.length;
	}

	public static void addfiles(String filename) throws IOException {
		File file = new File(filename);
		File[] listFiles = file.listFiles();
		for (int i = 0; i < listFiles.length; i++) {
			if (listFiles[i].isFile()) {
				index++;
				String path = listFiles[i].getAbsolutePath();
				String split[] = path.split("/");
				if (trainingPathIndex == -1) {
					for (int ii = 0; ii < split.length; ii++)
						if (categoryToInt.containsKey(split[ii])) {
							trainingPathIndex = ii;
						}
				}
				String label = split[trainingPathIndex];
				String content = TextFileReader.readAll(listFiles[i].getAbsolutePath(), "GB18030");
				int labelInt = categoryToInt.get(label);
				if (labelInt < 0) {
					System.out.println("Can not map " + listFiles[i] + "to any category");
					continue;
				} else {
					if (index % 100 == 0)
						System.err.println(
								index + " " + (System.currentTimeMillis() / 1000) + " " + tc.lexicon.getSize());
					tc.addTrainingText(content, labelInt);
				}
			} else {
				addfiles(listFiles[i].getAbsolutePath());
			}
		}
	}

	public static void testfiles(String filename) throws IOException {
		File file = new File(filename);
		File[] listFiles = file.listFiles();
		for (int i = 0; i < listFiles.length; i++) {
			if (listFiles[i].isFile()) {
				index++;
				String path = listFiles[i].getAbsolutePath();
				String split[] = path.split("/");
				if (testingPathIndex == -1) {
					for (int ii = 0; ii < split.length; ii++)
						if (categoryToInt.containsKey(split[ii])) {
							testingPathIndex = ii;
						}
				}
				String label = split[testingPathIndex];
				String content = TextFileReader.readAll(listFiles[i].getAbsolutePath(), "GB18030");
				int labelInt = categoryToInt.get(label);
				if (labelInt < 0) {
					System.out.println("Can not map " + listFiles[i] + "to any category");
					continue;
				} else {
					ClassifyResult cr = tc.classify(content);
					if (index % 100 == 0)
						System.err.println(index + " " + (System.currentTimeMillis() / 1000));
					predict[cr.label]++;
					answer[labelInt]++;
					if (labelInt == cr.label) {
						out.println("Right!" + path + " " + categoryList[cr.label] + " " + categoryList[labelInt]);
						correct[cr.label]++;
					} else {
						out.println("Wrong!" + path + " " + categoryList[cr.label] + " " + categoryList[labelInt]);
					}
				}
			} else {
				testfiles(listFiles[i].getAbsolutePath());
			}
		}
	}

	public static void main(String[] args) throws IOException {
		if (args.length != 4) {
			System.out.println("usage: <segmenter_type> <featureSize> <training-folder> <testing-folder> ");
			System.out.println("<segmenter_type> : 0 for NaiveSegment, 1 for AdvancedSegment");
			System.out.println("<featureSize> : Chi-Max reduced size");
			System.out.println("<training-folder> : Training folder");
			System.out.println("<testing-folder> : Test folder");
			return;
		}
		categoryToInt = new Hashtable<String, Integer>();
		for (int i = 0; i < categoryList.length; i++) {
			categoryToInt.put(categoryList[i], i);
		}

		if (Integer.parseInt(args[0]) == 1) {
			tc = new BigramChineseTextClassifier(categoryList.length);
		} else {
			System.out.println("0");
			tc = new BigramChineseTextClassifier(categoryList.length, new NaiveBigramWordSegment());
		}

		tc.setMaxFeatures(Integer.parseInt(args[1]));

		trainingPath = args[2];
		testingPath = args[3];
		addfiles(trainingPath);

		System.out.println("Begin training at " + System.currentTimeMillis());
		tc.train();
		System.out.println("model trained at " + System.currentTimeMillis());
		boolean success = tc.saveModel("bigmodel-" + (System.currentTimeMillis() / 1000));
		if (!success)
			System.out.println("Model saving failed!");

		File testingFile = new File(testingPath);
		if (!testingFile.exists()) {
			System.out.println(testingPath + " is not a directory!");
			return;
		}

		System.out.println("Begin testing at " + System.currentTimeMillis());

		index = 0;

		testfiles(testingPath);

		double precision[] = new double[55];
		double recall[] = new double[55];

		for (int i = 0; i < 55; i++) {
			if (correct[i] != 0) {
				precision[i] = correct[i] / predict[i];
				recall[i] = correct[i] / answer[i];
			} else {
				precision[i] = 0;
				recall[i] = 0;
			}
			out.println(categoryList[i] + ": " + "Precision: " + precision[i] + "Recall: " + recall[i] + "FMeasure: "
					+ 2 * precision[i] * recall[i] / (precision[i] + recall[i]));
		}
		double Macroprecision = average(precision);
		double Macrorecall = average(recall);
		out.println("[MacroAverage]: " + "Precision: " + Macroprecision + " Recall: " + Macrorecall + " FMeasure: "
				+ 2 * Macroprecision * Macrorecall / (Macroprecision + Macrorecall));
		System.out.println("Testing finished at " + System.currentTimeMillis());
	}
}
