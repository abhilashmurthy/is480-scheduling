/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package manager;

import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.Persistence;
import javax.persistence.Query;
import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author suresh
 */
public class UserManager {
	
	private static EntityManager em = Persistence.createEntityManagerFactory("scheduler").createEntityManager();
	private static Logger logger = LoggerFactory.getLogger(UserManager.class);
	
	public static List<User> getAllUsers() {
		logger.info("Getting all users");
		List<User> result = null;
		try {
			em.getTransaction().begin();
			Query q = em.createNativeQuery("select * from User");
			result = q.getResultList();
			em.getTransaction().commit();	
		} catch (Exception e) {
			logger.error("Database Operation Error");
			em.getTransaction().rollback();
		}
		return result;
	}
	
}
