package br.ufu.facom.lascam;

import java.text.DecimalFormat;
import java.util.ArrayList;

import weka.classifiers.Evaluation;
import weka.classifiers.functions.Logistic;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;

public class WekaLogisticClassifierTest {
	// LogisticClassifier<String, Double> stanfordClassifier;

	public static void main(String[] args) throws Exception {

		// numeric feature
		Attribute feature1 = new Attribute("f1");
		Attribute feature2 = new Attribute("f2");
		Attribute feature3 = new Attribute("f3");
		Attribute feature4 = new Attribute("f4");
		Attribute feature5 = new Attribute("f5");
		Attribute feature6 = new Attribute("f6");
		Attribute feature7 = new Attribute("f7");
		Attribute feature8 = new Attribute("f8");

		// Declare the class attribute along with its values
		ArrayList<String> classVal = new ArrayList<>(2);
		classVal.add("duplicated");
		classVal.add("non");
		Attribute classAttribute = new Attribute("theClass", classVal);

		ArrayList<Attribute> wekaAttributes = new ArrayList<>(2);
		wekaAttributes.add(feature1);
		wekaAttributes.add(feature2);
		wekaAttributes.add(feature3);
		wekaAttributes.add(feature4);
		wekaAttributes.add(feature5);
		wekaAttributes.add(feature6);
		wekaAttributes.add(feature7);
		wekaAttributes.add(feature8);

		wekaAttributes.add(classAttribute);

		// Create an empty training set
		Instances trainingData = new Instances("Train", wekaAttributes, 4);
		// Set class index
		trainingData.setClassIndex(8);

		// Create the instance
		Instance iExample = new DenseInstance(9);
		iExample.setValue((Attribute) wekaAttributes.get(0), 1.2);
		iExample.setValue((Attribute) wekaAttributes.get(1), 0.8);
		iExample.setValue((Attribute) wekaAttributes.get(2), 1.1);
		iExample.setValue((Attribute) wekaAttributes.get(3), 0.9);
		iExample.setValue((Attribute) wekaAttributes.get(4), 1.3);
		iExample.setValue((Attribute) wekaAttributes.get(5), 1.4);
		iExample.setValue((Attribute) wekaAttributes.get(6), 1.0);
		iExample.setValue((Attribute) wekaAttributes.get(7), 1.1);
		iExample.setValue((Attribute) wekaAttributes.get(8), "duplicated");

		// add the instance
		trainingData.add(iExample);

		// another
		iExample = new DenseInstance(9);
		iExample.setValue((Attribute) wekaAttributes.get(0), 1.1);
		iExample.setValue((Attribute) wekaAttributes.get(1), 0.1);
		iExample.setValue((Attribute) wekaAttributes.get(2), 1.1);
		iExample.setValue((Attribute) wekaAttributes.get(3), 0.9);
		iExample.setValue((Attribute) wekaAttributes.get(4), 1.1);
		iExample.setValue((Attribute) wekaAttributes.get(5), 1.1);
		iExample.setValue((Attribute) wekaAttributes.get(6), 1.0);
		iExample.setValue((Attribute) wekaAttributes.get(7), 1.1);
		iExample.setValue((Attribute) wekaAttributes.get(8), "duplicated");

		// add the instance
		trainingData.add(iExample);

		iExample = new DenseInstance(9);
		iExample.setValue((Attribute) wekaAttributes.get(0), 4.2);
		iExample.setValue((Attribute) wekaAttributes.get(1), 3.8);
		iExample.setValue((Attribute) wekaAttributes.get(2), 4.1);
		iExample.setValue((Attribute) wekaAttributes.get(3), 4.9);
		iExample.setValue((Attribute) wekaAttributes.get(4), 4.3);
		iExample.setValue((Attribute) wekaAttributes.get(5), 4.4);
		iExample.setValue((Attribute) wekaAttributes.get(6), 4.0);
		iExample.setValue((Attribute) wekaAttributes.get(7), 4.1);
		iExample.setValue((Attribute) wekaAttributes.get(8), "non");

		// add the instance
		trainingData.add(iExample);

		iExample = new DenseInstance(9);
		iExample.setValue((Attribute) wekaAttributes.get(0), 4.1);
		iExample.setValue((Attribute) wekaAttributes.get(1), 3.1);
		iExample.setValue((Attribute) wekaAttributes.get(2), 4.1);
		iExample.setValue((Attribute) wekaAttributes.get(3), 4.1);
		iExample.setValue((Attribute) wekaAttributes.get(4), 4.1);
		iExample.setValue((Attribute) wekaAttributes.get(5), 4.1);
		iExample.setValue((Attribute) wekaAttributes.get(6), 4.1);
		iExample.setValue((Attribute) wekaAttributes.get(7), 4.1);
		iExample.setValue((Attribute) wekaAttributes.get(8), "non");

		// add the instance
		trainingData.add(iExample);

		// testing !!!
		// Create test set alike training data
		Instances testData = new Instances("Test", wekaAttributes, 1);
		// don't know whether it is necessary to set
		testData.setClassIndex(8);
		// Create the instance
		Instance test = new DenseInstance(9);
		test.setValue((Attribute) wekaAttributes.get(0), 1.2);
		test.setValue((Attribute) wekaAttributes.get(1), 0.8);
		test.setValue((Attribute) wekaAttributes.get(2), 1.1);
		test.setValue((Attribute) wekaAttributes.get(3), 0.9);
		test.setValue((Attribute) wekaAttributes.get(4), 1.3);
		test.setValue((Attribute) wekaAttributes.get(5), 1.4);
		test.setValue((Attribute) wekaAttributes.get(6), 1.0);
		test.setValue((Attribute) wekaAttributes.get(7), 1.1);
		//just for testing
		test.setValue((Attribute) wekaAttributes.get(8), "duplicated");

		testData.add(test);
		
		//
		Logistic cls = new Logistic();
		cls.buildClassifier(trainingData);

		Evaluation eval = new Evaluation(trainingData);
		eval.evaluateModel(cls, testData);
		System.out.println(eval.toSummaryString("\nResults\n======\n", false));

		double[] distribution = cls.distributionForInstance(test);
		
		DecimalFormat df = new DecimalFormat("#0.000000");
		
		System.out.println("Duplicated: " + distribution[0]);
		System.out.println("Duplicated: " + df.format(distribution[0]));
		System.out.println("Non: " + df.format(distribution[1]));
	}
}
