/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package manager;

import javax.persistence.EntityManager;
import javax.persistence.Persistence;
import javax.persistence.Query;
import model.Milestone;
import model.Schedule;
import model.Term;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author suresh
 */
public class ScheduleManager {
	private static EntityManager em = Persistence.createEntityManagerFactory("scheduler").createEntityManager();
	private static Logger logger = LoggerFactory.getLogger(ScheduleManager.class);

	public static Schedule findByTermAndMilestone(Term term, Milestone milestone) {
		logger.info("Getting schedule by term and milestone");
		Schedule result = null;
		try {
			em.getTransaction().begin();
			Query q = em.createQuery("select o from Schedule where o.term = :term"
					+ " and o.milestone = :milestone");
			q.setParameter("term", term);
			q.setParameter("milestone", milestone);
			result = (Schedule) q.getSingleResult();
			em.getTransaction().commit();
		} catch (Exception e) {
			logger.error("Database Operation Error");
			em.getTransaction().rollback();
		}
		return result;
	}
}
