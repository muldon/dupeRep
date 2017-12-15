package br.ufu.facom.lascam.dupe.service;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

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
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.queryparser.classic.QueryParserBase;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import br.ufu.facom.lascam.dupe.domain.Post;
import br.ufu.facom.lascam.dupe.domain.Question;
import br.ufu.facom.lascam.dupe.repository.GenericRepository;

@Component
public class LuceneSearcherBM25 {
	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	protected GenericRepository genericRepository;

	private Analyzer standardAnalyzer;
	private Analyzer englishAnalyzerWithStopWords;
	private Analyzer englishAnalyzerOnlyStemming;
	private Analyzer whiteSpace;
	//private Analyzer analyzer;

	private Directory index;
	
	public static Integer indexedListSize;

	private ScoreDoc[] hits;

	private IndexSearcher searcher;

	private IndexReader reader;

	private Integer parseErrosNum;

	private Map<Integer, Post> questionsCache;
	
	private Map<Integer, Post> questionsParseErrors;

	private IndexWriterConfig config;
	
	private QueryParser englishQPWithStopWordsAnalyzer;
	private QueryParser englishQPOnlyStemmingAnalyzer;
	private QueryParser whiteSpaceQP;
	
	
	@PostConstruct
	public void initializeConfigs() throws Exception {
		CharArraySet stopWords = EnglishAnalyzer.getDefaultStopSet();
		parseErrosNum = 0;
		questionsCache = new HashMap<>();
		questionsParseErrors = new HashMap<>();
		// 0. Specify the analyzer for tokenizing text.
		// The same analyzer should be used for indexing and searching
		standardAnalyzer = new StandardAnalyzer();
		englishAnalyzerWithStopWords = new EnglishAnalyzer(stopWords);
		englishAnalyzerOnlyStemming = new EnglishAnalyzer();
		
		whiteSpace = new WhitespaceAnalyzer();
		
		// 1. create the index
		index = new RAMDirectory();

		config = new IndexWriterConfig(standardAnalyzer);
		config.setSimilarity(new BM25Similarity(ClassifyService.bm25ParameterK, ClassifyService.bm25ParameterB));
		
		englishQPWithStopWordsAnalyzer = new QueryParser("content", englishAnalyzerWithStopWords);
		englishQPOnlyStemmingAnalyzer = new QueryParser("onlyStemmingContent", englishAnalyzerOnlyStemming);
		
		whiteSpaceQP = new QueryParser("content", whiteSpace);

	}

	public void buildSearchManager(String mainTag, List<Post> questions) throws Exception {
		logger.info("LuceneSearcherBM25.buildSearchManager. Indexing por all questions for tag: " + mainTag);

		indexedListSize = questions.size();
		logger.info("Number of fetched questions: " + questions.size() + " \nIndexing.... ");
				
		//String postsIds = "Indexing... \n";
		IndexWriter w = new IndexWriter(index, config);
		for (int i = 0; i < questions.size(); i++) {
			Post post = questions.get(i);
			questionsCache.put(post.getId(), post);
			String finalContent = post.getTitle()+ " "+post.getBody()+ " "+post.getTagsSyn();			
			
			//logger.info("Indexando questão: "+post.getId()+ " - Conteudo: "+finalContent); 
			//postsIds+= post.getId() + " - ";
			/*if(i%20==0){
				postsIds+="\n";
			}*/
			addQuestion(w, finalContent, post.getId());
		}
		//logger.info(postsIds);
		w.close();
		
		//logger.info("Questions indexed. \nErrors in parse: "+questionsParseErrors.size());

		reader = DirectoryReader.open(index);
		searcher = new IndexSearcher(reader);
		//searcher.setSimilarity(new BM25Similarity(ClassifyService.bm25ParameterK, ClassifyService.bm25ParameterB));

	}
	
	

	/*public Query getStemmedFinalContent(Question post,Boolean isSearch) throws Exception {
		String content1 = DupeUtils.buildQuestionContentWithoutTagsAndCode(post);
		String content2 = DupeUtils.buildCodeContent(post.getBody());
		String contentTags = performOnlyStemming(DupeUtils.buildTagContent(post.getTags()),"tags",post);
		String erroMsg = "";
		Query stemmedQueryContent1 = performParse(englishQPWithStopWordsAnalyzer,content1,"QuestionContentWithoutTagsAndCode",post,"english");
		String questionContentSearch = null;
		
		if(stemmedQueryContent1==null){
			questionsParseErrors.put(post.getId(), post);
			if(isSearch){
				erroMsg = "Erro a tratar. Erro ao buscar questão parte 1: " + post.getId();	
			}else{
				erroMsg = "Erro a tratar. Erro ao indexar questão parte 1: " + post.getId();
			}
			logger.info(erroMsg);
			questionContentSearch = content1; //não conseguiu fazer o parse no english, continua sem parse
		}else{
			questionContentSearch = stemmedQueryContent1.toString("content"); 
		}
		
		questionContentSearch+=" "+content2+ " "+contentTags;
		
		Query stemmedFinalContent = performParse(whiteSpaceQP,questionContentSearch,"todo o conteudo do post",post,"white");
		if(stemmedFinalContent==null){
			questionsParseErrors.put(post.getId(), post);
			if(isSearch){
				erroMsg = "Erro a tratar. Erro ao buscar questão parte 2: " + post.getId();	
			}else{
				erroMsg = "Erro a tratar. Erro ao indexar questão parte 2: " + post.getId();
			}
			
			logger.info(erroMsg);
			return null;
		}
		
		
		return stemmedFinalContent;
	}*/

	/*public String performParseAndEscape(Question post, String escapedQuestionContent, Analyzer analizer) throws ParseException {
		QueryParser qp = new QueryParser("simpleContent", analizer);
		//String escapedQuestionContent = QueryParserBase.escape(content);
		//escapedQuestionContent = DupeUtils.retiraParenteses(escapedQuestionContent);
		
		Query stemmedQuery1 = performParse(qp,escapedQuestionContent,"parametrizado",post,"white");
		if(stemmedQuery1==null){ //não conseguiu parsear por tamanho de query
			return escapedQuestionContent;
		}
		
		qp = null;
		escapedQuestionContent = stemmedQuery1.toString("simpleContent");
		return escapedQuestionContent;
	}*/

	/*public String performStemmingStopWordsToIndex(Question post,Boolean isSearch) throws Exception {
		IndexWriterConfig config = null;
		Analyzer analyz = null;
		
        CharArraySet stopWords = EnglishAnalyzer.getDefaultStopSet();
        analyz = new EnglishAnalyzer(stopWords);
        config = new IndexWriterConfig(analyz);
        
        QueryParser qp = new QueryParser("text", config.getAnalyzer());
        Query stemmedQuery1 = getStemmedQuery(qp,post,false,null);
        String titleBody = DupeUtils.buildQuestionContent(post);
		if(stemmedQuery1!=null){
			titleBody = stemmedQuery1.toString("text");
		}
				
		qp = null;
		config = null;
		analyz = null;
		if(isSearch){ //tags são acrescentadas em outro momento
			return titleBody;
		}
		
		String finalQuestionContent = titleBody+" "+parseTags(DupeUtils.buildTagContent(post.getTags())); 
		return finalQuestionContent;
            
	}*/

	/*private String parseTags(String tagContent) throws ParseException {
		QueryParser qp = new QueryParser("tags", standardAnalyzer);
		Query tagsQuery = qp.parse(tagContent);
		return tagsQuery.toString("tags");
	}*/

	
	
	/*public String performParseWhiteSpaceAnalyzer(String questionContent,Question master) throws ParseException  {
		if("".equals(questionContent.trim())){
			return "";
		}
		Query stemmedQuery1 = performParse(whiteSpaceQP,questionContent,parte,master,"white");
		if(stemmedQuery1==null){ //não conseguiu parsear
			logger.error("Erro a tratar. Não consegui parsear, parte: "+parte+ " - "+master.getId());
			return questionContent;
		}
		
		return stemmedQuery1.toString("content");
	}*/
	
	//para tags
	/*public String performOnlyStemming(String content, String parte, Question master) throws Exception {
		if("".equals(content.trim())){
			return "";
		}
		Query stemmedQuery1 = performParse(englishQPOnlyStemmingAnalyzer,content,parte,master,"english");
		if(stemmedQuery1==null){ //não conseguiu parsear
			logger.error("Erro a tratar. Não consegui parsear em extract features. performOnlyStemming - Parte: "+parte+ " - "+master.getId()+ " -> "+content);
			return content;
		}
		
		return stemmedQuery1.toString("onlyStemmingContent");
	}*/
	
	

	/*private Query getStemmedQuerySearch(QueryParser qp, Question post, String stemmedStoppedQuestionContentWithoutTags) throws Exception {
		if(post.getId().equals(44619)){
			System.out.println();
		}
		//stemmedStoppedQuestionContentWithoutTags += " "+DupeUtils.buildTagContent(post.getTags());
		//stemmedStoppedQuestionContentWithoutTags = DupeUtils.buildQuestionContentWithoutTagsAndCode(post);
		 
		Query stemmedQuery1 = performParse(qp,stemmedStoppedQuestionContentWithoutTags);
		
		return stemmedQuery1;
	}
*/
	

	private Query performParse(QueryParser qp, String escapedQuestionContent, Post master, String analyzer, Boolean escape) throws ParseException {
		Query stemmedQuery1= null;
		boolean retry = true;
		while (retry) {
			try {
				retry = false;
				if(escape){
					escapedQuestionContent = QueryParserBase.escape(escapedQuestionContent);
				}
				stemmedQuery1 = qp.parse(escapedQuestionContent);
			} catch (ParseException e) {
				if(e.getCause().toString().contains("TooManyClauses")){
					// Double the number of boolean queries allowed.
					// The default is in org.apache.lucene.search.BooleanQuery and
					// is 1024.
					String defaultQueries = Integer.toString(BooleanQuery.getMaxClauseCount());
					int oldQueries = Integer.parseInt(System.getProperty("org.apache.lucene.maxClauseCount", defaultQueries));
					int newQueries = oldQueries * 2;
					logger.info("Too many hits for query. Increasing to " + newQueries);
					System.setProperty("org.apache.lucene.maxClauseCount", Integer.toString(newQueries));
					BooleanQuery.setMaxClauseCount(newQueries);
					retry = true;
				}				
				else{
					logger.error("Error. PerformParse - Analyzer: "+analyzer+" - I could not parse: "+escapedQuestionContent+ " - "+master.getId());
					throw e;
				}
			}
		}
		return stemmedQuery1;
	}
	/*private Query performParse(QueryParser qp, String escapedQuestionContent, String parte, Question master, String analyzer) throws ParseException {
		Query stemmedQuery1= null;
		boolean retry = true;
		while (retry) {
			try {
				retry = false;
				escapedQuestionContent = QueryParserBase.escape(escapedQuestionContent);
				stemmedQuery1 = qp.parse(escapedQuestionContent);
			} catch (ParseException e) {
				if(e.getCause().toString().contains("TooManyClauses")){
					// Double the number of boolean queries allowed.
					// The default is in org.apache.lucene.search.BooleanQuery and
					// is 1024.
					String defaultQueries = Integer.toString(BooleanQuery.getMaxClauseCount());
					int oldQueries = Integer.parseInt(System.getProperty("org.apache.lucene.maxClauseCount", defaultQueries));
					int newQueries = oldQueries * 2;
					logger.info("Too many hits for query. Increasing to " + newQueries);
					System.setProperty("org.apache.lucene.maxClauseCount", Integer.toString(newQueries));
					BooleanQuery.setMaxClauseCount(newQueries);
					retry = true;
				}				
				else{
					logger.error("Erro a tratar. PerformParse - Analyzer: "+analyzer+" - Não consegui parsear - parte: "+parte+ " - "+master.getId());
					throw e;
				}
			}
		}
		return stemmedQuery1;
	}*/

	public Integer search(Post post, Integer maxConsideredSeachNumberForClassifier) throws Exception {
		//Query stemmedFinalContent = getStemmedFinalContent(nonMaster,true);
		String content = post.getTitle()+ " "+post.getBody()+ " "+post.getTagsSyn();
		/*if(retiraTodosSimbolosTitleBodyTags){ //feito em pre-process
			content = DupeUtils.retiraSimbolosEspeciais(content);
		}*/
		
		Query stemmedFinalContent = performParse(whiteSpaceQP,content, post, "whiteSpace",true);
		if(stemmedFinalContent==null){
			return 0;
		}
		//String finalContent = stemmedFinalContent.toString("content");
		TopDocs docs = searcher.search(stemmedFinalContent, maxConsideredSeachNumberForClassifier);
				
		hits = docs.scoreDocs;
		//logger.info("Buscando por questão: "+post.getId()+ " ResultSize: "+hits.length+"- Conteudo: "+finalContent);
		logger.info("Searching por question: "+post.getId()+ " ResultSize: "+hits.length);
		docs = null;
		stemmedFinalContent = null;
		//finalContent = null;
		
		return hits.length;
	}

	/*
	 * public Integer search(String querystr, Integer
	 * maxConsideredSeachNumberForClassifier) throws Exception { Query q = new
	 * QueryParser("content", analyzer).parse(querystr); TopDocs docs =
	 * searcher.search(q, maxConsideredSeachNumberForClassifier); hits =
	 * docs.scoreDocs; q = null; return hits.length; }
	 */

	public Integer getQuestion(int numTest) throws Exception {
		int docId = hits[numTest].doc;
		Document d = searcher.doc(docId);

		Integer id = Integer.valueOf(d.get("id"));

		return id;
	}
	/*
	 * public Question getQuestion(int numTest) throws Exception { int docId =
	 * hits[numTest].doc; Document d = searcher.doc(docId);
	 * 
	 * Integer id = Integer.valueOf(d.get("id")); String title = d.get("title");
	 * String body = d.get("body"); String tags = d.get("tags"); Question
	 * question = new Question(id,title,body,tags);
	 * 
	 * return question; }
	 */

	public Integer getDocno(int numTest) throws Exception {
		int docId = hits[numTest].doc;
		Document d = searcher.doc(docId);
		return Integer.valueOf(d.get("id"));
	}

	public void finalize() throws Exception {
		reader.close();
	}

	private static void addQuestion(IndexWriter w, String postContent, Integer id) throws IOException {
		Document doc = new Document();
		doc.add(new TextField("content", postContent, Field.Store.YES));
		doc.add(new StringField("id", id.toString(), Field.Store.YES));
		w.addDocument(doc);
	}

	public Post getQuestionById(Integer id) {
		return questionsCache.get(id);
	}
	
	public static String escapeTags(String s) {
	    StringBuilder sb = new StringBuilder();
	    for (int i = 0; i < s.length(); i++) {
	      char c = s.charAt(i);
	      // These characters are part of the query syntax and must be escaped
	      if (c == '<' || c == '>') {
	        sb.append("\\\\");
	      }
	      sb.append(c);
	    }
	    return sb.toString();
	  }

	public void setSearchSimilarityParams(Float bm25ParameterK, Float bm25ParameterB) {
		searcher.setSimilarity(new BM25Similarity(bm25ParameterK, bm25ParameterB));
		config.setSimilarity(new BM25Similarity(bm25ParameterK, bm25ParameterB));
		logger.info("Setting k: "+bm25ParameterK+ " b: "+bm25ParameterB);
	}

	
}