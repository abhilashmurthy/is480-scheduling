/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package userAction;

import static com.opensymphony.xwork2.Action.SUCCESS;
import systemAction.*;
import com.opensymphony.xwork2.ActionSupport;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import manager.MilestoneManager;
import manager.ScheduleManager;
import manager.TermManager;
import manager.TimeslotManager;
import model.Milestone;
import model.Schedule;
import model.Term;
import model.Timeslot;
import org.apache.struts2.interceptor.ServletRequestAware;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static userAction.CreateScheduleAction.logger;
import util.MiscUtil;

/**
 *
 * @author User
 */
public class UpdateScheduleAction extends ActionSupport implements ServletRequestAware {

    private HttpServletRequest request;
    private static Logger logger = LoggerFactory.getLogger(UpdateScheduleAction.class);
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
            json.put("exception", false);
            EntityManager em = Persistence.createEntityManagerFactory(MiscUtil.PERSISTENCE_UNIT).createEntityManager();
            EntityTransaction transaction = null;
            
            Map parameters = request.getParameterMap();
            for (Object key : parameters.keySet()) {
                logger.info("Received key: " + key + ", value: " + ((String[]) parameters.get(key))[0]);
            }

            int year = Integer.parseInt(((String[]) parameters.get("year"))[0]);
            String semester = ((String[]) parameters.get("semester"))[0];
            int acceptanceId = Integer.parseInt(((String[]) parameters.get("acceptanceId"))[0]);
            int midtermId = Integer.parseInt(((String[]) parameters.get("midtermId"))[0]);
            int finalId = Integer.parseInt(((String[]) parameters.get("finalId"))[0]);
            String[] acceptanceDates = ((String[]) parameters.get("acceptanceDates[]"));
            String[] midtermDates = ((String[]) parameters.get("midtermDates[]"));
            String[] finalDates = ((String[]) parameters.get("finalDates[]"));
            
            for (String s : acceptanceDates) {
                logger.debug("AccDates: " + s);
            }
            
            logger.debug("Initialized variables");
            
            Schedule storedAcceptance = ScheduleManager.findById(em, acceptanceId);
            Schedule storedMidterm = ScheduleManager.findById(em, midtermId);
            Schedule storedFinal = ScheduleManager.findById(em, finalId);
            
            logger.debug("Retreieved storedMidterm: " + storedMidterm.getId());
            
            //Getting and setting timestamps
            Timestamp acceptanceStartTimeStamp = Timestamp.valueOf(acceptanceDates[0] + " 00:00:00");
            Timestamp acceptanceEndTimeStamp = Timestamp.valueOf(acceptanceDates[acceptanceDates.length - 1] + " 00:00:00");
            Timestamp midtermStartTimeStamp = Timestamp.valueOf(midtermDates[0] + " 00:00:00");
            Timestamp midtermEndTimeStamp = Timestamp.valueOf(midtermDates[midtermDates.length - 1] + " 00:00:00");
            Timestamp finalStartTimeStamp = Timestamp.valueOf(finalDates[0] + " 00:00:00");
            Timestamp finalEndTimeStamp = Timestamp.valueOf(finalDates[finalDates.length - 1] + " 00:00:00");
            
            logger.debug("Got timestamps");
            
            storedAcceptance.setStartDate(acceptanceStartTimeStamp);
            storedAcceptance.setEndDate(acceptanceEndTimeStamp);
            storedMidterm.setStartDate(midtermStartTimeStamp);
            storedMidterm.setEndDate(midtermEndTimeStamp);
            storedFinal.setStartDate(finalStartTimeStamp);
            storedFinal.setEndDate(finalEndTimeStamp);
            
            logger.debug("Updated timestamps");
            
            ScheduleManager.update(em, storedAcceptance, transaction);
            ScheduleManager.update(em, storedMidterm, transaction);
            ScheduleManager.update(em, storedFinal, transaction);
            
            logger.debug("Stored in database");
            
            //Get timeslots based on schedules
            List<Timeslot> acceptanceTimeslots = TimeslotManager.findBySchedule(em, storedAcceptance);
            List<Timeslot> midtermTimeslots = TimeslotManager.findBySchedule(em, storedMidterm);
            List<Timeslot> finalTimeslots = TimeslotManager.findBySchedule(em, storedFinal);
            
            //Update acceptance timeslots
            for (Timeslot t : acceptanceTimeslots) {
                logger.debug("Got acceptance timeslot: " + t.getId() + ", " + t.getStartTime());
                if (t.getStartTime().getDate() < acceptanceStartTimeStamp.getDate()
                        || t.getStartTime().getDate() > acceptanceEndTimeStamp.getDate()) {
                    if (!TimeslotManager.delete(em, t, transaction)) {
                        throw new Exception("Unable to delete: " + t);
                    }
                }
            }
            
            //Update midterm timeslots
            for (Timeslot t : midtermTimeslots) {
                if (t.getStartTime().getDate() < midtermStartTimeStamp.getDate()
                        || t.getStartTime().getDate() > midtermEndTimeStamp.getDate()) {
                    if (!TimeslotManager.delete(em, t, transaction)) {
                        throw new Exception("Unable to delete: " + t);
                    }
                }
            }
            
            //Update final timeslots
            for (Timeslot t : finalTimeslots) {
                if (t.getStartTime().getDate() < finalStartTimeStamp.getDate()
                        || t.getStartTime().getDate() > finalEndTimeStamp.getDate()) {
                    if (!TimeslotManager.delete(em, t, transaction)) {
                        throw new Exception("Unable to delete: " + t);
                    }
                }
            }

            json.put("success", true);
            json.put("message", "Schedules updated");
            return SUCCESS;
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

    public HttpServletRequest getRequest() {
        return request;
    }

    public void setRequest(HttpServletRequest request) {
        this.request = request;
    }

    public void setServletRequest(HttpServletRequest hsr) {
        this.request = hsr;
    }
}