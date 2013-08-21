/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package model;

import constant.BookingStatus;
import constant.Response;
import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
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
	
	//Store the overall status of the booking. Set to PENDING by default
	private BookingStatus status = BookingStatus.PENDING;
	
	@Column(length=19000000) //Track the responses of the required attendees
	private HashMap<User, Response> responseList = new HashMap<User, Response>();
	
	@ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	@JoinTable(name="Required_Attendees",
            joinColumns=@JoinColumn(name="booking_id"),
            inverseJoinColumns=@JoinColumn(name="user_id"))
	private Set<User> requiredAttendees = new HashSet<User>();
	
	@ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	@JoinTable(name="Optional_Attendees",
            joinColumns=@JoinColumn(name="booking_id"),
            inverseJoinColumns=@JoinColumn(name="user_id"))
	private Set<User> optionalAttendees = new HashSet<User>();
	
	@Column(length=19000000)
	private HashSet<String> externalAttendees = new HashSet<String>();

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

	public HashMap<User, Response> getResponseList() {
		return responseList;
	}

	public void setResponseList(HashMap<User, Response> responseList) {
		this.responseList = responseList;
	}

	public Set<User> getRequiredAttendees() {
		return requiredAttendees;
	}

	public void setRequiredAttendees(Set<User> attendees) {
		this.requiredAttendees = attendees;
	}

	public Set<User> getOptionalAttendees() {
		return optionalAttendees;
	}

	public void setOptionalAttendees(Set<User> optionalAttendees) {
		this.optionalAttendees = optionalAttendees;
	}

	public BookingStatus getStatus() {
		return status;
	}

	public void setBookingStatus(BookingStatus status) {
		this.status = status;
	}

	public HashSet<String> getExternalAttendees() {
		return externalAttendees;
	}

	public void setExternalAttendees(HashSet<String> externalAttendees) {
		this.externalAttendees = externalAttendees;
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
