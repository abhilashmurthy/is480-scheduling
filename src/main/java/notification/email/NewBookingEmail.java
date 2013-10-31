/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package notification.email;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import model.Booking;
import model.User;

/**
 *
 * @author suresh
 */
public class NewBookingEmail extends EmailTemplate{
	
	private Booking b;
	
	public NewBookingEmail(Booking b) {
		super("new_booking.html");
		this.b = b;
	}

	@Override
	public String generateEmailSubject() {
		return b.getTeam().getTeamName() + " - New Booking";
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
	
}
