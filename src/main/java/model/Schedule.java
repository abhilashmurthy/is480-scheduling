/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package model;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 *
 * @author Prakhar
 */
@Entity
@Table(name="schedule")
public class Schedule implements Serializable {
    private static final long serialVersionUID = 1L;
    
    @Id
    private SchedulePk id;
    @Column(name = "startDate")
    @Temporal(TemporalType.DATE)
    private Date startDate;
    @Column(name = "endDate")
    @Temporal(TemporalType.DATE)
    private Date endDate;
	@OneToMany
	@JoinColumns({
		@JoinColumn(name = "milestone", referencedColumnName = "milestone"),
		@JoinColumn(name = "term_id", referencedColumnName = "term_id")
	})
	private List<Timeslot> timeslots;

    public SchedulePk getId() {
        return id;
    }

    public void setId(SchedulePk id) {
        this.id = id;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }    
	
	public List<Timeslot> getTimeslots() {
		return timeslots;
	}

	public void setTimeslots(List<Timeslot> timeslots) {
		this.timeslots = timeslots;
	}
	
}
