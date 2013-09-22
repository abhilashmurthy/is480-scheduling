/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package model;

import constant.BookingStatus;
import constant.Response;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;

/**
 *
 * @author suresh
 */
@Entity
public class Booking implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @OneToOne(fetch = FetchType.EAGER)
    private Timeslot timeslot;
    @ManyToOne(fetch = FetchType.LAZY)
    private Team team;
    private Timestamp createdAt;
    //Store the overall status of the booking. Set to PENDING by default
    private BookingStatus bookingStatus = BookingStatus.PENDING;
    @Column(length = 19000000) //Track the responses of the required attendees
    private HashMap<User, Response> responseList = new HashMap<User, Response>();
    @ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinTable(name = "Booking_Required_Attendees",
            joinColumns =
            @JoinColumn(name = "booking_id"),
            inverseJoinColumns =
            @JoinColumn(name = "user_id"))
    private Set<User> requiredAttendees = new HashSet<User>();
    @Column(length = 19000000)
    private HashSet<String> optionalAttendees = new HashSet<String>();
    private String rejectReason;
    private String lastEditedBy;
    private Timestamp lastEditedAt;

    public Timeslot getTimeslot() {
        return timeslot;
    }

    public void setTimeslot(Timeslot timeslot) {
        this.timeslot = timeslot;
    }

    public Team getTeam() {
        return team;
    }

    public void setTeam(Team team) {
        this.team = team;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }
    
    public Timestamp getLastEditedAt() {
        return lastEditedAt;
    }

    public void setLastEditedAt(Timestamp lastEditedAt) {
        this.lastEditedAt = lastEditedAt;
    }
    
    public String getLastEditedBy() {
        return lastEditedBy;
    }

    public void setLastEditedBy(String lastEditedBy) {
        this.lastEditedBy = lastEditedBy;
    }

    public HashMap<User, Response> getResponseList() {
        return responseList;
    }

    public void setResponseList(HashMap<User, Response> responseList) {
        this.responseList = responseList;
    }

    public Set<User> getRequiredAttendees() {
        return requiredAttendees;
    }

    public void setRequiredAttendees(Set<User> attendees) {
        this.requiredAttendees = attendees;
    }

    public String getRejectReason() {
        return rejectReason;
    }

    public void setRejectReason(String rejectReason) {
        this.rejectReason = rejectReason;
    }

    public BookingStatus getBookingStatus() {
        return bookingStatus;
    }

    public void setBookingStatus(BookingStatus bookingStatus) {
        this.bookingStatus = bookingStatus;
    }

    public HashSet<String> getOptionalAttendees() {
        return optionalAttendees;
    }

    public void setOptionalAttendees(HashSet<String> optionalAttendees) {
        this.optionalAttendees = optionalAttendees;
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
        if (!(object instanceof Booking)) {
            return false;
        }
        Booking other = (Booking) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "model.Booking[ id=" + id + " ]";
    }
}
