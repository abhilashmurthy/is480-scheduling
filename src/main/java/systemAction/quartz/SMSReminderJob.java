/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package systemAction.quartz;

import constant.BookingStatus;
import java.net.URL;
import java.net.URLConnection;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import model.Booking;
import model.CronLog;
import model.Timeslot;
import model.User;
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
public class SMSReminderJob implements Job {

    private static Logger logger = LoggerFactory.getLogger(ClearPendingBookingJob.class);
    //private int noOfDaysToRespond1;
    //private int noOfDaysToRespond2;

    public void execute(JobExecutionContext jec) throws JobExecutionException {
        logger.debug("Started reminder job");
		//Initializing run log to be stored in database
		CronLog logItem = new CronLog();
		logItem.setJobName("Remind Pending Bookings");
		Calendar cal = Calendar.getInstance();
		Timestamp now = new Timestamp(cal.getTimeInMillis());
		logItem.setRunTime(now);
		
        EntityManager em = null;
        try {
            em = MiscUtil.getEntityManagerInstance();
            em.getTransaction().begin();
            List<Booking> approvedBookings = null;
            //get all the pending bookings
            Query queryBookings = em.createQuery("select p from Booking p where p.bookingStatus = :pendingBookingStatus")
                    .setParameter("pendingBookingStatus", BookingStatus.APPROVED);
            approvedBookings = (List<Booking>) queryBookings.getResultList();
			
                    if (approvedBookings.isEmpty()) throw new  NoResultException();

                    for (Booking approvedBooking : approvedBookings) {
                        //get the time for the present booking
                        cal.clear();
                        cal.setTimeInMillis(approvedBooking.getTimeslot().getStartTime().getTime());
                        //cal.add(Calendar.DATE, noOfDaysToRespond1);
                        Timestamp dueDate = new Timestamp(cal.getTimeInMillis());
                        
                        //find the difference in hours
                        long differenceInMins = dueDate.getTime()-now.getTime();
                        differenceInMins = differenceInMins / (60 * 1000);                      
                        
                        //if difference between approved booking and current time is 1 hour
                        if (differenceInMins == 60) {
                            logger.debug("Booking: " + approvedBooking + ". SMS sending..");
                            
                            //go through all required attendees to see if there is a number
                            Set<User> listOfU = approvedBooking.getRequiredAttendees();
                            
                            for (User eachU : listOfU) {
                                
                                //if user has mobile number
                                if(eachU.getMobileNumber()!=null){
                                    
                                    
                                    String teamName = approvedBooking.getTeam().getTeamName();
                                    teamName = teamName.replaceAll("\\s+", "+");
                                    
                                    String approvedTimeSlot = approvedBooking.getTimeslot().getStartTime().toString();
                                    approvedTimeSlot = approvedTimeSlot.replaceAll("\\s+", "+");
                                    
                                    String msg = "Team:+" + teamName
                                                        + "is+presenting+today:" + approvedTimeSlot
                                                             + ".+See+you+there!+From:IS480+Scheduling+System.";
                                    
                                    String number = "65";
                                    number += eachU.getMobileNumber();
                                    
                                    String appendURL = "http://smsc.vianett.no/v3/send.ashx?tel=" + number + "&msg=" + msg + "&username=tsgill.ps.2010@smu.edu.sg&password=h94c3";

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