/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package manager;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import javax.persistence.PersistenceException;
import javax.persistence.Query;
import javax.persistence.TemporalType;
import model.Milestone;
import model.Schedule;
import model.Term;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.MiscUtil;

/**
 *
 * @author suresh
 */
public class ScheduleManager {

    private static Logger logger = LoggerFactory.getLogger(ScheduleManager.class);

    public static Schedule findByTermAndMilestone(EntityManager em, Term term, Milestone milestone) {
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

    public static Schedule findActiveByTerm(EntityManager em, Term term) {
        logger.info("Getting active schedule by term");
        Schedule result = null;
        try {
            em.getTransaction().begin();
            Query q = em.createQuery("SELECT o FROM Schedule o WHERE o.term = :term"
                    + " AND o.endDate > :today ORDER BY o.startDate", Schedule.class);
            q.setParameter("term", term);
            q.setParameter("today", new Timestamp(Calendar.getInstance().getTimeInMillis()));
            List<Schedule> resultList = (List<Schedule>) q.getResultList();
            if (!resultList.isEmpty()) {
                result = resultList.get(0);
            }
            em.getTransaction().commit();
        } catch (Exception e) {
            logger.error(e.getMessage());
            em.getTransaction().rollback();
        }
        return result;
    }

    public static List<Schedule> findByTerm(EntityManager em, Term term) {
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

    public static boolean save(EntityManager em, List<Schedule> scheduleList, EntityTransaction transaction) {
        logger.info("Creating new schedule");
        try {
            transaction = em.getTransaction();
            transaction.begin();
            for (Schedule schedule : scheduleList) {
                em.persist(schedule);
            }
            transaction.commit();
            return true;
        } catch (PersistenceException ex) {
            //Rolling back data transactions
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }
            logger.error("Error making database call for Create Schedule");
            ex.printStackTrace();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            e.printStackTrace();
        }
        return false;
    }
    
    public static boolean update(EntityManager em, Schedule schedule, EntityTransaction transaction) {
        logger.info("Creating new schedule");
        try {
            transaction = em.getTransaction();
            transaction.begin();
            em.merge(schedule);
            transaction.commit();
            return true;
        } catch (PersistenceException ex) {
            //Rolling back data transactions
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }
            logger.error("Error making database call for Create Schedule");
            ex.printStackTrace();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            e.printStackTrace();
        }
        return false;
    }

    public static List<Schedule> getAllSchedules(EntityManager em) {
        logger.info("Getting all schedule objects");
        List<Schedule> sourceList = null;
        EntityTransaction transaction = em.getTransaction();
        try {
            transaction.begin();
            Query q = em.createQuery("Select s from Schedule s");
            sourceList = q.getResultList();
            transaction.commit();
            return sourceList;
        } catch (Exception e) {
            logger.error("Database Operation Error");
            e.printStackTrace();
        }
        return null;
    }
    
    public static Schedule findByWindow(EntityManager em, Timestamp startDate, Timestamp endDate) {
        logger.info("Getting schedule by window: start[" + startDate + "], end[" + endDate + "]");
        Schedule schedule = null;
        EntityTransaction transaction = em.getTransaction();
        try {
            transaction.begin();
            Query q = em.createQuery("select o from Schedule o where o.startDate = :startDate and o.endDate = :endDate", Schedule.class)
                    .setParameter("startDate", startDate, TemporalType.TIMESTAMP)
                    .setParameter("endDate", endDate, TemporalType.TIMESTAMP);
            schedule = (Schedule) q.getSingleResult();
            transaction.commit();
        } catch (Exception e) {
            logger.error("Database Operation Error");
            e.printStackTrace();
        }
        return schedule;
    }
    
    public static Schedule findById(EntityManager em, long id) {
        logger.info("Getting schedule by id: " + id);
        Schedule schedule = null;
        EntityTransaction transaction = em.getTransaction();
        try {
            transaction.begin();
            Query q = em.createQuery("select o from Schedule o where o.id = :id", Schedule.class)
                    .setParameter("id", id);
            schedule = (Schedule) q.getSingleResult();
            transaction.commit();
        } catch (Exception e) {
            logger.error("Database Operation Error");
            e.printStackTrace();
        }
        return schedule;
    }
}
