package org.thunlp.tagsuggest.evaluation;

import java.io.File;
import java.util.Properties;
import java.util.logging.Logger;

import org.thunlp.io.TextFileWriter;
import org.thunlp.misc.Flags;
import org.thunlp.tagsuggest.common.ConfigIO;
import org.thunlp.tagsuggest.common.ModelTrainer;
import org.thunlp.tagsuggest.common.TagSuggest;
import org.thunlp.tagsuggest.dataset.CutFolds;
import org.thunlp.tagsuggest.evaluation.Evaluator.Result;
import org.thunlp.tagsuggest.evaluation.EvaluatorByActual.Result2;
import org.thunlp.tool.GenericTool;

/** 
 * Perform cross-validation on given dataset with given tag suggestion algorithm
 * @author sixiance
 *
 */
public class CrossValidator implements GenericTool {
  private static Logger LOG = Logger.getAnonymousLogger();
  private Flags flags = new Flags();

  private void parseFlags(String [] args) {
    flags.add("dataset");
    flags.add("report");
    flags.add("working_dir");
    flags.add("config");
    flags.add("trainer_class");
    flags.add("suggester_class");
    flags.addWithDefaultValue("num_folds", "5", "");
    flags.addWithDefaultValue("at_n", "10", "");
    flags.parseAndCheck(args);
  }

  @Override
  public void run(String[] args) throws Exception {
    parseFlags(args);

    int numFolds = flags.getInt("num_folds");
    Properties config = ConfigIO.configFromString(flags.getString("config"));
    String workingDir = flags.getString("working_dir");
    String datasetPath = flags.getString("dataset");
    String reportPath = flags.getString("report");
    int atN = flags.getInt("at_n");
    String cutDatasetPath = workingDir + File.separator + "cut.gz";

    // Prepare trainer and suggester classes.
    String suggesterClassName = flags.getString("suggester_class");
    if (!suggesterClassName.startsWith("org.thunlp.tagsuggest.contentbase")) {
      suggesterClassName = "org.thunlp.tagsuggest.contentbase." + suggesterClassName;
    }

    String trainerName = flags.getString("trainer_class");
    if (!trainerName.startsWith("org.thunlp.tagsuggest.train")) {
      trainerName = "org.thunlp.tagsuggest.train." + trainerName;
    }
    ModelTrainer trainer = 
      (ModelTrainer) Class.forName(trainerName).newInstance();

    File workingDirFile = new File(workingDir);

    if (!workingDirFile.exists()) {
      workingDirFile.mkdir();
    }

    // Cut the dataset into given number of folds.
    if (!fileExists(cutDatasetPath)) {
      LOG.info("Cut the dataset into " + numFolds + " folds.");
      CutFolds cf = new CutFolds();
      String dataType = config.getProperty("dataType", "Post");
      LOG.info("dataType:" + dataType);
      cf.cutFolds(datasetPath, cutDatasetPath, numFolds,dataType);
    }

    // Train & test for each fold.
    Result [] results = new Result[numFolds];
    Evaluator evaluator = new Evaluator(config);
    evaluator.setMinLog(
        Double.parseDouble(config.getProperty("minlog", "-10")));
    for (int i = 0; i < numFolds; i++) {
      LOG.info("Fold " + i);
      config.setProperty("fold", Integer.toString(i));
      String modelPath = workingDir + File.separator + "model." + i + ".gz";

      if (!fileExists(modelPath)) {
        LOG.info("Training " + i);
        trainer.train(cutDatasetPath, modelPath, config);
      } else {
        LOG.info("Using existing model " + i);
      }
      LOG.info("Testing " + i);

      TagSuggest ts =
        (TagSuggest) Class.forName(suggesterClassName).newInstance();
      ts.setConfig(config);
      ts.loadModel(modelPath);

      results[i] = evaluator.evaluateSuggester(cutDatasetPath, ts, atN, i);
    }

    String report = writeReport(results);
    TextFileWriter.writeToFile(report, new File(reportPath), "UTF-8");
  }

  public boolean fileExists(String filename) {
    File file = new File(filename);
    return file.exists();
  }

  public String writeReport(Result [] results) {
    int atN = results[0].f1.length;
    Result result = new Result(atN);
    Result stdev = new Result(atN);
    // Compute mean.
    for (Result r : results) {
      for (int i = 0; i < atN; i++) {
        result.p[i] += r.p[i];
        result.r[i] += r.r[i];
        result.f1[i] += r.f1[i];
        result.record[i] += r.record[i];
      }

      result.num += r.num;
      result.suggestnum += r.suggestnum;
      result.answernum += r.answernum;
      
      result.loglikelihood += r.loglikelihood;
      result.perplexity += r.perplexity;
    }
    for (int i = 0; i < atN; i++) {
      result.p[i] /= results.length;
      result.r[i] /= results.length;
      result.f1[i] /= results.length;
    }
    
    result.loglikelihood /= results.length;
    result.perplexity /= results.length;

    // Compute standard deviation.
    for (Result r : results) {
      for (int i = 0; i < atN; i++) {
        stdev.p[i] += Math.pow(r.p[i] - result.p[i], 2); 
        stdev.r[i] += Math.pow(r.r[i] - result.r[i], 2); 
        stdev.f1[i] += Math.pow(r.f1[i] - result.f1[i], 2); 
      }
      stdev.loglikelihood += 
        Math.pow(r.loglikelihood - result.loglikelihood, 2);
      stdev.perplexity += 
        Math.pow(r.perplexity - result.perplexity, 2);
    }
    for (int i = 0; i < atN; i++) {
      stdev.p[i] = Math.sqrt(stdev.p[i] / results.length);
      stdev.r[i] = Math.sqrt(stdev.r[i] / results.length);
      stdev.f1[i] = Math.sqrt(stdev.f1[i] / results.length);
    }
    stdev.loglikelihood = Math.sqrt(stdev.loglikelihood / results.length);
    stdev.perplexity = Math.sqrt(stdev.perplexity / results.length);

    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < atN; i++) {
      sb.append(i+1);
      sb.append(" ");
      sb.append(String.format("%.5f", result.p[i]));
      sb.append(" ");
      sb.append(String.format("%.5f", stdev.p[i]));
      sb.append(" ");
      sb.append(String.format("%.5f", result.r[i]));
      sb.append(" ");
      sb.append(String.format("%.5f", stdev.r[i]));
      sb.append(" ");
      sb.append(String.format("%.5f", result.f1[i]));
      sb.append(" ");
      sb.append(String.format("%.5f", stdev.f1[i]));
      sb.append(" ");
      sb.append(String.format("%.5f", result.loglikelihood));
      sb.append(" ");
      sb.append(String.format("%.5f", stdev.loglikelihood));
      sb.append(" ");
      sb.append(String.format("%.5f", result.perplexity));
      sb.append(" ");
      sb.append(String.format("%.5f", stdev.perplexity));
      sb.append(" "+result.record[i]);
      sb.append("\n");
    }
    sb.append(result.num+" "+result.suggestnum+" "+result.answernum);
    return sb.toString();
  }

}
