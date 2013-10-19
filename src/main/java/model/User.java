/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package model;

import constant.Role;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.CascadeType;
import javax.persistence.FetchType;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

/**
 *
 * @author suresh
 */
@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@Table(uniqueConstraints = {
	@UniqueConstraint(name = "User_unique_constraint", columnNames = {"username", "role", "term_id"})})
public class User implements Serializable {
	
	protected User() {}
	
	public User(String username, String fullName, String mobileNumber, Role role, Term term) {
		this.username = username;
		this.fullName = fullName;
		this.mobileNumber = mobileNumber;
		this.role = role;
		if (term != null) this.term = term;
	}

	private static final long serialVersionUID = 1L;
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;
	
	private String username;
	
	private String fullName;
	
	private String mobileNumber;
	
	@ManyToOne(fetch = FetchType.EAGER) //Set to NULL for permanent roles
	private Term term;
	
	private Role role;
	
	@ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	private Set<Booking> subscribedBookings = new HashSet<Booking>();
	
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
	
	public Role getRole() {
		return role;
	}

	public void setRole(Role role) {
		this.role = role;
	}

	public String getMobileNumber() {
		return mobileNumber;
	}

	public void setMobileNumber(String mobileNumber) {
		this.mobileNumber = mobileNumber;
	}

	public Set<Booking> getSubscribedBookings() {
		return subscribedBookings;
	}

	public void setSubscribedBookings(Set<Booking> subscribedBookings) {
		this.subscribedBookings = subscribedBookings;
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
