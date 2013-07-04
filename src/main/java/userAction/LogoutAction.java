/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package userAction;

import static com.opensymphony.xwork2.Action.SUCCESS;
import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.ActionSupport;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Map;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.xml.bind.DatatypeConverter;
import model.User;
import model.dao.UserDAO;
import org.apache.struts2.interceptor.ParameterAware;
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
