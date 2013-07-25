/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package userAction;

import com.opensymphony.xwork2.ActionSupport;
import constant.Status;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import javax.persistence.EntityManager;
import javax.persistence.Persistence;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import manager.TimeslotManager;
import model.Team;
import model.Timeslot;
import model.User;
import org.apache.struts2.interceptor.ServletRequestAware;
import util.MiscUtil;
/**
 *
 * @author Tarlochan
 */
public class ViewBookingAction extends ActionSupport implements ServletRequestAware{

    private HttpServletRequest request;
    private String timeSlotID;
    private String longMsg;
    private HashMap<String, Object> json = new HashMap<String, Object>();
    
    
    public void setServletRequest(HttpServletRequest hsr) {
        request = hsr;
    }
    
    
    public HttpServletRequest getRequest() {
        return request;
    }
    
    public void setRequest(HttpServletRequest request) {
        this.request = request;
    }
    
    @Override
    public String execute() throws Exception {
        //convert the chosen ID into long
        long chosenID = Long.parseLong(timeSlotID);
        EntityManager em = Persistence.createEntityManagerFactory(MiscUtil.PERSISTENCE_UNIT).createEntityManager();
        Timeslot ts = TimeslotManager.findById(em,chosenID);
       
        if(ts!=null){
            Timestamp st = ts.getStartTime();
            Timestamp et = ts.getEndTime();
            Date date = new Date(st.getTime());
            Date date2 = new Date(et.getTime());
            
            //long dt = date.getTime();
            //put date into format
            SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, dd MMM yyyy");
            
            //get team's ID from timeslot
            
            String teamName = "-";
           
            
            if(ts.getTeam()!=null){
                Team team = ts.getTeam();
                teamName = team.getTeamName().toString();
            }
            
            HashMap<User,Status> attendees = null;
            
            //get every user
            String allUserNames = "-";
            if(ts.getStatusList() !=null){
               attendees = ts.getStatusList();
               Iterator it = attendees.entrySet().iterator();
               allUserNames = "";
               while (it.hasNext()) {
                    Map.Entry pairs = (Map.Entry)it.next();
                    User u = (User)pairs.getKey();
                    Status status = (Status)pairs.getValue();
                    //it.remove(); // avoids a ConcurrentModificationException
                    
                    if (!it.hasNext()) {
                        // last iteration
                        allUserNames += u.getFullName()+ "( " + status + " )";
                    }else{    
                        allUserNames += u.getFullName() + "( " + status + " ), ";
                    }
                    
               }
               
              //allUserNames = Integer.toString(attendees.size());
                
            }
            
            //get venue
            String venue = "-";
            venue = ts.getVenue();
            
            //Things this code cannot get as of now (can only do this when database has values)
            String nonCompulsoryAttendees = "-";
            String TA = "-";
            String teamWiki = "-";
            //Ends here
            
            String dateSlot = dateFormat.format(date);
            
            SimpleDateFormat dateFormat2 = new SimpleDateFormat("HH:mm");
            String startTimeString = dateFormat2.format(date);
            String endTimeString = dateFormat2.format(date2);
            json.put("success",true);
            longMsg = "Team Name: " + teamName + ", Date: " + dateSlot + ", Start Time: " + startTimeString
                    + ", End Time: " + endTimeString + ", Venue: " + venue + ", Compulsory Attendees: " + allUserNames
                        + ", Non-compulsory Attendees: " + nonCompulsoryAttendees + ", TA: " + TA + ", Team Wiki: " + teamWiki;
            json.put("message",longMsg);
            /*json.put("Date", dateSlot);
            json.put("Start Time", startTimeString);
            json.put("End Time", endTimeString);
            json.put("Venue", venue);
            json.put("Compulsory Attendees", attendees.toString());
            json.put("Non-compulsory Attendees", nonCompulsoryAttendees);
            json.put("TA",TA);
            json.put("Team Wiki", teamWiki);*/
            //em.getTransaction().commit();
            //request.setAttribute("details",longMsg);
            
            return SUCCESS;
        }
        
         json.put("error",true);
         json.put("message","there are no bookings!");
         //em.getTransaction().commit();
         return SUCCESS;
        
    }

    public HashMap<String, Object> getJson() {
		return json;
    }

    public void setJson(HashMap<String, Object> json) {
            this.json = json;
    }
    
    public String gettimeSlotID(){
        return timeSlotID;
    }
    
    public void setTimeSlotID(String newTimeSlotID){
        timeSlotID = newTimeSlotID;
    }
    
    public String getLongMsg(){
        return longMsg;
    }
    
    public void setLongMsg(String newMsg){
        longMsg = newMsg;
    }
}