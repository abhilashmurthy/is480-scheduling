/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package model;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 *
 * @author Prakhar
 */
@Embeddable
public class TimeslotPk implements Serializable {
    private static final long serialVersionUID = 1L;
    
    @Column(name = "term_id")
    private int termId;
    @Column(name = "startTime")
    @Temporal(TemporalType.DATE)
    private Date startTime;
    @Column(name = "milestone")
    private String milestone;

    public TimeslotPk() {
    }
    
    public TimeslotPk(int termId, Date startDate, String milestone) {
        this.termId = termId;
        this.startTime = startDate;
        this.milestone = milestone;
    }
    
    public int getTermId() {
        return termId;
    }

    public void setTermId(int termId) {
        this.termId = termId;
    }

    public Date getStartDate() {
        return startTime;
    }

    public void setStartDate(Date startDate) {
        this.startTime = startDate;
    }

    public String getMilestone() {
        return milestone;
    }

    public void setMilestone(String milestone) {
        this.milestone = milestone;
    }

    @Override
    public int hashCode() {
        return (int) milestone.hashCode() + (int) startTime.hashCode() + termId;
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
