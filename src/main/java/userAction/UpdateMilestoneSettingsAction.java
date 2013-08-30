/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package userAction;

import com.google.gson.Gson;
import static com.opensymphony.xwork2.Action.SUCCESS;
import com.opensymphony.xwork2.ActionSupport;
import constant.Role;
import java.util.ArrayList;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import org.apache.struts2.interceptor.ServletRequestAware;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.HashMap;
import javax.persistence.EntityManager;
import javax.persistence.Persistence;
import manager.SettingsManager;
import model.Settings;
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
		EntityManager em = null;
        try {
			json.put("exception", false);
            em = Persistence.createEntityManagerFactory(MiscUtil.PERSISTENCE_UNIT).createEntityManager();
			HttpSession session = request.getSession();
			
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

					String newAttendees = milestone.getString("newAttendees");
					//Removing the comma from the end
					String[] attendees = newAttendees.substring(0, newAttendees.length() - 1).split(",");
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
				json.put("message", "Milestone Settings Updated!");

			} else {
				//Incorrect user role
				json.put("error", true);
				json.put("message", "You are not authorized to access this functionality!");
			}
		} catch (Exception e) {
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
			if (em != null && em.getTransaction().isActive()) em.getTransaction().rollback();
			if (em != null && em.isOpen()) em.close();
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
