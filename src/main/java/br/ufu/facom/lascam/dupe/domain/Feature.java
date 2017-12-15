package br.ufu.facom.lascam.dupe.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@Entity
@Table(name = "feature")
public class Feature {
	@Id
    @SequenceGenerator(name="feature_id", sequenceName="feature_id",allocationSize=1)
    @GeneratedValue(strategy=GenerationType.SEQUENCE, generator="feature_id")
	private Integer id;
	
	
	@Column(name="pairid")
	private Integer pairId;
		
	/*
	 * TT: Title-Title
	 * TB: Title-Body
	 * BT: Body-Title
	 * BB: Body-Body
	 * TagTag: Tag-Tag
	 * CC: Code-Code
	 */
	private String type;
	
	private Double cosine;
	
	@Column(name="termoverlap")
	private Double termOverlap;
	
	@Column(name="entityoverlap")
	private Double entityOverlap;
	
	@Column(name="typeoverlap")
	private Double typeOverlap;
	
	@Column(name="wordnet")
	private Double wordNet;

	public Integer getPairId() {
		return pairId;
	}

	public void setPairId(Integer pairId) {
		this.pairId = pairId;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public Double getCosine() {
		return cosine;
	}

	public void setCosine(Double cosine) {
		this.cosine = cosine;
	}

	public Double getTermOverlap() {
		return termOverlap;
	}

	public void setTermOverlap(Double termOverlap) {
		this.termOverlap = termOverlap;
	}

	public Double getEntityOverlap() {
		return entityOverlap;
	}

	public void setEntityOverlap(Double entityOverlap) {
		this.entityOverlap = entityOverlap;
	}

	public Double getTypeOverlap() {
		return typeOverlap;
	}

	public void setTypeOverlap(Double typeOverlap) {
		this.typeOverlap = typeOverlap;
	}

	public Double getWordNet() {
		return wordNet;
	}

	public void setWordNet(Double wordNet) {
		this.wordNet = wordNet;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((pairId == null) ? 0 : pairId.hashCode());
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
		Feature other = (Feature) obj;
		if (pairId == null) {
			if (other.pairId != null)
				return false;
		} else if (!pairId.equals(other.pairId))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Feature [id=" + id + ", pairId=" + pairId + ", type=" + type + ", cosine=" + cosine + ", termOverlap=" + termOverlap + ", entityOverlap=" + entityOverlap + ", typeOverlap="
				+ typeOverlap + ", wordNet=" + wordNet + "]";
	}
	
	
	
	
	
	
}
