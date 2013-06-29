/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package model;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

/**
 *
 * @author Prakhar
 */
@Embeddable
public class SchedulePk implements Serializable {
    private static final long serialVersionUID = 1L;
    
    @Column(name = "milestone")
    private String milestone;
    @Column(name = "term_id")
    private int termId;

    public SchedulePk() {
    }
    
    public SchedulePk(String milestone, int termId) {
        this.milestone = milestone;
        this.termId = termId;
    }
    
    public String getMilestone() {
        return milestone;
    }

    public void setMilestone(String milestone) {
        this.milestone = milestone;
    }

    public int getTermId() {
        return termId;
    }

    public void setTermId(int termId) {
        this.termId = termId;
    }
    
    @Override
    public int hashCode() {
        return (int) milestone.hashCode() + termId;
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