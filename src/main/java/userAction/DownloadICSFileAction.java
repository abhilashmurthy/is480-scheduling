/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package userAction;

import static com.opensymphony.xwork2.Action.SUCCESS;
import com.opensymphony.xwork2.ActionSupport;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.HashMap;
import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import model.SystemActivityLog;
import model.User;
import org.apache.struts2.interceptor.ServletRequestAware;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.MiscUtil;

/**
 *
 * @author suresh
 */
public class DownloadICSFileAction extends ActionSupport implements ServletRequestAware{
	
	private HttpServletRequest request;
	private static Logger logger = LoggerFactory.getLogger(DownloadICSFileAction.class);
	private HashMap<String, Object> json = new HashMap<String, Object>();

	@Override
	public String execute() throws Exception {
		HttpSession session = request.getSession();
		User user = (User)session.getAttribute("user");
		Calendar nowCal = Calendar.getInstance();
		Timestamp now = new Timestamp(nowCal.getTimeInMillis());
		
		SystemActivityLog logItem = new SystemActivityLog();
		logItem.setActivity("User: Download ICS File");
		logItem.setRunTime(now);
		logItem.setUser(user);
		logItem.setMessage("Error with download ICS file");
		logItem.setSuccess(true);
		
		EntityManager em = null;
        try {
            
			logItem.setMessage("ICS file downloaded by " + user.toString());
        } catch (Exception e) {
			logItem.setSuccess(false);
			User userForLog = (User) session.getAttribute("user");
			logItem.setUser(userForLog);
			logItem.setMessage("Error: " + e.getMessage());
			
            logger.error("Exception caught: " + e.getMessage());
            if (MiscUtil.DEV_MODE) {
                for (StackTraceElement s : e.getStackTrace()) {
                    logger.debug(s.toString());
                }
            }
            json.put("success", false);
            json.put("message", "Error with CreateSchedule: Escalate to developers!");
        } finally {
			if (em != null) {
				if (em.getTransaction().isActive()) em.getTransaction().rollback();
				//Saving job log in database
				if (!em.getTransaction().isActive()) em.getTransaction().begin();
				em.persist(logItem);
				em.getTransaction().commit();
				if (em.isOpen()) em.close();
			}
		}
		return SUCCESS;
	}

	public void setServletRequest(HttpServletRequest hsr) {
		request = hsr;
	}
	
}
