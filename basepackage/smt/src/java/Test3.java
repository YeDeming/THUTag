import org.thunlp.io.JsonUtil;
import org.thunlp.io.RecordReader;
import org.thunlp.io.RecordWriter;

public class Test3 {
	public static void main(String[] args) {
		int counter = 0;
		try {
			RecordReader input = new RecordReader("/home/cxx/smt/sample/musicPost.dat");
			JsonUtil J = new JsonUtil();
			int n = 0;
			while (input.next()) {
				counter++;
				DoubanPost p = J.fromJson(input.value(), DoubanPost.class);
				n++;
			}
			input.close();
		} catch (Exception e) {
			// TODO: handle exception
			System.out.println(counter);
			e.printStackTrace();
		}
	}
}
