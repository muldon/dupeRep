package br.ufu.facom.lascam;

import java.util.ArrayList;
import java.util.List;

import edu.stanford.nlp.classify.Dataset;
import edu.stanford.nlp.classify.LogisticClassifier;
import edu.stanford.nlp.classify.LogisticClassifierFactory;

public class LogisticClassifierTest1 {
	LogisticClassifier<String, Double> classifier;
	
	public static void main(String[] args) {
		Dataset<String, Double> trainingData = new Dataset<>();
		
		List<Double> classFeatures = new ArrayList<>();
		classFeatures.add(1.2);
		classFeatures.add(0.8);
		classFeatures.add(1.1);
		classFeatures.add(0.9);
		classFeatures.add(1.3);
		classFeatures.add(1.4);
		classFeatures.add(1.0);
		classFeatures.add(1.1);
		
		List<Double> classFeatures2 = new ArrayList<>();
		
		classFeatures2.add(1.1);
		classFeatures2.add(0.1);
		classFeatures2.add(1.1);
		classFeatures2.add(0.9);
		classFeatures2.add(1.1);
		classFeatures2.add(1.1);
		classFeatures2.add(1.0);
		classFeatures2.add(1.1);
		
		trainingData.add(classFeatures, "duplicated");
		trainingData.add(classFeatures2, "duplicated");
		
		
		List<Double> classFeatures200 = new ArrayList<>();
		classFeatures200.add(4.2);
		classFeatures200.add(3.8);
		classFeatures200.add(4.1);
		classFeatures200.add(4.9);
		classFeatures200.add(4.3);
		classFeatures200.add(4.4);
		classFeatures200.add(4.0);
		classFeatures200.add(4.1);
		
		List<Double> classFeatures201 = new ArrayList<>();
		classFeatures201.add(4.1);
		classFeatures201.add(3.1);
		classFeatures201.add(4.1);
		classFeatures201.add(4.1);
		classFeatures201.add(4.1);
		classFeatures201.add(4.1);
		classFeatures201.add(4.1);
		classFeatures201.add(4.1);
		
		trainingData.add(classFeatures200, "non");
		trainingData.add(classFeatures201, "non");
				
		
		LogisticClassifierFactory<String, Double> factory = new LogisticClassifierFactory<>();
		LogisticClassifier<String, Double> classifier = factory.trainClassifier(trainingData);
		
		List<Double> testingList = new ArrayList<>();
		testingList.add(1.1);
		testingList.add(0.8);
		testingList.add(1.1);
		testingList.add(0.9);
		testingList.add(1.0);
		testingList.add(1.4);
		testingList.add(1.0);
		testingList.add(1.1);
		
		Double probability = classifier.probabilityOf(testingList, "duplicated");
		System.out.println(probability);
		
		probability = classifier.probabilityOf(testingList, "non");
		System.out.println(probability);
		
	}

}
