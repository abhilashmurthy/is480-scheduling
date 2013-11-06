/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package userAction;

import static com.opensymphony.xwork2.Action.SUCCESS;
import com.opensymphony.xwork2.ActionSupport;
import constant.Role;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import org.apache.struts2.interceptor.ServletRequestAware;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.HashMap;
import java.util.HashSet;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.persistence.EntityManager;
import model.Booking;
import model.SystemActivityLog;
import model.Timeslot;
import model.User;
import org.json.JSONArray;
import org.json.JSONObject;
import util.MiscUtil;

/**
 *
 * @author Prakhar
 */
public class SetOptionalAttendeesAction extends ActionSupport implements ServletRequestAware {

    private HashMap<String, Object> json = new HashMap<String, Object>();
	private ArrayList<HashMap<String, Object>> data = new ArrayList<HashMap<String, Object>>();
    private HttpServletRequest request;
    private static Logger logger = LoggerFactory.getLogger(SetOptionalAttendeesAction.class);
	
	@Override
    public String execute() {
		HttpSession session = request.getSession();
		
		Calendar nowCal = Calendar.getInstance();
		Timestamp now = new Timestamp(nowCal.getTimeInMillis());
		
		SystemActivityLog logItem = new SystemActivityLog();
		logItem.setActivity("Optional Attendees: Update");
		logItem.setRunTime(now);
		logItem.setUser((User)session.getAttribute("user"));
		logItem.setMessage("Error with validation / No changes made");
		logItem.setSuccess(true);
		
		EntityManager em = null;
        try {
			json.put("exception", false);
            em = MiscUtil.getEntityManagerInstance();
			
			//Checking whether the user setting optional attendees is student, admin or cc
			User user = (User) session.getAttribute("user");
			if (user.getRole().equals(Role.ADMINISTRATOR) || user.getRole().equals(Role.COURSE_COORDINATOR) ||
					user.getRole().equals(Role.STUDENT)) {
			
				//Getting input data from url
				JSONObject inputData = (JSONObject) new JSONObject (request.getParameter("jsonData"));
				JSONArray optionalAttendeesArray = (JSONArray) inputData.getJSONArray("attendees");
				
				//Constructing the optional attendees list to be stored in db
				HashSet<String> optionalAttendees = new HashSet<String>();

				for (int i = 0; i < optionalAttendeesArray.length(); i++) {
					String attendeeEmailAddress = optionalAttendeesArray.getString(i);
					//Validating the email address string
					boolean valid = validateEmailAddress(attendeeEmailAddress);
					if (valid == false) {
						json.put("error", true);
						json.put("message", "1 or more email addresses are invalid!");
						return SUCCESS;
					}
					//A duplicate email address will be replaced (due to hash set)
					optionalAttendees.add(attendeeEmailAddress);
				}
				
				//Getting the timeslot id linked to the optional attendees
				long timeslotId = Long.valueOf(inputData.getString("timeslotId"));
				Timeslot t = em.find(Timeslot.class, timeslotId);
				Booking b = t.getCurrentBooking();
				if (b != null) {
					em.getTransaction().begin();
					b.setOptionalAttendees(optionalAttendees);
					em.persist(b);
					em.getTransaction().commit();
					
					json.put("success", true);
					json.put("message", "Attendees have been invited for your booking!");
				} else {
					json.put("error", true);
					json.put("message", "No booking has been made at this timeslot!");
					return SUCCESS;
				}

				MiscUtil.logActivity(logger, user, "Optional attendees updated for " + b.toString());

				logItem.setMessage("Optional Attendees for the booking were updated successfully "
						+ "for " + b.toString());
				
			} else {
				//Incorrect user role
				json.put("error", true);
				json.put("message", "You cannot set the attendees for this booking!");
			}
		} catch (Exception e) {
			logItem.setSuccess(false);
			User userForLog = (User) session.getAttribute("user");
			logItem.setUser(userForLog);
			logItem.setMessage("Error: " + e.getMessage());
			
			logger.error("Exception caught: " + e.getMessage());
            if (MiscUtil.DEV_MODE) {
                for (StackTraceElement s : e.getStackTrace()) {
                    logger.debug(s.toString());
                }
            }
            json.put("success", false);
			json.put("exception", true);
            json.put("message", "Error with SetOptionalAttendees: Escalate to developers!");
        } finally {
			if (em != null) {
				//Saving job log in database
				if (!em.getTransaction().isActive()) em.getTransaction().begin();
				em.persist(logItem);
				em.getTransaction().commit();
				
				if (em.getTransaction().isActive()) em.getTransaction().rollback();
				if (em.isOpen()) em.close();
			}
		}
        return SUCCESS;
	 } //end of execute

	//To verify whether the string is a 'valid' email address
	public boolean validateEmailAddress(String emailAddress) {
		boolean result = false;
		try {
			InternetAddress emailAddr = new InternetAddress(emailAddress);
			emailAddr.validate();
			result = true;
		} catch (AddressException ex) {
			result = false;
		}
		return result;
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

	public HashMap<String, Object> getJson() {
		return json;
	}

	public void setJson(HashMap<String, Object> json) {
		this.json = json;
	}

	public ArrayList<HashMap<String, Object>> getData() {
		return data;
	}

	public void setData(ArrayList<HashMap<String, Object>> data) {
		this.data = data;
	}
}  // end of class
