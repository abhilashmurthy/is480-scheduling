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
    private HttpServletResponse response;
    
 
    static final Logger logger = LoggerFactory.getLogger(CreateBookingAction.class);
    
    @Override
    public String execute() throws Exception {
        logger.info("Reached LogoutAction");
        HttpSession session = request.getSession();
        session.removeAttribute("user");
        logger.info("Logout successful");
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
