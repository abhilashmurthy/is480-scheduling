/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package model;

import java.io.Serializable;
import java.sql.Timestamp;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

/**
 *
 * @author suresh
 */
@Entity
public class CronLog implements Serializable {
	private static final long serialVersionUID = 1L;
	@Id
    @GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;
	
	private Timestamp runTime;
	
	private boolean success = false;

	private String jobName;

	private String message;

	public Timestamp getRunTime() {
		return runTime;
	}

	public void setRunTime(Timestamp runTime) {
		this.runTime = runTime;
	}

	public String getJobName() {
		return jobName;
	}

	public boolean isSuccess() {
		return success;
	}

	public void setSuccess(boolean success) {
		this.success = success;
	}
	
	public void setJobName(String jobName) {
		this.jobName = jobName;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
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
		if (!(object instanceof CronLog)) {
			return false;
		}
		CronLog other = (CronLog) object;
		if ((this.id == null && other.getId() != null) || (this.id != null && !this.id.equals(other.getId()))) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "model.CronLog[ id=" + id + " ]";
	}
	
}
