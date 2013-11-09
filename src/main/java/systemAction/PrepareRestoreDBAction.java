/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package systemAction;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.opensymphony.xwork2.ActionSupport;
import constant.Role;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import javax.persistence.EntityManager;
import javax.persistence.Persistence;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import manager.ScheduleManager;
import manager.SettingsManager;
import manager.TermManager;
import manager.UserManager;
import model.Schedule;
import model.Settings;
import model.Team;
import model.Term;
import model.Timeslot;
import model.User;
import model.role.Faculty;
import model.role.Student;
import model.role.TA;
import org.apache.struts2.interceptor.ServletRequestAware;
import org.hibernate.Hibernate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.MiscUtil;

/**
 *
 * @author Abhilash
 */
public class PrepareRestoreDBAction extends ActionSupport implements ServletRequestAware {
	
	private ArrayList<HashMap<String, Object>> fileData = new ArrayList<HashMap<String, Object>>();
	private String fileJson;
	private String backupPath;	
    private HttpServletRequest request;
    private static Logger logger = LoggerFactory.getLogger(PrepareRestoreDBAction.class);
	
	@Override
    public String execute() {
		EntityManager em = null;
		Gson gson = new GsonBuilder().disableHtmlEscaping().create();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		
        try {
            em = MiscUtil.getEntityManagerInstance();
			HttpSession session = request.getSession();
			
			User user = (User) session.getAttribute("user");
			if (user.getRole().equals(Role.ADMINISTRATOR) || user.getRole().equals(Role.COURSE_COORDINATOR)) {
				em.getTransaction().begin();
				
				backupPath = MiscUtil.getProperty("General", "BACKUP_DIR");
				File backupDirectory = new File(backupPath);
				File[] backupFiles = backupDirectory.listFiles();
				for (File file : backupFiles) {
					HashMap<String, Object> fileMap = new HashMap<String, Object>();
					fileMap.put("fileName", file.getName());
					fileMap.put("datetime", sdf.format(file.lastModified()));
					fileData.add(fileMap);
				}
				
				fileJson = new Gson().toJson(fileData);
				
				em.getTransaction().commit();
			}
		} catch (Exception e) {
			logger.error("Exception caught: " + e.getMessage());
            if (MiscUtil.DEV_MODE) {
                for (StackTraceElement s : e.getStackTrace()) {
                    logger.debug(s.toString());
                }
            }
        } finally {
			if (em != null && em.getTransaction().isActive()) em.getTransaction().rollback();
			if (em != null && em.isOpen()) em.close();
		}
        return SUCCESS;
	 }
	 
	public HttpServletRequest getRequest() {
        return request;
    }

    public void setRequest(HttpServletRequest request) {
        this.request = request;
    }
	
	public void setServletRequest(HttpServletRequest hsr) {
        this.request = hsr;
    }
	
	public ArrayList<HashMap<String, Object>> getFileData() {
		return fileData;
	}

	public void setFileData(ArrayList<HashMap<String, Object>> fileData) {
		this.fileData = fileData;
	}

	public String getFileJson() {
		return fileJson;
	}

	public void setFileJson(String fileJson) {
		this.fileJson = fileJson;
	}
	
	public String getBackupPath() {
		return backupPath;
	}

	public void setBackupPath(String backupPath) {
		this.backupPath = backupPath;
	}
	
}
