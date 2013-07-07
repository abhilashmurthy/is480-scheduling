/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package util;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
 
@Entity
@Table(name="user")
public class TestUser {
    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private int id;
     
    @Column
    private String name;
     
    @Column
    private String password;
     
    @Override
    public boolean equals(Object obj) {
        if(obj instanceof TestUser){
            TestUser user = (TestUser) obj;
            return user.getId() == this.getId();
        }
         
        return false;
    }
 
    public int getId() {
        return id;
    }
 
    public void setId(int id) {
        this.id = id;
    }
 
    public String getName() {
        return name;
    }
 
    public void setName(String name) {
        this.name = name;
    }
 
    public String getPassword() {
        return password;
    }
 
    public void setPassword(String password) {
        this.password = password;
    }
}