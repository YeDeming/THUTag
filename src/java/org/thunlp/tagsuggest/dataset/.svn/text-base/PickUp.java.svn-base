package org.thunlp.tagsuggest.dataset;
import java.util.List;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

import org.thunlp.misc.WeightString;


public class PickUp {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception{
		// TODO Auto-generated method stub
		if(args.length != 2){
			System.out.println("Usage:\njava -jar PickUp.jar <input_dir> <your_output_file>");
			return;
		}
		
		BufferedReader inputCount = new BufferedReader(new InputStreamReader(new FileInputStream(args[0]+"cut.gzG")));
		String line;
		HashMap<String, Double> countMap = new HashMap<String, Double>();
		while((line = inputCount.readLine()) != null){
			String words[] = line.split(" ");
			countMap.put(words[0], Double.parseDouble(words[1]));
		}
		
		BufferedReader input = null;
		List<WeightString> tags = new ArrayList<WeightString>();
		for(int i = 0 ; i < 5; i ++){
			input = new BufferedReader(new InputStreamReader(new FileInputStream(args[0]+"cut.gzF"+i)));
			while((line = input.readLine()) != null){
				String words[] = line.split(" ");
				tags.add(new WeightString(words[0], Double.parseDouble(words[1])));
			}
		}
		Collections.sort(tags, new Comparator<WeightString>() {
			@Override
			public int compare(WeightString o1, WeightString o2) {
				return Double.compare(o2.weight, o1.weight);
			}
		});
		BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(args[1])));
		for(int i = 0; i < tags.size(); i ++){
			out.write(tags.get(i).text + " " + tags.get(i).weight + " "+countMap.get(tags.get(i).text));
			out.newLine();
			out.flush();
		}
		out.close();
	}
}
