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
import java.util.ArrayList;
import java.util.HashMap;
import javax.persistence.EntityManager;
import javax.persistence.Persistence;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import manager.SettingsManager;
import model.Settings;
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
		EntityManager em = null;
        try {
			em = Persistence.createEntityManagerFactory(MiscUtil.PERSISTENCE_UNIT).createEntityManager();
			HttpSession session = request.getSession();
			Role activeRole = (Role) session.getAttribute("activeRole");

			if (activeRole.equals(Role.ADMINISTRATOR) || activeRole.equals(Role.COURSE_COORDINATOR)) {
				Settings activeTerms = SettingsManager.getByName(em, "activeTerms");
				Settings defaultTerm = SettingsManager.getByName(em, "defaultTerm");
				
				//Getting input data from url
				JSONObject activeTermsObject = (JSONObject) new JSONObject (request.getParameter("jsonData"));
				JSONArray activeTermsArray = (JSONArray) activeTermsObject.getJSONArray("activeTerms");
				
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
				}
				
				//Now setting the default active term
				long defaultActiveTermId = Long.valueOf(activeTermsObject.getString("defaultTerm"));
				
				if (activeTermIds.size() > 0) {
					if (!activeTermIds.contains(defaultActiveTermId)) {
						json.put("message", "Error! Incorrect semesters selected!");
						json.put("success", false);
						return SUCCESS;
					}
				}
				
				//Storing the active terms list in settings table in db
				em.getTransaction().begin();
				activeTerms.setValue(new Gson().toJson(activeTermIds));
				defaultTerm.setValue(String.valueOf(defaultActiveTermId));
				em.persist(activeTerms);
				em.persist(defaultTerm);
				em.getTransaction().commit();
				
				json.put("success", true);
				json.put("message", "Your settings have been updated!");

			} else {
				request.setAttribute("error", "Oops. You're not authorized to access this page!");
				logger.error("User cannot access this page");
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
            json.put("message", "Error with UpdateActiveTerms: Escalate to developers!");
        } finally {
			if (em != null && em.getTransaction().isActive()) em.getTransaction().rollback();
			if (em != null && em.isOpen()) em.close();
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