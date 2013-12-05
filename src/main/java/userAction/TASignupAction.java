/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package userAction;

import com.opensymphony.xwork2.ActionSupport;
import constant.Role;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import javax.persistence.EntityManager;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import manager.ScheduleManager;
import manager.TimeslotManager;
import model.Schedule;
import model.SystemActivityLog;
import model.Timeslot;
import model.User;
import model.role.TA;
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
public class TASignupAction extends ActionSupport implements ServletRequestAware {
	private static Logger logger = LoggerFactory.getLogger(TASignupAction.class);
	private HttpServletRequest request;
	private HashMap<String, Object> json = new HashMap<String, Object>();
   
    @Override
    public String execute() throws ServletException, IOException, JSONException {
        HttpSession session = request.getSession();
		
		Calendar nowCal = Calendar.getInstance();
		Timestamp now = new Timestamp(nowCal.getTimeInMillis());
		
		SystemActivityLog logItem = new SystemActivityLog();
		logItem.setActivity("TA Signup: Update");
		logItem.setRunTime(now);
		logItem.setUser((User)session.getAttribute("user"));
		logItem.setMessage("Error with validation / No changes made");
		logItem.setSuccess(true);
		
        EntityManager em = null;
        try {
            em = MiscUtil.getEntityManagerInstance();
            
            //get current user object
            em.clear();
            User oldUser = (User) session.getAttribute("user");
            User user = em.find(User.class, oldUser.getId());
            
            //check if the user is ta
            Role activeRole = (Role) session.getAttribute("activeRole");
            
            if(!(activeRole.equals(Role.TA))){
                json.put("success", false);
                json.put("message", "You are not a TA");
                return SUCCESS;
            }
            
            //set TA object
            TA ta = em.find(TA.class, user.getId());
            
            //get a list of timeslots in array format
            //JSONArray inputData = new JSONArray(request.getParameter("jsonData[timeslots]"));
            JSONObject allData = new JSONObject(request.getParameter("jsonData"));
            JSONArray inputData = allData.getJSONArray("timeslots");
            long scheduleId = allData.getLong("scheduleId");
            
            Schedule sc = ScheduleManager.findById(em, scheduleId);
            
            //get all timeslots for this schedule
            List<Timeslot> ts = TimeslotManager.findBySchedule(em, sc);
            
            for (Timeslot obj: ts) {
                TA check = obj.getTA();
                if (check == ta) {
                   em.getTransaction().begin();
                   obj.setTA(null);
                   em.persist(obj);
                   em.getTransaction().commit();
                }
            }
            
            //loop through the list to get timeslotID and set the new slots
            for(int i = 0; i < inputData.length(); i++) {
                long timeslotId = inputData.getLong(i);
                
                //get the respective timeslot object
                Timeslot t = TimeslotManager.findById(em, timeslotId);
                
                //if timeslot doesn't exists
                if (t == null) {
                    json.put("success", false);
                    json.put("message", "Timeslot with ID: " + timeslotId + " already taken");
                    return SUCCESS;
                }

                //else proceed to add this timeslot for that respective TA
                //set this timeslot's TA
                em.getTransaction().begin();
                t.setTA(ta);
                em.persist(t);
                em.getTransaction().commit();
            }
            
            //succesfully updated
            json.put("success",true);
            json.put("message", "You have succesfully signed up for all slots!");
			MiscUtil.logActivity(logger, user, "Sign-ups updated for " + sc.toString());
			
			logItem.setMessage("TA Sign-ups were updated successfully for " + sc.toString());
        } catch(Exception e) {
			logItem.setSuccess(false);
			User userForLog = (User) session.getAttribute("user");
			logItem.setUser(userForLog);
			logItem.setMessage("Error: " + e.getMessage());
			
            json.put("success", false);
            json.put("exception", true);
            json.put("message", "Error with TA Sign Up: Escalate to developers!");
			logger.error("Exception caught: " + e.getMessage());
            if (MiscUtil.DEV_MODE) {
                for (StackTraceElement s : e.getStackTrace()) {
                    logger.debug(s.toString());
                }
            }
        } finally {
			if (em != null) {
				if (em.getTransaction().isActive()) em.getTransaction().rollback();
				//Saving job log in database
				if (!em.getTransaction().isActive()) em.getTransaction().begin();
				em.persist(logItem);
				em.getTransaction().commit();
				if (em.isOpen()) em.close();
			}
		}
        return SUCCESS; //To change body of generated methods, choose Tools | Templates.
    }
    
    public void setServletRequest(HttpServletRequest hsr) {
        request = hsr;
    }

    public HashMap<String, Object> getJson() {
        return json;
    }

    public void setJson(HashMap<String, Object> json) {
        this.json = json;
    }
    
    
}
