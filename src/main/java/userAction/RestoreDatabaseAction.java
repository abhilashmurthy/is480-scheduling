/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package userAction;

import static com.opensymphony.xwork2.Action.SUCCESS;
import com.opensymphony.xwork2.ActionSupport;
import com.sun.tools.internal.jxc.gen.config.Config;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.HashMap;
import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;
import model.SystemActivityLog;
import org.apache.struts2.interceptor.ServletRequestAware;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.MiscUtil;

/**
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

public class RestoreDatabaseAction extends ActionSupport implements ServletRequestAware {
	private HttpServletRequest request;
	static final Logger logger = LoggerFactory.getLogger(RestoreDatabaseAction.class);
	private HashMap<String, Object> json = new HashMap<String, Object>();
	private String user = "root";
	private String dbName = "is480-scheduling";
	private String restorePath = MiscUtil.getProperty("General", "BACKUP_DIR");
	private String mySQLDir = MiscUtil.getProperty("General", "MYSQL_DIR");
	private String restoreFileName;
	private String jsonData;

	@Override
	public String execute() throws Exception {
		EntityManager em = null;
		Process p = null;
		
		try {						
			JSONObject inputData = new JSONObject(jsonData);
			String restoreType = inputData.getString("restoreType");
			if (restoreType.equals("ddl")) {
				//Drop and recreate database
				logger.debug("DB Creation started");
				resetDB();
				logger.debug("DB Creation complete");
			}
			//Get file name and folder data
			restoreFileName = inputData.getString("fileName");
			File file = new File(restorePath + restoreFileName);
			if (!file.exists()) {
				json.put("success", false);
				json.put("message", "File " + restoreFileName + " does not exist!");
				return SUCCESS;
			}
			logger.debug("Got file: " + restoreFileName);

			//Execute restore
			String[] executeCmd = new String[]{
				mySQLDir + "mysql",
				"--user=" + user,
				dbName,
				"-e",
				"source " + restorePath + restoreFileName
			};
			Runtime runtime = Runtime.getRuntime();
			p = runtime.exec(executeCmd);
			StreamPuker errorPuker = new StreamPuker(p.getErrorStream(), "ERROR"); //Print errors
			StreamPuker outputPuker = new StreamPuker(p.getInputStream(), "OUTPUT"); //Print output
			errorPuker.start();
			outputPuker.start();
			logger.debug("Execute SQL restore command");
			if (p.waitFor() == 0) {
				json.put("success", true);
				json.put("message", "Database Restore Complete");
			} else {
				json.put("success", false);
				json.put("message", "Error with Database Restore");
			}
        } catch (Exception e) {
            logger.error("Exception caught: " + e.getMessage());
            if (MiscUtil.DEV_MODE) {
                for (StackTraceElement s : e.getStackTrace()) {
                    logger.debug(s.toString());
                }
            }
            json.put("exception", true);
            json.put("message", "Error with RestoreDatabaseAction: Escalate to developers!");
        }
		return SUCCESS;
	}

	private void resetDB() throws Exception {
		String url = "jdbc:mysql://localhost:3306/";
		String password = null;
		Connection conn = null;
		Statement stmt = null;
		try {
			Class.forName("com.mysql.jdbc.Driver");
			logger.debug("Connecting to phpmyadmin..");
			conn = DriverManager.getConnection(url, user, password);
			stmt = conn.createStatement();
			stmt.executeUpdate("DROP DATABASE `" + dbName + "`");
			stmt.executeUpdate("CREATE DATABASE `" + dbName + "`");
		} catch (Exception e) {
			logger.error("Exception caught: " + e.getMessage());
			if (MiscUtil.DEV_MODE) {
				for (StackTraceElement s : e.getStackTrace()) {
					logger.debug(s.toString());
				}
			}
		} finally {
			if (stmt != null) {
				stmt.close();
				conn.close();
			}
		}
	}

	public void setServletRequest(HttpServletRequest hsr) {
		this.request = hsr;
	}

	public HashMap<String, Object> getJson() {
		return json;
	}

	public void setJson(HashMap<String, Object> json) {
		this.json = json;
	}
	
	public String getJsonData() {
		return jsonData;
	}

	public void setJsonData(String jsonData) {
		this.jsonData = jsonData;
	}
	
}
