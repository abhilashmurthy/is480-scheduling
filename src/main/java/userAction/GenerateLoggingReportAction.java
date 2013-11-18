/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package userAction;

import static com.opensymphony.xwork2.Action.SUCCESS;
import com.opensymphony.xwork2.ActionSupport;
import constant.Role;
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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.ServletContext;
import manager.SystemActivityLogManager;
import org.json.JSONObject;

/**
 *
 * @author Prakhar
 */
public class GenerateLoggingReportAction extends ActionSupport implements ServletRequestAware {

	private HttpServletRequest request;
	private HttpServletResponse response;
	private HashMap<String, Object> json = new HashMap<String, Object>();
	private Logger logger = LoggerFactory.getLogger(GenerateLoggingReportAction.class);

	@Override
	public String execute() throws ServletException, IOException {
		HttpSession session = request.getSession();

		Calendar nowCal = Calendar.getInstance();
		Timestamp now = new Timestamp(nowCal.getTimeInMillis());

		SystemActivityLog logItem = new SystemActivityLog();
		logItem.setActivity("Administrator: Generate Logging Report");
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
				
				if (!reportNo.equalsIgnoreCase("3")) {
					json.put("error", true);
					json.put("message", "Wrong report selected. Please try again!");
					return SUCCESS;
				}
				
				String sDate = reportData.getString("startDate");
				String eDate = reportData.getString("endDate");
				
				if (sDate == null && eDate != null) {
					json.put("error", true);
					json.put("message", "Start Time not selected for report!");
					return SUCCESS;
				}
				
				if (sDate != null && eDate == null) {
					json.put("error", true);
					json.put("message", "End Time not selected for report!");
					return SUCCESS;
				}
				
				SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
				List<SystemActivityLog> allActivity = new ArrayList<SystemActivityLog>();
				//------------Getting data - Login/Logout, Administrator, Booking-----------------
				if ((sDate.equals("") || sDate == null) && (eDate.equals("") || eDate == null)) {
					allActivity = SystemActivityLogManager.getAllLogs(em);
				} else {
					sDate = sDate + " 00:00:00";
					eDate = eDate + " 23:59:59";
					Timestamp startDate = new Timestamp(sdf.parse(sDate).getTime());
					Timestamp endDate = new Timestamp(sdf.parse(eDate).getTime());
					allActivity = SystemActivityLogManager.getAllLogsBetween(em, startDate, endDate);
				}
				
				//Sorting the log activity according to the 3 categories - Login/Logout, Administrator, Booking
				ArrayList<SystemActivityLog> loginActivity = new ArrayList<SystemActivityLog>();
				ArrayList<SystemActivityLog> logoutActivity = new ArrayList<SystemActivityLog>();
				ArrayList<SystemActivityLog> administratorActivity = new ArrayList<SystemActivityLog>();
				ArrayList<SystemActivityLog> bookingActivity = new ArrayList<SystemActivityLog>();
				
				for (SystemActivityLog log: allActivity) {
					if (log.getActivity().startsWith("Login")) { 
						loginActivity.add(log);
					} else if (log.getActivity().startsWith("Logout")) {
						logoutActivity.add(log);
					} else if (log.getActivity().startsWith("Administrator")) {
						administratorActivity.add(log);
					} else if (log.getActivity().startsWith("Booking")) {
						bookingActivity.add(log);
					}
				}
				
				//-------------Writing the log activity to csv file - 'Logging Report'--------------
				ServletContext context = ServletActionContext.getServletContext();
				String path = context.getRealPath("/ReportCSV/LoggingReport.csv");

				BufferedWriter out = new BufferedWriter(new FileWriter(path));
				CSVWriter writer = new CSVWriter(out);
				
				//write the first row (column headers)
				String[] firstRow = {"Id", "Log Activity", "Activity Message", "Date and Time", "User", "Success"};
				writer.writeNext(firstRow);
				
				String[] blankLine = new String[6];
				
				//1st: Booking Activity
				if (bookingActivity.size() > 0) {
					for (SystemActivityLog log: bookingActivity) {
						//Prepare data and write to file
						String id = String.valueOf(log.getId());
						String activity = log.getActivity();
						String message = "";
						if (log.getMessage() != null) {
							message = log.getMessage();
						}
						String time = sdf.format(log.getRunTime());
						String username = "";
						if (log.getUser() != null) {
							username = log.getUser().getFullName();
						}
						String result = "";
						if (log.isSuccess() == true) {
							result = "SUCCESS";
						} else {
							result = "ERROR";
						}
						//Write to file
						String[] nextLine = {id, activity, message, time, username, result}; 
						writer.writeNext(nextLine);
					}
					//Print blank line
					writer.writeNext(blankLine);
				}
				
				//2nd: Administrator Activity
				if (administratorActivity.size() > 0) {
					for (SystemActivityLog log: administratorActivity) {
						//Prepare data and write to file
						String id = String.valueOf(log.getId());
						String activity = log.getActivity();
						String message = "";
						if (log.getMessage() != null) {
							message = log.getMessage();
						}
						String time = sdf.format(log.getRunTime());
						String username = "";
						if (log.getUser() != null) {
							username = log.getUser().getFullName();
						}
						String result = "";
						if (log.isSuccess() == true) {
							result = "SUCCESS";
						} else {
							result = "ERROR";
						}
						//Write to file
						String[] nextLine = {id, activity, message, time, username, result}; 
						writer.writeNext(nextLine);
					}
					//Print blank line
					writer.writeNext(blankLine);
				}
				
				//3rd: Login/Logout Activity
				if (loginActivity.size() > 0) {
					for (SystemActivityLog log: loginActivity) {
						//Prepare data and write to file
						String id = String.valueOf(log.getId());
						String activity = log.getActivity();
						String message = "";
						if (log.getMessage() != null) {
							message = log.getMessage();
						}
						String time = sdf.format(log.getRunTime());
						String username = "";
						if (log.getUser() != null) {
							username = log.getUser().getFullName();
						}
						String result = "";
						if (log.isSuccess() == true) {
							result = "SUCCESS";
						} else {
							result = "ERROR";
						}
						//Write to file
						String[] nextLine = {id, activity, message, time, username, result}; 
						writer.writeNext(nextLine);
					}
				}
				if (logoutActivity.size() > 0) {
					for (SystemActivityLog log: logoutActivity) {
						//Prepare data and write to file
						String id = String.valueOf(log.getId());
						String activity = log.getActivity();
						String message = "";
						if (log.getMessage() != null) {
							message = log.getMessage();
						}
						String time = sdf.format(log.getRunTime());
						String username = "";
						if (log.getUser() != null) {
							username = log.getUser().getFullName();
						}
						String result = "";
						if (log.isSuccess() == true) {
							result = "SUCCESS";
						} else {
							result = "ERROR";
						}
						//Write to file
						String[] nextLine = {id, activity, message, time, username, result}; 
						writer.writeNext(nextLine);
					}
				}
				
				writer.close();
				logItem.setMessage("Logging Report was created successfully between" + sDate + " and " + eDate);
				
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
			json.put("message", "Error generating report for logging activity");
			
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
