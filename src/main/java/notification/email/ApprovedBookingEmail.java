/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package notification.email;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import model.Booking;
import model.User;

/**
 *
 * @author suresh
 */
public class ApprovedBookingEmail extends EmailTemplate{
	Booking b;
	User approver;
	
	public ApprovedBookingEmail(Booking b, User approver) {
		super("approved_booking.html");
		this.b = b;
		this.approver = approver;
	}

	@Override
	public String generateEmailSubject() {
		return b.getTimeslot().getSchedule().getMilestone().getName() + " - Booking Approval";
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
		emails.add(approver.getUsername() + "@smu.edu.sg");
		return emails;
	}

	@Override
	public HashMap<String, String> prepareBodyData() {
		HashMap<String, String> map = new HashMap<String, String>();
		
		//Inserting milestone
		map.put("[MILESTONE]", b.getTimeslot().getSchedule().getMilestone().getName());
		
		//Inserting approver name
		map.put("[APPROVER_NAME]", approver.getFullName());
		
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
