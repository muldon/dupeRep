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
@Table(name = "experiment")
public class Experiment {
	@Id
    @SequenceGenerator(name="experiment_id", sequenceName="experiment_id",allocationSize=1)
    @GeneratedValue(strategy=GenerationType.SEQUENCE, generator="experiment_id")
	private Integer id;

	private String tag;

	@Column(name="numberoftestedquestions")
	private Integer numberOfTestedQuestions;

	@Column(name="date")
	private String date;
	
	@Column(name="ttweight")
	private Double ttWeight;
		
	@Column(name="ccweight")
	private Double ccWeight;
		
	@Column(name="bbweight")
	private Double bbWeight;
	
	@Column(name="topictopicweight")
	private Double topicTopicWeight;
	
	@Column(name="btweight")
	private Double btWeight;
		
	@Column(name="tbweight")
	private Double tbWeight;
	
	@Column(name="aaweight")
	private Double aaWeight;
	
	@Column(name="tagtagweight")
	private Double tagTagWeight;
	
	@Column(name="bm25k")
	private Float bm25k;
	
	@Column(name="bm25b")
	private Float bm25b;
	
	private String observacao;
	
	private String duration;
	
	private Integer lote;
	
	private String base;
	
	private String app;
	
	@Column(name="maxresultsize")
	private Integer maxresultsize;
	
	@Column(name="estimateweights")
	private Boolean estimateWeights;
	
	private String trm;
	
	
	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getTag() {
		return tag;
	}

	public void setTag(String tag) {
		this.tag = tag;
	}

	public Integer getNumberOfTestedQuestions() {
		return numberOfTestedQuestions;
	}

	public void setNumberOfTestedQuestions(Integer numberOfTestedQuestions) {
		this.numberOfTestedQuestions = numberOfTestedQuestions;
	}

	

	

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
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
		Experiment other = (Experiment) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

	

	@Override
	public String toString() {
		return "Experiment [id=" + id + ", tag=" + tag + ", numberOfTestedQuestions=" + numberOfTestedQuestions
				+ ", date=" + date + ", ttWeight=" + ttWeight + ", ccWeight=" + ccWeight + ", bbWeight=" + bbWeight
				+ ", btWeight=" + btWeight + ", tbWeight=" + tbWeight + ", bm25k=" + bm25k + ", bm25b=" + bm25b
				+ ", observacao=" + observacao + ", duration=" + duration + ", lote=" + lote + ", base=" + base
				+ ", maxresultsize=" + maxresultsize + "]";
	}

	public Double getTtWeight() {
		return ttWeight;
	}

	public void setTtWeight(Double ttWeight) {
		this.ttWeight = ttWeight;
	}

	public Double getCcWeight() {
		return ccWeight;
	}

	public void setCcWeight(Double ccWeight) {
		this.ccWeight = ccWeight;
	}

	public Double getBbWeight() {
		return bbWeight;
	}

	public void setBbWeight(Double bbWeight) {
		this.bbWeight = bbWeight;
	}

	public Double getBtWeight() {
		return btWeight;
	}

	public void setBtWeight(Double btWeight) {
		this.btWeight = btWeight;
	}

	public Double getTbWeight() {
		return tbWeight;
	}

	public void setTbWeight(Double tbWeight) {
		this.tbWeight = tbWeight;
	}

	public Float getBm25k() {
		return bm25k;
	}

	public void setBm25k(Float bm25k) {
		this.bm25k = bm25k;
	}

	public Float getBm25b() {
		return bm25b;
	}

	public void setBm25b(Float bm25b) {
		this.bm25b = bm25b;
	}

	public String getObservacao() {
		return observacao;
	}

	public void setObservacao(String observacao) {
		this.observacao = observacao;
	}

	

	public Integer getLote() {
		return lote;
	}

	public void setLote(Integer lote) {
		this.lote = lote;
	}

	public String getBase() {
		return base;
	}

	public void setBase(String base) {
		this.base = base;
	}

	

	public Integer getMaxresultsize() {
		return maxresultsize;
	}

	public void setMaxresultsize(Integer maxresultsize) {
		this.maxresultsize = maxresultsize;
	}

	public String getDuration() {
		return duration;
	}

	public void setDuration(String duration) {
		this.duration = duration;
	}

	public Double getAaWeight() {
		return aaWeight;
	}

	public void setAaWeight(Double aaWeight) {
		this.aaWeight = aaWeight;
	}

	public String getTrm() {
		return trm;
	}

	public void setTrm(String trm) {
		this.trm = trm;
	}

	public Double getTagTagWeight() {
		return tagTagWeight;
	}

	public void setTagTagWeight(Double tagTagWeight) {
		this.tagTagWeight = tagTagWeight;
	}

	public String getApp() {
		return app;
	}

	public void setApp(String app) {
		this.app = app;
	}

	public Boolean getEstimateWeights() {
		return estimateWeights;
	}

	public void setEstimateWeights(Boolean estimateWeights) {
		this.estimateWeights = estimateWeights;
	}

	public Double getTopicTopicWeight() {
		return topicTopicWeight;
	}

	public void setTopicTopicWeight(Double topicTopicWeight) {
		this.topicTopicWeight = topicTopicWeight;
	}

	
	
		
	

}
