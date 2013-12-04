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
import java.util.Calendar;
import java.util.HashMap;
import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import manager.SettingsManager;
import manager.TermManager;
import model.Milestone;
import model.Schedule;
import model.Settings;
import model.SystemActivityLog;
import model.Term;
import model.User;
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
		HttpSession session = request.getSession();
		
		Calendar nowCal = Calendar.getInstance();
		Timestamp now = new Timestamp(nowCal.getTimeInMillis());
		
		SystemActivityLog logItem = new SystemActivityLog();
		logItem.setActivity("Schedule: Create");
		logItem.setRunTime(now);
		logItem.setUser((User)session.getAttribute("user"));
		logItem.setMessage("Error with validation / No changes made");
		logItem.setSuccess(true);
		
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
			
			StringBuilder logMessage = new StringBuilder();
			logMessage.append("Schedule was created successfully. ScheduleId:");
			for (HashMap<String, Object> map: scheduleList) {
				logMessage.append(map.get("scheduleId"));
				logMessage.append(",");
			}
			logMessage.append(" MilestoneId:");
			for (Milestone mil: milestones) {
				logMessage.append(mil.getId());
				logMessage.append(",");
			}
			logItem.setMessage(logMessage.toString());
			
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
            json.put("message", "Error with CreateSchedule: Escalate to developers!");
        } finally {
             if (em != null) {
				if (em.getTransaction().isActive()) em.getTransaction().rollback();
                //Saving job log in database
                if (!em.getTransaction().isActive()) em.getTransaction().begin();
                em.persist(logItem);
                em.getTransaction().commit();
                if (em.isOpen()) em.close();
			}
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
				int milestoneOrder = obj.getInt("order");
				
				JSONArray reqArray = obj.getJSONArray("attendees");
				ArrayList<String> requiredAttendees = new ArrayList<String>();
				for (int j = 0; j < reqArray.length(); j++) {
					requiredAttendees.add(reqArray.getString(j));
				}

				Milestone m = new Milestone(milestoneOrder, name, duration, newTerm, requiredAttendees);
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
		
		ArrayList<Schedule> createdSchedules = new ArrayList<Schedule>();
		for (int i = 0; i < scheduleData.length(); i++) {
			JSONObject obj = scheduleData.getJSONObject(i);
			Milestone m = findMilestone(obj.getInt("milestoneOrder"), milestones);
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
			scheduleJson.put("bookable", obj.getBoolean("bookable"));
			
			JSONArray milestoneDates = obj.getJSONArray("dates[]");
			ArrayList<String> dates = new ArrayList<String>();
			for (int j = 0; j < milestoneDates.length(); j++) {
				dates.add(milestoneDates.getString(j));
			}
			scheduleJson.put("dates", dates);
			
			Timestamp startTimestamp = Timestamp.valueOf(milestoneDates.getString(0) + " 00:00:00");
			Timestamp endTimestamp = Timestamp.valueOf(milestoneDates.getString(milestoneDates.length() - 1) + " 00:00:00");
			int dayStartTime = obj.getInt("dayStartTime");
			int dayEndTime = obj.getInt("dayEndTime");
			boolean bookable = obj.getBoolean("bookable");
			
			//Check if the current schedule dates overlap with any previous schedules in the same term
			if (!createdSchedules.isEmpty()) {
				for (Schedule prevSch : createdSchedules) {
					if (prevSch.getEndDate().compareTo(startTimestamp) >= 0) {
						logger.error(startTimestamp.toString()
								+ " is before the end date of Schedule[id="
								+ prevSch.getId()
								+ ", milestone=" + prevSch.getMilestone().getName()
								+ "], " + prevSch.getEndDate());
						json.put("message", "Schedules have overlapping dates! Please choose the correct dates.");
						json.put("success", false);
						return null;
					}
				}
			}

			Schedule s = new Schedule();
			s.setMilestone(m);
			s.setStartDate(startTimestamp);
			//Calculating the real end of the schedule
			Calendar endCal = Calendar.getInstance();
			endCal.setTimeInMillis(endTimestamp.getTime());
			endCal.add(Calendar.DAY_OF_MONTH, 1); //Adding a day
			endCal.add(Calendar.MINUTE, - (s.getMilestone().getSlotDuration())); //Subtracting the slot duration
			endTimestamp.setTime(endCal.getTimeInMillis());
			s.setEndDate(endTimestamp);
			s.setDayStartTime(dayStartTime);
			s.setDayEndTime(dayEndTime);
			s.setBookable(bookable);

			em.persist(s);
			createdSchedules.add(s);
			scheduleJson.put("scheduleId", s.getId());
			scheduleList.add(scheduleJson);
		}
		
		return scheduleList;
	}
	
	private Milestone findMilestone(int milestoneOrder, ArrayList<Milestone> milestones) {
		for (Milestone m : milestones) {
			if (m.getMilestoneOrder() == milestoneOrder) {
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
