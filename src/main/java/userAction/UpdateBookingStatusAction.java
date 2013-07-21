/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package userAction;

import static com.opensymphony.xwork2.Action.ERROR;
import static com.opensymphony.xwork2.Action.SUCCESS;
import com.opensymphony.xwork2.ActionSupport;
import constant.Status;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import manager.TimeslotManager;
import model.Timeslot;
import model.User;
import org.apache.struts2.interceptor.ServletRequestAware;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.MiscUtil;

/**
 *
 * @author Prakhar
 */
public class UpdateBookingStatusAction extends ActionSupport implements ServletRequestAware{
	private String approveRejectArray[];
	private String approve;
	private String reject;
	private HttpServletRequest request;    
	static final Logger logger = LoggerFactory.getLogger(UpdateBookingStatusAction.class);
	
	@Override
	public String execute() throws Exception {
		EntityManager em = Persistence.createEntityManagerFactory(MiscUtil.PERSISTENCE_UNIT).createEntityManager();
		
		String status = null;
		if(approve != null) {
			  status = "ACCEPTED";
		} else if (reject != null) {
			  status = "REJECTED";
		} else {
			logger.error("No valid response recorded from user");
			return ERROR;
		}
		
		HttpSession session = request.getSession();
		User user = (User) session.getAttribute("user");
		
		//The list of slots to update in db
		List<Timeslot> timeslotsToUpdate = new ArrayList<Timeslot>();
		
		approveRejectArray = request.getParameterValues("approveRejectArray");
		if (approveRejectArray != null && approveRejectArray.length > 0) {
			for (int i = 0; i < approveRejectArray.length; i++) {
				String timeslotId = approveRejectArray[i];
				//Retrieving the timeslot to update
				Timeslot timeslot = TimeslotManager.findById(em, Long.parseLong(timeslotId));
				//Retrieving the status list of the timeslot
				HashMap<User,Status> statusList = timeslot.getStatusList();
				Iterator iter = statusList.keySet().iterator();
				while (iter.hasNext()) {
					if (iter.next() == user) {
						if (status.equalsIgnoreCase("ACCEPTED")) {
							statusList.put(user, Status.ACCEPTED);
						} else if (status.equalsIgnoreCase("REJECTED")) {
							statusList.put(user, Status.REJECTED);
						}
					}
				}
			
				if (statusList.size() > 0) {
					//Setting the new status
					timeslot.setStatusList(statusList);
					timeslotsToUpdate.add(timeslot);
				}
			}
			//Updating the time slot 
			EntityTransaction transaction = em.getTransaction();
			TimeslotManager.updateTimeslotStatus(em, timeslotsToUpdate, transaction);
		}
		return SUCCESS;
	} //end of execute

	
	//Getters and Setters
	
	public String[] getApproveRejectArray() {
		return approveRejectArray;
	}

	public void setApproveRejectArray(String[] approveRejectArray) {
		this.approveRejectArray = approveRejectArray;
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
	
	public void setServletRequest(HttpServletRequest hsr) {
		this.request = hsr;
	}
}