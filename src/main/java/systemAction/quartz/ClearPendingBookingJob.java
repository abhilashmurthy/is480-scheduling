/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package systemAction.quartz;

import constant.BookingStatus;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import model.Booking;
import model.CronLog;
import model.Timeslot;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.MiscUtil;

/**
 *
 * @author ABHILASHM.2010
 */
public class ClearPendingBookingJob implements Job {

    private static Logger logger = LoggerFactory.getLogger(ClearPendingBookingJob.class);
    private int noOfDaysToRespond;

    public void execute(JobExecutionContext jec) throws JobExecutionException {
        logger.debug("Started Clear Pending Bookings job");
		//Initializing run log to be stored in database
		CronLog logItem = new CronLog();
		logItem.setJobName("Clear Pending Bookings");
		Calendar cal = Calendar.getInstance();
		Timestamp now = new Timestamp(cal.getTimeInMillis());
		logItem.setRunTime(now);
		
        EntityManager em = null;
        noOfDaysToRespond = 1; //TODO: Remove hardcoding
        try {
            em = MiscUtil.getEntityManagerInstance();
            em.getTransaction().begin();
            List<Booking> pendingBookings = null;
            Query queryBookings = em.createQuery("select p from Booking p where p.bookingStatus = :pendingBookingStatus")
                    .setParameter("pendingBookingStatus", BookingStatus.PENDING);
            pendingBookings = (List<Booking>) queryBookings.getResultList();
			
			if (pendingBookings.isEmpty()) throw new  NoResultException();
            
			for (Booking pendingBooking : pendingBookings) {
                //Do the time calculation
                cal.clear();
                cal.setTimeInMillis(pendingBooking.getCreatedAt().getTime());
                cal.add(Calendar.DATE, noOfDaysToRespond);
//                    cal.add(Calendar.MINUTE, noOfDaysToRespond); //For testing
                Timestamp dueDate = new Timestamp(cal.getTimeInMillis());
                //Delete booking is date is passed
                if (now.after(dueDate)) {
                    logger.debug("Booking: " + pendingBooking + " passed due date. Deleting.");
                    pendingBooking.setBookingStatus(BookingStatus.DELETED);
                    pendingBooking.setLastEditedBy("IS480 Scheduling System");
                    pendingBooking.setLastEditedAt(now);
                    pendingBooking.setRejectReason("Faculty response overdue. Releasing timeslot.");
                    Timeslot ts = pendingBooking.getTimeslot();
                    ts.setCurrentBooking(null);
                    em.persist(pendingBooking);
                    em.persist(ts);
                    //TODO: Add email notification for this task
                }
            }
            em.getTransaction().commit();
			logItem.setMessage("Success: Pending bookings cleared.");
        } catch (NoResultException n) {
            //Normal, no pending bookings found
			logItem.setMessage("Success: No pending bookings to clear.");
            logger.trace("There are no pending bookings now");
        } catch (Exception e) {
			logItem.setMessage("Error: " + e.getMessage());
            logger.error("Exception caught: " + e.getMessage());
            if (MiscUtil.DEV_MODE) {
                for (StackTraceElement s : e.getStackTrace()) {
                    logger.debug(s.toString());
                }
            }
        } finally {
            if (em != null) {
				//Saving job log in database
				if (!em.getTransaction().isActive()) em.getTransaction().begin();
				em.persist(logItem);
				em.getTransaction().commit();
				
				if (em.getTransaction().isActive()) em.getTransaction().rollback();
				if (em.isOpen()) em.close();
			}
        }
        logger.debug("Finished ClearPendingBookingJob");
    }
}