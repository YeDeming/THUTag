package org.thunlp.matrix.pagerank;

import java.util.ArrayList;

import org.thunlp.matrix.MatrixInterface;
import org.thunlp.matrix.SparseMatrix.Pair;
import org.thunlp.matrix.SparseMatrix;

public class DiffusionRank {

	public static final double DEFAULT_D = 0.85;

	public static final double DEFAULT_I = 1.0;

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

	public static double[] diffusionRank(MatrixInterface adjacentMatrix, double gamma, int maxIteration) {
		double[] init = new double[adjacentMatrix.getRowsCount()];
		for (int i = 0; i != init.length; i++)
			init[i] = 1.0;
		return diffusionRank(adjacentMatrix, maxIteration, gamma, DEFAULT_D, init);
	}

	public static double[] diffusionRank(MatrixInterface adjacentMatrix, int maxIteration, double gamma, double d,
			double[] init) {
		double[] impact = new double[adjacentMatrix.getRowsCount()];
		for (int i = 0; i != init.length; i++)
			impact[i] = DEFAULT_I;
		return diffusionRank(adjacentMatrix, maxIteration, gamma, d, init, impact);
	}

	public static double[] diffusionRank(MatrixInterface adjacentMatrix, int maxIteration, double gamma, double d,
			double[] init, double[] impact) {
		double[] result = new double[init.length];
		for (int i = 0; i != init.length; i++)
			result[i] = init[i];
		int iteration = 0;
		double d2 = gamma / maxIteration;
		while (iteration < maxIteration) {
			iteration++;
			double[] oldResult = result;
			result = adjacentMatrix.multiply(result);
			for (int i = 0; i != result.length; i++)
				result[i] = (1 - d2) * oldResult[i] + d2 * (impact[i] * (1.0 - d) + d * result[i]);
		}
		return result;
	}
}