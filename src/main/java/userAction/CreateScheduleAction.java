/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package userAction;

import static com.opensymphony.xwork2.Action.ERROR;
import static com.opensymphony.xwork2.Action.SUCCESS;
import com.opensymphony.xwork2.ActionSupport;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.persistence.EntityTransaction;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import manager.MilestoneManager;
import manager.ScheduleManager;
import manager.TermManager;
import model.Milestone;
import model.Schedule;
import model.Term;
import model.User;
import org.apache.struts2.interceptor.ServletRequestAware;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Prakhar
 */
public class CreateScheduleAction extends ActionSupport implements ServletRequestAware{
    private String termId;  //To get the term id
	private String acceptanceStartDate;
	private String acceptanceEndDate;
	private String midtermStartDate;
	private String midtermEndDate;
	private String finalStartDate;
	private String finalEndDate;
	private String slotDuration;
	private String scheduleId;
	private List<HashMap<String, String>> dataList;
	private HttpServletRequest request;
	
	static final Logger logger = LoggerFactory.getLogger(CreateBookingAction.class);

	@Override
	public String execute() throws Exception {
		HttpSession session = request.getSession();
		//User user = (User) session.getAttribute("user");
		
		//Checking whether any dates have been entered
		if (acceptanceStartDate.equals("") && acceptanceEndDate.equals("") &&
				midtermStartDate.equals("") && midtermEndDate.equals("") &&
				finalStartDate.equals("") && finalEndDate.equals("")) {
			request.setAttribute("error", "Incorrect inputs. Please enter the Start Date and End Date for atleast 1 milestone!");
            logger.error("Start Date & End Date not entered for any milestone.");
            return ERROR;
		} 
		
		//Checking whether the dates entered are date format
		ArrayList<String> datesList = new ArrayList<String>();
		datesList.add(acceptanceStartDate);
		datesList.add(acceptanceEndDate);
		datesList.add(midtermStartDate);
		datesList.add(midtermEndDate);
		datesList.add(finalStartDate);
		datesList.add(finalEndDate);
		for (String dateToCheck: datesList) {
			if (!"".equals(dateToCheck)) {
				try {
					String timestampToCheck = dateToCheck + " 00:00:00";
					Timestamp checkTime = Timestamp.valueOf(timestampToCheck);
				} catch (Exception e) {
					request.setAttribute("error", "Please enter a valid date (" + (dateToCheck) + ")!");
					logger.error("User entered invalid date.");
					return ERROR;
				}
			}
		}
		
		//Checking whether both start and end date have been entered for Acceptance
		if ((!acceptanceStartDate.equals("") && acceptanceEndDate.equals("")) || 
				(acceptanceStartDate.equals("") && !acceptanceEndDate.equals(""))) {
			request.setAttribute("error", "Incorrect Inputs. Please enter Start Date/End Date for Acceptance!");
            logger.error("Start Date or End Date not entered for Acceptance.");
            return ERROR;
		}
		
		//Checking whether both start and end date have been entered for Midterm
		if ((!midtermStartDate.equals("") && midtermEndDate.equals("")) || 
				(midtermStartDate.equals("") && !midtermEndDate.equals(""))) {
			request.setAttribute("error", "Incorrect Inputs. Please enter Start Date/End Date for Midterm!");
            logger.error("Start Date or End Date not entered for Midterm.");
            return ERROR;
		}
		
		//Checking whether both start and end date have been entered for Final
		if ((!finalStartDate.equals("") && finalEndDate.equals("")) || 
				(finalStartDate.equals("") && !finalEndDate.equals(""))) {
			request.setAttribute("error", "Incorrect Inputs. Please enter Start Date/End Date for Final!");
            logger.error("Start Date or End Date not entered for Final.");
            return ERROR;
		}

		List<Schedule> scheduleList = new ArrayList<Schedule>();
		
		// For acceptance milestone
		if (!acceptanceStartDate.equals("") && !acceptanceEndDate.equals("")) {
			Timestamp acceptanceStartTimestamp;
			Timestamp acceptanceEndTimestamp;
			Milestone acceptanceMil;
			Term acceptanceTerm;
			
			acceptanceStartTimestamp = Timestamp.valueOf(acceptanceStartDate + " " + "00:00:00");
			acceptanceEndTimestamp = Timestamp.valueOf(acceptanceEndDate + " " + "00:00:00");
			//Getting the milestone object
			acceptanceMil = MilestoneManager.findByName("Acceptance");
			//Getting the term object
			acceptanceTerm = TermManager.findTermById(Long.parseLong(termId));
			//Creating schedule object
			Schedule acceptanceSched = new Schedule();
			acceptanceSched.setStartDate(acceptanceStartTimestamp);
			acceptanceSched.setEndDate(acceptanceEndTimestamp);
			acceptanceSched.setMilestone(acceptanceMil);
			acceptanceSched.setTerm(acceptanceTerm);
			
			scheduleList.add(acceptanceSched);
		}
		
		// For midterm milestone
		if (!midtermStartDate.equals("") && !midtermEndDate.equals("")) {
			Timestamp midtermStartTimestamp;
			Timestamp midtermEndTimestamp;
			Milestone midtermMil;
			Term midtermTerm;
			
			midtermStartTimestamp = Timestamp.valueOf(midtermStartDate + " " + "00:00:00");
			midtermEndTimestamp = Timestamp.valueOf(midtermEndDate + " " + "00:00:00");
			//Getting the milestone object
			midtermMil = MilestoneManager.findByName("Midterm");
			//Getting the term object
			midtermTerm = TermManager.findTermById(Long.parseLong(termId));
			//Creating schedule object
			Schedule midtermSched = new Schedule();
			midtermSched.setStartDate(midtermStartTimestamp);
			midtermSched.setEndDate(midtermEndTimestamp);
			midtermSched.setMilestone(midtermMil);
			midtermSched.setTerm(midtermTerm);
			
			scheduleList.add(midtermSched);
		}
		
		// For final milestone
		if (!finalStartDate.equals("") && !finalEndDate.equals("")) {
			Timestamp finalStartTimestamp;
			Timestamp finalEndTimestamp;
			Milestone finalMil;
			Term finalTerm;
			
			finalStartTimestamp = Timestamp.valueOf(finalStartDate + " " + "00:00:00");
			finalEndTimestamp = Timestamp.valueOf(finalEndDate + " " + "00:00:00");
			//Getting the milestone object
			finalMil = MilestoneManager.findByName("Final");
			//Getting the term object
			finalTerm = TermManager.findTermById(Long.parseLong(termId));
			//Creating schedule object
			Schedule finalSched = new Schedule();
			finalSched.setStartDate(finalStartTimestamp);
			finalSched.setEndDate(finalEndTimestamp);
			finalSched.setMilestone(finalMil);
			finalSched.setTerm(finalTerm);
			
			scheduleList.add(finalSched);
		}
		
		if (scheduleList.size() > 0) {
			//Checking whether the schedule already exists in the database or not
			List<Schedule> sourceList = ScheduleManager.getAllSchedules();
			for (Schedule oldSchedule: sourceList) {
				long oldMilestoneId = oldSchedule.getMilestone().getId();
				long oldTermId = oldSchedule.getTerm().getId();
				for (Schedule newSchedule: scheduleList) {
					long newMilestoneId = newSchedule.getMilestone().getId();
					long newTermId = newSchedule.getTerm().getId();
					String milestoneName = newSchedule.getMilestone().getName();
					if (oldMilestoneId == newMilestoneId && oldTermId == newTermId) {
						request.setAttribute("error", "The schedule for " + milestoneName + " already exists!");
						logger.error("Error while creating new schedule for " + milestoneName);
						return ERROR;
					}
				}
			}
			// Saving the schedules in the database
			EntityTransaction transaction = null;
			boolean result = ScheduleManager.save(scheduleList, transaction);
			if (result == false) {
				request.setAttribute("error", "Oops. Something went wrong on our end. Please try again later.");
				logger.error("Error while creating new schedule");
				return ERROR;
			}
		}
		
		//Creating a hashmap list to display the created schedule on the confirmation page
		if (scheduleList.size() > 0) {
			dataList = new ArrayList<HashMap<String, String>>();
			HashMap<String, String> map = null;
			for (Schedule schedule: scheduleList) {
				map = new HashMap<String, String>();
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
				String milestoneName = schedule.getMilestone().getName();
				String startDt = sdf.format(schedule.getStartDate()).toString();
				String endDt = sdf.format(schedule.getEndDate()).toString();
				map.put("milestoneName", milestoneName);
				map.put("startDate", startDt);
				map.put("endDate", endDt);
				map.put("slotDuration", Integer.toString(schedule.getMilestone().getSlotDuration()));
				map.put("scheduleId", schedule.getId().toString());
				
				dataList.add(map);
			}
		}
		return SUCCESS;
	}

	public String getTermId() {
		return termId;
	}

	public void setTermId(String termId) {
		this.termId = termId;
	}

	public String getAcceptanceStartDate() {
		return acceptanceStartDate;
	}

	public void setAcceptanceStartDate(String acceptanceStartDate) {
		this.acceptanceStartDate = acceptanceStartDate;
	}

	public String getAcceptanceEndDate() {
		return acceptanceEndDate;
	}

	public void setAcceptanceEndDate(String acceptanceEndDate) {
		this.acceptanceEndDate = acceptanceEndDate;
	}

	public String getMidtermStartDate() {
		return midtermStartDate;
	}

	public void setMidtermStartDate(String midtermStartDate) {
		this.midtermStartDate = midtermStartDate;
	}

	public String getMidtermEndDate() {
		return midtermEndDate;
	}

	public void setMidtermEndDate(String midtermEndDate) {
		this.midtermEndDate = midtermEndDate;
	}

	public String getFinalStartDate() {
		return finalStartDate;
	}

	public void setFinalStartDate(String finalStartDate) {
		this.finalStartDate = finalStartDate;
	}

	public String getFinalEndDate() {
		return finalEndDate;
	}

	public void setFinalEndDate(String finalEndDate) {
		this.finalEndDate = finalEndDate;
	}

	public void setServletRequest(HttpServletRequest hsr) {
		request = hsr;
	}

	public String getSlotDuration() {
		return slotDuration;
	}

	public void setSlotDuration(String slotDuration) {
		this.slotDuration = slotDuration;
	}

	public String getScheduleId() {
		return scheduleId;
	}

	public void setScheduleId(String scheduleId) {
		this.scheduleId = scheduleId;
	}

	public List<HashMap<String, String>> getDataList() {
		return dataList;
	}

	public void setDataList(List<HashMap<String, String>> dataList) {
		this.dataList = dataList;
	}
}
