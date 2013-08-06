/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package notification.email;

import java.io.IOException;
import java.io.InputStream;
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
import util.TestUtil;

/**
 *
 * @author suresh
 */
public class MailUtil {

	public static final Logger logger = LoggerFactory.getLogger(MailUtil.class);
	private static final Properties props = new Properties();
	private static Session session;
	private static final String USERNAME = "is480.scheduling@gmail.com";
	private static final String PASSWORD = "fyp2013-14";

	static {
		try {
			InputStream in = TestUtil.class.getClassLoader().getResourceAsStream("Properties/Mail.properties");
			props.load(in);
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

	public static void sendEmail(Set<String> recipients, String subject, String body) {

		try {
			Message message = new MimeMessage(session);
			message.setFrom(new InternetAddress("is480.scheduling@gmail.com",
					"IS480 Scheduling"));
			message.setRecipients(Message.RecipientType.TO,
					InternetAddress.parse(parseRecipientArray(recipients)));
			message.setSubject(subject);
			message.setText(body);

			Transport.send(message);
			logger.info("Email sent successfully");
		} catch (Exception e) {
			logger.error("Send Mail Error");
			logger.error(e.getMessage());
		}
	}
	
	public static void sendEmail(String recipient, String subject, String body) {
		Set<String> recipientList = new HashSet<String>();
		recipientList.add(recipient);
		sendEmail(recipientList, subject, body);
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
