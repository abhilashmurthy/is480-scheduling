/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package userAction;

import static com.opensymphony.xwork2.Action.SUCCESS;
import com.opensymphony.xwork2.ActionSupport;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import model.Role;
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
    private HttpServletResponse response;
	//One of these variables will have a value if the user has multiple roles
    private String administrator;
    private String supervisorReviewer;
    private String courseCoordinator;
	//Checking whether its users first login or not
	private String firstLogin;
	
    @Override
    public String execute() throws Exception {
        try {
            //Getting the session object
            HttpSession session = request.getSession();
            List<Role> userRoles = (List<Role>) session.getAttribute("userRoles");
			
			if (userRoles.size() > 1) {
				
				boolean isSupervisorReviewer = false;
				boolean isAdministrator = false;
				boolean isCourseCoordinator = false;
				for (Role role: userRoles) {
					if (role.getName().equalsIgnoreCase("Supervisor") || 
							role.getName().equalsIgnoreCase("Reviewer")) {
						isSupervisorReviewer = true;
					} else if (role.getName().equalsIgnoreCase("Administrator")) {
						isAdministrator = true;
					} else if (role.getName().equalsIgnoreCase("Course Coordinator")) {
						isCourseCoordinator = true;
					}
				}
				
				//Setting default role during first login for users with multiple roles
				if (firstLogin != null) {
					if (firstLogin.equalsIgnoreCase("Yes")) {
						if (isSupervisorReviewer) {
							if (isAdministrator || isCourseCoordinator) {
								session.setAttribute("activeRole", "Supervisor/Reviewer");
								return SUCCESS;
							}
						} else if(isAdministrator && isCourseCoordinator) {
							session.setAttribute("activeRole", "Course Coordinator");
							return SUCCESS;
						}
					}
				}
				
				//Checking whether the user is not just supervisor & reviewer 
				if (isAdministrator == true || isCourseCoordinator == true) {
					//Validation checking for user's roles and setting the active role (in case of multiple roles)
					if (administrator != null) {
						if (administrator.equalsIgnoreCase("Administrator")) {
							if (isAdministrator) {
								session.setAttribute("activeRole", "Administrator");
							} else {
								request.setAttribute("rolesError", "You are not authorized to access this page!");
								request.getRequestDispatcher("multipleroles.jsp").forward(request, response);
							}
						}
					} else if (supervisorReviewer != null) {
						if (supervisorReviewer.equalsIgnoreCase("Supervisor/Reviewer")) {
							if (isSupervisorReviewer) {
								session.setAttribute("activeRole", "Supervisor/Reviewer");
							} else {
								request.setAttribute("rolesError", "You are not authorized to access this page!");
								request.getRequestDispatcher("multipleroles.jsp").forward(request, response);
							}
						}
					} else if (courseCoordinator != null) {
						if (courseCoordinator.equalsIgnoreCase("Course Coordinator")) {
							if (isCourseCoordinator) {
								session.setAttribute("activeRole", "Course Coordinator");
							} else {
								request.setAttribute("rolesError", "You are not authorized to access this page!");
								request.getRequestDispatcher("multipleroles.jsp").forward(request, response);
							}
						}
					} else {
						//send error message
						request.setAttribute("rolesError", "Error. Please select a role!");
						request.getRequestDispatcher("multipleroles.jsp").forward(request, response);
					}
				} else {
					// This mean that user is both supervisor & reviewer only
					if (isSupervisorReviewer) {
						session.setAttribute("activeRole", "Supervisor/Reviewer");
					}
				}
				return SUCCESS;
			} 
			
			//Setting active role if user has only 1 role
			for (Role role : userRoles) {
				if (role.getName().equalsIgnoreCase("Supervisor") || role.getName().equalsIgnoreCase("Reviewer")) {
					session.setAttribute("activeRole", "Supervisor/Reviewer");
				} else {
					session.setAttribute("activeRole", role.getName());
				}
			}
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
        }
		
    } //end of execute function

    public void setServletRequest(HttpServletRequest hsr) {
        this.request = hsr;
    }

    public String getAdministrator() {
        return administrator;
    }

    public void setAdministrator(String administrator) {
        this.administrator = administrator;
    }

	public String getSupervisorReviewer() {
		return supervisorReviewer;
	}

	public void setSupervisorReviewer(String supervisorReviewer) {
		this.supervisorReviewer = supervisorReviewer;
	}

	public String getCourseCoordinator() {
		return courseCoordinator;
	}

	public void setCourseCoordinator(String courseCoordinator) {
		this.courseCoordinator = courseCoordinator;
	}

	public String isFirstLogin() {
		return firstLogin;
	}

	public void setFirstLogin(String firstLogin) {
		this.firstLogin = firstLogin;
	}
} //end of class