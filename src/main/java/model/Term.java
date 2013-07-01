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

/**
 *
 * @author Prakhar
 */
@Entity
@Table(name="term")
public class Term implements Serializable {
    private static final long serialVersionUID = 1L;
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private BigInteger id;
    @Column(name = "year")
    private Date year;
    @Column(name = "term")
    private int term;
    
    public BigInteger getId() {
        return id;
    }

    public void setId(BigInteger id) {
        this.id = id;
    }

    public Date getYear() {
        return year;
    }
    
    public int getIntYear() {
        return year.getYear();
    }

    public void setYear(Date year) {
        this.year = year;
    }
    
    public void setYear(int year) {
        Date yearDate = new Date(year - 1900, 1, 1);
        this.year = yearDate;
    }

    public int getTerm() {
        return term;
    }

    public void setTerm(int term) {
        this.term = term;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += id.intValue();
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Term)) {
            return false;
        }
        Term other = (Term) object;
        if (this.id != other.id) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "model.Term[ id=" + id + " ]";
    }
    
}
