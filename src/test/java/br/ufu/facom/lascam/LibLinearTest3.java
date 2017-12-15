package br.ufu.facom.lascam;

import java.io.IOException;

import de.bwaldvogel.liblinear.Feature;
import de.bwaldvogel.liblinear.FeatureNode;
import de.bwaldvogel.liblinear.Linear;
import de.bwaldvogel.liblinear.Model;
import de.bwaldvogel.liblinear.Parameter;
import de.bwaldvogel.liblinear.Problem;
import de.bwaldvogel.liblinear.SolverType;

public class LibLinearTest3 {

	public static void main(String[] args) throws IOException {

		double[] GROUPS_ARRAY = { 1, 1, 1, 1, -1,-1,-1,-1 };
		
		
		FeatureNode[] training1 = { new FeatureNode(1, 0.02), new FeatureNode(2, 0.13), new FeatureNode(3, 0.07), new FeatureNode(4, 0.01) };
		FeatureNode[] training2 = { new FeatureNode(1, 0.01), new FeatureNode(2, 0.11), new FeatureNode(3, 0.00), new FeatureNode(4, 0.02) };
		FeatureNode[] training3 = { new FeatureNode(1, 0.03), new FeatureNode(2, 0.18), new FeatureNode(3, 0.01), new FeatureNode(4, 0.08) };
		FeatureNode[] training4 = { new FeatureNode(1, 0.03), new FeatureNode(2, 0.19), new FeatureNode(3, 0.00), new FeatureNode(4, 0.02) };
		
		FeatureNode[] training5 = { new FeatureNode(1, 0.51), new FeatureNode(2, 0.21), new FeatureNode(3, 0.01), new FeatureNode(4, 0.73) };
		FeatureNode[] training6 = { new FeatureNode(1, 0.62), new FeatureNode(2, 0.31), new FeatureNode(3, 0.55), new FeatureNode(4, 0.85) };
		FeatureNode[] training7 = { new FeatureNode(1, 0.33), new FeatureNode(2, 0.74), new FeatureNode(3, 0.87), new FeatureNode(4, 0.92) };
		FeatureNode[] training8 = { new FeatureNode(1, 0.74), new FeatureNode(2, 0.85), new FeatureNode(3, 0.55), new FeatureNode(4, 0.99) };
		
		FeatureNode[] test1 = { new FeatureNode(1, 0.02), new FeatureNode(2, 0.17), new FeatureNode(3, 0.07), new FeatureNode(4, 0.02) };
		FeatureNode[] test2 = { new FeatureNode(1, 0.64), new FeatureNode(2, 0.75), new FeatureNode(3, 0.25), new FeatureNode(4, 0.79) };
		
		
		FeatureNode[][] trainingSet = { training1, training2, training3, training4,training5, training6,training7,training8 };
		
		FeatureNode[][] testSetWithUnknown = { test1, test2};
			
		
		Problem problem = new Problem();
		problem.l = trainingSet.length;
		problem.n = 4;
		problem.x = trainingSet;
		problem.y = GROUPS_ARRAY;

		SolverType solver = SolverType.L2R_LR; // -s 0
		double C = 1.0; // cost of constraints violation
		double eps = 0.001; // stopping criteria

		Parameter parameter = new Parameter(solver, C, eps);
		Model m = Linear.train(problem, parameter);

		for (int i = 0; i < trainingSet.length; i++)
			System.out.println(" Train.instance =  " + i + " =>  " + Linear.predict(m, trainingSet[i]));
		System.out.println("---------------------");
		double[] estimates = new double[testSetWithUnknown.length];
		for (int i = 0; i < testSetWithUnknown.length; i++) {
			System.out.println(" Test.instance     =  " + i + " =>  " + Linear.predict(m, testSetWithUnknown[i]));
			System.out.println(" Test.instance Prob=  " + i + " =>  " + Linear.predictProbability(m, trainingSet[i], estimates));
			System.out.println(estimates);
		}
		
		for (int i = 0; i < testSetWithUnknown.length; i++) {
			System.out.println(estimates[i]);
		}
			
		
		
		
		
	}
}