package br.ufu.facom.lascam.dupe.repository;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import br.ufu.facom.lascam.dupe.domain.Post;
import br.ufu.facom.lascam.dupe.domain.PostLink;



public interface PostLinksRepository extends CrudRepository<PostLink, Integer> {

	List<PostLink> getByPostId(Integer postId);
    
	

	
}
