/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package notification.email;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import manager.ICSFileManager;
import manager.UserManager;
import model.Booking;
import model.User;

/**
 *
 * @author suresh
 */
public class ConfirmedBookingEmail extends EmailTemplate{
	private Booking b;
	
	public ConfirmedBookingEmail(Booking b) {
		super("confirmed_booking.html");
		this.b = b;
	}

	@Override
	public String generateEmailSubject() {
		return generateBookingSubjectTitle(b, "Booking confirmed for ");
	}

	@Override
	public Set<String> generateToAddressList() {
		Set<String> emails = new HashSet<String>();
		for (User u : b.getTeam().getMembers()) {
			emails.add(u.getEmail());
		}
		
		return emails;
	}

	@Override
	public Set<String> generateCCAddressList() {
		HashSet<String> emails = new HashSet<String>();

		//Adding required attendees
		for (User u : b.getResponseList().keySet()) {
			emails.add(u.getEmail());
		}
		//Adding the course coordinator
		emails.add(UserManager.getCourseCoordinator(em).getEmail());

		//Adding the optional attendees
		for (String s : b.getOptionalAttendees()) {
			emails.add(s);
		}
		return emails;
	}

	@Override
	public HashMap<String, String> prepareBodyData() {
		HashMap<String, String> map = new HashMap<String, String>();
		map = generateStandardDetails(b, map);
		map.put("[REQUIRED_ATTENDEES_GRAMMAR]", (b.getResponseList().size() > 1) ? "have" : "has" );
		return map;
	}

	@Override
	public File getFileAttachment() {
		return ICSFileManager.createICSFile(b);
	}

	@Override
	public String getFileAttachmentName() {
		return b.getTeam().getTeamName() + " - Confirmed.ics";
	}
	
}
