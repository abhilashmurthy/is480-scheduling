/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package model;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import constant.Status;
import java.util.Collection;
import javax.persistence.Column;
import javax.persistence.ManyToOne;

/**
 *
 * @author suresh
 */
@Entity
public class Timeslot implements Serializable {
	
	private static final long serialVersionUID = 1L;
	@Id
    @GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;
	private Timestamp startTime;
	private Timestamp endTime;
	@Column(length=19000000)
	private HashMap<User, Status> statusList = new HashMap<User, Status>();
	private String venue;
	@ManyToMany
	private Set<User> attendees = new HashSet<User>();
	@ManyToOne
	private Team team;
	@ManyToOne
	private Schedule schedule;

	/**
	 * This method returns the overall status of the booking.
	 * @return
	 */
	public Status getOverallBookingStatus() {
		int counter = 0;
		Collection<Status> values = statusList.values();
		for (Status s: statusList.values()) {
			if (s == Status.REJECTED) {
				// Reject the booking if any one person has rejected it
				return Status.REJECTED;
			} else if (s == Status.ACCEPTED) {
				counter++;
			}
		}
		
		// Check if everyone has approved the booking
		if (counter == values.size()) {
			return Status.ACCEPTED;
		}
		return Status.PENDING;
	}
	
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

	public HashMap<User, Status> getStatusList() {
		return statusList;
	}

	public void setStatusList(HashMap<User, Status> statusList) {
		this.statusList = statusList;
	}

	public String getVenue() {
		return venue;
	}

	public void setVenue(String venue) {
		this.venue = venue;
	}

	public Set<User> getAttendees() {
		return attendees;
	}

	public void setAttendees(Set<User> attendees) {
		this.attendees = attendees;
	}

	public Team getTeam() {
		return team;
	}

	public void setTeam(Team team) {
		this.team = team;
	}

	public Schedule getSchedule() {
		return schedule;
	}

	public void setSchedule(Schedule schedule) {
		this.schedule = schedule;
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
		if (!(object instanceof Timeslot)) {
			return false;
		}
		Timeslot other = (Timeslot) object;
		if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "model.Timeslot[ id=" + id + " ]";
	}
	
}
