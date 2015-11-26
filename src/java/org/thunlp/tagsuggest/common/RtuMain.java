package org.thunlp.tagsuggest.common;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLEncoder;
import java.net.URLDecoder;
import java.io.UnsupportedEncodingException;
public class RtuMain {
	
    public static String getProjectPath()  {
    	try
    	{
    		URL url = RtuMain.class.getProtectionDomain().getCodeSource().getLocation();
    		String filePath = URLDecoder.decode(url.getPath(), "utf-8");
    		filePath = filePath.substring(5,  filePath.lastIndexOf("tagsuggest.jar"));
    		return filePath;
    	} catch (Exception e) {
            e.printStackTrace();
        }
    	return "";
    }

    public static String getRealPath() {
        String realPath = RtuMain.class.getClassLoader().getResource("").getFile();
        File file = new File(realPath);
        realPath = file.getAbsolutePath();
        try {
            realPath = URLDecoder.decode(realPath, "utf-8");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return realPath;
    }
}