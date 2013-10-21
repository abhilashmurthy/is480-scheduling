/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package userAction;

import static com.opensymphony.xwork2.Action.ERROR;
import static com.opensymphony.xwork2.Action.SUCCESS;
import com.opensymphony.xwork2.ActionSupport;
import constant.Role;
import java.util.ArrayList;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import model.Timeslot;
import org.apache.struts2.interceptor.ServletRequestAware;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.persistence.EntityManager;
import model.Booking;
import model.Team;
import model.User;
import util.MiscUtil;

/**
 *
 * @author Prakhar
 */
public class ViewSubscriptionsAction extends ActionSupport implements ServletRequestAware {

    private HttpServletRequest request;
    private static Logger logger = LoggerFactory.getLogger(ViewSubscriptionsAction.class);
    private ArrayList<HashMap<String, String>> data = new ArrayList<HashMap<String, String>>();

    @Override
    public String execute() throws Exception {
		EntityManager em = null;
        try {
			em = MiscUtil.getEntityManagerInstance();
            HttpSession session = request.getSession();
			User tempUser = (User) session.getAttribute("user");
			User user = em.find(User.class, tempUser.getId());
			
			Role activeRole = (Role) session.getAttribute("activeRole");
			if (activeRole.equals(Role.STUDENT) || activeRole.equals(Role.FACULTY) || activeRole.equals(Role.GUEST)
					|| activeRole.equals(Role.ADMINISTRATOR) || activeRole.equals(Role.TA)) {
				
				Set<Booking> subscribedTo;
				//Getting all the bookings the user has subscribed to
				subscribedTo = user.getSubscribedBookings();
				
				if (subscribedTo.size() > 0) {
					for (Booking b : subscribedTo) {
						Timeslot timeslot = b.getTimeslot();
						//Getting all the timeslot and booking details
						HashMap<String, String> map = new HashMap<String, String>();
						
						//SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy HH:mm aa");
						SimpleDateFormat sdfForStartTime = new SimpleDateFormat("MMM dd, HH:mm");
//						SimpleDateFormat sdfForStartTime = new SimpleDateFormat("HH:mm");
						SimpleDateFormat sdfForEndTime = new SimpleDateFormat("HH:mm aa");
						
						String venue = timeslot.getVenue();
						
						Team team = b.getTeam();
						String teamName = team.getTeamName();
						
						String time = sdfForStartTime.format(timeslot.getStartTime()) + " - " + 
								sdfForEndTime.format(timeslot.getEndTime());
						
						map.put("teamName", teamName);
						map.put("time", time);
						map.put("venue", venue);
//						map.put("wikiLink", wikiLink);

						data.add(map);
					}
				}
				return SUCCESS;
			} else {
				request.setAttribute("error", "Oops. You're not authorized to access this page!");
				MiscUtil.logActivity(logger, user, "User cannot access this page");
				return ERROR;
			}
        } catch (Exception e) {
            logger.error("Exception caught: " + e.getMessage());
            if (MiscUtil.DEV_MODE) {
                for (StackTraceElement s : e.getStackTrace()) {
                    logger.debug(s.toString());
                }
            }
            request.setAttribute("error", "Error with ViewSubscriptionsAction: Escalate to developers!");
            return ERROR;
        } finally {
			if (em != null && em.getTransaction().isActive()) em.getTransaction().rollback();
			if (em != null && em.isOpen()) em.close();
		}
    }
	
    public ArrayList<HashMap<String, String>> getData() {
        return data;
    }

    public void setData(ArrayList<HashMap<String, String>> data) {
        this.data = data;
    }

    public void setServletRequest(HttpServletRequest hsr) {
        this.request = hsr;
    }
}  //end of class