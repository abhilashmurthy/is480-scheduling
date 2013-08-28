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
import java.util.Set;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import javax.servlet.http.HttpServletRequest;
import manager.MilestoneManager;
import manager.ScheduleManager;
import manager.TermManager;
import model.Milestone;
import model.Schedule;
import model.Term;
import org.apache.struts2.interceptor.ServletRequestAware;
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

    public HashMap<String, Object> getJson() {
        return json;
    }

    public void setJson(HashMap<String, Object> json) {
        this.json = json;
    }

    @Override
    public String execute() throws Exception {
        try {
            EntityManager em = Persistence.createEntityManagerFactory(MiscUtil.PERSISTENCE_UNIT).createEntityManager();
            Map parameters = request.getParameterMap();
            for (Object key : parameters.keySet()) {
                logger.info("Received key: " + key + ", value: " + ((String[]) parameters.get(key))[0]);
            }
            
            EntityTransaction transaction = null;

            int year = Integer.parseInt(((String[]) parameters.get("year"))[0]);
            String semester = ((String[]) parameters.get("semester"))[0];
            
            logger.debug("Initialized year and semester");
            
            //MilestoneDetails in a list of Object[], 
            // where Object[0] = Milestone's name (String)
            // Object[1] = Set of dates (String[])
            List<Object[]> milestonesDates = new ArrayList<Object[]>();
            for (Object o: parameters.entrySet()) {
                Map.Entry entry = (Map.Entry) o;
                String entryKey = ((String) entry.getKey());
                if (entryKey.contains("Dates")) {
                    String[] dates = ((String[]) entry.getValue())[0].split(",");
                    Object[] milestoneDetails = new Object[]{entryKey.split("Dates")[0], dates};
                    milestonesDates.add(milestoneDetails);
                }
            }

            logger.debug("Objectified milestones");
            logger.debug("Example date is: " + ((String[]) milestonesDates.get(0)[1])[0]);

            Term storedTerm = TermManager.findByYearAndSemester(em, year, semester);

            logger.debug("Retreived storedTerm: " + storedTerm);
            
            //Hardcoded list of Milestone names before MilestoneConfig
            List<Milestone> milestones = new ArrayList<Milestone>();
            //milestones.add(new Milestone("Acceptance", 60, storedTerm));
            //milestones.add(new Milestone("Midterm", 90, storedTerm));
            //milestones.add(new Milestone("Final", 90, storedTerm));
            
            logger.debug("Simulated Milestone list from MilestoneConfig. Example: " + milestones.get(0).getName());

            //Persist milestones
            for (Milestone milestone : milestones) {
                MilestoneManager.save(em, milestone, transaction);
            }
  
            logger.debug("Persisted all milestones");
            
            List<Schedule> scheduleList = new ArrayList<Schedule>();
            
            logger.debug("Creating and attempting to persist schedules");
            
            for (Object[] milestoneDetails : milestonesDates) {
                String milestoneName = (String) milestoneDetails[0];
                String[] milestoneDates = (String[]) milestoneDetails[1];
                Arrays.sort(milestoneDates);
                Timestamp startTimestamp = Timestamp.valueOf(milestoneDates[0] + " 00:00:00");
                Timestamp endTimestamp = Timestamp.valueOf(milestoneDates[milestoneDates.length - 1] + " 00:00:00");
                Schedule schedule = new Schedule();
                schedule.setStartDate(startTimestamp);
                schedule.setEndDate(endTimestamp);
                schedule.setDayStartTime(9); //TODO: Remove hardcoding
                schedule.setDayEndTime(18); //TODO: Remove hardcoding
                Milestone milestone = MilestoneManager.findByNameAndTerm(em, milestoneName, storedTerm);
                schedule.setMilestone(milestone); //Get milestone from MilestoneConfig
                scheduleList.add(schedule);
                ScheduleManager.save(em, schedule, transaction);
            }
            
            logger.debug("Persisted all schedules");
            
            for (Schedule schedule : scheduleList) {
                HashMap<String, Object> milestoneDetails = new HashMap<String, Object>();
                Milestone milestone = schedule.getMilestone();
                milestoneDetails.put("scheduleId", schedule.getId());
                milestoneDetails.put("dayStartTime", schedule.getDayStartTime());
                milestoneDetails.put("dayEndTime", schedule.getDayEndTime());
                milestoneDetails.put("duration", milestone.getSlotDuration());
                json.put(milestone.getName().toLowerCase(), milestoneDetails);
            }
            
            logger.debug("JSONified milestone data");

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
        }
        return SUCCESS;
    }

    public void setServletRequest(HttpServletRequest hsr) {
        request = hsr;
    }
}
