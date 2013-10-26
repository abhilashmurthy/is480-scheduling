/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package userAction;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import static com.opensymphony.xwork2.Action.SUCCESS;
import com.opensymphony.xwork2.ActionSupport;
import constant.Role;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;
import manager.TeamManager;
import model.User;
import org.apache.struts2.interceptor.ServletRequestAware;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.CustomException;
import util.MiscUtil;

/**
 *
 * @author suresh
 */
public class ManageTeamAction extends ActionSupport implements ServletRequestAware {
	
	private HttpServletRequest request;
	private HashMap<String, Object> json = new HashMap<String, Object>();
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
			
			boolean result;
			if (actionType.equalsIgnoreCase("add") || actionType.equalsIgnoreCase("edit")) { //Add or edit a team
				result = addEditTeam(actionType, dataObj);
			} else if (actionType.equalsIgnoreCase("delete")) { //Delete an existing team
				result = deleteTeam();
			} else {
				json.put("success", false);
				json.put("message", "Unknown action. Options: add/edit/delete");
				return SUCCESS;
			}
			
			//Committing transaction if result = true
			if (result) em.getTransaction().commit();
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
	public boolean addEditTeam(String actionType, JsonObject dataObj) {
		try {
			long existingTeamId = 0;
			if (actionType.equalsIgnoreCase("edit")) {
				JsonElement teamIdInfo = dataObj.get("teamId");
				if (teamIdInfo == null) throw new CustomException("Please specify the team ID for editing!");
				else existingTeamId = teamIdInfo.getAsLong();
			}
			
			String teamName = dataObj.get("name").getAsString();
			String wiki = dataObj.get("wiki").getAsString();
			
			long supervisorId = 0;
			JsonElement supervisorInfo = dataObj.get("supervisor");
			if (supervisorInfo != null && supervisorInfo.getAsLong() != -1L) supervisorId = supervisorInfo.getAsLong();
			
			long reviewer1Id = 0;
			JsonElement reviewer1Info = dataObj.get("reviewer1");
			if (reviewer1Info != null && reviewer1Info.getAsLong() != -1L) reviewer1Id = reviewer1Info.getAsLong();
			
			long reviewer2Id = 0;
			JsonElement reviewer2Info = dataObj.get("reviewer2");
			if (reviewer2Info != null && reviewer2Info.getAsLong() != -1L) reviewer2Id = reviewer2Info.getAsLong();
			
			long termId = dataObj.get("termId").getAsLong();
			
			Type collectionType = new TypeToken<ArrayList<Long>>(){}.getType();
			ArrayList<Long> memberIds = new Gson().fromJson(dataObj.get("members"), collectionType);
		
			json = TeamManager.addEditTeam(em, teamName, wiki, termId, memberIds, supervisorId, reviewer1Id, reviewer2Id, existingTeamId);
			return true;
		} catch (CustomException e) {
			json.put("success", false);
			json.put("message", e.getMessage());
			return false;
		} catch (Exception e) {
			logger.error("Exception: " + e.getMessage());
			for (StackTraceElement s : e.getStackTrace()) {
				logger.error(s.toString());
			}
			json.put("success", false);
			json.put("message", "Oops. Something went wrong. Please try again!");
			return false;
		}
	}
	
	//Method to delete an existing team from the system
	public boolean deleteTeam() {
		return false;
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
