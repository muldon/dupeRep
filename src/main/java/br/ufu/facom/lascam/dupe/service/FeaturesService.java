package br.ufu.facom.lascam.dupe.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.ufu.facom.lascam.dupe.domain.Feature;
import br.ufu.facom.lascam.dupe.domain.Pair;
import br.ufu.facom.lascam.dupe.domain.Post;
import br.ufu.facom.lascam.dupe.domain.PostLink;
import br.ufu.facom.lascam.dupe.repository.FeatureRepository;
import br.ufu.facom.lascam.dupe.repository.GenericRepository;
import br.ufu.facom.lascam.dupe.repository.PairRepository;
import br.ufu.facom.lascam.dupe.repository.PostLinksRepository;
import br.ufu.facom.lascam.dupe.repository.PostsRepository;
import br.ufu.facom.lascam.dupe.util.DupeUtils;


@Service
@Transactional
public class FeaturesService {
	
	@Autowired
	protected PostsRepository postsRepository;
	
	@Autowired
	protected GenericRepository genericRepository;
	
	
	@Autowired
	protected PairRepository pairRepository;
	
	@Autowired
	protected DupeUtils dupeUtils;
	
	@Autowired
	protected PostLinksRepository postLinksRepository;
	
	@Autowired
	protected FeatureRepository featureRepository;
	
	
		
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Value("${tagFilter}")
	public String tagFilter;
	
	@Value("${maxCreationDate}")
	public String maxCreationDate;
	
	private List<Post> rawClosedDuplicatedNonMastersByTag;
	
	private List<Post> nonDuplicatedQuestions;
	
	//private Map<Integer,Post> closedDuplicatedNonMastersMap;
	
	private Integer pairCount;

	private long initTime;
	
	private List<PostLink> postsLinks;	
	
	public FeaturesService() {
		
	}
	
	
	@Transactional(readOnly = true)
	public void findClosedDuplicatedNonMastersByTag() throws Exception {
		
		logger.info("recuperando rawClosedDuplicatedNonMastersByTag... :"+tagFilter);
		rawClosedDuplicatedNonMastersByTag = genericRepository.findClosedDuplicatedNonMastersByTag(tagFilter,maxCreationDate);
		logger.info("rawClosedDuplicatedNonMastersByTag: "+rawClosedDuplicatedNonMastersByTag.size());
		//postsLinks = dupeUtils.getAllPostLinks();
		//removeNonExistingMasters(); //this is done in generateFeaturesForDuplicatedQuestions
		
	}
	

	@Transactional(readOnly = true)
	public void findNonDuplicatedQuestions() {
	
		logger.info("fetching nonDuplicatedQuestions by tag... :"+tagFilter);
		nonDuplicatedQuestions = genericRepository.fetchQuestionsAtRandom(pairCount, tagFilter);
	
	}

	public void cleanOldData() {
		//clean old features for this tag
		featureRepository.deleteByTag(tagFilter);
		pairRepository.deleteByMaintag(tagFilter);
		logger.info("cleaned old data for tag... :"+tagFilter);
	}

	
	public void generateFeaturesForDuplicatedQuestions() throws Exception {
		logger.info("generateFeaturesForDuplicatedQuestions init");
						
		try {
			//dummy
			/*List<Question> processedClosedDuplicatedNonMastersByTag = new ArrayList<>();
			Question question1 = questionsRepository.findOne(339669);
			processedClosedDuplicatedNonMastersByTag.add(question1);*/
			//end dummy
			
			//logger.info("getMastersWithoutAnswers...");
			//List<Integer> mastersWithoutAnswers = genericRepository.getMastersWithoutAnswers(tagFilter);
			//logger.info("getMastersWithoutAnswers done, size: "+mastersWithoutAnswers.size());
			
			
			logger.info("Saving duplicated pairs and their features. Duplicated: "+rawClosedDuplicatedNonMastersByTag.size());
			pairCount = 0;
			int notDuplicated = 0;
			int masterHasNoAswers = 0;
			int notFoundInPostsTable =0;
			
			for(Post rawclosedDuplicatedNonMasterByTag: rawClosedDuplicatedNonMastersByTag){
				
				Set<Integer> relatedPostIds = dupeUtils.getRelatedPostIds(rawclosedDuplicatedNonMasterByTag);
			
				//dummy
				/*Set<Integer> relatedPostIds = new HashSet<>();
				List<PostLink> postLinks = postLinksRepository.getByPostId(339669);
				for(PostLink postLink:postLinks){
					relatedPostIds.add(postLink.getRelatedPostId());
				}*/
				
				//end dummy
				
				for(Integer relatedPostId:relatedPostIds){
					/* 
					 * Section 5.3
					 * Questions that do not contain any answers cannot be a master question in Stack Overflow. Therefore we also filter those questions that do not have any answers.
					 * The remaining set of questions are used to create question pairs with q
					 */
					Boolean hasAnswers =  genericRepository.hasQuestionAnswers(relatedPostId);
					//Boolean masterWithoutAnswers = mastersWithoutAnswers.contains(relatedPostId);
					if(!hasAnswers){
						masterHasNoAswers++;
						logger.info("Master has no answers: relatedId: "+relatedPostId+ " - disconsidering ! Count: "+masterHasNoAswers);
						continue;
					}
											
					Pair pair = new Pair();
					//pair.setId(pairCount); //sequence
					pair.setDuplicated(true);
					pair.setQuestion1(rawclosedDuplicatedNonMasterByTag.getId());
					pair.setQuestion2(relatedPostId);
					pair.setMaintag(tagFilter);
					Post related = postsRepository.findOne(relatedPostId);
					//Question related = closedDuplicatedNonMastersMap.get(relatedPostId);
					if(related==null){
						logger.info("Question has been deleted: "+relatedPostId+ " - disconsidering...");
						notFoundInPostsTable++;
						continue;
					}
					
					if(!related.getTagsSyn().contains(tagFilter)){
						logger.info("Question has master which with no common tag... disconsidering... nonMaster: "+rawclosedDuplicatedNonMasterByTag.getId()+ " relatedId: "+relatedPostId+ " tags1:"+rawclosedDuplicatedNonMasterByTag.getTagsSyn()+ " -tags2:"+related.getTagsSyn());
						pair = null;
						continue;
					}
					
					//logger.info("Saving duplicated pair and its features: "+pairCount);
					pairRepository.save(pair);
					
					HashMap<String, Feature> features = dupeUtils.extractFeatures(rawclosedDuplicatedNonMasterByTag, related);
					for (Entry<String, Feature> entry : features.entrySet()) {
						Feature feature = entry.getValue();
						feature.setPairId(pair.getId());
						feature.setType(entry.getKey());
						featureRepository.save(feature);
						features = null;
					}
					
					pairCount++;
					
				}
				
				
			
				
			}
			
			logger.info("Master questions which has been deleted and thus disconsidered: "+notFoundInPostsTable);
			logger.info("Master questions which have no answers and thus disconsidered: "+masterHasNoAswers);
			logger.info("Number of pairs generated for duplicated: " + pairCount+ "\nNot duplicated disconsidered: "+notDuplicated+"\nDisconsidered because related was excluded in posts: "+notFoundInPostsTable);
			/*
			 * pairCount = 17.132;
			 * mesmo número para não duplicadas, totalizando 34.264 
			 */
			
			
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
		
		
	}
	

	public void generateFeaturesForNonDuplicatedQuestions() {
		// Non-duplicated
		logger.info("Do lists have the same size ?\nPairs: "+pairCount+ " - Non duplicated list size: "+nonDuplicatedQuestions.size());
		logger.info("Saving non duplicated pairs and their features...");
		
		
		pairCount = 0;
		for(Post nonDuplicated: nonDuplicatedQuestions){
			Pair pair = new Pair();
			try {
				Collections.shuffle(rawClosedDuplicatedNonMastersByTag);
				//pair.setId(pairCount); //sequence
				pair.setDuplicated(false);
				pair.setMaintag(tagFilter);
				pair.setQuestion1(nonDuplicated.getId());
				
				Post closedDuplicatedNonMastersByTagShuffled = rawClosedDuplicatedNonMastersByTag.get(0);
				//logger.info("Suffled question id: "+closedDuplicatedNonMastersByTagShuffled.getId()+ " -Title: "+closedDuplicatedNonMastersByTagShuffled.getTitle());
				pair.setQuestion2(closedDuplicatedNonMastersByTagShuffled.getId());
				
				//logger.info("Saving non-duplicated pair and its features: "+pairCount);
				pairRepository.save(pair);
				
				HashMap<String, Feature> features = dupeUtils.extractFeatures(nonDuplicated,closedDuplicatedNonMastersByTagShuffled);
				for (Entry<String, Feature> entry : features.entrySet()) {
					Feature feature = entry.getValue();
					feature.setPairId(pair.getId());
					feature.setType(entry.getKey());
					featureRepository.save(feature);
					features = null;
				}
				
				pairCount++;
			} catch (Exception e) {
				logger.info(e.getMessage());
				continue;
			}
			//pairList.add(pair);
			
			
		}
		//clean resources
		rawClosedDuplicatedNonMastersByTag = null;
		nonDuplicatedQuestions = null;
						
		logger.info("FeatureGen Pairs generated for non-duplicated: " + pairCount);
		
	}

	
	
	
	/*private void removeNonExistingMasters() {	
		Post post = null;
		logger.info("Verifying masters which does not exists in posts table...");
		List<Post> excludingNonMasters = new ArrayList<>();
		for(Post nonMaster: rawClosedDuplicatedNonMastersByTag) {
			Set<Integer> mastersIds = getMastersIdsOfNonMaster(postsLinks,nonMaster); 
			for(Integer masterId: mastersIds) {
				post = postsRepository.findOne(masterId);
				if(post == null) {
					logger.info("Master does not exist in posts table: "+masterId+ " - removing...");
					excludingNonMasters.add(nonMaster);
				}
			}
			mastersIds = null;
		}
		logger.info("Number of excluded questions because master does not exist in allPostsQuestions: "+excludingNonMasters.size());
		int sizeBefore = rawClosedDuplicatedNonMastersByTag.size();
		rawClosedDuplicatedNonMastersByTag.removeAll(excludingNonMasters);
		excludingNonMasters = null;
		logger.info("Size of rawClosedDuplicatedNonMastersByTag before: "+sizeBefore+ "\nSize after cleaning: "+rawClosedDuplicatedNonMastersByTag.size());
		
	}

	private Set<Integer> getMastersIdsOfNonMaster(List<PostLink> postsLinks,Post nonMaster) {
		Set<Integer> mastersIds = new HashSet<>();
		for(PostLink postLink: postsLinks) {
			if(postLink.getPostId().equals(nonMaster.getId())) {
				mastersIds.add(postLink.getRelatedPostId());
			}
		}
	
		return mastersIds;
	}*/
	

	@Transactional(readOnly = true)
	public List<PostLink> getPostsLinksByPostId(Integer postId)  {
		return genericRepository.getPostsLinksByPostId(postId);
	}
	
	

	@Transactional(readOnly = true)
	public Post findPostById(Integer id) {
		return postsRepository.findOne(id);
	}

	


	@Transactional(readOnly = true)
	public List<PostLink> getAllPostLinks() {
		return genericRepository.getAllPostLinks();
	}


	public String getTagFilter() {
		return tagFilter;
	}


	public void setTagFilter(String tagFilter) {
		this.tagFilter = tagFilter;
	}


	


	
	
}
