package org.thunlp.io;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.hadoop.io.Text;
import org.codehaus.jackson.map.ObjectMapper;
import org.thunlp.misc.Base64;

public class JsonUtil {
	private ObjectMapper mapper;
	private ReusableTextInputStream textInput = new ReusableTextInputStream();
	private ReusableTextOutputStream textOutput = new ReusableTextOutputStream();

	public JsonUtil() {
		mapper = new ObjectMapper();
	}

	private static class ReusableTextInputStream extends InputStream {
		private Text text = null;
		int p = 0;

		public void setCurrentText(Text text) {
			this.text = text;
			p = 0;
		}

		@Override
		public int read() throws IOException {
			if (text == null)
				throw new IOException("No internal Text");
			if (p < text.getLength()) {
				return text.getBytes()[p++] & 0xff;
			} else {
				return -1;
			}
		}
	}

	private static class ReusableTextOutputStream extends OutputStream {
		private Text text = null;
		private byte[] buffer = new byte[1024];
		int p = 0;

		public void setCurrentText(Text text) {
			this.text = text;
			p = 0;
		}

		@Override
		public void write(int b) throws IOException {
			if (text == null) {
				throw new IOException("No internal text.");
			}

			buffer[p++] = (byte) b;
			if (p >= buffer.length) {
				byte[] newbuffer = new byte[buffer.length + 512];
				for (int i = 0; i < buffer.length; i++) {
					newbuffer[i] = buffer[i];
				}
				buffer = null;
				buffer = newbuffer;
			}
		}

		@Override
		public void close() throws IOException {
			text.set(buffer, 0, p);
			p = 0;
			text = null;
		}
	}

	public <T> T fromJson(String json, Class<T> type) throws IOException {
		return mapper.readValue(json, type);
	}

	public String toJson(Object obj) throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		mapper.writeValue(bos, obj);
		return new String(bos.toByteArray());
	}

	public <T> T fromTextAsJson(Text text, Class<T> type) throws IOException {
		textInput.setCurrentText(text);
		return mapper.readValue(textInput, type);
	}

	public void toTextAsJson(Object obj, Text text) throws IOException {
		textOutput.setCurrentText(text);
		mapper.writeValue(textOutput, obj);
	}

	/**
	 * BASE64 encoding.
	 * 
	 * @param data
	 * @return
	 */
	public static String byteArrayToString(byte[] data) {
		if (data == null || data.length == 0) {
			return "";
		}
		return Base64.encodeBytes(data);
	}

	public static byte[] stringToByteArray(String s) throws IOException {
		byte[] result = null;
		if (s == null || s.length() == 0) {
			return new byte[0];
		}
		result = Base64.decode(s, Base64.DONT_GUNZIP);
		return result;
	}
}
