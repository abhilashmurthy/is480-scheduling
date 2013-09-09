/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package notification.email;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import javax.persistence.EntityManager;
import javax.persistence.Persistence;
import manager.UserManager;
import model.Booking;
import model.User;
import util.MiscUtil;

/**
 *
 * @author suresh
 */
public class ConfirmedBookingEmail extends EmailTemplate{
	private Booking b;
	
	public ConfirmedBookingEmail(Booking b) {
		super("confirmed_booking.html");
		this.b = b;
	}

	@Override
	public String generateEmailSubject() {
		return b.getTimeslot().getSchedule().getMilestone().getName() + " - Booking Confirmed";
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
			em = Persistence.createEntityManagerFactory(MiscUtil.PERSISTENCE_UNIT).createEntityManager();
			HashSet<String> emails = new HashSet<String>();

			//Adding required attendees
			for (User u : b.getResponseList().keySet()) {
				emails.add(u.getUsername() + "@smu.edu.sg");
			}
			//Adding the course coordinator
			emails.add(UserManager.getCourseCoordinator(em).getUsername() + "@smu.edu.sg");

			//Adding the optional attendees
			for (String s : b.getOptionalAttendees()) {
				emails.add(s);
			}
			return emails;
		} finally {
			if (em != null && em.isOpen()) em.close();
		}
	}

	@Override
	public HashMap<String, String> prepareBodyData() {
		HashMap<String, String> map = new HashMap<String, String>();
		return generateStandardDetails(b, map);
	}
	
}
