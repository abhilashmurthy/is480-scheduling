/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package userAction;

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
import javax.servlet.http.HttpServletRequest;
import manager.MilestoneManager;
import manager.ScheduleManager;
import model.Milestone;
import model.Schedule;
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
public class CreateScheduleAction extends ActionSupport implements ServletRequestAware {

    private HttpServletRequest request;
    static final Logger logger = LoggerFactory.getLogger(CreateScheduleAction.class);
    private HashMap<String, Object> json = new HashMap<String, Object>();

    @Override
    public String execute() throws Exception {
		EntityManager em = null;
        try {
            json.put("exception", false);
            em = Persistence.createEntityManagerFactory(MiscUtil.PERSISTENCE_UNIT).createEntityManager();
			
			//Getting input data
			JSONObject inputData = new JSONObject(request.getParameter("jsonData"));
			JSONArray milestones = inputData.getJSONArray("milestones");
			
			//Creating schedule objects for all milestones
			em.getTransaction().begin();
			ArrayList<HashMap<String, Object>> scheduleList = new ArrayList<HashMap<String, Object>>();
			for (int i = 0; i < milestones.length(); i++) {
				HashMap<String, Object> scheduleJson = new HashMap<String, Object>();
				JSONObject obj = milestones.getJSONObject(i);
				long milestoneId = obj.getLong("id");
				Milestone m = em.find(Milestone.class, milestoneId);
				if (m == null) {
					json.put("success", false);
					json.put("message", "Milestone with ID: " + milestoneId + " not found");
					return SUCCESS;
				}
				scheduleJson.put("milestoneName", m.getName());
				scheduleJson.put("duration", m.getSlotDuration());
				scheduleJson.put("dayStartTime", obj.getInt("dayStartTime"));
				scheduleJson.put("dayEndTime", obj.getInt("dayEndTime"));
				JSONArray milestoneDates = obj.getJSONArray("dates");
				ArrayList<String> dates = new ArrayList<String>();
				for (int j = 0; j < milestoneDates.length(); j++) {
					dates.add(milestoneDates.getString(j));
				}
				scheduleJson.put("dates", dates);
				Timestamp startTimestamp = Timestamp.valueOf
						(milestoneDates.getString(0) + " 00:00:00");
                Timestamp endTimestamp = Timestamp.valueOf
						(milestoneDates.getString(milestoneDates.length() - 1) + " 00:00:00");
				int dayStartTime = obj.getInt("dayStartTime");
				int dayEndTime = obj.getInt("dayEndTime");
				
				Schedule s = new Schedule();
				s.setMilestone(m);
				s.setStartDate(startTimestamp);
				s.setEndDate(endTimestamp);
				s.setDayStartTime(dayStartTime);
				s.setDayEndTime(dayEndTime);
				
				em.persist(s);
				scheduleJson.put("scheduleId", s.getId());
				scheduleList.add(scheduleJson);
			}
			em.getTransaction().commit();
			json.put("schedules", scheduleList);

            json.put("success", true);
        } catch (Exception e) {
			if (em != null && em.getTransaction().isActive()) em.getTransaction().rollback();
            logger.error("Exception caught: " + e.getMessage());
            if (MiscUtil.DEV_MODE) {
                for (StackTraceElement s : e.getStackTrace()) {
                    logger.debug(s.toString());
                }
            }
            json.put("success", false);
            json.put("message", "Error with CreateSchedule: Escalate to developers!");
        } finally {
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
        request = hsr;
    }
}
