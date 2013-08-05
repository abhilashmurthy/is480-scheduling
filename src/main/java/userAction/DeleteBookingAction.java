/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package userAction;

import systemAction.*;
import com.opensymphony.xwork2.ActionSupport;
import constant.Status;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import manager.MilestoneManager;
import manager.TimeslotManager;
import model.Team;
import model.Timeslot;
import model.User;
import org.apache.struts2.interceptor.ServletRequestAware;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.MiscUtil;

/**
 *
 * @author Tarlochan
 */
public class DeleteBookingAction extends ActionSupport implements ServletRequestAware {

    private HttpServletRequest request;
    private static Logger logger = LoggerFactory.getLogger(DeleteBookingAction.class);
    private String timeslotId;
    private HashMap<String, Object> json = new HashMap<String, Object>();

    @Override
    public String execute() throws ServletException, IOException {
        try {
            EntityManager em = Persistence.createEntityManagerFactory(MiscUtil.PERSISTENCE_UNIT).createEntityManager();

            //convert the chosen ID into long and get the corresponding Timeslot object
            long chosenID = Long.parseLong(timeslotId);
            Timeslot ts = TimeslotManager.findById(em, chosenID);

            //this would return a success/fail of the deletion
            boolean status = TimeslotManager.deleteTimeslotBooking(em, ts);

            //if the booking has been removed successfully
            if (status) {
                json.put("success", true);
                json.put("message", "Booking deleted successfully! Deletion email has been sent to all attendees. (Coming soon..)");
            } else {
                json.put("success", false);
                json.put("message", "unable to delete from datbase");
            }

        } catch (Exception e) {
            logger.error("Exception caught: " + e.getMessage());
            if (MiscUtil.DEV_MODE) {
                for (StackTraceElement s : e.getStackTrace()) {
                    logger.debug(s.toString());
                }
            }
            json.put("success", false);
            json.put("exception", true);
            json.put("message", "Error with Delete Booking: Escalate to developers!");
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

    public String getTimeslotId() {
        return timeslotId;
    }

    public void setTimeslotId(String timeslotId) {
        this.timeslotId = timeslotId;
    }

    public HashMap<String, Object> getJson() {
        return json;
    }

    public void setJson(HashMap<String, Object> json) {
        this.json = json;
    }
}
