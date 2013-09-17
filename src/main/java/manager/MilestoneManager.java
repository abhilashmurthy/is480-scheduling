/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package manager;

import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import javax.persistence.PersistenceException;
import javax.persistence.Query;
import model.Milestone;
import model.Term;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.MiscUtil;

/**
 *
 * @author suresh
 */
public class MilestoneManager {

    private static Logger logger = LoggerFactory.getLogger(MilestoneManager.class);

    public static boolean save(EntityManager em, Milestone milestone, EntityTransaction transaction) {
        logger.trace("Creating new milestone: " + milestone.getName());
        try {
            transaction = em.getTransaction();
            transaction.begin();
            em.persist(milestone);
            transaction.commit();
            return true;
        } catch (PersistenceException ex) {
            //Rolling back data transactions
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }
            logger.error("Error making database call for Create Milestone: " + milestone + ", message: " + ex.getMessage());
            ex.printStackTrace();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            e.printStackTrace();
        }
        return false;
    }

    public static Milestone findByNameAndTerm(EntityManager em, String name, Term term) {
        logger.trace("Getting milestone by name");
        Milestone result = null;
        try {
            em.getTransaction().begin();
            Query q = em.createNativeQuery("select * from Milestone where name = :name and term_id = :term", Milestone.class)
                    .setParameter("name", name)
                    .setParameter("term", term);
            result = (Milestone) q.getSingleResult();
            em.getTransaction().commit();
        } catch (Exception e) {
            e.printStackTrace();
            em.getTransaction().rollback();
        }
        return result;
    }
    
    public static List<Milestone> findByTerm(EntityManager em, Term term) {
        logger.trace("Getting milestones by term: " + term);
        List<Milestone> result = null;
        try {
            em.getTransaction().begin();
            Query q = em.createNativeQuery("select * from Milestone where term_id = :term", Milestone.class)
                    .setParameter("term", term);
            result = (List<Milestone>) q.getResultList();
            em.getTransaction().commit();
        } catch (Exception e) {
            e.printStackTrace();
            em.getTransaction().rollback();
        }
        return result;
    }
}
