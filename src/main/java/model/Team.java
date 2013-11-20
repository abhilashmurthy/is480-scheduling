/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package model;

import constant.PresentationType;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import model.role.Faculty;
import model.role.Student;

/**
 *
 * @author suresh
 */
@Entity
@Table(uniqueConstraints = {
		@UniqueConstraint(name = "Team_unique_constraint", columnNames = {"teamName", "term_id"})})
public class Team implements Serializable {
	private static final long serialVersionUID = 1L;
	@Id
    @GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;
	private String teamName;
	private String wiki;
	//Store the type of presentation
    private PresentationType presentationType = PresentationType.PRIVATE;
	@ManyToOne(fetch = FetchType.LAZY)
	private Term term;
	@ManyToOne(fetch = FetchType.LAZY)
	private Faculty supervisor;
	@ManyToOne(fetch = FetchType.LAZY)
	private Faculty reviewer1;
	@ManyToOne(fetch = FetchType.LAZY)
	private Faculty reviewer2;
	@OneToMany(mappedBy = "team")
	private Set<Student> members = new HashSet<Student>();

	public String getTeamName() {
		return teamName;
	}

	public void setTeamName(String teamName) {
		this.teamName = teamName;
	}

	public String getWiki() {
		return wiki;
	}

	public void setWiki(String wiki) {
		this.wiki = wiki;
	}

	public Term getTerm() {
		return term;
	}

	public void setTerm(Term term) {
		this.term = term;
	}

	public Faculty getSupervisor() {
		return supervisor;
	}

	public void setSupervisor(Faculty supervisor) {
		this.supervisor = supervisor;
	}

	public Faculty getReviewer1() {
		return reviewer1;
	}

	public void setReviewer1(Faculty reviewer1) {
		this.reviewer1 = reviewer1;
	}

	public Faculty getReviewer2() {
		return reviewer2;
	}

	public void setReviewer2(Faculty reviewer2) {
		this.reviewer2 = reviewer2;
	}

	public Set<Student> getMembers() {
		return members;
	}

	public void setMembers(Set<Student> members) {
		this.members = members;
	}

	public PresentationType getPresentationType() {
		return presentationType;
	}

	public void setPresentationType(PresentationType presentationType) {
		this.presentationType = presentationType;
	}
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@Override
	public int hashCode() {
		int hash = 0;
		hash += (id != null ? id.hashCode() : 0);
		return hash;
	}

	@Override
	public boolean equals(Object object) {
		if (!(object instanceof Team)) {
			return false;
		}
		Team other = (Team) object;
		if ((this.id == null && other.getId() != null) || (this.id != null && !this.id.equals(other.getId()))) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "model.Team[ id=" + id + ", name=" + teamName + " ]";
	}
	
}
