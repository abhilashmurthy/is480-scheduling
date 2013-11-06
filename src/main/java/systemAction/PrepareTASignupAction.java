/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package systemAction;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.opensymphony.xwork2.ActionSupport;
import constant.Role;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import javax.persistence.EntityManager;
import javax.persistence.Persistence;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import manager.ScheduleManager;
import manager.SettingsManager;
import manager.TermManager;
import manager.UserManager;
import model.Schedule;
import model.Settings;
import model.Team;
import model.Term;
import model.Timeslot;
import model.User;
import model.role.Faculty;
import model.role.Student;
import model.role.TA;
import org.apache.struts2.interceptor.ServletRequestAware;
import org.hibernate.Hibernate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.MiscUtil;

/**
 *
 * @author Prakhar
 */
public class PrepareTASignupAction extends ActionSupport implements ServletRequestAware {
	private String taJson;
	private ArrayList<HashMap<String, Object>> taData = new ArrayList<HashMap<String, Object>>();
	
    private HttpServletRequest request;
    private static Logger logger = LoggerFactory.getLogger(PrepareTASignupAction.class);
	
	@Override
    public String execute() {
		EntityManager em = null;
		Gson gson = new GsonBuilder().disableHtmlEscaping().create();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		
        try {
            em = MiscUtil.getEntityManagerInstance();
			HttpSession session = request.getSession();
			
			User user = (User) session.getAttribute("user");
			if (user.getRole().equals(Role.ADMINISTRATOR) || user.getRole().equals(Role.COURSE_COORDINATOR) || user.getRole().equals(Role.TA)) {
				em.getTransaction().begin();
				
				Term term = (Term) session.getAttribute("currentActiveTerm");
				
				//TA MANAGEMENT
				List<User> taUsers = UserManager.findByTermAndRole(em, term, Role.TA);
				for (User taUser : taUsers) {
					TA ta = (TA) taUser;
					HashMap<String, Object> taMap = new HashMap<String, Object>();
					taMap.put("id", ta.getId());
					taMap.put("name", ta.getFullName());
					taMap.put("username", ta.getUsername());
					List<Schedule> schedules = ScheduleManager.findByTerm(em, term);
					List<HashMap<String, Object>> mySignups = new ArrayList<HashMap<String, Object>>();
					for (Schedule schedule : schedules) {
						for (Timeslot timeslot : schedule.getTimeslots()) {
							if (timeslot.getTA() != null && timeslot.getTA().equals(ta)) {
								HashMap<String, Object> signupMap = new HashMap<String, Object>();
								signupMap.put("datetime", sdf.format(timeslot.getStartTime()));
								signupMap.put("milestone", schedule.getMilestone().getName());
								signupMap.put("timeslotId", timeslot.getId());
								signupMap.put("scheduleId", schedule.getId());
								mySignups.add(signupMap);
							}
						}
					}
					taMap.put("mySignups", mySignups);
					taData.add(taMap);
				}
				
				taJson = gson.toJson(taData);
				em.getTransaction().commit();
			}
		} catch (Exception e) {
			logger.error("Exception caught: " + e.getMessage());
            if (MiscUtil.DEV_MODE) {
                for (StackTraceElement s : e.getStackTrace()) {
                    logger.debug(s.toString());
                }
            }
        } finally {
			if (em != null && em.getTransaction().isActive()) em.getTransaction().rollback();
			if (em != null && em.isOpen()) em.close();
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

	public ArrayList<HashMap<String, Object>> getTaData() {
		return taData;
	}

	public void setTaData(ArrayList<HashMap<String, Object>> taData) {
		this.taData = taData;
	}

	public String getTaJson() {
		return taJson;
	}

	public void setTaJson(String taJson) {
		this.taJson = taJson;
	}
	
}
