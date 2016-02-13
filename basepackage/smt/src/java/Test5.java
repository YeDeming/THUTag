import java.io.IOException;

import org.thunlp.language.chinese.ForwardMaxWordSegment;
import org.thunlp.language.chinese.WordSegment;

public class Test5 {

	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		WordSegment ws = new ForwardMaxWordSegment();
		String s = "你好。你怎么了！“你不要死呀！”唉。";
		String[] sentence = s.split("。|！");
		for (int i = 0; i < sentence.length; i++) {
			String[] words = ws.segment(sentence[i]);
			for (int j = 0; j < words.length; j++) {
				System.out.print(words[j] + " ");
			}
			System.out.println();
		}
	}

}
