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
import manager.UserManager;
import model.Booking;
import model.SystemActivityLog;
import model.User;
import model.role.Faculty;
import model.role.Student;
import model.role.TA;
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
		
		EntityManager em = null;
        try {
			em = MiscUtil.getEntityManagerInstance();
			
			//List of all the bookings that will be included in the ICS file
			ArrayList<Booking> allBookings = new ArrayList<Booking>();
			
            Role role = user.getRole();
			if (role == Role.STUDENT) {
				Student student = em.find(Student.class, user.getId());
				Query studentSearch = em.createQuery("SELECT b FROM Booking b WHERE :student MEMBER OF b.requiredAttendees AND b.bookingStatus IN (:status)");
				studentSearch.setParameter("student", student);
				studentSearch.setParameter("status", Arrays.asList(BookingStatus.APPROVED, BookingStatus.PENDING));
				allBookings.addAll(studentSearch.getResultList());
			} else if (role == Role.FACULTY) {
				Faculty faculty = em.find(Faculty.class, user.getId());
				Query facultySearch = em.createQuery("SELECT b FROM Booking b WHERE :faculty MEMBER OF b.requiredAttendees AND b.bookingStatus IN (:status)");
				facultySearch.setParameter("faculty", faculty);
				facultySearch.setParameter("status", Arrays.asList(BookingStatus.APPROVED, BookingStatus.PENDING));
				allBookings.addAll(facultySearch.getResultList());
			} else if (role == Role.TA) {
				TA ta = em.find(TA.class, user.getId());
				Query taSearch = em.createQuery("SELECT b FROM Booking b WHERE b.timeslot.TA = :ta AND b.bookingStatus IN (:status)");
				taSearch.setParameter("ta", ta);
				taSearch.setParameter("status", Arrays.asList(BookingStatus.APPROVED, BookingStatus.PENDING));
				allBookings.addAll(taSearch.getResultList());
			}
			
			//Adding all RSVPs
			allBookings.addAll(UserManager.getSubscribedBookings(em, user.getEmail()));
			
			if (allBookings.size() > 0) {
				String downloadPath = ICSFileManager.createICSCalendar(allBookings, user, request.getSession().getServletContext());
				json.put("success", true);
				json.put("downloadPath", downloadPath);
			} else {
				json.put("success", false);
				json.put("message", "You don't have any presentations for this term!");
			}
        } catch (Exception e) {
			
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
