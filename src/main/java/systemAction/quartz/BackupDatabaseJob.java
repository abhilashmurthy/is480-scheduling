/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package systemAction.quartz;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import javax.persistence.EntityManager;
import model.SystemActivityLog;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.TestBackupDB;
import util.MiscUtil;

/**
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


public class BackupDatabaseJob implements Job {
	static Logger logger = LoggerFactory.getLogger(TestBackupDB.class);
	private static String host = "localhost";
	private static String port = "3306";
	private static String user = "root";
	private static String password = "";
	private static String dbName = "is480-scheduling";
	private static String mySQLDir = MiscUtil.getProperty("General", "MYSQL_DIR");

	public void execute(JobExecutionContext jec) throws JobExecutionException {
		logger.trace("Beginning Database Backup");
		SystemActivityLog logItem = new SystemActivityLog();
        logItem.setActivity("Database Backup");
        Calendar cal = Calendar.getInstance();
        Timestamp now = new Timestamp(cal.getTimeInMillis());
        logItem.setRunTime(now);
		logItem.setMessage("Database backup started " + now);
		
		EntityManager em = null;
		Process process = null;
		try {
			em = MiscUtil.getEntityManagerInstance();
			em.getTransaction().begin();
			StringBuilder command = new StringBuilder()
					.append(mySQLDir).append("mysqldump")
					.append(" --host=").append(host)
					.append(" --port=").append(port)
					.append(" --user=").append(user)
					.append(" ").append(dbName)
					.append(" --hex-blob");

			Runtime runtime = Runtime.getRuntime();
			process = runtime.exec(command.toString());
			
			StreamGobbler errorGobbler = new StreamGobbler(process.getErrorStream(), "ERROR"); //If there's any error
			StreamGobbler outputGobbler = new StreamGobbler(process.getInputStream(), "OUTPUT"); //The output that is printed
			errorGobbler.start();
			outputGobbler.start();
			int waitForValue = process.waitFor();
			if (waitForValue == 0) {
				logItem.setSuccess(true);
				logItem.setMessage("Database backup complete: " + now);
			} else {
				logItem.setSuccess(false);
				logItem.setMessage("Database backup failure: " + now);
			}
			logger.trace("Completed Database Backup");
			em.getTransaction().commit();
		} catch (Exception e) {
            logItem.setSuccess(false);
            logItem.setMessage("Error: " + e.getMessage());
            logger.error("Exception caught: " + e.getMessage());
            if (MiscUtil.DEV_MODE) {
                for (StackTraceElement s : e.getStackTrace()) {
                    logger.debug(s.toString());
                }
            }
		} finally {
            if (em != null) {
                //Saving job log in database
                if (!em.getTransaction().isActive()) {
                    em.getTransaction().begin();
                }
                em.persist(logItem);
                em.getTransaction().commit();

                if (em.getTransaction().isActive()) {
                    em.getTransaction().rollback();
                }
                if (em.isOpen()) {
                    em.close();
                }
            }
		}
	}
}