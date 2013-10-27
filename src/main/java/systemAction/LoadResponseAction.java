/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package systemAction;

import com.opensymphony.xwork2.ActionSupport;
import constant.BookingStatus;
import constant.Response;
import constant.Role;
import java.util.ArrayList;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import model.Timeslot;
import org.apache.struts2.interceptor.ServletRequestAware;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.EntityManager;
import manager.UserManager;
import model.Booking;
import model.Team;
import model.Term;
import model.User;
import model.role.Faculty;
import util.MiscUtil;

/**
 *
 * @author Prakhar
 */
public class LoadResponseAction extends ActionSupport implements ServletRequestAware {

    private HttpServletRequest request;
    private static Logger logger = LoggerFactory.getLogger(LoadResponseAction.class);
    private ArrayList<HashMap<String, String>> pendingData = new ArrayList<HashMap<String, String>>();
	private ArrayList<HashMap<String, String>> confirmedData = new ArrayList<HashMap<String, String>>();
	private ArrayList<HashMap<String, String>> termData = new ArrayList<HashMap<String, String>>();
	private long chosenTermId; //The term ID that the user chooses to switch to

    @Override
    public String execute() throws Exception {
		EntityManager em = null;
        try {
			em = MiscUtil.getEntityManagerInstance();
            HttpSession session = request.getSession();
			User user = (User) session.getAttribute("user");
			
			Role activeRole = (Role) session.getAttribute("activeRole");
			if (activeRole.equals(Role.FACULTY)) {
				Faculty faculty = loadFacultyMemberForTerm(em, user); //Load the appropriate faculty object based on the chosen/active term
				
				//Setting the chosen user object in session
				session.setAttribute("user", faculty);
				
				//Getting all the bookings for the faculty for the active term
				Set<Booking> bookingsList = faculty.getRequiredBookings();
				
				//Getting all bookings for which the user's status is pending
				Set<Booking> pendingBookingList = new HashSet<Booking>();
				Set<Booking> confirmedBookingList = new HashSet<Booking>();
				if (bookingsList.size() > 0) {
					for (Booking b : bookingsList) {
						HashMap<User, Response> responseList = b.getResponseList();
						//If the status of a booking is deleted, dont show it (regardless of the faculty's status)
						if (b.getBookingStatus() != BookingStatus.DELETED && 
								b.getBookingStatus() != BookingStatus.REJECTED) {
							if (responseList.get(faculty) == Response.PENDING) {
								pendingBookingList.add(b);
							} else if (responseList.get(faculty) == Response.APPROVED) {
								confirmedBookingList.add(b);
							}
						}
					}
				}

				//Putting all the booking details for the user in a hash map to display it 
				if (pendingBookingList.size() > 0) {
					for (Booking b : pendingBookingList) {
						Timeslot timeslot = b.getTimeslot();
						
						//Getting all the timeslot and booking details
						HashMap<String, String> map = new HashMap<String, String>();
						//SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy HH:mm aa");
						SimpleDateFormat sdfForDate = new SimpleDateFormat("MMM dd, EEE");
						SimpleDateFormat sdfForStartTime = new SimpleDateFormat("HH:mm");
						SimpleDateFormat sdfForEndTime = new SimpleDateFormat("HH:mm aa");
						
						String venue = timeslot.getVenue();
						Long bookingId = b.getId();
						Team team = b.getTeam();
						String teamName = team.getTeamName();
						String milestoneName = timeslot.getSchedule().getMilestone().getName();
						String time = sdfForStartTime.format(timeslot.getStartTime()) + " - " + 
								sdfForEndTime.format(timeslot.getEndTime());
						String date = sdfForDate.format(timeslot.getStartTime());
						String myStatus = b.getResponseList().get(faculty).toString();
						
						//A user can only have 1 role in a team (Supervisor and Reviewer cannot be same for the same team)
						String userRole = null;
						if (team.getSupervisor().equals(faculty)) {
							userRole = "Supervisor";
						} else if (team.getReviewer1().equals(faculty) || team.getReviewer2().equals(faculty)) {
							userRole = "Reviewer";
						}

						map.put("bookingId", String.valueOf(bookingId));
						map.put("teamName", teamName);
						map.put("milestone", milestoneName);
						map.put("userRole", userRole);
						map.put("time", time);
						map.put("date", date);
						map.put("venue", venue);
						map.put("myStatus", myStatus);

						pendingData.add(map);
					}
				}
				
				//Putting all the confirmed booking details for the user in a hash map to display it 
				if (confirmedBookingList.size() > 0) {
					for (Booking b : confirmedBookingList) {
						Timeslot timeslot = b.getTimeslot();
						
						//Getting all the timeslot and booking details
						HashMap<String, String> map = new HashMap<String, String>();
						//SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy HH:mm aa");
						SimpleDateFormat sdfForDate = new SimpleDateFormat("MMM dd, EEE");
						SimpleDateFormat sdfForStartTime = new SimpleDateFormat("HH:mm");
						SimpleDateFormat sdfForEndTime = new SimpleDateFormat("HH:mm aa");
						
						String venue = timeslot.getVenue();
						Long bookingId = b.getId();
						Team team = b.getTeam();
						String teamName = team.getTeamName();
						String milestoneName = timeslot.getSchedule().getMilestone().getName();
						String time = sdfForStartTime.format(timeslot.getStartTime()) + " - " + 
								sdfForEndTime.format(timeslot.getEndTime());
						String date = sdfForDate.format(timeslot.getStartTime());
						String myStatus = b.getResponseList().get(faculty).toString();
						
						//A user can only have 1 role in a team (Supervisor and Reviewer cannot be same for the same team)
						String userRole = null;
						if (team.getSupervisor().equals(faculty)) {
							userRole = "Supervisor";
						} else if (team.getReviewer1().equals(faculty) || team.getReviewer2().equals(faculty)) {
							userRole = "Reviewer";
						}

						map.put("bookingId", String.valueOf(bookingId));
						map.put("teamName", teamName);
						map.put("milestone", milestoneName);
						map.put("userRole", userRole);
						map.put("time", time);
						map.put("date", date);
						map.put("venue", venue);
						map.put("myStatus", myStatus);

						confirmedData.add(map);
					}
				}
				return SUCCESS;
			} else {
				request.setAttribute("error", "Oops. You're not authorized to access this page!");
				MiscUtil.logActivity(logger, user, "User cannot access this page");
				return ERROR;
			}
        } catch (Exception e) {
            logger.error("Exception caught: " + e.getMessage());
            if (MiscUtil.DEV_MODE) {
                for (StackTraceElement s : e.getStackTrace()) {
                    logger.debug(s.toString());
                }
            }
            request.setAttribute("error", "Error with ResponseAction: Escalate to developers!");
            return ERROR;
        } finally {
			if (em != null && em.getTransaction().isActive()) em.getTransaction().rollback();
			if (em != null && em.isOpen()) em.close();
		}
    }
	
	//Load the appropriate faculty object based on the chosen/active term
	private Faculty loadFacultyMemberForTerm(EntityManager em, User user) {
		//Get all the active terms that this user is Faculty for
		ArrayList<Faculty> availableTerms = UserManager.findActiveByRoleAndUsername(em, Faculty.class, user.getUsername());
		Faculty faculty;
		if (chosenTermId != 0) { //User has selected a term ID to switch to
			faculty = getFacultyByTerm(availableTerms, chosenTermId);
			request.getSession().setAttribute("currentActiveTerm", faculty.getTerm());
		} else { //Load object from session
			faculty = em.find(Faculty.class, user.getId());
		}
		availableTerms.remove(faculty); //Removing  chosen/active user from the list
		
		//Populate termData with the remaining available term options
		populateTermDataForDisplay(availableTerms);
		
		return faculty;
	}
	
	private Faculty getFacultyByTerm(ArrayList<Faculty> userObjs, long termId) {
		for (Faculty f : userObjs) {
			if (f.getTerm().getId() == termId) return f;
		}
		return null;
	}
	
	private void populateTermDataForDisplay(ArrayList<Faculty> availableTerms) {
		for (Faculty f : availableTerms) {
			HashMap<String, String> map = new HashMap<String, String>();
			Term term = f.getTerm();
			map.put("termName", term.getDisplayName());
			map.put("termId", String.valueOf(term.getId()));
			termData.add(map);
		}
	}

	public ArrayList<HashMap<String, String>> getTermData() {
		return termData;
	}

	public void setTermData(ArrayList<HashMap<String, String>> termData) {
		this.termData = termData;
	}

	public long getChosenTermId() {
		return chosenTermId;
	}

	public void setChosenTermId(long chosenTermId) {
		this.chosenTermId = chosenTermId;
	}

	public ArrayList<HashMap<String, String>> getPendingData() {
		return pendingData;
	}

	public void setPendingData(ArrayList<HashMap<String, String>> pendingData) {
		this.pendingData = pendingData;
	}

	public ArrayList<HashMap<String, String>> getConfirmedData() {
		return confirmedData;
	}

	public void setConfirmedData(ArrayList<HashMap<String, String>> confirmedData) {
		this.confirmedData = confirmedData;
	}

    public void setServletRequest(HttpServletRequest hsr) {
        this.request = hsr;
    }
}  //end of class