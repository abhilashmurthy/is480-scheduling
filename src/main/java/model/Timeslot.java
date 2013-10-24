/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package model;

import java.io.Serializable;
import java.sql.Timestamp;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import model.role.TA;

/**
 *
 * @author suresh
 */

@Entity
@Table(uniqueConstraints = {
		@UniqueConstraint(name = "Timeslot_unique_constraint", columnNames = {"schedule_id", "startTime"})})
public class Timeslot implements Serializable {
	
	private static final long serialVersionUID = 1L;
	@Id
    @GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;
	
	private Timestamp startTime;
	private Timestamp endTime;
	private String venue;
	
	@ManyToOne(fetch = FetchType.LAZY)
	private Schedule schedule;
	
	@OneToOne(fetch = FetchType.LAZY) //Stores the current active booking for the timeslot. If null, then the timeslot is available
	private Booking currentBooking;
	
	@ManyToOne
	private TA TA;

	public Timestamp getStartTime() {
		return startTime;
	}

	public void setStartTime(Timestamp startTime) {
		this.startTime = startTime;
	}

	public Timestamp getEndTime() {
		return endTime;
	}

	public void setEndTime(Timestamp endTime) {
		this.endTime = endTime;
	}

	public String getVenue() {
		return venue;
	}

	public void setVenue(String venue) {
		this.venue = venue;
	}

	public Schedule getSchedule() {
		return schedule;
	}

	public void setSchedule(Schedule schedule) {
		this.schedule = schedule;
	}
	
	public Booking getCurrentBooking() {
		return currentBooking;
	}

	public void setCurrentBooking(Booking currentBooking) {
		this.currentBooking = currentBooking;
	}
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public TA getTA() {
		return TA;
	}

	public void setTA(TA TA) {
		this.TA = TA;
	}

	@Override
	public int hashCode() {
		int hash = 0;
		hash += (id != null ? id.hashCode() : 0);
		return hash;
	}

	@Override
	public boolean equals(Object object) {
		if (!(object instanceof Timeslot)) {
			return false;
		}
		Timeslot other = (Timeslot) object;
		if ((this.id == null && other.getId() != null) || (this.id != null && !this.id.equals(other.getId()))) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "model.Timeslot[ id=" + id + " ]";
	}
	
}
