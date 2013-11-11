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
public class RespondToBookingEmail extends EmailTemplate{
	
	private Booking b;
	
	public RespondToBookingEmail(Booking b) {
		super("respond_to_booking.html");
		this.b = b;
	}

	@Override
	public String generateEmailSubject() {
		return generateBookingSubjectTitle(b, "Approval required for ");
	}

	@Override
	public Set<String> generateToAddressList() {
		HashSet<String> emails = new HashSet<String>();
		for (User u : b.getResponseList().keySet()) {
			emails.add(u.getUsername() + "@smu.edu.sg");
		}
		
		return emails;
	}

	@Override
	public HashMap<String, String> prepareBodyData() {
		HashMap<String, String> map = new HashMap<String, String>();
		map = generateStandardDetails(b, map);
		//Inserting the due date for response
		map = generateDueDate(map, b.getCreatedAt().getTime());
		return map;
	}

	@Override
	public Set<String> generateCCAddressList() {
		return null;
	}
	
	@Override
	public File getFileAttachment() {
		return ICSFileManager.createICSFile(b);
	}

	@Override
	public String getFileAttachmentName() {
		return b.getTeam().getTeamName() + " - Tentative.ics";
	}
	
}
