package br.ufu.facom.lascam.dupe.domain;

import java.util.HashMap;

import javax.persistence.Column;
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
@Table(name = "recallrate")
public class RecallRate {
	@Id
    @SequenceGenerator(name="recallrate_id", sequenceName="recallrate_id",allocationSize=1)
    @GeneratedValue(strategy=GenerationType.SEQUENCE, generator="recallrate_id")
	private Integer id;

	private String origem;

	private Integer hits50000;

	private Integer hits10000;
	
	private Integer hits1000;
	
	private Integer hits100;
	
	private Integer hits50;
	
	private Integer hits20;
	
	private Integer hits10;
	
	private Integer hits5;
	
	private Integer hits1;
	
	private Double recallrate_50000;

	private Double recallrate_10000;
	
	private Double recallrate_1000;
	
	private Double recallrate_100;
	
	private Double recallrate_50;
	
	private Double recallrate_20;
	
	private Double recallrate_10;
	
	private Double recallrate_5;
	
	private Double recallrate_1;
	
	@Column(name="experiment_id")
	private Integer experimentId;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getOrigem() {
		return origem;
	}

	public void setOrigem(String origem) {
		this.origem = origem;
	}

	
	public Integer getHits50000() {
		return hits50000;
	}

	public void setHits50000(Integer hits50000) {
		this.hits50000 = hits50000;
	}

	public Integer getHits10000() {
		return hits10000;
	}

	public void setHits10000(Integer hits10000) {
		this.hits10000 = hits10000;
	}

	public Integer getHits1000() {
		return hits1000;
	}

	public void setHits1000(Integer hits1000) {
		this.hits1000 = hits1000;
	}

	public Integer getHits100() {
		return hits100;
	}

	public void setHits100(Integer hits100) {
		this.hits100 = hits100;
	}

	public Integer getHits50() {
		return hits50;
	}

	public void setHits50(Integer hits50) {
		this.hits50 = hits50;
	}

	public Integer getHits20() {
		return hits20;
	}

	public void setHits20(Integer hits20) {
		this.hits20 = hits20;
	}

	public Integer getHits10() {
		return hits10;
	}

	public void setHits10(Integer hits10) {
		this.hits10 = hits10;
	}

	public Integer getHits5() {
		return hits5;
	}

	public void setHits5(Integer hits5) {
		this.hits5 = hits5;
	}

	public Integer getHits1() {
		return hits1;
	}

	public void setHits1(Integer hits1) {
		this.hits1 = hits1;
	}

	

	public Double getRecallrate_50000() {
		return recallrate_50000;
	}

	public void setRecallrate_50000(Double recallrate_50000) {
		this.recallrate_50000 = recallrate_50000;
	}

	public Double getRecallrate_10000() {
		return recallrate_10000;
	}

	public void setRecallrate_10000(Double recallrate_10000) {
		this.recallrate_10000 = recallrate_10000;
	}

	public Double getRecallrate_1000() {
		return recallrate_1000;
	}

	public void setRecallrate_1000(Double recallrate_1000) {
		this.recallrate_1000 = recallrate_1000;
	}

	public Double getRecallrate_100() {
		return recallrate_100;
	}

	public void setRecallrate_100(Double recallrate_100) {
		this.recallrate_100 = recallrate_100;
	}

	public Double getRecallrate_50() {
		return recallrate_50;
	}

	public void setRecallrate_50(Double recallrate_50) {
		this.recallrate_50 = recallrate_50;
	}

	public Double getRecallrate_20() {
		return recallrate_20;
	}

	public void setRecallrate_20(Double recallrate_20) {
		this.recallrate_20 = recallrate_20;
	}

	public Double getRecallrate_10() {
		return recallrate_10;
	}

	public void setRecallrate_10(Double recallrate_10) {
		this.recallrate_10 = recallrate_10;
	}

	public Double getRecallrate_5() {
		return recallrate_5;
	}

	public void setRecallrate_5(Double recallrate_5) {
		this.recallrate_5 = recallrate_5;
	}

	public Double getRecallrate_1() {
		return recallrate_1;
	}

	public void setRecallrate_1(Double recallrate_1) {
		this.recallrate_1 = recallrate_1;
	}

	

	@Override
	public String toString() {
		return "RecallRate [id=" + id + ", origem=" + origem + ", hits50000=" + hits50000 + ", hits10000=" + hits10000
				+ ", hits1000=" + hits1000 + ", hits100=" + hits100 + ", hits50=" + hits50 + ", hits20=" + hits20
				+ ", hits10=" + hits10 + ", hits5=" + hits5 + ", hits1=" + hits1 + ", recallrate_50000="
				+ recallrate_50000 + ", recallrate_10000=" + recallrate_10000 + ", recallrate_1000=" + recallrate_1000
				+ ", recallrate_100=" + recallrate_100 + ", recallrate_50=" + recallrate_50 + ", recallrate_20="
				+ recallrate_20 + ", recallrate_10=" + recallrate_10 + ", recallrate_5=" + recallrate_5
				+ ", recallrate_1=" + recallrate_1 + ", experimentId=" + experimentId + "]";
	}

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
		RecallRate other = (RecallRate) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

	public Integer getExperimentId() {
		return experimentId;
	}

	public void setExperimentId(Integer experimentId) {
		this.experimentId = experimentId;
	}

	
		
	

}
