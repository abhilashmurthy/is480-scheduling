/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package systemAction;

import com.opensymphony.xwork2.ActionSupport;
import java.util.ArrayList;
import java.util.List;
import model.Schedule;
import model.Timeslot;
import util.MiscUtil;

/**
 *
 * @author suresh
 */
public class NewBookingAction extends ActionSupport{

	@Override
	public String execute() throws Exception {
		Schedule activeSchedule = MiscUtil.getActiveSchedule();
		timeslots = activeSchedule.getTimeslots();
		return SUCCESS;
	}
	
	private List<Timeslot> timeslots;

	public List<Timeslot> getTimeslots() {
		return timeslots;
	}

	public void setTimeslots(List<Timeslot> timeslots) {
		this.timeslots = timeslots;
	}
	
}
