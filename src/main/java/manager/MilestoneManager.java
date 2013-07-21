/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package manager;

import javax.persistence.EntityManager;
import javax.persistence.Persistence;
import javax.persistence.Query;
import model.Milestone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.MiscUtil;

/**
 *
 * @author suresh
 */
public class MilestoneManager {
	private static EntityManager em = Persistence.createEntityManagerFactory(MiscUtil.PERSISTENCE_UNIT).createEntityManager();
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
