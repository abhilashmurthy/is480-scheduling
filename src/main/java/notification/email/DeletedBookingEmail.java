/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package notification.email;

import constant.Response;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.EntityManager;
import javax.persistence.Persistence;
import manager.UserManager;
import model.Booking;
import model.User;
import util.MiscUtil;

/**
 *
 * @author suresh
 */
public class DeletedBookingEmail extends EmailTemplate{
	Booking b;
	User deletor;
	
	public DeletedBookingEmail(Booking b, User deletor) {
		super("deleted_booking.html");
		this.b = b;
		this.deletor = deletor;
	}

	@Override
	public String generateEmailSubject() {
		return b.getTimeslot().getSchedule().getMilestone().getName() + " - Booking Deletion";
	}

	@Override
	public Set<String> generateToAddressList() {
		Set<String> emails = new HashSet<String>();
		for (User u : b.getTeam().getMembers()) {
			emails.add(u.getUsername() + "@smu.edu.sg");
		}
		
		return emails;
	}

	@Override
	public Set<String> generateCCAddressList() {
		EntityManager em = null;
		try {
			em = Persistence.createEntityManagerFactory(MiscUtil.PERSISTENCE_UNIT).createEntityManager();
			HashSet<String> emails = new HashSet<String>();

			//Adding required attendees
			for (User u : b.getResponseList().keySet()) {
				emails.add(u.getUsername() + "@smu.edu.sg");
			}

			//Check if the booking was previously confirmed
			boolean confirmed = true;
			for (Response r : b.getResponseList().values()) {
				if (r == Response.PENDING || r == Response.REJECTED) {
					confirmed = false;
					break;
				}
			}
			//Adding the course coordinator and optional attendees if this is a previously confirmed booking
			if (confirmed) {
				//Adding the course coordinator
				emails.add(UserManager.getCourseCoordinator(em).getUsername() + "@smu.edu.sg");

				//Adding the optional attendees
				for (String s : b.getOptionalAttendees()) {
					emails.add(s);
				}
			}
			return emails;
		} finally {
			if (em != null && em.isOpen()) em.close();
		}
	}

	@Override
	public HashMap<String, String> prepareBodyData() {
		HashMap<String, String> map = new HashMap<String, String>();
		
		//Inserting milestone
		map.put("[MILESTONE]", b.getTimeslot().getSchedule().getMilestone().getName());
		
		//Inserting approver name
		map.put("[DELETOR_NAME]", deletor.getFullName());
		
		//Insert team name
		map.put("[TEAM_NAME]", b.getTeam().getTeamName());
		
		//Insert start date
		SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, d MMM yyyy");
		map.put("[DATE]", dateFormat.format(b.getTimeslot().getStartTime()));
		
		//Insert start and end time
		SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm a");
		map.put("[START_TIME]", timeFormat.format(b.getTimeslot().getStartTime()));
		map.put("[END_TIME]", timeFormat.format(b.getTimeslot().getEndTime()));
		
		//Insert venue
		map.put("[VENUE]", b.getTimeslot().getVenue());
		
		//Insert required attendees
		Set<User> userList = b.getResponseList().keySet();
		StringBuilder result = new StringBuilder();
		
		for(User u : userList) {
			result.append("&nbsp;").append(u.getFullName());
		}
		map.put("[REQUIRED_ATTENDEES]", result.toString());
		
		return map;
	}
	
}
