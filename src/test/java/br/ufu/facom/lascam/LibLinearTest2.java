package br.ufu.facom.lascam;

import java.io.IOException;

import de.bwaldvogel.liblinear.Feature;
import de.bwaldvogel.liblinear.FeatureNode;
import de.bwaldvogel.liblinear.Linear;
import de.bwaldvogel.liblinear.Model;
import de.bwaldvogel.liblinear.Parameter;
import de.bwaldvogel.liblinear.Problem;
import de.bwaldvogel.liblinear.SolverType;

public class LibLinearTest2 {

	public static void main(String[] args) throws IOException {

		double C = 1.0; // cost of constraints violation
		double eps = 0.01; // stopping criteria
		Parameter param = new Parameter(SolverType.L2R_LR, C, eps);
		Problem problem = new Problem();
		double[] GROUPS_ARRAY = { 1, 0, 0, 0 };
		problem.y = GROUPS_ARRAY;

		int NUM_OF_TS_EXAMPLES = 4;
		problem.l = NUM_OF_TS_EXAMPLES;
		problem.n = 2;

		FeatureNode[] instance1 = { new FeatureNode(1, 1), new FeatureNode(2, 1) };
		FeatureNode[] instance2 = { new FeatureNode(1, -1), new FeatureNode(2, 1) };
		FeatureNode[] instance3 = { new FeatureNode(1, -1), new FeatureNode(2, -1) };
		FeatureNode[] instance4 = { new FeatureNode(1, 1), new FeatureNode(2, -1) };

		FeatureNode[] instance5 = { new FeatureNode(1, 1), new FeatureNode(2, -0.1) };
		FeatureNode[] instance6 = { new FeatureNode(1, -0.1), new FeatureNode(2, 1) };
		FeatureNode[] instance7 = { new FeatureNode(1, -0.1), new FeatureNode(2, -0.1) };

		FeatureNode[][] testSetWithUnknown = { instance5, instance6, instance7 };

		FeatureNode[][] trainingSetWithUnknown = { instance1, instance2, instance3, instance4 };

		problem.x = trainingSetWithUnknown;

		Model m = Linear.train(problem, param);

		for (int i = 0; i < trainingSetWithUnknown.length; i++)
			System.out.println(" Train.instance =  " + i + " =>  " + Linear.predict(m, trainingSetWithUnknown[i]));
		System.out.println("---------------------");
		double[] estimates = new double[testSetWithUnknown.length];
		for (int i = 0; i < testSetWithUnknown.length; i++) {
			System.out.println(" Test.instance =  " + i + " =>  " + Linear.predict(m, testSetWithUnknown[i]));
			System.out.println(" Test.instance prob =  " + i + " =>  " + Linear.predictProbability(m, trainingSetWithUnknown[i], estimates));
		}
			
		System.out.println(estimates);
		
		
		
	}
}