/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package util;

import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import manager.UserManager;
import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author suresh
 */
public class TestUtil {
	
	public static void main(String[] args) {
				Logger logger = LoggerFactory.getLogger(TestUtil.class);
                String name = "temp";
                String username = "temptemp";
                System.out.println("Writing new User " + name + ", username: " + username);
                
                EntityManagerFactory emf = Persistence.createEntityManagerFactory("scheduler");
                EntityManager em = emf.createEntityManager();
                EntityTransaction t = em.getTransaction();
				
				try {
					t.begin();
					User user = new User();
					user.setFullName("Hello Hello");
					user.setUsername("hihi");
					em.persist(user);
					System.out.println("Persisted User");
					t.commit();	
				} catch (Exception e) {
					logger.error(e.getClass().getName());
					t.rollback();
				}
                
				
				List<User> users = UserManager.getAllUsers();
				System.out.println("Total number of users: " + users.size());
	}
	
}
