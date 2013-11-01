/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package userAction;

import static com.opensymphony.xwork2.Action.SUCCESS;
import com.opensymphony.xwork2.ActionSupport;
import java.sql.Timestamp;
import java.util.Calendar;
import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import model.SystemActivityLog;
import model.User;
import org.apache.struts2.interceptor.ServletRequestAware;
import org.apache.struts2.interceptor.ServletResponseAware;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.MiscUtil;

/**
 *
 * @author Prakhar
 */
public class LogoutAction extends ActionSupport implements ServletRequestAware, ServletResponseAware {

    //Request and Response
    private HttpServletRequest request;
    private static Logger logger = LoggerFactory.getLogger(LogoutAction.class);
    private HttpServletResponse response;
    
    @Override
    public String execute() throws Exception {
		HttpSession session = request.getSession();
		
		Calendar nowCal = Calendar.getInstance();
		Timestamp now = new Timestamp(nowCal.getTimeInMillis());
		
		SystemActivityLog logItem = new SystemActivityLog();
		logItem.setActivity("Logout");
		logItem.setRunTime(now);
		logItem.setUser((User)session.getAttribute("user"));
		
		EntityManager em = null;
        try {
			em = MiscUtil.getEntityManagerInstance();
			User user = (User) session.getAttribute("user");
			session.invalidate();
			
			MiscUtil.logActivity(logger, user, "Logged out");
			
			logItem.setMessage("Logout successful. " + user.toString());
			
        } catch (Exception e) {
			User userForLog = (User) session.getAttribute("user");
			logItem.setUser(userForLog);
			logItem.setMessage("Error: " + e.getMessage());
			
            logger.error("Exception caught: " + e.getMessage());
            if (MiscUtil.DEV_MODE) {
                for (StackTraceElement s : e.getStackTrace()) {
                    logger.debug(s.toString());
                }
            }
            request.setAttribute("error", "Error with Logout: Escalate to developers!");
            return ERROR;
        } finally {
			 if (em != null) {
				//Saving job log in database
				if (!em.getTransaction().isActive()) em.getTransaction().begin();
				em.persist(logItem);
				em.getTransaction().commit();
				
				if (em.getTransaction().isActive()) em.getTransaction().rollback();
				if (em.isOpen()) em.close();
			}
		}
		
        return SUCCESS;
    }
    
    public HttpServletRequest getServletRequest() {
        return request;
    }
    
    public HttpServletResponse getServletResponse() {
        return response;
    }
    
    public void setServletRequest(HttpServletRequest request) {
        this.request = request;
    }

    public void setServletResponse(HttpServletResponse response) {
        this.response = response;
    }
}
