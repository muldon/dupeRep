package br.ufu.facom.lascam.dupe.domain;

import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlRootElement;

import edu.stanford.nlp.stats.Counter;

@XmlRootElement
@Entity
@Table(name = "postsquestions")
public class Question {
	@Id
	public Integer id;

	public String title;
	
	public String body;
	
	public String tags;
	
	public String code;
	
	public String errormessages;
	
	private Timestamp closeddate;
	
	public String originaltags;
	
	
	@Transient
	public Counter<String> titleText, titleEntity, titleEntityType;  
	
	@Transient
	public Counter<String> bodyText, bodyEntity, bodyEntityType;
	
	@Transient
	public Counter<String> tagText;
	
	@Transient
	public Integer relatedPostId;
	

	
	
	public Question() {
		super();
	}

	public Question(Integer id, String title, String body, String tags) {
		super();
		this.id = id;
		this.title = title;
		this.body = body;
		this.tags = tags;
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
		Question other = (Question) obj;
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

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getBody() {
		return body;
	}

	public void setBody(String body) {
		this.body = body;
	}

	public String getTags() {
		return tags;
	}

	public void setTags(String tags) {
		this.tags = tags;
	}

	public Counter<String> getTitleText() {
		return titleText;
	}

	public void setTitleText(Counter<String> titleText) {
		this.titleText = titleText;
	}

	public Counter<String> getTitleEntity() {
		return titleEntity;
	}

	public void setTitleEntity(Counter<String> titleEntity) {
		this.titleEntity = titleEntity;
	}

	public Counter<String> getTitleEntityType() {
		return titleEntityType;
	}

	public void setTitleEntityType(Counter<String> titleEntityType) {
		this.titleEntityType = titleEntityType;
	}

	public Counter<String> getBodyText() {
		return bodyText;
	}

	public void setBodyText(Counter<String> bodyText) {
		this.bodyText = bodyText;
	}

	public Counter<String> getBodyEntity() {
		return bodyEntity;
	}

	public void setBodyEntity(Counter<String> bodyEntity) {
		this.bodyEntity = bodyEntity;
	}

	public Counter<String> getBodyEntityType() {
		return bodyEntityType;
	}

	public void setBodyEntityType(Counter<String> bodyEntityType) {
		this.bodyEntityType = bodyEntityType;
	}

	public Integer getRelatedPostId() {
		return relatedPostId;
	}

	public void setRelatedPostId(Integer relatedPostId) {
		this.relatedPostId = relatedPostId;
	}

	@Override
	public String toString() {
		return "Question [id=" + id + ", title=" + title + ", body=" + body + ", tags=" + tags + "]";
	}

	public Counter<String> getTagText() {
		return tagText;
	}

	public void setTagText(Counter<String> tagText) {
		this.tagText = tagText;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public Timestamp getCloseddate() {
		return closeddate;
	}

	public void setCloseddate(Timestamp closeddate) {
		this.closeddate = closeddate;
	}

	public String getOriginaltags() {
		return originaltags;
	}

	public void setOriginaltags(String originaltags) {
		this.originaltags = originaltags;
	}

	public String getErrormessages() {
		return errormessages;
	}

	public void setErrormessages(String errormessages) {
		this.errormessages = errormessages;
	}

	
	

}
