/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package notification.email;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;
import javax.persistence.EntityManager;
import manager.SettingsManager;
import model.Booking;
import model.Settings;
import model.User;
import model.role.TA;
import org.jsoup.Jsoup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.MiscUtil;

/**
 * Abstract class for sending standardized emails.
 * @author suresh
 */
public abstract class EmailTemplate {
	private static final String BASE_PATH;
	public String body;
	private static Logger logger = LoggerFactory.getLogger(EmailTemplate.class);
	
	static {
		BASE_PATH = MiscUtil.getProperty("General", "EMAIL_TEMPLATE_PATH");
	}
	
	public EmailTemplate(String fileName) {
		InputStream in = null;
		try {
			in = getClass().getClassLoader().getResourceAsStream(BASE_PATH + fileName);
			body = Jsoup.parse(in, "UTF-8", "").toString();
		} catch (IOException ex) {
			logger.error("Could not read email template");
			logger.error(ex.getMessage());
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException ex) {
					logger.error("Error in closing InputStream for Mail Properties");
				}	
			}
		}
	}
	
	public void sendEmail() {
		Runnable r = new Runnable() {
			public void run() {
				MailSender.sendEmail(generateToAddressList(), generateCCAddressList(),
				generateEmailSubject(), generateEmailBody(),
				getFileAttachment(), getFileAttachmentName());
			}
		};
		Thread t = new Thread(r, "Mail Sender");
		t.start();
	}
	
	public String generateEmailBody() {
		HashMap<String, String> templateData = prepareBodyData();
		for (Entry<String, String> e : templateData.entrySet()) {
			body = body.replace(e.getKey(), e.getValue());
		}
		return body;
	}
	
	public String generateBookingSubjectTitle(Booking b, String content) {
		return "IS480: " + content + b.getTimeslot().getSchedule().getMilestone().getName() + " - " + b.getTeam().getTeamName();
	}
	
	/**
	 * Injects standard booking information. Covers the following tags:
	 * [TEAM_NAME], [MILESTONE], [DATE], [START_TIME], [END_TIME]
	 * [VENUE], [REQUIRED_ATTENDEES]
	 * @param b
	 * @param map
	 * @return 
	 */
	public HashMap<String, String> generateStandardDetails(Booking b, HashMap<String, String> map) {
		//Insert team name
		map.put("[TEAM_NAME]", b.getTeam().getTeamName());
		
		//Insert milestone name
		map.put("[MILESTONE]", b.getTimeslot().getSchedule().getMilestone().getName());
		
		//Insert start date
		SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, d MMM yyyy");
		map.put("[DATE]", dateFormat.format(b.getTimeslot().getStartTime()));
		
		//Insert start and end time
		SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm a");
		map.put("[START_TIME]", timeFormat.format(b.getTimeslot().getStartTime()));
		map.put("[END_TIME]", timeFormat.format(b.getTimeslot().getEndTime()));
		
		//Insert venue
		map.put("[VENUE]", b.getTimeslot().getVenue());
		
		//Insert required attendees
		Set<User> userList = b.getResponseList().keySet();
		Iterator<User> iter = userList.iterator();
		StringBuilder result = new StringBuilder();
		
		while (iter.hasNext()) {
			result.append(iter.next().getFullName());
			if (iter.hasNext()) {
				result.append(",&nbsp;");
			}
		}
		map.put("[REQUIRED_ATTENDEES]", result.toString());
		
		//Insert optional attendees
		HashSet<String> optionalEmails = b.getOptionalAttendees();
		StringBuilder optionalEmailsString = new StringBuilder();
		if (optionalEmails.isEmpty()) { //Insert "-" if there are no emails
			optionalEmailsString.append("-");
		} else { //Insert emails separated by commas
			Iterator<String> optionalIter = optionalEmails.iterator();
			while (optionalIter.hasNext()) {
				optionalEmailsString.append(optionalIter.next());
				if (optionalIter.hasNext()) {
					optionalEmailsString.append(",&nbsp;");
				}
			}
		}
		map.put("[OPTIONAL_ATTENDEES]", optionalEmailsString.toString());
		
		TA ta = b.getTimeslot().getTA();
		String taString = (ta != null) ? ta.getFullName() : "-" ;
		map.put("[TA]", taString);
		
		return map;
	}
	
	public HashMap<String, String> generateDueDate(HashMap<String, String> map, long createdAtMillis) {
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
			deadline.setTimeInMillis(createdAtMillis);
			deadline.add(Calendar.DAY_OF_MONTH, duration);
			
			//Showing the deadline date in a more readable manner: Date with 23:59 time
			int daysToSubtract = 0;
			if (deadline.get(Calendar.HOUR_OF_DAY) < 3) { //Booking was made between 12 and 3 AM
				daysToSubtract = -1;
			}
			deadline.add(Calendar.DAY_OF_MONTH, daysToSubtract);
			deadline.set(Calendar.HOUR_OF_DAY, 23);
			deadline.set(Calendar.MINUTE, 59);
			
			SimpleDateFormat sdf = new SimpleDateFormat("EEE, d MMM yyyy HH:mm");
			map.put("[DUE_DATE]", sdf.format(deadline.getTime()));
		} finally {
            if (em != null && em.getTransaction().isActive()) em.getTransaction().rollback();
            if (em != null && em.isOpen()) em.close();
        }
		return map;
	}
	
	public abstract String generateEmailSubject();
	
	public abstract Set<String> generateToAddressList();
	
	public abstract Set<String> generateCCAddressList();
	
	public abstract HashMap<String, String> prepareBodyData();
	
	public abstract File getFileAttachment();
	
	public abstract String getFileAttachmentName();
	
}
