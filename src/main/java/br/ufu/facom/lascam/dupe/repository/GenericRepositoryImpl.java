package br.ufu.facom.lascam.dupe.repository;

import java.math.BigInteger;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import br.ufu.facom.lascam.dupe.domain.Pair;
import br.ufu.facom.lascam.dupe.domain.Post;
import br.ufu.facom.lascam.dupe.domain.PostLink;
import br.ufu.facom.lascam.dupe.domain.Question;
import br.ufu.facom.lascam.dupe.util.DupeUtils;

@Repository
public class GenericRepositoryImpl implements GenericRepository {
	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@PersistenceContext
	private EntityManager em;

	

	
	
	@Override
	public List<PostLink> getPostsLinksByPostId(Integer postId) {
		Query q = em.createNativeQuery("select * from postlinks where linktypeid = 3 AND postid=:id ", PostLink.class);
		q.setParameter("id", postId);
		List<PostLink> postLinks = q.getResultList();
		//logger.info("PostLinks em getPostsLinksByPostId: "+postLinks.size());
		return postLinks;
		
	}
	
	
		

	@Override
	public List<PostLink> getAllPostLinks() {
		Query q = em.createNativeQuery("select * from postlinks p where linktypeid = 3", PostLink.class);
		return (List<PostLink>) q.getResultList();
	}

	
	

	@Override
	public int getTotalAnswersByQuestion(Integer id) {
		Query q = em.createNativeQuery(" select count(id) from posts p where p.parentid = :parentId ");
		q.setParameter("parentId", id);
		Integer count = ((BigInteger)q.getSingleResult()).intValue();
		return count;
	}
	
	
	
	
	
	@Override
	public List<Post> findClosedDuplicatedNonMastersByTag(String tagFilter,String maxCreationDate) {
		String sql = " select * from posts  p " 
				+ " WHERE   p.posttypeId = 1 "
				+ " and p.closeddate is not null";
		
		if(!StringUtils.isBlank(maxCreationDate)) {
			sql+= " and p.creationdate < '"+maxCreationDate+"'";
		}
		
		sql += DupeUtils.getQueryComplementByTag(tagFilter);
		
		sql +=  " and p.id in "
				+ " ( select distinct(pl.postid)"
				+ " from postlinks pl where pl.linktypeid = 3 ) ";
		
		sql += " order by p.creationdate ";
		
		
		Query q = em.createNativeQuery(sql, Post.class);
		
		List<Post> posts = q.getResultList();
				
		logger.info("Posts in findClosedDuplicatedNonMastersByTag: "+posts.size());
		for(Post post:posts){
			if(StringUtils.isBlank(post.getTitle()) || StringUtils.isBlank(post.getBody())) {
				continue;
			}	
			DupeUtils.setBlanks(post);
		}
		
		
		return posts;
	}
	
	
	

	@Override
	/**
	 * Perguntas por tag não fechadas
	 */
	public List<Post> fetchQuestionsAtRandom(int pairCount, String tagFilter) {
		String sql = " select * from posts where id in "
				+ "  (  SELECT id"
				+ "     FROM posts p"
				+ "     WHERE p.postTypeId = 1"
				+ "     and p.closeddate is null";
		
		sql += DupeUtils.getQueryComplementByTag(tagFilter);
		
		sql += "     ORDER BY random()"
			 + "    limit "+pairCount+" )";
		
		Query q = em.createNativeQuery(sql, Post.class);
		
		return (List<Post>) q.getResultList();
		
		
	}


	@Override
	public List<Pair> fetchPairs(Integer totalDuplicated, String tagFilter, boolean isDuplicated) {
		String sqlPairs = "SELECT * FROM pair WHERE duplicated = "+isDuplicated+" and maintag = '" + tagFilter+"'";
		if(totalDuplicated!=null){
			sqlPairs+= " limit " + totalDuplicated; 
		}
		
		Query q = em.createNativeQuery(sqlPairs, Pair.class);
		
		return (List<Pair>) q.getResultList();
	}
	
	
	
	

	
	
	@Override
	public List<Post> fetchQuestionsByTag(String tagFilter, Integer limit) {
		String sql = " select * from posts  p " 
				+ " WHERE   p.posttypeId = 1 ";
		
		sql += DupeUtils.getQueryComplementByTag(tagFilter);
		
		sql += " order by p.id ";
		
		if(limit!=null){
			sql+= " limit "+limit;
		}
				
		Query q = em.createNativeQuery(sql, Post.class);
		
		List<Post> posts = q.getResultList();
				
		logger.info("Posts in fetchQuestionsByTag: "+posts.size());
		for(Post post:posts){
			if(StringUtils.isBlank(post.getTitle()) || StringUtils.isBlank(post.getBody())) {
				continue;
			}	
			DupeUtils.setBlanks(post);
		}
		
		
		return posts;
	}

	

	@Override
	public Boolean hasQuestionAnswers(Integer relatedPostId) {
		Query q = em.createNativeQuery(" select count(p.id) from posts p where p.parentid = :parentid ");
		q.setParameter("parentid", relatedPostId);
		Integer count = ((BigInteger)q.getSingleResult()).intValue();
		return count > 0;
	}


	
	

	
}
