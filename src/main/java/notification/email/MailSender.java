/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package notification.email;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.MiscUtil;

/**
 *
 * @author suresh
 */
public class MailSender {

	public static Logger logger = LoggerFactory.getLogger(MailSender.class);
	private static Properties props = new Properties();
	private static Session session;
	private static String USERNAME;
	private static String PASSWORD;

	static {
		try {
			InputStream in = MailSender.class.getClassLoader()
					.getResourceAsStream("Properties/Mail.properties");
			props.load(in);
			USERNAME = MiscUtil.getProperty("General", "USERNAME");
			PASSWORD = MiscUtil.getProperty("General", "PASSWORD");
			session = Session.getInstance(props,
					new javax.mail.Authenticator() {
				@Override
				protected PasswordAuthentication getPasswordAuthentication() {
					return new PasswordAuthentication(USERNAME, PASSWORD);
				}
			});
		} catch (IOException ex) {
			logger.error("Mail Properties could not be loaded");
			logger.error(ex.getMessage());
		}
	}

	public synchronized static void sendEmail(Set<String> toEmails, Set<String> ccEmails, String subject, String body) {

		try {
			Message message = new MimeMessage(session);
			message.setFrom(new InternetAddress("is480.scheduling@gmail.com",
					"IS480 Scheduling"));
			
			//Setting TO emails
			if (MiscUtil.DEV_MODE) {
				message.setRecipients(Message.RecipientType.TO,
						InternetAddress.parse(
						MiscUtil.getProperty("General", "TEST_EMAIL_ID")));
//				body += "Sent from: " + InetAddress.getLocalHost().getHostName();
			} else {
				message.setRecipients(Message.RecipientType.TO,
					InternetAddress.parse(parseRecipientArray(toEmails)));	
			}
			
			//Setting CC emails
			if (ccEmails != null) {
				if (MiscUtil.DEV_MODE) {
					message.setRecipients(Message.RecipientType.CC,
							InternetAddress.parse(
							MiscUtil.getProperty("General", "TEST_EMAIL_ID")));
				} else {
					message.setRecipients(Message.RecipientType.CC,
						InternetAddress.parse(parseRecipientArray(ccEmails)));	
				}
			}
			
			message.setSubject(subject);
			message.setContent(body, "text/html");

			Transport.send(message);
			logger.info("Email sent successfully");
		} catch (Exception e) {
			logger.error("Send Mail Error");
			logger.error(e.getMessage());
		}
	}
	
	/**
	 * Generate a comma separated list of email addresses.
	 * @param recipients List of recipients
	 * @return Single String with all email addresses
	 */
	public static String parseRecipientArray(Set<String> recipients) {
		StringBuilder result = new StringBuilder();
		Iterator<String> iter = recipients.iterator();
		
		while (iter.hasNext()) {
			result.append(iter.next());
			if (iter.hasNext()) {
				result.append(",");
			}
		}
		
		return result.toString();
	}
}
