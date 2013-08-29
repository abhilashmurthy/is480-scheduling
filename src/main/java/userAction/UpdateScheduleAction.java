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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import javax.servlet.http.HttpServletRequest;
import manager.ScheduleManager;
import manager.TimeslotManager;
import model.Schedule;
import model.Timeslot;
import org.apache.struts2.interceptor.ServletRequestAware;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
            List<String> acceptanceDates = new ArrayList<String>(Arrays.asList(((String[]) parameters.get("acceptanceDates[]"))));
            List<String> midtermDates = new ArrayList<String>(Arrays.asList(((String[]) parameters.get("midtermDates[]"))));
            List<String> finalDates = new ArrayList<String>(Arrays.asList(((String[]) parameters.get("finalDates[]"))));
            
            Collections.sort(acceptanceDates);
            Collections.sort(midtermDates);
            Collections.sort(finalDates);
            
            for (String s : acceptanceDates) {
                logger.debug("AccDates: " + s);
            }
            
            logger.debug("Initialized variables");
            
            Schedule acceptanceSchedule = ScheduleManager.findById(em, acceptanceId);
            Schedule midtermSchedule = ScheduleManager.findById(em, midtermId);
            Schedule finalSchedule = ScheduleManager.findById(em, finalId);
            
            logger.debug("Retreieved midtermSchedule: " + midtermSchedule.getId());
            
            //Getting and setting timestamps
            Timestamp acceptanceStartTimeStamp = Timestamp.valueOf(acceptanceDates.get(0) + " 00:00:00");
            Timestamp acceptanceEndTimeStamp = Timestamp.valueOf(acceptanceDates.get(acceptanceDates.size() - 1) + " 00:00:00");
            Timestamp midtermStartTimeStamp = Timestamp.valueOf(midtermDates.get(0) + " 00:00:00");
            Timestamp midtermEndTimeStamp = Timestamp.valueOf(midtermDates.get(midtermDates.size() - 1) + " 00:00:00");
            Timestamp finalStartTimeStamp = Timestamp.valueOf(finalDates.get(0) + " 00:00:00");
            Timestamp finalEndTimeStamp = Timestamp.valueOf(finalDates.get(finalDates.size() - 1) + " 00:00:00");
            
            logger.debug("Got timestamps");
            
            acceptanceSchedule.setStartDate(acceptanceStartTimeStamp);
            acceptanceSchedule.setEndDate(acceptanceEndTimeStamp);
            midtermSchedule.setStartDate(midtermStartTimeStamp);
            midtermSchedule.setEndDate(midtermEndTimeStamp);
            finalSchedule.setStartDate(finalStartTimeStamp);
            finalSchedule.setEndDate(finalEndTimeStamp);
            
            logger.debug("Updated timestamps");
            
            ScheduleManager.update(em, acceptanceSchedule, transaction);
            ScheduleManager.update(em, midtermSchedule, transaction);
            ScheduleManager.update(em, finalSchedule, transaction);
            
            logger.debug("Stored in database");
            
            //Get timeslots based on schedules
            List<Timeslot> acceptanceTimeslots = TimeslotManager.findBySchedule(em, acceptanceSchedule);
            List<Timeslot> midtermTimeslots = TimeslotManager.findBySchedule(em, midtermSchedule);
            List<Timeslot> finalTimeslots = TimeslotManager.findBySchedule(em, finalSchedule);
            
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Calendar cal = Calendar.getInstance();
            
            List<String> oldDates = new ArrayList<String>();
            
            /*
              ACCEPTANCE TIMESLOT UPDATING
             */
            
            //Delete unwanted acceptance timeslots
            oldAcceptanceDates:
            for (Timeslot t : acceptanceTimeslots) {
                logger.debug("Got acceptance timeslot: " + t.getId() + ", " + t.getStartTime());
                Timestamp startTime = t.getStartTime();
                cal.setTimeInMillis(startTime.getTime());
                String startDate = sdf.format(cal.getTime());
                
                //Filters out dates without timeslots
                for (int i = 0; i < acceptanceDates.size(); i++) {
                    String acceptanceDate = acceptanceDates.get(i);
                    if (acceptanceDate.equals(startDate)) {
                        oldDates.add(acceptanceDate);
                        continue oldAcceptanceDates;
                    }
                }
                
                //If this is reached, the timeslot date doesn't exist. Therefore, remove it completely.
                if (!TimeslotManager.delete(em, t, transaction)) {
                    throw new Exception("Unable to delete: " + t);
                }
            }
            
            //Add wanted acceptance timeslots
            newAcceptanceDates:
            for (String acceptanceDate : acceptanceDates) {
                for (String oldDate : oldDates) {
                    if (acceptanceDate.equals(oldDate)) {
                        continue newAcceptanceDates;
                    }
                }
                int currentTime = 9;
                Timestamp newStartTime = Timestamp.valueOf(acceptanceDate + " 09:00:00");
                while (currentTime <= 18) {
                    Timestamp startTime = newStartTime;
                    cal.setTimeInMillis(startTime.getTime());
                    cal.add(Calendar.HOUR, 1);
                    Timestamp endTime = new Timestamp(cal.getTimeInMillis());
                    
                    Timeslot t = new Timeslot();
                    t.setStartTime(startTime);
                    t.setEndTime(endTime);
                    t.setVenue("SIS Seminar Room 2-1");
                    t.setSchedule(acceptanceSchedule);
                    em.persist(t);
                    currentTime++;
                    newStartTime = endTime;
                }
            }
            
            /*
              MIDTERM TIMESLOT UPDATING
             */
            
            oldDates = new ArrayList<String>();
            
            //Delete unwanted midterm timeslots
            oldMidtermDates:
            for (Timeslot t : midtermTimeslots) {
                logger.debug("Got midterm timeslot: " + t.getId() + ", " + t.getStartTime());
                Timestamp startTime = t.getStartTime();
                cal.setTimeInMillis(startTime.getTime());
                String startDate = sdf.format(cal.getTime());
                
                //Filters out dates without timeslots
                for (int i = 0; i < midtermDates.size(); i++) {
                    String midtermDate = midtermDates.get(i);
                    if (midtermDate.equals(startDate)) {
                        oldDates.add(midtermDate);
                        continue oldMidtermDates;
                    }
                }
                
                //If this is reached, the timeslot date doesn't exist. Therefore, remove it completely.
                if (!TimeslotManager.delete(em, t, transaction)) {
                    throw new Exception("Unable to delete: " + t);
                }
            }
            
            //Add wanted midterm timeslots
            newMidtermDates:
            for (String midtermDate : midtermDates) {
                for (String oldDate : oldDates) {
                    if (midtermDate.equals(oldDate)) {
                        continue newMidtermDates;
                    }
                }
                int currentTime = 9;
                Timestamp newStartTime = Timestamp.valueOf(midtermDate + " 09:00:00");
                while (currentTime <= 18) {
                    Timestamp startTime = newStartTime;
                    cal.setTimeInMillis(startTime.getTime());
                    cal.add(Calendar.HOUR, 1);
                    cal.add(Calendar.MINUTE, 30);
                    Timestamp endTime = new Timestamp(cal.getTimeInMillis());
                    
                    Timeslot t = new Timeslot();
                    t.setStartTime(startTime);
                    t.setEndTime(endTime);
                    t.setVenue("SIS Seminar Room 2-1");
                    t.setSchedule(midtermSchedule);
                    em.persist(t);
                    currentTime++;
                    newStartTime = endTime;
                }
            }

            /*
              FINAL TIMESLOT UPDATING
             */
            
            oldDates = new ArrayList<String>();
            
            //Delete unwanted final timeslots
            oldFinalDates:
            for (Timeslot t : finalTimeslots) {
                logger.debug("Got final timeslot: " + t.getId() + ", " + t.getStartTime());
                Timestamp startTime = t.getStartTime();
                cal.setTimeInMillis(startTime.getTime());
                String startDate = sdf.format(cal.getTime());
                
                //Filters out dates without timeslots
                for (int i = 0; i < finalDates.size(); i++) {
                    String finalDate = finalDates.get(i);
                    if (finalDate.equals(startDate)) {
                        oldDates.add(finalDate);
                        continue oldFinalDates;
                    }
                }
                
                //If this is reached, the timeslot date doesn't exist. Therefore, remove it completely.
                if (!TimeslotManager.delete(em, t, transaction)) {
                    throw new Exception("Unable to delete: " + t);
                }
            }
            
            //Add wanted final timeslots
            newFinalDates:
            for (String finalDate : finalDates) {
                for (String oldDate : oldDates) {
                    if (finalDate.equals(oldDate)) {
                        continue newFinalDates;
                    }
                }
                int currentTime = 9;
                Timestamp newStartTime = Timestamp.valueOf(finalDate + " 09:00:00");
                while (currentTime <= 18) {
                    Timestamp startTime = newStartTime;
                    cal.setTimeInMillis(startTime.getTime());
                    cal.add(Calendar.HOUR, 1);
                    cal.add(Calendar.MINUTE, 30);
                    Timestamp endTime = new Timestamp(cal.getTimeInMillis());
                    
                    Timeslot t = new Timeslot();
                    t.setStartTime(startTime);
                    t.setEndTime(endTime);
                    t.setVenue("SIS Seminar Room 2-1");
                    t.setSchedule(finalSchedule);
                    em.persist(t);
                    currentTime++;
                    newStartTime = endTime;
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