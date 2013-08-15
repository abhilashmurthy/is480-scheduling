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
import model.Team;
import model.Timeslot;
import model.User;

/**
 *
 * @author suresh
 */
public class DeletedBookingEmail extends EmailTemplate{
	Timeslot t;
	Team team;
	HashMap<User, Status> statusList;
	User deletor;
	
	public DeletedBookingEmail(Timeslot t, User deletor, Team team,
			HashMap<User, Status> statusList) {
		super("deleted_booking.html");
		this.t = t;
		this.deletor = deletor;
		this.team = team;
		this.statusList = statusList;
	}

	@Override
	public String generateEmailSubject() {
		return t.getSchedule().getMilestone().getName() + " - Booking Deletion";
	}

	@Override
	public Set<String> generateToAddressList() {
		Set<String> emails = new HashSet<String>();
		for (User u : team.getMembers()) {
			emails.add(u.getUsername() + "@smu.edu.sg");
		}
		
		return emails;
	}

	@Override
	public Set<String> generateCCAddressList() {
		HashSet<String> emails = new HashSet<String>();
		
		//Adding required attendees
		for (User u : statusList.keySet()) {
			emails.add(u.getUsername() + "@smu.edu.sg");
		}
		return emails;
	}

	@Override
	public HashMap<String, String> prepareBodyData() {
		HashMap<String, String> map = new HashMap<String, String>();
		
		//Inserting milestone
		map.put("[MILESTONE]", t.getSchedule().getMilestone().getName());
		
		//Inserting approver name
		map.put("[DELETOR_NAME]", deletor.getFullName());
		
		//Insert team name
		map.put("[TEAM_NAME]", team.getTeamName());
		
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
		Set<User> userList = statusList.keySet();
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
