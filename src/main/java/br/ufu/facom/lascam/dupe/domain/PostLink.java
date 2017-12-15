package br.ufu.facom.lascam.dupe.domain;

import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@Entity
@Table(name = "postlinks")
public class PostLink {
	private static final long serialVersionUID = -111652190111815641L;
	@Id
    private Integer id;
	
	@Column(name="creationdate")
	private Timestamp creationDate;
    	
	@Column(name="postid")
	private Integer postId;
	
	@Column(name="relatedpostid")
	private Integer relatedPostId;
	
	@Column(name="linktypeid")
	private Integer linkTypeId;
	
	
	@Transient
	private Integer posicao;
	
		
	
	public PostLink() {
	}
	
	
	

	public PostLink(Integer postId, Integer relatedPostId) {
		super();
		this.postId = postId;
		this.relatedPostId = relatedPostId;
	}




	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((postId == null) ? 0 : postId.hashCode());
		result = prime * result + ((relatedPostId == null) ? 0 : relatedPostId.hashCode());
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
		PostLink other = (PostLink) obj;
		if (postId == null) {
			if (other.postId != null)
				return false;
		} else if (!postId.equals(other.postId))
			return false;
		if (relatedPostId == null) {
			if (other.relatedPostId != null)
				return false;
		} else if (!relatedPostId.equals(other.relatedPostId))
			return false;
		return true;
	}




	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Timestamp getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(Timestamp creationDate) {
		this.creationDate = creationDate;
	}

	public Integer getPostId() {
		return postId;
	}

	public void setPostId(Integer postId) {
		this.postId = postId;
	}

	public Integer getRelatedPostId() {
		return relatedPostId;
	}

	public void setRelatedPostId(Integer relatedPostId) {
		this.relatedPostId = relatedPostId;
	}

	public Integer getLinkTypeId() {
		return linkTypeId;
	}

	public void setLinkTypeId(Integer linkTypeId) {
		this.linkTypeId = linkTypeId;
	}

	@Override
	public String toString() {
		return "PostLink [id=" + id + ", creationDate=" + creationDate + ", postId=" + postId + ", relatedPostId="
				+ relatedPostId + ", linkTypeId=" + linkTypeId + "]";
	}




	public Integer getPosicao() {
		return posicao;
	}




	public void setPosicao(Integer posicao) {
		this.posicao = posicao;
	}

	

	
	
    
}