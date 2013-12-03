/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package userAction;

import static com.opensymphony.xwork2.Action.SUCCESS;
import com.opensymphony.xwork2.ActionSupport;
import constant.BookingStatus;
import constant.Response;
import constant.Role;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import manager.BookingManager;
import manager.QuartzManager;
import model.Booking;
import model.SystemActivityLog;
import model.Timeslot;
import model.User;
import model.role.Faculty;
import notification.email.ApprovedBookingEmail;
import notification.email.ConfirmedBookingEmail;
import notification.email.RejectedBookingEmail;
import org.apache.struts2.interceptor.ServletRequestAware;
import org.json.JSONObject;
import org.hibernate.Hibernate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.MiscUtil;

/**
 *
 * @author Prakhar
 */
public class UpdateBookingStatusAction extends ActionSupport implements ServletRequestAware {

    private HttpServletRequest request;
	private HashMap<String, Object> json = new HashMap<String, Object>();
    private static Logger logger = LoggerFactory.getLogger(UpdateBookingStatusAction.class);

    @Override
    public String execute() throws Exception {
		HttpSession session = request.getSession();
		
		Calendar nowCal = Calendar.getInstance();
		Timestamp now = new Timestamp(nowCal.getTimeInMillis());
		
		SystemActivityLog logItem = new SystemActivityLog();
		logItem.setActivity("Booking: Approve/Reject");
		logItem.setRunTime(now);
		logItem.setUser((User)session.getAttribute("user"));
		logItem.setMessage("Error with validation / No changes made");
		logItem.setSuccess(true);
		
		EntityManager em = null;
        try {
			em = MiscUtil.getEntityManagerInstance();

			JSONObject inputObject = (JSONObject) new JSONObject(request.getParameter("jsonData"));
			long bookingId = Long.valueOf(inputObject.getString("bookingId"));
			//Status containing either approve or reject
			String status = inputObject.getString("status");

			String comment = null;
			Response response;
			if (status.equalsIgnoreCase("approve")) {
				response = Response.APPROVED;
			} else if (status.equalsIgnoreCase("reject")) {
				response = Response.REJECTED;
				//if status is rejected get the reson for rejection too (it could be blank)
				comment = inputObject.getString("comment");
			} else {
				logger.error("No valid response recorded from user");
				return SUCCESS;
			}

			User user = (User) session.getAttribute("user");

			//Getting the faculty object for the user. Cannot proceed if the user is not a faculty member.
			Faculty f;
			if (user.getRole() == Role.FACULTY) {
				f = em.find(Faculty.class, user.getId());
			} else {
				logger.error("User is not a faculty member");
				return SUCCESS;
			}
			
			em.getTransaction().begin();

			if (bookingId != 0) {
				//Retrieving the timeslot to update
				Booking booking = em.find(Booking.class, bookingId);
				
				//Retrieving the status list of the timeslot
				HashMap<User, Response> responseList = booking.getResponseList();
				if (responseList.containsKey(user)) { //Checking if the faculty is part of the response list for required attendees
					responseList.put(user, response);
				} else {
					logger.error("Faculty not found in responseList for required attendees");
					return SUCCESS;
				}
				
				//Storing the reason for rejection
				if (response == Response.REJECTED && comment != null) {
					booking.setComment(comment);
				}

				//Computing the overall status of the booking based on the new response
				int total = 0;
				Collection<Response> values = booking.getResponseList().values();
				for (Response r : values) {
					if (r == Response.REJECTED) {
						// Reject the booking if any one person has rejected it
						booking.setBookingStatus(BookingStatus.REJECTED);
						//Removing the current booking from the timeslot to make it available for others
						Timeslot t = booking.getTimeslot();
						t.setCurrentBooking(null);
						em.persist(t);
						break;
					} else if (r == Response.APPROVED) {
						total++;
						// Check if everyone has approved the booking
						if (total == values.size()) {
							booking.setBookingStatus(BookingStatus.APPROVED);
						}
					}
				}

				//Setting the new status
				booking.setResponseList(responseList);
				
				//Forcing initialization for sending email
				Hibernate.initialize(booking.getTeam().getMembers());
				Hibernate.initialize(booking.getTimeslot().getSchedule().getMilestone());
				Hibernate.initialize(booking.getRequiredAttendees());

				if (booking.getBookingStatus() == BookingStatus.APPROVED) {
					ConfirmedBookingEmail confirmationEmail = new ConfirmedBookingEmail(booking);
					confirmationEmail.sendEmail();
					QuartzManager.scheduleSMSReminder(booking, em, request);
				} else if (response == Response.APPROVED) {
					ApprovedBookingEmail approvedEmail = new ApprovedBookingEmail(booking, user);
					approvedEmail.sendEmail();
				} else if (response == Response.REJECTED) {
					RejectedBookingEmail rejectedEmail = new RejectedBookingEmail(booking, user);
					rejectedEmail.sendEmail();
				}
                                
				booking.setLastEditedBy(user.getFullName());
				booking.setLastEditedAt(new Timestamp(Calendar.getInstance().getTimeInMillis()));
			
				em.getTransaction().commit();
				
				//Setting the updated user object in session
				em.clear();
				Faculty newF = em.find(Faculty.class, f.getId());
				session.setAttribute("user", newF);

				json.put("success", true);
				if (response == Response.APPROVED) {
					json.put("message", "Your booking has been approved!");
				} else if (response == Response.REJECTED) {
					json.put("message", "Your booking has been rejected!");
				}
				
				MiscUtil.logActivity(logger, user, booking.toString() + " " + response.toString());
				
				logItem.setMessage("Booking was " + response.toString() + " successfully for " + booking.toString());
			} else {
				request.setAttribute("error", "No timeslot selected!");
				logger.error("User hasn't selected a timeslot to approve/reject!");
				return SUCCESS;
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
            json.put("message", "Error with UpdateBookingStatus: Escalate to developers!");
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
    }

    //Getters and Setters
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