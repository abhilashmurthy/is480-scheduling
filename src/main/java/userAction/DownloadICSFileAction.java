/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package userAction;

import static com.opensymphony.xwork2.Action.SUCCESS;
import com.opensymphony.xwork2.ActionSupport;
import constant.BookingStatus;
import constant.Role;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import manager.ICSFileManager;
import model.Booking;
import model.SystemActivityLog;
import model.User;
import model.role.Student;
import org.apache.struts2.interceptor.ServletRequestAware;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.MiscUtil;

/**
 *
 * @author suresh
 */
public class DownloadICSFileAction extends ActionSupport implements ServletRequestAware{
	
	private HttpServletRequest request;
	private static Logger logger = LoggerFactory.getLogger(DownloadICSFileAction.class);
	private HashMap<String, Object> json = new HashMap<String, Object>();

	@Override
	public String execute() throws Exception {
		HttpSession session = request.getSession();
		User user = (User)session.getAttribute("user");
		Calendar nowCal = Calendar.getInstance();
		Timestamp now = new Timestamp(nowCal.getTimeInMillis());
		
		SystemActivityLog logItem = new SystemActivityLog();
		logItem.setActivity("User: Download ICS File");
		logItem.setRunTime(now);
		logItem.setUser(user);
		logItem.setMessage("Error with download ICS file");
		logItem.setSuccess(true);
		
		EntityManager em = null;
        try {
			em = MiscUtil.getEntityManagerInstance();
			
			//List of all the bookings that will be included in the ICS file
			ArrayList<Booking> allBookings = new ArrayList<Booking>();
			
            Role role = user.getRole();
			if (role == Role.STUDENT) {
				Student student = em.find(Student.class, user.getId());
				Query studentSearch = em.createQuery("SELECT b FROM Booking b WHERE :student MEMBER OF b.requiredAttendees and b.bookingStatus IN (:status)");
				studentSearch.setParameter("student", student);
				studentSearch.setParameter("status", Arrays.asList(BookingStatus.APPROVED, BookingStatus.PENDING));
				allBookings.addAll(studentSearch.getResultList());
			}
			
			String downloadPath = ICSFileManager.createICSCalendar(allBookings, user, request.getSession().getServletContext());
			
			json.put("success", true);
			json.put("downloadPath", downloadPath);
			
			logItem.setMessage("ICS file downloaded by " + user.toString());
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
            json.put("message", "Oops, something went wrong. Please contact the administrator!");
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
		return SUCCESS;
	}

	public HashMap<String, Object> getJson() {
		return json;
	}

	public void setJson(HashMap<String, Object> json) {
		this.json = json;
	}

	public void setServletRequest(HttpServletRequest hsr) {
		request = hsr;
	}
	
}
