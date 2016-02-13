package org.thunlp.matrix;

import java.util.*;

public class SparseMatrix implements MatrixInterface {

	private ArrayList<Pair>[] matrix;

	int rows, cols;

	@SuppressWarnings("unchecked")
	public SparseMatrix(int rows, int cols) {
		this.rows = rows;
		this.cols = cols;
		matrix = new ArrayList[rows];
		for (int i = 0; i < rows; ++i) {
			matrix[i] = new ArrayList<Pair>();
		}
	}

	@Override
	public double get(int row, int col) {
		List<Pair> rowList = matrix[row];
		for (Pair pair : rowList)
			if (pair.first == col)
				return pair.second;
		return 0;
	}

	@Override
	public int getColsCount() {
		return cols;
	}

	@Override
	public int getRowsCount() {
		return rows;
	}

	public ArrayList<Pair> getRow(int row) {
		return matrix[row];
	}

	@SuppressWarnings("unchecked")
	@Override
	public void inv() {
		ArrayList<Pair> newMatrix[] = new ArrayList[cols];
		for (int i = 0; i < cols; ++i) {
			newMatrix[i] = new ArrayList<Pair>();
		}
		for (int i = 0; i < rows; i++) {
			ArrayList<Pair> row = matrix[i];
			for (int j = 0; j != row.size(); j++) {
				int col = row.get(j).getFirst();
				double val = row.get(j).getSecond();
				newMatrix[col].add(new Pair(i, val));
			}
		}
		matrix = newMatrix;
	}

	@Override
	public double[] multiply(double[] vector) {
		double result[] = new double[vector.length];
		for (int i = 0; i != matrix.length; i++) {
			double sum = 0.0;
			ArrayList<Pair> row = matrix[i];
			for (int j = 0; j != row.size(); j++) {
				int col = row.get(j).getFirst();
				double val = row.get(j).getSecond();
				sum += val * vector[col];
			}
			result[i] = sum;
		}
		return result;
	}

	@Override
	// The col is set to be the index in the children.
	public void set(int row, int col, double value) {
		List<Pair> rowList = matrix[row];
		for (Pair pair : rowList) {
			if (pair.first == col) {
				pair.second = value;
				return;
			}
		}
		add(row, col, value);
		;
	}

	public void add(int row, int col, double value) {
		matrix[row].add(new Pair(col, value));
	}

	public void inc(int row, int col, double value) {
		List<Pair> rowList = matrix[row];
		for (Pair pair : rowList) {
			if (pair.first == col) {
				pair.second += value;
				return;
			}
		}
		add(row, col, value);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(rows + "\t" + cols + "\n");
		for (int i = 0; i != matrix.length; i++) {
			for (Pair pair : matrix[i])
				sb.append(i + "\t" + pair.first + "\t" + pair.second + "\n");
		}
		return sb.toString();
	}

	public static class Pair {
		private final int first;
		private double second;

		public Pair(int f, double s) {
			this.first = f;
			this.second = s;
		}

		public int getFirst() {
			return first;
		}

		public double getSecond() {
			return second;
		}

		public void setSecond(double new_second) {
			this.second = new_second;
		}

		@Override
		public boolean equals(Object oth) {
			Pair other = getClass().cast(oth);
			if (other.getFirst() == this.first && other.getSecond() == this.second) {
				return true;
			} else {
				return false;
			}
		}
	}
}