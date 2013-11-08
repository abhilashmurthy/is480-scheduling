/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package userAction;

import com.opensymphony.xwork2.ActionSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author suresh
 */
public class AdminLoginAction extends ActionSupport {
	
	private String username;
	private String password;
	private Logger logger = LoggerFactory.getLogger(AdminLoginAction.class);

	@Override
	public String execute() throws Exception {
		logger.info("Username: " + username);
		logger.info("Password: " + password);
		return ERROR;
	}
	
	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

}
