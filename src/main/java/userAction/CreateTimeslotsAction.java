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
import javax.servlet.RequestDispatcher;
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
 * @author Prakhar
 */
public class CreateTimeslotsAction extends ActionSupport implements ServletRequestAware {

    private HttpServletRequest request;
    static final Logger logger = LoggerFactory.getLogger(CreateTimeslotsAction.class);
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
//        for (Object key : parameters.keySet()) {
//            logger.info("Received key: " + key + ", value: " + ((String[]) parameters.get(key))[0]);
//            if (((String[]) parameters.get(key)).length > 1) {
//                logger.info("Received key: " + key + ", value: " + ((String[]) parameters.get(key))[1]);
//                logger.info("Received key: " + key + ", value: " + ((String[]) parameters.get(key))[2]);
//            }
//        }

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

        try {
            //Set acceptance timeslots
            for (String s : acceptanceTimeslotStrings) {
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
                TimeslotManager.save(em, t, transaction);
                acceptanceTimeslots.add(t);
            }

            logger.debug("Persisted acceptance timeslots: count " + acceptanceTimeslots.size());

            //Set midterm timeslots
            for (String s : midtermTimeslotStrings) {
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
               TimeslotManager.save(em, t, transaction);
               midtermTimeslots.add(t);
            }

            logger.debug("Persisted midterm timeslots: count " + midtermTimeslots.size());

            //Set final timeslots
            for (String s : finalTimeslotStrings) {
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
                TimeslotManager.save(em, t, transaction);
                finalTimeslots.add(t);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        logger.debug("Persisted final timeslots: count " + finalTimeslots.size());

        json.put("success", true);
		json.put("message", "Timeslots stored successfully. Schedule creation complete!");
        } catch (Exception e) {
            logger.error("Exception caught: " + e.getMessage());
            if (MiscUtil.DEV_MODE) {
                for (StackTraceElement s : e.getStackTrace()) {
                    logger.debug(s.toString());
                }
            }
            json.put("success", false);
            json.put("message", "Error with CreateTimeslots: Escalate to developers!");
        }
        return SUCCESS;
    }

    public void setServletRequest(HttpServletRequest hsr) {
        request = hsr;
    }
}
