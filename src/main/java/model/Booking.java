/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package model;

import constant.Status;
import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;

/**
 *
 * @author suresh
 */
@Entity
public class Booking implements Serializable {
	private static final long serialVersionUID = 1L;
	@Id
    @GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;
	
	@OneToOne(fetch = FetchType.EAGER)
	private Timeslot timeslot;
	
	@ManyToOne(fetch = FetchType.LAZY)
	private Team team;
	
	@Column(length=19000000)
	private HashMap<User, Status> statusList = new HashMap<User, Status>();
	
	@ManyToMany(fetch = FetchType.LAZY)
	private Set<User> attendees = new HashSet<User>();
	
	@ManyToMany
	private Set<User> optionalAttendees = new HashSet<User>();
	
	private Set<String> externalAttendees = new HashSet<String>();
	
	private boolean isDeleted = false;
	
	/**
	 * This method returns the overall status of the booking.
	 * @return
	 */
	public Status getOverallBookingStatus() {
		Collection<Status> values = statusList.values();
		if (values.size() > 0) {
			int counter = 0;
			for (Status s: statusList.values()) {
				if (s == Status.REJECTED) {
					// Reject the booking if any one person has rejected it
					return Status.REJECTED;
				} else if (s == Status.APPROVED) {
					counter++;
				}
			}

			// Check if everyone has approved the booking
			if (counter == values.size()) {
				return Status.APPROVED;
			}
			return Status.PENDING;
		}
		
		return Status.AVAILABLE;
	}

	public Timeslot getTimeslot() {
		return timeslot;
	}

	public void setTimeslot(Timeslot timeslot) {
		this.timeslot = timeslot;
	}

	public Team getTeam() {
		return team;
	}

	public void setTeam(Team team) {
		this.team = team;
	}

	public HashMap<User, Status> getStatusList() {
		return statusList;
	}

	public void setStatusList(HashMap<User, Status> statusList) {
		this.statusList = statusList;
	}

	public Set<User> getAttendees() {
		return attendees;
	}

	public void setAttendees(Set<User> attendees) {
		this.attendees = attendees;
	}

	public Set<User> getOptionalAttendees() {
		return optionalAttendees;
	}

	public void setOptionalAttendees(Set<User> optionalAttendees) {
		this.optionalAttendees = optionalAttendees;
	}

	public Set<String> getExternalAttendees() {
		return externalAttendees;
	}

	public void setExternalAttendees(Set<String> externalAttendees) {
		this.externalAttendees = externalAttendees;
	}

	public boolean isIsDeleted() {
		return isDeleted;
	}

	public void setIsDeleted(boolean isDeleted) {
		this.isDeleted = isDeleted;
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
		// TODO: Warning - this method won't work in the case the id fields are not set
		if (!(object instanceof Booking)) {
			return false;
		}
		Booking other = (Booking) object;
		if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "model.Booking[ id=" + id + " ]";
	}
	
}
