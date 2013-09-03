/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package model.role;

import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
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
	
	
	@OneToMany(mappedBy = "TA")
	private List<Timeslot> chosenTimeslots;

	public List<Timeslot> getChosenTimeslots() {
		return chosenTimeslots;
	}

	public void setChosenTimeslots(List<Timeslot> chosenTimeslots) {
		this.chosenTimeslots = chosenTimeslots;
	}

	@Override
	public String toString() {
		return "model.role.TA[ id=" + super.getId() + " ]";
	}
	
}
