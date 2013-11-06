/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package notification.email;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import model.Booking;
import model.role.Faculty;

/**
 *
 * @author suresh
 */
public class FacultyReminderEmail extends EmailTemplate{
	
	Booking b;
	Faculty person;
	
	public FacultyReminderEmail(Booking b, Faculty person) {
		super("faculty_reminder.html");
		this.b = b;
		this.person = person;
	}

	@Override
	public String generateEmailSubject() {
		return "[Reminder] " + b.getTeam().getTeamName();
	}

	@Override
	public Set<String> generateToAddressList() {
		HashSet<String> emails = new HashSet<String>();
		emails.add(person.getUsername() + "@smu.edu.sg");
		return emails;
	}

	@Override
	public Set<String> generateCCAddressList() {
		return null;
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
	public File getFileAttachment() {
		return null;
	}

	@Override
	public String getFileAttachmentName() {
		return null;
	}
	
}
