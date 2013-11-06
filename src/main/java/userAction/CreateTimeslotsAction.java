/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package userAction;

import com.google.gson.Gson;
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
import static manager.SettingsManager.getByName;
import model.Schedule;
import model.Settings;
import model.SystemActivityLog;
import model.Term;
import model.Timeslot;
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
public class CreateTimeslotsAction extends ActionSupport implements ServletRequestAware {

    private HttpServletRequest request;
    static final Logger logger = LoggerFactory.getLogger(CreateTimeslotsAction.class);
    private HashMap<String, Object> json = new HashMap<String, Object>();

    @Override
    public String execute() throws Exception {
		HttpSession session = request.getSession();
		
		Calendar nowCal = Calendar.getInstance();
		Timestamp now = new Timestamp(nowCal.getTimeInMillis());
		
		SystemActivityLog logItem = new SystemActivityLog();
		logItem.setActivity("Timeslot: Create");
		logItem.setRunTime(now);
		logItem.setUser((User)session.getAttribute("user"));
		logItem.setMessage("Error with validation / No changes made");
		logItem.setSuccess(true);
		
        EntityManager em = null;
        try {
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
            JSONObject scheduleData = new JSONObject(request.getParameter("jsonData"));

            em.getTransaction().begin();
            Schedule s = em.find(Schedule.class, scheduleData.getLong("scheduleId"));
            if (s == null) {
                json.put("success", false);
                json.put("message", "Schedule with ID: " + scheduleData.getLong("scheduleId") + " not found.");
                return SUCCESS;
            }
            JSONArray timeslotTimes = scheduleData.getJSONArray("timeslots");

			boolean ignored = false;
			Calendar schStart = Calendar.getInstance(); schStart.setTimeInMillis(s.getStartDate().getTime());
			Calendar schEnd = Calendar.getInstance(); schEnd.setTimeInMillis(s.getEndDate().getTime());
			ArrayList<Timeslot> timeslotsLog = new ArrayList<Timeslot>();
            for (int j = 0; j < timeslotTimes.length(); j++) {
                //Getting startTime and endTime
                Timestamp startTime = Timestamp.valueOf(timeslotTimes.getString(j));
				Calendar startCal = Calendar.getInstance();
				startCal.setTimeInMillis(startTime.getTime());
                Calendar endCal = Calendar.getInstance();
				endCal.setTimeInMillis(startTime.getTime());
                endCal.add(Calendar.MINUTE, s.getMilestone().getSlotDuration());
                Timestamp endTime = new Timestamp(endCal.getTimeInMillis());
				
				//Check compliance with schedule start and end dates
				if (!(startCal.compareTo(schStart) >= 0)
						|| !(endCal.compareTo(schEnd) <= 0)) {
					logger.warn("Timestamp " + startTime.toString() + " not compliant with day end time for Schedule[id=" + s.getId() + "]");
					ignored = true;
					continue;
				}
				
				//Check start time compliance with day start
				if ((startCal.get(Calendar.HOUR_OF_DAY) < s.getDayStartTime())) { //Timeslot breaches day start
					logger.warn("Timestamp " + startTime.toString() + " not compliant with day start time for Schedule[id=" + s.getId() + "]");
					ignored = true;
					continue;
				}
				
				//Check end time compliance with day end
				if ((endCal.get(Calendar.HOUR_OF_DAY) > s.getDayEndTime()) ||
					(endCal.get(Calendar.HOUR_OF_DAY) == s.getDayEndTime() && endCal.get(Calendar.MINUTE) > 0)) { //Timeslot breaches day end
					logger.warn("Timestamp " + startTime.toString() + " not compliant with day end time for Schedule[id=" + s.getId() + "]");
					ignored = true;
					continue;
				}
				
                Timeslot t = new Timeslot();
                t.setStartTime(startTime);
                t.setEndTime(endTime);
				String venue = (scheduleData.getString("venue") != null
						&& !scheduleData.getString("venue").isEmpty())
						? scheduleData.getString("venue") : "N/A" ;
                t.setVenue(venue);
                t.setSchedule(s);
                em.persist(t);
				timeslotsLog.add(t);
            } //End of timeslot creation loop

            //Setting term as active for the created schedule
            setTermAsActive(em, s.getMilestone().getTerm());
            em.getTransaction().commit();

            json.put("success", true);
            String message = "Timeslots stored successfully";
			if (ignored) message += ". Certain timeslots were ignored as they breached the limits of the current schedule.";
            json.put("message", message);
			
			StringBuilder logMessage = new StringBuilder();
			logMessage.append("Timeslots were created successfully. TimeslotId:");
			for (Timeslot tim: timeslotsLog) {
				logMessage.append(tim.getId());
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
            json.put("message", "Error with CreateTimeslots: Escalate to developers!");
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

    private void setTermAsActive(EntityManager em, Term newTerm) {
        Settings result = getByName(em, "activeTerms");
        ArrayList<Long> activeTermIds = new ArrayList<Long>();
        for (Term t : SettingsManager.getActiveTerms(em)) {
            if (t.getId() != newTerm.getId()) {
                activeTermIds.add(t.getId());
            }
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
