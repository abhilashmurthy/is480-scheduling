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
import javax.persistence.EntityTransaction;
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

/**
 *
 * @author Prakhar
 */
public class CreateTimeslotsAction extends ActionSupport implements ServletRequestAware {

    private HttpServletRequest request;
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
            logger.info("Received key: " + key + ", value: " + ((String[]) parameters.get(key))[0]);
            if (((String[]) parameters.get(key)).length > 1) {
                logger.info("Received key: " + key + ", value: " + ((String[]) parameters.get(key))[1]);
                logger.info("Received key: " + key + ", value: " + ((String[]) parameters.get(key))[2]);
            }
        }

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
                acceptanceTimeslots.add(TimeslotManager.save(t, transaction));
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
                midtermTimeslots.add(TimeslotManager.save(t, transaction));
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
                //Save timeslot
                finalTimeslots.add(TimeslotManager.save(t, transaction));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        logger.debug("Persisted final timeslots: count " + finalTimeslots.size());

//        TimeslotManager.saveTimeslots(acceptanceTimeslots, transaction);
//        TimeslotManager.saveTimeslots(midtermTimeslots, transaction);
//        TimeslotManager.saveTimeslots(finalTimeslots, transaction);

        Schedule acceptanceSchedule = ScheduleManager.findById(acceptanceId);
        Schedule midtermSchedule = ScheduleManager.findById(midtermId);
        Schedule finalSchedule = ScheduleManager.findById(finalId);

        logger.debug("Got all schedules: acceptance[" + acceptanceSchedule + "], midterm[" + midtermSchedule + "], final[" + finalSchedule + "]");

        acceptanceSchedule.setTimeslots(acceptanceTimeslots);
        midtermSchedule.setTimeslots(midtermTimeslots);
        finalSchedule.setTimeslots(finalTimeslots);

        logger.debug("Set timeslots hashmaps into schedule objects");
        
        ScheduleManager.update(acceptanceSchedule, transaction);
        ScheduleManager.update(midtermSchedule, transaction);
        ScheduleManager.update(finalSchedule, transaction);
        
        logger.debug("Merged all schedules");

        json.put("success", true);
        return SUCCESS;
    }

    public void setServletRequest(HttpServletRequest hsr) {
        request = hsr;
    }
}