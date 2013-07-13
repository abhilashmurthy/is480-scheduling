// This class is for both accepting/rejecting a booking

package userAction;

import com.opensymphony.xwork2.ActionSupport;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Tarlochan
 */

public class ResponseAction extends ActionSupport{
    private int termId;
    private int userId;
    private String approve;
    private String reject;
    private List<String> message = new ArrayList();
	private String teamName;
	private int teamIdInt;
    //private String response;
    
    static final Logger logger = LoggerFactory.getLogger(ResponseAction.class);
    
    @Override
    public String execute() throws Exception {
 
        //get TimeSlotStatus based on term and user

//        List<TimeslotStatus> ts = TimeslotStatusDAO.findTimeSlotStatusByTermAndUser(termId, userId);
//
//        for(TimeslotStatus t: ts){
//
//            Timeslot timeslotDetails = TimeslotDAO.findByDate(t.getId().getStartTime());
//            
//            //message.add("Team ID: " + timeslotDetails.getTeamId().toString() + " , TimeSlot: " + timeslotDetails.getId().getStartTime());
//			//Retrieving Team Id and Team Name and displaying it
//            if(t.getStatus().toString().equals("PENDING")){
//                BigInteger teamId = timeslotDetails.getTeamId();
//				teamIdInt = teamId.intValue();
//				Team team = TeamDAO.findByTeamId(teamIdInt);
//				teamName = team.getTeamName();
//				//message.add(teamId.toString());
//				message.add(teamName);
//            }
//        }
//        setMessage(message);
        return SUCCESS;
    }

	  public int getTeamIdInt() {
			return teamIdInt;
	  }

	  public void setTeamIdInt(int teamIdInt) {
			this.teamIdInt = teamIdInt;
	  }
    
      public List<String> getMessage() {
            return message;
      }

      public void setMessage(List<String> message) {
            this.message = message;
       }
      
      public int getUserId(){
          return userId;
      }
      
      public int getTermId(){
          return termId;
      }
      
      public void setUserId(int userId){
          this.userId = userId;
      }
      
      public void setTermId(int termId){
          this.termId = termId;
      }
      
      public String getApprove(){
          return approve;
      }
      
      public String getReject(){
          return reject;
      }
      
      public void setApprove(String approve){
          this.approve = approve;
      }
      
      public void setReject(String reject){
          this.reject = reject;
      }
      
      public void updateSlot(int teamID){
          
          //
          
      }
 
    
}
