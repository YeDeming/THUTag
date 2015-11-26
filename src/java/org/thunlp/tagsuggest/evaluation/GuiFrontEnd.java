package org.thunlp.tagsuggest.evaluation;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.List;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Logger;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.border.LineBorder;

import org.thunlp.html.HtmlReformatter;
import org.thunlp.misc.Flags;
import org.thunlp.misc.WeightString;
import org.thunlp.tagsuggest.common.ConfigIO;
import org.thunlp.tagsuggest.common.Post;
import org.thunlp.tagsuggest.common.TagSuggest;
import org.thunlp.tool.GenericTool;

public class GuiFrontEnd implements GenericTool, ActionListener, KeyListener {
  private static Logger LOG = Logger.getAnonymousLogger();
  JTextArea inputTA = new JTextArea();
 // JTextArea logsTA = new JTextArea();			//explain
  JTextPane tagsLabel = new JTextPane();
  TagSuggest suggester = null;
  JButton suggestBtn = new JButton("suggest");
  JButton exitBtn = new JButton("exit");
  boolean realTimeSuggestion = true;
  
  public static class SuggestTimerTask extends TimerTask {
    GuiFrontEnd parent = null;
    
    public SuggestTimerTask(GuiFrontEnd g) {
      parent = g;
    }
    
    public void run() {
      parent.doSuggest();
    }
  }
  
  /**
   * @param args
   */
  public void run(String[] args) throws Exception {
    Flags flags = new Flags();
    flags.add("algorithm");
    flags.add("model_path");
    flags.add("config");
    flags.addWithDefaultValue("realtime", "true");
    flags.parseAndCheck(args);

    realTimeSuggestion = flags.getBoolean("realtime");
    
    Properties config = ConfigIO.configFromString(flags.getString("config"));
    suggester = loadTagSuggester(
        flags.getString("algorithm"), flags.getString("model_path"));
    suggester.setConfig(config);
    JFrame mainWindow = buildMainWindow();
    mainWindow.setVisible(true);
  }
  
  private TagSuggest loadTagSuggester(String name, String modelPath) 
  throws Exception {
    if (!name.startsWith("org")) {
      name = "org.thunlp.tagsuggest.contentbase." + name;
    }
    TagSuggest ts = (TagSuggest) Class.forName(name).newInstance();
    ts.loadModel(modelPath);
    return ts;
  }
  
  private JFrame buildMainWindow() {
    JFrame mainWindow = new JFrame();
    
    inputTA.setColumns(40);
    inputTA.setRows(5);
    inputTA.setLineWrap(true);
    inputTA.setEditable(true);
    inputTA.addKeyListener(this);
    Font bigFont = new Font("Sans", Font.PLAIN, 24);
    inputTA.setFont(bigFont);
/*
    logsTA.setColumns(40);
    logsTA.setRows(5);
    logsTA.setLineWrap(true);
    logsTA.setEditable(false);
    */
    tagsLabel.setContentType("text/html");
    tagsLabel.setBackground(Color.WHITE);
    tagsLabel.setFont(bigFont);
    tagsLabel.setBorder(new LineBorder(Color.RED));
    tagsLabel.setEditable(false);
    tagsLabel.setVisible(true);
    
    JLabel instructionLabel = new JLabel();
    instructionLabel.setText("Type text below to get suggestions:");
    
    JPanel buttonsPanel = new JPanel();
    buttonsPanel.setLayout(new BoxLayout(buttonsPanel, BoxLayout.LINE_AXIS));
    buttonsPanel.add(suggestBtn);
    buttonsPanel.add(exitBtn);
    JPanel southPanel = new JPanel();
    southPanel.setLayout(new BoxLayout(southPanel, BoxLayout.PAGE_AXIS));
    southPanel.add(tagsLabel);
    southPanel.add(buttonsPanel);
    
    JPanel pagePanel = new JPanel();
    pagePanel.setLayout(new BorderLayout());
    pagePanel.add(instructionLabel, BorderLayout.NORTH);
    pagePanel.add(inputTA, BorderLayout.CENTER);
   //pagePanel.add(logsTA, BorderLayout.EAST);
    pagePanel.add(southPanel, BorderLayout.SOUTH);

    mainWindow.add(pagePanel);
    mainWindow.setSize(500, 600);
    suggestBtn.addActionListener(this);
    exitBtn.addActionListener(this);
    return mainWindow;
  }
  
  public void actionPerformed(ActionEvent e) {
    if (e.getSource() == exitBtn){
      System.exit(0);
    } else {
      doSuggest();
    }
  }

  public void keyPressed(KeyEvent e) {
  }

  public void keyReleased(KeyEvent e) {

  }
  
  public void doSuggest() {
    String content = inputTA.getText();
    content = HtmlReformatter.getPlainText(content);
    content = content.trim();
    Post p = new Post();
    p.setContent(content);
    StringBuilder explain = new StringBuilder();
    List<WeightString> tags = suggester.suggest(p, explain);
   // logsTA.setText(explain.toString());
    if (tags.size() > 0) {
      String coloredTags = renderColoredTags(tags);
      tagsLabel.setText(coloredTags);
      tagsLabel.setVisible(true);
    } else {
      tagsLabel.setVisible(false);
    }
  }

  private String renderColoredTags(List<WeightString> tags) {
    double max = 0.0;
    for (WeightString ws : tags) {
      if (ws.weight > max)
        max = ws.weight;
    }
    StringBuilder sb = new StringBuilder();
    sb.append("<html>");
    for (WeightString ws : tags ) {
      sb.append("<font size=\"10\" face=\"Sans\" color=\"");
      sb.append(colorCode(ws.weight / max));
      sb.append("\">");
      sb.append(ws.text);
      sb.append("</font>&nbsp; ");
      if (ws.weight / max < 0.4) {
        break;
      }

    }
    sb.append("</html>");
    return sb.toString();
  }
  
  private String colorCode(double weight) {
    int n = (int)((1.0 - weight) * 255.0);
    return String.format("#%02X%02X%02X", n, n, n);
  }
  
  public void keyTyped(KeyEvent e) {
    if (!realTimeSuggestion) {
      return;
    }
    Timer timer = new Timer();
    SuggestTimerTask stt = new SuggestTimerTask(this);
    timer.schedule(stt, 500);
  }

}
