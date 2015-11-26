package org.thunlp.tagsuggest.common;

import java.io.IOException;
import java.util.Properties;

/**
 * Train a tag suggestion model.
 * @author sixiance
 *
 */
public interface ModelTrainer {
  /**
   * The train interface. To use the cross-validation framework, a model trainer
   * should accept the "fold=?" config properties.
   * @param inputPath
   * @param modelPath
   * @param config
   * @throws IOException
   */
  public void train(String inputPath, String modelPath, Properties config)
    throws IOException;
}
