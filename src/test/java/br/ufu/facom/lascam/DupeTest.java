package br.ufu.facom.lascam;

import java.io.IOException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.util.CharArraySet;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.queryparser.classic.QueryParserBase;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import br.ufu.facom.lascam.dupe.domain.Experiment;
import br.ufu.facom.lascam.dupe.domain.Question;
import br.ufu.facom.lascam.dupe.domain.RecallRate;
import br.ufu.facom.lascam.dupe.repository.ExperimentRepository;
import br.ufu.facom.lascam.dupe.repository.FeatureRepository;
import br.ufu.facom.lascam.dupe.repository.GenericRepository;
import br.ufu.facom.lascam.dupe.repository.PairRepository;
import br.ufu.facom.lascam.dupe.repository.PostsRepository;
import br.ufu.facom.lascam.dupe.repository.RecallRateRepository;
import br.ufu.facom.lascam.dupe.service.ClassifyService;
import br.ufu.facom.lascam.dupe.service.FeaturesService;
import br.ufu.facom.lascam.dupe.service.LuceneSearcherBM25;
import br.ufu.facom.lascam.dupe.util.DupeUtils;
import junit.framework.TestCase;

@RunWith(SpringRunner.class)
@SpringBootTest
public class DupeTest extends TestCase{


	@Autowired
	private FeaturesService featuresService;
	
	@Autowired
	private DupeUtils dupeUtils;
		
	@Autowired
	private ClassifyService classifyService;
	
	@Autowired
	protected PostsRepository postsRepository;
	
	
	@Autowired
	protected GenericRepository genericRepository;
	
	
	@Autowired
	protected PairRepository pairRepository;
	
	@Autowired
	protected FeatureRepository featureRepository;
	
	@Autowired
	protected ExperimentRepository experimentRepository;
	
	@Autowired
	protected RecallRateRepository recallRateRepository;
	
	@Autowired
	protected LuceneSearcherBM25 luceneSearcherBM25;
	
	private static final Pattern TAG_REGEX = Pattern.compile("(?sm)<code>(.*?)</code>", Pattern.DOTALL);
	private static final Pattern TAG_REGEX_POSTSQUESTIONS = Pattern.compile("(?sm)pre code(.*?)code pre", Pattern.DOTALL);

	private List<String> getTagValues(String str) {
	    final List<String> tagValues = new ArrayList<String>();
	    final Matcher matcher = TAG_REGEX_POSTSQUESTIONS.matcher(str);
	    while (matcher.find()) {
	        tagValues.add(matcher.group(1));
	    }
	    return tagValues;
	}
	
	
	@SuppressWarnings("static-access")
	//@Test
	public void dupeTest() throws Exception {
		
		/*ClosedDuplicatedQuestion master = closedDuplicatedQuestionsRepository.findOne(3148274);
		
		ClosedDuplicatedQuestion related = closedDuplicatedQuestionsRepository.findOne(5769813);*/
		
		
		/*
		List<Counter<String>> countersQ1 = featuresService.computeTokenNerPos("java get declar method order thei appear sourc code [Duplicate]");
		
		List<Counter<String>> countersQ2 = featuresService.computeTokenNerPos("java get declar method order thei appear sourc code [Duplicate]");
		
		Feature f = featuresService.computeFeatures(countersQ1.get(0), countersQ2.get(0));
		System.out.println(f);
		
		Feature f2 = featuresService.computeCossineForTags("<java> <swing> <validation> <rules> <invalidate>","<java> <swing> <validation> <rules> <invalidate>");
		
		System.out.println(f2);*/
		dupeUtils.initializeConfigs();
		//Question testQuestion = new Question();
		//testQuestion.setTags("<rails><j2me><java>");
		//System.out.println(DupeUtils.buildTagContent(testQuestion));
		
		
		//Post post1 = postsRepository.findOne(17656495);
		//Post post2 = postsRepository.findOne(4743709);
		
		//Question question1 = questionsRepository.findOne(158618);
		String acrescimo = "test testing for is all of them they if start still starting do doing done did \" \' { . , : ; + -  [Duplicate] ";
		
		
		String query = "C:/RailsInstaller/Ruby2.0.0/lib/ruby/gems/2.0.0/gems/execjs-2.1.0/lib/execjs/runtimes.rb:51:in `autodetect': Could not find a JavaScript runtime. See https://github.com/sstephenson/execjs for a list of available runtimes. "
				+ "(ExecJS::RuntimeUnavailable) from C:/RailsInstaller/Ruby2.0.0/lib/ruby/gems/2.0.0/gems/execjs-2.1.0/l ib/execjs.rb:5:in `<module:ExecJS>' from ";
		//Question duplicated = questionsRepository.findOne(9340117);
		
		//System.out.println(query);
		//System.out.println(luceneSearcherBM25.performStemmingStopWords(null,query));
		
		/*String before1 = ResearchStringUtils.removeStopWordsAndPerformSteeming(dupeUtils.buildQuestionContent(question1));
		System.out.println("\n1= "+before1);
		*/
		//String before =  luceneSearcherBM25.performStemmingStopWords(question1,DupeUtils.buildQuestionContent(question1));
		//before+= " "+QueryParserBase.escape(DupeUtils.buildTagContent(question1.getTags()));
		
		//System.out.println("\n1= "+before);
		//String questionContentSearch = DupeUtils.buildQuestionContent(question1);
		//System.out.println(questionContentSearch);
		//luceneSearcherBM25.search(question1, 1);
		
		//link 158618
		
		//String tagTest = luceneSearcherBM25.performStemmingStopWords("ruby","a",question1);
		/*String tagTest2 = luceneSearcherBM25.performStemmingStopWords("rubi","a",question1);
		String tagTest3 = luceneSearcherBM25.performStemmingStopWords("java-8","a",question1);
		String tagTest4 = luceneSearcherBM25.performStemmingStopWords("ruby-on-rails","a",question1);*/
		
		//String contentTags = luceneSearcherBM25.performOnlyStemming(DupeUtils.buildTagContent(question1.getTags()),"tags",question1);
		//System.out.println(contentTags);
		//dupeUtils.extractFeatures(question1, question2);
		//Query stemmedFinalContent = luceneSearcherBM25.getStemmedFinalContent(question1,false);
		//System.out.println(stemmedFinalContent.toString("content"));
		//String codeTags = dupeUtils.buildCodeAndTagsContent(question1);
		//String bodyWithoutCode = dupeUtils.buildBodyContentWithoutCode(question1.getBody());
		
		//System.out.println(codeTags);
		//System.out.println(bodyWithoutCode);
		
		//System.out.println(code);
		//System.out.println(dupeUtils.buildQuestionContentWithoutTagsAndCode(question1));
		
		//Query stemmedFinalContent = luceneSearcherBM25.getStemmedFinalContent(question1);
		
		//String finalContent = stemmedFinalContent.toString("content");
		//System.out.println(finalContent);
		//String bodyContent = question1.getBody();
		
		//finalContent = dupeUtils.buildBodyContentWithoutCode(bodyContent);
		//System.out.println(finalContent);
		//System.out.println(dupeUtils.tokenizeStopStem(question1.getBody()));
		
		//System.out.println("\n2= "+DupeUtils.tokenizeStopStem(dupeUtils.buildBodyContent(dupeUtils.buildQuestionContent(question1))));
		
		//System.out.println(dupeUtils.buildTitleContent(question1.getTitle()));
		//String questionContent = dupeUtils.buildQuestionContent((question1));
		//System.err.println(questionContent);
		//String parsed = luceneSearcherBM25.performStemmingStopWordsToIndex(question1,false);
		
		/*String questionContent = luceneSearcherBM25.performStemmingStopWords(question1,DupeUtils.buildQuestionContent(question1));
		if(questionContent!=null){
			questionContent+= " "+DupeUtils.buildTagContent(question1.getTags());
		}*/
		//System.out.println("Indexei =  "+questionContent);
		
		//System.out.println(dupeUtils.buildBodyContentWithoutCode(question1.getBody()));
		//Question question1 = questionsRepository.findOne(18493817);
		//Question question1 = questionsRepository.findOne(19298446);
		
		/*HashMap<String, Feature> features = dupeUtils.extractFeatures(question1, question2 ,true);
		for (Entry<String, Feature> entry : features.entrySet()) {
			Feature feature = entry.getValue();
			//System.out.println(feature);
		}*/
		/*String title = question1.getTitle()+ " - test testing for is all of them they if start starting do doing done did \" \' { . , : ; + -  [Duplicate]";
		System.out.println(title);
		title =  dupeUtils.buildTitleContent(title);
		System.out.println(title);
		String body = dupeUtils.buildBodyContent(question1.getBody()); 
		System.out.println(body);*/
		
		
				
		//preprocessed = dupeUtils.retiraSimbolosEspeciais(preprocessed);
		//System.out.println(preprocessed);
		
		//preprocessed = dupeUtils.tokenizeStopStem(preprocessed);
		//System.out.println(preprocessed);
		 
		
	
		
		//System.out.println(post.getBody());
		//featuresService.loadVariables();
		
		//String s = "<pre><code>$(document).bind('touchmove',function(e) {e.preventDefault(); });</code></pre>";
        //Pattern p = Pattern.compile("<code>(.*?)</code>");
        /*Matcher m = p.matcher(post.getBody());
        while(m.find())
        {
            System.out.println(m.group(1)); 
        }*/
		//String result = Arrays.toString(getTagValues(post.getBody()).toArray());
		
		/*String body1 = "<code>adaf d f</code> daf da\n dadsfa <code> ssee</code>";
		String body2 = "<code>adaf d f</code> daf da\n dadsfa <code> ssee</code> dasf sdfssfsdfasdfasdfsfsdfasdf  \n jhj \n<code> derr </code>";
		String body3 = "p below post led me evalu us jasonpatch json json transform p p href http stackoverflow.com question 13068267 json json transform json json transform p p project can found here p p href http github.com bruth jsonpatch js rel nofollow http github.com bruth jsonpatch js p p i am current try chang name all element arrai am see how thi possibl my current attempt p pre code var transform op move from hit 1 _id path hit 1 pizza code pre p thi swap out first element how do i do card type oper someth like p pre code var transform op move from hit _id path hit pizza code pre p i could see possibl call transform n time each element seem like hack p";
				
		
				
		List<String> results1 = getTagValues(body1);
		List<String> results2 = getTagValues(body2);
		List<String> results3 = getTagValues(body3);
		
		String result1 = "";
		String result2 = "";
		for(String token: results1){
			result1+= token+ " ";
		}
		for(String token: results2){
			result2+= token+ " ";
		}*/
		
		/*dupeUtils.initializeConfigs();
		List<Counter<String>> countersCode1 = dupeUtils.computeTokenNerPos(post.getBody());
		List<Counter<String>> countersCode2 = dupeUtils.computeTokenNerPos(result2);
		*/
		//f.setCosine(Counters.cosine(countersCode1.get(0), countersCode2.get(0)));
		
		//String body = question1.getBody();
		//System.out.println(body);
		//String cleanedBody = dupeUtils.preProcessQuestion(body);
		//System.out.println(cleanedBody);
		
		//System.out.println(dupeUtils.buildQuestionContent(question1));
		
		/*Feature f = new Feature();
		f = dupeUtils.computeCossineForCodes(post1.getBody(),post2.getBody());
		
		String tags1 = dupeUtils.buildTagContent(post1.getTags());
		String tags2 = dupeUtils.buildTagContent(post2.getTags());
		f = dupeUtils.computeFeatures(tags1,tags2);
		System.out.println(f.getCosine());*/
		//String result = Arrays.toString(getTagValues("<code>adaf d f</code> daf da\n dadsfa <code> ssee</code>").toArray());
		
		//System.out.println(f);
		//Integer id = 6028757;
		//Post post2 = postsRepository.findOne(id);
		//Question question2 = questionsRepository.findOne(id);
		
		//System.out.println(post2);
		//System.out.println(question2);
		
		
		//String processed = DupeUtils.buildQuestionContent(question2);
		//System.out.println(processed);
		
		/*String[] titleContent = dupeUtils.separaSomentePalavrasNaoSomentePalavras(question1.getTitle());
		System.out.println(titleContent[0]);
		System.out.println(titleContent[1]);
		
		String[] bodyContent = dupeUtils.separaSomentePalavrasNaoSomentePalavras(question1.getBody());
		System.out.println(bodyContent[0]);
		System.out.println(bodyContent[1]);
		System.out.println(bodyContent[2]);*/
		
		
		//String tags = dupeUtils.tagMastering("<ruby-on-rails><ruby>");
		//System.out.println(tags);
	
		
		//Thread.sleep(3000);
		
				
	}
	
	
	
	//@Test
	public void testSearch() throws Exception {
		CharArraySet stopWords = EnglishAnalyzer.getDefaultStopSet();
		
		Analyzer englishAnalizer = new EnglishAnalyzer(stopWords);
		Analyzer whitespaceAnalyzer = new WhitespaceAnalyzer();
		Analyzer standard = new StandardAnalyzer();

        // 1. create the index
        Directory index = new RAMDirectory();

        IndexWriterConfig config = new IndexWriterConfig(whitespaceAnalyzer);
        config.setSimilarity(new BM25Similarity(0.05f, 0.03f));
       
        String querystrIndex = "autodetect could find javascript runtim see C:/RailsInstaller/Ruby2.0.0/lib/ruby/gems/2.0.0/gems/execjs-2.1.0/lib/execjs/runtimes.rb:51:in `autodetect': Could not find a JavaScript runtime. See https://github.com/sstephenson/execjs for a list of available runtimes. (ExecJS::RuntimeUnavailable) from C:/RailsInstaller/Ruby2.0.0/lib/ruby/gems/2.0.0/gems/execjs-2.1.0/l ib/execjs.rb:5:in `<module:ExecJS>' from";
        String query = "autodetect could find javascript runtime.";
        
        IndexWriter w = new IndexWriter(index, config);
        /*addDoc(w, luceneSearcherBM25.performParseAndEscape(null, querystrIndex, englishAnalizer), "englishAnalizer");
        addDoc(w, luceneSearcherBM25.performParseAndEscape(null, querystrIndex, whitespaceAnalyzer), "whitespaceAnalyzer");
        addDoc(w, luceneSearcherBM25.performParseAndEscape(null, querystrIndex, standard), "standard");*/
        addDoc(w, querystrIndex, "raw");
        w.close();
        
        // 2. query
       

        // the "title" arg specifies the default field to use
        // when no field is explicitly specified in the query.
        //querystr = querystr.replaceAll(":","p");
        Query q = new QueryParser("title", whitespaceAnalyzer).parse(QueryParserBase.escape(query));

        // 3. search
        int hitsPerPage = 10;
        IndexReader reader = DirectoryReader.open(index);
        IndexSearcher searcher = new IndexSearcher(reader);
        searcher.setSimilarity(new BM25Similarity(1.2f, 0.75f));
        TopDocs docs = searcher.search(q, hitsPerPage);
        ScoreDoc[] hits = docs.scoreDocs;

        // 4. display results
        System.out.println("Found " + hits.length + " hits.");
        for(int i=0;i<hits.length;++i) {
            int docId = hits[i].doc;
            Document d = searcher.doc(docId);
            System.out.println((i + 1) + ". " + d.get("isbn") + "\t" + d.get("title"));
        }

        // reader can only be closed when there
        // is no need to access the documents any more.
        reader.close();

	}	
	
	 private static void addDoc(IndexWriter w, String title, String isbn) throws IOException {
	        Document doc = new Document();
	        doc.add(new TextField("title", title, Field.Store.YES));

	        // use a string field for isbn because we don't want it tokenized
	        doc.add(new StringField("isbn", isbn, Field.Store.YES));
	        w.addDocument(doc);
	    }
	 
	
	 
	 
	 //@Test
	 public void gravaResultados() {
		 SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
		 
		//Grava resultados
			Experiment experiment = new Experiment();
			experiment.setDate(dateFormat.format(new Timestamp(Calendar.getInstance().getTimeInMillis())));
			experiment.setNumberOfTestedQuestions(10);
			experiment.setTag("ruby");
			experiment.setBbWeight(1.00d);
			experiment.setTtWeight(2.00d);
			experiment.setCcWeight(1.00d);
			experiment.setBtWeight(1.00d);
			experiment.setTbWeight(1.10d);
			experiment.setBm25b(0.05f);
			experiment.setBm25k(0.03f);
			
			experimentRepository.save(experiment);
						
			
			RecallRate recallRate = new RecallRate();
			recallRate.setExperimentId(experiment.getId());
			recallRate.setOrigem("BM25");
			recallRate.setHits10000(10000);
			recallRate.setHits1000(1000);
			recallRate.setHits100(100);
			recallRate.setHits50(50);
			recallRate.setHits20(20);
			recallRate.setHits10(10);
			recallRate.setHits5(5);
			recallRate.setHits1(1);
			
			recallRate.setRecallrate_10000(80.15);
			recallRate.setRecallrate_1000(70.15);
			recallRate.setRecallrate_100(60.15);
			recallRate.setRecallrate_50(50.15);
			recallRate.setRecallrate_20(20.15);
			recallRate.setRecallrate_10(10.15);
			recallRate.setRecallrate_5(5.15);
			recallRate.setRecallrate_1(1.15);
			
			recallRateRepository.save(recallRate);
			
			
			recallRate = new RecallRate();
			recallRate.setExperimentId(experiment.getId());
			recallRate.setOrigem("Sum of Cosines");
			recallRate.setHits10000(10000);
			recallRate.setHits1000(1000);
			recallRate.setHits100(100);
			recallRate.setHits50(50);
			recallRate.setHits20(20);
			recallRate.setHits10(10);
			recallRate.setHits5(5);
			recallRate.setHits1(1);
			
			recallRate.setRecallrate_10000(80.15);
			recallRate.setRecallrate_1000(70.15);
			recallRate.setRecallrate_100(60.15);
			recallRate.setRecallrate_50(50.15);
			recallRate.setRecallrate_20(20.15);
			recallRate.setRecallrate_10(10.15);
			recallRate.setRecallrate_5(5.15);
			recallRate.setRecallrate_1(1.15);
			
			recallRateRepository.save(recallRate);
			
			System.out.println("\ntest\t a");
			System.out.println("\ntestaa\t b");

	}
	 

	 
	
	 
	
}
