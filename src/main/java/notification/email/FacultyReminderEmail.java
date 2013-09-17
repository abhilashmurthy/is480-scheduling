/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package notification.email;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Calendar;
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
	Calendar dueDate;
	
	public FacultyReminderEmail(Booking b, Faculty person, Calendar dueDate) {
		super("faculty_reminder.html");
		this.b = b;
		this.person = person;
		this.dueDate = dueDate;
	}

	@Override
	public String generateEmailSubject() {
		return "Reminder: Booking Approval";
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
		SimpleDateFormat sdf = new SimpleDateFormat("EEE, d MMM yyyy HH:mm");
		
		//Deducting a minute from the date. This is to show time as 23:59 on the previous day for convenience
		dueDate.add(Calendar.MINUTE, -1);
		
		//Inserting the due date for response
		map.put("[DUE_DATE]", sdf.format(dueDate.getTime()));
		
		return map;
	}
	
}
