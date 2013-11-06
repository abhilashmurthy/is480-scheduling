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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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
public class ShowIndexAction extends ActionSupport implements ServletRequestAware {

	private String allTeamsJson;
	private String allUsersJson;
	private String milestonesJson;
	private ArrayList<HashMap<String, Object>> myTeamsData;
    private long termId;   //To get the active term id user chooses
    private int pendingBookingCount;  //To display the number of pending bookings on the index page
    private ArrayList<HashMap<String, String>> data = new ArrayList<HashMap<String, String>>();
    private HashMap<String, Object> json = new HashMap<String, Object>();
    private HttpServletRequest request;
    private static Logger logger = LoggerFactory.getLogger(ShowIndexAction.class);
	private SimpleDateFormat viewDateFormat = new SimpleDateFormat("dd MMM yyyy HH:mm");

    @Override
    public String execute() throws Exception {
        EntityManager em = null;
        try {
            em = MiscUtil.getEntityManagerInstance();
            HttpSession session = request.getSession();


            //<----- 1st Part: To set the current active term based on users response ------>
            //Active term is first set during login. Only if user selects another term from UI will the new active term be set
            if (termId != 0) {
                ArrayList<Term> activeTerms = SettingsManager.getActiveTerms(em);
                for (Term term : activeTerms) {
                    if (term.getId() == termId) {
                        session.setAttribute("currentActiveTerm", term);
						User user = (User) session.getAttribute("user");
						//Refreshing the user object in the session based on the new term selected
						UserManager.initializeUser(em, session, user.getUsername(), user.getFullName(), term);
                    }
                }
            }

            //<----- 2nd Part: Displaying all active terms for the user to choose from ------>
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
			
			//<----- 3rd Part: To get number of pending bookings ------>
            //Checking whether user is supervisor/reviewer
            Role activeRole = (Role) session.getAttribute("activeRole");
            if (activeRole == Role.FACULTY) {
                em.clear();
				User user = (User) session.getAttribute("user");
                Faculty faculty = em.find(Faculty.class, user.getId());
                //Getting all bookings for the user
                for (Booking b : faculty.getRequiredBookings()) {
                    if (b.getBookingStatus() != BookingStatus.DELETED
                            && b.getBookingStatus() != BookingStatus.REJECTED) {
                        if (b.getResponseList().get(faculty) == Response.PENDING) {
                            pendingBookingCount++;
                        }
                    }
                }
				addMyTeamsJson(em, session, faculty);
            }  //end of outer if
			
            //Add teams into session if user is admin/course coordinator
            if (activeRole == Role.ADMINISTRATOR || activeRole == Role.COURSE_COORDINATOR) {
                addTeamsJson(em, session);
            }
            
            //Add users into session if user is admin/course coordinator/student
			addUsersJson(em, session);
			milestonesJson = SettingsManager.getMilestoneSettings(em).getValue();

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
        List<Team> teamList = new ArrayList<Team>();
        List<Booking> bookingList = new ArrayList<Booking>();
        EntityTransaction transaction = em.getTransaction();
        try {
            transaction.begin();
            Query q = em.createQuery("Select t from Team t where term_id = :term")
                    .setParameter("term", term);
			Query qb = em.createQuery("Select b from Booking b where b.timeslot.schedule.milestone.term = :term")
                    .setParameter("term", term);
            teamList = q.getResultList();
			bookingList = qb.getResultList();
            transaction.commit();
        } catch (Exception e) {
            logger.error("Database Operation Error");
			if (MiscUtil.DEV_MODE) {
				for (StackTraceElement s : e.getStackTrace()) {
					logger.debug(s.toString());
				}
			}
        }
        if (teamList != null) {
            myTeamsData = new ArrayList<HashMap<String, Object>>();
            for (Team t : teamList) {
				HashMap<String, Object> teamMap = new HashMap<String, Object>();
				teamMap.put("teamName", t.getTeamName());
				teamMap.put("teamId", t.getId());
				List<String> memberEmailList = new ArrayList<String>();
				for (Student s : t.getMembers()) {
					memberEmailList.add(s.getUsername() + "@smu.edu.sg");
				}
				teamMap.put("memberEmails", memberEmailList);
				List<HashMap<String, Object>> teamBookingsList = new ArrayList<HashMap<String, Object>>();
				for (Booking b : bookingList) {
					if (b.getTeam().equals(t) && b.getBookingStatus() != BookingStatus.DELETED && b.getBookingStatus() != BookingStatus.REJECTED) {
						HashMap<String, Object> bookingMap = new HashMap<String, Object>();
						bookingMap.put("datetime", viewDateFormat.format(b.getTimeslot().getStartTime()));
						bookingMap.put("milestone", b.getTimeslot().getSchedule().getMilestone().getName().toLowerCase());
						bookingMap.put("scheduleId", b.getTimeslot().getSchedule().getId());
						bookingMap.put("bookingStatus", b.getBookingStatus().toString().toLowerCase());
						teamBookingsList.add(bookingMap);
					}
				}
				teamMap.put("bookings", teamBookingsList);
				myTeamsData.add(teamMap);
            }
			sortMyTeamsData();
			allTeamsJson = new Gson().toJson(myTeamsData);
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
			if (MiscUtil.DEV_MODE) {
				for (StackTraceElement s : e.getStackTrace()) {
					logger.debug(s.toString());
				}
			}
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
			allUsersJson = new Gson().toJson(uniqueUsers.toArray());
        }
    }
	
	public void addMyTeamsJson(EntityManager em, HttpSession session, Faculty me) {
        //Get Teams from here and populate into session
        Term term = (Term) session.getAttribute("currentActiveTerm");
        List<Team> teamList = new ArrayList<Team>();
		List<Booking> bookingList = new ArrayList<Booking>();
        EntityTransaction transaction = em.getTransaction();
        try {
            transaction.begin();
            Query q = em.createQuery("Select t from Team t where term_id = :term")
                    .setParameter("term", term);
			Query qb = em.createQuery("Select b from Booking b where b.timeslot.schedule.milestone.term = :term")
                    .setParameter("term", term);
            teamList = q.getResultList();
			bookingList = qb.getResultList();
            transaction.commit();
        } catch (Exception e) {
            logger.error("Database Operation Error");
			if (MiscUtil.DEV_MODE) {
				for (StackTraceElement s : e.getStackTrace()) {
					logger.debug(s.toString());
				}
			}
        }
        if (teamList != null) {
            myTeamsData = new ArrayList<HashMap<String, Object>>();
            for (Team t : teamList) {
				if (t.getSupervisor().equals(me) || t.getReviewer1().equals(me) || t.getReviewer2().equals(me)) {
					HashMap<String, Object> teamMap = new HashMap<String, Object>();
					teamMap.put("teamName", t.getTeamName());
					teamMap.put("teamId", t.getId());
					List<String> memberEmailList = new ArrayList<String>();
					for (Student s : t.getMembers()) {
						memberEmailList.add(s.getUsername() + "@smu.edu.sg");
					}
					teamMap.put("memberEmails", memberEmailList);
					List<String> myRolesList = new ArrayList<String>();
					if (t.getSupervisor().equals(me)) myRolesList.add("supervisor");
					if (t.getReviewer1().equals(me)) myRolesList.add("reviewer1");
					if (t.getReviewer2().equals(me)) myRolesList.add("reviewer2");
					teamMap.put("myRoles", myRolesList);
					List<HashMap<String, Object>> teamBookingsList = new ArrayList<HashMap<String, Object>>();
					for (Booking b : bookingList) {
						if (b.getTeam().equals(t) && b.getBookingStatus() != BookingStatus.DELETED && b.getBookingStatus() != BookingStatus.REJECTED) {
							HashMap<String, Object> bookingMap = new HashMap<String, Object>();
							bookingMap.put("datetime", viewDateFormat.format(b.getTimeslot().getStartTime()));
							bookingMap.put("milestone", b.getTimeslot().getSchedule().getMilestone().getName().toLowerCase());
							bookingMap.put("scheduleId", b.getTimeslot().getSchedule().getId());
							bookingMap.put("bookingStatus", b.getBookingStatus().toString().toLowerCase());
							teamBookingsList.add(bookingMap);
						}
					}
					teamMap.put("bookings", teamBookingsList);
					myTeamsData.add(teamMap);
				}
            }
			sortMyTeamsData();
			allTeamsJson = new Gson().toJson(myTeamsData);
        }
	}
	
	public void sortMyTeamsData() {
		Collections.sort(myTeamsData, new Comparator<HashMap<String, Object>>(){
			public int compare(HashMap<String, Object> o1, HashMap<String, Object> o2) {
				return String.valueOf(o1.get("teamName")).compareToIgnoreCase(String.valueOf(o2.get("teamName")));
			}
		});
		Collections.sort(myTeamsData, new Comparator<HashMap<String, Object>>(){
			public int compare(HashMap<String, Object> o1, HashMap<String, Object> o2) {
				if (!((List) o1.get("bookings")).isEmpty()) return 1;
				else return 0;
			}
		});
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
	
	public String getAllTeamsJson() {
		return allTeamsJson;
	}

	public void setAllTeams(String allTeamsJson) {
		this.allTeamsJson = allTeamsJson;
	}

	public String getAllUsersJson() {
		return allUsersJson;
	}

	public void setAllUsers(String allUsersJson) {
		this.allUsersJson = allUsersJson;
	}
	
	public ArrayList<HashMap<String, Object>> getMyTeamsData() {
		return myTeamsData;
	}

	public void setMyTeamsData(ArrayList<HashMap<String, Object>> myTeamsData) {
		this.myTeamsData = myTeamsData;
	}
	
	public String getMilestonesJson() {
		return milestonesJson;
	}

	public void setMilestonesJson(String milestonesJson) {
		this.milestonesJson = milestonesJson;
	}
	
} //end of class
