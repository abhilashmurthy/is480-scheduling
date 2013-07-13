/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package userAction;

import static com.opensymphony.xwork2.Action.ERROR;
import static com.opensymphony.xwork2.Action.SUCCESS;
import com.opensymphony.xwork2.ActionSupport;
import model.dao.TimeslotStatusDAO;
import static userAction.ResponseAction.logger;

/**
 *
 * @author Prakhar
 */
public class UpdateBookingStatusAction extends ActionSupport{
	private String teamId;
	private String approve;
	private String reject;
	
	@Override
	public String execute() throws Exception {
		//To update the database with the approve/reject status
		 String status = null;
		 if(approve != null) {
               status = "ACCEPTED";
         } else if (reject != null) {
               status = "REJECTED";
		 } else {
			 logger.error("No valid response recorded from user");
			 return ERROR;
		 }
		 //Updating the time slot status of the respective team
         TimeslotStatusDAO.updateTimeSlotStatusByTeamId(Integer.parseInt(teamId), status);
		 
		 return SUCCESS;
	} //end of execute

	//Getters and Setters
	public String getTeamId() {
		return teamId;
	}

	public void setTeamId(String teamId) {
		this.teamId = teamId;
	}

	public String getApprove() {
		return approve;
	}

	public void setApprove(String approve) {
		this.approve = approve;
	}

	public String getReject() {
		return reject;
	}

	public void setReject(String reject) {
		this.reject = reject;
	}
}