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
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.servlet.http.HttpServletRequest;
import manager.TimeslotManager;
import model.Schedule;
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
public class UpdateScheduleAction extends ActionSupport implements ServletRequestAware {

    private HttpServletRequest request;
    static final Logger logger = LoggerFactory.getLogger(UpdateScheduleAction.class);
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
			
			//Getting the currently active term
			Term activeTerm = (Term) request.getSession().getAttribute("currentActiveTerm");
			
            //Getting input data
            JSONObject inputData = new JSONObject(request.getParameter("jsonData"));
            String newSemesterName = inputData.getString("semester");
			JSONArray scheduleData = inputData.getJSONArray("schedules");

            //Beginning overall transaction
            em.getTransaction().begin();
			
			//Updating term name
			boolean termUpdated = updateTerm(em, activeTerm, newSemesterName);
			
			if (!termUpdated) {
				return SUCCESS;
			}
			
            ArrayList<HashMap<String, Object>> scheduleList = new ArrayList<HashMap<String, Object>>();
			ArrayList<Schedule> updatedSchedules = new ArrayList<Schedule>();
			for (int i = 0; i < scheduleData.length(); i++) {
				JSONObject schData = scheduleData.getJSONObject(i);
				Schedule updatedSch = em.find(Schedule.class, schData.getLong("scheduleId"));
				if (updatedSch == null) {
					logger.error("Schedule with ID: " + schData.getLong("scheduleId") + " not found.");
					json.put("message", "Oops! Something went wrong");
					json.put("success", false);
					return SUCCESS;
				}
				
				LinkedHashSet<String> newDates = new LinkedHashSet<String>();
				JSONArray chosenDates = schData.getJSONArray("dates[]");
				for (int j = 0; j < chosenDates.length(); j++) {
					newDates.add(chosenDates.getString(j));
				}
				
				int newDayStart = schData.getInt("dayStartTime");
				int newDayEnd = schData.getInt("dayEndTime");
				
				Set<Timeslot> currentTimeslots = updatedSch.getTimeslots();
				Iterator<Timeslot> iter = currentTimeslots.iterator();
				while (iter.hasNext()) { //Clean up and verify timeslots based on new info provided (dates, start/end times)
					Timeslot t = iter.next();
					String tDate = t.getStartTime().toString().split(" ")[0];
					if (newDates.contains(tDate)) { //Day still exists in the schedule
						//Checking start time
						Calendar tStartTime = Calendar.getInstance();
						tStartTime.setTimeInMillis(t.getStartTime().getTime());
						if ((tStartTime.get(Calendar.HOUR_OF_DAY) < newDayStart)) { //Timeslot breaches new limits
							if (t.getCurrentBooking() == null) { //Delete slot if there's no booking
								TimeslotManager.delete(em, t);
								iter.remove();
								continue;
							} else { //Abort update if there's an active booking for the timeslot
								logger.error("Timeslot[id=" + t.getId() + " has an active booking. Cannot be removed");
								json.put("message", "One or more active bookings will be affected. Cannot update schedule!");
								json.put("success", false);
								return SUCCESS;
							}
						}
						
						//Checking end time
						Calendar tEndTime = Calendar.getInstance();
						tEndTime.setTimeInMillis(t.getEndTime().getTime());
						if ((tEndTime.get(Calendar.HOUR_OF_DAY) > newDayEnd) ||
							(tEndTime.get(Calendar.HOUR_OF_DAY) == newDayEnd && tEndTime.get(Calendar.MINUTE) > 0)) { //Timeslot breaches new limits
							if (t.getCurrentBooking() == null) { //Delete slot if there's no booking
								TimeslotManager.delete(em, t);
								iter.remove();
								continue;
							} else { //Abort update if there's an active booking for the timeslot
								logger.error("Timeslot[id=" + t.getId() + " has an active booking. Cannot be removed");
								json.put("message", "One or more active bookings will be affected. Cannot update schedule!");
								json.put("success", false);
								return SUCCESS;
							}
						}
					} else { //Day has been removed from the schedule
						if (t.getCurrentBooking() == null) { //Delete slot if there's no booking
							TimeslotManager.delete(em, t);
							iter.remove();
							continue;
						} else { //Abort update if there's an active booking for the timeslot
							logger.error("Timeslot[id=" + t.getId() + " has an active booking. Cannot be removed");
							json.put("message", "One or more active bookings will be affected. Cannot update schedule!");
							json.put("success", false);
							return SUCCESS;
						}
					}
				}
				
				//Timeslot updates completed. Move on to check overall schedule object
				Timestamp startTimestamp = Timestamp.valueOf(chosenDates.getString(0) + " 00:00:00");
				Timestamp endTimestamp = Timestamp.valueOf(chosenDates.getString(chosenDates.length() - 1) + " 00:00:00");
				//Check for date overlap
				//Check if the current schedule dates overlap with any previous schedules in the same term
				if (!updatedSchedules.isEmpty()) {
					for (Schedule prevSch : updatedSchedules) {
						if (prevSch.getEndDate().compareTo(startTimestamp) >= 0) {
							logger.error(startTimestamp.toString()
									+ " is before the end date of Schedule[id="
									+ prevSch.getId()
									+ ", milestone=" + prevSch.getMilestone().getName()
									+ "], " + prevSch.getEndDate());
							json.put("message", "Schedules have overlapping dates! Please choose the correct dates.");
							json.put("success", false);
							return SUCCESS;
						}
					}
				}
				//All clear. Updating information and storing in database
				updatedSch.setStartDate(startTimestamp);
				updatedSch.setEndDate(endTimestamp);
				updatedSch.setDayStartTime(newDayStart);
				updatedSch.setDayEndTime(newDayEnd);
				updatedSchedules.add(updatedSch);
			}
			em.flush();
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
	
	private boolean updateTerm(EntityManager em, Term activeTerm, String semester) {
		Query q = em.createQuery("select t from Term t where t.academicYear = :year AND t.semester = :semester AND t NOT IN (:term)")
				.setParameter("year", activeTerm.getAcademicYear())
				.setParameter("semester", semester)
				.setParameter("term", activeTerm);
		List<Term> terms = q.getResultList();
		//Checking if the term already exists
		if (!terms.isEmpty()) {
			logger.error("Term already exists");
			json.put("message", "Term already exists");
			json.put("success", false);
			return false;
		}
		
		//No conflict. Updating semester details
		Term updatedTerm = em.find(Term.class, activeTerm.getId());
		updatedTerm.setSemester(semester);
		em.persist(updatedTerm);
		
		//Refreshing term object in session
		request.getSession().setAttribute("currentActiveTerm", updatedTerm);
		
		return true;
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
