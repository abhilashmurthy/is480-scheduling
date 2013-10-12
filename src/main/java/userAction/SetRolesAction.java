/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package userAction;

import static com.opensymphony.xwork2.Action.ERROR;
import static com.opensymphony.xwork2.Action.SUCCESS;
import com.opensymphony.xwork2.ActionSupport;
import constant.Role;
import java.util.ArrayList;
import java.util.List;
import javax.management.relation.RoleStatus;
import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import manager.UserManager;
import model.User;
import model.role.Faculty;
import model.role.Student;
import model.role.TA;
import org.apache.struts2.interceptor.ServletRequestAware;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.MiscUtil;

/**
 *
 * @author Prakhar
 */
public class SetRolesAction extends ActionSupport implements ServletRequestAware {

    private HttpServletRequest request;
    private static Logger logger = LoggerFactory.getLogger(SetRolesAction.class);
	private long switchToUserId; //User ID of the role to be switched to

    @Override
    public String execute() throws Exception {
		EntityManager em = null;
        try {
            em = MiscUtil.getEntityManagerInstance();
            //Getting the session object
            HttpSession session = request.getSession();
			//Getting the currently active User object
			User currentUser = (User) session.getAttribute("user");
            //Getting all the user objects
            List<User> userRoles = (List<User>) session.getAttribute("userRoles");
			
			for (User u : userRoles) {
				if (u.getId() == switchToUserId) { //Found the role to be switch to
					session.setAttribute("user", UserManager.getUser(u)); //Setting the new user object in the session
					session.setAttribute("activeRole", u.getRole());
					userRoles.remove(u); //Removing the new user object from the list of roles
					userRoles.add(currentUser); //Adding the previously assumed role back into the list
				}
			}
			
			session.setAttribute("userRoles", userRoles); //Refreshing the role options in the session object

			return SUCCESS;
        } catch (Exception e) {
            logger.error("Exception caught: " + e.getMessage());
            if (MiscUtil.DEV_MODE) {
                for (StackTraceElement s : e.getStackTrace()) {
                    logger.debug(s.toString());
                }
            }
            request.setAttribute("error", "Error with SetRoles: Escalate to developers!");
            return ERROR;
        } finally {
			if (em != null && em.getTransaction().isActive()) em.getTransaction().rollback();
			if (em != null && em.isOpen()) em.close();
		}

    } //end of execute function

    public void setServletRequest(HttpServletRequest hsr) {
        this.request = hsr;
    }
	
	public long getSwitchToUserId() {
		return switchToUserId;
	}

	public void setSwitchToUserId(long switchToUserId) {
		this.switchToUserId = switchToUserId;
	}

} //end of class