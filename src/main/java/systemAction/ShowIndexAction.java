/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package systemAction;

import static com.opensymphony.xwork2.Action.SUCCESS;
import com.opensymphony.xwork2.ActionSupport;
import constant.Status;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import javax.persistence.EntityManager;
import javax.persistence.Persistence;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import manager.ScheduleManager;
import manager.TermManager;
import model.Schedule;
import model.Term;
import model.Timeslot;
import model.User;
import org.apache.struts2.interceptor.ServletRequestAware;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import userAction.DeleteBookingAction;
import util.MiscUtil;

/**
 *
 * @author Prakhar
 */
public class ShowIndexAction extends ActionSupport implements ServletRequestAware {
	
	private long termId;   //To get the active term id user chooses
	private int pendingBookingCount;  //To display the number of pending bookings on the index page
	private ArrayList<HashMap<String, String>> data = new ArrayList<HashMap<String, String>>();
	private HashMap<String, Object> json = new HashMap<String, Object>();
    private HttpServletRequest request;
    private static Logger logger = LoggerFactory.getLogger(ShowIndexAction.class);
	
	@Override
    public String execute() throws Exception {
		try {
			EntityManager em = Persistence.createEntityManagerFactory(MiscUtil.PERSISTENCE_UNIT).createEntityManager();
			HttpSession session = request.getSession();
			User user = (User) session.getAttribute("user");
			
			//<----- 1st Part: To get number of pending bookings ------>
			//Checking whether user is supervisor/reviewer
			String activeRole = (String) session.getAttribute("activeRole");
			if (activeRole.equalsIgnoreCase("Supervisor/Reviewer")) { 
				//Objective: Getting bookings with pending status (if any) for active term
				Term activeTerm = (Term) session.getAttribute("currentActiveTerm");

				//Getting all schedules for the active term
				List<Schedule> listSchedules = ScheduleManager.findByTerm(em, activeTerm);
				//Getting all timeslots for the user
				Set<Timeslot> userTimeslots = user.getTimeslots();

				if (userTimeslots.size() > 0) {
					for (Timeslot timeslot: userTimeslots) {
						for (Schedule schedule: listSchedules) {
							//Checking if the timeslot is part of active term
							if (timeslot.getSchedule().equals(schedule)) {
								HashMap<User, Status> statusList = timeslot.getStatusList();
								String status = statusList.get(user).toString();
								if (status.equalsIgnoreCase("Pending")) {
									pendingBookingCount++;
								}
							}
						}
					}
				}
			}  //end of outer if
			
			//<----- 2nd Part: Displaying all the terms for the user to choose from ------>
			List<Term> allTerms = TermManager.getAllTerms(em);
			if (allTerms.size() > 0) {
				for (Term term: allTerms) {
					HashMap<String, String> map = new HashMap<String, String>();
					long idOfTerm = term.getId();
					String semester = term.getSemester();
					int startAcademicYear = term.getAcademicYear();
					int endAcademicYear = startAcademicYear + 1;
					String academicYear = String.valueOf(startAcademicYear) + "-" + String.valueOf(endAcademicYear);
					
					map.put("termName", (academicYear + " " + semester));
					map.put("termId", String.valueOf(idOfTerm));
					
					data.add(map);
				}
			}
			
			//<----- 3rd Part: To set the current active term based on users response ------>
			//Active term is set during login. Only if user selects a term from UI will the new active term be set
			if (termId != 0) {
				Term activeTerm = TermManager.findTermById(em, termId);
				if (activeTerm != null) {
					session.setAttribute("currentActiveTerm", activeTerm);
				}
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
            json.put("message", "Error with Showing Index Page: Escalate to developers!");
        }
        return SUCCESS;
	} //end of execute
		
	public void setServletRequest(HttpServletRequest hsr) {
        request = hsr;
    }

	public long getTermId() {
		return termId;
	}

	public void setTermId(long termId) {
		this.termId = termId;
	}

	public int getPendingBookingCount() {
		return pendingBookingCount;
	}

	public void setPendingBookingCount(int pendingBookingCount) {
		this.pendingBookingCount = pendingBookingCount;
	}

	public HashMap<String, Object> getJson() {
		return json;
	}

	public void setJson(HashMap<String, Object> json) {
		this.json = json;
	}

	public ArrayList<HashMap<String, String>> getData() {
		return data;
	}

	public void setData(ArrayList<HashMap<String, String>> data) {
		this.data = data;
	}
} //end of class
