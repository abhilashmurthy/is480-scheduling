/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package model;

import java.io.Serializable;
import java.math.BigInteger;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.Table;

/**
 *
 * @author Prakhar
 */
@Entity
@Table(name="team")
public class Team implements Serializable {
    private static final long serialVersionUID = 1L;
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private BigInteger id;
    
    @Column(name = "name")
    private String teamName;
    
    @OneToOne(mappedBy="term_id")
    private Term term;

	@OneToOne(mappedBy="reviewer1")
    private User reviewer1;

	@OneToOne(mappedBy="reviewer2")
    private User reviewer2;
    
    @OneToOne(mappedBy="supervisor")
    private User supervisor;
    
    public BigInteger getId() {
        return id;
    }
    
    public void setId(BigInteger id) {
        this.id = id;
    }

    public String getTeamName() {
        return teamName;
    }

    public void setTeamName(String teamName) {
        this.teamName = teamName;
    }
    
    public Term getTerm() {
		return term;
	}

	public void setTerm(Term term) {
		this.term = term;
	}

    public User getReviewer1() {
		return reviewer1;
	}

	public void setReviewer1(User reviewer1) {
		this.reviewer1 = reviewer1;
	}

	public User getReviewer2() {
		return reviewer2;
	}

	public void setReviewer2(User reviewer2) {
		this.reviewer2 = reviewer2;
	}

	public User getSupervisor() {
		return supervisor;
	}

	public void setSupervisor(User supervisor) {
		this.supervisor = supervisor;
	}
    
    @Override
    public int hashCode() {
        int hash = 0;
        hash += id.intValue();
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Team)) {
            return false;
        }
        Team other = (Team) object;
        if (this.id != other.id) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "model.Team[ id=" + id + " ]";
    }
    
}
