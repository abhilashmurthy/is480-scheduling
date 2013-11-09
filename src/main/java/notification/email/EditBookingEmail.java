/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package notification.email;

import constant.Response;
import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.EntityManager;
import manager.ICSFileManager;
import manager.UserManager;
import model.Booking;
import model.User;
import util.MiscUtil;

/**
 *
 * @author suresh
 */
public class EditBookingEmail extends EmailTemplate {
	
	Booking b;
	User editor;
	
	public EditBookingEmail(Booking b, User editor) {
		super("edited_booking.html");
		this.b = b;
		this.editor = editor;
	}

	@Override
	public String generateEmailSubject() {
		return generateBookingSubjectTitle(b, "Booking updated for ");
	}

	@Override
	public Set<String> generateToAddressList() {
		Set<String> emails = new HashSet<String>();
		EntityManager em = null;
		try {
			em = MiscUtil.getEntityManagerInstance();
		
			//Adding the team members
			for (User u : b.getTeam().getMembers()) {
				emails.add(u.getUsername() + "@smu.edu.sg");
			}

			//Adding required attendees
			for (User u : b.getResponseList().keySet()) {
				emails.add(u.getUsername() + "@smu.edu.sg");
			}
			//Check if the booking was previously confirmed
			boolean confirmed = true;
			for (Response r : b.getResponseList().values()) {
				if (r == Response.PENDING || r == Response.REJECTED) {
					confirmed = false;
					break;
				}
			}
			//Adding the course coordinator and optional attendees if this is a previously confirmed booking
			if (confirmed) {
				//Adding the course coordinator
				emails.add(UserManager.getCourseCoordinator(em).getUsername() + "@smu.edu.sg");

				//Adding the optional attendees
				for (String s : b.getOptionalAttendees()) {
					emails.add(s);
				}
			}
		} finally {
			if (em != null && em.isOpen()) em.close();
		}
		return emails;
	}

	@Override
	public Set<String> generateCCAddressList() {
		HashSet<String> emails = new HashSet<String>();
		//Adding the person who updated the details
		emails.add(editor.getUsername() + "@smu.edu.sg");
		
		return emails;
	}

	@Override
	public HashMap<String, String> prepareBodyData() {
		HashMap<String, String> map = new HashMap<String, String>();
		
		map = generateStandardDetails(b, map);
		
		//Inserting editor name
		map.put("[EDITOR_NAME]", editor.getFullName());
		
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
