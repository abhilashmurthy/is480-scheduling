/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package model;

import java.io.Serializable;
import java.math.BigInteger;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import util.Milestone;

/**
 *
 * @author Prakhar
 */
@Embeddable
public class SchedulePk implements Serializable {
    private static final long serialVersionUID = 1L;
    
    @Column(name = "milestone")
	@Enumerated(EnumType.STRING)
    private Milestone milestone;
    @Column(name = "term_id")
    private BigInteger termId;

    public SchedulePk() {
    }
    
    public SchedulePk(Milestone milestone, BigInteger termId) {
        this.milestone = milestone;
        this.termId = termId;
    }
    
    public Milestone getMilestone() {
        return milestone;
    }

    public void setMilestone(Milestone milestone) {
        this.milestone = milestone;
    }

    public BigInteger getTermId() {
        return termId;
    }

    public void setTermId(BigInteger termId) {
        this.termId = termId;
    }
    
    @Override
    public int hashCode() {
        return (int) milestone.hashCode() + termId.intValue();
    }

    @Override
    public boolean equals(Object object) {
       if (object == this) return true;
        if (!(object instanceof SchedulePk)) return false;
        if (object == null) return false;
        SchedulePk pk = (SchedulePk) object;
        return pk.termId == termId && pk.milestone.equals(milestone);
    }
}