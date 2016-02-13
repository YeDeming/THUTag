import java.util.regex.Pattern;

public class Test2 {
	public static void main(String[] args) throws Exception {

		Pattern spaceRE = Pattern.compile("[\"\\\\]");

		String input = "\"男孩米奇的\\\\沙漠历险";
		System.out.println(input);
		String tag = spaceRE.matcher(input).replaceAll("");
		System.out.println(tag);

	}
}
