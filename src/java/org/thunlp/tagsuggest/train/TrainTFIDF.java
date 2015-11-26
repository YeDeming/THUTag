package org.thunlp.tagsuggest.train;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import org.thunlp.misc.Flags;
import org.thunlp.tagsuggest.common.ConfigIO;
import org.thunlp.tagsuggest.common.ModelTrainer;
import org.thunlp.tagsuggest.common.WordFeatureExtractor;
import org.thunlp.text.Lexicon;
import org.thunlp.tool.GenericTool;

public class TrainTFIDF implements GenericTool, ModelTrainer {

  @Override
  public void run(String[] args) throws Exception {
    Flags flags = new Flags();
    flags.add("input");
    flags.add("output");
    flags.add("config");
    flags.parseAndCheck(args);
    
    Properties config = ConfigIO.configFromString(flags.getString("config"));
    train(flags.getString("input"), flags.getString("output"), config);
  }

  @Override
  public void train(String inputPath, String modelPath, Properties config)
      throws IOException {
	File modelPathFile = new File(modelPath);
	if (!modelPathFile.exists()) {
		modelPathFile.mkdir();
	}
	
    Lexicon wordlex = new Lexicon();
    Lexicon taglex = new Lexicon();
    WordFeatureExtractor.buildLexicons(inputPath, wordlex, taglex, config);
    wordlex.saveToFile(new File(modelPath+ "/wordlex"));
  }
	public static void main(String[] args) throws IOException {
	
	}
  
}
