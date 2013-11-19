/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package userAction;

import com.google.gson.Gson;
import static com.opensymphony.xwork2.Action.ERROR;
import static com.opensymphony.xwork2.Action.SUCCESS;
import com.opensymphony.xwork2.ActionSupport;
import constant.Role;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import manager.SettingsManager;
import manager.UserManager;
import model.Milestone;
import model.Settings;
import model.SystemActivityLog;
import model.Term;
import model.User;
import org.apache.struts2.interceptor.ServletRequestAware;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.MiscUtil;

/**
 *
 * @author Prakhar
 */
public class UpdateActiveTermsAction extends ActionSupport implements ServletRequestAware {

    private HttpServletRequest request;
    private static Logger logger = LoggerFactory.getLogger(UpdateActiveTermsAction.class);
	private HashMap<String, Object> json = new HashMap<String, Object>();
	private ArrayList<HashMap<String, Object>> data = new ArrayList<HashMap<String, Object>>();

    @Override
    public String execute() throws Exception {
		HttpSession session = request.getSession();
		
		Calendar nowCal = Calendar.getInstance();
		Timestamp now = new Timestamp(nowCal.getTimeInMillis());
		
		Term currentTerm = (Term) session.getAttribute("currentActiveTerm");
		
		SystemActivityLog logItem = new SystemActivityLog();
		logItem.setActivity("Administrator: Update Active Term Settings");
		logItem.setRunTime(now);
		logItem.setUser((User)session.getAttribute("user"));
		logItem.setMessage("Error with validation / No changes made");
		logItem.setSuccess(true);
		
		EntityManager em = null;
        try {
			em = MiscUtil.getEntityManagerInstance();
			User user = (User) request.getSession().getAttribute("user");
			Role activeRole = (Role) session.getAttribute("activeRole");

			if (activeRole.equals(Role.ADMINISTRATOR) || activeRole.equals(Role.COURSE_COORDINATOR)) {
				Settings activeTerms = SettingsManager.getByName(em, "activeTerms");
				
				//Getting input data from url
				JSONObject activeTermsObject = (JSONObject) new JSONObject (request.getParameter("jsonData"));
				JSONArray activeTermsArray = (JSONArray) activeTermsObject.getJSONArray("activeTerms");
				
				//Checking whether atleast 1 term has been marked active
				if (activeTermsArray.length() == 0) {
					json.put("message", "Please select atleast 1 active term!");
					json.put("success", false);
					return SUCCESS;
				}
				
				//Constructing the active terms list to be stored in db
				ArrayList<Long> activeTermIds = new ArrayList<Long>();
				for (int i = 0; i < activeTermsArray.length(); i++) {
					long id = Long.valueOf(activeTermsArray.getString(i));
					if (id < 1) {
						json.put("message", "Error! Incorrect semesters selected!");
						json.put("success", false);
						return SUCCESS;
					}
					activeTermIds.add(activeTermsArray.getLong(i));
					if (currentTerm.getId() == activeTermsArray.getLong(i)) {
						json.put("message", "Cannot set current term as inactive!");
						json.put("success", false);
						return SUCCESS;
					}
				}
				
				//Storing the active terms list in settings table in db
				em.getTransaction().begin();
				activeTerms.setValue(new Gson().toJson(activeTermIds));
				em.persist(activeTerms);
				em.getTransaction().commit();				
				json.put("success", true);
				json.put("message", "Your settings have been updated!");
				MiscUtil.logActivity(logger, user, "Active terms updated");
				
				StringBuilder logMessage = new StringBuilder();
				logMessage.append("Active Term settings were updated successfully. ");
				logItem.setMessage(logMessage.toString());
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
            json.put("message", "Error with UpdateActiveTerms: Escalate to developers!");
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
    } //end of execute

	//Getters and Setters
	public HashMap<String, Object> getJson() {
		return json;
	}

	public void setJson(HashMap<String, Object> json) {
		this.json = json;
	}

	public ArrayList<HashMap<String, Object>> getData() {
		return data;
	}

	public void setData(ArrayList<HashMap<String, Object>> data) {
		this.data = data;
	}
	
	public void setRequest(HttpServletRequest request) {
        this.request = request;
    }
	
	public void setServletRequest(HttpServletRequest hsr) {
        this.request = hsr;
    }
} //end of class