package org.thunlp.tagsuggest.dataset;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import org.thunlp.io.JsonUtil;
import org.thunlp.tagsuggest.common.ThreeMethodKeyword;
import org.thunlp.tagsuggest.common.ThreeMethodTest;

public class ManualTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		if (args.length != 2) {
			System.out.println("Usage:\njava -jar ManualTest.jar limit <output_dir>");
			return;
		}
		double limit = Double.parseDouble(args[0]);
		File file = new File(args[1]);
		if(!file.exists()){
			file.mkdir();
		}
		BufferedWriter out = null;
		BufferedReader countInput = new BufferedReader(new InputStreamReader(
				new FileInputStream("pickup.txt")));
		JsonUtil J = new JsonUtil();
		Set<Integer> lines = new HashSet<Integer>();
		String line;
		int counter = 0;
		while((line = countInput.readLine()) != null){
			String[] words = line.split(" ");
			double num = Double.parseDouble(words[2]);
			if(num <= limit){
				int lineNum = Integer.parseInt(words[0]); 
				lines.add(lineNum);
				counter++;
				if(counter > 100){
					break;
				}
			}
		}
		BufferedReader input = new BufferedReader(new InputStreamReader(
				new FileInputStream("test.txt")));
		while((line = input.readLine()) != null){
			ThreeMethodKeyword key = J.fromJson(line, ThreeMethodKeyword.class);
			if(lines.contains(Integer.parseInt(key.getId()))){
				out = new BufferedWriter(new OutputStreamWriter(
						new FileOutputStream(file.getAbsolutePath() + File.separator + key.getId())));
				out.write("Title:"+key.getTitle());
				out.newLine();
				out.flush();
				
				out.write("Summary:"+key.getSummary());
				out.newLine();
				out.flush();
				
				out.write("Content:\n"+key.getContent());
				out.newLine();
				out.flush();
				
				for(String word : key.getSuggest()){
					out.write(word);
					out.newLine();
					out.flush();
				}
			}
		}
		out.close();
	}

}
