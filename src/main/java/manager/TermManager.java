/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package manager;

import javax.persistence.EntityManager;
import javax.persistence.Persistence;
import javax.persistence.Query;
import model.Term;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author suresh
 */
public class TermManager {
	private static EntityManager em = Persistence.createEntityManagerFactory("scheduler").createEntityManager();
	private static Logger logger = LoggerFactory.getLogger(TermManager.class);
	
	public static Term findByYearAndSemester (int year, int semester) {
		logger.info("Getting term by year and semester");
		Term result = null;
		try {
			em.getTransaction().begin();
			Query q = em.createNativeQuery("select * from Term where academicYear = :year "
					+ "and semester = :semester", Term.class);
			q.setParameter("year", year);
			q.setParameter("semester", semester);
			result = (Term) q.getSingleResult();
			em.getTransaction().commit();	
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Database Operation Error");
			em.getTransaction().rollback();
		}
		return result;
	}
}
