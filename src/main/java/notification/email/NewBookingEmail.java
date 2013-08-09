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
import model.Timeslot;
import model.User;

/**
 *
 * @author suresh
 */
public class NewBookingEmail extends EmailTemplate{
	
	private Timeslot t;
	
	public NewBookingEmail(Timeslot t) {
		super("new_booking.html");
		this.t = t;
	}

	@Override
	public String generateEmailSubject() {
		return t.getSchedule().getMilestone().getName() + " - New Booking";
	}

	@Override
	public Set<String> generateRecipientList() {
		Set<String> emails = new HashSet<String>();
		for (User u : t.getTeam().getMembers()) {
			emails.add(u.getUsername() + "@smu.edu.sg");
		}
		
		return emails;
	}

	@Override
	public HashMap<String, String> prepareBodyData() {
		HashMap<String, String> map = new HashMap<String, String>();
		
		//Insert team name
		map.put("[TEAM_NAME]", t.getTeam().getTeamName());
		
		//Insert milestone name
		map.put("[MILESTONE]", t.getSchedule().getMilestone().getName());
		
		//Insert start date
		SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, d MMM yyyy");
		map.put("[DATE]", dateFormat.format(t.getStartTime()));
		
		//Insert start and end time
		SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm a");
		map.put("[START_TIME]", timeFormat.format(t.getStartTime()));
		map.put("[END_TIME]", timeFormat.format(t.getEndTime()));
		
		//Insert venue
		map.put("[VENUE]", t.getVenue());
		
		//Insert required attendees
		Set<User> userList = t.getAttendees();
		Iterator<User> iter = userList.iterator();
		StringBuilder result = new StringBuilder();
		
		while (iter.hasNext()) {
			result.append(iter.next().getFullName());
			if (iter.hasNext()) {
				result.append(",");
			}
		}
		map.put("[REQUIRED_ATTENDEES]", result.toString());
		
		return map;
	}
	
}
