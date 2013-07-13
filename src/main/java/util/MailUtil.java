/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author suresh
 */
public class MailUtil {

	public static final Logger logger = LoggerFactory.getLogger(MailUtil.class);
	private static Properties props;
	private static Session session;
	private static final String USERNAME = "is480.scheduling@gmail.com";
	private static final String PASSWORD = "fyp2013-14";

	static {
		props = new Properties();
		props.put("mail.smtp.host", "smtp.gmail.com");
		props.put("mail.smtp.socketFactory.port", "465");
		props.put("mail.smtp.socketFactory.class",
				"javax.net.ssl.SSLSocketFactory");
		props.put("mail.smtp.auth", "true");
		props.put("mail.smtp.port", "465");
		session = Session.getInstance(props,
				new javax.mail.Authenticator() {
			@Override
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(USERNAME, PASSWORD);
			}
		});
	}

	public static void sendEmail(String[] recipients, String subject, String body) {

		try {
			Message message = new MimeMessage(session);
			message.setFrom(new InternetAddress("is480.scheduling@gmail.com", "IS480 Scheduling"));
			//TODO Use variables
			message.setRecipients(Message.RecipientType.TO,
					InternetAddress.parse("sureshs.592@gmail.com"));
			message.setSubject("Testing Subject");
			message.setText("Yo,"
					+ "\n\n Got your email bruh?");

			Transport.send(message);
			logger.info("Email sent successfully");
		} catch (Exception e) {
			logger.error("Send Mail Error");
			logger.error(e.getMessage());
		}
	}
}
