/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package systemAction.quartz;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
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

    private static Logger logger = LoggerFactory.getLogger(SMSReminderJob.class);
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

        SimpleDateFormat smsDateFormat = new SimpleDateFormat("EEE, dd MMM");
        SimpleDateFormat smsTimeFormat = new SimpleDateFormat("HH:mm");
        
        EntityManager em = null;
        HttpURLConnection connection = null;
        try {
            em = MiscUtil.getEntityManagerInstance();

            JobDataMap dataMap = jec.getJobDetail().getJobDataMap();
            long bookingId = dataMap.getLong("bookingId");
            Booking booking = em.find(Booking.class, bookingId);

            //Block SMSs if system is in Dev Mode
            if (false) { //TODO: Change to MiscUtil.DEV_MODE after testing
                logItem.setSuccess(true);
                logItem.setMessage("Job triggered with Booking ID: " + booking.getId() + ". SMS not sent.");
                return;
            }

            //Check if the booking was found
            if (booking != null) {
                logger.debug("Booking: " + booking + ". SMS sending..");
                StringBuilder msg = new StringBuilder();
                msg
                    .append("Reminder from IS480 Scheduling System").append("[NEWLINE][NEWLINE]")
                    .append("Team: ").append(booking.getTeam().getTeamName()).append("[NEWLINE]")
                    .append("Date: ").append(smsDateFormat.format(booking.getTimeslot().getStartTime())).append("[NEWLINE]")
                    .append("Time: ").append(smsTimeFormat.format(booking.getTimeslot().getStartTime())).append("[NEWLINE]")
                    .append("Venue: ").append(booking.getTimeslot().getVenue()).append("[NEWLINE][NEWLINE]")
					.append("Milestone: ").append(booking.getTimeslot().getSchedule().getMilestone().getName()).append("[NEWLINE]")
                    .append("See you there!");

                String countryCode = "65";
                StringBuilder phoneNums = new StringBuilder();
                
                //Get number of each required attendee
                for (User user : booking.getRequiredAttendees()) {
                    if (user.getMobileNumber() != null) {
                        phoneNums.append(countryCode)
                                    .append(user.getMobileNumber())
                                    .append(",");
                    }
                }
                
                //Builds RESTful URL
				if (phoneNums.length() == 0) return;
                StringBuilder url = new StringBuilder("");
				url
					.append("http://sgp.transmitsms.com/api-wrapper/messages.single?")
					.append("apikey=").append(MiscUtil.getProperty("General", "SMS_USERNAME"))
					.append("&apisecret=").append(MiscUtil.getProperty("General", "SMS_PASSWORD"))
					.append("&mobile=").append(phoneNums.toString().substring(0, phoneNums.length() - 1))
					.append("&message=").append(msg.toString().replace(" ", "%20").replace("[NEWLINE]", "%0A"))
					.append("&caller_id=")
					.append("IS480");

				//http://sgp.transmitsms.com/api-wrapper/messages.single?apikey=ca83b2ef57c5f75c8a846405f13eb5d0&apisecret=123321123321&mobile=6583870164&message=Hello%20there%0A%0AHow%20are%20you?&caller_id=IS480
				
                logger.debug("Sending to URL: " + url.toString());

                //Sends RESTful SMS
                URL myURL = new URL(url.toString());
                connection = (HttpURLConnection) myURL.openConnection();
                connection.connect();
                if (connection.getResponseCode() != 200) {
                    throw new Exception("Error response received");
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
            if (connection != null) {
                connection.disconnect();
            }
        }
        logger.debug("Finished SMSReminderJob");
    }
}