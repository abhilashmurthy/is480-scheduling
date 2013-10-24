/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package userAction;

import static com.opensymphony.xwork2.Action.ERROR;
import static com.opensymphony.xwork2.Action.SUCCESS;
import com.opensymphony.xwork2.ActionSupport;
import constant.Role;
import java.util.ArrayList;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import org.apache.struts2.interceptor.ServletRequestAware;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.HashMap;
import java.util.List;
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
public class GetSubscribedUsersAction extends ActionSupport implements ServletRequestAware {

    private HttpServletRequest request;
    private static Logger logger = LoggerFactory.getLogger(GetSubscribedUsersAction.class);
	private ArrayList<HashMap<String, String>> data = new ArrayList<HashMap<String, String>>();
	private HashMap<String, Object> json = new HashMap<String, Object>();

    @Override
    public String execute() throws Exception {
		EntityManager em = null;
        try {
			em = MiscUtil.getEntityManagerInstance();
            HttpSession session = request.getSession();
			User user = (User) session.getAttribute("user");
			
			Role activeRole = (Role) session.getAttribute("activeRole");
			//Need to change this for guests. Guests need to be users in our db before they can access any feature
			if (activeRole.equals(Role.STUDENT) || activeRole.equals(Role.COURSE_COORDINATOR) || activeRole.equals(Role.ADMINISTRATOR)) {
				
				//Getting input data from url
				JSONObject subscribeObject = (JSONObject) new JSONObject (request.getParameter("jsonData"));
				//Getting the booking id
				long bookingId = Long.valueOf(subscribeObject.getString("bookingId"));
				
				Booking b = em.find(Booking.class, bookingId);
				//Getting the list of users who have subscribed to the booking
				Set<User> subscribedUsers = b.getSubscribedUsers();
				List<String> sUsersList = new ArrayList<String>();
				if (subscribedUsers.size() > 0) {
					for (User sUser: subscribedUsers) {
//						HashMap<String, String> userMap = new HashMap<String, String>();
//						userMap.put("username", sUser.getFullName());
						sUsersList.add(sUser.getFullName());
//						data.add(userMap);
					}
				}
				json.put("data", sUsersList);
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
        } finally {
			if (em != null && em.getTransaction().isActive()) em.getTransaction().rollback();
			if (em != null && em.isOpen()) em.close();
		}
		return SUCCESS;
    }

	public ArrayList<HashMap<String, String>> getData() {
		return data;
	}

	public void setData(ArrayList<HashMap<String, String>> data) {
		this.data = data;
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