package br.ufu.facom.lascam.dupe.repository;

import java.util.List;

import br.ufu.facom.lascam.dupe.domain.Pair;
import br.ufu.facom.lascam.dupe.domain.Post;
import br.ufu.facom.lascam.dupe.domain.PostLink;
import br.ufu.facom.lascam.dupe.domain.Question;


public interface GenericRepository {
	
	
	public List<PostLink> getAllPostLinks();

	List<PostLink> getPostsLinksByPostId(Integer postId);

	public int getTotalAnswersByQuestion(Integer id);

	public List<Post> fetchQuestionsAtRandom(int pairCount, String tagFilter);

	
	public List<Pair> fetchPairs(Integer totalDuplicated, String tagFilter, boolean isDuplicated);

		
	public Boolean hasQuestionAnswers(Integer relatedPostId);

	
	List<Post> fetchQuestionsByTag(String tagFilter, Integer limit);

	public List<Post> findClosedDuplicatedNonMastersByTag(String tagFilter, String maxCreationDate);

	

	//public List<Integer> getMastersWithoutAnswers(String tagFilter);

	
	
    
}
