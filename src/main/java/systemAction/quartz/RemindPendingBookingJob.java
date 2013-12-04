/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package systemAction.quartz;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import constant.BookingStatus;
import constant.Response;
import constant.Role;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import manager.SettingsManager;
import manager.UserManager;
import model.Booking;
import model.Settings;
import model.SystemActivityLog;
import model.User;
import model.role.Faculty;
import notification.email.FacultyReminderEmail;
import org.hibernate.Hibernate;
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

    private static Logger logger = LoggerFactory.getLogger(RemindPendingBookingJob.class);

    public void execute(JobExecutionContext jec) throws JobExecutionException {
        logger.debug("Started faculty reminder");
		
		Calendar nowCal = Calendar.getInstance();
		Timestamp now = new Timestamp(nowCal.getTimeInMillis());
		
		SystemActivityLog logItem = new SystemActivityLog();
		logItem.setActivity("Faculty Booking Reminder");
		logItem.setRunTime(now);
		logItem.setMessage("Sending reminders to faculty started " + now);
		
        EntityManager em = null;
        
        try {
            em = MiscUtil.getEntityManagerInstance();
			
			Settings notificationSettings = SettingsManager.getByName(em, "manageNotifications");
			String jsonData = notificationSettings.getValue();
			Gson gson = new Gson();
			
			JsonArray notifArray = gson.fromJson(jsonData, JsonArray.class);
			JsonObject clearBookingSetting = notifArray.get(2).getAsJsonObject();
			String durationStr = clearBookingSetting.get("emailClearFrequency").getAsString();
			int duration = Integer.parseInt(durationStr); //Duration for faculty to respond to booking
			if (duration == 0) return; //0 means the feature is disabled
			Calendar tomorrow = Calendar.getInstance();
			if (MiscUtil.DEV_MODE) {
				tomorrow.add(Calendar.DAY_OF_MONTH, 1); //Tomorrow 3AM
	//			tomorrow.add(Calendar.MINUTE, 1); //TESTING. Next minute	
			} else {
				tomorrow.add(Calendar.DAY_OF_MONTH, 1); //Tomorrow 3AM
			}
            em.getTransaction().begin();
            List<Booking> pendingBookings = null;
            //get all the pending bookings
            Query queryBookings = em.createQuery("select p from Booking p where p.bookingStatus = :pendingBookingStatus")
                    .setParameter("pendingBookingStatus", BookingStatus.PENDING);
            pendingBookings = (List<Booking>) queryBookings.getResultList();

			ArrayList<Long> remindedIds = new ArrayList<Long>();
			for (Booking pendingBooking : pendingBookings) {
				Calendar deadline = Calendar.getInstance();
				deadline.setTimeInMillis(pendingBooking.getCreatedAt().getTime());
				if (MiscUtil.DEV_MODE) {
					deadline.add(Calendar.DAY_OF_MONTH, duration);
	//				deadline.add(Calendar.MINUTE, duration); //TESTING	
				} else {
					deadline.add(Calendar.DAY_OF_MONTH, duration);
				}
				//If the deadline is between today 3AM and tomorrow 3AM, send a reminder
				if (deadline.compareTo(nowCal) >= 0 && deadline.compareTo(tomorrow) <= 0) {
					logger.debug("Booking: " + pendingBooking + ". Reminder sent.");
					//get response list
					HashMap<User,Response> allStatus = pendingBooking.getResponseList();

					for (Map.Entry<User, Response> entry  : allStatus.entrySet()) {
						User user = entry.getKey();
						Response response = entry.getValue();

						if(user.getRole().equals(Role.FACULTY) && response.equals(Response.PENDING)){
							Faculty facultyMember = UserManager.getUser(user.getId(), Faculty.class);
							//Forcing initialization for sending email
							Hibernate.initialize(pendingBooking.getTeam().getMembers());
							Hibernate.initialize(pendingBooking.getTimeslot().getSchedule().getMilestone());
							
							FacultyReminderEmail facultyReminder = new FacultyReminderEmail(pendingBooking,facultyMember);
							facultyReminder.sendEmail();
						}
					}
					remindedIds.add(pendingBooking.getId());
				}
			}
			
			logItem.setSuccess(true);
			if (remindedIds.isEmpty()) throw new NoResultException(); //There were no reminders sent in this round
			
			StringBuilder idString = new StringBuilder();
			Iterator iter = remindedIds.iterator();
			while (iter.hasNext()) {
				idString.append(iter.next());
				if (iter.hasNext()) idString.append(",");
			}
			logItem.setMessage("Faculty reminded for bookings. (IDs: " + idString.toString() + ")");
        } catch (NoResultException n) {
            //Normal, no pending bookings found
			logItem.setSuccess(true);
			logItem.setMessage("No faculty to remind.");
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
				if (em.getTransaction().isActive()) em.getTransaction().rollback();
                //Saving job log in database
                if (!em.getTransaction().isActive()) em.getTransaction().begin();
                em.persist(logItem);
                em.getTransaction().commit();
                if (em.isOpen()) em.close();
			}
        }
        logger.debug("Finished reminding faculty");
    }
	
	
}