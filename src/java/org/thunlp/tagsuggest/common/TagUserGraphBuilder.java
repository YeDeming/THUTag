package org.thunlp.tagsuggest.common;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Map.Entry;
import java.util.logging.Logger;

import org.thunlp.io.GzipTextFileReader;
import org.thunlp.io.GzipTextFileWriter;
import org.thunlp.io.JsonUtil;
import org.thunlp.misc.Counter;
import org.thunlp.misc.Flags;
import org.thunlp.misc.SlidingWindowCounter;
import org.thunlp.misc.StringPair;
import org.thunlp.tagsuggest.common.ConfigIO;
import org.thunlp.tagsuggest.common.Post;
import org.thunlp.tool.GenericTool;
import org.thunlp.tool.StringUtil;

/**
 * This tool build a tag-user graph G(V,E). V is the set of nodes, including
 * tags and users. E is the set of edges, the weight of edges are co-occurrence
 * counts.
 * @author sixiance
 *
 */
public class TagUserGraphBuilder implements GenericTool {
  JsonUtil J = new JsonUtil();
  private static Logger LOG = Logger.getAnonymousLogger();
  private Properties config;
  private String fold;
  
  public static class Node {
    public static int TYPE_TAG = 0;
    public static int TYPE_USER = 1;
    private String name;
    private int type;
    Map<Node, Double> neighbors = new Hashtable<Node, Double>();
    
    public String getName() {
      return name;
    }
    
    public void setName(String name) {
      this.name = name;
    }
    
    public int getType() {
      return type;
    }
    
    public void setType(int t) {
      this.type = t;
    }
    
    public Map<Node, Double> getNeighbors() {
      return neighbors;
    }
    
    public void setNeighbors(Map<Node, Double> n) {
      this.neighbors = n;
    }
    
    @Override
    public boolean equals(Object o) {
      if (!(o instanceof Node))
        return false;
      Node n = (Node)o;
      return name.equals(n.name);
    }
    
    @Override
    public int hashCode() {
      return name.hashCode();
    }
  }

  @Override
  public void run(String[] args) throws Exception {
    Flags flags = new Flags();
    flags.add("input");
    flags.add("output");
    flags.add("min_count");
    flags.add("config");
    flags.parseAndCheck(args);

    File input = new File(flags.getString("input"));
    File output = new File(flags.getString("output"));
    int minCount = flags.getInt("min_count");
    Map<String, Node> graph = makeGraph(input, minCount);
    config = ConfigIO.configFromString(flags.getString("config"));
    fold = config.getProperty("fold", "");
    
    graphToFile(graph, output);
  }
  
  public void graphToFile(Map<String, Node> graph, File output)
  throws IOException {
    GzipTextFileWriter w = new GzipTextFileWriter(output);
    for (Entry<String, Node> e : graph.entrySet()) {
      List<String> l = new LinkedList<String>();
      Node n = e.getValue();
      l.add(n.getName());
      l.add(Integer.toString(n.getType()));
      for (Entry<Node, Double> nodeEntry : n.getNeighbors().entrySet()) {
        l.add(nodeEntry.getKey().getName() + ":" + nodeEntry.getValue());
      }
      w.writeLine(StringUtil.join(l, " "));
    }
    w.close();
  }

  public Map<String, Node> makeGraph(File input, int minCount)
  throws IOException {
    // Count co-occurrences.
    SlidingWindowCounter<StringPair> counter =
      new SlidingWindowCounter<StringPair>(minCount, 1000000);
    Counter<String> nodeCounter = new Counter<String>();
    countTagUser(input, counter, nodeCounter);
    
    Map<String, Node> graph = new Hashtable<String, Node>();
    for (Entry<StringPair, Long> e : counter) {
      StringPair pair = e.getKey();
      Node firstNode = addToGraphIfMiss(pair.first, graph);
      Node secondNode = addToGraphIfMiss(pair.second, graph);
      double weight = computeWeight(
          e.getValue(),
          nodeCounter.get(firstNode.getName()),
          nodeCounter.get(secondNode.getName())
      );
      firstNode.getNeighbors().put(secondNode, (double)e.getValue());
      secondNode.getNeighbors().put(firstNode, (double)e.getValue());
    }
    
    return graph;
  }
  
  private double computeWeight(double n, double na, double nb) {
    String algorithm = config.getProperty("weightby", "count");
    if (algorithm.equals("count")) {
      return n;
    } else if(algorithm.equals("mi")) {
      // Mutual information and Google Distance are to be implemented.
      /*double [] terms = {
          nwt / nd * Math.log(nwt / nw / nt * nd),
          nw == nwt ? 0 : 
            (nw - nwt) / nd * Math.log((nw - nwt) / nw / (nd - nt) * nd),
          nt == nwt ? 0 :
            (nt - nwt) / nd * Math.log((nt - nwt) / (nd - nw) / nt * nd),
          (nd-nw-nt+nwt) == 0 ? 0 :
            (nd-nw-nt+nwt)/nd * Math.log((nd-nw-nt+nwt)/(nd-nw)/(nd-nt)*nd)
      };*/
    } else if(algorithm.equals("gdis")) {
      
    }
    return n;
  }
  
  public Node addToGraphIfMiss(String name, Map<String, Node> graph) {
    Node n = graph.get(name);
    if (n == null) {
      n = new Node();
      n.setName(name);
      n.setType(name.startsWith("@") ? Node.TYPE_USER : Node.TYPE_TAG);
      graph.put(name, n);
    }
    return n;
  }

  public void countTagUser(File input,
      SlidingWindowCounter<StringPair> counter,
      Counter<String> nodeCounter) throws IOException { 
    GzipTextFileReader r = new GzipTextFileReader(input);
    String line;
    int n = 0;
    ArrayList<String> nodes = new ArrayList<String>();
    while ((line = r.readLine()) != null) {
      Post p = J.fromJson(line, Post.class);
      if (p.getExtras().equals(fold)) {
        continue;
      }
      nodes.clear();

      for (String tag : p.getTags()) {
        nodes.add(tag);
      }
      nodes.add("@" + p.getUserId());
      for (int i = 0; i < nodes.size(); i++) {
        nodeCounter.inc(nodes.get(i), 1);
        for (int j = i + 1; j < nodes.size(); j++) {
          StringPair sp = new StringPair();
          sp.first = nodes.get(i);
          sp.second = nodes.get(j);
          if (sp.first.compareTo(sp.second) < 0) {
            String tmp = sp.first;
            sp.first = sp.second;
            sp.second = tmp;
          }
          counter.inc(sp, 1);
        }
      }
      if (n % 1000 == 0) {
        LOG.info("Process " + n + " #pairs:" + counter.size());
      }
      n++;
    }
    r.close();
  }
}
