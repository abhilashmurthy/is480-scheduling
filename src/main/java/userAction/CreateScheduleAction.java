/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package userAction;

import static com.opensymphony.xwork2.Action.ERROR;
import static com.opensymphony.xwork2.Action.SUCCESS;
import com.opensymphony.xwork2.ActionSupport;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import model.User;
import org.apache.struts2.interceptor.ServletRequestAware;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static userAction.ResponseAction.logger;

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
	private HttpServletRequest request;
	static final Logger logger = LoggerFactory.getLogger(CreateBookingAction.class);

	@Override
	public String execute() throws Exception {
		HttpSession session = request.getSession();
		//User user = (User) session.getAttribute("user");
	
		//Checking whether any dates have been entered
		if (acceptanceStartDate == null && acceptanceEndDate == null &&
				midtermStartDate == null && midtermEndDate == null &&
				finalStartDate == null && finalEndDate == null) {
			request.setAttribute("error", "Incorrect inputs. Please enter the Start Date and End Date for atleast 1 milestone!");
            logger.error("Start Date & End Date not entered for any milestone.");
            return ERROR;
		} 
		
		if ((acceptanceStartDate != null && acceptanceEndDate == null) || 
				(acceptanceStartDate == null && acceptanceEndDate != null)) {
			request.setAttribute("error", "Incorrect Inputs. Please enter Start Date/End Date for Acceptance!");
            logger.error("Start Date or End Date not entered for Acceptance.");
            return ERROR;
		}
		
		if ((midtermStartDate != null && midtermEndDate == null) || 
				(midtermStartDate == null && midtermEndDate != null)) {
			request.setAttribute("error", "Incorrect Inputs. Please enter Start Date/End Date for Midterm!");
            logger.error("Start Date or End Date not entered for Midterm.");
            return ERROR;
		}
		
		if ((finalStartDate != null && finalEndDate == null) || 
				(finalStartDate == null && finalEndDate != null)) {
			request.setAttribute("error", "Incorrect Inputs. Please enter Start Date/End Date for Final!");
            logger.error("Start Date or End Date not entered for Final.");
            return ERROR;
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
}
