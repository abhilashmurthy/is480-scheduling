/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package util;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static util.MiscUtil.getProperty;
import static util.StreamGobbler.logger;

/**
 * Contains method to initialize data in a new database. WARNING! Please run
 * this file only on a blank database!
 *
 * @author ABHILASHM.2010
 */
class StreamGobbler extends Thread {

	InputStream is;
	String type;
	static Logger logger = LoggerFactory.getLogger(StreamGobbler.class);
	private static String backupPath = MiscUtil.getProperty("General", "BACKUP_DIR");
	private static String backupFileName = "is480Scheduling";
	private static SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");

	StreamGobbler(InputStream is, String type) {
		this.is = is;
		this.type = type;
	}

	public void run() {
		Calendar cal = Calendar.getInstance();
		Timestamp now = new Timestamp(cal.getTimeInMillis());
		PrintWriter writer = null;
		try {
			
			StringBuilder backupPathString = new StringBuilder()
					.append(backupPath)
					.append(backupFileName)
					.append("_")
					.append(sdf.format(now.getTime()))
					.append(".sql");
			
			File backupFolder = new File(backupPathString.toString()).getParentFile();
			if (!backupFolder.exists()) backupFolder.mkdirs();
			
			InputStreamReader isr = new InputStreamReader(is);
			BufferedReader br = new BufferedReader(isr);
			String line = null;
			writer = new PrintWriter(backupPathString.toString(), "UTF-8");
			while ((line = br.readLine()) != null) {
				writer.println(line);
			}
		} catch (IOException e) {
			StackTraceElement[] stackTrace = e.getStackTrace();
			for (StackTraceElement s : stackTrace) {
				logger.error(s.toString());
			}
		} finally {
			if (writer != null) writer.close();
		}
	}
}

public class TestBackupDB {

	static Logger logger = LoggerFactory.getLogger(TestBackupDB.class);
	private static String host = "localhost";
	private static String port = "3306";
	private static String user = "root";
	private static String password = "";
	private static String dbName = "is480-scheduling";
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

			StringBuilder command = new StringBuilder()
					.append(mySQLDir).append("mysqldump")
					.append(" --host=").append(host)
					.append(" --port=").append(port)
					.append(" --user=").append(user)
					.append(" ").append(dbName)
					.append(" --hex-blob");

			logger.debug("Command = " + command.toString());

			Runtime runtime = Runtime.getRuntime();
			p = runtime.exec(command.toString());

			//If there's any error
			StreamGobbler errorGobbler = new StreamGobbler(p.getErrorStream(), "ERROR");

			//The output that is printed
			StreamGobbler outputGobbler = new StreamGobbler(p.getInputStream(), "OUTPUT");

			errorGobbler.start();
			outputGobbler.start();

			logger.debug("WaitFor value: " + p.waitFor());
		} catch (Exception e) {
			logger.error("Exception caught: " + e.getMessage());
			if (MiscUtil.DEV_MODE) {
				for (StackTraceElement s : e.getStackTrace()) {
					logger.debug(s.toString());
				}
			}
		} finally {
		}
	}
}