/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package model;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import org.hibernate.annotations.Cascade;

/**
 *
 * @author suresh
 */
@Entity
@Table(uniqueConstraints = {
		@UniqueConstraint(name = "Schedule_unique_constraint", columnNames = {"milestone_id"})})
public class Schedule implements Serializable {
	private static final long serialVersionUID = 1L;
	@Id
    @GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;
	
	@OneToOne(fetch = FetchType.LAZY)
	private Milestone milestone;
	
	private Timestamp startDate;
	private Timestamp endDate;
	private int dayStartTime;
	private int dayEndTime;
	
	@Cascade(org.hibernate.annotations.CascadeType.ALL)
	@OneToMany(mappedBy = "schedule", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	private Set<Timeslot> timeslots = new HashSet<Timeslot>();

	public Milestone getMilestone() {
		return milestone;
	}

	public void setMilestone(Milestone milestone) {
		this.milestone = milestone;
	}

	public Timestamp getStartDate() {
		return startDate;
	}

	public void setStartDate(Timestamp startDate) {
		this.startDate = startDate;
	}

	public Timestamp getEndDate() {
		return endDate;
	}

	public void setEndDate(Timestamp endDate) {
		this.endDate = endDate;
	}
	
	public int getDayStartTime() {
		return dayStartTime;
	}

	public void setDayStartTime(int dayStartTime) {
		this.dayStartTime = dayStartTime;
	}

	public int getDayEndTime() {
		return dayEndTime;
	}

	public void setDayEndTime(int datEndTime) {
		this.dayEndTime = datEndTime;
	}
	
	public Set<Timeslot> getTimeslots() {
		return timeslots;
	}

	public void setTimeslots(Set<Timeslot> timeslots) {
		this.timeslots = timeslots;
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
		if (!(object instanceof Schedule)) {
			return false;
		}
		Schedule other = (Schedule) object;
		if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "model.Schedule[ id=" + id + " ]";
	}
	
}
