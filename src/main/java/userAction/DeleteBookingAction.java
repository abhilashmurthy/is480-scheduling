/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package userAction;

import com.opensymphony.xwork2.ActionSupport;
import constant.BookingStatus;
//import constant.Status;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.EntityManager;
import javax.persistence.Persistence;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import manager.TimeslotManager;
import manager.UserManager;
import model.Booking;
import model.Timeslot;
import model.User;
import notification.email.DeletedBookingEmail;
import org.apache.struts2.interceptor.ServletRequestAware;
import org.hibernate.Hibernate;
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
		EntityManager em = null;
        try {
            json.put("exception", false);
            em = MiscUtil.getEntityManagerInstance();
            HttpSession session = request.getSession();
            
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

                //set the current booking to null
                ts.setCurrentBooking(null);

                em.persist(b);
                em.persist(ts);

                em.getTransaction().commit();
				
				//Forcing initialization for sending email
				Hibernate.initialize(b.getTeam().getMembers());
				Hibernate.initialize(b.getTimeslot().getSchedule().getMilestone());
				//Sending email
				DeletedBookingEmail deletedEmail = new DeletedBookingEmail(b, (User)request.getSession().getAttribute("user"));
				deletedEmail.sendEmail();

                //if the booking has been removed successfully
                json.put("message", "Booking deleted successfully! Deletion email has been sent to all attendees. (Coming soon..)");

            } catch (Exception e) {
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
			if (em != null && em.getTransaction().isActive()) em.getTransaction().rollback();
			if (em != null && em.isOpen()) em.close();
		}
        return SUCCESS;
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
