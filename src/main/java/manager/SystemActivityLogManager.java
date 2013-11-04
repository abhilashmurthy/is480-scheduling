/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package manager;

import java.util.Date;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import model.SystemActivityLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Prakhar
 */
public class SystemActivityLogManager {
	private static Logger logger = LoggerFactory.getLogger(SystemActivityLogManager.class);
	
	/* To get all the logs*/
	 public static List<SystemActivityLog> getAllLogs(EntityManager em) {
        logger.trace("Getting all logs");
        List<SystemActivityLog> result = null;
        try {
            em.getTransaction().begin();
            Query q = em.createQuery("select l from SystemActivityLog l");
            result = q.getResultList();
            em.getTransaction().commit();
        } catch (Exception e) {
            logger.error("Database Operation Error");
            em.getTransaction().rollback();
        }
        return result;
    }
	 
	/* To get all the logs between start time and end time*/
	public static List<SystemActivityLog> getAllLogsBetween(EntityManager em, Date startTime, Date endTime) {
	   logger.trace("Getting all logs between");
	   List<SystemActivityLog> result = null;
	   try {
		   em.getTransaction().begin();
		   Query q = em.createQuery("select l from SystemActivityLog l");
		   result = q.getResultList();
		   em.getTransaction().commit();
	   } catch (Exception e) {
		   logger.error("Database Operation Error");
		   em.getTransaction().rollback();
	   }
	   return result;
   }
}
