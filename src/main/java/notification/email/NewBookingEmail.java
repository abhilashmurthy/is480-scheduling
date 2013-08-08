/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package notification.email;

import java.text.SimpleDateFormat;
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
	public String generateEmailBody() {
		//Insert team name
		body = body.replace("[TEAM_NAME]", t.getTeam().getTeamName());
		
		//Insert milestone name
		body = body.replace("[MILESTONE]", t.getSchedule().getMilestone().getName());
		
		//Insert start date
		SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, d MMM yyyy");
		body = body.replace("[DATE]", dateFormat.format(t.getStartTime()));
		
		//Insert start and end time
		SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm a");
		body = body.replace("[START_TIME]", timeFormat.format(t.getStartTime()));
		body = body.replace("[END_TIME]", timeFormat.format(t.getEndTime()));
		
		//Insert venue
		body = body.replace("[VENUE]", t.getVenue());
		
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
		body = body.replace("[REQUIRED_ATTENDEES]", result);
		
		return body;
	}
	
}
