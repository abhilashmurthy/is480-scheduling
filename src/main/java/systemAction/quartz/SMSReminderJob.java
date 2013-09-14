/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package systemAction.quartz;

import java.net.URL;
import java.net.URLConnection;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Set;
import javax.persistence.EntityManager;
import model.Booking;
import model.CronLog;
import model.User;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.MiscUtil;

/**
 *
 * @author Tarlochan
 */
public class SMSReminderJob implements Job {

    private static Logger logger = LoggerFactory.getLogger(ClearPendingBookingJob.class);
    //private int noOfDaysToRespond1;
    //private int noOfDaysToRespond2;

    public void execute(JobExecutionContext jec) throws JobExecutionException {
        logger.debug("Started SMSReminderJob");
		//Initializing run log to be stored in database
		CronLog logItem = new CronLog();
		logItem.setJobName("SMS Reminders");
		Calendar cal = Calendar.getInstance();
		Timestamp now = new Timestamp(cal.getTimeInMillis());
		logItem.setRunTime(now);
		
        EntityManager em = null;
        try {
            em = MiscUtil.getEntityManagerInstance();

			JobDataMap dataMap = jec.getJobDetail().getJobDataMap();
			long bookingId = dataMap.getLong("bookingId");
			Booking booking = em.find(Booking.class, bookingId);
			
			//Block SMSs if system is in Dev Mode
			if (MiscUtil.DEV_MODE) {
				return;
			}

			//Check if the booking was found
			if (booking != null) {
				logger.debug("Booking: " + booking + ". SMS sending..");

				//go through all required attendees to see if there is a number
				Set<User> listOfU = booking.getRequiredAttendees();

				for (User eachU : listOfU) {

					//if user has mobile number
					if(eachU.getMobileNumber()!=null){


						String teamName = booking.getTeam().getTeamName();
						teamName = teamName.replaceAll("\\s+", "+");

						String approvedTimeSlot = booking.getTimeslot().getStartTime().toString();
						approvedTimeSlot = approvedTimeSlot.replaceAll("\\s+", "+");

						String msg = "Team:+" + teamName
											+ "is+presenting+on:" + approvedTimeSlot
												 + ".+See+you+there!+From:IS480+Scheduling+System.";

						String number = "65";
						number += eachU.getMobileNumber();

						String appendURL = "http://smsc.vianett.no/v3/send.ashx?tel="
								+ number + "&msg=" + msg + "&username=tsgill.ps.2010@smu.edu.sg&password=h94c3";

						//code for HTTP request to send sms
						URL myURL = new URL(appendURL);
						URLConnection myURLConnection = myURL.openConnection();
						myURLConnection.connect();

						//this code is for HTTP response
						/*BufferedReader in = new BufferedReader(new InputStreamReader(
								myURLConnection.getInputStream()));
						String inputLine;
						while ((inputLine = in.readLine()) != null) 
							System.out.println(inputLine);
						in.close();*/
					}
				}
				logItem.setSuccess(true);
				logItem.setMessage("SMS Reminders sent for booking ID: " + booking.getId());
			} else {
				throw new Exception("Booking not found!");
			}
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
        logger.debug("Finished SMSReminderJob");
    }
}