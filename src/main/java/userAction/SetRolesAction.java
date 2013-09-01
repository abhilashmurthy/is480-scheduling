/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package userAction;

import com.google.gson.Gson;
import static com.opensymphony.xwork2.Action.ERROR;
import static com.opensymphony.xwork2.Action.SUCCESS;
import com.opensymphony.xwork2.ActionSupport;
import constant.Role;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import javax.persistence.Query;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import model.Team;
import model.Term;
import model.User;
import model.role.Faculty;
import model.role.Student;
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
    private String faculty;
    private String courseCoordinator;
    //Checking whether its users first login or not
    private String firstLogin;

    @Override
    public String execute() throws Exception {
		EntityManager em = null;
        try {
            em = Persistence.createEntityManagerFactory(MiscUtil.PERSISTENCE_UNIT).createEntityManager();
            //Getting the session object
            HttpSession session = request.getSession();
            //Getting all the user objects
            List<User> userRoles = (List<User>) session.getAttribute("userRoles");

            if (userRoles.size() > 1) {
                boolean isFaculty = false;
                boolean isAdministrator = false;
                boolean isCourseCoordinator = false;
                long facultyId = 0;
                long administratorId = 0;
                long courseCoordinatorId = 0;

                for (User user : userRoles) {
                    if (user.getRole().equals(Role.FACULTY)) {
                        isFaculty = true;
                        facultyId = user.getId();
                    } else if (user.getRole().equals(Role.ADMINISTRATOR)) {
                        isAdministrator = true;
                        administratorId = user.getId();
                    } else if (user.getRole().equals(Role.COURSE_COORDINATOR)) {
                        isCourseCoordinator = true;
                        courseCoordinatorId = user.getId();
                    }
                }

                //Setting default role during first login for users with multiple roles
                if (firstLogin != null && firstLogin.equalsIgnoreCase("Yes")) {
                    if (isFaculty) {
                        if (isAdministrator || isCourseCoordinator) {
                            session.setAttribute("activeRole", Role.FACULTY);
                            Faculty faculty = em.find(Faculty.class, facultyId);
                            session.setAttribute("user", faculty);
                        }
                    } else if (isAdministrator && isCourseCoordinator) {
                        session.setAttribute("activeRole", Role.COURSE_COORDINATOR);
                        User userCC = em.find(User.class, courseCoordinatorId);
                        session.setAttribute("user", userCC);
                    }
                    return SUCCESS;
                }

                //Validation checking for user's roles and setting the active role (in case of multiple roles)
                if (administrator != null) {
                    if (administrator.equalsIgnoreCase("Administrator")) {
                        if (isAdministrator) {
                            session.setAttribute("activeRole", Role.ADMINISTRATOR);
                            User userAdmin = em.find(User.class, administratorId);
                            session.setAttribute("user", userAdmin);
                        } else {
                            request.setAttribute("error", "Error with SetRoles: Escalate to developers!");
                            return ERROR;
                        }
                    }
                } else if (faculty != null) {
                    if (faculty.equalsIgnoreCase("Faculty")) {
                        if (isFaculty) {
                            session.setAttribute("activeRole", Role.FACULTY);
                            Faculty faculty = em.find(Faculty.class, facultyId);
                            session.setAttribute("user", faculty);
                        } else {
                            request.setAttribute("error", "Error with SetRoles: Escalate to developers!");
                            return ERROR;
                        }
                    }
                } else if (courseCoordinator != null) {
                    if (courseCoordinator.equalsIgnoreCase("Course Coordinator")) {
                        if (isCourseCoordinator) {
                            session.setAttribute("activeRole", Role.COURSE_COORDINATOR);
                            User userCC = em.find(User.class, courseCoordinatorId);
                            session.setAttribute("user", userCC);
                        } else {
                            request.setAttribute("error", "Error with SetRoles: Escalate to developers!");
                            return ERROR;
                        }
                    }
                } else {
                    //send error message
                    request.setAttribute("error", "Error with SetRoles: Escalate to developers!");
                    return ERROR;
                }
                return SUCCESS;
            }

            // --------When user has only 1 role---------
            //Setting active role 
            session.setAttribute("activeRole", userRoles.get(0).getRole());
            if (userRoles.get(0).getRole().equals(Role.ADMINISTRATOR)) {
                session.setAttribute("user", userRoles.get(0));
            } else if (userRoles.get(0).getRole().equals(Role.COURSE_COORDINATOR)) {
                session.setAttribute("user", userRoles.get(0));
            } else if (userRoles.get(0).getRole().equals(Role.FACULTY)) {
                Faculty faculty = em.find(Faculty.class, userRoles.get(0).getId());
                session.setAttribute("user", faculty);
            } else if (userRoles.get(0).getRole().equals(Role.STUDENT)) {
                Student student = em.find(Student.class, userRoles.get(0).getId());
                session.setAttribute("user", student);
            } else if (userRoles.get(0).getRole().equals(Role.TA)) {
                //TA ta = em.find(TA.class, userRoles.get(0).getId());
                //session.setAttribute("user", ta);
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
        } finally {
			if (em != null && em.getTransaction().isActive()) em.getTransaction().rollback();
			if (em != null && em.isOpen()) em.close();
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

    public String getFaculty() {
        return faculty;
    }

    public void setFaculty(String faculty) {
        this.faculty = faculty;
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