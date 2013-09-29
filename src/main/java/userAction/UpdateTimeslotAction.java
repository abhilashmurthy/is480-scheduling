/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package userAction;

import static com.opensymphony.xwork2.Action.SUCCESS;
import com.opensymphony.xwork2.ActionSupport;
import constant.Response;
import constant.Role;
import java.io.IOException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.persistence.EntityManager;
import javax.persistence.Persistence;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import manager.ScheduleManager;
import manager.TimeslotManager;
import model.Booking;
import model.Schedule;
import model.Timeslot;
import model.User;
import model.role.Student;
import org.apache.struts2.interceptor.ServletRequestAware;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.MiscUtil;

/**
 *
 * @author Tarlochan
 */
public class UpdateTimeslotAction extends ActionSupport implements ServletRequestAware {

    private HttpServletRequest request;
    private static Logger logger = LoggerFactory.getLogger(UpdateTimeslotAction.class);
    private HashMap<String, Object> json = new HashMap<String, Object>();

    @Override
    public String execute() throws ServletException, IOException {
        EntityManager em = null;
        try {
            //Code here
            //convert the chosen ID into long and get the corresponding Timeslot object
            em = MiscUtil.getEntityManagerInstance();
			em.getTransaction().begin();
            HttpSession session = request.getSession();

            //Checking whether the user setting optional attendees is student, admin or cc
            User user = (User) session.getAttribute("user");
            if (user.getRole().equals(Role.ADMINISTRATOR) || user.getRole().equals(Role.COURSE_COORDINATOR)) {
                //Getting input data from url
                JSONObject inputData = (JSONObject) new JSONObject(request.getParameter("jsonData"));
                long chosenID = Long.parseLong(inputData.getString("timeslotId"));
                Timeslot oldTimeslot = TimeslotManager.findById(em, chosenID);
				logger.debug("Timeslot is " + oldTimeslot);

                //Update timeslot parameters
                String newVenue = null;
                try {
                    newVenue = inputData.getString("venue");
					if (newVenue == null || newVenue.length() == 0) throw new Exception();
                } catch (Exception e) {
                    logger.debug("newVenue not found");
					json.put("success", false);
					json.put("message", "No venue detected");
					return SUCCESS;
                }
				
				if (newVenue.equalsIgnoreCase(oldTimeslot.getVenue())) {
					json.put("success", false);
					json.put("message", "No change made..");
					return SUCCESS;
				}
				
				oldTimeslot.setVenue(newVenue);
                json.put("success", true);
                json.put("message", "Timeslot updated successfully!");
				em.getTransaction().commit();
            } else {
                //Incorrect user role
                json.put("success", false);
                json.put("message", "You are not authorized to update the timeslot!");
            }
            return SUCCESS;
        } catch (Exception e) {
            logger.error("Exception caught: " + e.getMessage());
            if (MiscUtil.DEV_MODE) {
                for (StackTraceElement s : e.getStackTrace()) {
                    logger.debug(s.toString());
                }
            }
            request.setAttribute("error", "Error with UpdateTimeslot: Escalate to developers!");
            json.put("success", false);
            json.put("message", "Error with UpdateTimeslot: Escalate to developers!");
            return ERROR;
        } finally {
            if (em != null && em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            if (em != null && em.isOpen()) {
                em.close();
            }
        }
    } //end of execute

    public HttpServletRequest getRequest() {
        return request;
    }

    public void setRequest(HttpServletRequest request) {
        this.request = request;
    }

    public void setServletRequest(HttpServletRequest hsr) {
        this.request = hsr;
    }

    public HashMap<String, Object> getJson() {
        return json;
    }

    public void setJson(HashMap<String, Object> json) {
        this.json = json;
    }
}
