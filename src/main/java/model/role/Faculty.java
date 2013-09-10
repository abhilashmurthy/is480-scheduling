/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package model.role;

import constant.Role;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import model.Booking;
import model.Term;
import model.Timeslot;
import model.User;

/**
 *
 * @author suresh
 */
@Entity
public class Faculty extends User implements Serializable {
	
	protected Faculty() {}
	
	public Faculty(String username, String fullName, String mobileNumber, Term term) {
		super(username, fullName, mobileNumber, Role.FACULTY, term);
	}
	
	private static final long serialVersionUID = 1L;
	
	@ManyToMany(mappedBy = "requiredAttendees", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	private Set<Booking> requiredBookings = new HashSet<Booking>();
	@ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	@JoinTable(name="Unavailable_Timeslots",
            joinColumns=@JoinColumn(name="faculty_id"),
            inverseJoinColumns=@JoinColumn(name="timeslot_id"))
	private Set<Timeslot> unavailableTimeslots = new HashSet<Timeslot>();

        public Set<Timeslot> getUnavailableTimeslots() {
            return unavailableTimeslots;
        }

        public void setUnavailableTimeslots(Set<Timeslot> unavailableTimeslots) {
            this.unavailableTimeslots = unavailableTimeslots;
        }

	public Set<Booking> getRequiredBookings() {
		return requiredBookings;
	}

	public void setRequiredBookings(Set<Booking> requiredBookings) {
		this.requiredBookings = requiredBookings;
	}

	@Override
	public void setRole(Role role) {
		super.setRole(Role.FACULTY);
	}

	@Override
	public String toString() {
		return "model.role.Faculty[ id=" + super.getId() + " ]";
	}
	
}
