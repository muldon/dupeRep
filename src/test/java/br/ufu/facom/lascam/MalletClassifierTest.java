package br.ufu.facom.lascam;


import cc.mallet.classify.Classifier;
import cc.mallet.classify.ClassifierTrainer;
import cc.mallet.classify.MaxEntTrainer;
import cc.mallet.classify.Trial;
import cc.mallet.pipe.Pipe;
import cc.mallet.pipe.SerialPipes;
import cc.mallet.types.Alphabet;
import cc.mallet.types.FeatureVector;
import cc.mallet.types.Instance;
import cc.mallet.types.InstanceList;

public class MalletClassifierTest {
    // LogisticClassifier<String, Double> classifier;

    public static void main(String[] args) throws Exception {

        Alphabet alphabet = new Alphabet();

        FeatureVector featureVector1 = new FeatureVector(alphabet, new String[]{"f1", "f2", "f3", "f4", "f5", "f6", "f7", "f8"},
                new double[]{1.2, 0.8, 1.1, 0.9, 1.3, 1.4, 1.0, 1.1});
        Instance instance1 = new Instance(featureVector1, "duplicated", "name1", null);

        FeatureVector featureVector2 = new FeatureVector(alphabet, new String[]{"f1", "f2", "f3", "f4", "f5", "f6", "f7", "f8"},
                new double[]{1.1, 0.1, 1.1, 0.9, 1.1, 1.1, 1.0, 1.1});
        Instance instance2 = new Instance(featureVector1, "duplicated", "name2", null);

        FeatureVector featureVector3 = new FeatureVector(alphabet, new String[]{"f1", "f2", "f3", "f4", "f5", "f6", "f7", "f8"},
                new double[]{4.2, 3.8, 4.1, 4.9, 4.3, 4.4, 4.0, 4.1});
        Instance instance3 = new Instance(featureVector3, "non", "name3", null);

        FeatureVector featureVector4 = new FeatureVector(alphabet, new String[]{"f1", "f2", "f3", "f4", "f5", "f6", "f7", "f8"},
                new double[]{4.1, 3.1, 4.1, 4.1, 4.1, 4.1, 4.1, 4.1});
        Instance instance4 = new Instance(featureVector4, "non", "name4", null);

        InstanceList trainingInstances = new InstanceList(new SerialPipes());
        //trainingInstances.addAll()
        trainingInstances.add(instance1);
        trainingInstances.add(instance2);
        trainingInstances.add(instance3);
        trainingInstances.add(instance4);

        FeatureVector featureVector5 = new FeatureVector(alphabet, new String[]{"f1", "f2", "f3", "f4", "f5", "f6", "f7", "f8"},
                new double[]{1.2, 0.8, 1.1, 0.9, 1.3, 1.4, 1.0, 1.1});
        Instance instance5 = new Instance(featureVector5, "duplicated", "name5", null);

        InstanceList testInstances = new InstanceList(new SerialPipes());
        //trainingInstances.addAll()
        testInstances.add(instance5);

        ClassifierTrainer trainer = new MaxEntTrainer();
        Classifier classifier = trainer.train(trainingInstances);

        Trial trial = new Trial(classifier, testInstances);

        System.out.println("Accuracy: " + trial.getAccuracy());

        // precision, recall, and F1 are calcuated for a specific
        //  class, which can be identified by an object (usually
        //  a String) or the integer ID of the class

        System.out.println("F1 for class 'duplicated': " + trial.getF1("duplicated"));

        System.out.println("Precision for class '" +
                classifier.getLabelAlphabet().lookupLabel(1) + "': " +
                trial.getPrecision(1));
    }
}
