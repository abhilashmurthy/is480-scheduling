/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package notification.email;

import constant.Status;
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
public class RespondToBookingEmail extends EmailTemplate{
	
	private Timeslot t;
	
	public RespondToBookingEmail(Timeslot t) {
		super("respond_to_booking.html");
		this.t = t;
	}

	@Override
	public String generateEmailSubject() {
		return t.getSchedule().getMilestone().getName() + " - Approve Booking";
	}

	@Override
	public Set<String> generateToAddressList() {
		HashSet<String> emails = new HashSet<String>();
		HashMap<User, Status> map = t.getStatusList();
		for (User u : map.keySet()) {
			emails.add(u.getUsername() + "@smu.edu.sg");
		}
		
		return emails;
	}

	@Override
	public HashMap<String, String> prepareBodyData() {
		HashMap<String, String> map = new HashMap<String, String>();
		
		//Insert milestone
		map.put("[MILESTONE]", t.getSchedule().getMilestone().getName());
		
		//Insert team name
		map.put("[TEAM_NAME]", t.getTeam().getTeamName());
		
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
		Set<User> userList = t.getStatusList().keySet();
		Iterator<User> iter = userList.iterator();
		StringBuilder result = new StringBuilder();
		
		int numBullet = 1;
		while (iter.hasNext()) {
			result.append(numBullet++).append(". ")
					.append(iter.next().getFullName());
			if (iter.hasNext()) {
				result.append("<br />");
			}
		}
		map.put("[REQUIRED_ATTENDEES]", result.toString());
		
		return map;
	}

	@Override
	public Set<String> generateCCAddressList() {
		return null;
	}
	
}
