/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package userAction;

import static com.opensymphony.xwork2.Action.ERROR;
import static com.opensymphony.xwork2.Action.SUCCESS;
import com.opensymphony.xwork2.ActionSupport;
import constant.BookingStatus;
import constant.PresentationType;
import constant.Role;
import java.sql.Timestamp;
import java.util.Calendar;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import org.apache.struts2.interceptor.ServletRequestAware;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.HashMap;
import java.util.HashSet;
import javax.persistence.EntityManager;
import model.Booking;
import model.SystemActivityLog;
import model.Team;
import model.User;
import org.json.JSONObject;
import util.MiscUtil;

/**
 *
 * @author Prakhar
 */
public class SetSubscriptionAction extends ActionSupport implements ServletRequestAware {

    private HttpServletRequest request;
    private static Logger logger = LoggerFactory.getLogger(SetSubscriptionAction.class);
	private HashMap<String, Object> json = new HashMap<String, Object>();

    @Override
    public String execute() throws Exception {
		HttpSession session = request.getSession();
		
		Calendar nowCal = Calendar.getInstance();
		Timestamp now = new Timestamp(nowCal.getTimeInMillis());
		
		SystemActivityLog logItem = new SystemActivityLog();
		logItem.setActivity("Subscription: Update");
		logItem.setRunTime(now);
		logItem.setUser((User)session.getAttribute("user"));
		logItem.setMessage("Error with validation / No changes made");
		logItem.setSuccess(true);
		
		EntityManager em = null;
        try {
			json.put("exception", false);
			em = MiscUtil.getEntityManagerInstance();
			User tempUser = (User) session.getAttribute("user");
			User user = em.find(User.class, tempUser.getId());
			
			Role activeRole = (Role) session.getAttribute("activeRole");
			
			if (activeRole.equals(Role.STUDENT) || activeRole.equals(Role.FACULTY) || activeRole.equals(Role.TA) || 
					activeRole.equals(Role.GUEST)) {
				
				//Getting input data from url
				JSONObject subscribeObject = (JSONObject) new JSONObject (request.getParameter("jsonData"));
				//Getting the subscription status (e.g. "Subscribe", "Unsubscribe")
				String status = subscribeObject.getString("subscriptionStatus");
				//Getting the booking id
				long bookingId = Long.valueOf(subscribeObject.getString("subscribedBooking"));
				
				em.getTransaction().begin();
				Booking b = em.find(Booking.class, bookingId);
				if (status.equalsIgnoreCase("Unsubscribe")) {
					HashSet<String> subscribedUsers = b.getSubscribers();
					for (String userEmail: subscribedUsers) {
						if (userEmail.equals(user.getEmail())) {
							subscribedUsers.remove(userEmail);
							break;
						}
					}
					b.setSubscribers(subscribedUsers);
					json.put("message", "You have successfully cancelled your RSVP!");
				} else if (status.equalsIgnoreCase("Subscribe")) {
					//Check whether the team's presentation is PRIVATE, INTERNAL or PUBLIC
					Team team = b.getTeam();
					if (team.getPresentationType() == PresentationType.PRIVATE) {
						//Only for faculty and students part of the team only
						if (user.getId() != team.getSupervisor().getId() && user.getId() != team.getReviewer1().getId() 
								&& user.getId() != team.getReviewer2().getId()) {
							json.put("message", "This presentation is " + team.getPresentationType() + ". You cannot RSVP!");
							json.put("success", true);
							return SUCCESS;
						}
					} else {
						//For all SIS staff and students only
						String smuGroups = (String) session.getAttribute("smu_groups");
						if (!smuGroups.toLowerCase().contains("sis")) {
							json.put("message", "This presentation is " + team.getPresentationType() + ". You cannot RSVP!");
							json.put("success", true);
							return SUCCESS;
						}
					} 
					
					//Checking whether the booking has been confirmed or not
					if (b.getBookingStatus() != BookingStatus.APPROVED) {
						json.put("message", "This presentation has not yet been confirmed. Please try again later!");
						json.put("success", true);
						return SUCCESS;
					}
					
					HashSet<String> subscribedUsers = b.getSubscribers();
					if (subscribedUsers == null) {
						subscribedUsers = new HashSet<String>();
					}
					subscribedUsers.add(user.getEmail());
					b.setSubscribers(subscribedUsers);
					json.put("message", "Your RSVP was successful!");
				}
				em.persist(b);
				em.getTransaction().commit();
				
				logItem.setMessage("Subscription was updated successfully. "
						+ status + " for " + b.toString());
				
				json.put("success", true);
			} else {
				request.setAttribute("error", "Oops. You're not authorized to access this page!");
				MiscUtil.logActivity(logger, user, "User cannot access this page");
				return ERROR;
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
            json.put("message", "Error with SetSubscription: Escalate to developers!");
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

	public HashMap<String, Object> getJson() {
		return json;
	}

	public void setJson(HashMap<String, Object> json) {
		this.json = json;
	}
	
    public void setServletRequest(HttpServletRequest hsr) {
        this.request = hsr;
    }
}  //end of class