/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package userAction;

import static com.opensymphony.xwork2.Action.SUCCESS;
import com.opensymphony.xwork2.ActionSupport;
import constant.Role;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;
import manager.SettingsManager;
import manager.TermManager;
import model.Milestone;
import model.Schedule;
import model.Settings;
import model.Term;
import org.apache.struts2.interceptor.ServletRequestAware;
import org.json.JSONArray;
import org.json.JSONException;
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
            em = MiscUtil.getEntityManagerInstance();
			
			//Checking user role
			Role activeRole = (Role) request.getSession().getAttribute("activeRole");
			if (activeRole != Role.ADMINISTRATOR && activeRole != Role.COURSE_COORDINATOR) {
				logger.error("Unauthorized user");
				json.put("message", "You do not have the permission to perform this function!");
				json.put("success", false);
				return SUCCESS;
			}

            //Getting input data
            JSONObject inputData = new JSONObject(request.getParameter("jsonData"));
			int year = inputData.getInt("year");
            String semester = inputData.getString("semester");
			JSONArray scheduleData = inputData.getJSONArray("milestones[]");
			
			//Beginning database transaction
			em.getTransaction().begin();
			
			//Validating information and creating milestone objects
			ArrayList<Milestone> milestones = createMilestones(em, year, semester);
			
			if (milestones == null) {
				return SUCCESS;
			}
			
			//Return error if schedule data is not enough for the number of milestones created
			if (milestones.size() != scheduleData.length()) {
				logger.error("Term already exists");
				json.put("message", "Term already exists");
				json.put("success", false);
				return SUCCESS;
			}

            //Creating schedule objects for all milestones
            ArrayList<HashMap<String, Object>> scheduleList = createSchedules(em, milestones, scheduleData);
			
			if (scheduleList == null) {
				return SUCCESS;
			}
			
            em.getTransaction().commit();
            json.put("schedules", scheduleList);
            json.put("success", true);
        } catch (Exception e) {
            logger.error("Exception caught: " + e.getMessage());
            if (MiscUtil.DEV_MODE) {
                for (StackTraceElement s : e.getStackTrace()) {
                    logger.debug(s.toString());
                }
            }
            json.put("success", false);
            json.put("message", "Error with CreateSchedule: Escalate to developers!");
        } finally {
            if (em != null && em.getTransaction().isActive()) em.getTransaction().rollback();
			if (em != null && em.isOpen()) em.close();
        }
        return SUCCESS;
    }
	
	private ArrayList<Milestone> createMilestones(EntityManager em, int year, String semester) throws JSONException{
		ArrayList<Milestone> createdMilestones = new ArrayList<Milestone>();
		//Checking if the term already exists
		Term existingTerm = TermManager.findByYearAndSemester(em, year, semester);
		if (existingTerm != null) {
			logger.error("Term already exists");
			json.put("message", "Term already exists");
			json.put("success", false);
			return null;
		}

		//Creating term and milestones in DB
		if (year != 0 && semester != null && !semester.equals("")) {
			Term newTerm = new Term(year, semester);
			em.persist(newTerm);
			json.put("termId", newTerm.getId());

			//Creating milestones
			Settings milestoneSettings = SettingsManager.getMilestoneSettings(em);
			JSONArray milestoneArray = new JSONArray(milestoneSettings.getValue());

			for (int i = 0; i < milestoneArray.length(); i++) {
				JSONObject obj = milestoneArray.getJSONObject(i);
				String name = obj.getString("milestone");
				int duration = obj.getInt("duration");
				int order = obj.getInt("order");
				
				JSONArray reqArray = obj.getJSONArray("attendees");
				ArrayList<String> requiredAttendees = new ArrayList<String>();
				for (int j = 0; j < reqArray.length(); j++) {
					requiredAttendees.add(reqArray.getString(j));
				}

				Milestone m = new Milestone(order, name, duration, newTerm, requiredAttendees);
				em.persist(m);
				createdMilestones.add(m);
			}
		} else {
			logger.error("Insufficient information");
			json.put("message", "Please check the details provided!");
			json.put("success", false);
			return null;
		}
		
		return createdMilestones;
	}
	
	private ArrayList<HashMap<String, Object>> createSchedules(
			EntityManager em, ArrayList<Milestone> milestones, JSONArray scheduleData) throws JSONException {
		ArrayList<HashMap<String, Object>> scheduleList = new ArrayList<HashMap<String, Object>>();
		
		for (int i = 0; i < scheduleData.length(); i++) {
			JSONObject obj = scheduleData.getJSONObject(i);
			Milestone m = findMilestone(obj.getInt("order"), milestones);
			if (m == null) {
				logger.error("Milestone with ID: " + obj.getInt("milestoneOrder") + " not found");
				json.put("message", "Milestone not found");
				json.put("success", false);
				return null;
			}
			
			HashMap<String, Object> scheduleJson = new HashMap<String, Object>();
			scheduleJson.put("milestoneName", m.getName());
			scheduleJson.put("duration", m.getSlotDuration());
			scheduleJson.put("dayStartTime", obj.getInt("dayStartTime"));
			scheduleJson.put("dayEndTime", obj.getInt("dayEndTime"));
			
			JSONArray milestoneDates = obj.getJSONArray("dates[]");
			ArrayList<String> dates = new ArrayList<String>();
			for (int j = 0; j < milestoneDates.length(); j++) {
				dates.add(milestoneDates.getString(j));
			}
			scheduleJson.put("dates", dates);
			
			Timestamp startTimestamp = Timestamp.valueOf(milestoneDates.getString(0) + " 00:00:00");
			Timestamp endTimestamp = Timestamp.valueOf(milestoneDates.getString(milestoneDates.length() - 1) + " 00:00:00");
			if (startTimestamp.after(endTimestamp)) { //Checking if the start time is after the end time for a milestone
				logger.error("Start time after end time!");
				json.put("message", "Start time is after the end time for a milestone!");
				json.put("success", false);
				return null;
			}
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
		
		return scheduleList;
	}
	
	private Milestone findMilestone(int order, ArrayList<Milestone> milestones) {
		for (Milestone m : milestones) {
			if (m.getMilestoneOrder() == order) {
				return m;
			}
		}
		return null;
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
