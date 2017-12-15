package br.ufu.facom.lascam.dupe.util;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DurationFormatUtils;
import org.apache.commons.lang3.text.StrMatcher;
import org.apache.commons.lang3.text.StrTokenizer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.core.StopFilter;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.en.PorterStemFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.util.CharArraySet;
import org.apache.lucene.util.AttributeFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import br.ufu.facom.lascam.dupe.domain.Feature;
import br.ufu.facom.lascam.dupe.domain.Pair;
import br.ufu.facom.lascam.dupe.domain.Post;
import br.ufu.facom.lascam.dupe.domain.PostLink;
import br.ufu.facom.lascam.dupe.domain.Question;
import br.ufu.facom.lascam.dupe.repository.FeatureRepository;
import br.ufu.facom.lascam.dupe.repository.GenericRepository;
import br.ufu.facom.lascam.dupe.service.LuceneSearcherBM25;
import edu.cmu.lti.jawjaw.pobj.POS;
import edu.cmu.lti.lexical_db.ILexicalDatabase;
import edu.cmu.lti.ws4j.RelatednessCalculator;
import edu.stanford.nlp.classify.Dataset;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.stats.ClassicCounter;
import edu.stanford.nlp.stats.Counter;
import edu.stanford.nlp.stats.Counters;
import edu.stanford.nlp.util.CoreMap;
import weka.classifiers.functions.Logistic;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;

@Component
public class DupeUtils {
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	/*
	 * Variáveis de antigo pre-process
	 */
	private AttributeFactory factory;
	private static StandardTokenizer standardTokenizer;
	
	
	private static Boolean calcularWordNet;
	private static Boolean calcularEntityOverlap;
	private static Boolean calcularEntityTypeOverlap;
	//private static Map<Integer, Post> allPosts;
	private static List<PostLink> allPostLinks;
		
	
	private Properties props;
	private static StanfordCoreNLP pipeline;
	private static ILexicalDatabase db;
	private static RelatednessCalculator rcs;
	private static List<POS[]> posPairs;
	private Set<Integer> allDuplicatedQuestionsIds;
	private static Map<Integer, Set<Integer>> bucketDuplicatiosMap;
	
	@Autowired
	protected GenericRepository genericRepository;
	@Autowired
	protected FeatureRepository featureRepository;
	
	@Autowired
	protected LuceneSearcherBM25 luceneSearcherBM25;
	
	protected Boolean configsInitialized = false;
	
	protected ArrayList<Attribute> wekaAttributes;
		
	
	//private static final Pattern CODE_REGEX = Pattern.compile("(?sm)<code>(.*?)</code>", Pattern.DOTALL);
	
	
	private static long initTime;
	
	private static long endTime;
	
	
	private Double ttWeight;
	
	private Double ccWeight;
	
	private Double bbWeight;
	
	private Double btWeight;
	
	private Double tbWeight;
	
	private Double eeWeight;
	
	private Double aaWeight;
	
	private CosineSimilarity cs1;
	
	@Value("${trm}")
	public  String trm;  
	

	@Value("${tagFilter}")
	public String tagFilter;

	public DupeUtils() {
		/*logger.info("Inicializando DupeUtils...");
		props = new Properties();
		props.put("annotators", "tokenize, ssplit, pos, lemma, ner, depparse");
		pipeline = new StanfordCoreNLP(props);
		db = new NictWordNet();
		WS4JConfiguration.getInstance().setMFS(true);
		rcs = new HirstStOnge(db);
		posPairs = rcs.getPOSPairs();*/
		loadWekaFeatures();
		
	}
	
	
	private void loadWekaFeatures() {
		// numeric feature
		Attribute feature1 = new Attribute("f1");
		Attribute feature2 = new Attribute("f2");
		Attribute feature3 = new Attribute("f3");
		Attribute feature4 = new Attribute("f4");
		Attribute feature5 = new Attribute("f5");
		Attribute feature6 = new Attribute("f6");
		Attribute feature7 = new Attribute("f7");
	
		// Declare the class attribute along with its values
		ArrayList<String> classVal = new ArrayList<>(2);
		classVal.add("duplicated");
		classVal.add("non");
		Attribute classAttribute = new Attribute("theClass", classVal);

		wekaAttributes = new ArrayList<>(2);
		wekaAttributes.add(feature1);
		wekaAttributes.add(feature2);
		wekaAttributes.add(feature3);
		wekaAttributes.add(feature4);
		wekaAttributes.add(feature5);
		wekaAttributes.add(feature6);
		wekaAttributes.add(feature7);
	
		wekaAttributes.add(classAttribute);
		
	}
	
	

	public void initializeConfigs() throws Exception {
		if(!configsInitialized){
			configsInitialized = true;
		
			cs1 = new CosineSimilarity();
		}
		
				
	}
	
	public void getPostsLinks() {
		logger.info("Recuperando PostLinks... ");
		if(allPostLinks==null){
			allPostLinks = genericRepository.getAllPostLinks();
		}
		logger.info("PostLinks recuperados: " + allPostLinks.size());

	}
	
	
	//@PostConstruct
	public void generateBuckets() {
		if(bucketDuplicatiosMap==null){
			bucketDuplicatiosMap = new HashMap<Integer, Set<Integer>>();	
			allDuplicatedQuestionsIds = new HashSet<Integer>();
			getPostsLinks();
			//Pode haver mais de uma duplicada por questao.. Bucket structure
			logger.info("Building buckets");
					
			for(PostLink postLink:allPostLinks){
				allDuplicatedQuestionsIds.add(postLink.getId());
				allDuplicatedQuestionsIds.add(postLink.getRelatedPostId());
				
				Set<Integer> duplicatedOfMaster = bucketDuplicatiosMap.get(postLink.getRelatedPostId());  
				if(duplicatedOfMaster==null){
					duplicatedOfMaster = new HashSet<Integer>();
					bucketDuplicatiosMap.put(postLink.getRelatedPostId(),duplicatedOfMaster);
				}
				duplicatedOfMaster.add(postLink.getPostId());
			}
			
			logger.info("Buckets gerados...");
		}
	}
	
	
	
	public static List<String> getCodeValues(Pattern patter,String str) {
	    final List<String> tagValues = new ArrayList<String>();
	    final Matcher matcher = patter.matcher(str);
	    while (matcher.find()) {
	        tagValues.add(matcher.group(1));
	    }
	    return tagValues;
	}
	
	


	@SuppressWarnings("unchecked")
	public HashMap<String, Feature> extractFeatures(Post master, Post duplicated) throws Exception {

		HashMap<String, Feature> result = new LinkedHashMap();
				
		String masterTitle = master.getTitle();
		String duplicatedTitle = duplicated.getTitle();
		
		String masterBody = master.getBody();
		String duplicatedBody = duplicated.getBody();
		
		String masterTags = master.getTagsSyn();
		String duplicatedTags = duplicated.getTagsSyn();
	
		
		String masterCode= master.getCode();
		String duplicatedCode = duplicated.getCode();
		
		//CC
		Feature codeCode = computeFeatures(masterCode,duplicatedCode);	
		result.put("CC", codeCode);
		
		//TT
		Feature titleTitle  = computeFeatures(masterTitle, duplicatedTitle);
		result.put("TT", titleTitle);
		
		//BB
		Feature bodyBody = computeFeatures(masterBody, duplicatedBody);
		result.put("BB", bodyBody);
		
		//TagTag
		Feature tagTag  = computeFeatures(masterTags, duplicatedTags);
		result.put("TagTag", tagTag);
		
		//TB
		Feature titleBody = computeFeatures(masterTitle, duplicatedBody);
		result.put("TB", titleBody);

		//BT
		Feature bodyTitle = computeFeatures(masterBody, duplicatedTitle);
		result.put("BT", bodyTitle);

		//TitleTag
		Feature TitleTag = computeFeatures(masterTitle, duplicatedTags);
		result.put("TitleTag", TitleTag);
		
		/*Feature TagTitle = computeFeatures(masterTags,duplicatedTitle);
		result.put("TagTitle", TagTitle);*/
	
		
		
		
		return result;
	}

	



	public Feature computeCossineForCodes(Pattern pattern, String body1, String body2) throws Exception {
		List<String> results1 = getCodeValues(pattern,body1);
		List<String> results2 = getCodeValues(pattern,body2);
		
		String result1 = "";
		String result2 = "";
		for(String token: results1){
			result1+= token+ " ";
		}
		for(String token: results2){
			result2+= token+ " ";
		}
		
		return computeFeatures(result1,result2);
		
	}


	public Feature computeFeatures(String string1, String string2) throws Exception {
		Feature f = new Feature();
		/*
		List<Counter<String>> countersTagQ1 = computeTokenNerPos(tags1);
		
		List<Counter<String>> countersTagQ2 = computeTokenNerPos(tags2);
		
		f.setCosine(Counters.cosine(countersTagQ1.get(0), countersTagQ2.get(0)));*/
		f.setCosine(calculateCosine(string1,string2));
		
		
		return f;
	}




	/*private static String extraiTextoTags(String tags1) throws Exception {
		tags1 = tagMastering(tags1);
		tags1 = tags1.replaceAll("<","");
		tags1 = tags1.replaceAll(">"," ");
		//tags1 = tokenizeStopStem(tags1); 
		return tags1;
	}
*/
	/**
	 * Counters for NamedEntityTag for title and body
	 * 
	 * @param q
	 */
	private static void computeCountersForQuestion(Question q , String title, String body, String tags) {

		// Title
		List<Counter<String>> counters = computeTokenNerPos(title);
		q.setTitleText(counters.get(0));
		q.setTitleEntity(counters.get(1));
		q.setTitleEntityType(counters.get(2));

		// Body
		counters = computeTokenNerPos(body);
		q.setBodyText(counters.get(0));
		q.setBodyEntity(counters.get(1));
		q.setBodyEntityType(counters.get(2));
		
		// Tag
		counters = computeTokenNerPos(tags);
		q.setTagText(counters.get(0));
	}

	public static List<Counter<String>> computeTokenNerPos(String corpus) {
		Annotation document = new Annotation(corpus);
		pipeline.annotate(document);
		List<CoreMap> sentences = document.get(SentencesAnnotation.class);

		Counter<String> text = new ClassicCounter<String>();
		Counter<String> ner = new ClassicCounter<String>();
		Counter<String> pos = new ClassicCounter<String>();

		for (CoreMap sentence : sentences) {
			for (CoreLabel cl : sentence.get(CoreAnnotations.TokensAnnotation.class)) {

				text.incrementCount(cl.get(CoreAnnotations.TextAnnotation.class));
				
				if(calcularEntityOverlap){
					ner.incrementCount(cl.get(CoreAnnotations.NamedEntityTagAnnotation.class));
				}
				if(calcularEntityTypeOverlap){
					pos.incrementCount(cl.get(CoreAnnotations.PartOfSpeechAnnotation.class));
				}
			}
		}
		return new ArrayList<Counter<String>>(Arrays.asList(text, ner, pos));
	}
	
	/*private Feature computeFeaturesTitleTitle(@SuppressWarnings("unchecked") Counter<String>... counters) {
		Feature tt = computeFeatures(counters);
		tt.setTermOverlap(termOverlap(counters[0], counters[1]));
		return tt;
	}
	*/
	

/*	public static Feature computeFeatures(@SuppressWarnings("unchecked") Counter<String>... counters) {
		Feature f = new Feature();
		f.setCosine(Counters.cosine(counters[0], counters[1]));
		
		return f;
	}*/

	
	
	private static Double termOverlap(Counter<String> c1, Counter<String> c2) {

		return (2.0 * Counters.intersection(c1, c2).size()) / (Counters.union(c1, c2).size());
	}

	/*private static Double wordNet(Counter<String> left, Counter<String> right) {

		Double result = -1D;
		Double tempResult = -1D;

		for (POS[] posPair : posPairs) {
			for (String word1 : left.keySet()) {
				for (String word2 : right.keySet()) {

					List<Concept> synsets1 = (List<Concept>) db.getAllConcepts(word1, posPair[0].toString());
					List<Concept> synsets2 = (List<Concept>) db.getAllConcepts(word2, posPair[1].toString());

					for (Concept synset1 : synsets1) {
						for (Concept synset2 : synsets2) {
							double score = rcs.calcRelatednessOfSynset(synset1, synset2).getScore();
							if (score > tempResult) {
								tempResult = score;
							}
						}
					}
					result += tempResult;
				}
			}
		}
		return (result.equals(-1D)) ? 0.0 : result;
	}*/
	
	private void getFeaturesFromDB(Pair pair) {
		List<Feature> features = featureRepository.findByPairId(pair.getId(), new Sort(Sort.Direction.ASC, "type"));
		HashMap<String, Feature> result = new HashMap<>();
		for(Feature feature: features){
			result.put(feature.getType(), feature);
		}
		pair.setFeatures(result);
	}
	
	public Double getSumOfCosines(Pair pair) {
		List<Double> cosines = createFeatures(pair); 
		Double sum = 0d;
		for(Double cosine: cosines){
			sum+= cosine;
		}
		return sum;
	}
	
	public List<Double> createFeatures(Pair p) {
		List<Double> listCosines = new ArrayList<>();
		
		HashMap<String, Feature> features = p.getFeatures();
		if(features==null){
			getFeaturesFromDB(p);
			features = p.getFeatures();
		}
		
		//we guarantee the order of the cosines
		listCosines.add(features.get("CC").getCosine());
		listCosines.add(features.get("TT").getCosine());
		listCosines.add(features.get("BB").getCosine());
		listCosines.add(features.get("TagTag").getCosine());
		listCosines.add(features.get("TB").getCosine());
		listCosines.add(features.get("BT").getCosine());
		listCosines.add(features.get("TitleTag").getCosine());
				
		/*for (Entry<String, Feature> entry : p.getFeatures().entrySet()) {
			String key = entry.getKey();
			Feature f = entry.getValue();
			//cosine = getCosineBasedOnFeatureWeight(key,f.getCosine());
			//cosine = f.getCosine();
			listCosines.add(f.getCosine());
		}*/
		//classFeatures.add(cosine);
		
		return listCosines;
	}
	
	
	public double[] createFeaturesForSimpleClassifier(Pair p) {
		double[] listCosines = new double[5];
		//List<Double> listCosines = new ArrayList<>();
		
		HashMap<String, Feature> features = p.getFeatures();
		if(features==null){
			getFeaturesFromDB(p);
		}
		int count = 0;
		for (Entry<String, Feature> entry : p.getFeatures().entrySet()) {
			String key = entry.getKey();
			Feature f = entry.getValue();
			if(key.equals("EE") || key.equals("AA")){
				continue; //somente 5 features para esse classificador
			}
			listCosines[count]= f.getCosine();
			count++;
		}
		
		return listCosines;
	}
	
	
	private double getCosineBasedOnFeatureWeight(String key, Double cosine) {
		double weight = 1;
		if(key.equals("TT")){
			weight = ttWeight;
		}else if(key.equals("CC")){
			weight = ccWeight;
		}else if(key.equals("BB")){
			weight = bbWeight;
		}else if(key.equals("BT")){
			weight = btWeight;
		}else if(key.equals("TB")){
			weight = tbWeight;
		}else if(key.equals("EE")){
			weight = eeWeight;
		}else if(key.equals("AA")){
			weight = aaWeight;
		}
		return weight * cosine;
	}

	/**
	 * @param Pairs
	 * @return
	 */
	public Dataset<String, Double> extractFeaturesForTrainingStanfordClassifier(List<Pair> pairs,String label) {
		Dataset<String, Double> trainingData = new Dataset<>();
		for (Pair p : pairs) {
			try {
				List<Double> classFeatures = createFeatures(p);
				trainingData.add(classFeatures, label);
			} catch (Exception e) {
				logger.info("Erro em extractFeaturesForTraining... Pair: "+p.getId());
				throw e;
			}
			
		}
		return trainingData;
	}


	public void extractFeaturesForTrainingWekaClassifier(List<Pair> pairs, String label, Instances trainingData) {
		for (Pair p : pairs) {
			List<Double> classFeatures = createFeatures(p);
			trainingData.add(getWekaInstance(p,classFeatures,label));
		}
	}
	
	
	public Instance getWekaInstance(Pair p, List<Double> classFeatures,String label) {
		Instance iExample = new DenseInstance(8);
		iExample.setValue((Attribute) wekaAttributes.get(0), classFeatures.get(0));
		iExample.setValue((Attribute) wekaAttributes.get(1), classFeatures.get(1));
		iExample.setValue((Attribute) wekaAttributes.get(2), classFeatures.get(2));
		iExample.setValue((Attribute) wekaAttributes.get(3), classFeatures.get(3));
		iExample.setValue((Attribute) wekaAttributes.get(4), classFeatures.get(4));
		iExample.setValue((Attribute) wekaAttributes.get(5), classFeatures.get(5));
		iExample.setValue((Attribute) wekaAttributes.get(6), classFeatures.get(6));
		iExample.setValue((Attribute) wekaAttributes.get(7), label);
		return iExample;

	}


	public static <K, V extends Comparable<? super V>> List<Entry<K, V>> entriesSortedByValuesDesc(Map<K, V> map) {

		List<Entry<K, V>> sortedEntries = new LinkedList<Entry<K, V>>(map.entrySet());

		Collections.sort(sortedEntries, new Comparator<Entry<K, V>>() {
			@Override
			public int compare(Entry<K, V> e1, Entry<K, V> e2) {
				return e2.getValue().compareTo(e1.getValue());
			}
		});

		return sortedEntries;
	}

	public static <K, V extends Comparable<? super V>> List<Entry<K, V>> entriesSortedByValues(Map<K, V> map) {

		List<Entry<K, V>> sortedEntries = new LinkedList<Entry<K, V>>(map.entrySet());

		Collections.sort(sortedEntries, new Comparator<Entry<K, V>>() {
			@Override
			public int compare(Entry<K, V> e1, Entry<K, V> e2) {
				return e1.getValue().compareTo(e2.getValue());
			}
		});

		return sortedEntries;
	}
	
	
	

	public static Set<Integer> getRelatedPostIds(Post closedDuplicatedQuestion) {
		
		Set<Integer> relatedPostIds = new HashSet<Integer>();
		for(PostLink postLink: allPostLinks){
			if(postLink.getPostId().equals(closedDuplicatedQuestion.getId())){
				relatedPostIds.add(postLink.getRelatedPostId());
			}
		}
		
		return relatedPostIds;
	}
	
	public static Set<Integer> getRelatedPostIds(Integer questionId) {
		Set<Integer> relatedPostIds = new HashSet<Integer>();
		for(PostLink postLink: allPostLinks){
			if(postLink.getPostId().equals(questionId)){
				relatedPostIds.add(postLink.getRelatedPostId());
			}
		}
		
		return relatedPostIds;
	}
	


	public static Map<Integer, Set<Integer>> getBucketDuplicatiosMap() {
		return bucketDuplicatiosMap;
	}

	

	
	
	public static String getDataBase(String fullPath) {
		String dataBaseName[] = fullPath.split("5432/?");
		return dataBaseName[1];
	}

	
	public void reportElapsedTime(long initTime, String processName) {
		
		endTime = System.currentTimeMillis();
		String duration = DurationFormatUtils.formatDuration(endTime-initTime, "HH:mm:ss,SSS");
		logger.info("Time elapsed : "+duration+ " in execution of  "+processName);
		
	}

	public double calculateCosine(String string1, String string2) throws Exception {
		if(string1==null || string2==null){
			return 0d;
		}
		string1 = string1.trim();
		string2 = string2.trim();
		if(string1.equals("") || string2.equals("")){
			return 0d;
		}
		
		double sim_score = cs1.getCosineSimilarityScore(string1,string2);
		return sim_score;
	}

	
	
	
	

	
	public static List<String> getWords(Pattern patter,String str) {
	    final List<String> tagValues = new ArrayList<String>();
	    final Matcher matcher = patter.matcher(str);
	    while (matcher.find()) {
	        tagValues.add(matcher.group(0));
	    }
	    return tagValues;
	}
	
	
	
	 public static float redondear(float pNumero, int pCantidadDecimales) {
	    // the function is call with the values Redondear(625.3f, 2)
	    BigDecimal value = new BigDecimal(pNumero);
	    value = value.setScale(pCantidadDecimales, RoundingMode.HALF_EVEN); // here the value is correct (625.30)
	    return value.floatValue(); // but here the values is 625.3
	}
	 
	 public static double redondear(double pNumero, int pCantidadDecimales) {
		    // the function is call with the values Redondear(625.3f, 2)
		    BigDecimal value = new BigDecimal(pNumero);
		    value = value.setScale(pCantidadDecimales, RoundingMode.HALF_EVEN); // here the value is correct (625.30)
		    return value.doubleValue(); // but here the values is 625.3
		}
	 
	 
	 
	 public static String readFile(String file) throws IOException {
	    BufferedReader reader = new BufferedReader(new FileReader(file));
	    String         line = null;
	    StringBuilder  stringBuilder = new StringBuilder();
	    String         ls = System.getProperty("line.separator");

	    try {
	        while((line = reader.readLine()) != null) {
	            stringBuilder.append(line);
	            stringBuilder.append(ls);
	        }

	        return stringBuilder.toString();
	    } finally {
	        reader.close();
	    }
	}

	public static List<PostLink> getAllPostLinks() {
		return allPostLinks;
	}

	public static void setAllPostLinks(List<PostLink> allPostLinks) {
		DupeUtils.allPostLinks = allPostLinks;
	}
	 
	 
	public static void setBlanks(Post post) {
		post.setAcceptedAnswerId(null);
		post.setAnswerCount(null);
		post.setClosedDate(null);
		post.setCommentCount(null);
		post.setCommunityOwnedDate(null);
		post.setFavoriteCount(null);
		post.setLastActivityDate(null);
		post.setLastEditorDisplayName(null);
		post.setLastEditorDisplayName(null);
		post.setLastEditorUserId(null);
		post.setOwnerUserId(null);
		post.setPostTypeId(null);
		post.setViewCount(null);
		post.setParentId(null);
		post.setCreationDate(null);
	}
	
	
	public static String getQueryComplementByTag(String tagFilter) {
		String query="";
		if(tagFilter!=null && !"".equals(tagFilter)) {
			if(tagFilter.equals("java")) {
				query += " and tagssyn like '%java%' and tags not like '%javascript%' "; 
			}else {
				query += " and tagssyn like '%"+tagFilter+"%'";
			}
		}
		return query;
		
	}


	public ArrayList<Attribute> getWekaAttributes() {
		return wekaAttributes;
	}


	public void setWekaAttributes(ArrayList<Attribute> wekaAttributes) {
		this.wekaAttributes = wekaAttributes;
	}

	

	
	 
}
