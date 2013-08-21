/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.FetchType;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import org.hibernate.annotations.Cascade;

/**
 *
 * @author suresh
 */
@Entity
@Table(uniqueConstraints = {
	@UniqueConstraint(name = "uniquePerTerm", columnNames = {"username", "term_id"})})
public class User implements Serializable {

	private static final long serialVersionUID = 1L;
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;
	
	private String username;
	
	private String fullName;
	
	/*
	 * COMMON VARIABLES
	 */
	
	@ManyToOne(fetch = FetchType.EAGER) //Set to NULL for permanent roles
	private Term term;
	
	@ManyToMany(fetch = FetchType.LAZY)
	private List<Role> roles = new ArrayList<Role>();
	
	@ManyToMany(mappedBy = "requiredAttendees", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@Cascade(org.hibernate.annotations.CascadeType.ALL)
	private Set<Booking> requiredBookings = new HashSet<Booking>();
	
	@ManyToMany(mappedBy = "optionalAttendees", fetch = FetchType.LAZY)
	private Set<Booking> optionalBookings;
	
	/*
	 * STUDENT SPECIFIC VARIABLES
	 */
	
	@ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@Cascade(org.hibernate.annotations.CascadeType.ALL)
	private Team team;

	/*
	 * GETTERS, SETTERS AND OTHER METHODS
	 */
	
	public void addRole(Role role) {
		roles.add(role);
	}
	
	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getFullName() {
		return fullName;
	}

	public void setFullName(String fullName) {
		this.fullName = fullName;
	}

	public Term getTerm() {
		return term;
	}

	public void setTerm(Term term) {
		this.term = term;
	}
	
	public Team getTeam() {
		return team;
	}

	public void setTeam(Team team) {
		this.team = team;
	}
	
	public List<Role> getRoles() {
		return roles;
	}

	public void setRoles(List<Role> roles) {
		this.roles = roles;
	}

	public Set<Booking> getRequiredBookings() {
		return requiredBookings;
	}

	public void setRequiredBookings(Set<Booking> RequiredBookings) {
		this.requiredBookings = requiredBookings;
	}

	public Set<Booking> getOptionalBookings() {
		return optionalBookings;
	}

	public void setOptionalBookings(Set<Booking> optionalBookings) {
		this.optionalBookings = optionalBookings;
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
		if (!(object instanceof User)) {
			return false;
		}
		User other = (User) object;
		if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "model.User[ id=" + id + " ]";
	}
}
