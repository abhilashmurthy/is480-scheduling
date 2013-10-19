/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package notification.email;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.EntityManager;
import manager.SettingsManager;
import model.Booking;
import model.Settings;
import model.User;
import util.MiscUtil;

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
		return b.getTimeslot().getSchedule().getMilestone().getName() + " - New Booking";
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
		EntityManager em = null;
		try {
			em = MiscUtil.getEntityManagerInstance();
			Settings notificationSettings = SettingsManager.getByName(em, "manageNotifications");
			String jsonData = notificationSettings.getValue();
			Gson gson = new Gson();
			
			JsonArray notifArray = gson.fromJson(jsonData, JsonArray.class);
			JsonObject clearBookingSetting = notifArray.get(2).getAsJsonObject();
			String durationStr = clearBookingSetting.get("emailClearFrequency").getAsString();
			int duration = Integer.parseInt(durationStr);
			
			Calendar deadline = Calendar.getInstance();
			deadline.setTimeInMillis(b.getCreatedAt().getTime());
			deadline.add(Calendar.DAY_OF_MONTH, duration);
			deadline.setTimeInMillis(b.getCreatedAt().getTime());
			
			SimpleDateFormat sdf = new SimpleDateFormat("EEE, d MMM yyyy HH:mm");
			map.put("[DUE_DATE]", sdf.format(deadline.getTime()));
		} finally {
            if (em != null && em.getTransaction().isActive()) { em.getTransaction().rollback(); }
            if (em != null && em.isOpen()) { em.close(); }
        }
		
		return map;
	}

	@Override
	public Set<String> generateCCAddressList() {
		return null;
	}
	
}
