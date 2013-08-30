/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package userAction;

import com.google.gson.Gson;
import static com.opensymphony.xwork2.Action.SUCCESS;
import com.opensymphony.xwork2.ActionSupport;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import javax.persistence.EntityManager;
import javax.persistence.Persistence;
import javax.servlet.http.HttpServletRequest;
import manager.SettingsManager;
import static manager.SettingsManager.getByName;
import model.Schedule;
import model.Settings;
import model.Term;
import model.Timeslot;
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
public class CreateTimeslotsAction extends ActionSupport implements ServletRequestAware {

    private HttpServletRequest request;
    static final Logger logger = LoggerFactory.getLogger(CreateTimeslotsAction.class);
    private HashMap<String, Object> json = new HashMap<String, Object>();

    @Override
    public String execute() throws Exception {
		EntityManager em = null;
        try {
			em = Persistence.createEntityManagerFactory(MiscUtil.PERSISTENCE_UNIT).createEntityManager();

			//Getting input data
			JSONObject inputData = new JSONObject(request.getParameter("jsonData"));
			JSONArray schedules = inputData.getJSONArray("schedules");
			
			em.getTransaction().begin();
			for (int i = 0; i < schedules.length(); i++) {
				JSONObject scheduleData = schedules.getJSONObject(i);
				Schedule s = em.find(Schedule.class, scheduleData.getLong("scheduleId"));
				if (s == null) {
					json.put("success", false);
					json.put("message", "Schedule with ID: " + scheduleData.getLong("scheduleId") + " not found.");
					return SUCCESS;
				}
				JSONArray timeslotTimes = scheduleData.getJSONArray("timeslots");
				
				for (int j = 0; j < timeslotTimes.length(); j++) {
					//Getting startTime and endTime
					Timestamp startTime = Timestamp.valueOf(timeslotTimes.getString(j));
					Calendar cal = Calendar.getInstance();
					cal.setTimeInMillis(startTime.getTime());
					cal.add(Calendar.MINUTE, s.getMilestone().getSlotDuration());
					Timestamp endTime = new Timestamp(cal.getTimeInMillis());

					Timeslot t = new Timeslot();
					t.setStartTime(startTime);
					t.setEndTime(endTime);
					t.setVenue(scheduleData.getString("venue"));
					t.setSchedule(s);
					em.persist(t);
				} //End of timeslot creation loop
			}

			//Setting term for newly created schedules as active
			Schedule firstSchedule = em.find(Schedule.class, schedules.getJSONObject(0).getLong("scheduleId"));
			setTermAsActive(em, firstSchedule.getMilestone().getTerm());
			
			em.getTransaction().commit();

			json.put("success", true);
			json.put("message", "Timeslots stored successfully. Schedule creation complete!");
        } catch (Exception e) {
            logger.error("Exception caught: " + e.getMessage());
            if (MiscUtil.DEV_MODE) {
                for (StackTraceElement s : e.getStackTrace()) {
                    logger.debug(s.toString());
                }
            }
            json.put("success", false);
            json.put("message", "Error with CreateTimeslots: Escalate to developers!");
        } finally {
			if (em != null && em.getTransaction().isActive()) em.getTransaction().rollback();
			if (em != null && em.isOpen()) em.close();
		}
        return SUCCESS;
    }

	private void setTermAsActive(EntityManager em, Term newTerm) {
		Settings result = getByName(em, "activeTerms");
		ArrayList<Long> activeTermIds = new ArrayList<Long>();
		for (Term t : SettingsManager.getActiveTerms(em)) {
			activeTermIds.add(t.getId());
		}
		activeTermIds.add(newTerm.getId());
		result.setValue(new Gson().toJson(activeTermIds));
		em.persist(result);
	}

    public HashMap<String, Object> getJson() {
        return json;
    }

    public void setJson(HashMap<String, Object> json) {
        this.json = json;
    }

    public void setServletRequest(HttpServletRequest hsr) {
        request = hsr;
    }
}
