/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package manager;

import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.EntityTransaction;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceException;
import model.Term;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.MiscUtil;

/**
 *
 * @author suresh
 */
public class TermManager {

    private static Logger logger = LoggerFactory.getLogger(TermManager.class);
    
    public static boolean update(EntityManager em, Term term, EntityTransaction transaction) {
        logger.trace("Updating term: " + term);
        try {
            transaction = em.getTransaction();
            transaction.begin();
            if (term != null) {
                em.merge(term);
            }
            transaction.commit();
            return true;
        } catch (PersistenceException ex) {
            //Rolling back data transactions
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }
            logger.error("Error making database call for Create Term Details");
            ex.printStackTrace();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            e.printStackTrace();
        }
        return false;
    }

    public static List<Term> getAllTerms(EntityManager em) {
        logger.trace("Getting all term objects");
        List<Term> sourceList = null;
        EntityTransaction transaction = em.getTransaction();
        try {
            transaction.begin();
            Query q = em.createQuery("Select t from Term t");
            sourceList = q.getResultList();
            transaction.commit();
        } catch (Exception e) {
            logger.error("Database Operation Error");
            if (MiscUtil.DEV_MODE) {
                for (StackTraceElement s : e.getStackTrace()) {
                    logger.debug(s.toString());
                }
            }
            //em.getTransaction().rollback();
        }
        return sourceList;
    }

    public static Term findByYearAndSemester(EntityManager em, int year, String semester) {
        logger.trace("Getting term by year and semester");
        Term result = null;
        try {
            Query q = em.createNativeQuery("select * from Term where academicYear = :year "
                    + "and semester = :semester", Term.class);
            q.setParameter("year", year);
            q.setParameter("semester", semester);
            result = (Term) q.getSingleResult();
        } catch (NoResultException e) {
			return null;
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Database Operation Error");
            if (MiscUtil.DEV_MODE) {
                for (StackTraceElement s : e.getStackTrace()) {
                    logger.debug(s.toString());
                }
            }
        }
        return result;
    }
	
	public static Term findTermById (EntityManager em, long id) {
		logger.trace("Getting term by id");
		Term term = null;
		try {
			Query q = em.createQuery("SELECT t FROM Term t WHERE t.id = :id", Term.class);
			q.setParameter("id", id);
			term = (Term) q.getSingleResult();
			return term;
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
		return null;
	}
	
	public static Term getTermByDisplayName (EntityManager em, String displayName) {
		logger.trace("Getting term by display name");
		Term term = null;
		try {
			Query q = em.createQuery("SELECT t FROM Term t WHERE t.displayName = :displayName", Term.class);
			q.setParameter("displayName", displayName);
			term = (Term) q.getSingleResult();
			return term;
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
		return null;
	}
	
}
