package org.thunlp.matrix;

public class MatrixFactory {
	public final static String NORMAL_MATRIX = "normal";

	public final static String SPARSE_MATRIX = "sparse";

	public static MatrixInterface getMatrix(int rows, int cols, String matrixName) {
		if (matrixName.equals(NORMAL_MATRIX))
			return new NormalMatrix(rows, cols);
		else if (matrixName.equals(SPARSE_MATRIX))
			return new SparseMatrix(rows, cols);
		else
			return null;
	}
}
