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
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.servlet.ServletContext;
import manager.MilestoneManager;
import manager.ScheduleManager;
import manager.TermManager;
import model.Booking;
import model.Milestone;
import model.Schedule;
import model.Team;
import model.Term;
import model.Timeslot;
import org.json.JSONObject;

/**
 *
 * @author Tarlochan
 */
public class GenerateWikiReportAction extends ActionSupport implements ServletRequestAware {

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

				if (!reportNo.equalsIgnoreCase("2")) {
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
				String path = context.getRealPath("/ReportCSV/WikiReport.txt");
				//CSVWriter writer = new CSVWriter(new FileWriter(path), ',');

				BufferedWriter out = new BufferedWriter(new FileWriter(path));

				//get the milestone first
				Milestone milestoneSelected = MilestoneManager.findByNameAndTerm(em, milestone, thisTerm);

				//get the schedule from that milestone
				Schedule s = ScheduleManager.findByMilestone(em, milestoneSelected);

				//get all slots for this schedule
				Set<Timeslot> allSlots = s.getTimeslots();

				//arraylist to compare days
				ArrayList<Timeslot> timeArr = new ArrayList<Timeslot>();

				//for each timeslot, get the information for each column
				for (Timeslot t : allSlots) {

					timeArr.add(t);

				}


				//Dates that have already been added
				ArrayList<String> isAdded = new ArrayList<String>();

				//new array sorted
				ArrayList<Timeslot> sortTimes = new ArrayList<Timeslot>();

				//loop through list
				for (int i = 0; i < timeArr.size(); i++) {

					Timeslot t = timeArr.get(i);
					String date = t.getStartTime().toString();
					String startDate = date.substring(0, 10);

					boolean foundFirst = false;

					//see if startDate already exists in the added list
					for (String eachString : isAdded) {

						if (eachString.equals(startDate)) {

							foundFirst = true;

						}

					}

					//if not found add this to the sorted list
					if (!foundFirst) {

						sortTimes.add(t);

					}

					if (i != timeArr.size() - 1) {

						for (int j = i + 1; j < timeArr.size(); j++) {

							Timeslot tCompare = timeArr.get(j);
							String dateCompare = tCompare.getStartTime().toString();
							String startDateCompare = dateCompare.substring(0, 10);

							//compare the first timeslot with the next few to see if same starting date	
							if (startDateCompare.equals(startDate)) {

								boolean found = false;

								//see if startDate already exists in the added list
								for (String eachString : isAdded) {

									if (eachString.equals(startDateCompare)) {

										found = true;

									}

								}

								//if not found add this to the sorted list
								if (!found) {

									sortTimes.add(tCompare);

								}

							}

						}

						//add this slot to the added slot, so that will never add again
						isAdded.add(startDate);

					}

				}


				//to see if already added
				ArrayList<String> alreadyAdded = new ArrayList<String>();


				for (int i = 0; i < sortTimes.size(); i++) {

					Timeslot t = sortTimes.get(i);

					String startDateTime = "";
					startDateTime = t.getStartTime().toString();
					String startDate = startDateTime.substring(0, 10);
					int day = t.getStartTime().getDay();

					boolean added = false;

					for (String st : alreadyAdded) {

						if (st.equals(startDate)) {

							added = true;

						}
					}

					//when added is false
					if (!added) {

						//get day
						String dayString = "";

						if (day == 0) {
							dayString = "Sunday";
						} else if (day == 1) {
							dayString = "Monday";
						} else if (day == 2) {
							dayString = "Tuesday";
						} else if (day == 3) {
							dayString = "Wednesday";
						} else if (day == 4) {
							dayString = "Thursday";
						} else if (day == 5) {
							dayString = "Friday";
						} else if (day == 6) {
							dayString = "Saturday";
						}

						//if i is not the last one
						if (i != sortTimes.size()) {

							//count the number of occurences
							int overAllCounter = 0;

							for (int j = i; j < sortTimes.size(); j++) {

								//get all the similar timeslots and add them
								Timeslot tCompare = sortTimes.get(j);
								String startDateTimeCompare = "";
								startDateTimeCompare = tCompare.getStartTime().toString();
								String startDateCompare = startDateTimeCompare.substring(0, 10);

								Booking book = tCompare.getCurrentBooking();

								if (startDateCompare.equals(startDate) && book != null) {

									overAllCounter++;

								}

							}

							int compareAll = 0;

							boolean printHeader = true;

							for (int j = i; j < sortTimes.size(); j++) {

								//get all the similar timeslots and add them
								Timeslot tCompare = sortTimes.get(j);
								String startDateTimeCompare = "";
								startDateTimeCompare = tCompare.getStartTime().toString();
								String startDateCompare = startDateTimeCompare.substring(0, 10);
								String startTimeEach = startDateTimeCompare.substring(11, 13);

								if (startDateCompare.equals(startDate)) {

									//if there is a booking
									Booking book = tCompare.getCurrentBooking();

									if (book != null) {

										//print header
										if (printHeader) {

											//start new table header
											out.write("==" + startDate + " " + dayString + "==");
											out.newLine();
											out.write("{| border=\"1\" cellpadding=\"1\"");
											out.newLine();
											out.write("|- style=\"background:blue; color:white\"");
											out.newLine();
											out.write("|width=\"150pt\"|Time");
											out.newLine();
											out.write("||Team");
											out.newLine();
											out.write("||Faculty");
											out.newLine();
											out.write("||Venue");
											out.newLine();
											out.write("||Sponsor Presence");
											out.newLine();
											out.write("||Comments");
											out.newLine();
											out.newLine();

											printHeader = false;

										}

										//print out stuff for this section
										out.write("|-");
										out.newLine();
										out.write("|width=\"160pt\"|" + startTimeEach + ":00hrs");
										out.newLine();


										Team team = tCompare.getCurrentBooking().getTeam();

										if (team != null) {
											String teamName = team.getTeamName();
											teamName = "||" + teamName;
											out.write(teamName);
											out.newLine();
										} else {

											out.write("||");
											out.newLine();

										}

										Set<User> allUsers = tCompare.getCurrentBooking().getRequiredAttendees();

										int counter = 0;

										String attendees = "";

										if (allUsers.size() > 0) {
											for (User u : allUsers) {

												if (u.getRole() == Role.FACULTY) {

													if (counter == 0) {
														attendees += u.getFullName();
														counter++;
													} else {
														attendees += " & " + u.getFullName();
													}
												}

											}
										}

										out.write("||" + attendees);
										out.newLine();

										//venue
										String venue = "";

										venue = tCompare.getVenue().toString();

										if (venue == null) {
											venue = "";
										}

										out.write("||" + venue);
										out.newLine();

										//sponsor presence
										out.write("|| NA");
										out.newLine();

										//comments

										//get TA first
										String TA = "NA";

										try {
											TA = tCompare.getTA().getFullName();
										} catch (Exception e) {
										}
										
										if (TA == null) {
											TA = "NA";
										}

										//get status of presentation
										String pt = null;
										
										try {
											pt = tCompare.getCurrentBooking().getTeam().getPresentationType().name();
										} catch (Exception e) {
											
										}
										
										if (pt == null) {
											pt = "NA";
										}


										out.write("||" + pt + " / " + TA);
										out.newLine();

										compareAll++;

										//to close or not
										if (overAllCounter == compareAll) {
											out.write("|}");
										} else {
											out.write("|-");
										}

										out.newLine();
									}
								}

							}

						}

						//add this string to already added
						alreadyAdded.add(startDate);
					}

					out.newLine();

				}

				out.close();

				json.put("message", "Report created successfully");
				json.put("success", true);


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
			json.put("message", "Error with wiki report");
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
