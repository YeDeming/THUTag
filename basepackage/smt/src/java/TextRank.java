import java.util.HashMap;
import java.util.Vector;

import org.thunlp.matrix.NormalMatrix;
import org.thunlp.matrix.pagerank.PageRank;

public class TextRank {

	public static void addEdge(NormalMatrix matrix, Vector<Integer> v, int start, int end) {
		for (int i = start; i < end; i++) {
			for (int j = i + 1; j <= end; j++) {
				matrix.add(v.get(i), v.get(j), 1);
				matrix.add(v.get(j), v.get(i), 1);
			}
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		HashMap<String, Integer> textMap = new HashMap<String, Integer>();
		HashMap<Integer, String> idMap = new HashMap<Integer, String>();
		Vector<Integer> textWordId = new Vector<Integer>();
		int num = 0;
		String words[] = { "三国", "演义", "三国", "演义", "一部", "断代", "历史", "小说", "第一", "它把", "通俗", "本来", "高级", "知识", "分子" };
		for (String word : words) {
			if (!textMap.containsKey(word)) {
				textMap.put(word, num);
				idMap.put(num, word);
				textWordId.add(num);
				num++;
			} else {
				textWordId.add(textMap.get(word));
			}
		}
		NormalMatrix matrix = new NormalMatrix(num, num);
		int window = 10;
		int len = textWordId.size();
		if (len < window) {
			for (int i = 1; i < len; i++) {
				addEdge(matrix, textWordId, 0, i);
			}
			for (int i = 1; i < len - 1; i++) {
				addEdge(matrix, textWordId, i, len - 1);
			}
		} else {
			for (int i = 1; i < window - 1; i++) {
				addEdge(matrix, textWordId, 0, i);
			}
			for (int i = 0; i <= len - window; i++) {
				addEdge(matrix, textWordId, i, i + window - 1);
			}
			for (int i = len - window + 1; i < len - 1; i++) {
				addEdge(matrix, textWordId, i, len - 1);
			}
		}

		PageRank.prepareMatrix(matrix);
		int counter = 0;
		for (double d : PageRank.pageRank(matrix, 100)) {
			System.out.println(idMap.get(counter) + ":" + d);
			counter++;
		}
	}

}
