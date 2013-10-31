/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package userAction;

import static com.opensymphony.xwork2.Action.ERROR;
import static com.opensymphony.xwork2.Action.SUCCESS;
import com.opensymphony.xwork2.ActionSupport;
import constant.Role;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import org.apache.struts2.interceptor.ServletRequestAware;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.HashMap;
import java.util.Set;
import javax.persistence.EntityManager;
import model.Booking;
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
		EntityManager em = null;
        try {
			json.put("exception", false);
			em = MiscUtil.getEntityManagerInstance();
            HttpSession session = request.getSession();
			User tempUser = (User) session.getAttribute("user");
			User user = em.find(User.class, tempUser.getId());
			
			Role activeRole = (Role) session.getAttribute("activeRole");
			//Need to change this for guests. Guests need to be users in our db before they can access any feature
			if (activeRole.equals(Role.STUDENT) || activeRole.equals(Role.FACULTY) || activeRole.equals(Role.ADMINISTRATOR) 
					|| activeRole.equals(Role.TA)) {
				
				//Getting input data from url
				JSONObject subscribeObject = (JSONObject) new JSONObject (request.getParameter("jsonData"));
				//Getting the subscription status (e.g. "Subscribe", "Unsubscribe")
				String status = subscribeObject.getString("subscriptionStatus");
				//Getting the booking id
				long bookingId = Long.valueOf(subscribeObject.getString("subscribedBooking"));
				
				em.getTransaction().begin();
				Booking b = em.find(Booking.class, bookingId);
				if (status.equalsIgnoreCase("Unsubscribe")) {
					Set<User> subscribedUsers = b.getSubscribedUsers();
					for (User userObj: subscribedUsers) {
						if (userObj.equals(user)) {
							subscribedUsers.remove(userObj);
							break;
						}
					}
					b.setSubscribedUsers(subscribedUsers);
					json.put("message", "You have successfully cancelled your RSVP!");
				} else if (status.equalsIgnoreCase("Subscribe")) {
					Set<User> subscribedUsers = b.getSubscribedUsers();
					subscribedUsers.add(user);
					b.setSubscribedUsers(subscribedUsers);
					json.put("message", "Your RSVP was successful!");
				}
				em.persist(b);
				em.getTransaction().commit();
				
				json.put("success", true);
			} else {
				request.setAttribute("error", "Oops. You're not authorized to access this page!");
				MiscUtil.logActivity(logger, user, "User cannot access this page");
				return ERROR;
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
            json.put("message", "Error with SetSubscription: Escalate to developers!");
        } finally {
			if (em != null && em.getTransaction().isActive()) em.getTransaction().rollback();
			if (em != null && em.isOpen()) em.close();
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