package org.thunlp.hadoop;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.apache.hadoop.io.Writable;

/**
 * 这个writable可以用来作其他writable的包装容器，在使用的时候要先继承此类， 在 setClasses方法中把元素的个数和类型分别指定。
 * 
 * @author adam
 *
 */
public abstract class DynamicListWritable implements Writable {

	public Class[] types;
	public Writable[] elements;

	public DynamicListWritable() throws InstantiationException, IllegalAccessException {
		setClasses();
		elements = new Writable[types.length];
		for (int i = 0; i < types.length; i++) {
			elements[i] = (Writable) types[i].newInstance();
		}
	}

	/**
	 * 初始化types数组，把各个元素的类对象放在其中
	 *
	 */
	protected abstract void setClasses();

	public void readFields(DataInput in) throws IOException {
		for (int i = 0; i < elements.length; i++) {
			elements[i].readFields(in);
		}
	}

	public void write(DataOutput out) throws IOException {
		for (int i = 0; i < elements.length; i++) {
			elements[i].write(out);
		}
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (Writable w : elements) {
			sb.append(w.toString());
			sb.append(" + ");
		}
		return sb.toString();
	}
}
