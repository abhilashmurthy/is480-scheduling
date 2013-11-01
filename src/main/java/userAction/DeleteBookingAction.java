/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package userAction;

import com.opensymphony.xwork2.ActionSupport;
import constant.BookingStatus;
//import constant.Status;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.HashMap;
import javax.persistence.EntityManager;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import manager.TimeslotManager;
import model.Booking;
import model.SystemActivityLog;
import model.Timeslot;
import model.User;
import notification.email.DeletedBookingEmail;
import org.apache.struts2.interceptor.ServletRequestAware;
import org.hibernate.Hibernate;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.ee.servlet.QuartzInitializerListener;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.MiscUtil;

/**
 *
 * @author Tarlochan
 */
public class DeleteBookingAction extends ActionSupport implements ServletRequestAware {

    private HttpServletRequest request;
    private static Logger logger = LoggerFactory.getLogger(DeleteBookingAction.class);
    private String timeslotId;
    private HashMap<String, Object> json = new HashMap<String, Object>();

    @Override
    public String execute() throws ServletException, IOException {
		HttpSession session = request.getSession();
		
		Calendar nowCal = Calendar.getInstance();
		Timestamp now = new Timestamp(nowCal.getTimeInMillis());
		
		SystemActivityLog logItem = new SystemActivityLog();
		logItem.setActivity("Booking: Delete");
		logItem.setRunTime(now);
		logItem.setUser((User)session.getAttribute("user"));
		logItem.setMessage("Error with validation / No changes made");
		logItem.setSuccess(true);
		
		EntityManager em = null;
        try {
            json.put("exception", false);
            em = MiscUtil.getEntityManagerInstance();
            
            User user = (User) session.getAttribute("user");

            //convert the chosen ID into long and get the corresponding Timeslot object
            long chosenID = Long.parseLong(timeslotId);
            Timeslot ts = TimeslotManager.findById(em, chosenID);

            try {
                em.getTransaction().begin();
				
                //set the current booking's status to deleted
                Booking b = ts.getCurrentBooking();
                b.setBookingStatus(BookingStatus.DELETED);
                b.setLastEditedBy(user.getFullName());
                b.setLastEditedAt(new Timestamp(Calendar.getInstance().getTimeInMillis()));

                //set the current booking to null
                ts.setCurrentBooking(null);

                em.persist(b);
                em.persist(ts);

                em.getTransaction().commit();
				
				//Forcing initialization for sending email
				Hibernate.initialize(b.getTeam().getMembers());
				Hibernate.initialize(b.getTimeslot().getSchedule().getMilestone());
				
				deleteSMSReminder(b);

				//Sending email
				DeletedBookingEmail deletedEmail = new DeletedBookingEmail(b, (User)request.getSession().getAttribute("user"));
				deletedEmail.sendEmail();
				
                //if the booking has been removed successfully
                json.put("message", "Booking deleted successfully! All attendees have been notified via email.");

				MiscUtil.logActivity(logger, user, b.toString() + " deleted");
				
				logItem.setMessage("Booking was deleted successfully. " + b.toString());
				
            } catch (Exception e) {
				logItem.setSuccess(false);
				User userForLog = (User) session.getAttribute("user");
				logItem.setUser(userForLog);
				logItem.setMessage("Error: " + e.getMessage());
				
                logger.error("Exception caught: " + e.getMessage());
                if (MiscUtil.DEV_MODE) {
                    for (StackTraceElement s : e.getStackTrace()) {
                        logger.debug(s.toString());
                    }
                }
                json.put("success", false);
                json.put("exception", true);
                json.put("message", "Error with persisting timeslot object: Escalate to developers!");
            }

        } catch (Exception e) {
			logItem.setSuccess(false);
			User userForLog = (User) session.getAttribute("user");
			logItem.setUser(userForLog);
			logItem.setMessage("Error: " + e.getMessage());
				
            logger.error("Exception caught: " + e.getMessage());
            if (MiscUtil.DEV_MODE) {
                for (StackTraceElement s : e.getStackTrace()) {
                    logger.debug(s.toString());
                }
            }
            json.put("success", false);
            json.put("exception", true);
            json.put("message", "Error with Delete Booking: Escalate to developers!");
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
        return SUCCESS;
    }
	
	//Deleting the scheduled job to send SMS reminders
	private void deleteSMSReminder(Booking b) throws Exception {
		StdSchedulerFactory factory = (StdSchedulerFactory) request.getSession()
				.getServletContext()
				.getAttribute(QuartzInitializerListener.QUARTZ_FACTORY_KEY);
		Scheduler scheduler = factory.getScheduler();
		
		scheduler.deleteJob(new JobKey(b.getId().toString(), MiscUtil.SMS_REMINDER_JOBS));
	}

    public HttpServletRequest getRequest() {
        return request;
    }

    public void setRequest(HttpServletRequest request) {
        this.request = request;
    }

    public void setServletRequest(HttpServletRequest hsr) {
        this.request = hsr;
    }

    public String getTimeslotId() {
        return timeslotId;
    }

    public void setTimeslotId(String timeslotId) {
        this.timeslotId = timeslotId;
    }

    public HashMap<String, Object> getJson() {
        return json;
    }

    public void setJson(HashMap<String, Object> json) {
        this.json = json;
    }
}
