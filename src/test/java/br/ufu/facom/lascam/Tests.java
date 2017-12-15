package br.ufu.facom.lascam;

import java.io.IOException;
import java.io.StringReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DurationFormatUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.core.StopFilter;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.en.PorterStemFilter;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
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
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.AttributeFactory;

import br.ufu.facom.lascam.dupe.domain.Post;
import br.ufu.facom.lascam.dupe.domain.Question;
import br.ufu.facom.lascam.dupe.util.DupeUtils;


public class Tests {
	
	
	private QueryParser whiteSpaceQP;
	private StandardAnalyzer standardAnalyzer;
	private Analyzer whiteSpace;
	private QueryParser standardQP;
	private Analyzer englishAnalyzerWithStopWords;
	private QueryParser englishQP;
	public static Float bm25ParameterK = 0.05f;
	public static Float bm25ParameterB = 0.03f;
	DecimalFormat twoDForm = new DecimalFormat("#.##");
	
	public Tests() throws Exception {
		CharArraySet stopWords = EnglishAnalyzer.getDefaultStopSet();
		standardAnalyzer = new StandardAnalyzer();
		standardQP = new QueryParser("content", standardAnalyzer);
		whiteSpace = new WhitespaceAnalyzer();
		whiteSpaceQP = new QueryParser("content", whiteSpace);
		
		englishAnalyzerWithStopWords = new EnglishAnalyzer(stopWords);
		englishQP = new QueryParser("content", englishAnalyzerWithStopWords);
		
		Integer zero = 0;
		System.out.println(zero.equals(0));
		
		
		
		List<Integer> dupe = new ArrayList<>();
		List<Integer> non = new ArrayList<>();
		for(int i=0; i<100; i++){
			if(i%5==0){
				dupe.add(i);
			}else{
				non.add(i);
			}
		}
		//System.out.println(dupe);
		//System.out.println(non);
		
		String tester = "     ";
		//String testerTrim = tester.trim();
		//System.out.println(testerTrim);	
		//System.out.println(tester);
		Question question = new Question();
		question.setBody(tester);
		//System.out.println(DupeUtils.buildQuestionContentToIndex(question));
		tester= " - teste \\ + - ! ( ) : ^ [ ] { } ~ * ? & | / .  , ' \" ` ' = % $ # @ < > ; +-!():^[]{}";
		String title = "What are :+ and &:+ in Ruby?\"WSS\" qq";
		String finalContent = title;
		
	
		
		//String titleComEspecialChars = "What's the, equivalent ,of C#'s int.TryParse(): T3st :- :! method in Ruby? Nokogiri/Xpath namespace query or tester ?[duplicate] teste. teste .ss - teste testing tested a the is all he are you read reading need needing do did done \\ + - ! ( ) : ^ [ ] { } ~ * ? & | / .  , ' \" ` ' = % $ # @ < > ; +-!():^[]{} OR AND";
		String titleComEspecialChars = "here bla bla2 <pre><code><structure:Description xml:lang=\"en\">Canola (rapeseed) error message .. 23323 3jh34 jkldfa jjd \nclass Ability\n" + 
				"  include CanCan::Ability\n" + 
				"\n" + 
				"  def initialize(user)\n" + 
				"\n" + 
				"    user ||= User.new # guest user (not logged in)\n" + 
				"\n" + 
				"    if user.roles == \"admin\"\n" + 
				"      can :manage , :all\n" + 
				"    elsif user.roles == \"manager\"\n" + 
				"      can :read, Products, Delivery\n" + 
				"    elsif user.roles == \"customer\"\n" + 
				"      can :read, Services\n" + 
				"    end\n" + 
				"end\n" + 
				"end</structure:Description></code></pre> outrobla ";
		
		titleComEspecialChars = "<p><strong>How can you <code>git-checkout</code> without overwriting the data?</strong></p>\n" + 
				"\n" + 
				"<p>I run </p>\n" + 
				"\n" + 
				"<pre><code> git checkout master\n" + 
				"</code></pre>\n" + 
				"\n" + 
				"<p>I get</p>\n" + 
				"\n" + 
				"<pre><code>error: Entry 'forms/answer.php' would be overwritten by merge. Cannot merge.\n" + 
				"</code></pre>\n" + 
				"\n" + 
				"<p>This is surprising, since I did not know that Git merges when I <code>git-checkout</code>.\n" + 
				"I have always run after the command separately <code>git merge new-feature</code>. \n" + 
				"This seems to be apparently unnecessary if Git merges at checkout.</p>\n" + 
				"";
		
		
		System.out.println(titleComEspecialChars.contains("error: "));
	/*	
		DupeUtils dupeUtils = new DupeUtils();
		dupeUtils.initializeConfigs();
		dupeUtils.setTagFilter("git");
		String[] test = dupeUtils.separaSomentePalavrasNaoSomentePalavras(titleComEspecialChars,null,"title");
		
		System.out.println(test[0]); //somente palavras
		System.out.println(test[1]); //nao somente palavras
		System.out.println(test[2]); //somente codigo
		System.out.println(test[3]); //mensagens de erro
	
		*/
		String a = "";
		a = a.trim();
		//System.out.println(a.trim());
		//testSearch();
		/*
		 */
		/*Integer maxBM25ResultSize;
		
		for(bm25ParameterK = 0.01f; bm25ParameterK <= 1.50f; bm25ParameterK += 0.01f) {
				for(bm25ParameterB = 0.00f; bm25ParameterB <= 1.00f; bm25ParameterB += 0.01f) {
					count++;
					bm25ParameterK = redondear(bm25ParameterK,2);
					bm25ParameterB = redondear(bm25ParameterB,2);
					System.out.println("k: "+Tests.bm25ParameterK+ " - B: "+Tests.bm25ParameterB);
				}
		}*/
		
		
		//Double aaa = 0.01d;
		
		/*for(aaa=0.01d; aaa< 1.50d; aaa+=0.01d) {
			aaa= dupeUtils.redondear(aaa, 2);
			System.out.println(aaa);
		}*/
		
		
		//System.out.println(dupeUtils.calculateCosine("b d cdfaasfa eeeea 1", "e d c b a"));
		long initTimeQuestion = System.currentTimeMillis();
		
		/*Double ttWeight= 1d, bbWeight = 1d, btWeight = 1d ,tbWeight = 1d, ccWeight = 0d, eeWeight = 0d, aaWeight = 0d;
		int count =0;
		for(ttWeight = 0.5d; ttWeight <= 2.0d; ttWeight += 0.1f) {
			for(bbWeight = 0.5d; bbWeight <= 1.5f; bbWeight += 0.1f) {
				for(btWeight = 0.1d; btWeight <= 1.5f; btWeight += 0.1f) {
					for(tbWeight = 0.1d; tbWeight <= 1.5f; tbWeight += 0.1f) {
						count++;
						
						//System.out.println(count);
						ttWeight = dupeUtils.redondear(ttWeight,2);
						bbWeight = dupeUtils.redondear(bbWeight,2);
						btWeight = dupeUtils.redondear(btWeight,2);
						tbWeight = dupeUtils.redondear(tbWeight,2);
						
				
		}}}}*/
		long endTimeQuestion = System.currentTimeMillis();
		String duration = DurationFormatUtils.formatDuration(endTimeQuestion - initTimeQuestion, "HH:mm:ss,SSS");
		//System.out.println("Tempo gasto: "+duration);
		
		//System.out.println(count);
		
	/*	count =0;
		for(bm25ParameterK = 0.01f; bm25ParameterK <= 0.10f; bm25ParameterK += 0.01f) {
			for(bm25ParameterB = 0.01f; bm25ParameterB <= 0.10f; bm25ParameterB += 0.01f) {
				count++;
				
				
		}}
		System.out.println(count);*/
		//System.out.println(count);
		
		String rubyOnRailsRegex = "<ruby-on-rails-?(.*?)>";
		String tester2 = "<ruby-on-rails><ruby-on-rails-3><carrierwave><ruby>";
		
		Pattern ruby = Pattern.compile(rubyOnRailsRegex);
		final Matcher matcher = ruby.matcher(tester2);
		boolean found = false;
	    while (matcher.find()) {
	    	found = true;
	    	tester2 = tester2.replaceAll(matcher.group(0), "");
	    }
	    if(found && !tester2.contains("<ruby>")){
	    	tester2 = tester2 + "<ruby>";
	    }
	    
	    /*String s = "test test3 t3st test: word%5 test! testing t[st \\s includes newline. Try (?:(?!\n)\\s) or [^\\S\\n] instead: ^((\\w+\\S+)[^\\S\\n]+){1,6}.*$ Also, this includes the following space, so the rows with exactly six words will not be matched. Try this:"
				+ " ^((?:\\w+\\S*)(?:[^\\S\\n]+(\\w+\\S*)){0,5}).*$";
		
		Pattern pattern = Pattern.compile("(?<!\\S)\\p{Alpha}+(?!\\S)");
		Matcher matcher = pattern.matcher(s);
		while (matcher.find()){
			logger.info(matcher.group(0)); 
		} 
	    
	    */
		String body = "i can t rubygems gems? aaarubygems? figur out life me why thi won t work i m get that those thi error us thi yml folder _data    {% for game in site.data.games %}     <span>{% game.name %}</span> {% endfor %}     liquid exception: unknown tag 'game' in games/index.html  games:   - name: game1   (...)";
		System.out.println(body);
		body = body.replaceAll("rubygem", "gem");
		System.out.println(body);
		/*final Matcher matcher = dupeUtils.ONLY_WORDS_PATTERN.matcher(body);
		String sanitized = "";
		List<String> otherStopWords = dupeUtils.otherStopWords;
		
		
		StringTokenizer st = new StringTokenizer(body);
		while (st.hasMoreElements()) {
			//System.out.println(st.nextElement());
			//if(matcher.matches());
			String next = st.nextElement().toString();
			if(!otherStopWords.contains(next)){
				sanitized+= next+" ";
			}
		}
		System.out.println(sanitized);*/
	/*	test = dupeUtils.separaSomentePalavrasNaoSomentePalavras(body,null,"body");
		
		System.out.println(test[0]);
		System.out.println(test[1]);
		System.out.println(test[2]);*/
		//String san = dupeUtils.retiraSimbolosImportantes(body,"body");
		//System.out.println(san);
		
		/*while (matcher.find()) {
			found = matcher.group(0);
	    }
	    if(found && !tester2.contains("<ruby>")){
	    	tester2 = tester2 + "<ruby>";
	    }*/
		System.gc ();
		System.runFinalization();
		
	    //24091172
	    
	    //System.out.println(tester2);
		
		/*String rubyOnRailsRegex = "<ruby-on-rails?(.*?)>";
		String tags = "<ruby-on-rails> <gem>";
		Pattern ruby = Pattern.compile(rubyOnRailsRegex);
		final Matcher matcher = ruby.matcher(tags);
		boolean found = false;
	    while (matcher.find()) {
	        //tagValues.add(matcher.group(0));
	    	found = true;
	    	tags = tags.replaceAll(matcher.group(0), "");
	    }
	    if(found && !tags.contains("<ruby>")){
	    	tags = tags + "<ruby>";
	    }
		System.out.println(tags);*/
		int count = 0;
		/*while(true){
			Thread.sleep(1000);
			
		}*/
		/*for(int i=0; i<100; i++){
			double ttWeight = Math.random();
			ttWeight = dupeUtils.redondear(ttWeight,2);
			System.out.println(ttWeight);
		}*/
		double num;
		long iPart;
		double fPart;

		// Get user input
		num = 2.3d;
		iPart = (long) num;
		fPart = num - iPart;
		System.out.println("Integer part = " + iPart);
		System.out.println("Fractional part = " + fPart);

		double random, ttWeight, bbWeight;
		for(int i=0; i<10; i++){
			//int randomNum = ThreadLocalRandom.current().nextInt(6,10);
			//d= new Double(randomNum);
			random = ThreadLocalRandom.current().nextDouble(0.60,1.50);
			ttWeight = DupeUtils.redondear(random,2);
			
			random = ThreadLocalRandom.current().nextDouble(0.40,1.10);
			bbWeight = DupeUtils.redondear(random,2);
			
			System.out.println(ttWeight + " - "+bbWeight);
			
		}
		
		/*int randomNum = ThreadLocalRandom.current().nextInt(1, 30 + 1);
		long min = 30000;
		long totalTime = min + randomNum*1000;
		System.out.println(totalTime);
		//randon = randon*1000+ ;
		Thread.sleep(totalTime);
		System.out.println("ok");*/
		Integer ints[] = {1,2,3,4,5,6,7,8,9,10};
		
		List<Integer> integers = Arrays.asList(ints);
		Queue<Integer> queue = new LinkedList<Integer>(integers);
		
		while (!queue.isEmpty()) {
		    Integer i = queue.remove();
		    if (i == 2)
		        queue.add(42);

		   // System.out.println(i);
		}
		
		
		
		String trm;  
		String trmList[] = {"PL2","BM25"};
		for(int i=0; i<trmList.length; i++){
			
			trm = trmList[i];
			//System.out.println(trm);
		}
		
		DupeUtils dupeUtils = new DupeUtils();
		dupeUtils.initializeConfigs();
		//String[] test = dupeUtils.separaSomentePalavrasNaoSomentePalavrasTags("ruby-on-rails");
		//System.out.println(test);
		
	}
	
	
	public void testStemmingStop() throws Exception {
		//StringReader reader = new StringReader("teste \\ + - ! ( ) : ^ [ ] { } ~ * ? & | / .  , ' \" ` ' = % $ # @ < > ; +-!():^[]{} testing tested a the is all ");
		
		String titleComEspecialChars = "What's the, equivalent ,of C#'s int.TryParse(): T3st :- :! method in Ruby? or tester ?[duplicate] teste. teste .ss - teste testing tested a the is all he are you read reading need needing do did done \\ + - ! ( ) : ^ [ ] { } ~ * ? & | / .  , ' \" ` ' = % $ # @ < > ; +-!():^[]{} OR AND";
		String title2 = "The I am I am difference they between :+ and &:+ [duplicate]";
		
        //String title = "What's the equivalent of C#'s int.TryParse() method in Ruby? [Duplicate]";
        //String result = luceneSearcherBM25.performParseWhiteSpaceAnalyzer(titleComEspecialChars, "title", question1);
        String lower = title2.toLowerCase();
		
		String result = tokenizeStopStem(lower);
		//System.out.println(result);
		
		
		
		//String lower = titleComEspecialChars.toLowerCase();
		
		
		//String result = performStemmingStopWords(whiteSpaceQP,lower);
		//conserva especial chars
		
		//String result = performStemmingStopWords(standardQP,lower);
		//poda caracteres especiais
		
		//String result = performStemmingStopWords(englishQP,lower);
		//poda caracteres especiais
		
		//trata antes de pegar somenta as palavras
		/*String separated = lower.replaceAll("\\? ", " ");
		separated = separated.replaceAll(" \\?", " ");
		separated = separated.replaceAll(" \\.", " "); //retira somente pontos com espaços
		separated = separated.replaceAll("\\. ", " "); 
		separated = separated.replaceAll(" \\,", " "); 
		separated = separated.replaceAll("\\, ", " "); 
		separated = separated.replaceAll("\\: ", " "); 
		separated = separated.replaceAll("'s", "");
		separated = " "+separated+" ";
		
		String somentePalavras = "";
		List<String> palavras = getPalavras(DupeUtils.ONLY_WORDS_PATTERN, separated);
		for(String word: palavras){
			somentePalavras+= word+ " ";
		}
		
		
		String naoSomentePalavras = "";
		List<String> naoPalavras = getPalavras(DupeUtils.NOT_ONLY_WORDS_PATTERN, separated);
		for(String word: naoPalavras){
			naoSomentePalavras+= word+ " ";
		}
		
		
		//assertEquals(title, result);
		System.out.println("Original= "+titleComEspecialChars);
		System.out.println("Separado= "+separated);
		System.out.println("Words   = "+somentePalavras);
		System.out.println("not Words   = "+naoSomentePalavras);
		
		String result = tokenizeStopStem(somentePalavras);
		System.out.println("Token stop= "+result);*/
		
		
		/*String s = "test test3 t3st test: word%5 test! testing t[st \\s includes newline. Try (?:(?!\n)\\s) or [^\\S\\n] instead: ^((\\w+\\S+)[^\\S\\n]+){1,6}.*$ Also, this includes the following space, so the rows with exactly six words will not be matched. Try this:"
				+ " ^((?:\\w+\\S*)(?:[^\\S\\n]+(\\w+\\S*)){0,5}).*$";
		
		Pattern pattern = Pattern.compile("(?<!\\S)\\p{Alpha}+(?!\\S)");
		Matcher matcher = pattern.matcher(s);
		while (matcher.find()){
			System.out.println(matcher.group(0)); 
		} */

	}
	
	public static String tokenizeStopStem(String input) throws Exception {
		if (StringUtils.isBlank(input)) {
			return "";
		}
		CharArraySet stopWords = EnglishAnalyzer.getDefaultStopSet(); 
		AttributeFactory factory = AttributeFactory.DEFAULT_ATTRIBUTE_FACTORY;
		StandardTokenizer standardTokenizer = new StandardTokenizer(factory);
		standardTokenizer.setReader(new StringReader(input));
		TokenStream stream = new StopFilter(new LowerCaseFilter(new PorterStemFilter(standardTokenizer)), stopWords);

		CharTermAttribute charTermAttribute = standardTokenizer.addAttribute(CharTermAttribute.class);
		stream.reset();

		StringBuilder sb = new StringBuilder();
		String term = "";
		while (stream.incrementToken()) {
			if (sb.length() > 0) {
				sb.append(" ");
			}
			term = charTermAttribute.toString();
			sb.append(term);
		}

		stream.end();
		stream.close();
		
		String stopSteam1 = sb.toString();
		
		return stopSteam1;
	}
	
	public static List<String> getPalavras(Pattern patter,String str) {
	    final List<String> tagValues = new ArrayList<String>();
	    final Matcher matcher = patter.matcher(str);
	    while (matcher.find()) {
	        tagValues.add(matcher.group(0));
	    }
	    return tagValues;
	}
	
	
	

	public String performStemmingStopWords(QueryParser qp, String questionContent) throws ParseException  {
		if("".equals(questionContent.trim())){
			return "";
		}
		questionContent = QueryParserBase.escape(questionContent);
		Query stemmedQuery1 = qp.parse(questionContent);
		return stemmedQuery1.toString("content");
	}


	public static void main(String[] args) throws Exception {
		Tests t = new Tests();
		
		
		
		
		Map<Post,Integer> m = new HashMap<>();
		Post p1 = new Post(1);
		Post p2 = new Post(2);
		Post p3 = new Post(3);
		
		m.put(p1, 1);
		m.put(p2, 2);
		m.put(p3, 3);
		
		p1=null;
		p2=null;
		p3=null;
		
		for (Map.Entry<Post, Integer> entry : m.entrySet()) {
			System.out.println(entry.getKey() + " - "+entry.getValue());
		}
		
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
	       
	         
	        String query = " below ";
	        
	        IndexWriter w = new IndexWriter(index, config);
	        addDoc(w, "autodetect could find javascript runtim? below?", "1");
	        addDoc(w, "jdfd dflsadjf  sdljdfas dsf ds belowww", "2");
	        addDoc(w, "low", "3");
	        addDoc(w, "below here jaklfda as dfalkf ajdf dj h", "4");
	        addDoc(w, "below. aqui djassdjd dfd df", "5");
	        addDoc(w, "bel ere eejer  ", "6");
	        addDoc(w, "bellll ere eejer  ", "7");
	        
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
	 
	 public static float redondear(float pNumero, int pCantidadDecimales) {
		    // the function is call with the values Redondear(625.3f, 2)
		    BigDecimal value = new BigDecimal(pNumero);
		    value = value.setScale(pCantidadDecimales, RoundingMode.HALF_EVEN); // here the value is correct (625.30)
		    return value.floatValue(); // but here the values is 625.3
		}
	 
	 
}
