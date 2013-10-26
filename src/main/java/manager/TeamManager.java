/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package manager;

import java.util.HashMap;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import model.Team;
import model.Term;
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
			(EntityManager em, String teamName, String wiki, long termId,
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
		
		Term term = em.find(Term.class, termId);
		if (term == null) throw new CustomException("Term not found");
		
		if (teamNameExists(em, teamName, term, team)) {
			throw new CustomException("Team name already exists for the current term");
		}
		
		if (team == null) { //ADD operation
			
		} else { //EDIT operation
			
		}
		
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
	
}
