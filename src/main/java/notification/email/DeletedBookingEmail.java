/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package notification.email;

import constant.Response;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.EntityManager;
import manager.UserManager;
import model.Booking;
import model.User;
import util.MiscUtil;

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
		return b.getTeam().getTeamName() + " - Booking Deleted";
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
		EntityManager em = null;
		try {
			em = MiscUtil.getEntityManagerInstance();
			HashSet<String> emails = new HashSet<String>();
			
			//Adding the person who perform the delete action
			emails.add(deletor.getUsername() + "@smu.edu.sg");

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
			return emails;
		} finally {
			if (em != null && em.isOpen()) em.close();
		}
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
	
}
