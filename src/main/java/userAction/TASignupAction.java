/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package userAction;

import com.opensymphony.xwork2.ActionSupport;
import constant.Role;
import java.io.IOException;
import java.util.HashMap;
import javax.persistence.EntityManager;
import javax.persistence.Persistence;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import manager.TimeslotManager;
import model.Timeslot;
import model.User;
import model.role.TA;
import org.apache.struts2.interceptor.ServletRequestAware;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import util.MiscUtil;

/**
 *
 * @author Tarlochan
 */
public class TASignupAction extends ActionSupport implements ServletRequestAware {
   private HttpServletRequest request;
   private HashMap<String, Object> json = new HashMap<String, Object>();
   
    public void setServletRequest(HttpServletRequest hsr) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String execute() throws ServletException, IOException, JSONException {
        
        EntityManager em = null;
        
        try{
            
            HttpSession session = request.getSession();
            em = Persistence.createEntityManagerFactory(MiscUtil.PERSISTENCE_UNIT).createEntityManager();
            
            //get current user object
            em.clear();
            User oldUser = (User) session.getAttribute("user");
            User user = em.find(User.class, oldUser.getId());
            
            //check if the user is ta
            Role activeRole = (Role) session.getAttribute("activeRole");
            
            if(!(activeRole.equals(Role.TA))){
                
                json.put("success", false);
                json.put("message", "You are not a TA");
                return SUCCESS;
                
            }
            
            //set TA object
            TA ta = em.find(TA.class, user.getId());
     
            //get a list of timeslots in array format
            JSONArray inputData = new JSONArray(request.getParameter("jsonData"));

            //loop through the list to get timeslotID
            for(int i=0;i<inputData.length();i++){
                
                //convert each ID to long
                JSONObject obj = inputData.getJSONObject(i);
                long timeslotId = obj.getLong("id");
                
                //get the respective timeslot object
                Timeslot t = TimeslotManager.findById(em, timeslotId);
                
                //if timeslot doesn't exists or if TA already signed up
                if (t == null) {
                    json.put("success", false);
                    json.put("message", "Timeslot with ID: " + timeslotId + " already taken");
                    return SUCCESS;
                }
                
                //else proceed to add this timeslot for that respective TA
                //set this timeslot's TA
                em.getTransaction().begin();
             
                t.setTA(ta);
              
                em.persist(t);

                em.getTransaction().commit();
            }
            
            //succesfully updated
            json.put("success",true);
            json.put("message", "You have succesfully signed up for all slots!");

        }catch(Exception e){
            json.put("success", false);
            json.put("exception", true);
            json.put("message", "Error with Delete Booking: Escalate to developers!");
        }finally {
            if (em != null && em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            if (em != null && em.isOpen()) {
                em.close();
            }
	}
        
        return SUCCESS; //To change body of generated methods, choose Tools | Templates.
    }
    
    public HttpServletRequest getRequest() {
        
        return request;
    }

    public void setRequest(HttpServletRequest request) {
        this.request = request;
    }
    
    public HashMap<String, Object> getJson() {
        return json;
    }

    public void setJson(HashMap<String, Object> json) {
        this.json = json;
    }
    
    
}
