/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package systemAction.quartz;

import static com.opensymphony.xwork2.Action.ERROR;
import constant.BookingStatus;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Persistence;
import javax.persistence.Query;
import model.Booking;
import model.Timeslot;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import userAction.TestAction;
import util.MiscUtil;

/**
 *
 * @author ABHILASHM.2010
 */
public class ClearPendingBookingJob implements Job {

    private static Logger logger = LoggerFactory.getLogger(ClearPendingBookingJob.class);
    private int noOfDaysToRespond;

    public void execute(JobExecutionContext jec) throws JobExecutionException {
        logger.debug("Started ClearPendingBookingJob");
        EntityManager em = null;
        noOfDaysToRespond = 1; //TODO: Remove hardcoding
        try {
            em = MiscUtil.getEntityManagerInstance();
            em.getTransaction().begin();
            List<Booking> pendingBookings = null;
            Query queryBookings = em.createQuery("select p from Booking p where p.bookingStatus = :pendingBookingStatus")
                    .setParameter("pendingBookingStatus", BookingStatus.PENDING);
            pendingBookings = (List<Booking>) queryBookings.getResultList();
            Calendar cal = Calendar.getInstance();
            Timestamp now = new Timestamp(cal.getTimeInMillis());
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
        } catch (NoResultException n) {
            //Normal, no pending bookings found
            logger.trace("There are no pending bookings now");
        } catch (Exception e) {
            logger.error("Exception caught: " + e.getMessage());
            if (MiscUtil.DEV_MODE) {
                for (StackTraceElement s : e.getStackTrace()) {
                    logger.debug(s.toString());
                }
            }
        } finally {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            if (em.isOpen()) {
                em.close();
            }
        }
        logger.debug("Finished ClearPendingBookingJob");
    }
}