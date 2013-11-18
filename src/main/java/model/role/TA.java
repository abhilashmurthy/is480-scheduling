/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package model.role;

import constant.Role;
import java.io.Serializable;
import java.util.List;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import model.Term;
import model.Timeslot;
import model.User;

/**
 *
 * @author suresh
 */
@Entity
public class TA extends User implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	//Constructors
	public TA() {
		
	}
	
	public TA(String username, String fullName, String mobileNumber, Term term) {
		super(username, fullName, mobileNumber, Role.TA, term);
	}
	
	@OneToMany(mappedBy = "TA")
	private List<Timeslot> chosenTimeslots;

	public List<Timeslot> getChosenTimeslots() {
		return chosenTimeslots;
	}

	public void setChosenTimeslots(List<Timeslot> chosenTimeslots) {
		this.chosenTimeslots = chosenTimeslots;
	}

	@Override
	public void setRole(Role role) {
		super.setRole(Role.TA);
	}
	
	@Override
	public String toString() {
		return "model.role.TA[ id=" + super.getId() + ", username=" + super.getUsername() + " ]";
	}
	
}
