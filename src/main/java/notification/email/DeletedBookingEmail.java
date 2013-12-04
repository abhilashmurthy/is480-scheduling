/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package notification.email;

import constant.Response;
import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;
import manager.ICSFileManager;
import manager.UserManager;
import model.Booking;
import model.User;

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
		return generateBookingSubjectTitle(b, "Booking deleted for ");
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

		//Adding the person who perform the delete action
		emails.add(deletor.getEmail());

		//Adding required attendees
		for (User u : b.getResponseList().keySet()) {
			emails.add(u.getEmail());
		}

		//Check if the booking was previously confirmed
		boolean confirmed = true;
		for (Response r : b.getResponseList().values()) {
			if (r == Response.PENDING || r == Response.REJECTED) {
				confirmed = false;
				break;
			}
		}
		//Adding others if this is a previously confirmed booking
		if (confirmed) {
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
		map.put("[DELETOR_NAME]", deletor.getFullName());
		map.put("[DELETOR_COMMENT]", b.getComment());
		
		return map;
	}

	@Override
	public File getFileAttachment() {
		boolean previouslyConfirmed = true;
		for (Entry e : b.getResponseList().entrySet()) {
			if (e.getValue() != Response.APPROVED) {
				previouslyConfirmed = false;
				 break;
			}
		}
		return ICSFileManager.createICSFile(b, previouslyConfirmed);
	}

	@Override
	public String getFileAttachmentName() {
		return b.getTeam().getTeamName() + " - Deleted.ics";
	}
	
}
