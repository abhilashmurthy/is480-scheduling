/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package userAction;

import static com.opensymphony.xwork2.Action.ERROR;
import static com.opensymphony.xwork2.Action.SUCCESS;
import com.opensymphony.xwork2.ActionSupport;
import constant.BookingStatus;
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
import java.util.List;
import java.util.Set;
import javax.persistence.EntityManager;
import manager.UserManager;
import model.Booking;
import model.Team;
import model.User;
import util.MiscUtil;

/**
 *
 * @author Prakhar
 */
public class ViewSubscribedBookingsAction extends ActionSupport implements ServletRequestAware {

    private HttpServletRequest request;
    private static Logger logger = LoggerFactory.getLogger(ViewSubscribedBookingsAction.class);
    private ArrayList<HashMap<String, String>> data = new ArrayList<HashMap<String, String>>();

    @Override
    public String execute() throws Exception {
		EntityManager em = null;
        try {
			em = MiscUtil.getEntityManagerInstance();
            HttpSession session = request.getSession();
			User user = (User) session.getAttribute("user");
			
			Role activeRole = (Role) session.getAttribute("activeRole");
			//Need to change this for guests. Guests need to be users in our db before they can access any feature
			if (activeRole.equals(Role.STUDENT) || activeRole.equals(Role.FACULTY) || activeRole.equals(Role.GUEST)
					|| activeRole.equals(Role.TA)) {
				
				ArrayList<Booking> subscribedTo;
				//Getting all the bookings the user has subscribed to
				subscribedTo = UserManager.getSubscribedBookings(em, user.getEmail());
				
				//Sort the bookings in ascending order of time
				if (subscribedTo.size() > 0) {
					
					Long[] ts = new Long[subscribedTo.size()];
					int i = 0;
					for (Booking b: subscribedTo) {
						ts[i] = b.getTimeslot().getStartTime().getTime();
						i++;
					}
					ts = sortTimestamps(ts);
					
					List<Booking> subscribedBookings = new ArrayList<Booking>();
					for (int j = 0; j < ts.length; j++) {
						for (Booking b: subscribedTo) {
							if (b.getTimeslot().getStartTime().getTime() == ts[j]) {
								subscribedBookings.add(b);
								break;
							}
						}
					}
					
					//Now sorting the bookings by their status - APPROVED, REJECTED, DELETED
					List<Booking> sBookings = new ArrayList<Booking>();
					for (Booking b: subscribedBookings) {
						if (b.getBookingStatus() == BookingStatus.APPROVED) {
							sBookings.add(b);
						}
					}
					for (Booking b: subscribedBookings) {
						if (b.getBookingStatus() == BookingStatus.REJECTED) {
							sBookings.add(b);
						}
					}
					for (Booking b: subscribedBookings) {
						if (b.getBookingStatus() == BookingStatus.DELETED) {
							sBookings.add(b);
						}
					}
					
					for (Booking b : sBookings) {
						Timeslot timeslot = b.getTimeslot();
						//Getting all the timeslot and booking details
						HashMap<String, String> map = new HashMap<String, String>();
						
						//SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy HH:mm aa");
						SimpleDateFormat sdfForStartTime = new SimpleDateFormat("MMM dd, HH:mm");
//						SimpleDateFormat sdfForStartTime = new SimpleDateFormat("HH:mm");
						SimpleDateFormat sdfForEndTime = new SimpleDateFormat("HH:mm aa");
						
						long bookingId = b.getId();
						
						String milestoneName = b.getTimeslot().getSchedule().getMilestone().getName();
						String termName = b.getTimeslot().getSchedule().getMilestone().getTerm().getDisplayName();
						String termMilestone = milestoneName + ", " + termName;
						
						String venue = timeslot.getVenue();
						
						Team team = b.getTeam();
						String teamName = team.getTeamName();
						
						String time = sdfForStartTime.format(timeslot.getStartTime()) + " - " + 
								sdfForEndTime.format(timeslot.getEndTime());
						
						String wikiLink = "";
						if (team.getWiki() != null) {
							wikiLink = team.getWiki();
						}
						
						String bookingStatus = b.getBookingStatus().toString();
						
						map.put("bookingId", String.valueOf(bookingId));
						map.put("bookingStatus", bookingStatus);
						map.put("teamName", teamName);
						map.put("termMilestone", termMilestone);
						map.put("time", time);
						map.put("venue", venue);
						map.put("wikiLink", wikiLink);

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
	
	//Sorting timestamps in increasing order of bookings 
	private static Long[] sortTimestamps(Long[] ts) {
		for (int i = 0; i < ts.length - 1; i++) {
			for (int j = 1; j < ts.length; j++) {
				if (ts[j] < ts[i]) {
					long temp = 0;
					temp = ts[i];
					ts[i] = ts[j];
					ts[j] = temp;
				}
			}
		}
		return ts;
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