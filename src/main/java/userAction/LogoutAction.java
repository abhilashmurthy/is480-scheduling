/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package userAction;

import static com.opensymphony.xwork2.Action.SUCCESS;
import com.opensymphony.xwork2.ActionSupport;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
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
        try {
			HttpSession session = request.getSession();
			User user = (User) session.getAttribute("user");
			session.invalidate();
			MiscUtil.logActivity(logger, user, "Logged out");
        } catch (Exception e) {
            logger.error("Exception caught: " + e.getMessage());
            if (MiscUtil.DEV_MODE) {
                for (StackTraceElement s : e.getStackTrace()) {
                    logger.debug(s.toString());
                }
            }
            request.setAttribute("error", "Error with Logout: Escalate to developers!");
            return ERROR;
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
