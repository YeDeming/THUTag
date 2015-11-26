package util.segment;
import java.io.*;
import java.util.HashSet;
import java.util.Vector;
public class ThulacJni{
	
	/*
	 * 功能：初始化
	 * 输入：String,代表models文件夹所在位置，注意最后要加/
	 * 输出：boolean，代表是否初始化成功
	*/
	public native boolean init(String path);
	/*
	 * 功能：加入用户自定义词表，每行一个词，UTF8编码
	 * 输入：String,代表用户自定义词表的路径
	 * 输出：boolean，代表是否加入成功
	*/
	public native boolean addUserDict(String path);
	/*
	 * 功能：分词
	 * 输入：String,代表要分词的文本
	 * 输出：byte[]，可用new String(byte[])得到分词结果的String
	*/
	public native String segment(String content);
	static{
		System.loadLibrary("thulacjni");
	}
	
	private HashSet<String> filterSet = new HashSet<String>();
	public HashSet<String> getFilterSet() {
		return filterSet;
	}
	
	public void setFilterSet(HashSet<String> filterSet) {
		this.filterSet = filterSet;
	}
	
	public String[] segmentOnly(String content){
		String output = segment(content);
		String[] words= output.split(" ");
		String[] results = new String[words.length];
		for(int i = 0;i < words.length; i ++){
			results[i] = words[i].split("_")[0];
		}
		return results;
	}
	
	public String[] segmentAndTag(String content){
		String output = segment(content);
		return output.split(" ");
	}
	
	public String[] segmentWithFilter(String content){
		String output = segment(content);
		String[] words= output.split(" ");
		Vector<String> results = new Vector<String>();
		for(int i = 0;i < words.length; i ++){
			String[] datas = words[i].split("_");
			if(!filterSet.contains(datas[1])){
				continue;
			}
			results.add(datas[0]);
		}
		return results.toArray(new String[0]);
	}
	
	public static void main(String[] args) throws IOException{
		String dataPath = "/home/cxx/Workspaces/cxx/res/models/";
		ThulacJni thulac = new ThulacJni();
		boolean isInit = thulac.init(dataPath);
		if(!isInit){
			System.out.println("Init Failed!");
		}
		if(thulac.addUserDict("/home/cxx/Workspaces/cxx/res/tagList10.dat")){
			String output = thulac.segment("我爱北京天安门");
			System.out.println(output);
			output = thulac.segment("我在看算法与数据结构");
			System.out.println(new String(output));
			System.out.println(System.getProperty("java.library.path"));
			/*
			BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(""),"UTF8"));
			String line = "";
			while((line = in.readLine()) != null){
				
			}
			/*
			String text = "我在看算法与数据结构";
			String[] words = thulac.segmentOnly(text);
			for(String word : words){
				System.out.print(word+" ");
			}
			System.out.println();
			
			String[] tags = thulac.segmentAndTag(text);
			for(String tag : tags){
				System.out.print(tag+" ");
			}
			System.out.println();
			
			HashSet<String> filter = new HashSet<String>();
			filter.add("ni");
			thulac.setFilterSet(filter);
			String[] filters = thulac.segmentWithFilter(text);
			for(String word: filters){
				System.out.print(word+" ");
			}
			System.out.println();
			//byte[] output = thulac.segment(text);
			//System.out.println((new String(output)));
			*/
		}
	}
}
