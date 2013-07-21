/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package userAction;

import static com.opensymphony.xwork2.Action.ERROR;
import static com.opensymphony.xwork2.Action.SUCCESS;
import com.opensymphony.xwork2.ActionSupport;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.persistence.EntityTransaction;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import manager.MilestoneManager;
import manager.ScheduleManager;
import manager.TermManager;
import model.Milestone;
import model.Schedule;
import model.Term;
import model.User;
import org.apache.struts2.interceptor.ServletRequestAware;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Prakhar
 */
public class CreateScheduleAction extends ActionSupport implements ServletRequestAware {
    
    private HttpServletRequest request;
    private List<Schedule> scheduleList;
    private HashMap<String, Object> json = new HashMap<String, Object>();
    static final Logger logger = LoggerFactory.getLogger(CreateBookingAction.class);
    
    public HashMap<String, Object> getJson() {
        return json;
    }

    public void setJson(HashMap<String, Object> json) {
        this.json = json;
    }

    @Override
    public String execute() throws Exception {
        Map parameters = request.getParameterMap();
        for (Object key : parameters.keySet()) {
            logger.info("Received key: " + key + ", value: " + ((String[])parameters.get(key))[0]);
        }
        
        int year = Integer.parseInt(((String[])parameters.get("year"))[0]);
        String semester = ((String[])parameters.get("semester"))[0];
        String midtermDatesString = ((String[])parameters.get("midtermDates"))[0];
        String acceptanceDatesString = ((String[])parameters.get("acceptanceDates"))[0];
        String finalDatesString = ((String[])parameters.get("finalDates"))[0];
        
        logger.debug("Initialized variables");
        
        //Initate scheduleList
        scheduleList = new ArrayList<Schedule>();
        
        //Save Term in DB
        EntityTransaction transaction = null;
        Term newTerm = new Term();
        newTerm.setAcademicYear(year);
        newTerm.setSemester(semester);
        TermManager.save(newTerm, transaction);
        
        logger.debug("Saved term");
        
        //Save schedule in DB
        String[] acceptanceDates = acceptanceDatesString.split(",");
        String[] midtermDates = midtermDatesString.split(",");
        String[] finalDates = finalDatesString.split(",");
        
        logger.debug("Arrayed dates");
        
        //Getting and setting milestones
        Milestone acceptanceMil = MilestoneManager.findByName("Acceptance");
        Milestone midtermMil = MilestoneManager.findByName("Midterm");
        Milestone finalMil = MilestoneManager.findByName("Final");
        
        logger.debug("Got milestones");
        logger.debug("Acceptance date is: " + acceptanceDates[0]);
        
        //Getting and setting timestamps
        Timestamp acceptanceStartTimeStamp = Timestamp.valueOf(acceptanceDates[0] + " 00:00:00");
        Timestamp acceptanceEndTimeStamp = Timestamp.valueOf(acceptanceDates[acceptanceDates.length - 1] + " 00:00:00");
        Timestamp midtermStartTimeStamp = Timestamp.valueOf(midtermDates[0] + " 00:00:00");
        Timestamp midtermEndTimeStamp = Timestamp.valueOf(midtermDates[midtermDates.length - 1] + " 00:00:00");
        Timestamp finalStartTimeStamp = Timestamp.valueOf(finalDates[0] + " 00:00:00");
        Timestamp finalEndTimeStamp = Timestamp.valueOf(finalDates[finalDates.length - 1] + " 00:00:00");
        
        logger.debug("Created timestamps");
        
        Term storedTerm = TermManager.findByYearAndSemester(year, semester);
        
        logger.debug("Retreived storedTerm");
        
        //Create schedule objects
        Schedule acceptanceSched = new Schedule();
        acceptanceSched.setMilestone(acceptanceMil);
        acceptanceSched.setTerm(storedTerm);
        acceptanceSched.setStartDate(acceptanceStartTimeStamp);
        acceptanceSched.setEndDate(acceptanceEndTimeStamp);
        
        Schedule midtermSched = new Schedule();
        midtermSched.setMilestone(midtermMil);
        midtermSched.setTerm(storedTerm);
        midtermSched.setStartDate(midtermStartTimeStamp);
        midtermSched.setEndDate(midtermEndTimeStamp);
        
        Schedule finalSched = new Schedule();
        finalSched.setMilestone(finalMil);
        finalSched.setTerm(storedTerm);
        finalSched.setStartDate(finalStartTimeStamp);
        finalSched.setEndDate(finalEndTimeStamp);
        
        logger.debug("Created schedules");
        
        //Add schedules to scheduleList
        scheduleList.add(acceptanceSched);
        scheduleList.add(midtermSched);
        scheduleList.add(finalSched);
        
        logger.debug("Added to scheduleList");
        
        //Save schedules to DB
        ScheduleManager.save(scheduleList, transaction);
        logger.info("Schedules have been stored");
        
        json.put("success", true);
        return SUCCESS;
    }

    public void setServletRequest(HttpServletRequest hsr) {
        request = hsr;
    }
}
