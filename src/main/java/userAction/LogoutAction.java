/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package userAction;

import static com.opensymphony.xwork2.Action.SUCCESS;
import com.opensymphony.xwork2.ActionSupport;
import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.apache.struts2.interceptor.ServletRequestAware;
import org.apache.struts2.interceptor.ServletResponseAware;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Prakhar
 */
public class LogoutAction extends ActionSupport implements ServletRequestAware, ServletResponseAware {

    //Request and Response
    private HttpServletRequest request;
    private static Logger logger = LoggerFactory.getLogger(LogoutAction.class);
    private final boolean debugMode = true;
    private HttpServletResponse response;
    
    @Override
    public String execute() throws Exception {
        try {
        logger.info("Reached LogoutAction");
        HttpSession session = request.getSession();
        session.removeAttribute("user");
        logger.info("Logout successful");
        } catch (Exception e) {
            logger.error("Exception caught: " + e.getMessage());
            if (debugMode) {
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
