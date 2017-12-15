package br.ufu.facom.lascam.dupe.service;

import java.io.StringReader;
import java.util.HashMap;
import java.util.List;

import org.apache.lucene.queryparser.classic.QueryParserBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.terrier.indexing.Document;
import org.terrier.indexing.TaggedDocument;
import org.terrier.indexing.tokenisation.EnglishTokeniser;
import org.terrier.matching.ResultSet;
import org.terrier.querying.Manager;
import org.terrier.querying.SearchRequest;
import org.terrier.realtime.memory.MemoryIndex;
import org.terrier.utility.ApplicationSetup;

import br.ufu.facom.lascam.dupe.domain.Post;
import br.ufu.facom.lascam.dupe.domain.Question;
import br.ufu.facom.lascam.dupe.repository.GenericRepository;
import br.ufu.facom.lascam.dupe.util.DupeUtils;

@Component
public class TerrierSearcherPL2 {
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	@Autowired
	protected GenericRepository genericRepository;
	
	private Manager queryingManager;
	
	private ResultSet results;
	
	@Value("${maxResultSize}")
	public  String maxResultSize;  
	
	public void buildSearchManager(String mainTag, List<Post> questions) {
		try {
			
			ApplicationSetup.setProperty("indexer.meta.forward.keys", "docno");
	        ApplicationSetup.setProperty("indexer.meta.forward.keylens", "200");
	        ApplicationSetup.setProperty("querying.postfilters.order", "org.terrier.querying.SimpleDecorate");
	        ApplicationSetup.setProperty("querying.postfilters.controls", "decorate:org.terrier.querying.SimpleDecorate");
	        	
			logger.info("TerrierSearcherPL2.buildSearchManager. \nIndexando.... ");
			Integer maxIdFromPostsQuestions = new Integer(maxResultSize);
			 ApplicationSetup.setProperty("interactive.output.format.length", maxIdFromPostsQuestions+"");
		     ApplicationSetup.setProperty("trec.output.format.length", maxIdFromPostsQuestions+"");
		     ApplicationSetup.setProperty("matching.trecresults.length", maxIdFromPostsQuestions+"");
		     ApplicationSetup.setProperty("matching.retrieved_set_size", maxIdFromPostsQuestions+"");
		     ApplicationSetup.setProperty("block.indexing", true+"");
		     
			
		     logger.info("Number of questions to index: "+questions.size()+" \nIndexing.... ");
			
			
			MemoryIndex memIndex = new MemoryIndex();
			for (int i = 0; i < questions.size(); i++) {
				Post post = questions.get(i);
				String finalContent = post.getTitle()+ " "+post.getBody()+ " "+post.getTagsSyn();		
				//finalContent = DupeUtils.retiraSimbolosEspeciais(finalContent);
				Document document  = new TaggedDocument(new StringReader(finalContent), new HashMap<>(),new EnglishTokeniser());
				String docno = post.getId().toString();
				document.getAllProperties().put("docno",docno);
				memIndex.indexDocument(document);
			}
			
			queryingManager = new Manager(memIndex);
			
			logger.info("buildSearchManager finished ");
			
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
		
		
	}
	
	
	public Integer search(String content) {
		//content = QueryParserBase.escape(content);
		
		//content = content.replaceAll(":", "");
		//content = DupeUtils.retiraSimbolosEspeciais(content);
		
		SearchRequest srq = queryingManager.newSearchRequestFromQuery(content);
		srq.addMatchingModel("Matching", "PL2");
		srq.setControl("decorate", "on");
						
		queryingManager.runSearchRequest(srq);
		
		results = srq.getResultSet();
		
		Integer resultSize = results.getResultSize();
		
		return resultSize;
	}


	public Integer getDocno(int numTest) {
		return Integer.valueOf(results.getMetaItem("docno",numTest));
	}
	

}
