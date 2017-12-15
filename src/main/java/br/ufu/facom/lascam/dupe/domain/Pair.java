package br.ufu.facom.lascam.dupe.domain;

import java.util.HashMap;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlRootElement;

import edu.stanford.nlp.stats.Counter;

@XmlRootElement
@Entity
@Table(name = "pair")
public class Pair {
	@Id
    @SequenceGenerator(name="pair_id", sequenceName="pair_id",allocationSize=1)
    @GeneratedValue(strategy=GenerationType.SEQUENCE, generator="pair_id")
	private Integer id;

	private Integer question1;

	private Integer question2;

	private Boolean duplicated;

	private String maintag;
	
	@Transient
	private HashMap<String, Feature> features;

	
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Pair other = (Pair) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Integer getQuestion1() {
		return question1;
	}

	public void setQuestion1(Integer question1) {
		this.question1 = question1;
	}

	public Integer getQuestion2() {
		return question2;
	}

	public void setQuestion2(Integer question2) {
		this.question2 = question2;
	}

	
	public Boolean getDuplicated() {
		return duplicated;
	}

	public void setDuplicated(Boolean duplicated) {
		this.duplicated = duplicated;
	}

	

	public String getMaintag() {
		return maintag;
	}

	public void setMaintag(String maintag) {
		this.maintag = maintag;
	}

	public HashMap<String, Feature> getFeatures() {
		return features;
	}

	public void setFeatures(HashMap<String, Feature> features) {
		this.features = features;
	}

	
		
	

}
