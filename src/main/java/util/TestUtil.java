/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package util;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import model.User;

/**
 *
 * @author suresh
 */
public class TestUtil {
	
	public static void main(String[] args) {
                String name = "temp";
                String username = "temptemp";
                System.out.println("Writing new User " + name + ", username: " + username);
                
                EntityManagerFactory emf = Persistence.createEntityManagerFactory("scheduler");
                EntityManager em = emf.createEntityManager();
                em.getTransaction().begin();

                User user = new User();
                user.setFullName("Hello Hello");
                user.setUsername("hihi");
                em.persist(user);
                System.out.println("Persisted User");
                em.getTransaction().commit();
	}
	
}
