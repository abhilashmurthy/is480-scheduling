/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package manager;

import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.Persistence;
import javax.persistence.Query;
import javax.persistence.EntityTransaction;
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

    private static EntityManager em = Persistence.createEntityManagerFactory(MiscUtil.PERSISTENCE_UNIT).createEntityManager();
    private static Logger logger = LoggerFactory.getLogger(TermManager.class);

    public static boolean save(Term term, EntityTransaction transaction) {
        logger.info("Creating new term");
        try {
            transaction = em.getTransaction();
            transaction.begin();
            if (term != null) {
                em.persist(term);
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

    public static List<Term> getAllTerms() {
        logger.info("Getting all term objects");
        List<Term> sourceList = null;
        EntityTransaction transaction = em.getTransaction();
        try {
            transaction.begin();
            Query q = em.createQuery("Select t from Term t");
            sourceList = q.getResultList();
            transaction.commit();
        } catch (Exception e) {
            logger.error("Database Operation Error");
            //em.getTransaction().rollback();
        }
        return sourceList;
    }

    public static Term findByYearAndSemester(int year, String semester) {
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
	
	public static Term findTermById (long id) {
		logger.info("Getting term by id");
		Term term = null;
		try {
			em.getTransaction().begin();
			Query q = em.createQuery("SELECT t FROM Term t WHERE t.id = :id", Term.class);
			q.setParameter("id", id);
			term = (Term) q.getSingleResult();
			em.getTransaction().commit();
			return term;
		} catch (Exception e) {
			logger.error(e.getMessage());
			em.getTransaction().rollback();
		}
		return null;
	}
	
}
