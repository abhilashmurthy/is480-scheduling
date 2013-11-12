/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package manager;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TemporalType;
import model.Schedule;
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
	public static List<SystemActivityLog> getAllLogsBetween(EntityManager em, Timestamp startDate, Timestamp endDate) {
	   logger.trace("Getting all logs between " + startDate.toString() + " and " + endDate.toString());
	   List<SystemActivityLog> result = null;
	   try {
		   em.getTransaction().begin();
		   Query q = em.createQuery("select l from SystemActivityLog l where l.runTime >= :startDate and l.runTime <= :endDate", SystemActivityLog.class)
                    .setParameter("startDate", startDate)
                    .setParameter("endDate", endDate);
		   result = q.getResultList();
		   em.getTransaction().commit();
	   } catch (Exception e) {
		   logger.error("Database Operation Error");
		   em.getTransaction().rollback();
	   }
	   return result;
   }
}
