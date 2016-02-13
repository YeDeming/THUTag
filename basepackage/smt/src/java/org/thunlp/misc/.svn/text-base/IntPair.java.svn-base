package org.thunlp.misc;

public class IntPair {
	public int first;
	public int second;
	
	public IntPair() {
		first = 0;
		second = 0;
	}
	public IntPair(int first, int second) {
		this.first = first;
		this.second = second;
	}
	
	public boolean equals(Object o) {
	  if (!(o instanceof IntPair)) {
	    return false;
	  }
	  IntPair ip = (IntPair) o;
	  if (ip.first == first && ip.second == second) {
	    return true;
	  } else {
	    return false;
	  }
	}
	
	public int hashCode() {
	  return first + second;
	}
	
	public String toString() {
		return first + ":" + second;
	}
	
	public boolean fromString(String str) {
		String [] cols = str.split(" ");
		if ( cols.length != 2 ) 
			return false;
		first = Integer.parseInt(cols[0]);
		second = Integer.parseInt(cols[1]);
		return true;
	}
}
