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
import java.util.Date;
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
                
                /************************
                  SCHEDULE MANAGEMENT 
                ************************/
                
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

                
                /************************
                  TIMESLOT MANAGEMENT 
                ************************/
                Calendar cal = Calendar.getInstance();
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
                List<Timeslot> oldTimeslots = TimeslotManager.findBySchedule(em, s);

                //Delete unwanted acceptance timeslots
                Map<Timestamp, Timestamp> keepTimestamps = new HashMap<Timestamp, Timestamp>();
                keepTimestamps:
                for (Timeslot t : oldTimeslots) {
                    Timestamp startTimeslotTimestamp = t.getStartTime();
                    Timestamp endTimeslotTimestamp = t.getEndTime();
                    if (t.getCurrentBooking() != null) {
                        keepTimestamps.put(startTimeslotTimestamp, endTimeslotTimestamp);
                        continue keepTimestamps;
                    }
                    logger.debug("Got timeslot: " + t.getId() + ", " + t.getStartTime());
                    //Filters out dates without timeslots
                    for (int j = 0; j < milestoneDates.length(); j++) {
                        String newDate = milestoneDates.getString(j);
                        cal.clear();
                        cal.set(Calendar.HOUR_OF_DAY, dayStartTime);
                        Timestamp currentDayTimestamp = Timestamp.valueOf(newDate + " " + timeFormat.format(cal.getTime()));
                        cal.set(Calendar.HOUR_OF_DAY, dayEndTime);
                        Timestamp endDayTimestamp = Timestamp.valueOf(newDate + " " + timeFormat.format(cal.getTime()));
                        while (currentDayTimestamp.before(endDayTimestamp)) {
                            cal.clear();
                            cal.setTimeInMillis(currentDayTimestamp.getTime());
                            cal.add(Calendar.MINUTE, m.getSlotDuration());
                            Timestamp nextTimeslotTimestamp = new Timestamp(cal.getTimeInMillis());
                            if ((startTimeslotTimestamp.equals(currentDayTimestamp) && endTimeslotTimestamp.equals(nextTimeslotTimestamp)) && (endTimeslotTimestamp.before(endDayTimestamp) || endTimeslotTimestamp.equals(endDayTimestamp))) {
                                keepTimestamps.put(startTimeslotTimestamp, endTimeslotTimestamp);
                                continue keepTimestamps;
                            }
                            currentDayTimestamp = nextTimeslotTimestamp;
                        }
                    }
                    //If this is reached, the timeslot doesn't exist. remove it completely.
                    logger.debug("Removing timeslot: " + t.getId() + ", " + t.getStartTime());
                    if (!TimeslotManager.delete(em, t, em.getTransaction())) { //Remove from database
                        throw new Exception("Unable to delete: " + t);
                    }
                }

                //Add new Timeslots -- WAIT SHOULD WE ADD NEW TIMESLOTS FOR ALL NEW DATES SELECTED??
                logger.debug("Adding timeslots now");
                logger.debug("These are the new dates: " + milestoneDates.toString());
                for (int j = 0; j < milestoneDates.length(); j++) {
                    //Add new Dates
                    String newDate = milestoneDates.getString(j);
                    cal.clear();
                    cal.set(Calendar.HOUR_OF_DAY, dayStartTime);
                    Timestamp currentDayTimestamp = Timestamp.valueOf(newDate + " " + timeFormat.format(cal.getTime()));
                    cal.set(Calendar.HOUR_OF_DAY, dayEndTime);
                    Timestamp endDayTimestamp = Timestamp.valueOf(newDate + " " + timeFormat.format(cal.getTime()));
                    logger.debug("Day start: " + currentDayTimestamp + ", Day end: " + endDayTimestamp);
                    newTimestamps: 
                    while (currentDayTimestamp.before(endDayTimestamp)) {
                        Timestamp endTimeslotTimestamp = keepTimestamps.get(currentDayTimestamp);
                        for (Timestamp keepTimestamp : keepTimestamps.keySet()) {
                            if (keepTimestamp.equals(currentDayTimestamp)) {
                                logger.debug("Keeping: " + currentDayTimestamp);
                                currentDayTimestamp = endTimeslotTimestamp; //Continue while loop
                                continue newTimestamps;
                            }
                        }
                        //This must be a new timeslot -- Add it
                        if (endTimeslotTimestamp.before(endDayTimestamp) || endTimeslotTimestamp.equals(endDayTimestamp)) { //Add only fitting timeslots
                            Timeslot newT = new Timeslot();
                            newT.setStartTime(currentDayTimestamp);
                            newT.setEndTime(endTimeslotTimestamp);
                            logger.debug("Persiting start: " + currentDayTimestamp + ", end: " + endTimeslotTimestamp);
                            newT.setVenue("Not set yet");
                            newT.setSchedule(s);
                            logger.debug("Adding timeslot: " + currentDayTimestamp);
                            em.persist(newT);
                        }
                        currentDayTimestamp = endTimeslotTimestamp; //Continue while loop
                    }
                }
                logger.debug("Finished adding");

                scheduleJson.put("scheduleId", scheduleId);
                scheduleList.add(scheduleJson);
            }
            em.getTransaction().commit();
            json.put("schedules", scheduleList);
            json.put("success", true);
        } catch (Exception e) {
            logger.error("Exception caught: " + e.getClass().getName() + " " + e.getMessage());
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
