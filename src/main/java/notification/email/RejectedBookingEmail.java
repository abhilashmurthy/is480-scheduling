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
public class RejectedBookingEmail extends EmailTemplate{
	Booking b;
	User rejector;
	boolean previouslyConfirmed;
	
	public RejectedBookingEmail(Booking b, User rejector, boolean previouslyConfirmed) {
		super("rejected_booking.html");
		this.b = b;
		this.rejector = rejector;
		this.previouslyConfirmed = previouslyConfirmed;
	}

	@Override
	public String generateEmailSubject() {
		return generateBookingSubjectTitle(b, "Booking rejected for ");
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
		
		//Adding others if this is a previously confirmed booking
		if (previouslyConfirmed) {
			//Adding the course coordinator
			emails.add(UserManager.getCourseCoordinator(em).getEmail());

			//Adding the optional attendees
			for (String s : b.getOptionalAttendees()) {
				emails.add(s);
			}
			
			//Adding the TA
			if (b.getTimeslot().getTA() != null) emails.add(b.getTimeslot().getTA().getEmail());
		}
		
		return emails;
	}

	@Override
	public HashMap<String, String> prepareBodyData() {
		HashMap<String, String> map = new HashMap<String, String>();
		
		map = generateStandardDetails(b, map);
		
		//Inserting approver name
		map.put("[REJECTOR_NAME]", rejector.getFullName());
		
		//Inserting the reason for rejection
		map.put("[REJECT_REASON]", b.getComment());
		
		return map;
	}
	
	@Override
	public File getFileAttachment() {
		return ICSFileManager.createICSFile(b);
	}

	@Override
	public String getFileAttachmentName() {
		return b.getTeam().getTeamName() + " - Rejected.ics";
	}
	
}
