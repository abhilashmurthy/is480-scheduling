/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package userAction;

import static com.opensymphony.xwork2.Action.ERROR;
import static com.opensymphony.xwork2.Action.SUCCESS;
import com.opensymphony.xwork2.ActionSupport;
import constant.BookingStatus;
import constant.Response;
import constant.Role;
import java.util.ArrayList;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import model.Schedule;
import model.Timeslot;
import model.User;
import org.apache.struts2.interceptor.ServletRequestAware;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import javax.persistence.EntityManager;
import manager.BookingManager;
import manager.SettingsManager;
import manager.UserManager;
import model.Booking;
import model.Term;
import model.role.Faculty;
import model.role.Student;
import model.role.TA;
import util.MiscUtil;

/**
 *
 * @author Prakhar
 */
public class BookingHistoryAction extends ActionSupport implements ServletRequestAware {

    private HttpServletRequest request;
    private static Logger logger = LoggerFactory.getLogger(BookingHistoryAction.class);
    private ArrayList<HashMap<String, Object>> data = new ArrayList<HashMap<String, Object>>();
	private ArrayList<HashMap<String, String>> termData = new ArrayList<HashMap<String, String>>();
	private long chosenTermId; //The term ID that the user chooses to switch to
    private HashMap<String, Object> json = new HashMap<String, Object>();

    @Override
    public String execute() throws Exception {
		EntityManager em = null;
        try {
            em = MiscUtil.getEntityManagerInstance();
            HttpSession session = request.getSession();
            //Getting the active role of the user
            Role activeRole = (Role) session.getAttribute("activeRole");
			
			//Setting updated user object in session
			//For updating user object after booking is creating (Status update & delete booking code already present in respective files)
			em.clear();
			User user = (User) session.getAttribute("user");
			if (user.getRole() == Role.ADMINISTRATOR || user.getRole() == Role.COURSE_COORDINATOR) { //Only load term dropdown for permanent roles
				ArrayList<Term> allActiveTerms = SettingsManager.getActiveTerms(em);
				Term termToDisplay = (Term) request.getSession().getAttribute("currentActiveTerm");
				if (chosenTermId != 0) { //User has selected a term ID to switch to
					termToDisplay = getTermById(allActiveTerms, chosenTermId);
					request.getSession().setAttribute("currentActiveTerm", termToDisplay);
				}
				allActiveTerms.remove(termToDisplay); //Removing the active/chosen term from getting populated in the dropdown list
				for (Term t : allActiveTerms) {
					addTermDataToDropdown(t);
				}
			} else { //Load applicable terms and objects for other users
				//Loading the appropriate object for the user based on the term selected and populating the term dropdown with available options
				user = loadUserForTerm(em, user, user.getRole().getBaseClassType());
			}
			session.setAttribute("user", user);
			
				
			/* Getting active term. Only bookings for active term will be displayed to the user. 
			 * If the user wishes to view bookin history for another term, he/she will have to change the 
			 * active term from the dropdown list.
			*/
			Term activeTerm = (Term) session.getAttribute("currentActiveTerm");
			
			Set<Booking> bookings = new HashSet<Booking>();
			//Retrieving all bookings for admin and course coordinator
			if (activeRole.equals(Role.ADMINISTRATOR) || activeRole.equals(Role.COURSE_COORDINATOR)) {
				//Getting all the bookings for the active term
				ArrayList<Booking> bookingsList = BookingManager.getBookingsByTerm(em, activeTerm);
				//Converting list to set (Duplicated values will be ignored)
				if (bookingsList != null) {
					bookings = new HashSet(bookingsList);
				}
				
			} else if (activeRole.equals(Role.TA)) {
				/* TO DO after TA Model class is ready */
				 TA ta = (TA) session.getAttribute("user");
				 List<Timeslot> taTimeslots = null;
				 taTimeslots = ta.getChosenTimeslots();
				 for (Timeslot t : taTimeslots) {
					Booking taBooking = t.getCurrentBooking();
					if (taBooking != null && taBooking.getBookingStatus() == BookingStatus.APPROVED) {
						bookings.add(taBooking);
					}
				 }
			} else {
				
				Faculty faculty = null;
				Student student = null;
				if (activeRole.equals(Role.FACULTY)) {
					faculty = em.find(Faculty.class, user.getId());
					bookings = faculty.getRequiredBookings();
				} else if (activeRole.equals(Role.STUDENT)) {
					student = em.find(Student.class, user.getId());
					bookings = student.getRequiredBookings();
					for (Booking b: bookings) {
						Term term = b.getTimeslot().getSchedule().getMilestone().getTerm();
					}
				}
			}
			
			
			//Iterating over the list and getting the necessary details
			if (bookings != null && bookings.size() > 0) {
				List<Booking> pendingBookings = new ArrayList<Booking>();
				List<Booking> approvedBookings = new ArrayList<Booking>();
				List<Booking> rejectedBookings = new ArrayList<Booking>();
				List<Booking> deletedBookings = new ArrayList<Booking>();
				
				/* Arranging the booking depending on their status. 
				 * Bookings will be displayed in the following order: PENDING->APPROVED->RJEJECTED->DELETED
				 * Under each category, they will be sorted by "Latest Date Modified"
				 */
				for (Booking b: bookings) {
					if (b.getBookingStatus() == BookingStatus.PENDING) {
						pendingBookings.add(b);
					} else if (b.getBookingStatus() == BookingStatus.APPROVED) {
						approvedBookings.add(b);
					} else if (b.getBookingStatus() == BookingStatus.REJECTED) {
						rejectedBookings.add(b);
					} else if (b.getBookingStatus() == BookingStatus.DELETED) {
						deletedBookings.add(b);
					}
				}
				
				//Adding bookings in the relevant order but sorting according to timestamp
				ArrayList<Booking> orderedBookings = new ArrayList<Booking>();
				if (pendingBookings.size() > 0) {
					Long[] ts = new Long[pendingBookings.size()];
					int i = 0;
					for (Booking b: pendingBookings) {
						ts[i] = b.getLastEditedAt().getTime();
						i++;
					}
					ts = sortTimestamps(ts);
					for (int j = 0; j < ts.length; j++) {
						for (Booking b: pendingBookings) {
							if (b.getLastEditedAt().getTime() == ts[j]) {
								orderedBookings.add(b);
								break;
							}
						}
					}
				}
				if (approvedBookings.size() > 0) {
					Long[] ts = new Long[approvedBookings.size()];
					int i = 0;
					for (Booking b: approvedBookings) {
						ts[i] = b.getLastEditedAt().getTime();
						i++;
					}
					ts = sortTimestamps(ts);
					for (int j = 0; j < ts.length; j++) {
						for (Booking b: approvedBookings) {
							if (b.getLastEditedAt().getTime() == ts[j]) {
								orderedBookings.add(b);
								break;
							}
						}
					}
				}
				if (rejectedBookings.size() > 0) {
					Long[] ts = new Long[rejectedBookings.size()];
					int i = 0;
					for (Booking b: rejectedBookings) {
						ts[i] = b.getLastEditedAt().getTime();
						i++;
					}
					ts = sortTimestamps(ts);
					for (int j = 0; j < ts.length; j++) {
						for (Booking b: rejectedBookings) {
							if (b.getLastEditedAt().getTime() == ts[j]) {
								orderedBookings.add(b);
								break;
							}
						}
					}
				}
				if (deletedBookings.size() > 0) {
					Long[] ts = new Long[deletedBookings.size()];
					int i = 0;
					for (Booking b: deletedBookings) {
						ts[i] = b.getLastEditedAt().getTime();
						i++;
					}
					ts = sortTimestamps(ts);
					for (int j = 0; j < ts.length; j++) {
						for (Booking b: deletedBookings) {
							if (b.getLastEditedAt().getTime() == ts[j]) {
								orderedBookings.add(b);
								break;
							}
						}
					}
				}
				
				//<---------Getting the actual details of the bookings starts here-------->
				for (Booking b: orderedBookings) {
					Timeslot timeslot = b.getTimeslot();
					HashMap<String, Object> map = new HashMap<String, Object>();
					SimpleDateFormat sdfForDate = new SimpleDateFormat("MMM dd, EEE");
					SimpleDateFormat sdfForStartTime = new SimpleDateFormat("HH:mm");
					SimpleDateFormat sdfForEndTime = new SimpleDateFormat("HH:mm aa");

					String venue = timeslot.getVenue();
					String teamName = b.getTeam().getTeamName();
					//Getting the schedule based on timeslot (Each timeslot belongs to 1 unique schedule)
					Schedule schedule = timeslot.getSchedule();
					String milestoneName = "";
					if (schedule != null) {
						milestoneName = schedule.getMilestone().getName();
					}
					String date = sdfForDate.format(timeslot.getStartTime());
					String time = sdfForStartTime.format(timeslot.getStartTime()) + "-" + 
							sdfForEndTime.format(timeslot.getEndTime());

					//Only for supervisors/reviewers (or Faculty)
					if (activeRole.equals(Role.FACULTY)) {
						Faculty faculty = em.find(Faculty.class, user.getId());
						String myStatus = b.getResponseList().get(faculty).toString();
						map.put("myStatus", myStatus);
					}

					//Overall status (will be seen by all stakeholders)
					String overallBookingStatus = b.getBookingStatus().toString();
					map.put("overallBookingStatus", overallBookingStatus);

					if (activeRole.equals(Role.STUDENT) || activeRole.equals(Role.ADMINISTRATOR) 
							|| activeRole.equals(Role.COURSE_COORDINATOR)) {
						//Detailed status
						List<HashMap<String, String>> individualStatusList = new ArrayList<HashMap<String, String>>();
						if (b.getResponseList() != null) {
							for (Entry<User, Response> e: b.getResponseList().entrySet()) {
								HashMap<String, String> userMap = new HashMap<String, String>();
								userMap.put("name", e.getKey().getFullName());
								userMap.put("status", e.getValue().toString());

								individualStatusList.add(userMap);
							}
						}
						map.put("individualBookingStatus", individualStatusList);
					}
					
					//Getting the reason for rejection if booking has been rejected
					String comment = b.getComment();
					
					//Getting the last edited by and time for the booking
					SimpleDateFormat sdfForEdited = new SimpleDateFormat("MMM dd, HH:mm:ss");
					String lastModifiedAt = sdfForEdited.format(b.getLastEditedAt());
					String lastModifiedBy = b.getLastEditedBy();
					
					//Getting the list of users who have subscribed to the booking
					Set<User> subscribedUsers = b.getSubscribedUsers();
					List<HashMap<String, String>> sUsernamesList = new ArrayList<HashMap<String, String>>();
					if (subscribedUsers.size() > 0) {
						for (User sUser: subscribedUsers) {
							HashMap<String, String> userMap = new HashMap<String, String>();
							userMap.put("username", sUser.getFullName());
							sUsernamesList.add(userMap);
						}
					}
					map.put("subscribedUsers", sUsernamesList);
					map.put("noOfSubscribers", sUsernamesList.size());
					
					map.put("bookingId", b.getId());
					map.put("teamName", teamName);
					map.put("milestone", milestoneName);
					map.put("date", date);
					map.put("time", time);
					map.put("venue", venue);
					map.put("comment", comment);
					map.put("lastModifiedAt", lastModifiedAt);
					map.put("lastModifiedBy", lastModifiedBy);


					data.add(map);
				}
			}
			json.put("success", true);
			
			MiscUtil.logActivity(logger, user, "Viewed booking history (My Bookings)");
			return SUCCESS;

		} catch (Exception e) {
            logger.error("Exception caught: " + e.getMessage());
            if (MiscUtil.DEV_MODE) {
                for (StackTraceElement s : e.getStackTrace()) {
                    logger.debug(s.toString());
                }
            }
            json.put("success", false);
            json.put("message", "Error with BookingHistory: Escalate to developers!");
//			request.setAttribute("error", "Error with BookingHistory: Escalate to developers!");
        } finally {
			if (em != null && em.getTransaction().isActive()) em.getTransaction().rollback();
			if (em != null && em.isOpen()) em.close();
		}
        return ERROR;
    } //end of execute function
	
	//Load the appropriate faculty object based on the chosen/active term
	private <T extends User> T loadUserForTerm(EntityManager em, User user, Class<T> baseClass) {
		//Get all the active terms applicable for this user
		ArrayList<T> availableTerms = UserManager.findActiveByRoleAndUsername(em, baseClass, user.getUsername());
		T currentUser;
		if (chosenTermId != 0) { //User has selected a term ID to switch to
			currentUser = getUserByTerm(availableTerms, chosenTermId);
			request.getSession().setAttribute("currentActiveTerm", currentUser.getTerm());
		} else { //Load object from session
			currentUser = em.find(baseClass, user.getId());
		}
		availableTerms.remove(currentUser); //Removing  chosen/active user from the list
		
		//Populate termData with the remaining available term options
		populateTermDataForDisplay(availableTerms);
		
		return currentUser;
	}
	
	private <T extends User> T getUserByTerm(ArrayList<T> userObjs, long termId) {
		for (T user : userObjs) {
			if (user.getTerm().getId() == termId) return user;
		}
		return null;
	}
	
	private <T extends User> void populateTermDataForDisplay(ArrayList<T> availableTerms) {
		for (T user : availableTerms) {
			addTermDataToDropdown(user.getTerm());
		}
	}
	
	private void addTermDataToDropdown(Term term) {
		HashMap<String, String> map = new HashMap<String, String>();
		map.put("termName", term.getDisplayName());
		map.put("termId", String.valueOf(term.getId()));
		termData.add(map);
	}
	
	private Term getTermById (ArrayList<Term> terms, long id) {
		for (Term t : terms) {
			if (t.getId() == id) return t;
		}
		return null;
	}

	//Sorting timestamps by descending order (latest) to sort bookings 
	private static Long[] sortTimestamps(Long[] ts) {
		for (int i = 0; i < ts.length - 1; i++) {
			for (int j = 1; j < ts.length; j++) {
				if (ts[j] > ts[i]) {
					long temp = 0;
					temp = ts[i];
					ts[i] = ts[j];
					ts[j] = temp;
				}
			}
		}
		return ts;
	}
	
    public ArrayList<HashMap<String, Object>> getData() {
        return data;
    }

    public void setData(ArrayList<HashMap<String, Object>> data) {
        this.data = data;
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

    public HashMap<String, Object> getJson() {
        return json;
    }

    public void setJson(HashMap<String, Object> json) {
        this.json = json;
    }

    public HttpServletRequest getRequest() {
        return request;
    }

    public void setServletRequest(HttpServletRequest hsr) {
        this.request = hsr;
    }
} //end of class