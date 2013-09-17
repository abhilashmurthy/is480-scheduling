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
 * @author Tarlochan
 */
public class RemindPendingBookingJob implements Job {

    private static Logger logger = LoggerFactory.getLogger(ClearPendingBookingJob.class);
    private int noOfDaysToRespond1;
    private int noOfDaysToRespond2;

    public void execute(JobExecutionContext jec) throws JobExecutionException {
        logger.debug("Started reminder job");
		//Initializing run log to be stored in database
		CronLog logItem = new CronLog();
		logItem.setJobName("Remind Pending Bookings");
		Calendar cal = Calendar.getInstance();
		Timestamp now = new Timestamp(cal.getTimeInMillis());
		logItem.setRunTime(now);
		
        EntityManager em = null;
        noOfDaysToRespond1 = 1;
        noOfDaysToRespond2 = 2;//TODO: Remove hardcoding
        try {
            em = MiscUtil.getEntityManagerInstance();
            em.getTransaction().begin();
            List<Booking> pendingBookings = null;
            //get all the pending bookings
            Query queryBookings = em.createQuery("select p from Booking p where p.bookingStatus = :pendingBookingStatus")
                    .setParameter("pendingBookingStatus", BookingStatus.PENDING);
            pendingBookings = (List<Booking>) queryBookings.getResultList();
			
                    if (pendingBookings.isEmpty()) throw new  NoResultException();

                    for (Booking pendingBooking : pendingBookings) {
                        //get one day break
                        cal.clear();
                        cal.setTimeInMillis(pendingBooking.getCreatedAt().getTime());
                        cal.add(Calendar.DATE, noOfDaysToRespond1);
                        Timestamp dueDate1 = new Timestamp(cal.getTimeInMillis());
                        
                        //get two days break
                        cal.clear();
                        cal.setTimeInMillis(pendingBooking.getCreatedAt().getTime());
                        cal.add(Calendar.DATE, noOfDaysToRespond2);
                        Timestamp dueDate2 = new Timestamp(cal.getTimeInMillis());
                        
                        
                        //if due date is greater than now, send a reminder for action
                        //if will check if the two day gap is greater than now, else
                        //will check if there is a one day gap and submit a final reminder
                        if (dueDate2.compareTo(now) >=0) {
                            logger.debug("Booking: " + pendingBooking + ". First Reminder sent.");
                            //TODO: Add email notification for this task (first reminder)
                        }else if(dueDate1.compareTo(now) >=0){
                            logger.debug("Booking: " + pendingBooking + ". Second Reminder sent.");
                            //TODO: Add email notification for this task (last and final reminder)
                        }
                     }
            em.getTransaction().commit();
			logItem.setSuccess(true);
			logItem.setMessage("Pending bookings cleared.");
        } catch (NoResultException n) {
            //Normal, no pending bookings found
			logItem.setSuccess(true);
			logItem.setMessage("No pending bookings to clear.");
            logger.trace("There are no pending bookings now");
        } catch (Exception e) {
			logItem.setSuccess(false);
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
        logger.debug("Finished RemindPendingBookingJob");
    }
}