/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package systemAction.quartz;

import constant.BookingStatus;
import constant.Response;
import constant.Role;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
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

    private static Logger logger = LoggerFactory.getLogger(ClearPendingBookingJob.class);
    private int noOfDaysToRespond;

    public void execute(JobExecutionContext jec) throws JobExecutionException {
        logger.debug("Started faculty reminder");
		//Initializing run log to be stored in database
		CronLog logItem = new CronLog();
		logItem.setJobName("Remind faculty Bookings");
		Calendar cal = Calendar.getInstance();
		Timestamp now = new Timestamp(cal.getTimeInMillis());
		logItem.setRunTime(now);
		
        EntityManager em = null;
        
        try {
            em = MiscUtil.getEntityManagerInstance();
			
			//get the number of days for email reminder
			Settings currentSettings = SettingsManager.getByName(em, "manageNotifications");
			String value = currentSettings.getValue();
			
			//convert settingsDetails into an array
			String[] setArr = value.split(",");
			
			//get the number of days
			noOfDaysToRespond = Integer.parseInt(setArr[2]);
			noOfDaysToRespond--;
			//see if the email functionality is set as on
			boolean isOn = Boolean.parseBoolean(setArr[1]);

            em.getTransaction().begin();
            List<Booking> pendingBookings = null;
            //get all the pending bookings
            Query queryBookings = em.createQuery("select p from Booking p where p.bookingStatus = :pendingBookingStatus")
                    .setParameter("pendingBookingStatus", BookingStatus.PENDING);
            pendingBookings = (List<Booking>) queryBookings.getResultList();
			
                    if (pendingBookings.isEmpty()) throw new  NoResultException();

                    for (Booking pendingBooking : pendingBookings) {
                        //get one day break
						Calendar cal2 = Calendar.getInstance();
                        cal2.clear();
                        cal2.setTimeInMillis(pendingBooking.getTimeslot().getStartTime().getTime());
                        cal2.add(Calendar.DATE, 0);
                        Timestamp dueDate1 = new Timestamp(cal2.getTimeInMillis());
						
						long difference = (long)dueDate1.getTime()-(long)now.getTime();
						logger.debug("due date" + (long)dueDate1.getDate());
						logger.debug("now " + (long)now.getTime());
                        logger.debug(" the difference between due and now" + (long)difference);
                        
                        //if difference between due date and current time is less than or equal 24hours
                        if ((long)dueDate1.getTime()-(long)now.getTime() <= noOfDaysToRespond*86400000 && isOn) {
                            logger.debug("Booking: " + pendingBooking + ". First Reminder sent.");
							
							//get response list
							HashMap<User,Response> allStatus = pendingBooking.getResponseList();
							
							for(Map.Entry<User, Response> entry  : allStatus.entrySet()) {
								
								User user = entry.getKey();
								Response response = entry.getValue();
								

								if(user.getRole().equals(Role.FACULTY) && response.equals(Response.PENDING)){
									
									//Forcing initialization for sending email
									Hibernate.initialize(pendingBooking.getTeam().getMembers());
									Hibernate.initialize(pendingBooking.getTimeslot().getSchedule().getMilestone());
				
									
									Faculty facultyMember = UserManager.getUser(user.getId(), Faculty.class);
									
									FacultyReminderEmail facultyReminder = new FacultyReminderEmail(pendingBooking,facultyMember,cal2);
									facultyReminder.sendEmail();
									
								}
							}
							
							
							/*NewBookingEmail newEmail = new NewBookingEmail(booking);
							RespondToBookingEmail responseEmail = new RespondToBookingEmail(booking);
							newEmail.sendEmail();
							responseEmail.sendEmail();*/
							
							
							
                        }
						
						/*else if(dueDate1.compareTo(now) >=0){
                            logger.debug("Booking: " + pendingBooking + ". Second Reminder sent.");
                            //TODO: Add email notification for this task (last and final reminder)
                        }*/
                     }
            //em.getTransaction().commit();
			logItem.setSuccess(true);
			logItem.setMessage("Faculty reminded.");
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
				//Saving job log in database
				if (!em.getTransaction().isActive()) em.getTransaction().begin();
				em.persist(logItem);
				em.getTransaction().commit();
				
				if (em.getTransaction().isActive()) em.getTransaction().rollback();
				if (em.isOpen()) em.close();
			}
        }
        logger.debug("Finished reminding faculty");
    }
	
	
}