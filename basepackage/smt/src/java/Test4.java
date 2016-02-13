import java.io.File;

public class Test4 {
	public static void main(String[] args) {
		String input = "/media/disk1/private/cxx/douban/workingdir/model.0.gz";
		// String tagVcbPath = input.substring(0,input.length() - 6)+ "model." +
		// 0 + ".gz"+File.separator+"bookTag.vcb";
		String tagVcbPath = input.substring(0, input.length() - 10) + "cut.gz.wordlex";
		System.out.println(input);
		System.out.print(tagVcbPath);
	}
}
