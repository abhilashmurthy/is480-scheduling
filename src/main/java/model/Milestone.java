/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package model;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

/**
 *
 * @author suresh
 */
@Entity
@Table(uniqueConstraints = {
	@UniqueConstraint(name = "Milestone_unique_constraint", columnNames = {"name", "term_id"})})
public class Milestone implements Serializable {
	private static final long serialVersionUID = 1L;
	
	@Id
    @GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;
	
	private String name;
	private int slotDuration;
	
	@ManyToOne(fetch = FetchType.LAZY)
	private Term term;
        
        public Milestone() {}
        
        public Milestone(String name, int slotDuration, Term term) {
            this.name = name;
            this.slotDuration = slotDuration;
            this.term = term;
        }

	public Term getTerm() {
		return term;
	}

	public void setTerm(Term term) {
		this.term = term;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getSlotDuration() {
		return slotDuration;
	}

	/**
	 * Method to set the duration of the slot.
	 * @param slotDuration Duration of slot in minutes
	 */
	public void setSlotDuration(int slotDuration) {
		this.slotDuration = slotDuration;
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
		if (!(object instanceof Milestone)) {
			return false;
		}
		Milestone other = (Milestone) object;
		if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "model.Milestone[ id=" + id + " ]";
	}
	
}
