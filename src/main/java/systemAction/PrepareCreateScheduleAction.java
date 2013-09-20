/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package systemAction;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import userAction.*;
import static com.opensymphony.xwork2.Action.SUCCESS;
import com.opensymphony.xwork2.ActionSupport;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.persistence.EntityManager;
import javax.persistence.Persistence;
import javax.persistence.Query;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import manager.MilestoneManager;
import manager.ScheduleManager;
import manager.SettingsManager;
import model.Milestone;
import model.Schedule;
import model.Settings;
import model.Term;
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
public class PrepareCreateScheduleAction extends ActionSupport implements ServletRequestAware {
    private HttpServletRequest request;
    static final Logger logger = LoggerFactory.getLogger(PrepareCreateScheduleAction.class);
	
	//Struts variables
	String milestoneJson;
	String termNameJson;

    @Override
    public String execute() throws Exception {
        EntityManager em = null;
		Gson gson = new GsonBuilder().disableHtmlEscaping().create();
        try {
            em = MiscUtil.getEntityManagerInstance();
			em.getTransaction().begin();
			
			//Get milestones
			Settings milestoneSettings = SettingsManager.getMilestoneSettings(em);
			JSONArray milestoneArray = new JSONArray(milestoneSettings.getValue());
			ArrayList<HashMap<String, Object>> milestoneList = new ArrayList<HashMap<String, Object>>();
			for (int i = 0; i < milestoneArray.length(); i++) {
				HashMap<String, Object> milestoneInfo = new HashMap<String, Object>();
				JSONObject obj = milestoneArray.getJSONObject(i);
				String name = obj.getString("milestone");
				int duration = obj.getInt("duration");
				int order = obj.getInt("order");
				JSONArray reqArray = obj.getJSONArray("attendees");
				ArrayList<String> requiredAttendees = new ArrayList<String>();
				for (int j = 0; j < reqArray.length(); j++) {
					requiredAttendees.add(reqArray.getString(j));
				}
				milestoneInfo.put("name", name);
				milestoneInfo.put("duration", duration);
				milestoneInfo.put("order", order);
				milestoneInfo.put("attendees", requiredAttendees);
				milestoneList.add(milestoneInfo);
			}
			milestoneJson = gson.toJson(milestoneList);
			
			//Get Term Names
			Query q = em.createQuery("select t from Term t");
			List<Term> terms = q.getResultList();
			List<HashMap<String, Object>> termsMap = new ArrayList<HashMap<String, Object>>();
			for (Term t : terms) {
				HashMap termMap = new HashMap();
				termMap.put("year", t.getAcademicYear());
				termMap.put("term", t.getSemester());
				termsMap.add(termMap);
			}
			termNameJson = gson.toJson(termsMap);
			
			return SUCCESS;
        } catch (Exception e) {
            logger.error("Exception caught: " + e.getMessage());
            if (MiscUtil.DEV_MODE) {
                for (StackTraceElement s : e.getStackTrace()) {
                    logger.debug(s.toString());
                }
            }
        } finally {
            if (em != null && em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            if (em != null && em.isOpen()) {
                em.close();
            }
        }
        return SUCCESS;
    }
	
	public String getMilestoneJson() {
		return milestoneJson;
	}

	public void setMilestoneJson(String milestoneJson) {
		this.milestoneJson = milestoneJson;
	}

	public String getTermNameJson() {
		return termNameJson;
	}

	public void setTermNameJson(String termNameJson) {
		this.termNameJson = termNameJson;
	}

    public void setServletRequest(HttpServletRequest hsr) {
        request = hsr;
    }
}
