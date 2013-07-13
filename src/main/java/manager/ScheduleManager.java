/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package manager;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.List;
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
			Query q = em.createQuery("SELECT o FROM Schedule o WHERE o.term = :term"
					+ " AND o.milestone = :milestone", Schedule.class);
			q.setParameter("term", term);
			q.setParameter("milestone", milestone);
			result = (Schedule) q.getSingleResult();
			em.getTransaction().commit();
		} catch (Exception e) {
			logger.error(e.getMessage());
			em.getTransaction().rollback();
		}
		return result;
	}
	
	public static Schedule findActiveByTerm(Term term) {
		logger.info("Getting active schedule by term");
		Schedule result = null;
		try {
			em.getTransaction().begin();
			Query q = em.createQuery("SELECT o FROM Schedule o WHERE o.term = :term"
					+ " AND o.endDate > :today ORDER BY o.startDate", Schedule.class);
			q.setParameter("term", term);
			q.setParameter("today", new Timestamp(Calendar.getInstance().getTimeInMillis()));
			List<Schedule> resultList = (List<Schedule>) q.getResultList();
			if (!resultList.isEmpty())
				result = resultList.get(0);
			em.getTransaction().commit();
		} catch (Exception e) {
			logger.error(e.getMessage());
			em.getTransaction().rollback();
		}
		return result;
	}
	
	public static List<Schedule> findByTerm(Term term) {
		logger.info("Getting schedules by term");
		List<Schedule> result = null;
		try {
			em.getTransaction().begin();
			Query q = em.createQuery("SELECT o FROM Schedule o WHERE o.term = :term", Schedule.class);
			q.setParameter("term", term);
			result = (List<Schedule>) q.getResultList();
			em.getTransaction().commit();
		} catch (Exception e) {
			logger.error(e.getMessage());
			em.getTransaction().rollback();
		}
		return result;
	}
	
}
