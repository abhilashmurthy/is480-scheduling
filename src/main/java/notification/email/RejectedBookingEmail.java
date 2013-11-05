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
		return b.getTeam().getTeamName() + " - Booking Rejected";
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
		HashSet<String> emails = new HashSet<String>();
		
		//Adding required attendees
		for (User u : b.getResponseList().keySet()) {
			emails.add(u.getUsername() + "@smu.edu.sg");
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
		map.put("[REJECT_REASON]", b.getRejectReason());
		
		return map;
	}
	
	@Override
	public File getFileAttachment() {
		return ICSFileManager.createICSFile(b);
	}

	@Override
	public String getFileAttachmentName() {
		return b.getTeam().getTeamName() + ".ics";
	}
	
}
