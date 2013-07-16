/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package manager;

import constant.Status;
import java.util.HashMap;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import javax.persistence.PersistenceException;
import javax.persistence.Query;
import model.Timeslot;
import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Prakhar
 */
public class TimeslotManager {

    private static EntityManager em = Persistence.createEntityManagerFactory("scheduler").createEntityManager();
    private static Logger logger = LoggerFactory.getLogger(UserManager.class);
    
    public static Timeslot findById(long id) {
        logger.info("Getting timeslot based on id.");
		Timeslot timeslot = null;
        try {
            em.getTransaction().begin();
            Query q = em.createQuery("select t from Timeslot t where t.id = :id")
                    .setParameter("id", id);
            timeslot = (Timeslot) q.getSingleResult();
            em.getTransaction().commit();
			return timeslot;
        } catch (Exception e) {
            logger.error("Database Operation Error");
            em.getTransaction().rollback();
        }
		return null;
    }
	
	public static boolean updateTimeslotStatus (Timeslot timeslot, EntityTransaction transaction) {
		logger.info("Updating timeslot status");
        try {
            transaction = em.getTransaction();
            transaction.begin();
			em.merge(timeslot);
            transaction.commit();
            return true;
        } catch (PersistenceException ex) {
            //Rolling back data transactions
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }
            logger.error("Error making database call for update timeslot status");
            ex.printStackTrace();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            e.printStackTrace();
        }
        return false;
	}
}
