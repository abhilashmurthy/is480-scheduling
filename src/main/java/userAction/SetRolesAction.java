/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package userAction;

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
    private String administrator;
    private String supervisor;
    private String reviewer;

    @Override
    public String execute() throws Exception {
        try {
            //Getting the session object
            HttpSession session = request.getSession();
            boolean isStudent = (Boolean) session.getAttribute("isStudent");
            boolean isSupervisor = (Boolean) session.getAttribute("isSupervisor");
            boolean isReviewer = (Boolean) session.getAttribute("isReviewer");
            boolean isTA = (Boolean) session.getAttribute("isTA");
            boolean isAdmin = (Boolean) session.getAttribute("isAdmin");
            List<Role> userRoles = (List<Role>) session.getAttribute("userRoles");

            if (userRoles.size() > 1) {
                //Validation checking for user's roles and setting the active role (in case of multiple roles)
                if (administrator != null) {
                    if (administrator.equalsIgnoreCase("Administrator")) {
                        if (isAdmin) {
                            session.setAttribute("activeRole", "Administrator");
                        } else {
                            request.setAttribute("rolesError", "You are not authorized to access this page!");
                            request.getRequestDispatcher("multipleroles.jsp").forward(request, response);
                        }
                    }
                } else if (supervisor != null) {
                    if (supervisor.equalsIgnoreCase("Supervisor")) {
                        if (isSupervisor) {
                            session.setAttribute("activeRole", "Supervisor");
                        } else {
                            request.setAttribute("rolesError", "You are not authorized to access this page!");
                            request.getRequestDispatcher("multipleroles.jsp").forward(request, response);
                        }
                    }
                } else if (reviewer != null) {
                    if (reviewer.equalsIgnoreCase("Reviewer")) {
                        if (isReviewer) {
                            session.setAttribute("activeRole", "Reviewer");
                        } else {
                            request.setAttribute("rolesError", "You are not authorized to access this page!");
                            request.getRequestDispatcher("multipleroles.jsp").forward(request, response);
                        }
                    }
                } else {
                    //send error message
                    request.setAttribute("rolesError", "Error. Please select a correct role!");
                    request.getRequestDispatcher("multipleroles.jsp").forward(request, response);
                }

                //Checking user's inactive role(s)
                List<Role> inactiveRoles = new ArrayList<Role>();
                String activeRole = (String) session.getAttribute("activeRole");
                for (Role role : userRoles) {
                    if (!role.getName().equalsIgnoreCase(activeRole)) {
                        inactiveRoles.add(role);
                    }
                }
                session.setAttribute("inactiveRolesList", inactiveRoles);   //Putting inactive roles in session

            } else if (userRoles.size() == 1) {    //Setting active role if user has only 1 role
                for (Role role : userRoles) {
                    session.setAttribute("activeRole", role.getName());
                }
            }
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
        return SUCCESS;
    }

    public void setServletRequest(HttpServletRequest hsr) {
        this.request = hsr;
    }

    public String getAdministrator() {
        return administrator;
    }

    public void setAdministrator(String administrator) {
        this.administrator = administrator;
    }

    public String getSupervisor() {
        return supervisor;
    }

    public void setSupervisor(String supervisor) {
        this.supervisor = supervisor;
    }

    public String getReviewer() {
        return reviewer;
    }

    public void setReviewer(String reviewer) {
        this.reviewer = reviewer;
    }
} //end of class