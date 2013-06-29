/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package model;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 *
 * @author Prakhar
 */
@Entity
@Table(name = "TimeSlotStatus")
public class TimeslotStatus implements Serializable {
    private static final long serialVersionUID = 1L;
    
    @Id
    private TimeslotStatusPk id;
    @Column(name = "status")
    private int status;

    public TimeslotStatusPk getId() {
        return id;
    }

    public void setId(TimeslotStatusPk id) {
        this.id = id;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}
