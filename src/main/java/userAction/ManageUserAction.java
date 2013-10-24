/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package userAction;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import static com.opensymphony.xwork2.Action.SUCCESS;
import com.opensymphony.xwork2.ActionSupport;
import constant.Role;
import java.util.HashMap;
import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;
import manager.UserManager;
import model.Team;
import model.Term;
import model.User;
import model.role.Faculty;
import model.role.Student;
import model.role.TA;
import org.apache.struts2.interceptor.ServletRequestAware;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.MiscUtil;

/**
 * JSON package action
 */
public class ManageUserAction extends ActionSupport implements ServletRequestAware {
	
	private HttpServletRequest request;
	private HashMap<String, Object> json = new HashMap<String, Object>();
	private Logger logger = LoggerFactory.getLogger(ManageUserAction.class);
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
			if (actionType.equalsIgnoreCase("add")) { //Add a new user
				addUser(dataObj);
			} else if (actionType.equalsIgnoreCase("edit")) { //Edit an existing user
				editUser();
			} else if (actionType.equalsIgnoreCase("delete")) { //Delete an existing user
				deleteUser();
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
					if (s.getClassName().equals(this.getClass().getName())) {
						logger.error(s.toString());
					}
                }
            }
            json.put("success", false);
            json.put("exception", true);
            json.put("message", "Error with ManageUsers: Escalate to developers!");
        } finally {
            if (em != null && em.getTransaction().isActive()) em.getTransaction().rollback();
            if (em != null && em.isOpen()) em.close();
        }
        return SUCCESS;
	}
	
	//Method to add a new user to the system
	public void	addUser(JsonObject dataObj) {
		try {
			//Getting the role of the user object to be created
			Role role = Role.valueOf(dataObj.get("type").getAsString());
			
			//Getting the term information if required
			Term term = null;
			//Term info not required for permanent roles: Administrator and Course Coordinator
			if (role != Role.ADMINISTRATOR && role != Role.COURSE_COORDINATOR) {
				long termId = dataObj.get("termId").getAsLong();
				term = em.find(Term.class, termId);
				if (term == null) throw new CustomException("Term not found.");
			}
			
			String username = dataObj.get("username").getAsString();
			//Checking if this combination of username and term exists
			if (UserManager.usernameExists(em, username, role, term, null)) {
				throw new CustomException("Username already exists for the selected term & role.");
			}
			String fullName = dataObj.get("fullName").getAsString();
			
			User user = null;
			if (role == Role.STUDENT) { //Create Student object
				user = new Student(username, fullName, null, term);
				Team team = null;
				JsonElement teamIdInfo = dataObj.get("teamId");
				if (teamIdInfo != null) { //Specifying team information is optional
					long teamId = teamIdInfo.getAsLong();
					team = em.find(Team.class, teamId);
					if (team == null) {
						throw new CustomException("Specified team not found.");
					}
				}
				((Student)user).setTeam(team);
			} else if (role == Role.FACULTY) {
				user = new Faculty(username, fullName, null, term);
			} else if (role == Role.TA) {
				user = new TA(username, fullName, null, term);
			} else if (role == Role.ADMINISTRATOR || role == Role.COURSE_COORDINATOR) {
				user = new User(username, fullName, null, role, term);
			} //TODO Store guest roles if needed
			
			em.persist(user);
			json.put("success", true);
			json.put("userId", user.getId());
		} catch (CustomException e) {
			json.put("success", false);
			json.put("message", e.getMessage());
		} catch (IllegalArgumentException e) {
			json.put("success", false);
			json.put("message", "Specified role not found.");
		} catch (Exception e) {
			json.put("success", false);
			json.put("message", "Oops. Something went wrong. Please try again!");
		}
	}
	
	//Method to edit an existing user in the system
	public void	editUser() {
		
	}
	
	//Method to delete an existing user from the system
	public void	deleteUser() {
		
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
	
	public class CustomException extends Exception {
		public CustomException(String string) {
			super(string);
		}
	}
	
}
