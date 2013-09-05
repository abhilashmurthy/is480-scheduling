/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package userAction;

import static com.opensymphony.xwork2.Action.SUCCESS;
import com.opensymphony.xwork2.ActionSupport;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.persistence.EntityManager;
import javax.persistence.Persistence;
import javax.servlet.http.HttpServletRequest;
import manager.MilestoneManager;
import manager.ScheduleManager;
import manager.TimeslotManager;
import model.Booking;
import model.Milestone;
import model.Schedule;
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
public class UpdateScheduleAction extends ActionSupport implements ServletRequestAware {

    private HttpServletRequest request;
    static final Logger logger = LoggerFactory.getLogger(UpdateScheduleAction.class);
    private HashMap<String, Object> json = new HashMap<String, Object>();

    @Override
    public String execute() throws Exception {
        EntityManager em = null;
        try {
            json.put("exception", false);
            em = Persistence.createEntityManagerFactory(MiscUtil.PERSISTENCE_UNIT).createEntityManager();

            //Getting input data
            JSONObject inputData = new JSONObject(request.getParameter("jsonData"));
            JSONArray milestones = inputData.getJSONArray("milestones[]");

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
                
                long scheduleId = obj.getLong("scheduleId");
                Schedule s = em.find(Schedule.class, scheduleId);
                s.setStartDate(startTimestamp);
                s.setEndDate(endTimestamp);
                s.setDayStartTime(dayStartTime);
                s.setDayEndTime(dayEndTime);
                em.merge(s);
                
                Calendar cal = Calendar.getInstance();
                SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
                
                //Timeslot Management
                List<Timeslot> timeslots = TimeslotManager.findBySchedule(em, s);
                oldTimeslots:
                for (Timeslot t : timeslots) {
                    Timestamp oldStartTime = t.getStartTime();
                    if (t.getCurrentBooking() != null) {
                        continue oldTimeslots;
                    }
                    //Check if timeslots already exist
                   JSONArray newDates = obj.getJSONArray("dates[]");
                   for (int k = 0; k < newDates.length(); k++) {
                       String date = newDates.getString(k);
                       cal.set(Calendar.HOUR_OF_DAY, dayStartTime);
                       Timestamp currentTimestamp = Timestamp.valueOf(date + " " + timeFormat.format(cal.getTime()));
                       cal.set(Calendar.HOUR_OF_DAY, dayEndTime);
                       Timestamp dayEndTimestamp = Timestamp.valueOf(date + " " + timeFormat.format(cal.getTime()));
                       while (currentTimestamp.before(dayEndTimestamp)) {
                           if (currentTimestamp.equals(oldStartTime)) {
                               continue oldTimeslots;
                           }
                           cal.setTimeInMillis(currentTimestamp.getTime());
                           cal.add(Calendar.MINUTE, m.getSlotDuration());
                           currentTimestamp = Timestamp.valueOf(dateTimeFormat.format(cal.getTime()));
                       }
                       //New timeslot not found in old timeslots
                       //Add all new timeslots
                       cal.set(Calendar.HOUR_OF_DAY, dayStartTime);
                       currentTimestamp = Timestamp.valueOf(date + " " + timeFormat.format(cal.getTime()));
                       int currentTime = dayStartTime;
                       int endTime = dayEndTime;
                       while (currentTimestamp.before(dayEndTimestamp)) {
                           Timestamp startTimeslotTimestamp = currentTimestamp;
                           cal.setTimeInMillis(startTimeslotTimestamp.getTime());
                           cal.add(Calendar.MINUTE, m.getSlotDuration());
                           Timestamp endTimeslotTimestamp = new Timestamp(cal.getTimeInMillis());
                           
                           Timeslot newT = new Timeslot();
                           newT.setStartTime(startTimeslotTimestamp);
                           newT.setEndTime(endTimeslotTimestamp);
                           newT.setVenue(t.getVenue());
                           newT.setSchedule(s);
                           em.persist(newT);
                           cal.add(Calendar.MINUTE, m.getSlotDuration());
                           currentTimestamp = Timestamp.valueOf(dateTimeFormat.format(cal.getTime()));
                       }
                    }
                    //Can delete old timeslot here
                    em.remove(t);
                }
                
                scheduleJson.put("scheduleId", scheduleId);
                scheduleList.add(scheduleJson);
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
            json.put("message", "Error with UpdateSchedule: Escalate to developers!");
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
