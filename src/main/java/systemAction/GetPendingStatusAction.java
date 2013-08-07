/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package systemAction;

import static com.opensymphony.xwork2.Action.ERROR;
import static com.opensymphony.xwork2.Action.SUCCESS;
import com.opensymphony.xwork2.ActionSupport;
import constant.Status;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.persistence.EntityManager;
import javax.persistence.Persistence;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import manager.ScheduleManager;
import model.Schedule;
import model.Term;
import model.Timeslot;
import model.User;
import org.apache.struts2.interceptor.ServletRequestAware;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import userAction.SetRolesAction;
import util.MiscUtil;

/**
 * To check whether there are pending bookings (for supervisor/reviewer only)  
 * @author Prakhar
 */
public class GetPendingStatusAction extends ActionSupport implements ServletRequestAware {
	
	private int pendingBookingCount;
	private HttpServletRequest request;
	private static Logger logger = LoggerFactory.getLogger(GetPendingStatusAction.class);
	
	@Override
    public String execute() throws Exception {
		try {
			EntityManager em = Persistence.createEntityManagerFactory(MiscUtil.PERSISTENCE_UNIT).createEntityManager();
			HttpSession session = request.getSession();
			
			//Checking whether user is supervisor/reviewer
			String activeRole = (String) session.getAttribute("activeRole");
			if (activeRole.equalsIgnoreCase("Supervisor") || activeRole.equalsIgnoreCase("Reviewer")) {
				//Objective: Getting bookings with pending status (if any) for active term
				Term activeTerm = (Term) session.getAttribute("currentActiveTerm");
				User user = (User) session.getAttribute("user");

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
			}
		} catch (Exception e) {
			logger.error("Exception caught: " + e.getMessage());
            if (MiscUtil.DEV_MODE) {
                for (StackTraceElement s : e.getStackTrace()) {
                    logger.debug(s.toString());
                }
            }
            request.setAttribute("error", "Error with Getting Pending Status: Escalate to developers!");
            return ERROR;
		}
		return SUCCESS;
	}

	public void setServletRequest(HttpServletRequest hsr) {
        this.request = hsr;
    }

	public int getPendingBookingCount() {
		return pendingBookingCount;
	}

	public void setPendingBookingCount(int pendingBookingCount) {
		this.pendingBookingCount = pendingBookingCount;
	}
	
} //end of class
