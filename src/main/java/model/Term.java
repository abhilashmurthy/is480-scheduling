/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package model;

import java.io.Serializable;
import java.util.Set;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

/**
 *
 * @author suresh
 */
@Entity
@Table(uniqueConstraints = @UniqueConstraint(name = "Timeslot_unique_constraint", columnNames = {"academicYear","semester"}))
public class Term implements Serializable {
	private static final long serialVersionUID = 1L;
	@Id
    @GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;
	
	private int academicYear;
	private String semester;
	private String displayName;

	@OneToMany(mappedBy = "term")
	private Set<Milestone> milestones;
	
	protected Term() {
		
	}
	
	public Term(int academicYear, String semester) {
		this.academicYear = academicYear;
		this.semester = semester;
		String endAcademicYear = String.valueOf(academicYear + 1);
		displayName = String.valueOf(academicYear) + "-"
				+ endAcademicYear.substring(2) + " " + semester;
	}

	public Set<Milestone> getMilestones() {
		return milestones;
	}

	public void setMilestones(Set<Milestone> milestones) {
		this.milestones = milestones;
	}

	public int getAcademicYear() {
		return academicYear;
	}

	public void setAcademicYear(int academicYear) {
		this.academicYear = academicYear;
		String endAcademicYear = String.valueOf(academicYear + 1);
		displayName = String.valueOf(academicYear) + "-"
				+ endAcademicYear.substring(2) + " " + semester;
	}

	public String getSemester() {
		return semester;
	}

	public void setSemester(String semester) {
		this.semester = semester;
		String endAcademicYear = String.valueOf(academicYear + 1);
		displayName = String.valueOf(academicYear) + "-"
				+ endAcademicYear.substring(2) + " " + semester;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
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
		if (!(object instanceof Term)) {
			return false;
		}
		Term other = (Term) object;
		if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "model.Term[ id=" + id + " ]";
	}
	
}
