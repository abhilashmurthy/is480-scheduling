/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package notification.email;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
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
	private static boolean TRUE_RECIPIENTS;

	static {
		InputStream in = null;
		try {
			in = MailSender.class.getClassLoader()
					.getResourceAsStream("Properties/Mail.properties");
			props.load(in);
			USERNAME = MiscUtil.getProperty("General", "USERNAME");
			PASSWORD = MiscUtil.getProperty("General", "PASSWORD");
			TRUE_RECIPIENTS = Boolean.parseBoolean(MiscUtil.getProperty("General", "TRUE_RECIPIENTS"));
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

	public synchronized static void sendEmail
			(Set<String> toEmails, Set<String> ccEmails,
			String subject, String body,
			File attachFile, String filename) {

		try {
			MimeMessage message = new MimeMessage(session);
			message.setSubject(subject);
			message.setFrom(new InternetAddress("is480.scheduling@gmail.com",
					"IS480 Scheduling"));
			
			//Setting TO emails
			if (!TRUE_RECIPIENTS) {
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
				if (!TRUE_RECIPIENTS) {
					message.setRecipients(Message.RecipientType.CC,
							InternetAddress.parse(
							MiscUtil.getProperty("General", "TEST_EMAIL_ID")));
				} else {
					message.setRecipients(Message.RecipientType.CC,
						InternetAddress.parse(parseRecipientArray(ccEmails)));	
				}
			}
			
			//Adding the message body (HTML text)
			MimeMultipart multipart = new MimeMultipart();
			MimeBodyPart msgBody = new MimeBodyPart();
			msgBody.setContent(body, "text/html");
			multipart.addBodyPart(msgBody);
			
			//Adding the file attachment (if any)
			if (attachFile != null) {
				MimeBodyPart attachment = new MimeBodyPart();
				DataSource file = new FileDataSource(attachFile);
				attachment.setDataHandler(new DataHandler(file));
				attachment.setFileName(filename);
				multipart.addBodyPart(attachment);
			}
			
			message.setContent(multipart);
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
