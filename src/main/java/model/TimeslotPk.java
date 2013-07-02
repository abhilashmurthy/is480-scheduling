/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package model;

import java.io.Serializable;
import java.math.BigInteger;
import java.sql.Timestamp;
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
public class TimeslotPk implements Serializable {
    private static final long serialVersionUID = 1L;
    
    @Column(name = "term_id")
    private BigInteger termId;
    @Column(name = "startTime")
//    @Temporal(TemporalType.TIMESTAMP)
    private Timestamp startTime;
    @Column(name = "milestone")
	@Enumerated(EnumType.STRING)
    private Milestone milestone;

    public TimeslotPk() {
    }
    
    public TimeslotPk(BigInteger termId, Timestamp startDate, Milestone milestone) {
        this.termId = termId;
        this.startTime = startDate;
        this.milestone = milestone;
    }
    
    public BigInteger getTermId() {
        return termId;
    }

    public void setTermId(BigInteger termId) {
        this.termId = termId;
    }

    public Timestamp getStartTime() {
        return startTime;
    }

    public void setStartTime(Timestamp startTime) {
        this.startTime = startTime;
    }

    public Milestone getMilestone() {
        return milestone;
    }

    public void setMilestone(Milestone milestone) {
        this.milestone = milestone;
    }

    @Override
    public int hashCode() {
        return (int) milestone.hashCode() + (int) startTime.hashCode() + termId.intValue();
    }

    @Override
    public boolean equals(Object object) {
        if (object == this) return true;
        if (!(object instanceof TimeslotPk)) return false;
        if (object == null) return false;
        TimeslotPk pk = (TimeslotPk) object;
        return pk.termId == termId && pk.milestone.equals(milestone) && pk.startTime.equals(startTime);
    }
}
