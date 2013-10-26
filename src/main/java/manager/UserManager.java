/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package manager;

import constant.Role;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.servlet.http.HttpSession;
import model.Team;
import model.Term;
import model.User;
import model.role.Faculty;
import model.role.Student;
import model.role.TA;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.CustomException;
import util.MiscUtil;

/**
 *
 * @author suresh
 */
public class UserManager {

    private static Logger logger = LoggerFactory.getLogger(UserManager.class);
	
	//Adding all the roles in the list in decreasing order of importance/power
	private static void populateAllRoles(ArrayList<Role> allRoles) {
		allRoles.add(Role.ADMINISTRATOR);
		allRoles.add(Role.COURSE_COORDINATOR);
		allRoles.add(Role.FACULTY);
		allRoles.add(Role.STUDENT);
		allRoles.add(Role.TA);
		allRoles.add(Role.GUEST);
	}
	
	//Choosing the user object with the least important/powerful role
	private static User chooseRole(ArrayList<User> users) {
		ArrayList<Role> allRoles = new ArrayList<Role>(); populateAllRoles(allRoles);
		User user = users.get(0);
		int smallestRoleIndex = allRoles.indexOf(user.getRole());
		
		Iterator<User> iter = users.iterator();
		while (iter.hasNext()) {
			User u = iter.next();
			if (allRoles.indexOf(u.getRole()) > smallestRoleIndex) { //Current object's role is the least important until now
				user = u;
				smallestRoleIndex = allRoles.indexOf(u.getRole());
			}
		}
		
		return user;
	}
	
	/**
	 * Sets the default user object and role based on the current active term in the system
	 */
	public static void initializeUser(EntityManager em, HttpSession session, String username, String fullName, Term activeTerm) {
		ArrayList<User> users = findActiveRolesByUsername (em, username, activeTerm);

		if (users.isEmpty()) {
			User tempUser = new User(username, fullName, null, Role.GUEST, activeTerm);
			session.setAttribute("user", tempUser);
			session.setAttribute("activeRole", tempUser.getRole());
		} else {
			User chosenRole;
			User currentUser = (User) session.getAttribute("user");
			//Checking if the existing user is an Admin or Course Coordinator. Prevening automatic selection of role then. (Not applicable during first login)
			if (currentUser != null && (currentUser.getRole() == Role.ADMINISTRATOR || currentUser.getRole() == Role.COURSE_COORDINATOR)) {
				chosenRole = currentUser;
			} else {
				chosenRole = chooseRole(users); //Choosing the default role to begin with
				session.setAttribute("user", getUser(chosenRole));
				session.setAttribute("activeRole", chosenRole.getRole());
			}
			users.remove(chosenRole); //Removing the chosen object from the list of users
		}

		session.setAttribute("userRoles", users);
	}
	
	public static <T> ArrayList<T> findActiveByRoleAndUsername(EntityManager em, Class<T> type, String username) {
		ArrayList<Term> activeTerms = SettingsManager.getActiveTerms(em);
		ArrayList<T> list;
		try {
			Query q = em.createQuery("SELECT x FROM " + type.getSimpleName() + " x WHERE x.username = :username AND x.term.id IN (:termIds)");
			q.setParameter("username", username);
			ArrayList<Long> termIds = new ArrayList<Long>();
			for (Term t : activeTerms) { termIds.add(t.getId()); }
			q.setParameter("termIds", termIds);
			list = (ArrayList<T>) q.getResultList();
		} catch (Exception e) {
			logger.error("Error in findActiveByRoleAndUsername()");
			logger.error(e.getMessage());
			return null;
		}
		return list;
	}

    public static void save(EntityManager em, User user) {
        logger.trace("Saving user: " + user.getFullName());
        try {
            em.getTransaction().begin();
            em.persist(user);
            em.getTransaction().commit();
        } catch (Exception e) {
            logger.error("Database Operation Error");
            em.getTransaction().rollback();
        }
    }

    /**
     * Method to find all the active roles for a user. Includes roles for the
     * current active term and permanent roles.
     *
     * @param em
     * @param username
     * @param activeTerm
     * @return List of User objects
     */
    public static ArrayList<User> findActiveRolesByUsername(EntityManager em, String username, Term activeTerm) {
        logger.trace("Finding active roles for: " + username);
        ArrayList<User> users = new ArrayList<User>();
        try {
            em.getTransaction().begin();
            Query q = em.createQuery("select o from User o where o.username = :username and (term.id = :termId or term is null)");
            q.setParameter("username", username);
            q.setParameter("termId", activeTerm.getId());
            users = (ArrayList<User>) q.getResultList();
            em.getTransaction().commit();
        } catch (Exception e) {
            logger.error("Database Operation Error");
            em.getTransaction().rollback();
        }
        return users;
    }

    /**
     * Method to get a unique user object based on the role and term.
     *
     * @param em
     * @param role
     * @param term
     * @param username
     * @return Single User object satisfying all criteria
     */
    public static User findByRoleTermUsername(EntityManager em, Role role, Term term, String username) {
        logger.trace("Getting specific user object");
        User user = null;
        try {
            Query q = em.createQuery("select o from User o where role = :role and term = :term and username = :username");
            q.setParameter("role", role);
            q.setParameter("term", term);
            q.setParameter("username", username);
            user = (User) q.getSingleResult();
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
        return user;
    }

    public static List<User> getAllUsers(EntityManager em) {
        logger.trace("Getting all users");
        List<User> result = null;
        try {
            em.getTransaction().begin();
            Query q = em.createQuery("select o from User o");
            result = q.getResultList();
            em.getTransaction().commit();
        } catch (Exception e) {
            logger.error("Database Operation Error");
            em.getTransaction().rollback();
        }
        return result;
    }

    public static User getCourseCoordinator(EntityManager em) {
        logger.trace("Getting Course Coordinator");
		Query q = em.createQuery("select o from User o where o.role = :role", User.class);
		q.setParameter("role", Role.COURSE_COORDINATOR);
		User cc;
		try { cc = (User) q.getSingleResult(); }
		catch (NoResultException e) { cc = null; }
		return cc;
    }
	
	public static <T extends User> T getUser(User user) {
		return (T) getUser(user.getId(), user.getRole().getBaseClassType());
	}

    public static <T extends User> T getUser(long id, Class<T> type) {
        logger.trace("Getting User: " + id + ", by class: " + type);
        T user = null;
		EntityManager em = null;
        try {
			em = MiscUtil.getEntityManagerInstance();
            em.getTransaction().begin();
            user = (T) em.find(type, id);
            em.getTransaction().commit();
        } catch (Exception e) {
            logger.error("Database Operation Error");
            em.getTransaction().rollback();
        } finally {
            if (em != null && em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            if (em != null && em.isOpen()) {
                em.close();
            }
        }
        return user;
    }
	
    public static <T extends User> List<T> findByTermAndRole(EntityManager em, Term term, Role role) {
        logger.trace("Getting Users for term: " + (term != null?term.getId():"none") + ", by role: " + role.toString());
        List<T> users = null;
		boolean justHere = false;
        try {
			if (!em.getTransaction().isActive()) {
				justHere = true;
				em.getTransaction().begin();
			}
			if (role.equals(Role.ADMINISTRATOR) || role.equals(Role.COURSE_COORDINATOR)) {
				Query q = em.createQuery("select o from User o where role = :role")
						.setParameter("role", role);
				users = q.getResultList();
			} else {
				Query q = em.createQuery("select o from User o where role = :role and term = :term")
						.setParameter("role", role)
						.setParameter("term", term);
				users = q.getResultList();
			}
            if (justHere) em.getTransaction().commit();
        } catch (Exception e) {
            logger.error("Database Operation Error");
            em.getTransaction().rollback();
        }
        return users;
    }
	
    public static List<Team> getTeamsByTerm(EntityManager em, Term term) {
        logger.trace("Getting teams for term: " + term.getId());
        List<Team> teams = null;
		boolean justHere = false;
        try {
			if (!em.getTransaction().isActive()) {
				justHere = true;
				em.getTransaction().begin();
			}
            Query q = em.createQuery("select o from Team o where term = :term")
					.setParameter("term", term);
			teams = q.getResultList();
            if (justHere) em.getTransaction().commit();
        } catch (Exception e) {
            logger.error("Database Operation Error");
            em.getTransaction().rollback();
        }
        return teams;
    }
	
	public static boolean usernameExists(EntityManager em, String username, Role role, Term term, User user) {
		StringBuilder queryString = new StringBuilder("select u from User u where u.username = :username AND u.role = :role");
		
		if (term == null) { //NULL check in query
			queryString.append(" AND u.term IS NULL");
		} else { //Actual term object in query
			queryString.append(" AND u.term = :term");
		}
		
		if (user != null) { //Add existing user object in query
			queryString.append(" AND u NOT IN (:user)");
		}
		//Checking if the username already exists for the current term
		Query q = em.createQuery(queryString.toString())
				.setParameter("username", username)
				.setParameter("role", role);
		
		if (term != null) { //Injecting term into query if not NULL
			q.setParameter("term", term);
		}
		
		if (user != null) { //Adding existing user object in query
			q.setParameter("user", user);
		}
		
		List users = q.getResultList();
		if (users.isEmpty()) {
			return false;
		} else {
			return true;
		}
	}
	
	/**
	 * Method to add a new user or edit an existing one in the database. This method can be used for all roles in the system.
	 * @param em
	 * @param role
	 * @param termId
	 * @param username
	 * @param fullName
	 * @param teamId Optional information.
	 * @param existingUserId User ID of an existing object to edit
	 * @return HashMap with two keys. "success" --> true/false, "message" --> User ID of newly created object or a descriptive error message
	 * @throws Exception Throws CustomException with a descriptive error message in case of validation errors. To be handled by the caller.
	 */
	public static HashMap<String, Object> addEditUser
			(EntityManager em, Role role, long termId,
			String username, String fullName, long teamId, long existingUserId)
			throws Exception
	{
		HashMap<String, Object> json = new HashMap<String, Object>();
		User user = null;
		
		//Basic validation for username and fullName
		if (username == null) {
			throw new CustomException("Please specify the username!");
		} else {
			username = username.trim();
			if (username.isEmpty()) throw new CustomException("Please specify the username!");
		}
		if (fullName == null) {
			throw new CustomException("Please specify the full name!");
		} else {
			fullName = fullName.trim();
			if (fullName.isEmpty()) throw new CustomException("Please specify the full name!");
		}
		
		//User ID is specified if this is an EDIT operation
		if (existingUserId != 0) {
			user = em.find(role.getBaseClassType(), existingUserId);
			if (user == null || user.getRole() != role) throw new CustomException("User not found");
		} else { //Checks specific to an ADD operation
			//There can be only 1 course coordinator!
			if (role == Role.COURSE_COORDINATOR) {
				User cc = UserManager.getCourseCoordinator(em);
				if (cc != null) throw new CustomException("Course Coordinator already exists. There can be only one person having this role!");
			}
		}

		Term term = null;
		//Finding the chosen term if the ID is specified
		if (role != Role.ADMINISTRATOR && role != Role.COURSE_COORDINATOR) {
			if (termId == 0) throw new CustomException("Term information required for this role");
			term = em.find(Term.class, termId);
			if (term == null) throw new CustomException("Term not found");
		}

		//Checking if this combination of username and term exists
		if (usernameExists(em, username, role, term, user)) {
			throw new CustomException("Username already exists for the selected term & role");
		}
		
		Team team = null;
		//Specifying team information is optional
		if (role == Role.STUDENT && teamId != 0) {
			team = em.find(Team.class, teamId);
			if (team == null) throw new CustomException("Specified team not found");
		}
		
		if (user == null) { //ADD operation
			if (role == Role.STUDENT) {
				user = new Student(username, fullName, null, term);
				((Student)user).setTeam(team);
			} else if (role == Role.FACULTY) {
				user = new Faculty(username, fullName, null, term);
			} else if (role == Role.TA) {
				user = new TA(username, fullName, null, term);
			} else if (role == Role.ADMINISTRATOR || role == Role.COURSE_COORDINATOR) {
				user = new User(username, fullName, null, role, term);
			} //TODO Store guest roles if needed

			em.persist(user);
			json.put("success", true);
			json.put("userId", user.getId());
		} else { //EDIT operation
			user.setUsername(username);
			user.setFullName(fullName);
			user.setTerm(term);
			if (role == Role.STUDENT) ((Student)user).setTeam(team);
			
			json.put("success", true);
		}
		
		return json;
	}
}
