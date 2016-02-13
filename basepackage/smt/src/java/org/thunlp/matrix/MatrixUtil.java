package org.thunlp.matrix;

import org.thunlp.io.TextFileReader;

import java.io.IOException;
import java.io.File;

public class MatrixUtil {
	/**
	 * The first line of the input file should be "rows cols", and the following
	 * line should be "rowIndex colIndex value"
	 * 
	 * @param file
	 *            the input file
	 * @return
	 * @throws IOException
	 */
	public static SparseMatrix loadSparseMatrix(String filename) throws IOException, IllegalFormatException {
		return loadSparseMatrix(new File(filename));
	}

	/**
	 * The first line of the input file should be "rows cols", and the following
	 * line should be "rowIndex colIndex value"
	 * 
	 * @param file
	 *            the input file
	 * @return
	 * @throws IOException
	 */
	public static SparseMatrix loadSparseMatrix(File file) throws IOException, IllegalFormatException {
		String str;
		TextFileReader reader = new TextFileReader(file);
		str = reader.readLine();
		String[] parts = str.split("\\s+");
		if (parts.length != 2)
			throw new IllegalFormatException();
		int rows = Integer.parseInt(parts[0]);
		int cols = Integer.parseInt(parts[1]);
		SparseMatrix matrix = new SparseMatrix(rows, cols);
		while ((str = reader.readLine()) != null) {
			parts = str.split("\\s+");
			if (parts.length != 3)
				throw new IllegalFormatException();
			int row = Integer.parseInt(parts[0]);
			int col = Integer.parseInt(parts[1]);
			double value = Double.parseDouble(parts[2]);
			matrix.add(row, col, value);
		}
		reader.close();
		return matrix;
	}

	public static NormalMatrix loadSquareMatrix(String filename) throws IOException {
		return loadNormalSquareMatrix(new File(filename));
	}

	public static NormalMatrix loadNormalSquareMatrix(File file) throws IOException {
		TextFileReader br = new TextFileReader(file);
		String str;
		str = br.readLine();
		String[] tmp = str.split("\\s");
		NormalMatrix matrix = new NormalMatrix(tmp.length, tmp.length);
		int j = 0;
		do {
			tmp = str.split("\\s");
			for (int i = 0; i != tmp.length; i++)
				matrix.set(j, i, Double.parseDouble(tmp[i]));
			j++;
		} while ((str = br.readLine()) != null);
		return matrix;
	}

	public static void printMatrix(NormalMatrix matrix, java.io.Writer writer) throws IOException {
		for (int i = 0; i != matrix.getRowsCount(); i++) {
			for (int j = 0; j != matrix.getColsCount(); j++) {
				writer.write(matrix.get(i, j) + "\t");
			}
			writer.write('\n');
		}
	}

	@SuppressWarnings("serial")
	public static class IllegalFormatException extends Exception {

	}

	public static void main(String[] argv) throws IOException {
		java.io.BufferedWriter bw = new java.io.BufferedWriter(
				new java.io.FileWriter("/home/lipeng/Desktop/2_output2.matrix"));
		printMatrix(loadSquareMatrix("/home/lipeng/Desktop/2_output.matrix"), bw);
		bw.close();
	}
};