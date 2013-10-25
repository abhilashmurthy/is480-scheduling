/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package userAction;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import static com.opensymphony.xwork2.Action.SUCCESS;
import com.opensymphony.xwork2.ActionSupport;
import constant.Role;
import java.util.HashMap;
import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;
import model.User;
import org.apache.struts2.interceptor.ServletRequestAware;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.MiscUtil;

/**
 *
 * @author suresh
 */
public class ManageTeamAction extends ActionSupport implements ServletRequestAware {
	
	private HttpServletRequest request;
	private HashMap<String, Object> json;
	private Logger logger = LoggerFactory.getLogger(ManageTeamAction.class);
	private EntityManager em = null;

	@Override
	public String execute() throws Exception {
		try {
			em = MiscUtil.getEntityManagerInstance();
			User user = (User) request.getSession().getAttribute("user");
			
			//Checking if the user is allowed to perform this function
			if (user.getRole() != Role.ADMINISTRATOR && user.getRole() != Role.COURSE_COORDINATOR) {
				json.put("success", false);
				json.put("message", "User does not have access to this page!");
				return SUCCESS;
			}
			
			//Retrieveing the form data
			JsonObject dataObj = new Gson().fromJson(request.getParameter("jsonData"), JsonObject.class);
			String actionType = dataObj.get("action").getAsString(); //Getting the action type being performed
			
			//Beginning transaction
			em.getTransaction().begin();
			
			//God I wish they had switch-case for Strings (Java 7!)
			if (actionType.equalsIgnoreCase("add") || actionType.equalsIgnoreCase("edit")) { //Add or edit a team
				addEditTeam(actionType, dataObj);
			} else if (actionType.equalsIgnoreCase("delete")) { //Delete an existing team
				deleteTeam();
			} else {
				json.put("success", false);
				json.put("message", "Unknown action. Options: add/edit/delete");
				return SUCCESS;
			}
			
			//Committing transaction
			em.getTransaction().commit();
        } catch (Exception e) {
            logger.error("Exception caught: " + e.getMessage());
            if (MiscUtil.DEV_MODE) {
                for (StackTraceElement s : e.getStackTrace()) {
                    logger.debug(s.toString());
                }
            }
            json.put("success", false);
            json.put("exception", true);
            json.put("message", "Error with ManageUsers: Escalate to developers!");
        }
        return SUCCESS;
	}
	
	//Method to add a new team to the system
	public void	addEditTeam(String actionType, JsonObject dataObj) {
		
	}
	
	//Method to delete an existing team from the system
	public void	deleteTeam() {
		
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
	
}
