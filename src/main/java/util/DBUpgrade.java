/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package util;

import java.util.List;
import javax.persistence.EntityManager;
import model.Settings;
import model.User;

/**
 *
 * @author suresh
 */
public class DBUpgrade {
	
	public static void main(String[] args) {
		EntityManager em = MiscUtil.getEntityManagerInstance();
		
		em.getTransaction().begin();
		
		List<User> users = em.createQuery("SELECT u from User u").getResultList();
		for (User u : users) {
			u.setEmail(u.getUsername() + "@smu.edu.sg");
		}
		
		Settings emailURL = new Settings();
		emailURL.setName("emailURL");
		emailURL.setValue("http://202.161.45.167/is480-scheduling/");
		
		em.persist(emailURL);
		
		em.getTransaction().commit();
	}
	
}
