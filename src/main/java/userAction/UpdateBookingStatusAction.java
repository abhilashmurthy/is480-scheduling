/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package userAction;

import static com.opensymphony.xwork2.Action.ERROR;
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
import javax.persistence.Persistence;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import manager.BookingManager;
import model.Booking;
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
		EntityManager em = null;
        try {
			em = Persistence.createEntityManagerFactory(MiscUtil.PERSISTENCE_UNIT).createEntityManager();

			JSONObject inputObject = (JSONObject) new JSONObject(request.getParameter("jsonData"));
			long bookingId = Long.valueOf(inputObject.getString("bookingId"));
			//Status containing either approve or reject
			String status = inputObject.getString("status");

			String rejectReason = null;
			Response response = null;
			if (status.equalsIgnoreCase("approve")) {
				response = Response.APPROVED;
			} else if (status.equalsIgnoreCase("reject")) {
				response = Response.REJECTED;
				//if status is rejected get the reson for rejection too (it could be blank)
				rejectReason = inputObject.getString("rejectReason");
			} else {
				logger.error("No valid response recorded from user");
				return ERROR;
			}

			HttpSession session = request.getSession();
			User user = (User) session.getAttribute("user");

			//Getting the faculty object for the user. Cannot proceed if the user is not a faculty member.
			Faculty f;
			if (user.getRole() == Role.FACULTY) {
				f = em.find(Faculty.class, user.getId());
			} else {
				logger.error("User is not a faculty member");
				return ERROR;
			}

			//The list of slots to update in db
			List<Booking> bookingsToUpdate = new ArrayList<Booking>();

			if (bookingId != 0) {
				//Retrieving the timeslot to update
				Booking booking = em.find(Booking.class, bookingId);
				
				//Forcing initialization for sending email
				Hibernate.initialize(booking.getTeam().getMembers());
				Hibernate.initialize(booking.getTimeslot().getSchedule().getMilestone());
				
				//Retrieving the status list of the timeslot
				HashMap<User, Response> responseList = booking.getResponseList();
				if (responseList.containsKey(user)) { //Checking if the faculty is part of the response list for required attendees
					responseList.put(user, response);
					if (response == Response.APPROVED) {
						ApprovedBookingEmail approvedEmail = new ApprovedBookingEmail(booking, user);
						approvedEmail.sendEmail();
					} else if (response == Response.REJECTED) {
						RejectedBookingEmail rejectedEmail = new RejectedBookingEmail(booking, user);
						rejectedEmail.sendEmail();
					}
				} else {
					logger.error("Faculty not found in responseList for required attendees");
					return ERROR;
				}
				
				//Storing the reason for rejection
				if (response == Response.REJECTED && rejectReason != null) {
					booking.setRejectReason(rejectReason);
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
							break;
						}
					}
				}

				//Setting the new status
				booking.setResponseList(responseList);
				bookingsToUpdate.add(booking);

				//TODO Send an email when booking's rejected
				if (booking.getBookingStatus() == BookingStatus.APPROVED) {
					ConfirmedBookingEmail confirmationEmail = new ConfirmedBookingEmail(booking);
					confirmationEmail.sendEmail();
				}
                                
                                booking.setLastEditedBy(user.getFullName());
                                booking.setLastEditedAt(new Timestamp(Calendar.getInstance().getTimeInMillis()));
			
				//Updating the time slot 
				EntityTransaction transaction = em.getTransaction();
				boolean result = BookingManager.updateBookings(em, bookingsToUpdate, transaction);
				if (result == true) {
					//em.close();
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
				}
			} else {
				request.setAttribute("error", "No timeslot selected!");
				logger.error("User hasn't selected a timeslot to approve/reject!");
				return ERROR;
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
            json.put("message", "Error with UpdateBookingStatus: Escalate to developers!");
        } finally {
			if (em != null && em.getTransaction().isActive()) em.getTransaction().rollback();
			if (em != null && em.isOpen()) em.close();
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