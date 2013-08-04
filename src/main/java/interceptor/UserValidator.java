/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package interceptor;

import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.interceptor.Interceptor;
import java.util.Map;
import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author suresh
 */
public class UserValidator implements Interceptor{
	Logger logger = LoggerFactory.getLogger(UserValidator.class);

	public String intercept(ActionInvocation ai) throws Exception {
		Map<String, Object> session = ActionContext.getContext().getSession();
		
		if (session != null) {
			User user = (User) session.get("user");
			
			if (user != null) {
				return ai.invoke();	
			}
		}
		
		return "login";
	}

	public void destroy() {
		//Do nothing
	}

	public void init() {
		//Do nothing
	}
	
}
