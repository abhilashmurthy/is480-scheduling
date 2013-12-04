/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package userAction;

import com.google.gson.Gson;
import static com.opensymphony.xwork2.Action.SUCCESS;
import com.opensymphony.xwork2.ActionSupport;
import constant.Role;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import org.apache.struts2.interceptor.ServletRequestAware;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.HashMap;
import javax.persistence.EntityManager;
import manager.SettingsManager;
import model.Settings;
import model.SystemActivityLog;
import model.User;
import org.json.JSONArray;
import org.json.JSONObject;
import util.MiscUtil;

/**
 *
 * @author Prakhar
 */
public class UpdateMilestoneSettingsAction extends ActionSupport implements ServletRequestAware {

    private HashMap<String, Object> json = new HashMap<String, Object>();
	private ArrayList<HashMap<String, Object>> data = new ArrayList<HashMap<String, Object>>();
    private HttpServletRequest request;
    private static Logger logger = LoggerFactory.getLogger(UpdateMilestoneSettingsAction.class);
	
	@Override
    public String execute() {
		HttpSession session = request.getSession();
		
		Calendar nowCal = Calendar.getInstance();
		Timestamp now = new Timestamp(nowCal.getTimeInMillis());
		SystemActivityLog logItem = new SystemActivityLog();
		
		logItem.setActivity("Administrator: Update Milestone Settings");
		logItem.setRunTime(now);
		logItem.setUser((User)session.getAttribute("user"));
		logItem.setMessage("Error with validation / No changes made");
		logItem.setSuccess(true);
		
		EntityManager em = null;
        try {
			json.put("exception", false);
            em = MiscUtil.getEntityManagerInstance();
			
			//Checking whether the active user is admin/course coordinator or not
			User user = (User) session.getAttribute("user");
			if (user.getRole().equals(Role.ADMINISTRATOR) || user.getRole().equals(Role.COURSE_COORDINATOR)) {
			
				ArrayList<HashMap<String, Object>> milestonesList = new ArrayList<HashMap<String, Object>>();
				//Getting input data from url
				JSONArray inputArray = (JSONArray) new JSONArray(request.getParameter("jsonData"));

				boolean error = false;
				double[] orderList = new double[inputArray.length()];

				for (int i = 0; i < inputArray.length(); i++) {
					JSONObject milestone = inputArray.getJSONObject(i);
					HashMap<String, Object> map = new HashMap<String, Object>();
					//Storing the values in a hashmap
					String order = milestone.getString("newOrderNumber");
					orderList[i] = Double.valueOf(order);
					map.put("order", Double.valueOf(order));

					String milestoneName = milestone.getString("newMilestoneName");
					map.put("milestone", milestoneName);

					String duration = milestone.getString("newDuration");
					map.put("duration", Double.valueOf(duration));

					String newAttendees = milestone.getString("newAttendees").replaceAll(", ", ",");
					//Removing the comma from the end
					String[] attendees = newAttendees.split(",");
					map.put("attendees", attendees);

					//If more than 3 attendees for a milestone return error
					if (attendees.length > 3) {
						json.put("message", "Error! More than 3 attendees for a milestone!");
						json.put("success", false);
						return SUCCESS;
					}
					//Checking whether any 2 attendees are same 
					for (int j = 0; j < attendees.length; j++) {
						for (int k = j + 1; k < attendees.length; k++) {
							if (attendees[k].equalsIgnoreCase(attendees[j])) { 
								error = true;
								break;
							}
						}
					}
					if (error == true) {
						json.put("message", "Error! Required attendees are same for a milestone!");
						json.put("success", false);
						return SUCCESS;
					}

					milestonesList.add(map);
				} //end of for loop
				
				//Check whether milestone names exists
				for (int i = 0; i < milestonesList.size(); i++) {
					HashMap<String, Object> startMilestoneMap = milestonesList.get(i);
					String milestoneName = (String) startMilestoneMap.get("milestone");
					if (milestoneName.trim().equals("")) {
						json.put("message", "Milestones must have names!");
						json.put("success", false);
						return SUCCESS;
					}
				}

				//Check whether the milestone names are the same or not
				for (int i = 0; i < milestonesList.size() - 1; i++) {
					HashMap<String, Object> startMilestoneMap = milestonesList.get(i);
					String startMilestoneName = (String) startMilestoneMap.get("milestone");
					for (int j = i + 1; j < milestonesList.size(); j++) {
						HashMap<String, Object> nextMilestoneMap = milestonesList.get(j);
						String nextMilestoneName = (String) nextMilestoneMap.get("milestone");
						if (startMilestoneName.equals(nextMilestoneName)) {
							json.put("message", "Error! Milestone names cannot be same!");
							json.put("success", false);
							return SUCCESS;
						}
					}
				}
				
				
				//Checking for error whether any 2 order numbers are the same 
				if (orderList != null) {
					for (int j = 0; j < orderList.length; j++) {
						for (int k = j + 1; k < orderList.length; k++) {
							if (orderList[k] == orderList[j]) { // or use .equals()
								error = true;
								break;
							}
						}
					}
				}
				if (error == true) {
					json.put("message", "Error! Order numbers are same!");
					json.put("success", false);
					return SUCCESS;
				}
				
				//Checking whether order numbers are 0 or less than 0
				if (orderList != null) {
					for (int j = 0; j < orderList.length; j++) {
						if (orderList[j] < 1) { // or use .equals()
							error = true;
							break;
						}
					}
				}
				if (error == true) {
					json.put("message", "Error! Incorrect order number!");
					json.put("success", false);
					return SUCCESS;
				}
				//-------------Validation Checks completed----------------
				
				//Sorting the orders array in ascending order of orders
				if (orderList != null) {
					double temp = 0;
					for (int i = 0; i < orderList.length; i++) { 
						for (int j = 0; j < orderList.length - 1; j++) {
							if (orderList[j+1] < orderList[j]) {
								temp = orderList[j+1];
								orderList[j+1] = orderList[j];
								orderList[j] = temp;  
							}
						}
					}
				}
				//Sorting the hashmaps according to sorted order numbers
				ArrayList<HashMap<String, Object>> sortedMilestonesList = new ArrayList<HashMap<String, Object>>();
				if (orderList != null) {
					for (int i = 0; i < orderList.length; i++) {
						for (HashMap<String, Object> map : milestonesList) {
							double unsortedOrder = (Double) map.get("order");
							if (orderList[i] == unsortedOrder) {
								sortedMilestonesList.add(map);
							}
						}
					}
				}
				
				Settings currentSettings = SettingsManager.getByName(em, "milestones");
				//Storing the milestones list in settings table in db
				em.getTransaction().begin();
				currentSettings.setValue(new Gson().toJson(sortedMilestonesList));
				em.persist(currentSettings);
				em.getTransaction().commit();
				
				json.put("success", true);
				json.put("message", "Your settings have been updated!");
				MiscUtil.logActivity(logger, user, "Milestone settings updated");
				
				logItem.setMessage("Milestone settings were updated successfully");
			} else {
				//Incorrect user role
				json.put("error", true);
				json.put("message", "You are not authorized to access this functionality!");
			}
		} catch (Exception e) {
			logItem.setSuccess(false);
			User userForLog = (User) session.getAttribute("user");
			logItem.setUser(userForLog);
			logItem.setMessage("Error: " + e.getMessage());
			
			logger.error("Exception caught: " + e.getMessage());
            if (MiscUtil.DEV_MODE) {
                for (StackTraceElement s : e.getStackTrace()) {
                    logger.debug(s.toString());
                }
            }
            json.put("success", false);
			json.put("exception", true);
            json.put("message", "Error with UpdateMilestoneSettings: Escalate to developers!");
        } finally {
			if (em != null) {
				if (em.getTransaction().isActive()) em.getTransaction().rollback();
                //Saving job log in database
                if (!em.getTransaction().isActive()) em.getTransaction().begin();
                em.persist(logItem);
                em.getTransaction().commit();
                if (em.isOpen()) em.close();
			}
		}
        return SUCCESS;
	 } //end of execute
	 
	public HttpServletRequest getRequest() {
        return request;
    }

    public void setRequest(HttpServletRequest request) {
        this.request = request;
    }
	
	public void setServletRequest(HttpServletRequest hsr) {
        this.request = hsr;
    }

	public HashMap<String, Object> getJson() {
		return json;
	}

	public void setJson(HashMap<String, Object> json) {
		this.json = json;
	}

	public ArrayList<HashMap<String, Object>> getData() {
		return data;
	}

	public void setData(ArrayList<HashMap<String, Object>> data) {
		this.data = data;
	}
}  // end of class
