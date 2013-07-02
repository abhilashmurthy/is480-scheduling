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
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import util.Milestone;

/**
 *
 * @author Prakhar
 */
@Embeddable
public class TimeslotStatusPk implements Serializable {
    private static final long serialVersionUID = 1L;
    
    @Column(name = "term_id")
    private BigInteger termId;
    @Column(name = "milestone")
	@Enumerated(EnumType.STRING)
    private Milestone milestone;
    @Column(name = "startTime")
//    @Temporal(TemporalType.TIMESTAMP)
    private Timestamp startTime;
    @Column(name = "user_id")
    private BigInteger userId;

    public TimeslotStatusPk() {
    }
    
    public TimeslotStatusPk(BigInteger termId, Milestone milestone, Timestamp startTime, BigInteger userId) {
        this.termId = termId;
        this.milestone = milestone;
        this.startTime = startTime;
        this.userId = userId;
    }
    
    public BigInteger getTermId() {
        return termId;
    }

    public void setTermId(BigInteger termId) {
        this.termId = termId;
    }

    public Milestone getMilestone() {
        return milestone;
    }

    public void setMilestone(Milestone milestone) {
        this.milestone = milestone;
    }

    public Timestamp getStartTime() {
        return startTime;
    }

    public void setStartTime(Timestamp startTime) {
        this.startTime = startTime;
    }

    public BigInteger getUserId() {
        return userId;
    }

    public void setUserId(BigInteger userId) {
        this.userId = userId;
    }
    
    @Override
    public int hashCode() {
       return (int) milestone.hashCode() + (int) startTime.hashCode() + termId.intValue() + userId.intValue();
    }

    @Override
    public boolean equals(Object object) {
        if (object == this) return true;
        if (!(object instanceof TimeslotStatusPk)) return false;
        if (object == null) return false;
        TimeslotStatusPk pk = (TimeslotStatusPk) object;
        return pk.termId == termId && pk.milestone.equals(milestone) && pk.startTime.equals(startTime) && pk.userId == userId;
    }
}
