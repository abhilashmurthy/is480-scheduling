/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package userAction;

import systemAction.*;
import com.opensymphony.xwork2.ActionSupport;
import constant.Status;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.persistence.EntityManager;
import javax.persistence.Persistence;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import manager.TimeslotManager;
import model.Timeslot;
import model.User;
import org.apache.struts2.interceptor.ServletRequestAware;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.MiscUtil;

/**
 *
 * @author Tarlochan
 */
public class UpdateBookingAction extends ActionSupport implements ServletRequestAware {

    private HttpServletRequest request;
    private static Logger logger = LoggerFactory.getLogger(UpdateBookingAction.class);
    private HashMap<String, Object> json = new HashMap<String, Object>();
    private String timeslotId;
    private String changedDate;

    @Override
    public String execute() throws ServletException, IOException {
        try {
            //Code here
            //convert the chosen ID into long and get the corresponding Timeslot object
            EntityManager em = Persistence.createEntityManagerFactory(MiscUtil.PERSISTENCE_UNIT).createEntityManager();
            long chosenID = Long.parseLong(timeslotId);
            Timeslot ts = TimeslotManager.findById(em, chosenID);
            //Timeslot oldSlot = (Timeslot)ts.clone();

            //get list of users
            //List<User> allUsers = UserManager.getAllUsers(em);

            //get both the old list of attendees and new list in the same order
            /*String[] newAttendees = newList.split(",");
             List<String> newAttendeesList = Arrays.asList(newAttendees);
             String[] oldAttendees = oldList.split(",");
             List<String> oldAttendeesList = Arrays.asList(oldAttendees);*/

            //get all availabe timeslots
            List<Timeslot> allSlots = TimeslotManager.getAllTimeslots(em);

            //boolean anyUpdateToAttendees = false;

            /*if(newAttendeesList.size()>0){

             for(int i=0;i<newAttendeesList.size();i++){
                    
             String newName = (String)newAttendeesList.get(i);                      
                    
             if(newName!=null){
             if(!oldAttendeesList.contains(newName.toString())){

             //check to see if new user exists in db
             for(User everyUser: allUsers){

             //if new user exists
             if(everyUser.getFullName().equalsIgnoreCase(newName.toString())){
             oldAttendeesList.set(i, everyUser.getFullName());
             anyUpdateToAttendees = true;
             }

             }

             }
             }

             }

             }*/

            Timestamp newbookingTime = null;
            boolean proceed = false;
            
            //see if the edited start date and time is in the correct format
            try {
                newbookingTime = Timestamp.valueOf(changedDate);
                proceed = true;
            } catch (Exception e) {
                json.put("success", false);
                json.put("message", "Start Date and Time in the wrong format!");
                return SUCCESS;
            }

            //if date is empty and updatetoattendee fails
            /*if(!anyUpdateToAttendees && !(newbookingTime instanceof Timestamp)){
             json.put("success", false);
             json.put("message", "No changes made to date/time or attendees. Please try again!");
             return SUCCESS;
                
             }*/

            //update timeslot and change it based on attendees
            /*if(anyUpdateToAttendees){
                
             String push ="";
             String namet = "";
                
             Set<User> pastAttendee = ts.getAttendees();
             Set<User> presentAttendees = new HashSet<User>();
             HashMap<User, Status> presentList = new HashMap<User, Status>();
                
             //set all attendees and statuslist to null
             em.getTransaction().begin();
             ts.setStatusList(presentList);
             ts.setAttendees(presentAttendees);
             em.persist(ts);
                
             em.getTransaction().commit();
                
             //set users from this timeslot to no team
             for(User u: allUsers){
                    
             if(u.getTeam()==ts.getTeam()){
             em.getTransaction().begin();
                        
             u.setTeam(null);
             em.persist(u);
                        
             em.getTransaction().commit();
             }
                    
             }
                
             //this adds the attendees and sets status list to the latest
             //supervisor or/and reviewer
             for(int i=0;i<oldAttendeesList.size();i++){
             for(User eachU: pastAttendee){
             if(oldAttendeesList.get(i).equalsIgnoreCase(eachU.getFullName())){
                            
             boolean isStudent=false;
                            
             for(int x=0;x<eachU.getRoles().size();x++){
                                
             Role r = eachU.getRoles().get(x);
             if(r.getName().equalsIgnoreCase("Student")){
                                    
             isStudent = true;
                                    
             }
                                
             }
                            
             //set the student to this team
             if(isStudent){
             eachU.setTeam(ts.getTeam());
             em.getTransaction().begin();
             em.persist(eachU);
             em.getTransaction().commit();
             //push = eachU.getTeam().getTeamName();
             //namet = eachU.getFullName();
             //json.put("message", push + namet + "here");
             //return SUCCESS;
             }
             //if user has no team, then it is a sup/reviewer
             //add it to presentList
             else{
             em.getTransaction().begin();
             em.persist(eachU);
             em.getTransaction().commit();
             //presentList.put(eachU, Status.PENDING);
             }
                            
             presentAttendees.add(eachU);
             }
             }
             }
                
             //update timeslot based on status list and attendees
             /*em.getTransaction().begin();
                
             ts.setTeam(ts.getTeam());
             ts.setAttendees(presentAttendees);
             ts.setStatusList(presentList);
                   
             em.persist(ts);
             em.getTransaction().commit();    */

            //json.put("message", "Booking updated successfully! Update email has been sent to all attendees. (Coming soon..)" + push + namet);

            //}

            //update timeslot and change it based on date
            if (proceed) {
                try {
                    //go through timeslot to compare start time with the user
                    //suggested start time and see if the timeslot is taken.
                    //if the timeslot is not assigned to any team, update timeslot to
                    //the new timeslot
                    boolean successUpdate = false;

                    for (Timeslot toCompare : allSlots) {

                        if (toCompare.getStartTime().equals(newbookingTime) && toCompare.getTeam() == null) {

                            em.getTransaction().begin();
                            toCompare.setAttendees(ts.getAttendees());
                            toCompare.setStatusList(ts.getStatusList());
                            toCompare.setTeam(ts.getTeam());

                            HashMap<User, Status> statusList = new HashMap<User, Status>();
                            Set<User> attendees = new HashSet<User>();

                            ts.setAttendees(attendees);
                            ts.setTeam(null);
                            ts.setStatusList(statusList);

                            em.persist(toCompare);
                            em.persist(ts);
                            em.getTransaction().commit();

                            successUpdate = true;

                            break;
                        }
                    }

                    if (!successUpdate) {
                        json.put("success", false);
                        json.put("message", "Error saving new date and time");
                        return SUCCESS;

                    } else {
                        json.put("message", "Booking updated successfully! Update email has been sent to all attendees. (Coming soon..)");

                    }

                } catch (Exception e) {
                    logger.error("Start time could not be parsed");
                    json.put("success", false);
                    json.put("message", "Error occured. Please try again!");
                    return SUCCESS;
                }
            }


        } catch (Exception e) {
            logger.error("Exception caught: " + e.getMessage());
            if (MiscUtil.DEV_MODE) {
                for (StackTraceElement s : e.getStackTrace()) {
                    logger.debug(s.toString());
                }
            }
            request.setAttribute("error", "Error with UpdateBooking: Escalate to developers!");
            return ERROR;
        }
        return SUCCESS;
    }

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

    public String getTimeslotId() {
        return timeslotId;
    }

    public void setTimeslotId(String timeslotId) {
        this.timeslotId = timeslotId;
    }

    public String getChangedDate() {
        return changedDate;
    }

    public void setChangedDate(String changedDate) {
        this.changedDate = changedDate;
    }

}
