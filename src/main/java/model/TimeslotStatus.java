/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package model;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import util.Status;

/**
 *
 * @author Prakhar
 */
@Entity
@Table(name = "time_slot_status")
public class TimeslotStatus implements Serializable {
    private static final long serialVersionUID = 1L;
    
    @Id
    private TimeslotStatusPk id;
    @Column(name = "status", length = 50)
    private Status status;

    public TimeslotStatusPk getId() {
        return id;
    }

    public void setId(TimeslotStatusPk id) {
        this.id = id;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }
}
