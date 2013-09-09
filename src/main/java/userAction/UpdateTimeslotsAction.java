/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package userAction;

import static com.opensymphony.xwork2.Action.SUCCESS;
import com.opensymphony.xwork2.ActionSupport;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
 * @author Prakhar
 */
public class UpdateTimeslotsAction extends ActionSupport implements ServletRequestAware {

    private HttpServletRequest request;
    static final Logger logger = LoggerFactory.getLogger(UpdateTimeslotsAction.class);
    private HashMap<String, Object> json = new HashMap<String, Object>();

    public HashMap<String, Object> getJson() {
        return json;
    }

    public void setJson(HashMap<String, Object> json) {
        this.json = json;
    }

    @Override
    public String execute() throws Exception {
		EntityManager em = null;
        try {
            json.put("exception", false);
            em = MiscUtil.getEntityManagerInstance();

            Map parameters = request.getParameterMap();

            //Getting schedule id's
            int acceptanceId = Integer.parseInt(((String[]) parameters.get("acceptanceId"))[0]);
            int midtermId = Integer.parseInt(((String[]) parameters.get("midtermId"))[0]);
            int finalId = Integer.parseInt(((String[]) parameters.get("finalId"))[0]);

            logger.debug("Got schedule ids");

            //Getting timeslot values
            String[] acceptanceTimeslotStrings = (String[]) parameters.get("timeslot_acceptance[]");
            String[] midtermTimeslotStrings = (String[]) parameters.get("timeslot_midterm[]");
            String[] finalTimeslotStrings = (String[]) parameters.get("timeslot_final[]");

            logger.debug("Got string arrays of schedules");

            for (String s : acceptanceTimeslotStrings) {
                logger.debug("Acceptance timeslot: " + s);
            }

            //Making lists of STORED timeslots
            Set<Timeslot> acceptanceTimeslots = new HashSet<Timeslot>();
            Set<Timeslot> midtermTimeslots = new HashSet<Timeslot>();
            Set<Timeslot> finalTimeslots = new HashSet<Timeslot>();
            Calendar cal = Calendar.getInstance();
            EntityTransaction transaction = null;

            Schedule acceptanceSchedule = ScheduleManager.findById(em, acceptanceId);
            Schedule midtermSchedule = ScheduleManager.findById(em, midtermId);
            Schedule finalSchedule = ScheduleManager.findById(em, finalId);

            logger.debug("Got all schedules: acceptance[" + acceptanceSchedule + "], midterm[" + midtermSchedule + "], final[" + finalSchedule + "]");
            
            //Get old based on schedules
            List<Timeslot> oldAcceptanceTimeslots = TimeslotManager.findBySchedule(em, acceptanceSchedule);
            List<Timeslot> oldMidtermTimeslots = TimeslotManager.findBySchedule(em, midtermSchedule);
            List<Timeslot> oldFinalTimeslots = TimeslotManager.findBySchedule(em, finalSchedule);
            
            //Delete old timeslots
            List<Timeslot> acceptanceDeletes = getTimeslotsToDelete(oldAcceptanceTimeslots, acceptanceTimeslotStrings);
            List<Timeslot> midtermDeletes = getTimeslotsToDelete(oldMidtermTimeslots, midtermTimeslotStrings);
            List<Timeslot> finalDeletes = getTimeslotsToDelete(oldFinalTimeslots, finalTimeslotStrings);
            
            for (Timeslot t : acceptanceDeletes) {
                logger.debug("Acceptance delete: " + t.getStartTime());
            }
            
            List<Timeslot> allDeletes = new ArrayList<Timeslot>();
            allDeletes.addAll(acceptanceDeletes);
            allDeletes.addAll(midtermDeletes);
            allDeletes.addAll(finalDeletes);
            
            for (Timeslot d : allDeletes) {
                TimeslotManager.delete(em, d, transaction);
            }
            
            logger.debug("Deleted : " + allDeletes.size());
            
            //Add new timeslots
            List<String> acceptanceAppends = getTimeslotsToAppend(oldAcceptanceTimeslots, acceptanceTimeslotStrings);
            List<String> midtermAppends = getTimeslotsToAppend(oldMidtermTimeslots, midtermTimeslotStrings);
            List<String> finalAppends = getTimeslotsToAppend(oldFinalTimeslots, finalTimeslotStrings);
            
            for (String s : acceptanceAppends) {
                logger.debug("Acceptance append: " + s);
            }
            
            //Append new timeslots
            for (String s : acceptanceAppends) {
                //Getting startTime and endTime
                Timestamp startTime = Timestamp.valueOf(s);
                cal.setTimeInMillis(startTime.getTime());
                //TODO: Change the 1 to a variable
                cal.add(Calendar.HOUR, 1);
                Timestamp endTime = new Timestamp(cal.getTimeInMillis());
                
                Timeslot t = new Timeslot();
                t.setStartTime(startTime);
                t.setEndTime(endTime);
                //TODO: Change to the venue variable
                t.setVenue("SIS Seminar Room 2-1");
                //TODO: Handle write error
                t.setSchedule(acceptanceSchedule);
                em.persist(t);
                acceptanceTimeslots.add(t);
            }
            
            logger.debug("Persisted acceptance timeslots: count " + acceptanceTimeslots.size());
            
                //Set midterm timeslots
                for (String s : midtermAppends) {
                    //Getting startTime and endTime
                    Timestamp startTime = Timestamp.valueOf(s);
                    cal.setTimeInMillis(startTime.getTime());
                    //TODO: Change the 1 to a variable
                    cal.add(Calendar.HOUR, 1);
                    cal.add(Calendar.MINUTE, 30);
                    Timestamp endTime = new Timestamp(cal.getTimeInMillis());

                    Timeslot t = new Timeslot();
                    t.setStartTime(startTime);
                    t.setEndTime(endTime);
                    //TODO: Change to the venue variable
                    t.setVenue("SIS Seminar Room 2-1");
                    t.setSchedule(midtermSchedule);
                    //TODO: Handle write error
                    em.persist(t);
                    midtermTimeslots.add(t);
                }

                logger.debug("Persisted midterm timeslots: count " + midtermTimeslots.size());

                //Set final timeslots
                for (String s : finalAppends) {
                    //Getting startTime and endTime
                    Timestamp startTime = Timestamp.valueOf(s);
                    cal.setTimeInMillis(startTime.getTime());
                    //TODO: Change the 1 to a variable
                    cal.add(Calendar.HOUR, 1);
                    cal.add(Calendar.MINUTE, 30);
                    Timestamp endTime = new Timestamp(cal.getTimeInMillis());

                    Timeslot t = new Timeslot();
                    t.setStartTime(startTime);
                    t.setEndTime(endTime);
                    //TODO: Change to the venue variable
                    t.setVenue("SIS Seminar Room 2-1");
                    t.setSchedule(finalSchedule);
                    //Save timeslot
                    //TODO: Handle write error
                    em.persist(t);
                    finalTimeslots.add(t);
                }
                
            logger.debug("Persisted final timeslots: count " + finalTimeslots.size());

            json.put("success", true);
            json.put("message", "Timeslots udpated");
            return SUCCESS;
        } catch (Exception e) {
            logger.error("Exception caught: " + e.getMessage());
            if (MiscUtil.DEV_MODE) {
                for (StackTraceElement s : e.getStackTrace()) {
                    logger.debug(s.toString());
                }
            }
            json.put("success", false);
            json.put("message", "Error with CreateTimeslots: Escalate to developers!");
        } finally {
			if (em != null && em.getTransaction().isActive()) em.getTransaction().rollback();
			if (em != null && em.isOpen()) em.close();
		}
        return SUCCESS;
    }
    
    public List<Timeslot> getTimeslotsToDelete(List<Timeslot> oldTimeslots, String[] newTimeslotStrings) {
        List<Timeslot> timeslotsToDelete = new ArrayList<Timeslot>();
        oldTimeslots:
        for (Timeslot t : oldTimeslots) {
            for (String s : newTimeslotStrings) {
                if (t.getStartTime().equals(Timestamp.valueOf(s))) {
                    continue oldTimeslots;
                }
            }
            //Timeslot not found
            timeslotsToDelete.add(t);
        }
        return timeslotsToDelete;
    }
    
    public List<String> getTimeslotsToAppend(List<Timeslot> oldTimeslots, String[] newTimeslotStrings) {
        List<String> timeslotsToAppend = new ArrayList<String>();
        newTimeslots:
        for (String s : newTimeslotStrings) {
            for (Timeslot t : oldTimeslots) {
                if (t.getStartTime().equals(Timestamp.valueOf(s))) {
                    continue newTimeslots;
                }
            }
            //Timeslot not found
            timeslotsToAppend.add(s);
        }
        return timeslotsToAppend;
    }

    public void setServletRequest(HttpServletRequest hsr) {
        request = hsr;
    }
}
