/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package notification.email;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;
import model.Booking;
import model.User;
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
				generateEmailSubject(), generateEmailBody());
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
		
		return map;
	}
	
	public abstract String generateEmailSubject();
	
	public abstract Set<String> generateToAddressList();
	
	public abstract Set<String> generateCCAddressList();
	
	public abstract HashMap<String, String> prepareBodyData();
	
}
