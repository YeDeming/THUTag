package org.thunlp.language.chinese;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.nio.channels.Selector;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class IctclasWordSegment implements WordSegment {
	private static ReentrantLock lock = new ReentrantLock();
	private static Process ictclasProc = null;
	private static int roundToLive = 0;
	private static TimedReader resultReader = null;
	private static OutputStream cmdStream = null;
	private static String libpath;
	public IctclasWordSegment ( String libpath ) {
		IctclasWordSegment.libpath = libpath;
		lock.lock();
		initIctclas ( libpath );
		lock.unlock();
	}
	
	private boolean initIctclas ( String libpath ) {
		boolean done = false;
		IctclasWordSegment.roundToLive --;
		if ( IctclasWordSegment.roundToLive <= 0 ) {
			if ( IctclasWordSegment.ictclasProc != null ) 
				IctclasWordSegment.ictclasProc.destroy();
			IctclasWordSegment.ictclasProc = null;
		}
		if ( IctclasWordSegment.ictclasProc == null ) {
			Runtime rt = Runtime.getRuntime();
			try {
				String os = System.getProperty("os.name").toLowerCase();
				String arch = System.getProperty("os.arch").toLowerCase();
				String ictclasExecutable = "";
				if ( os.contains("mac")) {
					if ( arch.equals("ppc") ) {
						ictclasExecutable = "ictclas-macppc";
					} else {
						ictclasExecutable = "ictclas-macintel";
					}
				} else if ( os.contains("linux") ){
					if (arch.contains("amd64"))
						ictclasExecutable = "ictclas-linux-amd64";
					else
						ictclasExecutable = "ictclas-linux";
				} else if ( os.contains("windows") ) {
					ictclasExecutable = "ictclas-win32";
				}
				String runCmd = libpath + File.separator + ictclasExecutable + " " + libpath + File.separator;
				System.out.println(runCmd);
				ictclasProc = rt.exec(runCmd);
				
				InputStream is = ictclasProc.getInputStream();
				cmdStream = ictclasProc.getOutputStream();
				resultReader = new TimedReader( new InputStreamReader(is, "gb2312") );
				done = true;
				IctclasWordSegment.roundToLive = 4096;
			} catch (IOException e) {
				//e.printStackTrace();
				ictclasProc = null;
			}
		} else {
			done = true;
		}
		return done;
	}
	
	public boolean outputPosTag () {
		return true;
	}
	
	public String [] segmentWithPostag( String text) {
		String result = getSegmentResult( text );
		return result.split(" +");
	}
	
	private Pattern filterPat = null;
	private String bufferedFilters = "";
	public String [] segmentWithPostagFilter( String text, String filters ) {
		String result = getSegmentResult( text );
		List<String> filtered = new LinkedList<String>();
		synchronized(this) {
			if ( ! bufferedFilters.equals(filters) ) {
				filterPat = Pattern.compile("([^ /]+)/(" + filters + ") " );
			}
			Matcher m = filterPat.matcher(result);
			while ( m.find() ) {
				filtered.add(m.group(1));
			}
		}
		return filtered.toArray(new String[filtered.size()]);
	}
	
	public String [] segment ( String text ) {
		String result = getSegmentResult( text );
		result = result.replaceAll("/[A-za-z!]+ ", " ");
		return result.split(" +");
	}
	
	synchronized private String getSegmentResult( String text ) {
		String result = "";
		byte [] lineEnd = {'\n'};
	
		IctclasWordSegment.lock.lock();
		String [] textSegs = text.split("[ \t\n\r]+");
		for ( String seg : textSegs ) {
			if (! initIctclas( IctclasWordSegment.libpath ) ) {
				return "";
			}
			String output;
			boolean retrying = false;
			byte[] bytes;
			try {
				bytes = seg.getBytes("gb2312");
			} catch (UnsupportedEncodingException e2) {
				e2.printStackTrace();
				return "";
			}
			while ( true ) {
				try {
					cmdStream.write(bytes);
					cmdStream.write(lineEnd);
					cmdStream.flush();
					output = resultReader.readLine();
					break;
				} catch ( IOException e ) {
					try {
						cmdStream.close();
						resultReader.close();
					} catch (IOException e1) {
						e1.printStackTrace();
					}
					ictclasProc = null;
					output = null;
					System.out.println("cannot segment [" + seg + "]");
					if ( retrying )
						break;
					retrying = true;
				}
			}
			if ( output != null ) {
				result += output + " ";
			} 
		}
		IctclasWordSegment.lock.unlock();
		return result;
	}
	
	private static class TimedReader {
		private Reader in;
		private static int TIMEOUT = 10 * 1000;
		private char [] buf;
			
		public TimedReader(Reader in) throws IOException {
			this.in = in;
			buf = new char[40960];
		}

		public String readLine() throws IOException
		{
			long msec = System.currentTimeMillis();
			int start = 0;
			int size;
			while ( true ) {
				if (this.in.ready()) { 
					size = this.in.read(buf, start, buf.length - start);
					if ( size < 0 ) {
						start = 0;
						return null;
					}
					for ( int i = start ; i < start + size ; i++ ) {
						if ( buf[i] == '\n') {
							// 读到行尾
							int end = i;
							if ( buf[i] == '\r' )
								end--;
							String line = new String(buf, 0, end);
							for ( int j = 0 ; j < size - i - 1 ; j++ ) {
								buf[j] = buf[ j + i + 1];
							}
							start = 0;
							return line;
						}
					}
					start += size;
				}
				if (System.currentTimeMillis() - msec >= TIMEOUT )
				{
					throw new IOException("Timeout for reading");
				}
				try { Thread.sleep(0); }
				catch (InterruptedException e) {  }
			}
		}
		
		public void close() throws IOException {
			in.close();
		}
	}
}
