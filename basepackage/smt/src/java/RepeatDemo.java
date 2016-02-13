
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Vector;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.text.StyledDocument;

import prefuse.Constants;
import prefuse.Display;
import prefuse.Visualization;
import prefuse.action.ActionList;
import prefuse.action.RepaintAction;
import prefuse.action.assignment.ColorAction;
import prefuse.action.assignment.DataColorAction;
import prefuse.action.layout.graph.ForceDirectedLayout;
import prefuse.activity.Activity;
import prefuse.controls.DragControl;
import prefuse.controls.PanControl;
import prefuse.controls.ZoomControl;
import prefuse.data.Graph;
import prefuse.data.io.DataIOException;
import prefuse.data.io.GraphMLReader;
import prefuse.render.DefaultRendererFactory;
import prefuse.render.LabelRenderer;
import prefuse.util.ColorLib;
import prefuse.visual.VisualItem;

public class RepeatDemo {
	// for Demo
	static JFrame frame;

	private JTextPane textPane;
	private StyledDocument doc;
	private JScrollPane textScroll;
	private JButton demoButton;
	private JPanel inputPanel;

	private JPanel graphPanel;
	private JTextArea debugArea;
	private JScrollPane debugScroll;
	private JPanel outputPanel;

	private JPanel basicPanel;

	// for data
	private String text;
	// use inverted list
	private HashMap<Character, Vector<Integer>> invertedList;
	private HashMap<String, Vector<Integer>> reiteration;
	private HashMap<String, Vector<Integer>> oriReiter;
	private HashSet<String> appear;
	private Pattern spaceRE;

	// for color
	private Color[] colors = { new Color(255, 0, 0), new Color(255, 255, 0), new Color(128, 42, 42),
			new Color(0, 0, 255), new Color(0, 255, 255), new Color(34, 139, 34), new Color(160, 32, 240),
			new Color(156, 102, 31), new Color(227, 207, 87), new Color(163, 148, 128), new Color(61, 89, 171),
			new Color(59, 94, 15), new Color(189, 252, 201), new Color(138, 43, 226), new Color(255, 127, 80),
			new Color(255, 153, 18), new Color(210, 105, 30), new Color(30, 144, 255), new Color(8, 46, 84),
			new Color(107, 142, 35), new Color(160, 102, 211), new Color(178, 34, 34), new Color(235, 142, 85),
			new Color(255, 125, 64), new Color(11, 23, 70), new Color(127, 255, 212), new Color(48, 128, 20),
			new Color(153, 51, 250), new Color(176, 48, 96), new Color(255, 227, 132), new Color(188, 143, 143),
			new Color(3, 168, 158), new Color(64, 224, 208), new Color(46, 139, 87), new Color(218, 112, 214),
			new Color(255, 192, 203), new Color(255, 215, 0), new Color(199, 97, 20), new Color(25, 25, 112),
			new Color(0, 255, 0), new Color(0, 255, 127), new Color(221, 160, 221), new Color(250, 128, 114),
			new Color(218, 165, 105), new Color(115, 74, 18), new Color(51, 161, 201), new Color(127, 255, 0),
			new Color(210, 180, 140), new Color(106, 90, 205), new Color(255, 69, 0), new Color(255, 97, 0),
			new Color(160, 82, 45), new Color(0, 199, 140), new Color(61, 145, 64), new Color(139, 69, 19),
			new Color(135, 206, 235), new Color(255, 0, 255), new Color(245, 222, 179), new Color(139, 69, 19),
			new Color(65, 105, 225), new Color(0, 201, 87), new Color(210, 180, 140) };

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					RepeatDemo window = new RepeatDemo();
					window.frame.setLocationRelativeTo(null);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application
	 */
	public RepeatDemo() {
		createContents();
		invertedList = new HashMap<Character, Vector<Integer>>();
		reiteration = new HashMap<String, Vector<Integer>>();
		oriReiter = new HashMap<String, Vector<Integer>>();
		appear = new HashSet<String>();
		spaceRE = Pattern.compile("[《》，、“”（）：\t\r]");
	}

	/**
	 * Initialize the contents of the frame
	 */
	private void createContents() {
		textPane = new JTextPane();
		textPane.setFont(new Font(Font.DIALOG, Font.PLAIN, 20));
		textPane.setEditable(true);
		textPane.setPreferredSize(new Dimension(1110, 375));
		doc = textPane.getStyledDocument();
		textScroll = new JScrollPane(textPane);

		// textPane.setText("Hello");

		demoButton = new JButton("Demo");
		demoButton.setPreferredSize(new Dimension(130, 375));
		demoButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				demoAction(e);
			}
		});

		inputPanel = new JPanel();
		inputPanel.add(textScroll, BorderLayout.CENTER);
		inputPanel.add(demoButton, BorderLayout.WEST);

		graphPanel = new JPanel(new BorderLayout());

		// graphPanel.setSize(200, 200);
		debugArea = new JTextArea(15, 15);
		debugArea.setBackground(Color.WHITE);
		debugArea.setForeground(Color.BLACK);
		debugArea.setEditable(false);
		debugArea.setLineWrap(true);
		debugArea.setFont(new Font(Font.DIALOG, Font.PLAIN, 16));
		debugScroll = new JScrollPane(debugArea);
		debugScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

		outputPanel = new JPanel(new BorderLayout());
		outputPanel.add(graphPanel, BorderLayout.CENTER);
		outputPanel.add(debugScroll, BorderLayout.EAST);

		basicPanel = new JPanel(new GridLayout(2, 1));
		basicPanel.add(inputPanel);
		basicPanel.add(outputPanel);

		frame = new JFrame();
		frame.setTitle("ReiterationDemo");
		frame.setVisible(true);
		frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getRootPane().setDefaultButton(demoButton);
		frame.getContentPane().add(basicPanel);
	}

	private FontAttrib getFontAttrib(Color color, Color backColor) {
		FontAttrib att = new FontAttrib();
		att.setName("宋体");
		att.setSize(20);
		att.setStyle(FontAttrib.GENERAL);
		att.setColor(color);
		att.setBackColor(backColor);
		return att;
	}

	public void demoAction(ActionEvent evt) {
		invertedList.clear();
		reiteration.clear();
		oriReiter.clear();
		appear.clear();
		debugArea.setText("");

		text = textPane.getText();
		text = spaceRE.matcher(text).replaceAll(" ");
		text = text.replaceAll("\n", "");
		text = text.replaceAll("　", "");

		// debugArea.setText(text);
		for (int i = 0; i < text.length(); i++) {
			char ch = text.charAt(i);
			if (ch == ' ') {
				continue;
			} else {
				if (!invertedList.containsKey(ch)) {
					invertedList.put(ch, new Vector<Integer>());
				}
				invertedList.get(ch).add(i);
			}
		}

		for (Entry<Character, Vector<Integer>> e : invertedList.entrySet()) {
			debugArea.append(e.getKey() + ":");
			for (int i = 0; i < e.getValue().size(); i++) {
				debugArea.append(e.getValue().get(i) + " ");
			}
			debugArea.append("\n");
		}
		debugArea.append("\n\nReiteration:\n");

		Vector<String> preWords = new Vector<String>();
		HashSet<String> suffix = new HashSet<String>();
		for (int i = 0; i < text.length() - 2; i++) {
			char ch = text.charAt(i);
			char secondCh = text.charAt(i + 1);
			if (ch == ' ' || secondCh == ' ' || ch == '。' || secondCh == '。') {
				continue;
			}
			// String word = text.substring(i, i + 2);
			Vector<Integer> first = invertedList.get(ch);
			Vector<Integer> second = invertedList.get(secondCh);
			if (first.size() == 1 || second.size() == 1) {
				continue;
			}
			Vector<Integer> ans = mergeVector(first, second, i, i + 1);
			if (ans.size() == 0) {
				continue;
			}
			int j = i + 2;
			while (j < text.length()) {
				char nextCh = text.charAt(j);
				if (nextCh == ' ' || nextCh == '。') {
					Vector<Integer> record = new Vector<Integer>();
					record.add(i);
					for (int k = 0; k < ans.size(); k++) {
						record.add(ans.get(k) + i - j + 1);
					}
					String newWord = text.substring(i, j);
					if (!oriReiter.containsKey(newWord)) {
						boolean isSuffix = false;
						Vector<Integer> copy = new Vector<Integer>(record);
						for (Entry<String, Vector<Integer>> e : oriReiter.entrySet()) {
							if (e.getKey().contains(newWord)) {
								if ((e.getValue().size() >= record.size()) && (!suffix.contains(newWord))) {
									isSuffix = true;
									break;
								} else {
									int pos = e.getKey().indexOf(newWord);
									Vector<Integer> tmp = new Vector<Integer>();
									for (int k = 0; k < e.getValue().size(); k++) {
										tmp.add(e.getValue().get(k) + pos);
									}
									record.removeAll(tmp);
									suffix.add(newWord);
								}
							}
						}
						if (isSuffix == false) {
							reiteration.put(newWord, record);
							oriReiter.put(newWord, copy);
							for (String preWord : preWords) {
								reiteration.get(preWord).removeAll(record);
							}
							preWords.add(newWord);
						}
					}
					break;
				}
				Vector<Integer> next = invertedList.get(nextCh);
				Vector<Integer> newAns = mergeVector(ans, next, j - 1, j);
				if (newAns.size() == 0) {
					Vector<Integer> record = new Vector<Integer>();
					record.add(i);
					for (int k = 0; k < ans.size(); k++) {
						record.add(ans.get(k) + i - j + 1);
					}
					String newWord = text.substring(i, j);
					if (!oriReiter.containsKey(newWord)) {
						boolean isSuffix = false;
						Vector<Integer> copy = new Vector<Integer>(record);
						for (Entry<String, Vector<Integer>> e : oriReiter.entrySet()) {
							if (e.getKey().contains(newWord)) {
								if ((e.getValue().size() >= record.size()) && (!suffix.contains(newWord))) {
									isSuffix = true;
									break;
								} else {
									int pos = e.getKey().indexOf(newWord);
									Vector<Integer> tmp = new Vector<Integer>();
									for (int k = 0; k < e.getValue().size(); k++) {
										tmp.add(e.getValue().get(k) + pos);
									}
									record.removeAll(tmp);
									suffix.add(newWord);
								}

							}
						}
						if (isSuffix == false) {
							reiteration.put(newWord, record);
							oriReiter.put(newWord, copy);
							for (String preWord : preWords) {
								reiteration.get(preWord).removeAll(record);
							}
							preWords.add(newWord);
						}
					}
					break;
				}
				if (newAns.size() != ans.size()) {
					Vector<Integer> record = new Vector<Integer>();
					record.add(i);
					for (int k = 0; k < ans.size(); k++) {
						record.add(ans.get(k) + i - j + 1);
					}
					String newWord = text.substring(i, j);
					if (!oriReiter.containsKey(newWord)) {
						boolean isSuffix = false;
						Vector<Integer> copy = new Vector<Integer>(record);
						for (Entry<String, Vector<Integer>> e : oriReiter.entrySet()) {
							if (e.getKey().contains(newWord)) {
								if ((e.getValue().size() >= record.size()) && (!suffix.contains(newWord))) {
									isSuffix = true;
									break;
								} else {
									int pos = e.getKey().indexOf(newWord);
									Vector<Integer> tmp = new Vector<Integer>();
									for (int k = 0; k < e.getValue().size(); k++) {
										tmp.add(e.getValue().get(k) + pos);
									}
									record.removeAll(tmp);
									suffix.add(newWord);
								}
							}
						}
						if (isSuffix == false) {
							reiteration.put(newWord, record);
							oriReiter.put(newWord, copy);
							for (String preWord : preWords) {
								reiteration.get(preWord).removeAll(record);
							}
							preWords.add(newWord);
						}
					} else {
						preWords.add(newWord);
					}
					ans = newAns;
					j++;
				} else {
					ans = newAns;
					j++;
				}
			}
			preWords.clear();
			i = j;
		}

		try {
			BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("test.xml")));
			out.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
					+ "<!--  An excerpt of an egocentric social network  -->\n"
					+ "<graphml xmlns=\"http://graphml.graphdrawing.org/xmlns\">\n"
					+ "<graph edgedefault=\"undirected\">\n\n" + "<!-- data schema -->\n"
					+ "<key id=\"name\" for=\"node\" attr.name=\"name\" attr.type=\"string\"/>\n"
					+ "<key id=\"gender\" for=\"node\" attr.name=\"gender\" attr.type=\"string\"/>)\n\n");
			out.flush();
			String[] sentences = text.split("。|!|？");
			if (sentences.length == 0) {
				return;
			}
			out.write("<!-- nodes -->\n");
			out.flush();
			String textSplit = "";
			for (int i = 0; i < sentences.length; i++) {
				if (sentences[i].length() < 2) {
					continue;
				}
				textSplit += (i + 1) + ":" + sentences[i] + "\n";
				String gender = (i % 2 == 0) ? "M" : "F";
				out.write("<node id=\"" + (i + 1) + "\">\n" + " <data key=\"name\">" + (i + 1) + "</data>\n"
						+ " <data key=\"gender\">" + gender + "</data>\n" + " </node>\n");
				out.flush();
			}
			out.newLine();
			out.flush();
			textPane.setText(textSplit);

			out.write("<!-- edges -->\n");
			out.flush();
			int[] domain = new int[text.length()];
			/*
			 * int count = 0; for(int i = 0 ; i < sentences.length; i ++){
			 * for(int j = 0; j < sentences[i].length(); j ++){ domain[count +
			 * j] = i; } count += sentences[i].length(); domain[count] = i;
			 * count ++; }
			 */
			int num = 0;
			for (int i = 0; i < text.length(); i++) {
				domain[i] = num;
				if (text.charAt(i) == '。' || text.charAt(i) == '！') {
					num++;
				}
			}
			Vector<HashSet<String>> wordVec = new Vector<HashSet<String>>();
			for (int i = 0; i < sentences.length; i++) {
				wordVec.add(new HashSet<String>());
			}
			HashMap<String, Vector<Integer>> senMap = new HashMap<String, Vector<Integer>>();
			for (Entry<String, Vector<Integer>> e : reiteration.entrySet()) {
				Vector<Integer> tmp = new Vector<Integer>();
				for (int i = 0; i < e.getValue().size(); i++) {
					int senId = domain[e.getValue().get(i)];
					wordVec.get(senId).add(e.getKey());
					tmp.add(senId);
				}
				senMap.put(e.getKey(), tmp);
			}

			// output
			for (Entry<String, Vector<Integer>> e : reiteration.entrySet()) {
				if (e.getValue().size() == 0 || e.getValue().size() == 1) {
					continue;
					// reiteration.remove(e.getKey());
				}
				debugArea.append(e.getKey() + ":");
				for (int i = 0; i < e.getValue().size(); i++) {
					debugArea.append((domain[e.getValue().get(i)] + 1) + " ");
				}
				debugArea.append("\n");
			}

			for (int i = 0; i < wordVec.size(); i++) {
				debugArea.append((i + 1) + ":");
				for (String word : wordVec.get(i)) {
					debugArea.append(word + " ");
				}
				debugArea.append("\n");
			}

			// add color
			int dis = 2;
			int counter = 1;
			for (Entry<String, Vector<Integer>> e : reiteration.entrySet()) {
				int len = e.getKey().length();
				for (Integer i : e.getValue()) {
					if (domain[i] >= 9) {
						if (domain[i] >= 99) {
							dis = 4 * (domain[i] - 98) + 288;
						} else {
							dis = 3 * domain[i] - 6;
						}
					} else {
						dis = 2 * (domain[i] + 1);
					}
					doc.setCharacterAttributes(i + dis, len,
							getFontAttrib(colors[counter % 62], new Color(200, 200, 200)).getAttrSet(), true);
				}
				counter++;
			}
			// doc.setCharacterAttributes(0, 10,
			// getFontAttrib().getAttrSet(),true);

			for (int i = 0; i < wordVec.size() - 1; i++) {
				int[] record = new int[sentences.length];
				for (int j = 0; j < sentences.length; j++) {
					record[j] = 0;
				}
				for (String word : wordVec.get(i)) {
					Vector<Integer> tmp = senMap.get(word);
					int preSen = -1;
					for (int j = 0; j < tmp.size(); j++) {
						if (tmp.get(j) != preSen) {
							record[tmp.get(j)]++;
							preSen = tmp.get(j);
						}
					}
				}
				for (int j = i + 1; j < wordVec.size(); j++) {
					if (record[j] > 2) {
						out.write("<edge source=\"" + (i + 1) + "\" target=\"" + (j + 1) + "\"></edge>\n");
						out.flush();
					}
				}
			}

			out.write("\n</graph>\n</graphml>\n");
			out.flush();
			out.close();
		} catch (IOException e) {
			// TODO: handle exception
			e.printStackTrace();
		}

		graphPanel.removeAll();
		// -- 1. load the data ------------------------------------------------

		// load the socialnet.xml file. it is assumed that the file can be
		// found at the root of the java classpath
		Graph graph = null;
		try {
			graph = new GraphMLReader().readGraph("test.xml");
		} catch (DataIOException e) {
			e.printStackTrace();
			System.err.println("Error loading graph. Exiting...");
			System.exit(1);
		}

		// -- 2. the visualization --------------------------------------------

		// add the graph to the visualization as the data group "graph"
		// nodes and edges are accessible as "graph.nodes" and "graph.edges"
		Visualization vis = new Visualization();
		vis.add("graph", graph);
		vis.setInteractive("graph.edges", null, false);

		// -- 3. the renderers and renderer factory ---------------------------

		// draw the "name" label for NodeItems
		LabelRenderer r = new LabelRenderer("name");
		r.setRoundedCorner(8, 8); // round the corners

		// create a new default renderer factory
		// return our name label renderer as the default for all non-EdgeItems
		// includes straight line edges for EdgeItems by default
		vis.setRendererFactory(new DefaultRendererFactory(r));

		// -- 4. the processing actions ---------------------------------------

		// create our nominal color palette
		// pink for females, baby blue for males
		int[] palette = new int[] { ColorLib.rgb(255, 180, 180), ColorLib.rgb(190, 190, 255) };
		// map nominal data values to colors using our provided palette
		DataColorAction fill = new DataColorAction("graph.nodes", "gender", Constants.NOMINAL, VisualItem.FILLCOLOR,
				palette);
		// use black for node text
		ColorAction text = new ColorAction("graph.nodes", VisualItem.TEXTCOLOR, ColorLib.gray(0));
		// use light grey for edges
		ColorAction edges = new ColorAction("graph.edges", VisualItem.STROKECOLOR, ColorLib.gray(200));

		// create an action list containing all color assignments
		ActionList color = new ActionList();
		color.add(fill);
		color.add(text);
		color.add(edges);

		// create an action list with an animated layout
		ActionList layout = new ActionList(Activity.INFINITY);
		layout.add(new ForceDirectedLayout("graph"));
		layout.add(new RepaintAction());

		// add the actions to the visualization
		vis.putAction("color", color);
		vis.putAction("layout", layout);

		// -- 5. the display and interactive controls -------------------------

		Display d = new Display(vis);
		d.setSize(1000, 500); // set display size
		// drag individual items around
		d.addControlListener(new DragControl());
		// pan with left-click drag on background
		d.addControlListener(new PanControl());
		// zoom with right-click drag
		d.addControlListener(new ZoomControl());

		// -- 6. launch the visualization -------------------------------------
		graphPanel.add(d);
		// assign the colors
		vis.run("color");
		// start up the animated layout
		vis.run("layout");

	}

	public Vector<Integer> mergeVector(Vector<Integer> a, Vector<Integer> b, int aStart, int bStart) {
		Vector<Integer> result = new Vector<Integer>();
		int aLen = a.size();
		int bLen = b.size();
		int i = aLen;
		int j = bLen;
		for (int k = 0; k < aLen; k++) {
			if (a.get(k).intValue() > aStart) {
				i = k;
				break;
			}
		}
		for (int k = 0; k < bLen; k++) {
			if (b.get(k).intValue() > bStart) {
				j = k;
				break;
			}
		}
		while (i < aLen && j < bLen) {
			if (a.get(i) + 1 == b.get(j)) {
				result.add(b.get(j));
				i++;
				j++;
				continue;
			}
			if (a.get(i) + 1 < b.get(j)) {
				i++;
				continue;
			}
			if (a.get(i) + 1 > b.get(j)) {
				j++;
				continue;
			}
		}

		return result;
	}
}
