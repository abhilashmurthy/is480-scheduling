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
public class NewBookingEmail extends EmailTemplate{
	
	private Booking b;
	private User creator;
	
	public NewBookingEmail(Booking b, User creator) {
		super("new_booking.html");
		this.b = b;
		this.creator = creator;
	}

	@Override
	public String generateEmailSubject() {
		return generateBookingSubjectTitle(b, "New booking for ");
	}

	@Override
	public Set<String> generateToAddressList() {
		Set<String> emails = new HashSet<String>();
		for (User u : b.getTeam().getMembers()) {
			emails.add(u.getEmail());
		}
		
		//Removing the person who created the booking from the TO address list
		emails.remove(creator.getEmail());
		
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
		Set<String> emails = new HashSet<String>();
		emails.add(creator.getEmail());
		return emails;
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
