/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package util;

import java.util.List;
import javax.persistence.EntityManager;
import model.User;

/**
 *
 * @author suresh
 */
public class UserEmailSetupScript {
	
	public static void main(String[] args) {
		EntityManager em = MiscUtil.getEntityManagerInstance();
		
		em.getTransaction().begin();
		
		List<User> users = em.createQuery("SELECT u from User u").getResultList();
		for (User u : users) {
			u.setEmail(u.getUsername() + "@smu.edu.sg");
		}
		
		em.getTransaction().commit();
	}
	
}
