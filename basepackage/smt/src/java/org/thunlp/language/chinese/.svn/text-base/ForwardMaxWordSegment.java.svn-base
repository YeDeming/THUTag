package org.thunlp.language.chinese;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.Vector;
import java.util.Map.Entry;
import java.util.logging.Logger;

import org.thunlp.io.TextFileReader;

/**
 * This class implemented the dictionary-based Forward Maximum Matching algorithm
 * for Chinese word segmentation. The main advantage of this algorithm is speed,
 * and this implementation can segment Chinese text at 10MB per second. 
 * 
 * The main drawback of this algorithm is accuracy. Out-of-vocabulary(OOV) words,
 * and overlapping ambiguities cannot be detected and usually are handled badly.
 * OOV words can be handled well by adding them to the dictionary. Since ambiguity
 * is usually only a small proportion of all corpus, they could be ignored safely
 * if your task is not heavily relied on finer accuracy of segmentation.
 * 
 * Example usage:
 * WordSegment ws = new ForwardMaxWordSegment(); // The default dictionary will
 *                                               // be loaded automatically.
 * String text = "一些中文数据";
 * String [] words = ws.segment(text);
 * for (String word : words) {
 *   // word should be "一些", "中文", "数据", respectively.
 * }
 * 
 * @author sixiance
 *
 */
public class ForwardMaxWordSegment implements WordSegment {
	private static Logger LOG = Logger.getAnonymousLogger();
	private Hashtable<Character, Integer> [] automata;
	private boolean [] finalStates;

	public ForwardMaxWordSegment() throws IOException {
		String automataFile = System.getProperty("wordsegment.automata.file",
				"lib/lexicon-automata");
		InputStream automataStream = null;
		if (!System.getProperties().containsKey("wordsegment.automata.file")) {
			LOG.warning("Property 'wordsegment.automata.file' is not set, " +
					"will use default model instead");
			automataStream = this.getClass().getClassLoader()
				.getResourceAsStream("org/thunlp/language/chinese/lexicon.model");
		} else {
			automataStream = new FileInputStream(automataFile);
		}
		loadAutomata(automataStream);
	}

	protected void loadAutomata(InputStream automataStream) throws IOException {
		DataInputStream input = 
			new DataInputStream(
					new BufferedInputStream(automataStream));
		int numStates = input.readInt();
		automata = new Hashtable[numStates];
		finalStates = new boolean[numStates];
		LOG.info("loading " + numStates + " states");
		for ( int i = 0 ; i < numStates ; i++ ) {
			automata[i] = new Hashtable<Character, Integer>();
			finalStates[i] = input.readBoolean();
			int numJumps = input.readInt();
			for ( int j = 0 ; j < numJumps ; j++ ) {
				char key = input.readChar();
				int value = input.readInt();
				automata[i].put(key, value);
			}
		}

		input.close();
	}

	public static void buildAutomata(String inputfile, String automataFile) throws IOException {
		Vector<Hashtable<Character, Integer>> automata = 
			new Vector<Hashtable<Character, Integer>>();
		Vector<Boolean> finalStates = new Vector<Boolean>();
		automata.clear();
		automata.add(new Hashtable<Character, Integer>());
		finalStates.clear();
		finalStates.add(false);

		// Read word list file into a vector
		LOG.info("loading words...");
		Vector<String> wordlist = new Vector<String>();
		int maxLength = 0;
		TextFileReader reader = new TextFileReader(inputfile, "UTF-8");
		String line;
		while ( (line = reader.readLine()) != null ) {
			wordlist.add(line);
			if (line.length() > maxLength) {
				maxLength = line.length();
			}
		}
		reader.close();

		LOG.info( wordlist.size() + " words loaded, sorting...");
		String [] sorted = wordlist.toArray(new String[0]);
		Arrays.sort(sorted);

		LOG.info( "wordlist sorted, building automata..." );

		char [] charStack = new char[maxLength + 1];
		int [] stateStack = new int[maxLength + 1];
		charStack[0] = '$';
		stateStack[0] = 0;
		int stackPos = 0;
		int newStackPos = -1;
		for (int i = 0 ; i < wordlist.size() ; i++) {
			String word = sorted[i];
			newStackPos = 0;
			for (int j = 1 ; j < word.length() + 1 && j <= stackPos ; j++) {
				if (word.charAt(j-1) != charStack[j]) {
					break;
				}
				newStackPos = j;
			}

			if (newStackPos == stackPos && word.length() == stackPos) {
				continue;
			}

			stackPos = newStackPos;
			for (int j = newStackPos ; j < word.length() ; j++) {
				int newStateID = automata.size();
				Hashtable<Character, Integer> jumpTable = automata.get(stateStack[j]);
				jumpTable.put(word.charAt(j), newStateID);
				automata.add(new Hashtable<Character, Integer>());  // Final state.
				if (j == word.length() - 1) {
					finalStates.add(true);
				} else {
					finalStates.add(false);
				}
				++stackPos;
				stateStack[stackPos] = newStateID;
				charStack[stackPos] = word.charAt(j);
			}
		}

		LOG.info("output automata");
		DataOutputStream output = 
			new DataOutputStream(
					new BufferedOutputStream(
							new FileOutputStream(automataFile)));
		output.writeInt(automata.size());
		for (int i = 0 ; i < automata.size() ; i++) {
			output.writeBoolean(finalStates.get(i));
			Hashtable<Character, Integer> jumpTable = automata.get(i);
			output.writeInt(jumpTable.size());
			for (Entry<Character, Integer> e : jumpTable.entrySet()) {
				output.writeChar(e.getKey());
				output.writeInt(e.getValue());
			}
		}
		output.close();
		LOG.info("done");
	}

	public boolean outputPosTag() {
		return false;
	}

	public String[] segment(String text) {
		Vector<String> segments = new Vector<String>();
		int current_state = 0, match_length = 0;
		int start_pos = 0, end_pos = 0;
		for (int i = 0 ; i < text.length() ; i++) {
			Hashtable<Character, Integer> jump_table = automata[current_state];
			assert(jump_table != null);
			Integer jump = jump_table.get(text.charAt(i));
			if (jump != null) {
				current_state = jump;
				match_length++;
				if (finalStates[current_state]) {
					end_pos = i + 1;
				}
			} else {
				if ( match_length == 0 && Character.isSpaceChar(text.charAt(i))) {
					end_pos = start_pos = i + 1;
					continue;
				}

				if (match_length > 0 || (!isLetterOrDigit(text.charAt(i)) && match_length == 0)) {
					if (end_pos <= start_pos) {
						end_pos = start_pos + 1;
					}
					segments.add(text.substring(start_pos, end_pos));
					start_pos = end_pos;
				}

				// Start from where the last word ends
				current_state = 0;
				match_length = 0;

				if (isLetterOrDigit(text.charAt(i))) {
					while (i < text.length() && isLetterOrDigit(text.charAt(i))) {

						i++;
						end_pos = i;
					}
					segments.add(text.substring(start_pos, end_pos));
					start_pos = end_pos;
				}

				while (i < text.length() && Character.isSpaceChar(text.charAt(i))) {
					i++;
					start_pos = i;
				}
				i = start_pos - 1;
			}
		}

		// Collect the last segment
		if (text.length() - start_pos > 0) {
			segments.add(text.substring(start_pos));
		}
		return segments.toArray(new String[segments.size()]);
	}

	private boolean isLetterOrDigit( char c ) {
		return ((c >= 'a' && c <= 'z') || 
				(c >= 'A' && c <= 'Z') || 
				(c >= '0' && c <= '9') ||
				(c >= '０' && c <= '９')); 
	}
	
	public static void main( String [] args ) throws IOException {
		if ( args.length != 2 ) {
			System.out.println("building automata for lexicon from a word list");
			System.out.println("usage:<wordlist> <out:automata-file>");
			return;
		}
		ForwardMaxWordSegment.buildAutomata(args[0], args[1]);
	}
}
