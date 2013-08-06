/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package notification.email;

import java.io.IOException;
import java.io.InputStream;
import java.util.Set;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.MiscUtil;

/**
 * Abstract class for sending standardized emails.
 * @author suresh
 */
public abstract class EmailTemplate {
	private static final String BASE_PATH;
	public String emailBody;
	private static Logger logger = LoggerFactory.getLogger(EmailTemplate.class);
	
	static {
		BASE_PATH = MiscUtil.getProperty("General", "EMAIL_TEMPLATE_PATH");
	}
	
	public EmailTemplate(String fileName) {
		try {
			InputStream in = getClass().getClassLoader().getResourceAsStream(BASE_PATH + fileName);
			emailBody = IOUtils.toString(in);
		} catch (IOException ex) {
			logger.error("Could not read email template");
			logger.error(ex.getMessage());
		}
	}
	
	public abstract String generateEmailSubject();
	
	public abstract Set<String> generateRecipientList();
	
	public abstract String generateEmailBody();
	
	public abstract void sendEmail();
	
}
