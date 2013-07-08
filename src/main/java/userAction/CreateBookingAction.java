/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package userAction;

import com.opensymphony.xwork2.ActionSupport;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import model.Schedule;
import model.Team;
import model.Term;
import model.Timeslot;
import model.TimeslotStatus;
import model.TimeslotStatusPk;
import model.User;
import model.dao.ScheduleDAO;
import model.dao.TermDAO;
import model.dao.TimeslotDAO;
import model.dao.TimeslotStatusDAO;
import org.apache.struts2.interceptor.ServletRequestAware;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.Milestone;
import util.Status;

/**
 *
 * @author Prakhar
 */
public class CreateBookingAction extends ActionSupport implements ServletRequestAware {

    private String date;
    private String startTime;
    private String endTime;
    private String termId;
    private String milestone;
    private HttpServletRequest request;
    static final Logger logger = LoggerFactory.getLogger(CreateBookingAction.class);

    @Override
    public String execute() throws Exception {
        HttpSession session = request.getSession();

        User user = (User) session.getAttribute("user");
        Team team = user.getTeam();

        // Checking if the user is part of any team
        if (team == null) {
            request.setAttribute("error", "Doesn't look like you're part of any team."
                    + " Can't let you make a booking!");
            logger.error("User's team information not found");
            return ERROR;
        }

        //Sanity check for milestone info
        Milestone enumMilestone;
        if (milestone.equalsIgnoreCase("acceptance")) {
            enumMilestone = Milestone.ACCEPTANCE;
        } else if (milestone.equalsIgnoreCase("midterm")) {
            enumMilestone = Milestone.MIDTERM;
        } else if (milestone.equalsIgnoreCase("final")) {
            enumMilestone = Milestone.FINAL;
        } else {
            request.setAttribute("error", "Oops. Something went wrong on our end. Please try again!");
            logger.error("Milestone not found");
            return ERROR;
        }

        //Retreiving the term
        Term term;
        try {
            int academicYear = Integer.valueOf(termId.split(",")[0]);
            int termNum = Integer.valueOf(termId.split(",")[1]);
            term = TermDAO.findByYearAndTerm(academicYear, termNum);
            if (term == null) {
                throw new Exception();
            }
        } catch (Exception e) {
            request.setAttribute("error", "Oops. Something went wrong on our end. Please try again!");
            logger.error("Term not found");
            return ERROR;
        }


        //Retrieve the corresponding schedule object and its timeslots
        Schedule schedule = ScheduleDAO.findByScheduleId(term.getId(), enumMilestone);
        if (schedule == null || schedule.getTimeslots() == null) {
            request.setAttribute("error", "Oops. Something went wrong on our end. Please try again!");
            logger.error("Schedule not found");
			return ERROR;
        }
        List<Timeslot> timeslots = schedule.getTimeslots();

        //Checking if the team already has a booking (pending/confirmed)
        for (Timeslot t : timeslots) {
            if (t.getTeamId() != null && t.getTeamId().equals(team.getId())) {
                request.setAttribute("error", "Seems like you already have a booking for this milestone."
                        + " Can't let you make a booking!");
                logger.error("Team's already booked a timeslot for the milestone this term");
                return ERROR;
            }
        }

        //Retrieve the corresponding booking slot
        Timestamp bookingTime;
        try {
            String timestampStr = date + " " + startTime;
            bookingTime = Timestamp.valueOf(timestampStr);
        } catch (IllegalArgumentException e) {
            request.setAttribute("error", "Date information not entered correctly. Please try again!");
            logger.error("Start time could not be parsed");
            return ERROR;
        }
        Timeslot bookingSlot = null;
        for (Timeslot t : timeslots) {
            Timestamp tStartTime = t.getId().getStartTime();
            if (tStartTime.equals(bookingTime)) {
                bookingSlot = t;
                break;
            }
        }

        //Check if timeslot has been found
        if (bookingSlot == null) {
            request.setAttribute("error", "We can't find the timeslot you're trying to book."
                    + " Please check the details entered!");
            logger.error("Chosen timeslot not found");
            return ERROR;
        }

        //Check if the timeslot is free
        if (bookingSlot.getTeamId() != null) { //Slot is full
            request.setAttribute("error", "Oops. This timeslot is already taken."
                    + " Please book another slot!");
            logger.error("Chosen timeslot already booked");
            return ERROR;
        }

        //All conditions met. Assign timeslot to team
        bookingSlot.setTeamId(team.getId());
        TimeslotDAO.save(bookingSlot);

        //Create timeslot status entries based on milestone
        Milestone bookingMilestone = bookingSlot.getId().getMilestone();
        List<User> confirmationUsers = new ArrayList<User>();
        if (bookingMilestone.equals(Milestone.ACCEPTANCE)) {
            confirmationUsers.add(team.getSupervisor());
        } else if (bookingMilestone.equals(Milestone.MIDTERM)) {
            confirmationUsers.add(team.getReviewer1());
            confirmationUsers.add(team.getReviewer2());
        } else if (bookingMilestone.equals(Milestone.FINAL)) {
            confirmationUsers.add(team.getSupervisor());
            confirmationUsers.add(team.getReviewer1());
        } else {
            request.setAttribute("error", "Oops. Something went wrong on our end. Please try again!");
            logger.error("FATAL ERROR: Code not to be reached!");
            return ERROR;
        }

        for (User u : confirmationUsers) {
            TimeslotStatusPk statusPk = new TimeslotStatusPk(
                    bookingSlot.getId().getTermId(),
                    bookingSlot.getId().getMilestone(),
                    bookingSlot.getId().getStartTime(),
                    u.getId());
            TimeslotStatus status = new TimeslotStatus();
            status.setId(statusPk);
            status.setStatus(Status.PENDING);
            TimeslotStatusDAO.save(status);
        }

        return SUCCESS;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public String getTermId() {
        return termId;
    }

    public void setTermId(String termId) {
        this.termId = termId;
    }

    public String getMilestone() {
        return milestone;
    }

    public void setMilestone(String milestone) {
        this.milestone = milestone;
    }

    public void setServletRequest(HttpServletRequest hsr) {
        request = hsr;
    }
}
