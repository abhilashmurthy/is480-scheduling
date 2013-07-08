/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package manager;

import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.Persistence;
import javax.persistence.Query;
import model.Milestone;
import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author suresh
 */
public class MilestoneManager {
	private static EntityManager em = Persistence.createEntityManagerFactory("scheduler").createEntityManager();
	private static Logger logger = LoggerFactory.getLogger(MilestoneManager.class);
	
	public static Milestone findByName (String name) {
		logger.info("Getting milestone by name");
		Milestone result = null;
		try {
			em.getTransaction().begin();
			Query q = em.createNativeQuery("select * from Milestone where name = :name", Milestone.class);
			q.setParameter("name", name);
			result = (Milestone) q.getSingleResult();
			em.getTransaction().commit();	
		} catch (Exception e) {
			e.printStackTrace();
			em.getTransaction().rollback();
		}
		return result;
	}
}
