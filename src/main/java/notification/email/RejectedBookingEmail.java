/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package notification.email;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import model.Booking;
import model.User;

/**
 *
 * @author suresh
 */
public class RejectedBookingEmail extends EmailTemplate{
	Booking b;
	User rejector;
	
	public RejectedBookingEmail(Booking b, User rejector) {
		super("rejected_booking.html");
		this.b = b;
		this.rejector = rejector;
	}

	@Override
	public String generateEmailSubject() {
		return b.getTimeslot().getSchedule().getMilestone().getName() + " - Booking Rejection";
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
		Set<String> emails = new HashSet<String>();
		emails.add(rejector.getUsername() + "@smu.edu.sg");
		return emails;
	}

	@Override
	public HashMap<String, String> prepareBodyData() {
		HashMap<String, String> map = new HashMap<String, String>();
		
		//Inserting milestone
		map.put("[MILESTONE]", b.getTimeslot().getSchedule().getMilestone().getName());
		
		//Inserting approver name
		map.put("[REJECTOR_NAME]", rejector.getFullName());
		
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
		Iterator<User> iter = userList.iterator();
		StringBuilder result = new StringBuilder();
		
		while (iter.hasNext()) {
			result.append("&nbsp;").append(iter.next().getFullName());
			if (iter.hasNext()) {
				result.append("<br />");
			}
		}
		map.put("[REQUIRED_ATTENDEES]", result.toString());
		
		return map;
	}
	
}
