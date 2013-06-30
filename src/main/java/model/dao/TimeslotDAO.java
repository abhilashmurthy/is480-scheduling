/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package model.dao;

import java.math.BigInteger;
import java.util.Date;
import model.Timeslot;
import static model.dao.TimeslotDAO.session;
import org.hibernate.Query;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.HibernateUtil;

/**
 *
 * @author suresh
 */
public class TimeslotDAO {
	static final Logger logger = LoggerFactory.getLogger(TimeslotDAO.class);

    static Session session = HibernateUtil.getSession();

    public static void save(Timeslot timeslot) {
        session.beginTransaction();
        session.save(timeslot);
        session.getTransaction().commit();
        logger.info("Added timeslot TermId:" + timeslot.getId().getTermId()
				+ " Milestone: " + timeslot.getId().getMilestone()
				+ " Start Time: " + timeslot.getId().getStartTime());
    }

    public static void update(Timeslot timeslot) {
        session.beginTransaction();
        session.update(timeslot);
        session.getTransaction().commit();
        logger.info("Updated timeslot TermId:" + timeslot.getId().getTermId()
				+ " Milestone: " + timeslot.getId().getMilestone()
				+ " Start Time: " + timeslot.getId().getStartTime());
    }

    public static void delete(Timeslot timeslot) {
        session.beginTransaction();
        session.delete(timeslot);
        session.getTransaction().commit();
        logger.info("Delete timeslot TermId:" + timeslot.getId().getTermId()
				+ " Milestone: " + timeslot.getId().getMilestone()
				+ " Start Time: " + timeslot.getId().getStartTime());
    }

    public static Timeslot findByTimeslotId(int termId, String milestone, Date startTime) {
        session.beginTransaction();
        BigInteger bigIntTermId = BigInteger.valueOf(termId);
        Query query = session.createQuery("from Timeslot where "
				+ "id.termId = :termId and "
				+ "id.milestone = :milestone "
				+ "id.startTime = :startTime");
        query.setParameter("termId", bigIntTermId);
		query.setParameter("milestone", milestone);
		query.setParameter("startTime", startTime);
        Timeslot timeslot = (Timeslot) query.uniqueResult();
        session.getTransaction().commit();
        return timeslot;
    }
}
