package org.thunlp.tagsuggest.common;

import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.Map.Entry;

/**
 * 2D Counter.
 * @author sixiance
 *
 * @param <KEY>
 */
public class SparseCounter<KEY extends Comparable<KEY>> {
  private Set<KEY> EMPTY_LIST = new TreeSet<KEY>();

  private static class KeyPair<KEY extends Comparable<KEY>>
  implements Comparable<KeyPair<KEY>>{
    public KeyPair(KEY row, KEY column) {
      this.row = row;
      this.column = column;
    }
    public KEY row;
    public KEY column;

    @SuppressWarnings("unchecked")
    @Override
    public boolean equals(Object obj) {
      if (!(obj instanceof KeyPair)) {
        return false;
      } else {
        KeyPair<KEY> pair = (KeyPair<KEY>) obj;
        return pair.row.equals(row) && pair.column.equals(column);
      }
    }

    @Override
    public int hashCode() {
      return row.hashCode() + column.hashCode();
    }

    @Override
    public int compareTo(KeyPair<KEY> o) {
      int result = this.row.compareTo(o.row);
      if (result == 0) {
        result = this.column.compareTo(o.column);
      }
      return result;
    }

  }

  private static class Info<KEY> {
    public Set<KEY> values;
    public long sum;
  }

  private Map<KeyPair<KEY>, Long> counts = new Hashtable<KeyPair<KEY>, Long>();
  private Map<KEY, Info<KEY>> rows = new Hashtable<KEY, Info<KEY>>();
  private Map<KEY, Info<KEY>> columns = new Hashtable<KEY, Info<KEY>>();
  private long total = 0;

  public void inc(SparseCounter<KEY> another) {
    for (Entry<KeyPair<KEY>, Long> e : another.counts.entrySet()) {
      inc(e.getKey().row, e.getKey().column, e.getValue());
    }
  }

  public void clear() {
    counts.clear();
    rows.clear();
    columns.clear();
    total = 0;
  }

  public long total() {
    return total;
  }

  public int size() {
    return counts.size();
  }

  public double sparsity() {
    return (double) counts.size() /
    (double)rows.size() / (double)columns.size();
  }

  public long numNonZeroElements() {
    return counts.size();
  }

  public void inc(KEY row, KEY column, long delta) {
    KeyPair<KEY> pair = new KeyPair<KEY>(row, column);
    Long value = counts.get(pair);
    if (value == null) {
      value = 0l;
    }
    if (value + delta == 0) {
      counts.remove(pair);
    } else {
      counts.put(pair, value + delta);
    }
    // Update row info.
    Info<KEY> rowinfo = rows.get(row);
    if (rowinfo == null) {
      rowinfo = new Info<KEY>();
      rowinfo.sum = 0;
      rowinfo.values = new HashSet<KEY>();
      rows.put(row, rowinfo);
    }
    rowinfo.sum += delta;

    if (rowinfo.sum == 0) {
      rows.remove(row);
    } else {
      if (value + delta == 0) {
        rowinfo.values.remove(column);
      } else {
        rowinfo.values.add(column);
      }
    }
    // Update column info.
    Info<KEY> columninfo = columns.get(column);
    if (columninfo == null) {
      columninfo = new Info<KEY>();
      columninfo.sum = 0;
      columninfo.values = new HashSet<KEY>();
      columns.put(column, columninfo);
    }
    columninfo.sum += delta;
    if (columninfo.sum == 0) {
      columns.remove(column);
    } else {
      if (value + delta == 0) {
        columninfo.values.remove(row);
      } else {
        columninfo.values.add(row);
      }
    }
    // Update total.
    total += delta;
  }

  public long get(KEY row, KEY column) {
    Long value = counts.get(new KeyPair<KEY>(row, column));
    if (value == null) {
      return 0;
    } else {
      return value.longValue();
    }
  }

  public long rowSum(KEY row) {
    Info<KEY> info = rows.get(row);
    if (info == null) {
      return 0;
    } else {
      return info.sum;
    }
  }

  public long columnSum(KEY column) {
    Info<KEY> info = columns.get(column);
    if (info == null) {
      return 0;
    } else {
      return info.sum;
    }
  }

  public Set<KEY> columns(KEY row) {
    Info<KEY> info = rows.get(row);
    if (info == null) {
      return (Set<KEY>) EMPTY_LIST;
    } else {
      return info.values;
    }
  }

  public Set<KEY> rows(KEY column) {
    Info<KEY> info = columns.get(column);
    if (info == null) {
      return (Set<KEY>) EMPTY_LIST;
    } else {
      return info.values;
    }
  }
  public Set<KEY> rows() {
    return rows.keySet();
  }

  public Set<KEY> columns() {
    return columns.keySet();
  }
}
