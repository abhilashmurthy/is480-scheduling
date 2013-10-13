/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package util;

import constant.Role;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class to put miscellaneous code
 * @author suresh
 */
public class MiscUtil {
	private static Logger logger = LoggerFactory.getLogger(MiscUtil.class);
	
	/**
	 * EntityManagerFactory to be used to obtain EntityManager instances application-wide
	 */
	public static final EntityManagerFactory emf = Persistence.createEntityManagerFactory("scheduler");
	
	/**
	 * 
	 */
	public static final String SMS_REMINDER_JOBS = "SMS Reminders";
	
	/**
	 * Boolean variable to check if the system is currently running in development mode
	 */
	public static final boolean DEV_MODE = Boolean.parseBoolean(getProperty("General", "DEV_MODE"));
	
	public static String getProperty(String fileName, String propertyName) {
		try {
			Properties p = new Properties();	
			InputStream in = MiscUtil.class.getClassLoader().getResourceAsStream("Properties/" + fileName + ".properties");
			p.load(in);
			return p.getProperty(propertyName);
		} catch (IOException ex) {
			logger.error("Error reading properties file");
			logger.error(ex.getMessage());
		}
		
		return null;
	}
	
	public static EntityManager getEntityManagerInstance() {
		return emf.createEntityManager();
	}
	
	/**
	 * Overloaded method using a user object instead
	 * @param logObj
	 * @param user
	 * @param message 
	 */
	public static void logActivity(Logger logObj, User user, String message) {
		logActivity(logObj, user.getUsername(), user.getRole(), message);
	}
	
	/**
	 * Method to log user activity in the system. Role information is optional
	 * @param logObj Logger object to be used for logging. (Retains class location)
	 * @param username Username of the user performing the action
	 * @param role (Optional) Role of the user at that point of time
	 * @param message Message describing the action performed
	 */
	public static void logActivity(Logger logObj, String username, Role role, String message) {
		StringBuilder logMsg = new StringBuilder();
		logMsg.append(username);
		if (role != null) logMsg.append("[").append(role).append("]");
		logMsg.append(": ").append(message);
		logObj.info(logMsg.toString());
	}
}
