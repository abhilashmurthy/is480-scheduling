/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package model;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 *
 * @author Prakhar
 */
@Entity
@Table(name="time_slot")
public class Timeslot implements Serializable {
    private static final long serialVersionUID = 1L;
    
    @Id
    private TimeslotPk id;
    @Column(name = "endTime")
    @Temporal(TemporalType.DATE)
    private Date endTime;
    @Column(name = "team_id")
    private BigInteger teamId;

    public TimeslotPk getId() {
        return id;
    }

    public void setId(TimeslotPk id) {
        this.id = id;
    }

    public Date getEndDate() {
        return endTime;
    }

    public void setEndDate(Date endDate) {
        this.endTime = endDate;
    }

    public BigInteger getTeamId() {
        return teamId;
    }

    public void setTeamId(BigInteger teamId) {
        this.teamId = teamId;
    }
}
