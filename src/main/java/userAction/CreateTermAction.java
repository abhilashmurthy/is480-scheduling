/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package userAction;

import static com.opensymphony.xwork2.Action.SUCCESS;
import com.opensymphony.xwork2.ActionSupport;
import java.util.ArrayList;
import java.util.HashMap;
import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;
import model.Term;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.persistence.Persistence;
import manager.SettingsManager;
import manager.TermManager;
import model.Milestone;
import model.Settings;
import org.apache.struts2.interceptor.ServletRequestAware;
import org.json.JSONArray;
import org.json.JSONObject;
import util.MiscUtil;

/**
 *
 * @author Prakhar
 */
public class CreateTermAction extends ActionSupport implements ServletRequestAware {

    private HttpServletRequest request;
    static final Logger logger = LoggerFactory.getLogger(CreateTermAction.class);
    private HashMap<String, Object> json = new HashMap<String, Object>();

    @Override
    public String execute() throws Exception {
		EntityManager em = null;
        try {
            json.put("exception", false);
            em = Persistence.createEntityManagerFactory(MiscUtil.PERSISTENCE_UNIT).createEntityManager();
			
			//Getting input data
			JSONObject inputData = new JSONObject(request.getParameter("jsonData"));
			int year = inputData.getInt("year");
			String semester = inputData.getString("semester");
			
			//Checking if the term already exists
            Term existingTerm = TermManager.findByYearAndSemester(em, year, semester);
            if (existingTerm != null) {
                logger.error("Term already exists");
                json.put("message", "Term already exists");
				json.put("success", false);
                return SUCCESS;
            }
            
			//Creating term and milestones in DB
            if (year != 0 && semester != null && !semester.equals("")) {
                em.getTransaction().begin();
                Term newTerm = new Term(year, semester);
                em.persist(newTerm);
				json.put("termId", newTerm.getId());
				
				//Creating milestones
				Settings milestoneSettings = SettingsManager.getMilestoneSettings(em);
				JSONArray milestoneArray = new JSONArray(milestoneSettings.getValue());
				ArrayList<HashMap<String, Object>> list = new ArrayList<HashMap<String, Object>>();
				
				for (int i = 0; i < milestoneArray.length(); i++) {
					HashMap<String, Object> milestoneInfo = new HashMap<String, Object>();
					JSONObject obj = milestoneArray.getJSONObject(i);
					String name = obj.getString("name");
					int duration = obj.getInt("duration");
					JSONArray reqArray = obj.getJSONArray("requiredAttendees");
					ArrayList<String> requiredAttendees = new ArrayList<String>();
					for (int j = 0; j < reqArray.length(); j++) { requiredAttendees.add(reqArray.getString(j)); }
					
					Milestone m = new Milestone(name, duration, newTerm, requiredAttendees);
					em.persist(m);
					milestoneInfo.put("name", m.getName());
					milestoneInfo.put("id", m.getId());
					list.add(milestoneInfo);
				}
				json.put("milestones", list);
				
				em.getTransaction().commit();
                json.put("message", "Term and milestones created");
                json.put("success", true);
            } else {
                json.put("message", "Term information provided incorrect.");
                json.put("success", false);
            }
            
        } catch (Exception e) {
            logger.error("Exception caught: " + e.getMessage());
            if (MiscUtil.DEV_MODE) {
                for (StackTraceElement s : e.getStackTrace()) {
                    logger.debug(s.toString());
                }
            }
            json.put("exception", true);
            json.put("message", "Error with CheckTerm: Escalate to developers!");
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
}
