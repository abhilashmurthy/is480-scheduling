/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package manager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import model.Booking;
import model.Team;
import model.Term;
import model.Timeslot;
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
			(EntityManager em, String teamName, String wiki, long termId, List<Long> memberIds,
			long supervisorId, long reviewer1Id, long reviewer2Id, long existingTeamId)
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
		team.setTerm(term);
		
		//Supervisor setting
		//Checking if the new supervisor is the same as the previous one
		if (team.getSupervisor() != null && !team.getSupervisor().equals(supervisor)) { //New supervisor is different from the previous one
			//TODO REFRESH PENDING BOOKINGS!
		}
		team.setSupervisor(supervisor); //Setting the new guy as the supervisor
		
		//Reviewer1 setting
		//Checking if the new reviewer1 is the same as the previous one
		if (team.getReviewer1() != null && !team.getReviewer1().equals(reviewer1)) { //New reviewer1 is different from the previous one
			//TODO REFRESH PENDING BOOKINGS!
		}
		team.setReviewer1(reviewer1); //Setting the new guy as reviewer1
		
		//Reviewer2 setting
		//Checking if the new reviewer2 is the same as the previous one
		if (team.getReviewer2() != null && !team.getReviewer2().equals(reviewer2)) { //New reviewer2 is different from the previous one
			//TODO REFRESH PENDING BOOKINGS!
		}
		team.setReviewer2(reviewer2); //Setting the new guy as reviewer2
		
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
			b.setSubscribedUsers(null);
			em.remove(b);
		}
		em.flush(); //Forcing write to DB
		
		//Deleting team
		em.remove(team);
		em.flush();
	}
	
}
