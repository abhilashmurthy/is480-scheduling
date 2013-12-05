/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package manager;

import constant.BookingStatus;
import constant.PresentationType;
import constant.Response;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.servlet.ServletContext;
import model.Booking;
import model.Team;
import model.Term;
import model.Timeslot;
import model.User;
import model.role.Faculty;
import model.role.Student;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.CustomException;

/**
 *
 * @author suresh
 */
public class TeamManager {
	
	private static Logger logger = LoggerFactory.getLogger(TeamManager.class);
	
	public static HashMap<String, Object> addEditTeam
			(EntityManager em, User doer, String teamName, String wiki, PresentationType presentationType, long termId,
			List<Long> memberIds, long supervisorId, long reviewer1Id, long reviewer2Id, long existingTeamId,
			ServletContext ctx)
			throws Exception
	{
		HashMap<String, Object> json = new HashMap<String, Object>();
		Team team = null;
		
		//Basic validation
		if (teamName == null) {
			throw new CustomException("Please specify the team name!");
		} else {
			teamName = teamName.trim();
			if (teamName.isEmpty()) throw new CustomException("Please specify the team name!");
		}
		if (termId == 0) {
			throw new CustomException("Please specify the term for this team!");
		}
		
		//Retrieveing the team object if this is an EDIT operation
		if (existingTeamId != 0) {
			team = em.find(Team.class, existingTeamId);
			if (team == null) throw new CustomException("Team not found");
		}
		
		//Validating term information
		Term term = em.find(Term.class, termId);
		if (term == null) throw new CustomException("Term not found");
		
		//Validating supervisor information
		Faculty supervisor;
		if (supervisorId != 0) {
			supervisor = em.find(Faculty.class, supervisorId);
			//Checking if the supervisor is found and belongs to the chosen term
			if (supervisor == null || !supervisor.getTerm().equals(term)) {
				throw new CustomException("Specified supervisor not found or not from the current term");
			}
		} else { //No supervisor specified. Fallback to default (Course Coordinator)
			supervisor = UserManager.getFacultyObjForCCForTerm(em, term);
		}
		
		//Validating Reviewer 1 information
		Faculty reviewer1;
		if (reviewer1Id != 0) {
			reviewer1 = em.find(Faculty.class, reviewer1Id);
			//Checking if the reviewer1 is found and belongs to the chosen term
			if (reviewer1 == null || !reviewer1.getTerm().equals(term)) {
				throw new CustomException("Specified Reviewer 1 not found or not from the current term");
			}
		} else { //No reviewer1 specified. Fallback to default (Course Coordinator)
			reviewer1 = UserManager.getFacultyObjForCCForTerm(em, term);
		}
		
		//Validating Reviewer 2 information
		Faculty reviewer2;
		if (reviewer2Id != 0) {
			reviewer2 = em.find(Faculty.class, reviewer2Id);
			//Checking if the reviewer2 is found and belongs to the chosen term
			if (reviewer2 == null || !reviewer2.getTerm().equals(term)) {
				throw new CustomException("Specified Reviewer 2 not found or not from the current term");
			}
		} else { //No reviewer2 specified. Fallback to default (Course Coordinator)
			reviewer2 = UserManager.getFacultyObjForCCForTerm(em, term);
		}
		
		//Checking if the team name exists for the chosen term
		if (teamNameExists(em, teamName, term, team)) {
			throw new CustomException("Team name already exists for the current term");
		}
		
		if (team == null) { //ADD operation
			team = new Team();
		}
		
		//Common steps for ADD and EDIT
		team.setTeamName(teamName);
		team.setWiki(wiki);
		team.setPresentationType(presentationType);
		team.setTerm(term);
		Faculty oldSup = team.getSupervisor();
		team.setSupervisor(supervisor);
		Faculty oldRev1 = team.getReviewer1();
		team.setReviewer1(reviewer1);
		Faculty oldRev2 = team.getReviewer2();
		team.setReviewer2(reviewer2);
		
		//Supervisor setting
		//Checking if the new supervisor is the same as the previous one
		if (oldSup != null && !oldSup.equals(supervisor)) { //New supervisor is different from the previous one
			swapFacultyForTeam(em, doer, oldSup, supervisor, team, "Supervisor", ctx);
		}
		
		//Reviewer1 setting
		//Checking if the new reviewer1 is the same as the previous one
		if (oldRev1 != null && !oldRev1.equals(reviewer1)) { //New reviewer1 is different from the previous one
			swapFacultyForTeam(em, doer, oldRev1, reviewer1, team, "Reviewer1", ctx);
		}
		
		//Reviewer2 setting
		//Checking if the new reviewer2 is the same as the previous one
		if (oldRev2 != null && !oldRev2.equals(reviewer2)) { //New reviewer2 is different from the previous one
			swapFacultyForTeam(em, doer, oldRev2, reviewer2, team, "Reviewer2", ctx);
		}
		
		if (team.getId() == null) { //ADD operation
			em.persist(team);
		}
		
		//Removing student-team relationship from existing members
		for (Student s : team.getMembers()) {
			s.setTeam(null);
		}
		
		//Adding the new members to the team
		for (Long studentId : memberIds) {
			Student stu = em.find(Student.class, studentId);
			if (stu == null || !stu.getTerm().equals(term)) throw new CustomException("Student(s) not found/part of the current term");
			else stu.setTeam(team);
		}
		
		json.put("success", true);
		json.put("teamId", team.getId());
		
		return json;
	}
	
	public static boolean teamNameExists(EntityManager em, String teamName, Term term, Team team) {
		StringBuilder queryString = new StringBuilder("select t from Team t where t.teamName = :teamName AND t.term = :term");
		
		if (team != null) { //Add existing team object in query
			queryString.append(" AND t NOT IN (:team)");
		}
		
		//Checking if the team name already exists for the current term
		Query q = em.createQuery(queryString.toString())
				.setParameter("teamName", teamName)
				.setParameter("term", term);
		if (team != null) { //Adding existing team object in query
			q.setParameter("team", team);
		}
		
		List users = q.getResultList();
		if (users.isEmpty()) {
			return false;
		} else {
			return true;
		}
	}

	public static void deleteTeam(EntityManager em, long teamId) throws Exception {
		Team team = em.find(Team.class, teamId);
		if (team == null) throw new CustomException("Team not found");
		
		//Removing team - student link for all members
		for (Student s : team.getMembers()) {
			s.setTeam(null);
		}
		
		//Removing all bookings related to this team
		ArrayList<Booking> bookings = BookingManager.getBookingsByTeam(em, team);
		for (Booking b : bookings) {
			Timeslot t = b.getTimeslot();
			Booking currentBooking = t.getCurrentBooking();
			if (currentBooking != null && currentBooking.equals(b)) t.setCurrentBooking(null);
			b.setRequiredAttendees(null);
			em.remove(b);
		}
		em.flush(); //Forcing write to DB
		
		//Deleting team
		em.remove(team);
		em.flush();
	}
	
	public static void swapFaculty(EntityManager em, User doer, Faculty oldFac, Faculty newFac, ServletContext ctx) throws Exception {
		Query findTeams = em.createQuery("SELECT t FROM Team t WHERE t.supervisor = :faculty OR t.reviewer1 = :faculty OR t.reviewer2 = :faculty");
		findTeams.setParameter("faculty", oldFac);
		ArrayList<Team> teams = (ArrayList<Team>) findTeams.getResultList();
		
		for (Team t : teams) {
			String role = null;
			if (t.getSupervisor().equals(oldFac)) role = "Supervisor";
			else if (t.getReviewer1().equals(oldFac)) role = "Reviewer1";
			else if (t.getReviewer2().equals(oldFac)) role = "Reviewer2";
			swapFacultyForTeam(em, doer, oldFac, newFac, t, role, ctx);
		}
	}
	
	public static void swapFacultyForTeam(EntityManager em, User doer, Faculty oldFac, Faculty newFac, Team team, String role, ServletContext ctx) throws Exception {
		//Setting the new faculty in the specified role for the team
		Method roleSetter = Team.class.getDeclaredMethod("set" + role, Faculty.class);
		roleSetter.invoke(team, newFac);
		
		//Retrieving all bookings that correspond to this team
		Query existingBookingQuery = em.createQuery("SELECT b FROM Booking b WHERE b.team = :team AND :faculty MEMBER OF b.requiredAttendees");
		existingBookingQuery.setParameter("team", team);
		existingBookingQuery.setParameter("faculty", oldFac);
		
		ArrayList<Booking> existingBookings = (ArrayList<Booking>) existingBookingQuery.getResultList();
		for (Booking b : existingBookings) {
			//Check if we need to act on this booking. We'll see if faculty affected is part of the required attendees for the milestone
			if (!b.getTimeslot().getSchedule().getMilestone().getRequiredAttendees().contains(role)) {
				//Affected faculty member is not part of the required attendees for this booking. Move onto next booking.
				continue;
			}
			if (b.getBookingStatus() == BookingStatus.PENDING 
					|| b.getBookingStatus() == BookingStatus.APPROVED) {
				//Active booking. Delete and recreate at the same spot
				HashMap<String, Object> deleteResult = BookingManager.deleteBooking(em, b.getTimeslot(), "Faculty member changed. Please contact administrator!", doer, ctx);
				if (!deleteResult.containsKey("success") && Boolean.parseBoolean(deleteResult.get("success").toString()) == false) {
					throw new CustomException("Oops. Could not update faculty. Please try again!");
				}
				
				HashMap<String, Object> createResult = BookingManager.createBooking(em, b.getTimeslot(), doer, team, ctx, false);
				if (!createResult.containsKey("success") && Boolean.parseBoolean(createResult.get("success").toString()) == false) {
					throw new CustomException("Oops. Could not update faculty. Please try again!");
				}
			} else {
				//Old booking. Just do the sneaky swap.
				//Replace the old guy with the new guy in the response list (approve/reject HashMap) and required attendees list.
				Set<User> requiredAttendees = b.getRequiredAttendees();
				//Swapping in required attendees list
				requiredAttendees.add(newFac);
				requiredAttendees.remove(oldFac);
				b.setRequiredAttendees(requiredAttendees);

				//Swapping in response list
				HashMap<User, Response> responseList = b.getResponseList();
				Response r = responseList.get(oldFac);
				responseList.remove(oldFac);
				responseList.put(newFac, r);
				b.setResponseList(responseList);	
			}
		}
	}
	
}
