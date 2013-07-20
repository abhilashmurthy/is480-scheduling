/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package userAction;

import static com.opensymphony.xwork2.Action.ERROR;
import static com.opensymphony.xwork2.Action.SUCCESS;
import com.opensymphony.xwork2.ActionSupport;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import model.User;
import org.apache.struts2.interceptor.ServletRequestAware;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static userAction.ResponseAction.logger;

/**
 *
 * @author Prakhar
 */
public class CreateScheduleAction extends ActionSupport implements ServletRequestAware {
    
    private HttpServletRequest request;
    private HashMap<String, Object> json = new HashMap<String, Object>();
    static final Logger logger = LoggerFactory.getLogger(CreateBookingAction.class);
    
    public HashMap<String, Object> getJson() {
        return json;
    }

    public void setJson(HashMap<String, Object> json) {
        this.json = json;
    }

    @Override
    public String execute() throws Exception {
        Map parameters = request.getParameterMap();
        for (Object key : parameters.keySet()) {
            logger.info("Received key: " + key + ", value: " + ((String[])parameters.get(key))[0]);
        }
        
        int year = Integer.parseInt(((String[])parameters.get("year"))[0]);
        int semester = Integer.parseInt(((String[])parameters.get("semester"))[0]);
        String midtermDatesString = ((String[])parameters.get("midtermDates"))[0];
        String acceptanceDatesString = ((String[])parameters.get("acceptanceDates"))[0];
        String finalDatesString = ((String[])parameters.get("finalDates"))[0];
        
        json.put("success", true);
        return SUCCESS;
    }

    public void setServletRequest(HttpServletRequest hsr) {
        request = hsr;
    }
}
