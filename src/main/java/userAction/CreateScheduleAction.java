/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package userAction;

import static com.opensymphony.xwork2.Action.SUCCESS;
import com.opensymphony.xwork2.ActionSupport;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import javax.servlet.http.HttpServletRequest;
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
    private List<Schedule> scheduleList;
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

        int year = Integer.parseInt(((String[]) parameters.get("year"))[0]);
        String semester = ((String[]) parameters.get("semester"))[0];
        String midtermDatesString = ((String[]) parameters.get("midtermDates"))[0];
        String acceptanceDatesString = ((String[]) parameters.get("acceptanceDates"))[0];
        String finalDatesString = ((String[]) parameters.get("finalDates"))[0];

        logger.debug("Initialized variables");

        //Initate scheduleList
        scheduleList = new ArrayList<Schedule>();

        EntityTransaction transaction = null;;

        //Save schedule in DB
        String[] acceptanceDates = acceptanceDatesString.split(",");
        String[] midtermDates = midtermDatesString.split(",");
        String[] finalDates = finalDatesString.split(",");

        logger.debug("Arrayed dates");

		Term storedTerm = TermManager.findByYearAndSemester(em, year, semester);

        logger.debug("Retreived storedTerm");
		
        //Getting and setting milestones
        Milestone acceptanceMil = new Milestone();
		acceptanceMil.setName("Acceptance");
		acceptanceMil.setSlotDuration(60);
		acceptanceMil.setTerm(storedTerm);
		
        Milestone midtermMil = new Milestone();
		midtermMil.setName("Midterm");
		midtermMil.setSlotDuration(90);
		midtermMil.setTerm(storedTerm);
        
		Milestone finalMil = new Milestone();
		finalMil.setName("Final");
		finalMil.setSlotDuration(90);
		finalMil.setTerm(storedTerm);

        logger.debug("Acceptance date is: " + acceptanceDates[0]);

        //TODO: Check that the windows don't overlap with other schedules

        //Getting and setting timestamps
        Timestamp acceptanceStartTimeStamp = Timestamp.valueOf(acceptanceDates[0] + " 00:00:00");
        Timestamp acceptanceEndTimeStamp = Timestamp.valueOf(acceptanceDates[acceptanceDates.length - 1] + " 00:00:00");
        Timestamp midtermStartTimeStamp = Timestamp.valueOf(midtermDates[0] + " 00:00:00");
        Timestamp midtermEndTimeStamp = Timestamp.valueOf(midtermDates[midtermDates.length - 1] + " 00:00:00");
        Timestamp finalStartTimeStamp = Timestamp.valueOf(finalDates[0] + " 00:00:00");
        Timestamp finalEndTimeStamp = Timestamp.valueOf(finalDates[finalDates.length - 1] + " 00:00:00");

        logger.debug("Created timestamps");

        //Create schedule objects
        Schedule acceptanceSched = new Schedule();
        acceptanceSched.setMilestone(acceptanceMil);
        acceptanceSched.setStartDate(acceptanceStartTimeStamp);
        acceptanceSched.setEndDate(acceptanceEndTimeStamp);

        Schedule midtermSched = new Schedule();
        midtermSched.setMilestone(midtermMil);
        midtermSched.setStartDate(midtermStartTimeStamp);
        midtermSched.setEndDate(midtermEndTimeStamp);

        Schedule finalSched = new Schedule();
        finalSched.setMilestone(finalMil);
        finalSched.setStartDate(finalStartTimeStamp);
        finalSched.setEndDate(finalEndTimeStamp);

        logger.debug("Created schedules");

        //Add schedules to scheduleList
        scheduleList.add(acceptanceSched);
        scheduleList.add(midtermSched);
        scheduleList.add(finalSched);
        logger.debug("Added to scheduleList");

        //Save schedules to DB
        //TODO Handle write error
        ScheduleManager.save(em, scheduleList, transaction);
        logger.info("Schedules have been stored");

        Schedule storedAcceptance = ScheduleManager.findByWindow(em, acceptanceStartTimeStamp, acceptanceEndTimeStamp);
        Schedule storedMidterm = ScheduleManager.findByWindow(em, midtermStartTimeStamp, midtermEndTimeStamp);
        Schedule storedFinal = ScheduleManager.findByWindow(em, finalStartTimeStamp, finalEndTimeStamp);

        json.put("acceptanceScheduleId", storedAcceptance.getId());
        json.put("midtermScheduleId", storedMidterm.getId());
        json.put("finalScheduleId", storedFinal.getId());

        logger.debug("Retreived storedAcceptance: " + storedAcceptance.getId());

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
