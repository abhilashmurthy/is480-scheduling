/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package userAction;

import com.opensymphony.xwork2.ActionSupport;
import constant.Role;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.persistence.EntityManager;
import javax.persistence.Persistence;
import javax.servlet.http.HttpServletRequest;
import manager.ScheduleManager;
import model.Milestone;
import model.Schedule;
import model.Timeslot;
import model.User;
import model.role.Faculty;
import org.apache.struts2.interceptor.ServletRequestAware;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.MiscUtil;

/**
 *
 * @author suresh
 */
public class SetAvailabilityAction extends ActionSupport implements ServletRequestAware {

    private static Logger logger = LoggerFactory.getLogger(SetAvailabilityAction.class);
    private HttpServletRequest request;
    private HashMap<String, Object> json = new HashMap<String, Object>();

    @Override
    public String execute() throws Exception {
        EntityManager em = null;
        try {
            em = MiscUtil.getEntityManagerInstance();
			em.getTransaction().begin();
            User user = (User) request.getSession().getAttribute("user");
            if (user.getRole() != Role.FACULTY) {
                json.put("success", false);
                json.put("message", "Cannot set availability. User is not a faculty member.");
                return SUCCESS;
            }
            Faculty faculty = em.find(Faculty.class, user.getId());

            Map parameters = request.getParameterMap();

            //Getting timeslot values
            String[] timeslotIdArray = (String[]) parameters.get("timeslot_data[]");
            int scheduleId = Integer.parseInt(((String[])parameters.get("scheduleId"))[0]);
            Schedule dealingWithSchedule = em.find(Schedule.class, Long.valueOf(scheduleId));

            HashSet<Timeslot> availability = new HashSet<Timeslot>();
            //Populate timeslots in availability list
            if (timeslotIdArray != null && timeslotIdArray.length > 0) {
                for (String s : timeslotIdArray) {
                    Long timeslotId = Long.parseLong(s.split("_")[1]);
                    Timeslot t = em.find(Timeslot.class, timeslotId);
                    availability.add(t);
                }
            }
            
            try {
                Set<Timeslot> existingAvailability = faculty.getUnavailableTimeslots();
                for (Timeslot existingTimeslot : existingAvailability) {
                    //Add other schedules' unavailable timeslots
                    if (existingTimeslot.getSchedule().getId() != scheduleId) {
                        availability.add(existingTimeslot);
                    }
                }
                faculty.setUnavailableTimeslots(availability);
                em.persist(faculty);
                em.getTransaction().commit();
				
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				
				List<String> unavailableTimeslots = new ArrayList<String>();
				for (Timeslot t : availability) {
					unavailableTimeslots.add("timeslot_" + t.getId());
				}
				json.put("unavailableTimeslots", unavailableTimeslots);

                //Reloading the user object in the session
                request.getSession().setAttribute("user", faculty);

                json.put("success", true);
                json.put("message", "Your availability has been updated successfully!");
				MiscUtil.logActivity(logger, user, "Updated availability for " + dealingWithSchedule.toString());
            } catch (NullPointerException n) {
				logger.error(n.getMessage());
				if (MiscUtil.DEV_MODE) {
					for (StackTraceElement s : n.getStackTrace()) {
						logger.debug(s.toString());
					}
				}
                json.put("success", false);
                json.put("message", "An error was detected. Please reload and try again.");
            }
            
        } catch (Exception e) {
            logger.error(e.getMessage());
            if (MiscUtil.DEV_MODE) {
                for (StackTraceElement s : e.getStackTrace()) {
                    logger.debug(s.toString());
                }
            }
            json.put("success", false);
            json.put("message", "Error with SetAvailability: Escalate to developers!");
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
