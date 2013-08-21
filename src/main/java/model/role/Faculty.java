/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package model.role;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import model.Booking;
import model.User;

/**
 *
 * @author suresh
 */
@Entity
public class Faculty extends User implements Serializable {
	private static final long serialVersionUID = 1L;
	
	@ManyToMany(mappedBy = "requiredAttendees", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	private Set<Booking> requiredBookings = new HashSet<Booking>();

	public Set<Booking> getRequiredBookings() {
		return requiredBookings;
	}

	public void setRequiredBookings(Set<Booking> requiredBookings) {
		this.requiredBookings = requiredBookings;
	}

	@Override
	public String toString() {
		return "model.role.Faculty[ id=" + super.getId() + " ]";
	}
	
}
