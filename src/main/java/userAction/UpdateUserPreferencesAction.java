/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package userAction;

import static com.opensymphony.xwork2.Action.SUCCESS;
import com.opensymphony.xwork2.ActionSupport;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.HashMap;
import java.util.regex.Pattern;
import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import model.SystemActivityLog;
import model.User;
import org.apache.struts2.interceptor.ServletRequestAware;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.MiscUtil;

/**
 *
 * @author Prakhar
 */
public class UpdateUserPreferencesAction extends ActionSupport implements ServletRequestAware {

    private HttpServletRequest request;
    static final Logger logger = LoggerFactory.getLogger(userAction.UpdateUserPreferencesAction.class);
    private HashMap<String, Object> json = new HashMap<String, Object>();

    @Override
    public String execute() throws Exception {
		HttpSession session = request.getSession();
		
		Calendar nowCal = Calendar.getInstance();
		Timestamp now = new Timestamp(nowCal.getTimeInMillis());
		
		SystemActivityLog logItem = new SystemActivityLog();
		logItem.setActivity("User Preferences: Update");
		logItem.setRunTime(now);
		logItem.setUser((User)session.getAttribute("user"));
		logItem.setMessage("Error with validation / No changes made");
		logItem.setSuccess(true);
		
        EntityManager em = null;
        try {
            json.put("exception", false);
            em = MiscUtil.getEntityManagerInstance();
			//Getting user object from session
			User userFromSession = (User) session.getAttribute("user");
			
			//Getting user object from db
			User userToUpdate = em.find(User.class, userFromSession.getId());
			
            JSONObject inputData = new JSONObject(request.getParameter("jsonData"));
            String number = inputData.getString("mobileNumber");

			//Check whether mobile number entered contains only digits
			if (number != null && !number.equals("")) {
				if (userToUpdate.getMobileNumber() != null && userToUpdate.getMobileNumber().equals(number)) {
					json.put("message", "No changes made...");
					json.put("success", true);
					return SUCCESS;
				}
				if (Pattern.matches("[a-zA-Z]+", number) == true || number.length() < 8){
					json.put("message", "Mobile Number is invalid!");
					json.put("success", false);
					return SUCCESS;
				}
				
				//Storing the users mobile number
				em.getTransaction().begin();
				userToUpdate.setMobileNumber(number);
				em.persist(userToUpdate);
				em.getTransaction().commit();
				MiscUtil.logActivity(logger, userToUpdate, "User preferences updated");
				
			} else {
				//Deleting user's mobile number
				em.getTransaction().begin();
				userToUpdate.setMobileNumber(null);
				em.persist(userToUpdate);
				em.getTransaction().commit();
			}
			
			logItem.setMessage("User Preferences were updated successfully for " + userToUpdate.toString());
			
			//Setting the updated user object in the session
			User user = em.find(User.class, userToUpdate.getId());
			session.setAttribute("user", user);
			
			json.put("success", true);
			json.put("message", "Your settings have been updated!");
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
            json.put("exception", true);
            json.put("message", "Error with UpdateUserPreferencesAction: Escalate to developers!");
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
		return SUCCESS;
    }

    public void setServletRequest(HttpServletRequest hsr) {
        this.request = hsr;
    }
	
    public HashMap<String, Object> getJson() {
        return json;
    }

    public void setJson(HashMap<String, Object> json) {
        this.json = json;
    }
}
