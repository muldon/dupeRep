package br.ufu.facom.lascam.dupe.service;

import java.io.IOException;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang.time.DurationFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import br.ufu.facom.lascam.dupe.domain.Experiment;
import br.ufu.facom.lascam.dupe.domain.Pair;
import br.ufu.facom.lascam.dupe.domain.Post;
import br.ufu.facom.lascam.dupe.domain.Question;
import br.ufu.facom.lascam.dupe.domain.RecallRate;
import br.ufu.facom.lascam.dupe.repository.ExperimentRepository;
import br.ufu.facom.lascam.dupe.repository.FeatureRepository;
import br.ufu.facom.lascam.dupe.repository.GenericRepository;
import br.ufu.facom.lascam.dupe.repository.PostsRepository;
import br.ufu.facom.lascam.dupe.repository.RecallRateRepository;
import br.ufu.facom.lascam.dupe.util.DupeUtils;
import edu.stanford.nlp.classify.Dataset;
import edu.stanford.nlp.classify.LogisticClassifier;
import edu.stanford.nlp.classify.LogisticClassifierFactory;
import weka.classifiers.functions.Logistic;
import weka.core.Instance;
import weka.core.Instances;


@Service
public class ClassifyService {
	@Value("${maxResultSize}")
	public  Integer maxResultSize;   
	
	@Value("${trm}")
	public  String trm;  
	
	@Value("${percentOfTestedQuestions}")
	public  Integer percentOfTestedQuestions;   
	
	@Value("${spring.datasource.url}")
	public String database;
	
	//public Integer maxResultSize = 4000;
	//public  Integer maxTestSearchNumberForTest = maxConsideredSeachNumberForClassifier;
	public  Integer limitPairsForTest = null;  //null para todos 
	public  Integer limitIndexQuestionsForTests = null; //null para todos
	//public  Integer recallRateCropNumber = maxConsideredSeachNumberForClassifier; 
	public static Float bm25ParameterK = 0.05f;  //0.05 = default of paper 
	public static Float bm25ParameterB = 0.03f;  //0.03 = default of paper 
		
	
	@Value("${tagFilter}")
	public String tagFilter;
	
	@Value("${observation}")
	public String observation;
	
	@Value("${lote}")
	public Integer lote;
		

	private Map<Pair, Double> rankingStanfordClassifier;
	private Map<Pair, Double> rankingWekaClassifier;
	private Map<Pair, Double> rankingSumCosine;
	private Map<Pair, Integer> rankingTrm;
	private Map<Pair, Double> rankingDupPredictor;
	
	private List<Pair> toTest;
	
		
	protected static SimpleDateFormat dateFormat;
	
	@Autowired
	protected PostsRepository postsRepository;
	
	@Autowired
	protected ExperimentRepository experimentRepository;
	
	@Autowired
	protected RecallRateRepository recallRateRepository;
	
	
	@Autowired
	protected GenericRepository genericRepository;
	
		
	@Autowired
	protected FeatureRepository featureRepository;
	
	@Autowired
	protected DupeUtils dupeUtils;
	
	@Autowired
	protected TerrierSearcherPL2 terrierSearcher;
	
	@Autowired
	protected LuceneSearcherBM25 luceneSearcherBM25;
		
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	List<Question> rawClosedDuplicatedNonMastersByTag;
	
	private long initTimeQuestion;
	private long initTimeLoop; //time to each experiment
	
	
	private long endTimeQuestion;
	private long endTimeLoop;
	
	protected List<Pair> duplicatedPairs;
	protected List<Pair> nonDuplicated;
	protected LogisticClassifier<String, Double> stanfordClassifier;
	//protected Instances wekaTestData;
	
	protected Logistic cls;
	
	
	//SimpleLogisticClassifier simpleLogisticClassifier;
	
	
	public ClassifyService() {
		dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
		cls = new Logistic();
		
	}
	
	

	
	

	private <K, V extends Comparable<? super V>> Integer verifyHitInMemory(Integer nonMasterId, List<Entry<K, V>> subList, String origem) throws IOException {
		int count = 0;
		for (Entry<K, V> entry : subList) {
			count++;
			Pair testPair = (Pair)entry.getKey();
			
			Set<Integer> nonMasters = DupeUtils.getBucketDuplicatiosMap().get(testPair.getQuestion2());
			if(nonMasters!=null && !nonMasters.isEmpty()){
				for(Integer nonMasterTestId: nonMasters){
					 
					if(nonMasterTestId.equals(nonMasterId)){
						String found = "Duplicated found - Origin: "+origem+" -  dup id: " + nonMasterId + " - Master: "+testPair.getQuestion2()+ " - posição: "+count;
						logger.info(found);
						return count;
					}					
			}
		 }
		}

		return -1;
	}
	

	

	private LogisticClassifier<String, Double> trainStanfordClassifier() throws Exception {

		try {
			logger.info("LogisticClassifier trainStanfordClassifier()");
			initTimeQuestion = System.currentTimeMillis();
			
			// 80% for training
			List<Pair> eightyPercentDuplicatedPairs =  duplicatedPairs; //after remove all 
								
			
			Dataset<String, Double> trainingData = new Dataset<>();
			logger.info("Number of pairs to be used in training as duplicated (80%): "+eightyPercentDuplicatedPairs.size());
			trainingData.addAll(dupeUtils.extractFeaturesForTrainingStanfordClassifier(eightyPercentDuplicatedPairs, "duplicated"));
			
			List<Pair> eightyPercentNonDuplicatedPairs = nonDuplicated.subList(0, eightyPercentDuplicatedPairs.size());
			
			logger.info("Number of pairs to be used in training as non duplicated (80%): "+eightyPercentNonDuplicatedPairs.size());
			trainingData.addAll(dupeUtils.extractFeaturesForTrainingStanfordClassifier(eightyPercentNonDuplicatedPairs, "non"));
			
			logger.info("Total of pairs in training: "+trainingData.size());
	
			LogisticClassifierFactory<String, Double> factory = new LogisticClassifierFactory<>();
			
			LogisticClassifier<String, Double> classifier = factory.trainClassifier(trainingData);
			dupeUtils.reportElapsedTime(initTimeQuestion,"classifyService.trainStanfordClassifier()");
			
			eightyPercentDuplicatedPairs = null;
			eightyPercentNonDuplicatedPairs = null;
			
			return classifier;

		} catch (Exception e) {
			System.err.println("Training error!");
			throw e;
		}
	}

	
	
	private void trainWekaClassifier() throws Exception {

		try {
			logger.info("LogisticClassifier trainWekaClassifier()");
			initTimeQuestion = System.currentTimeMillis();
			
			// 80% for training
			List<Pair> eightyPercentDuplicatedPairs =  duplicatedPairs; //after remove all 
			List<Pair> eightyPercentNonDuplicatedPairs = nonDuplicated.subList(0, eightyPercentDuplicatedPairs.size());
			
			logger.info("Number of pairs to be used in training as duplicated (80%): "+eightyPercentDuplicatedPairs.size());
			logger.info("Number of pairs to be used in training as non duplicated (80%): "+eightyPercentNonDuplicatedPairs.size());
			int size = eightyPercentDuplicatedPairs.size()*2;
			Instances trainingData = new Instances("Train", dupeUtils.getWekaAttributes(), size );
			trainingData.setClassIndex(7);
			dupeUtils.extractFeaturesForTrainingWekaClassifier(eightyPercentDuplicatedPairs,"duplicated",trainingData);
			dupeUtils.extractFeaturesForTrainingWekaClassifier(eightyPercentNonDuplicatedPairs,"non",trainingData);
			
			logger.info("Total of pairs in training: "+trainingData.size());
	
			cls.buildClassifier(trainingData);
			
			dupeUtils.reportElapsedTime(initTimeQuestion,"classifyService.trainWekaClassifier()");
			
			eightyPercentDuplicatedPairs = null;
			eightyPercentNonDuplicatedPairs = null;
			
		} catch (Exception e) {
			System.err.println("Training error in trainWekaClassifier!");
			throw e;
		}
	}


	



	public void run() throws Exception {
		DecimalFormat decimalFormat = new DecimalFormat("##.##");
		decimalFormat.setRoundingMode(RoundingMode.DOWN);
		DecimalFormat wekaDf = new DecimalFormat("#0.000000");
		
		dupeUtils.initializeConfigs();
		String duration, timeMessage;
		
		initTimeLoop = System.currentTimeMillis();
		
		float divNumber = 100/(float)percentOfTestedQuestions;
		
		try {
			//test set
			toTest = new ArrayList<Pair>();
			
			duplicatedPairs = genericRepository.fetchPairs(null,tagFilter,true);
			nonDuplicated = genericRepository.fetchPairs(null,tagFilter,false);
			logger.info("Total of duplications for tag "+tagFilter+ " -> "+duplicatedPairs.size());
			
			Collections.reverse(duplicatedPairs);  
			toTest = new ArrayList<Pair>();
			
			for(int i=0; i< duplicatedPairs.size();i++){
				if(i%divNumber==0){
					toTest.add(duplicatedPairs.get(i));
				}
			}
					
			Set<Integer> nonMastersIdsForTest = new LinkedHashSet<>();
			for(Pair pair: toTest) {
				nonMastersIdsForTest.add(pair.getQuestion1());
			}
			
			duplicatedPairs.removeAll(toTest);
			logger.info("Total of duplications to train the stanfordClassifier for tag "+tagFilter+ " -> "+duplicatedPairs.size());
			logger.info("Total of duplications to test for tag "+tagFilter+ " -> "+toTest.size());			
			stanfordClassifier = trainStanfordClassifier();
			trainWekaClassifier();
			
			List<Post> allQuestionsByTag = genericRepository.fetchQuestionsByTag(tagFilter, limitIndexQuestionsForTests);
			
			if(trm.contains("BM25")){
				luceneSearcherBM25.buildSearchManager(tagFilter,allQuestionsByTag);
				luceneSearcherBM25.setSearchSimilarityParams(bm25ParameterK,bm25ParameterB);
				
			}else if(trm.contains("PL2")){
				terrierSearcher.buildSearchManager(tagFilter,allQuestionsByTag);
			}
				
						
			int totalQuestionsToTest = nonMastersIdsForTest.size();
			int hitsMaxStanfordClassifier=0, hits10000StanfordClassifier=0, hits1000StanfordClassifier=0,hits500StanfordClassifier=0, hits100StanfordClassifier=0, hits50StanfordClassifier=0, hits20StanfordClassifier = 0, hits10StanfordClassifier = 0, hits5StanfordClassifier=0,hits1StanfordClassifier=0 ,hitOrderStanfordClassifier = 0;
			int hitsMaxWekaClassifier=0, hits10000WekaClassifier=0, hits1000WekaClassifier=0,hits500WekaClassifier=0, hits100WekaClassifier=0, hits50WekaClassifier=0, hits20WekaClassifier = 0, hits10WekaClassifier = 0, hits5WekaClassifier=0,hits1WekaClassifier=0 ,hitOrderWekaClassifier = 0;
			int hitsMaxTrm=0, hits10000Trm=0,hits1000Trm=0,hits500Trm=0, hits100Trm=0, hits50Trm=0, hits20Trm = 0, hits10Trm = 0, hits5Trm=0,hits1Trm=0 ,hitOrderTrm = 0;
			int hitsMaxSumCosines=0, hits10000SumCosines=0,hits1000SumCosines=0,hits500SumCosines=0, hits100SumCosines=0, hits50SumCosines=0, hits20SumCosines = 0, hits10SumCosines = 0, hits5SumCosines=0,hits1SumCosines=0 ,hitOrderSumCosines = 0;
			int hitsMaxDupPredictor=0, hits10000DupPredictor=0,hits1000DupPredictor=0,hits500DupPredictor=0, hits100DupPredictor=0, hits50DupPredictor=0, hits20DupPredictor = 0, hits10DupPredictor = 0, hits5DupPredictor=0,hits1DupPredictor=0 ,hitOrderDupPredictor = 0;
			
			double recallRateMaxStanfordClassifier = 0d, recallRate10000StanfordClassifier = 0d, recallRate1000StanfordClassifier = 0d, recallRate500StanfordClassifier = 0d,
				   recallRate100StanfordClassifier = 0d, recallRate50StanfordClassifier = 0d, recallRate20StanfordClassifier = 0d, recallRate10StanfordClassifier = 0d,
				   recallRate5StanfordClassifier = 0d, recallRate1StanfordClassifier = 0d;
			
			double recallRateMaxWekaClassifier = 0d, recallRate10000WekaClassifier = 0d, recallRate1000WekaClassifier = 0d, recallRate500WekaClassifier = 0d,
					   recallRate100WekaClassifier = 0d, recallRate50WekaClassifier = 0d, recallRate20WekaClassifier = 0d, recallRate10WekaClassifier = 0d,
					   recallRate5WekaClassifier = 0d, recallRate1WekaClassifier = 0d;
			
			double recallRateMaxTrm = 0d, recallRate10000Trm = 0d, recallRate1000Trm = 0d, recallRate500Trm = 0d,
				   recallRate100Trm = 0d, recallRate50Trm = 0d, recallRate20Trm = 0d, recallRate10Trm = 0d,
				   recallRate5Trm = 0d, recallRate1Trm = 0d;
			
			double recallRateMaxSumCosines = 0d, recallRate10000SumCosines = 0d, recallRate1000SumCosines = 0d, recallRate500SumCosines = 0d,
				   recallRate100SumCosines = 0d, recallRate50SumCosines = 0d, recallRate20SumCosines = 0d, recallRate10SumCosines = 0d,
				   recallRate5SumCosines = 0d, recallRate1SumCosines = 0d;
				
			double recallRateMaxDupPredictor = 0d, recallRate10000DupPredictor = 0d, recallRate1000DupPredictor = 0d, recallRate500DupPredictor = 0d,
					   recallRate100DupPredictor = 0d, recallRate50DupPredictor = 0d, recallRate20DupPredictor = 0d, recallRate10DupPredictor = 0d,
					   recallRate5DupPredictor = 0d, recallRate1DupPredictor = 0d;
		
			logger.info("Initiating recall-rates... \nTotal of unique ids to test: " + totalQuestionsToTest);
					
			
			int count=0;
			Post nonMaster = null;
			Post searchTestingQuestion = null;
			double[] distribution;
			
			
			for (Integer nonMasterIdForTest : nonMastersIdsForTest) {
				count++;
				initTimeQuestion = System.currentTimeMillis();
				rankingStanfordClassifier = new HashMap<>();
				rankingWekaClassifier = new HashMap<>();
				rankingSumCosine =  new HashMap<>();
				rankingTrm = new HashMap<>();
				rankingDupPredictor = new HashMap<>();
				
				nonMaster = postsRepository.findOne(nonMasterIdForTest);
				logger.info("Testing pair: "+count+ " of "+totalQuestionsToTest + " - Question id: "+nonMaster.getId());
								
				//Integer resultSize = terrierSearcher.search(DupeUtils.buildQuestionContent(nonMaster));
				int resultSize=0;
				try {
					
					if(trm.contains("BM25")){
						resultSize = luceneSearcherBM25.search(nonMaster,maxResultSize);
					}else if(trm.contains("PL2")){
						resultSize = terrierSearcher.search(nonMaster.getTitle()+ " "+nonMaster.getBody()+ " "+nonMaster.getTagsSyn());
					}
											
				} catch (Exception e)  {
					logger.error("Erro ao testar questão: "+nonMaster.getId()+ " - desconsiderando...");
					continue;
				}
				
				for (int numTest = 0; numTest < resultSize; numTest++) {
					//int docid = results.getDocids()[numTest];
					//double score = results.getScores()[numTest];
					Integer searchTestingQuestionId = null;
					if(trm.contains("BM25")){
						searchTestingQuestionId = luceneSearcherBM25.getQuestion(numTest);
					}else if(trm.contains("PL2")){
						searchTestingQuestionId = terrierSearcher.getDocno(numTest);
					}
																	
					//Integer searchTestingQuestionId = luceneSearcherBM25.getQuestion(numTest);
					searchTestingQuestion = postsRepository.findOne(searchTestingQuestionId);
					
					if(!searchTestingQuestionId.equals(nonMaster.getId())) { //do not compare the question with itself
						Pair pair = new Pair();
						pair.setId(numTest);
						pair.setDuplicated(true);// really???
						pair.setMaintag(this.tagFilter);
						pair.setQuestion1(nonMaster.getId());
						pair.setQuestion2(searchTestingQuestion.getId());
						
						pair.setFeatures(dupeUtils.extractFeatures(nonMaster, searchTestingQuestion));
						List<Double> classFeatures = dupeUtils.createFeatures(pair);
						
						double probability = stanfordClassifier.probabilityOf(classFeatures, "duplicated");
						rankingStanfordClassifier.put(pair, probability);
						
						Instance wekaTestInstance = dupeUtils.getWekaInstance(pair,classFeatures,"duplicated");
						
						//wekaTestData = new Instances("Test", dupeUtils.getWekaAttributes(), 1);
						//wekaTestData.setClassIndex(8);
						//wekaTestData.add(dupeUtils.getWekaInstance(pair,classFeatures,"duplicated"));
						distribution = cls.distributionForInstance(wekaTestInstance);
						rankingWekaClassifier.put(pair, distribution[0]);
						
						double sumCosine = dupeUtils.getSumOfCosines(pair);
						rankingSumCosine.put(pair, sumCosine);
						
						//0.8d * TT + 0.51d * BB + 0.37d * TagTag;
						double sumCosinesDupPredictor = 0.8d * classFeatures.get(1) + 0.51d * classFeatures.get(2) + 0.37d * classFeatures.get(3);   
						rankingDupPredictor.put(pair, sumCosinesDupPredictor);
						
						rankingTrm.put(pair, numTest);
						pair = null;
						//wekaTestData = null;
						wekaTestInstance = null;
										
						
					}
					
				}
				
				// order as questões por score desc
				List<Entry<Pair, Double>> descCosines       = DupeUtils.entriesSortedByValuesDesc(rankingSumCosine);
				List<Entry<Pair,Integer>> ascSortedPosition = DupeUtils.entriesSortedByValues(rankingTrm);
				List<Entry<Pair, Double>> descSortedEntriesStanford = DupeUtils.entriesSortedByValuesDesc(rankingStanfordClassifier);
				List<Entry<Pair, Double>> descSortedEntriesWeka = DupeUtils.entriesSortedByValuesDesc(rankingWekaClassifier);
				List<Entry<Pair, Double>> descSortedEntriesDupePredictor = DupeUtils.entriesSortedByValuesDesc(rankingDupPredictor);
				
				int listSize = ascSortedPosition.size(); //lists are of same size
				int consideredCropNumber = 1000;
				if(consideredCropNumber>listSize){
					consideredCropNumber = listSize;
				}
				
				
				// first recallK elements
				
				List<Entry<Pair, Double>> subListCosines = new ArrayList<Entry<Pair, Double>>(descCosines.subList(0, consideredCropNumber));
				List<Entry<Pair, Integer>> subListTrm = new ArrayList<Entry<Pair, Integer>>(ascSortedPosition.subList(0, consideredCropNumber));
				
				
				List<Entry<Pair, Double>> subListStanfordClassifier = new ArrayList<Entry<Pair, Double>>(descSortedEntriesStanford.subList(0, consideredCropNumber));
				List<Entry<Pair, Double>> subListWekaClassifier = new ArrayList<Entry<Pair, Double>>(descSortedEntriesWeka.subList(0, consideredCropNumber));
				List<Entry<Pair, Double>> subListDupPredictor = new ArrayList<Entry<Pair, Double>>(descSortedEntriesDupePredictor.subList(0, consideredCropNumber));
				
				hitOrderStanfordClassifier = verifyHitInMemory(nonMaster.getId(), subListStanfordClassifier, "Stanford Classifier");
				subListStanfordClassifier = null;
				descSortedEntriesStanford = null;
				
				hitOrderWekaClassifier = verifyHitInMemory(nonMaster.getId(), subListWekaClassifier, "Weka Classifier");
				subListWekaClassifier = null;
				descSortedEntriesWeka = null;
				
				hitOrderSumCosines = verifyHitInMemory(nonMaster.getId(), subListCosines, "SUM of Cosines");
				subListCosines = null;
				descCosines = null;
				
				hitOrderTrm = verifyHitInMemory(nonMaster.getId(), subListTrm, trm);
				ascSortedPosition = null;
				subListTrm = null;
				
				hitOrderDupPredictor = verifyHitInMemory(nonMaster.getId(), subListDupPredictor, "DupPredictor");
				subListDupPredictor = null;
				descSortedEntriesDupePredictor = null;
				
				if(hitOrderStanfordClassifier>-1){
					/*if(hitOrderClassifier<=maxResultSize){
						hitsMaxClassifier++;
					}
					if(hitOrderClassifier<=100000){
						hits100000Classifier++;
					}
					if(hitOrderClassifier<=10000){
						hits10000Classifier++;
					}*/
					if(hitOrderStanfordClassifier<=1000){
						hits1000StanfordClassifier++;
					}
					if(hitOrderStanfordClassifier<=500){
						hits500StanfordClassifier++;
					}
					if(hitOrderStanfordClassifier<=100){
						hits100StanfordClassifier++;
					}
					if(hitOrderStanfordClassifier<=50){
						hits50StanfordClassifier++;
					}
					if(hitOrderStanfordClassifier<=20){
						hits20StanfordClassifier++;
					}
					if(hitOrderStanfordClassifier<=10){
						hits10StanfordClassifier++;
					}
					if(hitOrderStanfordClassifier<=5){
						hits5StanfordClassifier++;
					}
					if(hitOrderStanfordClassifier==1){
						hits1StanfordClassifier++;
					}
				}
				if(hitOrderWekaClassifier>-1){
					/*if(hitOrderClassifier<=maxResultSize){
						hitsMaxClassifier++;
					}
					if(hitOrderClassifier<=100000){
						hits100000Classifier++;
					}
					if(hitOrderClassifier<=10000){
						hits10000Classifier++;
					}*/
					if(hitOrderWekaClassifier<=1000){
						hits1000WekaClassifier++;
					}
					if(hitOrderWekaClassifier<=500){
						hits500WekaClassifier++;
					}
					if(hitOrderWekaClassifier<=100){
						hits100WekaClassifier++;
					}
					if(hitOrderWekaClassifier<=50){
						hits50WekaClassifier++;
					}
					if(hitOrderWekaClassifier<=20){
						hits20WekaClassifier++;
					}
					if(hitOrderWekaClassifier<=10){
						hits10WekaClassifier++;
					}
					if(hitOrderWekaClassifier<=5){
						hits5WekaClassifier++;
					}
					if(hitOrderWekaClassifier==1){
						hits1WekaClassifier++;
					}
				}
				if(hitOrderTrm>-1){
					/*if(hitOrderTrm<=maxResultSize){
						hitsMaxTrm++;
					}
					if(hitOrderTrm<=100000){
						hits100000Trm++;
					}
					if(hitOrderTrm<=10000){
						hits10000Trm++;
					}*/
					if(hitOrderTrm<=1000){
						hits1000Trm++;
					}
					if(hitOrderTrm<=500){
						hits500Trm++;
					}
					if(hitOrderTrm<=100){
						hits100Trm++;
					}
					if(hitOrderTrm<=50){
						hits50Trm++;
					}
					if(hitOrderTrm<=20){
						hits20Trm++;
					}
					if(hitOrderTrm<=10){
						hits10Trm++;
					}
					if(hitOrderTrm<=5){
						hits5Trm++;
					}
					if(hitOrderTrm==1){
						hits1Trm++;
					}
				}
				if(hitOrderSumCosines>-1){
					/*if(hitOrderSumCosines<=maxResultSize){
						hitsMaxSumCosines++;
					}
					if(hitOrderSumCosines<=100000){
						hits100000SumCosines++;
					}
					if(hitOrderSumCosines<=10000){
						hits10000SumCosines++;
					}*/
					if(hitOrderSumCosines<=1000){
						hits1000SumCosines++;
					}
					if(hitOrderSumCosines<=500){
						hits500SumCosines++;
					}
					if(hitOrderSumCosines<=100){
						hits100SumCosines++;
					}
					if(hitOrderSumCosines<=50){
						hits50SumCosines++;
					}
					if(hitOrderSumCosines<=20){
						hits20SumCosines++;
					}
					if(hitOrderSumCosines<=10){
						hits10SumCosines++;
					}
					if(hitOrderSumCosines<=5){
						hits5SumCosines++;
					}
					if(hitOrderSumCosines==1){
						hits1SumCosines++;
					}
				}
				if(hitOrderDupPredictor>-1){
					if(hitOrderDupPredictor<=1000){
						hits1000DupPredictor++;
					}
					if(hitOrderDupPredictor<=500){
						hits500DupPredictor++;
					}
					if(hitOrderDupPredictor<=100){
						hits100DupPredictor++;
					}
					if(hitOrderDupPredictor<=50){
						hits50DupPredictor++;
					}
					if(hitOrderDupPredictor<=20){
						hits20DupPredictor++;
					}
					if(hitOrderDupPredictor<=10){
						hits10DupPredictor++;
					}
					if(hitOrderDupPredictor<=5){
						hits5DupPredictor++;
					}
					if(hitOrderDupPredictor==1){
						hits1DupPredictor++;
					}
				}
						
				dupeUtils.reportElapsedTime(initTimeQuestion,"calculating recall-rates for question: "+count);
				
				
				//recallRateMaxClassifier = hitsMaxClassifier*100 / (double)count;
				//recallRate100000Classifier = hits100000Classifier*100 / (double)count;
				//recallRate10000Classifier = hits10000Classifier*100 / (double)count;
				recallRate1000StanfordClassifier = hits1000StanfordClassifier*100 / (double)count;
				recallRate500StanfordClassifier = hits500StanfordClassifier*100 / (double)count;
				recallRate100StanfordClassifier = hits100StanfordClassifier*100 / (double)count;
				recallRate50StanfordClassifier = hits50StanfordClassifier*100 / (double)count;
				recallRate20StanfordClassifier = hits20StanfordClassifier*100 / (double)count;
				recallRate10StanfordClassifier = hits10StanfordClassifier*100 / (double)count;
				recallRate5StanfordClassifier = hits5StanfordClassifier*100/ (double)count;
				recallRate1StanfordClassifier = hits1StanfordClassifier*100 / (double)count;
				
				String recallRateMsgStanfordClassifier = "\nResults for Stanford Classifier: "+ count+ " of  "+totalQuestionsToTest
						//+ "\nHits Max ("+maxResultSize+"):  "+hitsMaxClassifier + " - Recall rate max:  "+ decimalFormat.format(recallRateMaxClassifier)
					//	+ "\nHits 100000:  "+hits100000Classifier + " - Recall rate 10000:  "+ df.format(recallRate100000Classifier)
						//+ "\nHits 10000:  "+hits10000Classifier + " - Recall rate 10000:  "+ decimalFormat.format(recallRate10000Classifier)
						+ "\nHits 1000:  "+hits1000StanfordClassifier + " - Recall rate 1000:  "+ decimalFormat.format(recallRate1000StanfordClassifier)
						+ "\nHits 100:  "+hits100StanfordClassifier + " - Recall rate 100:  "+ decimalFormat.format(recallRate100StanfordClassifier)
					//	+ "\nHits 50:  "+hits50StanfordClassifier + " - Recall rate 50:  "+ decimalFormat.format(recallRate50StanfordClassifier)
						+ "\nHits 20:  "+hits20StanfordClassifier + " - Recall rate 20:  "+ decimalFormat.format(recallRate20StanfordClassifier)
						+ "\nHits 10  "+hits10StanfordClassifier + " - Recall rate 10:  "+ decimalFormat.format(recallRate10StanfordClassifier)
						+ "\nHits 5:  "+hits5StanfordClassifier + " - Recall rate 5:  "+ decimalFormat.format(recallRate5StanfordClassifier)
						+ "\nHits 1:  "+hits1StanfordClassifier + " - Recall rate 1:  "+ decimalFormat.format(recallRate1StanfordClassifier);
						
				
				logger.info(recallRateMsgStanfordClassifier+"\n");
				recallRateMsgStanfordClassifier = null;
				
				
				recallRate1000WekaClassifier = hits1000WekaClassifier*100 / (double)count;
				recallRate500WekaClassifier = hits500WekaClassifier*100 / (double)count;
				recallRate100WekaClassifier = hits100WekaClassifier*100 / (double)count;
				recallRate50WekaClassifier = hits50WekaClassifier*100 / (double)count;
				recallRate20WekaClassifier = hits20WekaClassifier*100 / (double)count;
				recallRate10WekaClassifier = hits10WekaClassifier*100 / (double)count;
				recallRate5WekaClassifier = hits5WekaClassifier*100/ (double)count;
				recallRate1WekaClassifier = hits1WekaClassifier*100 / (double)count;
				
				String recallRateMsgWekaClassifier = "\nResults for Weka Classifier: "+ count+ " of  "+totalQuestionsToTest
						//+ "\nHits Max ("+maxResultSize+"):  "+hitsMaxClassifier + " - Recall rate max:  "+ decimalFormat.format(recallRateMaxClassifier)
					//	+ "\nHits 100000:  "+hits100000Classifier + " - Recall rate 10000:  "+ df.format(recallRate100000Classifier)
						//+ "\nHits 10000:  "+hits10000Classifier + " - Recall rate 10000:  "+ decimalFormat.format(recallRate10000Classifier)
						+ "\nHits 1000:  "+hits1000WekaClassifier + " - Recall rate 1000:  "+ decimalFormat.format(recallRate1000WekaClassifier)
						+ "\nHits 100:  "+hits100WekaClassifier + " - Recall rate 100:  "+ decimalFormat.format(recallRate100WekaClassifier)
					//	+ "\nHits 50:  "+hits50WekaClassifier + " - Recall rate 50:  "+ decimalFormat.format(recallRate50WekaClassifier)
						+ "\nHits 20:  "+hits20WekaClassifier + " - Recall rate 20:  "+ decimalFormat.format(recallRate20WekaClassifier)
						+ "\nHits 10  "+hits10WekaClassifier + " - Recall rate 10:  "+ decimalFormat.format(recallRate10WekaClassifier)
						+ "\nHits 5:  "+hits5WekaClassifier + " - Recall rate 5:  "+ decimalFormat.format(recallRate5WekaClassifier)
						+ "\nHits 1:  "+hits1WekaClassifier + " - Recall rate 1:  "+ decimalFormat.format(recallRate1WekaClassifier);
						
				
				logger.info(recallRateMsgWekaClassifier+"\n");
				recallRateMsgWekaClassifier = null;
				
				
				//recallRateMaxTrm = hitsMaxTrm*100 / (double)count;
				//recallRate100000Trm = hits100000Trm*100 / (double)count;
				//recallRate10000Trm = hits10000Trm*100 / (double)count;
				recallRate1000Trm = hits1000Trm*100 / (double)count;
				recallRate500Trm = hits500Trm*100 / (double)count;
				recallRate100Trm = hits100Trm*100 / (double)count;
				recallRate50Trm = hits50Trm*100 / (double)count;
				recallRate20Trm = hits20Trm*100 / (double)count;
				recallRate10Trm = hits10Trm*100 / (double)count;
				recallRate5Trm = hits5Trm*100 / (double)count;
				recallRate1Trm = hits1Trm*100 / (double)count;
				
				String recallRateMsgTrm = "\nResults for Trm: "+ count+ " of  "+totalQuestionsToTest
					//	+ "\nHits max ("+maxResultSize+"):  "+hitsMaxTrm + " - Recall rate max:  "+ decimalFormat.format(recallRateMaxTrm)
					//	+ "\nHits 100000:  "+hits100000Trm + " - Recall rate 10000:  "+ df.format(recallRate100000Trm)
					//	+ "\nHits 10000:  "+hits10000Trm + " - Recall rate 10000:  "+ decimalFormat.format(recallRate10000Trm)
						+ "\nHits 1000:  "+hits1000Trm + " - Recall rate 1000:  "+ decimalFormat.format(recallRate1000Trm)
						+ "\nHits 100:  "+hits100Trm + " - Recall rate 100:  "+ decimalFormat.format(recallRate100Trm)
					//	+ "\nHits 50:  "+hits50Trm + " - Recall rate 50:  "+ decimalFormat.format(recallRate50Trm)
						+ "\nHits 20:  "+hits20Trm + " - Recall rate 20:  "+ decimalFormat.format(recallRate20Trm)
						+ "\nHits 10  "+hits10Trm + " - Recall rate 10:  "+ decimalFormat.format(recallRate10Trm)
						+ "\nHits 5:  "+hits5Trm + " - Recall rate 5:  "+ decimalFormat.format(recallRate5Trm)
						+ "\nHits 1  "+hits1Trm + " - Recall rate 1:  "+ decimalFormat.format(recallRate1Trm);
				
				
				logger.info(recallRateMsgTrm+"\n");
				recallRateMsgTrm = null;
				
				
				//recallRateMaxTrm = hitsMaxTrm*100 / (double)count;
				//recallRate100000Trm = hits100000Trm*100 / (double)count;
				//recallRate10000Trm = hits10000Trm*100 / (double)count;
				recallRate1000DupPredictor = hits1000DupPredictor*100 / (double)count;
				recallRate500DupPredictor = hits500DupPredictor*100 / (double)count;
				recallRate100DupPredictor = hits100DupPredictor*100 / (double)count;
				recallRate50DupPredictor = hits50DupPredictor*100 / (double)count;
				recallRate20DupPredictor = hits20DupPredictor*100 / (double)count;
				recallRate10DupPredictor = hits10DupPredictor*100 / (double)count;
				recallRate5DupPredictor = hits5DupPredictor*100 / (double)count;
				recallRate1DupPredictor = hits1DupPredictor*100 / (double)count;
				
				String recallRateMsgDupPredictor = "\nResults for DupPredictor: "+ count+ " of  "+totalQuestionsToTest
					//	+ "\nHits max ("+maxResultSize+"):  "+hitsMaxDupPredictor + " - Recall rate max:  "+ decimalFormat.format(recallRateMaxDupPredictor)
					//	+ "\nHits 100000:  "+hits100000DupPredictor + " - Recall rate 10000:  "+ df.format(recallRate100000DupPredictor)
					//	+ "\nHits 10000:  "+hits10000DupPredictor + " - Recall rate 10000:  "+ decimalFormat.format(recallRate10000DupPredictor)
						+ "\nHits 1000:  "+hits1000DupPredictor + " - Recall rate 1000:  "+ decimalFormat.format(recallRate1000DupPredictor)
						+ "\nHits 100:  "+hits100DupPredictor + " - Recall rate 100:  "+ decimalFormat.format(recallRate100DupPredictor)
					//	+ "\nHits 50:  "+hits50DupPredictor + " - Recall rate 50:  "+ decimalFormat.format(recallRate50DupPredictor)
						+ "\nHits 20:  "+hits20DupPredictor + " - Recall rate 20:  "+ decimalFormat.format(recallRate20DupPredictor)
						+ "\nHits 10  "+hits10DupPredictor + " - Recall rate 10:  "+ decimalFormat.format(recallRate10DupPredictor)
						+ "\nHits 5:  "+hits5DupPredictor + " - Recall rate 5:  "+ decimalFormat.format(recallRate5DupPredictor)
						+ "\nHits 1  "+hits1DupPredictor + " - Recall rate 1:  "+ decimalFormat.format(recallRate1DupPredictor);
				
				
				logger.info(recallRateMsgDupPredictor+"\n");
				recallRateMsgDupPredictor = null;
				
				//recallRateMaxSumCosines = hitsMaxSumCosines*100 / (double)count;
				//recallRate100000SumCosines = hits100000SumCosines*100 / (double)count;
				//recallRate10000SumCosines = hits10000SumCosines*100 / (double)count;
				recallRate1000SumCosines = hits1000SumCosines*100 / (double)count;
				recallRate500SumCosines = hits500SumCosines*100 / (double)count;
				recallRate100SumCosines = hits100SumCosines*100 / (double)count;
				recallRate50SumCosines = hits50SumCosines*100 / (double)count;
				recallRate20SumCosines = hits20SumCosines*100 / (double)count;
				recallRate10SumCosines = hits10SumCosines*100 / (double)count;
				recallRate5SumCosines = hits5SumCosines*100 / (double)count;
				recallRate1SumCosines = hits1SumCosines*100 / (double)count;
				
				String recallRateMsgSumCosines = "\nResults for Sum of Cosines: "+ count+ " of  "+totalQuestionsToTest
					//	+ "\nHits max ("+maxResultSize+"):  "+hitsMaxSumCosines + " - Recall rate max:  "+ decimalFormat.format(recallRateMaxSumCosines)
					//	+ "\nHits 100000:  "+hits100000SumCosines + " - Recall rate 10000:  "+ df.format(recallRate100000SumCosines)
					//	+ "\nHits 10000:  "+hits10000SumCosines + " - Recall rate 10000:  "+ decimalFormat.format(recallRate10000SumCosines)
						+ "\nHits 1000:  "+hits1000SumCosines + " - Recall rate 1000:  "+ decimalFormat.format(recallRate1000SumCosines)
						+ "\nHits 100:  "+hits100SumCosines + " - Recall rate 100:  "+ decimalFormat.format(recallRate100SumCosines)
					//	+ "\nHits 50:  "+hits50SumCosines + " - Recall rate 50:  "+ decimalFormat.format(recallRate50SumCosines)
						+ "\nHits 20:  "+hits20SumCosines + " - Recall rate 20:  "+ decimalFormat.format(recallRate20SumCosines)
						+ "\nHits 10  "+hits10SumCosines + " - Recall rate 10:  "+ decimalFormat.format(recallRate10SumCosines)
						+ "\nHits 5:  "+hits5SumCosines + " - Recall rate 5:  "+ decimalFormat.format(recallRate5SumCosines)
						+ "\nHits 1  "+hits1SumCosines + " - Recall rate 1:  "+ decimalFormat.format(recallRate1SumCosines);
						
				
				logger.info(recallRateMsgSumCosines);
				recallRateMsgSumCosines = null;
				
				
				
				logger.info("\n\n");
				
				rankingTrm = null;
				rankingStanfordClassifier = null;
				rankingSumCosine = null;
				rankingWekaClassifier = null;
				rankingDupPredictor = null;
			}
			
			//dupeUtils.reportElapsedTime(initTimeQuestion,"experiment: ");
			endTimeLoop = System.currentTimeMillis();
			duration = DurationFormatUtils.formatDuration(endTimeLoop-initTimeLoop, "HH:mm:ss,SSS");
			logger.info("Time to execute experiment: "+duration);
						
			//Save results
			Experiment experiment = new Experiment();
			experiment.setDate(dateFormat.format(new Timestamp(Calendar.getInstance().getTimeInMillis())));
			experiment.setNumberOfTestedQuestions(totalQuestionsToTest);
			experiment.setObservacao(observation);
			experiment.setTag(tagFilter);
			experiment.setBm25b(bm25ParameterB);
			experiment.setBm25k(bm25ParameterK);
			experiment.setApp("Dupe");
			experiment.setBase(DupeUtils.getDataBase(database));
			experiment.setLote(lote);
			experiment.setMaxresultsize(maxResultSize);
			experiment.setDuration(duration);
			experiment.setTrm(trm);
			experimentRepository.save(experiment);
						
			
			RecallRate recallRate = new RecallRate();
			recallRate.setExperimentId(experiment.getId());
			recallRate.setOrigem(trm);
			recallRate.setHits50000(hitsMaxTrm);
			recallRate.setHits10000(hits10000Trm);
			recallRate.setHits1000(hits1000Trm);
			recallRate.setHits100(hits100Trm);
			recallRate.setHits50(hits50Trm);
			recallRate.setHits20(hits20Trm);
			recallRate.setHits10(hits10Trm);
			recallRate.setHits5(hits5Trm);
			recallRate.setHits1(hits1Trm);
			recallRate.setRecallrate_50000(new Double(decimalFormat.format(recallRateMaxTrm)));
			recallRate.setRecallrate_10000(new Double(decimalFormat.format(recallRate10000Trm)));
			recallRate.setRecallrate_1000(new Double(decimalFormat.format(recallRate1000Trm)));
			recallRate.setRecallrate_100(new Double(decimalFormat.format(recallRate100Trm)));
			recallRate.setRecallrate_50(new Double(decimalFormat.format(recallRate50Trm)));
			recallRate.setRecallrate_20(new Double(decimalFormat.format(recallRate20Trm)));
			recallRate.setRecallrate_10(new Double(decimalFormat.format(recallRate10Trm)));
			recallRate.setRecallrate_5(new Double(decimalFormat.format(recallRate5Trm)));
			recallRate.setRecallrate_1(new Double(decimalFormat.format(recallRate1Trm)));
			
			recallRateRepository.save(recallRate);
			
			recallRate = new RecallRate();
			recallRate.setExperimentId(experiment.getId());
			recallRate.setOrigem("Sum of Cosines");
			recallRate.setHits50000(hitsMaxSumCosines);
			recallRate.setHits10000(hits10000SumCosines);
			recallRate.setHits1000(hits1000SumCosines);
			recallRate.setHits100(hits100SumCosines);
			recallRate.setHits50(hits50SumCosines);
			recallRate.setHits20(hits20SumCosines);
			recallRate.setHits10(hits10SumCosines);
			recallRate.setHits5(hits5SumCosines);
			recallRate.setHits1(hits1SumCosines);
			recallRate.setRecallrate_50000(new Double(decimalFormat.format(recallRateMaxSumCosines)));
			recallRate.setRecallrate_10000(new Double(decimalFormat.format(recallRate10000SumCosines)));
			recallRate.setRecallrate_1000(new Double(decimalFormat.format(recallRate1000SumCosines)));
			recallRate.setRecallrate_100(new Double(decimalFormat.format(recallRate100SumCosines)));
			recallRate.setRecallrate_50(new Double(decimalFormat.format(recallRate50SumCosines)));
			recallRate.setRecallrate_20(new Double(decimalFormat.format(recallRate20SumCosines)));
			recallRate.setRecallrate_10(new Double(decimalFormat.format(recallRate10SumCosines)));
			recallRate.setRecallrate_5(new Double(decimalFormat.format(recallRate5SumCosines)));
			recallRate.setRecallrate_1(new Double(decimalFormat.format(recallRate1SumCosines)));
			
			recallRateRepository.save(recallRate);
			
			//if(useLogisticClassifier) {
				recallRate = new RecallRate();
				recallRate.setExperimentId(experiment.getId());
				recallRate.setOrigem("Stanford Classifier");
				recallRate.setHits50000(hitsMaxStanfordClassifier);
				recallRate.setHits10000(hits10000StanfordClassifier);
				recallRate.setHits1000(hits1000StanfordClassifier);
				recallRate.setHits100(hits100StanfordClassifier);
				recallRate.setHits50(hits50StanfordClassifier);
				recallRate.setHits20(hits20StanfordClassifier);
				recallRate.setHits10(hits10StanfordClassifier);
				recallRate.setHits5(hits5StanfordClassifier);
				recallRate.setHits1(hits1StanfordClassifier);
				recallRate.setRecallrate_50000(new Double(decimalFormat.format(recallRateMaxStanfordClassifier)));
				recallRate.setRecallrate_10000(new Double(decimalFormat.format(recallRate10000StanfordClassifier)));
				recallRate.setRecallrate_1000(new Double(decimalFormat.format(recallRate1000StanfordClassifier)));
				recallRate.setRecallrate_100(new Double(decimalFormat.format(recallRate100StanfordClassifier)));
				recallRate.setRecallrate_50(new Double(decimalFormat.format(recallRate50StanfordClassifier)));
				recallRate.setRecallrate_20(new Double(decimalFormat.format(recallRate20StanfordClassifier)));
				recallRate.setRecallrate_10(new Double(decimalFormat.format(recallRate10StanfordClassifier)));
				recallRate.setRecallrate_5(new Double(decimalFormat.format(recallRate5StanfordClassifier)));
				recallRate.setRecallrate_1(new Double(decimalFormat.format(recallRate1StanfordClassifier)));
				
				recallRateRepository.save(recallRate);
				
				
				
				
				recallRate = new RecallRate();
				recallRate.setExperimentId(experiment.getId());
				recallRate.setOrigem("Weka Classifier");
				recallRate.setHits50000(hitsMaxWekaClassifier);
				recallRate.setHits10000(hits10000WekaClassifier);
				recallRate.setHits1000(hits1000WekaClassifier);
				recallRate.setHits100(hits100WekaClassifier);
				recallRate.setHits50(hits50WekaClassifier);
				recallRate.setHits20(hits20WekaClassifier);
				recallRate.setHits10(hits10WekaClassifier);
				recallRate.setHits5(hits5WekaClassifier);
				recallRate.setHits1(hits1WekaClassifier);
				recallRate.setRecallrate_50000(new Double(decimalFormat.format(recallRateMaxWekaClassifier)));
				recallRate.setRecallrate_10000(new Double(decimalFormat.format(recallRate10000WekaClassifier)));
				recallRate.setRecallrate_1000(new Double(decimalFormat.format(recallRate1000WekaClassifier)));
				recallRate.setRecallrate_100(new Double(decimalFormat.format(recallRate100WekaClassifier)));
				recallRate.setRecallrate_50(new Double(decimalFormat.format(recallRate50WekaClassifier)));
				recallRate.setRecallrate_20(new Double(decimalFormat.format(recallRate20WekaClassifier)));
				recallRate.setRecallrate_10(new Double(decimalFormat.format(recallRate10WekaClassifier)));
				recallRate.setRecallrate_5(new Double(decimalFormat.format(recallRate5WekaClassifier)));
				recallRate.setRecallrate_1(new Double(decimalFormat.format(recallRate1WekaClassifier)));
				
				recallRateRepository.save(recallRate);
				
				
				recallRate = new RecallRate();
				recallRate.setExperimentId(experiment.getId());
				recallRate.setOrigem("DupPredictor");
				recallRate.setHits50000(hitsMaxDupPredictor);
				recallRate.setHits10000(hits10000DupPredictor);
				recallRate.setHits1000(hits1000DupPredictor);
				recallRate.setHits100(hits100DupPredictor);
				recallRate.setHits50(hits50DupPredictor);
				recallRate.setHits20(hits20DupPredictor);
				recallRate.setHits10(hits10DupPredictor);
				recallRate.setHits5(hits5DupPredictor);
				recallRate.setHits1(hits1DupPredictor);
				recallRate.setRecallrate_50000(new Double(decimalFormat.format(recallRateMaxDupPredictor)));
				recallRate.setRecallrate_10000(new Double(decimalFormat.format(recallRate10000DupPredictor)));
				recallRate.setRecallrate_1000(new Double(decimalFormat.format(recallRate1000DupPredictor)));
				recallRate.setRecallrate_100(new Double(decimalFormat.format(recallRate100DupPredictor)));
				recallRate.setRecallrate_50(new Double(decimalFormat.format(recallRate50DupPredictor)));
				recallRate.setRecallrate_20(new Double(decimalFormat.format(recallRate20DupPredictor)));
				recallRate.setRecallrate_10(new Double(decimalFormat.format(recallRate10DupPredictor)));
				recallRate.setRecallrate_5(new Double(decimalFormat.format(recallRate5DupPredictor)));
				recallRate.setRecallrate_1(new Double(decimalFormat.format(recallRate1DupPredictor)));
				
				recallRateRepository.save(recallRate);
				
			//}
		
				

		} catch (Exception ex) {
			logger.error("Erro ao gerar recall rate");
			ex.printStackTrace();
		} 
	}




	/*private List<Instance> getInstances() {
		try {
			logger.info("SimpleLogisticClassifier getInstances()");
			initTimeQuestion = System.currentTimeMillis();
			List<Instance> dataset = new ArrayList<Instance>();
			
			// 80% for training
			
			List<Pair> eightyPercentDuplicatedPairs =  duplicatedPairs; //after remove all 
			logger.info("Número de pares shuffled para serem adicionados como duplicados no treinamento (80%): "+eightyPercentDuplicatedPairs.size());
			
			for (Pair p : eightyPercentDuplicatedPairs) {
				try {
					double[] classFeatures = dupeUtils.createFeaturesForSimpleClassifier(p);
					Instance instance = new Instance(classFeatures[4], classFeatures);
					dataset.add(instance);
				} catch (Exception e) {
					logger.info("Erro em extractFeaturesForTraining... Pair: "+p.getId());
					throw e;
				}
				
			}
			
			endTimeQuestion = System.currentTimeMillis();
			String duration = DurationFormatUtils.formatDuration(endTimeQuestion - initTimeQuestion, "HH:mm:ss,SSS");
			logger.info("getInstances finished...Tempo gasto: "+duration);
			return dataset;

		} catch (Exception e) {
			System.err.println("getInstances error!");
			throw e;
		}
	}
*/
































	
	
}
