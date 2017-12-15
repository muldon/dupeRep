package br.ufu.facom.lascam;

import java.io.IOException;

import de.bwaldvogel.liblinear.Feature;
import de.bwaldvogel.liblinear.FeatureNode;
import de.bwaldvogel.liblinear.Linear;
import de.bwaldvogel.liblinear.Model;
import de.bwaldvogel.liblinear.Parameter;
import de.bwaldvogel.liblinear.Problem;
import de.bwaldvogel.liblinear.SolverType;

public class LibLinearTest {

	// 1 = quadratic, -1 = non-quadratic
	static double[] GROUPS_ARRAY = { 1, 1, 1, 1, -1, -1, -1, -1 };

	// quadratic
	static FeatureNode[] tp1 = { new FeatureNode(1, 2), new FeatureNode(2, 4) };
	static FeatureNode[] tp2 = { new FeatureNode(1, 4), new FeatureNode(2, 8) };
	static FeatureNode[] tp3 = { new FeatureNode(1, 9), new FeatureNode(2, 8) };
	static FeatureNode[] tp4 = { new FeatureNode(1, 10), new FeatureNode(2, 10) };

	// not quadratic
	static FeatureNode[] tp5 = { new FeatureNode(1, 51), new FeatureNode(2, 60) };
	static FeatureNode[] tp6 = { new FeatureNode(1, 32), new FeatureNode(2, 40) };
	static FeatureNode[] tp7 = { new FeatureNode(1, 63), new FeatureNode(2, 90) };
	static FeatureNode[] tp8 = { new FeatureNode(1, 41), new FeatureNode(2, 20) };

	// unknown
	static FeatureNode[] up1 = { new FeatureNode(1, 2), new FeatureNode(2, 7) };
	static FeatureNode[] up2 = { new FeatureNode(1, 55), new FeatureNode(2, 55) };
	static FeatureNode[] up3 = { new FeatureNode(1, 4), new FeatureNode(2, 2) };

	static FeatureNode[][] trainingSetWithUnknown = { tp1, tp2, tp3, tp4, tp5, tp6, tp7, tp8 };

	public static void main(String[] args) throws IOException {

		Problem problem = new Problem();
		problem.l = trainingSetWithUnknown.length;
		problem.n = 2;
		problem.x = trainingSetWithUnknown;
		problem.y = GROUPS_ARRAY;

		SolverType solver = SolverType.L2R_LR; // -s 0
		double C = 1.0; // cost of constraints violation
		double eps = 0.001; // stopping criteria

		Parameter parameter = new Parameter(solver, C, eps);
		Model model = Linear.train(problem, parameter);

		Feature[] instance = up3;
		double prediction = Linear.predict(model, instance);

		System.out.println("prediction : " + prediction);
	}
}