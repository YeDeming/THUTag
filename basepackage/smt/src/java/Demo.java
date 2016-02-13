import java.io.File;

public class Demo {
	public static void main(String args[]) {
		Runtime rn = Runtime.getRuntime();
		Process p = null;
		try {/*
				 * String[] command = new String[]{
				 * "/home/cxx/smt/Demo/plain2snt.out","chinese","english" };
				 */
			p = rn.exec("/home/cxx/smt/Demo/plain2snt.out chinese english", null, new File("/home/cxx/smt/Demo"));

			p.waitFor();
		} catch (Exception e) {
			System.out.println("Error exec!");
		}
	}
}
