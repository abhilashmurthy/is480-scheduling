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
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import model.Booking;
import model.Team;
import model.Term;
import model.User;
import org.hibernate.annotations.Cascade;

/**
 *
 * @author suresh
 */
@Entity
public class Student extends User implements Serializable {
	
	protected Student() {}
	
	public Student(String username, String fullName, Term term) {
		super(username, fullName, Role.STUDENT, term);
	}
	
	private static final long serialVersionUID = 1L;

	@ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@Cascade(org.hibernate.annotations.CascadeType.ALL)
	private Team team;
	@ManyToMany(mappedBy = "requiredAttendees", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
//	@ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
//	@Cascade(org.hibernate.annotations.CascadeType.ALL)
//	@JoinTable(name="Required_Attendees",
//            joinColumns=@JoinColumn(name="user_id"),
//            inverseJoinColumns=@JoinColumn(name="booking_id"))
	private Set<Booking> requiredBookings = new HashSet<Booking>();

	public Team getTeam() {
		return team;
	}

	public void setTeam(Team team) {
		this.team = team;
	}

	public Set<Booking> getRequiredBookings() {
		return requiredBookings;
	}

	public void setRequiredBookings(Set<Booking> requiredBookings) {
		this.requiredBookings = requiredBookings;
	}
	
	@Override
	public String toString() {
		return "model.role.Student[ id=" + super.getId() + " ]";
	}
	
}
