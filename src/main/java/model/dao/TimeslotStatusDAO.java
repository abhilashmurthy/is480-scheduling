/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package model.dao;

import java.math.BigInteger;
import java.sql.Timestamp;
import model.TimeslotStatus;
import org.hibernate.Query;
import org.hibernate.Session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.HibernateUtil;

/**
 *
 * @author Prakhar
 */
public class TimeslotStatusDAO {
    static final Logger logger = LoggerFactory.getLogger(TimeslotStatusDAO.class);
    static Session session = HibernateUtil.getSession();
    
	public static void save(TimeslotStatus timeslotStatus) {
        session.beginTransaction();
        session.saveOrUpdate(timeslotStatus);
        session.getTransaction().commit();
        logger.info("Added timeslotStatus TermId:" + timeslotStatus.getId().getTermId()
				+ " Milestone: " + timeslotStatus.getId().getMilestone()
				+ " Start Time: " + timeslotStatus.getId().getStartTime()
				+ " User ID: " + timeslotStatus.getId().getUserId());
    }

    public static void update(TimeslotStatus timeslotStatus) {
        session.beginTransaction();
        session.update(timeslotStatus);
        session.getTransaction().commit();
        logger.info("Updated timeslotStatus TermId:" + timeslotStatus.getId().getTermId()
				+ " Milestone: " + timeslotStatus.getId().getMilestone()
				+ " Start Time: " + timeslotStatus.getId().getStartTime()
				+ " User ID: " + timeslotStatus.getId().getUserId());
    }

    public static void delete(TimeslotStatus timeslotStatus) {
        session.beginTransaction();
        session.delete(timeslotStatus);
        session.getTransaction().commit();
        logger.info("Deleted timeslotStatus TermId:" + timeslotStatus.getId().getTermId()
				+ " Milestone: " + timeslotStatus.getId().getMilestone()
				+ " Start Time: " + timeslotStatus.getId().getStartTime()
				+ " User ID: " + timeslotStatus.getId().getUserId());
    }
}
