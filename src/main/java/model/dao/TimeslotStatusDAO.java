/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package model.dao;

import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.List;
import model.Timeslot;
import model.TimeslotStatus;
import org.hibernate.Query;
import org.hibernate.Session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.HibernateUtil;
import util.Status;

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
    
    public static List<TimeslotStatus> findTimeSlotStatusByTermAndUser(int termId, int userId) {
        session.beginTransaction();
        BigInteger bigIntId = BigInteger.valueOf(termId);
        BigInteger bigIntId2 = BigInteger.valueOf(userId);
        Query query = session.createQuery("from TimeslotStatus where " + "id.termId = :termId1 and id.userId = :userId2");
        query.setParameter("termId1", bigIntId);
        query.setParameter("userId2", bigIntId2);
        List<TimeslotStatus> ts = (List<TimeslotStatus>) query.list();
        session.getTransaction().commit();
        return ts;
    }
    
    //get timeslot by teamID and update status to Accept/Reject
     public static void updateTimeSlotStatusByTeamId(int teamId, String status) {
        
        //find timeslot by teamID
        Timeslot ts = TimeslotDAO.findTimeSlotByTeam(teamId);
        
        //get timeslotstatus item based on ts time
        session.beginTransaction();
        Query query = session.createQuery("from TimeslotStatus where " + "id.startTime = :startTime");
        query.setParameter("startTime", ts.getId().getStartTime());
        TimeslotStatus tsStatus = (TimeslotStatus) query.uniqueResult();
        session.getTransaction().commit();

        //based on the timeslot, change the status
        Status finalstatus = (status.equalsIgnoreCase("ACCEPTED"))
				? Status.ACCEPTED
				: (status.equalsIgnoreCase("REJECTED"))
				? Status.REJECTED
                                : Status.PENDING;
        
        tsStatus.setStatus(finalstatus);
        
        //update
        update(tsStatus);
     }
}
