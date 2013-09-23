/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Timestamp;
import java.util.Calendar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Contains method to initialize data in a new database. WARNING! Please run
 * this file only on a blank database!
 *
 * @author ABHILASHM.2010
 */
class StreamPuker extends Thread {
	InputStream is;
	String type;
	static Logger logger = LoggerFactory.getLogger(StreamPuker.class);

	StreamPuker(InputStream is, String type) {
		this.is = is;
		this.type = type;
	}

	public void run() {
		try {
			InputStreamReader isr = new InputStreamReader(is);
			BufferedReader br = new BufferedReader(isr);
			String line = null;
			while ((line = br.readLine()) != null) {
				logger.debug(type + " > " + line);
			}
		} catch (IOException e) {
			StackTraceElement[] stackTrace = e.getStackTrace();
			for (StackTraceElement s : stackTrace) {
				logger.error(s.toString());
			}
		}
	}
}

public class TestRestoreDB {

	static Logger logger = LoggerFactory.getLogger(TestRestoreDB.class);
	private static String user = "root";
	private static String dbName = "is480-scheduling";
	private static String restorePath = MiscUtil.getProperty("General", "BACKUP_DIR");
	private static String mySQLDir = MiscUtil.getProperty("General", "MYSQL_DIR");

	/**
	 * Method to initialize data in a new database. WARNING! Please run this
	 * file only on a blank database!
	 */
	public static void main(String[] args) {
		Process p = null;
		try {
			Calendar cal = Calendar.getInstance();
			Timestamp now = new Timestamp(cal.getTimeInMillis());
			
			String[] executeCmd = new String[]{mySQLDir + "mysql", "--user=" + user, dbName,"-e", "source "+restorePath+"is480Scheduling_20130923_215045.sql"};

			Runtime runtime = Runtime.getRuntime();
			p = runtime.exec(executeCmd);

			//If there's any error
			StreamPuker errorPuker = new StreamPuker(p.getErrorStream(), "ERROR");

			//The output that is printed
			StreamPuker outputPuker = new StreamPuker(p.getInputStream(), "OUTPUT");

			errorPuker.start();
			outputPuker.start();
			
			errorPuker.join();
			outputPuker.join();

			logger.debug("WaitFor value: " + p.waitFor());
		} catch (Exception e) {
			logger.error("Exception caught: " + e.getMessage());
			if (MiscUtil.DEV_MODE) {
				for (StackTraceElement s : e.getStackTrace()) {
					logger.debug(s.toString());
				}
			}
		}
	}
}