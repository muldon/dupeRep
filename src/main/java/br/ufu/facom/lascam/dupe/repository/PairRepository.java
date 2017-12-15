package br.ufu.facom.lascam.dupe.repository;

import org.springframework.data.repository.CrudRepository;

import br.ufu.facom.lascam.dupe.domain.Pair;



public interface PairRepository extends CrudRepository<Pair, Integer> {

	void deleteByMaintag(String tagFilter);
    
	

	
}
