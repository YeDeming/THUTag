package org.thunlp.matrix.pagerank;

import java.util.ArrayList;

import org.thunlp.matrix.MatrixInterface;
import org.thunlp.matrix.NormalMatrix;
import org.thunlp.matrix.SparseMatrix.Pair;
import org.thunlp.matrix.SparseMatrix;

public class PageRank {

	public static final double DEFAULT_D = 0.85;

	public static final double DEFAULT_I = 1.0;

	/**
	 * Transfer the adjacency matrix into the form pagerank can use, that's
	 * normalization and transposition
	 * 
	 * @param adjMatrix
	 *            adjacency matrix, A_{i,j}=1 if there is an edge from vertex i
	 *            to j
	 */
	public static void prepareMatrix(MatrixInterface adjMatrix) {
		if (adjMatrix instanceof SparseMatrix) {
			prepareSparseMatrix((SparseMatrix) adjMatrix);
		} else {
			for (int i = 0; i != adjMatrix.getRowsCount(); i++) {
				double sum = 0.0;
				for (int j = 0; j != adjMatrix.getColsCount(); j++) {
					sum += adjMatrix.get(i, j);
				}
				if (sum != 0.0) {
					for (int j = 0; j != adjMatrix.getColsCount(); j++)
						adjMatrix.set(i, j, adjMatrix.get(i, j) / sum);
				} else {
					double tmp = 1.0 / adjMatrix.getColsCount();
					for (int j = 0; j != adjMatrix.getColsCount(); j++) {
						adjMatrix.set(i, j, tmp);
					}
				}
			}
			adjMatrix.inv();
		}
	}

	/**
	 * Transfer the adjacency matrix into the form pagerank can use, that's
	 * normalization and transposition
	 * 
	 * @param adjMatrix
	 *            adjacency matrix, A_{i,j}=1 if there is an edge from vertex i
	 *            to j
	 */
	protected static void prepareSparseMatrix(SparseMatrix adjMatrix) {
		for (int i = 0; i != adjMatrix.getRowsCount(); i++) {
			double sum = 0.0;
			ArrayList<Pair> row = adjMatrix.getRow(i);
			for (int j = 0; j != row.size(); j++) {
				sum += row.get(j).getSecond();
			}
			if (sum != 0.0) {
				for (int j = 0; j != row.size(); j++) {
					double old_value = row.get(j).getSecond();
					row.get(j).setSecond(old_value / sum);
				}
			}
		}
		adjMatrix.inv();
	}

	/**
	 * Call prepareMatrix or prepareSparseMatrix first
	 * 
	 * @param preparedMatrix
	 * @param maxIteration
	 * @return
	 */
	public static double[] pageRank(MatrixInterface preparedMatrix, int maxIteration) {
		double[] init = new double[preparedMatrix.getRowsCount()];
		for (int i = 0; i != init.length; i++)
			init[i] = 1.0;
		return pageRank(preparedMatrix, maxIteration, DEFAULT_D, init);
	}

	/**
	 * Call prepareMatrix or prepareSparseMatrix first
	 * 
	 * @param preparedMatrix
	 * @param maxIteration
	 * @param d
	 *            pageRank=d*A*pageRank+(1-d)*1/N
	 * @param init
	 *            initial value
	 * @return
	 */
	public static double[] pageRank(MatrixInterface preparedMatrix, int maxIteration, double d, double[] init) {
		double[] impact = new double[preparedMatrix.getRowsCount()];
		for (int i = 0; i != init.length; i++)
			impact[i] = DEFAULT_I;
		return pageRank(preparedMatrix, maxIteration, d, init, impact);
	}

	/**
	 * Call prepareMatrix or prepareSparseMatrix first
	 * 
	 * @param preparedMatrix
	 * @param maxIteration
	 * @param d
	 *            pageRank=d*A*pageRank+(1-d)*impact
	 * @param init
	 *            initial value
	 * @param impact
	 * @return
	 */
	public static double[] pageRank(MatrixInterface preparedMatrix, int maxIteration, double d, double[] init,
			double[] impact) {
		double[] result = new double[init.length];
		for (int i = 0; i != init.length; i++)
			result[i] = init[i];
		int iteration = 0;
		// Normalize each column to 1.
		/*
		 * for (int col = 0; col < adjacentMatrix.getColsCount(); col++) {
		 * double colNorm = 0.0; for (int row = 0; row <
		 * adjacentMatrix.getRowsCount(); row++) { colNorm +=
		 * adjacentMatrix.get(row, col); } for (int row = 0; row <
		 * adjacentMatrix.getRowsCount(); row++) { adjacentMatrix.set( row, col,
		 * adjacentMatrix.get(row, col) / colNorm ); } }
		 */
		// Compute PageRank.
		while (iteration < maxIteration) {
			iteration++;
			result = preparedMatrix.multiply(result);
			for (int i = 0; i != result.length; i++)
				result[i] = impact[i] * (1.0 - d) + d * result[i];
		}
		return result;
	}

	public static void main(String[] argv) {
		MatrixInterface matrix = new NormalMatrix(8, 8);
		matrix.set(1, 0, 1);
		matrix.set(0, 1, 1);
		matrix.set(1, 3, 1);
		matrix.set(1, 5, 1);
		matrix.set(1, 7, 8);

		matrix.set(2, 0, 2);
		matrix.set(2, 1, 2);
		matrix.set(2, 7, 8);

		matrix.set(3, 2, 1.0);
		matrix.set(3, 4, 2);
		matrix.set(3, 7, 8);

		matrix.set(4, 1, 2);
		matrix.set(4, 6, 2);
		matrix.set(4, 7, 8);

		matrix.set(5, 6, 2);
		matrix.set(5, 7, 8);

		matrix.set(6, 7, 8);

		matrix.set(7, 4, 2);
		matrix.set(7, 7, 8);

		PageRank.prepareMatrix(matrix);
		for (double d : pageRank(matrix, 100))
			System.out.println(d);
		System.out.println("\n\n");

		// ================================================
		SparseMatrix adjMatrix = new SparseMatrix(4, 4);
		double[] init = { 1.0, 1.0, 1.0, 1.0 };
		double[] impact = { 1.0, 1.0, 1.0, 1.0 };
		adjMatrix.add(0, 1, 1);
		adjMatrix.add(2, 0, 1);
		adjMatrix.add(1, 2, 1);
		adjMatrix.add(2, 1, 1);
		adjMatrix.add(2, 3, 1);
		adjMatrix.add(3, 1, 1);
		PageRank.prepareMatrix(adjMatrix);
		double[] result = PageRank.pageRank(adjMatrix, 10000, 0.85, impact, init);
		for (int i = 0; i < result.length; i++) {
			System.out.println(result[i]);
		}

		System.out.println("\n\n");

		matrix = new NormalMatrix(4, 4);
		matrix.set(0, 1, 1);
		matrix.set(2, 0, 1);
		matrix.set(1, 2, 1);
		matrix.set(2, 1, 1);
		matrix.set(2, 3, 1);
		matrix.set(3, 1, 1);
		PageRank.prepareMatrix(matrix);
		double[] result2 = PageRank.pageRank(matrix, 10000, 0.85, impact, init);
		for (int i = 0; i < result2.length; i++) {
			System.out.println(result2[i]);
		}
	}
}