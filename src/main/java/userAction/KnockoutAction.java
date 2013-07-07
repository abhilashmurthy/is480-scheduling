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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.xml.bind.DatatypeConverter;
import manager.UserManager;
import model.User;
import org.apache.struts2.ServletActionContext;
import org.apache.struts2.interceptor.ParameterAware;
import org.apache.struts2.interceptor.ServletRequestAware;
import org.apache.struts2.interceptor.ServletResponseAware;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Prakhar
 */
public class KnockoutAction extends ActionSupport {

    //Request and Response
    private HttpServletRequest request;
    private HttpServletResponse response;
    private List data;

    public List getResult() {
        return data;
    }

    public void setResult(List result) {
        this.data = result;
    }
    static final Logger logger = LoggerFactory.getLogger(KnockoutAction.class);

    @Override
    public String execute() throws Exception {
        
        logger.info("Reached KnockoutAction");
        
        request = ServletActionContext.getRequest();
        response = ServletActionContext.getResponse();
        
        logger.info("Reached KnockoutAction");
        Map parameters = request.getParameterMap();
        for (Object key : parameters.keySet()) {
            logger.info("Got key: " + (String) key + ", value: " + parameters.get(key));
        }

        String action = (parameters.get("action") == null) ? null : ((String[]) parameters.get("action"))[0];
        String name = (parameters.get("name") == null) ? null : ((String[]) parameters.get("name"))[0];
        String username = (parameters.get("username") == null) ? null : ((String[]) parameters.get("username"))[0];

        if (action != null) {
            switch (action.charAt(0)) {
                case 'i':
                    //insert
                    User user = new User();
                    user.setFullName(name);
                    user.setUsername(username);
                    UserManager.save(user);
                    break;
                default:
            }
        } else {
            //Fetch
            this.data = new ArrayList();
            List<User> users = UserManager.getAllUsers();
            for (User user : users) {
                HashMap record = new HashMap();
                record.put("name", user.getFullName());
                record.put("username", user.getUsername());
                record.put("id", user.getId().toString());
                data.add(record);
            }
        }

        return SUCCESS;
    }

//    public HttpServletRequest getServletRequest() {
//        return request;
//    }
//
//    public HttpServletResponse getServletResponse() {
//        return response;
//    }
//
//    public void setServletRequest(HttpServletRequest request) {
//        this.request = request;
//    }
//
//    public void setServletResponse(HttpServletResponse response) {
//        this.response = response;
//    }
}
