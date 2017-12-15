package br.ufu.facom.lascam.dupe.domain;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlRootElement;

import edu.stanford.nlp.stats.Counter;

@XmlRootElement
@Entity
@Table(name = "posts")
public class Post {
	private static final long serialVersionUID = -111652190111815641L;
	@Id
    private Integer id;
    	
	@Column(name="posttypeid")
	private Integer postTypeId;
	
	@Column(name="acceptedanswerid")
	private Integer acceptedAnswerId;
	
	@Column(name="parentid")
	private Integer parentId;
	
	@Column(name="creationdate")
	private Timestamp creationDate;
	
	@Column(name="score")
	private Integer score;
	
	
	@Column(name="viewcount")
	private Integer viewCount;
	
	@Column(name="body")
    private String body;
	
	@Column(name="owneruserid")
	private Integer ownerUserId;
	
	
	@Column(name="lasteditoruserid")
	private Integer lastEditorUserId;
	
	@Column(name="lasteditordisplayname")
    private String lastEditorDisplayName;
	
	@Column(name="lasteditdate")
	private Timestamp lastEditDate;
	
	@Column(name="lastactivitydate")
	private Timestamp lastActivityDate;
	
	@Column(name="title")
    private String title;
	
	@Column(name="tags")
    private String tags;
	
	@Column(name="answercount")
	private Integer answerCount;
	
	@Column(name="commentcount")
	private Integer commentCount;
	
	@Column(name="favoritecount")
	private Integer favoriteCount;
	
	@Column(name="closeddate")
	private Timestamp closedDate;
	
	@Column(name="communityowneddate")
	private Timestamp communityOwnedDate;
	
	@Column(name="tagssyn")
	private String tagsSyn;
		
	private String code;
	
	@Transient
	private ArrayList<Post> topKrelatedQuestions;  //lista de ids de posts com maior similaridade ( maior para menor )
	
	
	public Post() {
	}
	
	public Post(Integer id) {
		this.id = id;
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
		Post other = (Post) obj;
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

	public Integer getPostTypeId() {
		return postTypeId;
	}

	public void setPostTypeId(Integer postTypeId) {
		this.postTypeId = postTypeId;
	}

	public Integer getAcceptedAnswerId() {
		return acceptedAnswerId;
	}

	public void setAcceptedAnswerId(Integer acceptedAnswerId) {
		this.acceptedAnswerId = acceptedAnswerId;
	}

	public Integer getParentId() {
		return parentId;
	}

	public void setParentId(Integer parentId) {
		this.parentId = parentId;
	}

	public Timestamp getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(Timestamp creationDate) {
		this.creationDate = creationDate;
	}

	public Integer getScore() {
		return score;
	}

	public void setScore(Integer score) {
		this.score = score;
	}

	public Integer getViewCount() {
		return viewCount;
	}

	public void setViewCount(Integer viewCount) {
		this.viewCount = viewCount;
	}

	public String getBody() {
		return body;
	}

	public void setBody(String body) {
		this.body = body;
	}

	public Integer getOwnerUserId() {
		return ownerUserId;
	}

	public void setOwnerUserId(Integer ownerUserId) {
		this.ownerUserId = ownerUserId;
	}

	public Integer getLastEditorUserId() {
		return lastEditorUserId;
	}

	public void setLastEditorUserId(Integer lastEditorUserId) {
		this.lastEditorUserId = lastEditorUserId;
	}

	public String getLastEditorDisplayName() {
		return lastEditorDisplayName;
	}

	public void setLastEditorDisplayName(String lastEditorDisplayName) {
		this.lastEditorDisplayName = lastEditorDisplayName;
	}

	public Timestamp getLastEditDate() {
		return lastEditDate;
	}

	public void setLastEditDate(Timestamp lastEditDate) {
		this.lastEditDate = lastEditDate;
	}

	public Timestamp getLastActivityDate() {
		return lastActivityDate;
	}

	public void setLastActivityDate(Timestamp lastActivityDate) {
		this.lastActivityDate = lastActivityDate;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getTags() {
		return tags;
	}

	public void setTags(String tags) {
		this.tags = tags;
	}

	public Integer getAnswerCount() {
		return answerCount;
	}

	public void setAnswerCount(Integer answerCount) {
		this.answerCount = answerCount;
	}

	public Integer getCommentCount() {
		return commentCount;
	}

	public void setCommentCount(Integer commentCount) {
		this.commentCount = commentCount;
	}

	public Integer getFavoriteCount() {
		return favoriteCount;
	}

	public void setFavoriteCount(Integer favoriteCount) {
		this.favoriteCount = favoriteCount;
	}

	public Timestamp getClosedDate() {
		return closedDate;
	}

	public void setClosedDate(Timestamp closedDate) {
		this.closedDate = closedDate;
	}

	public Timestamp getCommunityOwnedDate() {
		return communityOwnedDate;
	}

	public void setCommunityOwnedDate(Timestamp communityOwnedDate) {
		this.communityOwnedDate = communityOwnedDate;
	}

	

	@Override
	public String toString() {
		return "Posts [id=" + id + ", parentId=" + parentId + ", body=" + body + ", title=" + title + ", tags=" + tags + "]";
	}

	public String getTagsSyn() {
		return tagsSyn;
	}

	public void setTagsSyn(String tagsSyn) {
		this.tagsSyn = tagsSyn;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	

	

	
	
	

	
	
    
}