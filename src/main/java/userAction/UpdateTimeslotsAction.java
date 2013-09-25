/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package userAction;

import static com.opensymphony.xwork2.Action.SUCCESS;
import com.opensymphony.xwork2.ActionSupport;
import constant.Role;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;
import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;
import manager.TimeslotManager;
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
public class UpdateTimeslotsAction extends ActionSupport implements ServletRequestAware {

    private HttpServletRequest request;
    static final Logger logger = LoggerFactory.getLogger(UpdateTimeslotsAction.class);
    private HashMap<String, Object> json = new HashMap<String, Object>();

    @Override
    public String execute() throws Exception {
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
            JSONObject inputData = new JSONObject(request.getParameter("jsonData"));
			String venue = inputData.getString("venue");

            Schedule s = em.find(Schedule.class, inputData.getLong("scheduleId"));
            if (s == null) {
                json.put("success", false);
				json.put("message", "Oops. Something went wrong!");
                logger.error("Schedule with ID: " + inputData.getLong("scheduleId") + " not found.");
                return SUCCESS;
            }
			//Beginning overall transaction
			em.getTransaction().begin();
			
			JSONArray inputDateTimes = inputData.getJSONArray("timeslots[]");
			TreeSet<String> dateTimes = new TreeSet<String>();
			for (int i =0; i < inputDateTimes.length(); i++) {
				dateTimes.add(inputDateTimes.getString(i) + ".0");
			}
			
			Set<Timeslot> timeslots = s.getTimeslots();
			Iterator<Timeslot> iter = timeslots.iterator();
			while(iter.hasNext()) {
				Timeslot t = iter.next();
				if (!dateTimes.contains(t.getStartTime().toString())) { //Timeslot has been removed from the schedule
					if (t.getCurrentBooking() == null) { //Delete slot if there's no booking
						TimeslotManager.delete(em, t);
						iter.remove();
						dateTimes.remove(t.getStartTime().toString());
						continue;
					} else { //Abort update if there's an active booking for the timeslot
						logger.error("Timeslot[id=" + t.getId() + "] has an active booking. Cannot be removed");
						json.put("message", "Timeslot starting on " + t.getStartTime().toString() + " has an active booking. Cannot update timeslots!");
						json.put("success", false);
						return SUCCESS;
					}
				} else { //Just update the venue if required. Timeslot still exists in schedule.
					if (venue != null && !venue.isEmpty()) t.setVenue(venue);
					dateTimes.remove(t.getStartTime().toString());
				}
			}
			
			boolean ignored = false;
			Calendar schStart = Calendar.getInstance(); schStart.setTimeInMillis(s.getStartDate().getTime());
			Calendar schEnd = Calendar.getInstance(); schEnd.setTimeInMillis(s.getEndDate().getTime());
			//Create timeslot objects for newly chosen times
			for (String timestampStr : dateTimes) {
                //Getting startTime and endTime
                Timestamp startTime = Timestamp.valueOf(timestampStr);
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
                String venueToSet = (venue != null && !venue.isEmpty()) ? venue : "N/A" ;
				t.setVenue(venueToSet);
                t.setSchedule(s);
                em.persist(t);
            } //End of timeslot creation loop
			
			em.flush(); //Forcing write to database
			em.getTransaction().commit();
			
            json.put("success", true);
			String message = "Timeslots updated successfully";
			if (ignored) message += ". Certain timeslots were ignored as they breached the limits of the current schedule.";
            json.put("message", message);
            return SUCCESS;
        } catch (Exception e) {
            logger.error("Exception caught: " + e.getMessage());
            if (MiscUtil.DEV_MODE) {
                for (StackTraceElement s : e.getStackTrace()) {
                    logger.debug(s.toString());
                }
            }
            json.put("success", false);
            json.put("message", "Error with UpdateTimeslots: Escalate to developers!");
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
        request = hsr;
    }
}
