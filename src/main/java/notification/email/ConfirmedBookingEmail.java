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
import javax.persistence.EntityManager;
import javax.persistence.Persistence;
import manager.UserManager;
import model.Timeslot;
import model.User;
import util.MiscUtil;

/**
 *
 * @author suresh
 */
public class ConfirmedBookingEmail extends EmailTemplate{
	private Timeslot t;
	
	public ConfirmedBookingEmail(Timeslot t) {
		super("confirmed_booking.html");
		this.t = t;
	}

	@Override
	public String generateEmailSubject() {
		return t.getSchedule().getMilestone().getName() + " - Booking Confirmed";
	}

	@Override
	public Set<String> generateToAddressList() {
		Set<String> emails = new HashSet<String>();
		for (User u : t.getTeam().getMembers()) {
			emails.add(u.getUsername() + "@smu.edu.sg");
		}
		
		return emails;
	}

	@Override
	public Set<String> generateCCAddressList() {
		EntityManager em = Persistence.createEntityManagerFactory(MiscUtil.PERSISTENCE_UNIT).createEntityManager();
		HashMap<User, Status> statusList = t.getStatusList();
		HashSet<String> emails = new HashSet<String>();
		
		//Adding required attendees
		for (User u : statusList.keySet()) {
			emails.add(u.getUsername() + "@smu.edu.sg");
		}
		//Adding the course coordinator
		emails.add(UserManager.getCourseCoordinator(em).getUsername() + "@smu.edu.sg");
		
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
		Set<User> userList = t.getStatusList().keySet();
		Iterator<User> iter = userList.iterator();
		StringBuilder result = new StringBuilder();
		
		while (iter.hasNext()) {
			result.append("&nbsp;").append(iter.next().getFullName());
			if (iter.hasNext()) {
				result.append("<br />");
			}
		}
		map.put("[CONFIRMED_ATTENDEES]", result.toString());
		
		return map;
	}
	
}
