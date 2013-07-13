/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package userAction;

import static com.opensymphony.xwork2.Action.ERROR;
import com.opensymphony.xwork2.ActionSupport;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import model.Schedule;
import model.Team;
import model.Term;
import model.Timeslot;
import model.User;
import org.apache.struts2.interceptor.ServletRequestAware;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import constant.Status;
import java.util.HashMap;
import java.util.Set;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import manager.MilestoneManager;
import manager.ScheduleManager;
import manager.TermManager;
import model.Milestone;
import static userAction.CreateBookingAction.logger;

/**
 *
 * @author Prakhar
 */
public class CreateTermAction extends ActionSupport implements ServletRequestAware{
	private int year;
	private int semester;
	private HttpServletRequest request;
	static final Logger logger = LoggerFactory.getLogger(CreateBookingAction.class);
	
	@Override
	public String execute() throws Exception {
		//To check if this term already exists
		
		Term existingTerm = TermManager.findByYearAndSemester(year, semester);
		if (existingTerm != null) {
			request.setAttribute("error", "This term has already been created. Please try again!");
			logger.error("Term already created");
			return ERROR;
		}
		
		EntityTransaction transaction = null;
		Term term = new Term();
		term.setAcademicYear(year);
		term.setSemester(semester);
		boolean result = TermManager.save(term, transaction);
		if (result == false) {
			request.setAttribute("error", "Oops. Something went wrong on our end. Please try again later.");
			logger.error("Error while creating new term");
			return ERROR;
		} 
		return SUCCESS;
	}

	public int getYear() {
		return year;
	}

	public void setYear(int year) {
		this.year = year;
	}

	public int getSemester() {
		return semester;
	}

	public void setSemester(int semester) {
		this.semester = semester;
	}

	public void setServletRequest(HttpServletRequest hsr) {
		this.request = hsr;
	}
}
