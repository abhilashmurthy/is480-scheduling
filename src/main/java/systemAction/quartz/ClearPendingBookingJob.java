/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package systemAction.quartz;

import constant.BookingStatus;
import constant.Role;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import manager.SettingsManager;
import manager.UserManager;
import model.Booking;
import model.CronLog;
import model.Settings;
import model.Timeslot;
import model.User;
import notification.email.DeletedBookingEmail;
import notification.email.RejectedBookingEmail;
import org.hibernate.Hibernate;
import org.json.JSONArray;
import org.json.JSONObject;
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
        //Initializing run log to be stored in database
        CronLog logItem = new CronLog();
        logItem.setJobName("Clear Pending Bookings");
        Calendar cal = Calendar.getInstance();
        Timestamp now = new Timestamp(cal.getTimeInMillis());
        logItem.setRunTime(now);

        EntityManager em = null;
        
        try {
            em = MiscUtil.getEntityManagerInstance();
			
			//get the number of days for email reminder
			//Getting the email settings value
			Settings notificationSettings = SettingsManager.getNotificationSettings(em);

			JSONArray notificationArray = new JSONArray(notificationSettings.getValue());
			
			//get email settings
			JSONObject obj = notificationArray.getJSONObject(2);
			String getEmailStatus = obj.getString("emailClearStatus");
			int emailFrequency = obj.getInt("emailClearFrequency");
			
			//get the number of days
			noOfDaysToRespond = emailFrequency;
			
			//see if the email functionality is set as on
			boolean isOn = false;
			
			if(getEmailStatus.equalsIgnoreCase("On")){
				 isOn = true;
			}
            em.getTransaction().begin();
            
            User systemAsUser = new User("is480.scheduling", "IS480 Scheduling System", null, null, null);
                    
            List<Booking> pendingBookings = null;
            Query queryBookings = em.createQuery("select p from Booking p where p.bookingStatus = :pendingBookingStatus")
                    .setParameter("pendingBookingStatus", BookingStatus.PENDING);
            pendingBookings = (List<Booking>) queryBookings.getResultList();
			
			ArrayList<Long> remindedIds = new ArrayList<Long>();
            for (Booking pendingBooking : pendingBookings) {
                //Do the time calculation
                cal.clear();
                cal.setTimeInMillis(pendingBooking.getCreatedAt().getTime());
                cal.add(Calendar.DATE, noOfDaysToRespond);
//                cal.add(Calendar.MINUTE, noOfDaysToRespond); //For testing
                Timestamp dueDate = new Timestamp(cal.getTimeInMillis());
                //Delete booking is date is passed
                if (now.after(dueDate)) {
                    logger.debug("Booking: " + pendingBooking + " passed due date. Deleting.");
                    pendingBooking.setBookingStatus(BookingStatus.DELETED);
                    pendingBooking.setLastEditedBy("IS480 Scheduling System");
                    pendingBooking.setLastEditedAt(now);
                    pendingBooking.setComment("Faculty response overdue. Releasing timeslot.");
                    Timeslot ts = pendingBooking.getTimeslot();
                    ts.setCurrentBooking(null);
                    em.persist(pendingBooking);
                    em.persist(ts);
					//Send an email to inform that booking has been rejected by system
					if(isOn){
						logger.debug("email sending now");
						
						//Forcing initialization for sending email
						Hibernate.initialize(pendingBooking.getTeam().getMembers());
						Hibernate.initialize(pendingBooking.getTimeslot().getSchedule().getMilestone());
						
						DeletedBookingEmail email = new DeletedBookingEmail(pendingBooking, systemAsUser);
						email.sendEmail();
					}
					remindedIds.add(pendingBooking.getId());
                }
            }
            em.getTransaction().commit();
            logItem.setSuccess(true);
			if (remindedIds.isEmpty()) throw new NoResultException(); //There were no reminders sent in this round
			
			StringBuilder idString = new StringBuilder();
			Iterator iter = remindedIds.iterator();
			while (iter.hasNext()) {
				idString.append(iter.next());
				if (iter.hasNext()) idString.append(",");
			}
			logItem.setMessage("Pending bookings cleared. (IDs: " + idString.toString() + ")");
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
                if (!em.getTransaction().isActive()) {
                    em.getTransaction().begin();
                }
                em.persist(logItem);
                em.getTransaction().commit();

                if (em.getTransaction().isActive()) {
                    em.getTransaction().rollback();
                }
                if (em.isOpen()) {
                    em.close();
                }
            }
        }
    }
}