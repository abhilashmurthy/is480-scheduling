/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package systemAction;

import com.google.gson.Gson;
import static com.opensymphony.xwork2.Action.SUCCESS;
import com.opensymphony.xwork2.ActionSupport;
import constant.BookingStatus;
import constant.Response;
import constant.Role;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.Query;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import manager.SettingsManager;
import manager.UserManager;
import model.Booking;
import model.Team;
import model.Term;
import model.User;
import model.role.Faculty;
import org.apache.struts2.interceptor.ServletRequestAware;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.MiscUtil;

/**
 *
 * @author Prakhar
 */
public class ShowIndexAction extends ActionSupport implements ServletRequestAware {

    private long termId;   //To get the active term id user chooses
    private int pendingBookingCount;  //To display the number of pending bookings on the index page
    private ArrayList<HashMap<String, String>> data = new ArrayList<HashMap<String, String>>();
    private HashMap<String, Object> json = new HashMap<String, Object>();
    private HttpServletRequest request;
    private static Logger logger = LoggerFactory.getLogger(ShowIndexAction.class);

    @Override
    public String execute() throws Exception {
        EntityManager em = null;
        try {
            em = MiscUtil.getEntityManagerInstance();
            HttpSession session = request.getSession();
            User user = (User) session.getAttribute("user");

            //<----- 1st Part: To get number of pending bookings ------>
            //Checking whether user is supervisor/reviewer
            Role activeRole = (Role) session.getAttribute("activeRole");
            if (activeRole == Role.FACULTY) {
                em.clear();
                Faculty faculty = em.find(Faculty.class, user.getId());
                session.setAttribute("user", faculty);
                //Getting all bookings for the user
                for (Booking b : faculty.getRequiredBookings()) {
                    if (b.getBookingStatus() != BookingStatus.DELETED
                            && b.getBookingStatus() != BookingStatus.REJECTED) {
                        if (b.getResponseList().get(faculty) == Response.PENDING) {
                            pendingBookingCount++;
                        }
                    }
                }
            }  //end of outer if

            //<----- 2rd Part: To set the current active term based on users response ------>
            //Active term is first set during login. Only if user selects another term from UI will the new active term be set
            if (termId != 0) {
                ArrayList<Term> activeTerms = SettingsManager.getActiveTerms(em);
                for (Term term : activeTerms) {
                    if (term.getId() == termId) {
                        session.setAttribute("currentActiveTerm", term);
						//Refreshing the user object in the session based on the new term selected
						new UserManager().initializeUser(em, session, user.getUsername(), user.getFullName(), term);
                    }
                }
            }

            //<----- 3rd Part: Displaying all active terms for the user to choose from ------>
            ArrayList<Term> allActiveTerms = SettingsManager.getActiveTerms(em);
            //Removing the active term set during login or in the above code from the list to be displayed
            Term activeTerm = (Term) session.getAttribute("currentActiveTerm");
            if (activeTerm != null) {
                allActiveTerms.remove(activeTerm);
            }
            if (allActiveTerms.size() > 0) {
                for (Term term : allActiveTerms) {
                    HashMap<String, String> map = new HashMap<String, String>();
                    long idOfTerm = term.getId();

                    map.put("termName", term.getDisplayName());
                    map.put("termId", String.valueOf(idOfTerm));

                    data.add(map);
                }
            }
            //Add teams into session if user is admin/course coordinator
            if (activeRole == Role.ADMINISTRATOR || activeRole == Role.COURSE_COORDINATOR) {
                addTeamsJson(em, session);
            }
            
            //Add users into session if user is admin/course coordinator/student
            if (activeRole != Role.TA) {
                addUsersJson(em, session);
            }

        } catch (Exception e) {
            logger.error("Exception caught: " + e.getMessage());
            if (MiscUtil.DEV_MODE) {
                for (StackTraceElement s : e.getStackTrace()) {
                    logger.debug(s.toString());
                }
            }
            json.put("success", false);
            json.put("exception", true);
            json.put("message", "Error with Showing Index Page: Escalate to developers!");
        } finally {
            if (em != null && em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            if (em != null && em.isOpen()) {
                em.close();
            }
        }
        return SUCCESS;
    } //end of execute

    public void addTeamsJson(EntityManager em, HttpSession session) {
        //Get Teams from here and populate into session
        Term term = (Term) session.getAttribute("currentActiveTerm");
        List<Team> teamList = null;
        EntityTransaction transaction = em.getTransaction();
        try {
            transaction.begin();
            Query q = em.createQuery("Select t from Team t where term_id = :term")
                    .setParameter("term", term);
            teamList = q.getResultList();
            transaction.commit();
        } catch (Exception e) {
            logger.error("Database Operation Error");
        }
        if (teamList != null) {
            ArrayList<HashMap<String, Object>> teamJsonList = new ArrayList<HashMap<String, Object>>();
            for (Team t : teamList) {
                HashMap<String, Object> teamMap = new HashMap<String, Object>();
                teamMap.put("teamName", t.getTeamName());
                teamMap.put("teamId", t.getId());
                teamJsonList.add(teamMap);
            }
            session.setAttribute("allTeams", new Gson().toJson(teamJsonList));
        }
    }
    
    public void addUsersJson(EntityManager em, HttpSession session) {
        //Get all users
        Term term = (Term) session.getAttribute("currentActiveTerm");
        List<User> userList = null;
        EntityTransaction transaction = em.getTransaction();
        try {
            transaction.begin();
            Query q = em.createQuery("select u from User u");
            userList = q.getResultList();
            transaction.commit();
        } catch (Exception e) {
            logger.error("Database Operation Error");
        }
        if (userList != null) {
            ArrayList<HashMap<String, Object>> userJsonList = new ArrayList<HashMap<String, Object>>();
            for (User u : userList) {
                HashMap<String, Object> userMap = new HashMap<String, Object>();
                userMap.put("id", u.getUsername() + "@smu.edu.sg");
                userMap.put("name", u.getFullName());
                userJsonList.add(userMap);
            }
            HashSet<HashMap<String, Object>> uniqueUsers = new HashSet<HashMap<String, Object>>(userJsonList);
            session.setAttribute("allUsers", new Gson().toJson(uniqueUsers.toArray()));
        }
    }

    public void setServletRequest(HttpServletRequest hsr) {
        request = hsr;
    }

    public long getTermId() {
        return termId;
    }

    public void setTermId(long termId) {
        this.termId = termId;
    }

    public int getPendingBookingCount() {
        return pendingBookingCount;
    }

    public void setPendingBookingCount(int pendingBookingCount) {
        this.pendingBookingCount = pendingBookingCount;
    }

    public HashMap<String, Object> getJson() {
        return json;
    }

    public void setJson(HashMap<String, Object> json) {
        this.json = json;
    }

    public ArrayList<HashMap<String, String>> getData() {
        return data;
    }

    public void setData(ArrayList<HashMap<String, String>> data) {
        this.data = data;
    }
} //end of class
