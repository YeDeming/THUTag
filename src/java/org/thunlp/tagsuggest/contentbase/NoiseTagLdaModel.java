package org.thunlp.tagsuggest.contentbase;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.Map.Entry;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.thunlp.misc.WeightString;

/**
 * This is a LDA model. It holds all parameters, namely the n(w,z) matrix. It is
 * able to do inference. It can be serialized through input/output streams. It
 * uses GibbsSampling as the inference algorithm.
 * @author sixiance
 */
public class NoiseTagLdaModel {
  private static Logger LOG = Logger.getAnonymousLogger();
  public static int NO_TOPIC = -2;

  //////////////////////////////////////////////////////////////////////////////
  // Hyper-parameters and model controlling variables.
  protected double alpha = 0;
  protected double beta = 0;
  protected double eta = 0;
  protected int numTopics = 0;
  protected boolean locked = false;

  //////////////////////////////////////////////////////////////////////////////
  // The model parameters.
  protected Map<String, int []> nwz = new Hashtable<String, int []>();
  protected Map<String, int []> ntz = new Hashtable<String, int []>();
  protected int [] wnz = null;
  protected int [] tnz = null;
  protected int nw = 0;
  protected int nt = 0;
  protected int noise;

  //////////////////////////////////////////////////////////////////////////////
  // Internal helpers.
  protected Random random = new Random();

  //////////////////////////////////////////////////////////////////////////////
  // Constructor and model I/O.

  /**
   * Initialize a new model with given number of topics.
   * @param numTopics
   */
  public NoiseTagLdaModel(int numTopics) {
    alpha = 50.0 / numTopics;
    beta = 0.01;
    eta = 1.0;
    locked = false;
    this.numTopics = numTopics;
    wnz = new int[numTopics];
    tnz = new int[numTopics + 1];
    Arrays.fill(wnz, 0);
    Arrays.fill(tnz, 0);
    noise = numTopics;
  }

  /**
   * Load an existing model.
   * @param in
   * @throws IOException
   */
  public NoiseTagLdaModel(InputStream in) throws IOException {
    GZIPInputStream zipin = new GZIPInputStream(in);
    BufferedInputStream bin = new BufferedInputStream(zipin);
    DataInputStream datain = new DataInputStream(bin);
    numTopics = datain.readInt();
    int numWords = datain.readInt();
    alpha = datain.readDouble();
    beta = datain.readDouble();
    locked = datain.readBoolean();
    wnz = new int[numTopics];

    Arrays.fill(wnz, 0);
    for (int i = 0; i < numWords; i++) {
      String word = datain.readUTF();
      int [] count = new int[numTopics];
      for (int j = 0; j < numTopics; j++) {
        count[j] = datain.readInt();
        wnz[j] += count[j];
        nw += count[j];
      }
      nwz.put(word, count);
    }
    tnz = new int[numTopics + 1];
    Arrays.fill(tnz, 0);
    int numTags = datain.readInt();
    eta = datain.readDouble();
    for (int i = 0; i < numTags; i++) {
      String word = datain.readUTF();
      int [] count = new int[numTopics + 1];
      for (int j = 0; j < numTopics + 1; j++) {
        count[j] = datain.readInt();
        tnz[j] += count[j];
        nt += count[j];
      }
      ntz.put(word, count);
    }
    noise = numTopics;
  }

  /**
   * Save the current model to the output stream.
   * @param out
   * @throws IOException
   */
  public void saveModel(OutputStream out) throws IOException {
    GZIPOutputStream zipout = new GZIPOutputStream(out);
    DataOutputStream dataout = new DataOutputStream(zipout);
    dataout.writeInt(numTopics);
    dataout.writeInt(nwz.size());
    dataout.writeDouble(alpha);
    dataout.writeDouble(beta);
    dataout.writeBoolean(locked);
    for (Entry<String, int []> e : nwz.entrySet()) {
      dataout.writeUTF(e.getKey());
      int [] c = e.getValue();
      for (int i = 0; i < numTopics; i++) {
        dataout.writeInt(c[i]);
      }
    }
    dataout.writeInt(ntz.size());
    dataout.writeDouble(eta);
    for (Entry<String, int []> e : ntz.entrySet()) {
      dataout.writeUTF(e.getKey());
      int [] c = e.getValue();
      for (int i = 0; i < numTopics + 1; i++) {
        dataout.writeInt(c[i]);
      }
    }
    dataout.flush();
    zipout.finish();
  }

  //////////////////////////////////////////////////////////////////////////////
  // Setters and getters for hyper-parameters and controlling variables.
  public double getAlpha() {
    return alpha;
  }

  public double getBeta() {
    return beta;
  }

  public void setAlpha(double v) {
    if (v < 0)
      v = 50.0 / numTopics;
    alpha = v;
  }

  public void setBeta(double v) {
    if (v < 0)
      v = 0.01;
    beta = v;
  }

  public int getNumTopics() {
    return numTopics;
  }

  public int getNumWords() {
    return nwz.size();
  }

  public Set<String> getAllWords() {
    return nwz.keySet();
  }

  /**
   * Lock the model, then inference won't change the parameter of the model.
   */
  public void setLocked(boolean b) {
    locked = b;
  }

  public boolean getLocked() {
    return locked;
  }

  public static class Document implements Serializable{
    public Document(String [] words, String [] tags) {
      this.words = words;
      this.topics = new int[words.length];
      this.tags = tags;
      this.reasons = new int[tags.length];
    }

    String [] words;
    int [] topics;
    String [] tags;
    int [] reasons;
  }

  //////////////////////////////////////////////////////////////////////////////
  // Training.
  public void train(List<Document> docs, int numIterations) {
    // Training for 100 iterations.
    setLocked(false);
    double [] pzd = new double[numTopics];
    for (int i = 0; i < numIterations; i++) {
      double loglikelihood = 0;
      for (Document d : docs) {
        if (i == 0) {
          initializeTopics(d);
        } else {
          loglikelihood += 
            inferenceByGibbsSampling(d, pzd, 0, 1);
        } 
      }
      LOG.info(i + " LL: " + loglikelihood +
          " NR: " + pnoise() );
    }
    setLocked(true);
  }

  //////////////////////////////////////////////////////////////////////////////
  // Inference methods.

  public void initializeTopics(Document d) {
    // Initialize the topic vector.
    for (int i = 0; i < d.words.length; i++) {
      if (nwz.containsKey(d.words[i]) || !locked) {
        d.topics[i] = random.nextInt(numTopics);
        updateCounts(d.words[i], d.topics[i], 1);
      } else {
        d.topics[i] = NO_TOPIC;
      }
    }
    for (int i = 0; i < d.tags.length; i++) {
      if (nwz.containsKey(d.tags[i]) || !locked) {
        d.reasons[i] = random.nextInt(numTopics + 1);
        updateTagCounts(d.tags[i], d.reasons[i], 1);
      } else {
        d.reasons[i] = NO_TOPIC;
      }
    }
  }

  /**
   * Inference with the given document. Given the input document, which is a
   * list of words, the method outputs the assignment of topics, the document's
   * aggregated distribution over topics, and return the log-likelihood.
   * 
   * If the model is unlocked, which means in training mode, we will use the
   * values in []topics to initialize the model parameters. If any element in
   * []topics is less than 0, we help fill the []topics with random topics.
   * If the model is locked, we will not update the model parameters with the
   * content of []topics.
   * 
   * @param words The input document.
   * @param topics The topic assignment, size >= number of words.
   * @param pzd The document's distribution over topics.
   * @param numBurnIn The number of burn-in iterations.
   * @param numSampling The number of actual sampling iterations.
   * @return The likelihood.
   */
  public double inference(Document d, double [] pzd) {
    initializeTopics(d);
    return inferenceByGibbsSampling(d, pzd, 30, 10);
  }

  /**
   * Inference by Gibbs Sampling.
   * @param words The input document.
   * @param topics The topic assignment, size >= number of words.
   * @param pzd The document's distribution over topics.
   * @param numBurnIn The number of burn-in iterations.
   * @param numSampling The number of actual sampling iterations.
   * @return The likelihood.
   */
  public double inferenceByGibbsSampling(
      Document d,
      double [] pzd, 
      int numBurnIn,
      int numSampling) {
    Arrays.fill(pzd, 0);

    // Sampling.
    double loglikelihood = 0.0;
    for (int i = 0; i < numBurnIn + numSampling; i++) {
      loglikelihood += gibbsSampling(d);
      if (i >= numBurnIn) {
        for (int topic : d.topics) {
          if (topic != NO_TOPIC)
            pzd[topic]++;
        }
      } else {
        loglikelihood = 0;
      }
    }

    // Normalize pzd.
    normalize(pzd, alpha);

    // Get mean log-likelihood.
    return loglikelihood / numSampling;
  }

  //////////////////////////////////////////////////////////////////////////////
  // Getting more detail about the probabilities.
  public double pwz(String word, int z) {
    int [] n = nwz.get(word);
    if (n == null)
      return 0;
    else
      return (n[z] + beta) / (wnz[z] + nwz.size() * beta);
  }

  public void pwz(String word, double [] p) {
    int [] n = nwz.get(word);
    if (n == null)
      Arrays.fill(p, 1.0 / p.length);
    else {
      for (int i = 0; i < numTopics; i++)
        p[i] = (n[i] + beta) / (wnz[i] + nwz.size() * beta);
    }
  }

  public double pz(int z) {
    return (wnz[z] + alpha) / (nw + numTopics * alpha);
  }

  public void pz(double [] p) {
    for (int i = 0; i < numTopics; i++)
      p[i] = pz(i);
  }

  public double pw(String word) {
    int [] n = nwz.get(word);
    if (n == null)
      return 0;
    else {
      double p = 0;
      for (int i = 0; i < numTopics; i++)
        p += n[i];
      return (p + beta) / (nw + nwz.size() * beta);
    }
  }

  public double pnoise() {
    return (tnz[noise] + eta) / (nt + 2*eta) ;
  }

  public void ptz(String tag, double [] p) {
    int [] n = ntz.get(tag);
    if (n == null)
      Arrays.fill(p, 1.0 / p.length);
    else {
      for (int i = 0; i < numTopics + 1; i++)
        p[i] = (n[i] + beta) / (tnz[i] + ntz.size() * beta);
    }
  }

  public Set<String> tags() {
    return ntz.keySet();
  }

  //////////////////////////////////////////////////////////////////////////////
  // Internals.
  protected int [] updateCounts(String word, int topic, int delta) {
    int [] counts = nwz.get(word);
    if (!locked) { 
      if (counts == null) {
        counts = new int[numTopics];
        Arrays.fill(counts, 0);
        nwz.put(word, counts);
      }
      counts[topic] += delta;
      wnz[topic] += delta;
      nw += delta;
    }
    return counts;
  }

  protected int [] updateTagCounts(String tag, int topic, int delta) {
    int [] counts = ntz.get(tag);
    if (!locked) { 
      if (counts == null) {
        counts = new int[numTopics + 1];
        Arrays.fill(counts, 0);
        ntz.put(tag, counts);
      }
      counts[topic] += delta;
      tnz[topic] += delta;
      nt += delta;
    }
    return counts;
  }

  protected double gibbsSampling(Document d) {
    double loglikelihood = 0;
    int [] nzd = new int[numTopics];
    double [] p = new double[numTopics];
    Arrays.fill(nzd, 0);
    for (int i = 0; i < d.words.length; i++) {
      if (d.topics[i] != NO_TOPIC)
        nzd[d.topics[i]]++;
    }
    // Sample word-topic allocation.
    for (int i = 0; i < d.words.length; i++) {
      if (d.topics[i] == NO_TOPIC)
        continue;
      updateCounts(d.words[i], d.topics[i], -1);
      nzd[d.topics[i]]--;
      int [] nwzi = nwz.get(d.words[i]);
      for (int z = 0; z < numTopics; z++) {
        double pwz = (nwzi[z] + beta) / (wnz[z] + nwz.size() * beta);
        double pzd = (nzd[z] + alpha) / (d.words.length + numTopics * alpha);
        p[z] = pwz * pzd;
        checkProb(pwz);
        checkProb(pzd);
        checkProb(p[z]);
      }
      int newTopic = sampleBy(p);
      d.topics[i] = newTopic;
      updateCounts(d.words[i], d.topics[i], 1);
      nzd[d.topics[i]]++;
      loglikelihood += Math.log(p[newTopic]);
    }
    // Sample tag-topic allocation.
    double [] pz = new double[numTopics + 1];
    for (int i = 0; i < d.tags.length; i++) {
      if (d.reasons[i] == NO_TOPIC)
        continue;
      updateTagCounts(d.tags[i], d.reasons[i], -1);
      ptz(d.tags[i], pz);
      //double sumNonNoise = 0;
      //int sumNzd = 0;
      for (int z = 0; z < numTopics; z++) {
        // double pzd = (nzd[z] + alpha) / (d.words.length + numTopics * alpha);
        double pzd = (double)nzd[z] / (double)d.words.length;
        //sumNzd += nzd[z];
        pz[z] *= (1.0 - pnoise()) * pzd;
        //sumNonNoise += pz[z];
        //checkProb(pz[z]);
      }
      pz[noise] *= pnoise();
      //checkProb(pz[noise]);
      //LOG.info("non-noise: " + sumNonNoise + " nzd:" + sumNzd + " noise:" + pz[noise]);
      int newTopic = sampleBy(pz);
      d.reasons[i] = newTopic;
      updateTagCounts(d.tags[i], d.reasons[i], 1);
      loglikelihood += Math.log(pz[newTopic]);
    }

    return loglikelihood;
  }

  public void checkProb(double p) {
    if (Double.isNaN(p)) {
      throw new RuntimeException(" p= NAN");
    }
    if (p < 0 || p > 1) {
      throw new RuntimeException(" p=" + p);
    }
  }

  protected int sampleBy(double [] p) {
    double sum = 0;
    for (double v : p) {
      sum += v;
    }
    double sample = random.nextDouble() * sum;
    sum = 0;
    int result = 0;
    for (int i = 0; i < p.length; i++) {
      sum += p[i];
      if (sum >= sample) {
        result = i;
        break;
      }
    }
    return result;
  }

  protected void normalize(double [] a, double smoother) {
    double norm = 0;
    for (double d : a) {
      norm += d + smoother;
    }
    for (int i = 0; i < a.length; i++)
      a[i] = (a[i] + smoother) / norm;
  }

  /**
   * Render the model in HTML code.
   */
  public String toString() {
    StringBuilder html = new StringBuilder();
    ArrayList<WeightString> tokens = new ArrayList<WeightString>();
    html.append("<html>");
    html.append("<style>body {font-family:Consolas;}");
    html.append("span {margin-right:3px}</style>");
    Comparator<WeightString> cmp = new Comparator<WeightString>() {
      @Override
      public int compare(WeightString o1, WeightString o2) {
        return Double.compare(o2.weight, o1.weight);
      }
    };
    for (int i = 0; i < numTopics + 1; i++) {
      html.append("<div><span>" + i + ":</span>");
      // Render words per topics.
      double max = 0;
      if (i < numTopics) {
        tokens.clear();
        for (Entry<String, int []> e : nwz.entrySet()) {
          int [] counts = e.getValue();
          double pwz = (double)counts[i] / (double)wnz[i];
          tokens.add(new WeightString(e.getKey(), pwz));
        }
        Collections.sort(tokens, cmp);
        max = 0;
        html.append("<span>WORDS:</span>");
        for (int k = 0; k < 5 && k < tokens.size(); k++) {
          WeightString ws = tokens.get(k);
          if (ws.weight > max) {
            max = ws.weight;
          }
          html.append("<span style='"+ getWeightedStyle(ws.weight/max) + "'>");
          html.append(tokens.get(k).text);
          html.append("</span>");
        }
      }
      // Render tags per topics.
      tokens.clear();
      for (Entry<String, int []> e : ntz.entrySet()) {
        int [] counts = e.getValue();
        double ptz = (double)counts[i] / (double)tnz[i];
        tokens.add(new WeightString(e.getKey(), ptz));
      }
      Collections.sort(tokens, cmp);
      max = 0;
      html.append("<span>TAGS:</span>");
      for (int k = 0; k < 5 && k < tokens.size(); k++) {
        WeightString ws = tokens.get(k);
        if (ws.weight > max) {
          max = ws.weight;
        }
        html.append("<span style='"+ getWeightedStyle(ws.weight/max) + "'>");
        html.append(tokens.get(k).text);
        html.append(String.format(" %.3f", ws.weight/max));
        html.append("</span>");
      }
      html.append("</div>");
    }
    html.append("</html>");
    return html.toString();
  }
  
  private String getWeightedStyle(double weight) {
    if (weight > 1)
      weight = 1;
    if (weight < 0)
      weight = 0;
    int background = (int)((1.0 - weight) * 255.0);
    int foreground = weight > 0.1 ? 255 : 178;
    StringBuilder sb = new StringBuilder();
    sb.append("background-color:rgb(");
    sb.append(Integer.toString(background));
    sb.append(",");
    sb.append(Integer.toString(background));
    sb.append(",");
    sb.append(Integer.toString(background));
    sb.append(");color:rgb(");
    sb.append(Integer.toString(foreground));
    sb.append(",");
    sb.append(Integer.toString(foreground));
    sb.append(",");
    sb.append(Integer.toString(foreground));
    sb.append(")");
    return sb.toString();
  }
}
