/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
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
}
