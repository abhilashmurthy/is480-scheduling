/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package userAction;

import static com.opensymphony.xwork2.Action.SUCCESS;
import com.opensymphony.xwork2.ActionSupport;
import constant.Role;
//import constant.Status;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.HashMap;
import javax.persistence.EntityManager;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import model.SystemActivityLog;
import model.User;
import org.apache.struts2.ServletActionContext;
import org.apache.struts2.interceptor.ServletRequestAware;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.MiscUtil;
import au.com.bytecode.opencsv.*;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.servlet.ServletContext;
import manager.MilestoneManager;
import manager.ScheduleManager;
import manager.TermManager;
import manager.TimeslotManager;
import model.Milestone;
import model.Schedule;
import model.Term;
import model.Timeslot;
import org.json.JSONObject;

/**
 *
 * @author Tarlochan
 */
public class GenerateScheduleReportAction extends ActionSupport implements ServletRequestAware {

	private HttpServletRequest request;
	private HttpServletResponse response;
	private HashMap<String, Object> json = new HashMap<String, Object>();
	private Logger logger = LoggerFactory.getLogger(UpdateNotificationSettingsAction.class);

	@Override
	public String execute() throws ServletException, IOException {
		HttpSession session = request.getSession();

		Calendar nowCal = Calendar.getInstance();
		Timestamp now = new Timestamp(nowCal.getTimeInMillis());

		SystemActivityLog logItem = new SystemActivityLog();
		logItem.setActivity("Administrator: Update Notification Settings");
		logItem.setRunTime(now);
		logItem.setUser((User) session.getAttribute("user"));
		logItem.setMessage("Error with validation / No changes made");
		logItem.setSuccess(true);

		EntityManager em = null;
		try {
			json.put("exception", false);
			em = MiscUtil.getEntityManagerInstance();

			User user = (User) session.getAttribute("user");

			if (user.getRole().equals(Role.ADMINISTRATOR) || user.getRole().equals(Role.COURSE_COORDINATOR)) {
				
				JSONObject reportData = new JSONObject(request.getParameter("jsonData"));
				String reportNo = reportData.getString("reportNumber");
				
				if (!reportNo.equalsIgnoreCase("1")) {
					json.put("error", true);
					json.put("message", "Wrong report selected. Please try again!");
					return SUCCESS;
				}
				
				long termId = Long.parseLong(reportData.getString("termId"));
				String milestone = reportData.getString("milestoneName");
				
				//termId = 3;
				//milestone = "Final";
				
				boolean termMilestoneMismatch = true;
				//check if this term and milestone belong together
				Term thisTerm = TermManager.findTermById(em, termId);
				List<Milestone> milestones = MilestoneManager.findByTerm(em, thisTerm);
				

				//for every milestone for the term
				for (Milestone m : milestones) {
					if (m.getName().equals(milestone)) {
						termMilestoneMismatch = false;
						break;
					}
				}

				//if term does not contain selected milestone
				if (termMilestoneMismatch) {
					json.put("error", true);
					json.put("message", "Milestone for the term not found!");
					return SUCCESS;
				}

				//else if term and milestone exists
				ServletContext context = ServletActionContext.getServletContext();
				String path = context.getRealPath("/ReportCSV/ScheduleReport.csv");
				//CSVWriter writer = new CSVWriter(new FileWriter(path), ',');

				BufferedWriter out = new BufferedWriter(new FileWriter(path));
				CSVWriter writer = new CSVWriter(out);

				//write the first row (column headers
				String[] firstRow = {"Start Date & Time", "End Date & Time", "Venue", "TA", "Current Booking Status",
					"Required Attendees", "Optional Attendees", "Team", "Team Members", "Last Edited By"};
				writer.writeNext(firstRow);
				
				//get the milestone first
				Milestone milestoneSelected = MilestoneManager.findByNameAndTerm(em,milestone,thisTerm);
				
				//get the schedule from that milestone
				Schedule s = ScheduleManager.findByMilestone(em, milestoneSelected);
				
				//get all slots for this schedule
				Set<Timeslot> allSlots = s.getTimeslots();
				
				//get schedules for this term
				/*List<Schedule> thisSchedules = ScheduleManager.getAllSchedules(em);
				Schedule thisSchedule = new Schedule();

				for (Schedule sch : thisSchedules) {
					if (sch.getMilestone().getName().equals(milestone.toString())) {
						thisSchedule = sch;
						break;
					}
				}
				//get the timeslots for this schedule
				List<Timeslot> allSlots = TimeslotManager.findBySchedule(em, thisSchedule);*/

				//for each timeslot, get the information for each column
				for (Timeslot t : allSlots) {

					String startDate = "";
					startDate = t.getStartTime().toString();
//					String startTime = "";
					String endDate = "";
					endDate = t.getEndTime().toString();
//					String endTime ="";

//					String starting = t.getStartTime().toString();

//					String[] startArray = starting.split("\\s+");
//					
//					startDate = startArray[0].toString();
//					
//					startTime = startArray[1].toString() + " " + startArray[2].toString();

//					String ending = t.getEndTime().toString();
//					
//					String[] endArray = ending.split("\\s+");
//					
//					endDate = endArray[0].toString();
//					
//					endTime = endArray[1].toString() + " " + endArray[2].toString();
					String venue = "-";
					if (t.getVenue() != null) {
						venue = t.getVenue();
					}

					String TA = "-";
					if (t.getTA() != null) {
						TA = t.getTA().getFullName();
						//User userTA = em.find(User.class, t.getId());	
					}

					String currentStatus = "-";
					String requiredAttendees = "-";
					String optionalAttendees = "-";
					String team = "-";
					String teamMembers = "-";
					String lastEdited = "-";

					//if booking is not null
					if (t.getCurrentBooking() != null) {

						//get the current overall status of the booking
						currentStatus = t.getCurrentBooking().getBookingStatus().toString();

						//get the required attendees
						Set<User> allUsers = t.getCurrentBooking().getRequiredAttendees();
						String toAddRequired = "";
						String toAddStudents = "";
						
						int counter = 0;
						
						int facultyCounter = 0;
						
						for (User u: allUsers){

							if (u.getRole() == Role.STUDENT) {
								
								if(counter!=allUsers.size()-1){
									toAddStudents += u.getFullName() + ",";
								}else{
									toAddStudents += u.getFullName();
								}
								
							}else{
								
								facultyCounter++;
								
							}
							
							counter++;
						}
						
						counter = 0;
						
						for (User u: allUsers){
							
							counter++;

							if (u.getRole() == Role.FACULTY) {
								
								if(counter!=facultyCounter){
									toAddRequired += u.getFullName() + ",";
								}else{
									toAddRequired += u.getFullName() + ",";
								}
								
							}
							
							
						}
						
						

						requiredAttendees = toAddRequired;

						if (toAddStudents.length() > 0) {
							teamMembers = toAddStudents;
						}

						//get the optional attendees
						HashSet<String> allOptional = t.getCurrentBooking().getOptionalAttendees();

						String optional = "";
						
						counter = 0;

						if (allOptional != null) {
							for (String eachOptional : allOptional) {
								
								if(counter!=allOptional.size()-1){
									optional += eachOptional + ",";
								}else{
									optional += eachOptional;
								}
								
								counter++;
							}

							if (optional.length() > 0) {
								optionalAttendees = optional;
							}
						}

						//get the team
						team = t.getCurrentBooking().getTeam().getTeamName();
						lastEdited = t.getCurrentBooking().getLastEditedBy();
					}

					String[] eachRow = {startDate, endDate, venue, TA, currentStatus,
						requiredAttendees, optionalAttendees, team, teamMembers, lastEdited};

					writer.writeNext(eachRow);
				}

				writer.close();

				json.put("message", "Report created successfully");
				json.put("success", true);

				//feed in your array (or convert your data to an array)
//				String[] values = {"abc,", "fcb"};
//				
//				writer.writeNext(values);
//				String[] values2  = {"Haha","hehe"};
//				writer.writeNext(values2);
//			    writer.close();

			} else {
				//Incorrect user role
				json.put("error", true);
				json.put("message", "You are not authorized to access this page!");
			}
		} catch (Exception e) {
			logItem.setSuccess(false);
			User userForLog = (User) session.getAttribute("user");
			logItem.setUser(userForLog);
			logItem.setMessage("Error: " + e.getMessage());

			if (MiscUtil.DEV_MODE) {
				for (StackTraceElement s : e.getStackTrace()) {
				}
			}
			json.put("success", false);
			json.put("exception", true);
			json.put("message", "Error with changing notification settings");
		} finally {
			if (em != null) {
				//Saving job log in database
				if (!em.getTransaction().isActive()) {
					em.getTransaction().begin();
				}
				em.persist(logItem);
				em.getTransaction().commit();

				if (em.getTransaction().isActive()) {
					em.getTransaction().rollback();
				}
				if (em.isOpen()) {
					em.close();
				}
			}
		}
		return SUCCESS;
	}

	public HttpServletRequest getRequest() {
		return request;
	}

	public void setRequest(HttpServletRequest request) {
		this.request = request;
	}

	public void setServletRequest(HttpServletRequest hsr) {
		this.request = hsr;
	}

	public HttpServletResponse getResponse() {
		return response;
	}

	public void setResponse(HttpServletResponse response) {
		this.response = response;
	}

	public HashMap<String, Object> getJson() {
		return json;
	}

	public void setJson(HashMap<String, Object> json) {
		this.json = json;
	}
}
