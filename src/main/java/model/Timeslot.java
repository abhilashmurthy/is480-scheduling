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
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.ManyToOne;
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
	@ManyToOne
	@JoinColumns({
		@JoinColumn(name = "milestone", referencedColumnName = "milestone", insertable=false, updatable=false),
		@JoinColumn(name = "term_id", referencedColumnName = "term_id", insertable=false, updatable=false),
	})
	private Schedule schedule;

    public TimeslotPk getId() {
        return id;
    }

    public void setId(TimeslotPk id) {
        this.id = id;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public BigInteger getTeamId() {
        return teamId;
    }

    public void setTeamId(BigInteger teamId) {
        this.teamId = teamId;
    }

	public Schedule getSchedule() {
		return schedule;
	}

	public void setSchedule(Schedule schedule) {
		this.schedule = schedule;
	}

}
