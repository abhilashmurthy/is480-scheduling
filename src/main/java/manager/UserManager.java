/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package manager;

import constant.Role;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.servlet.http.HttpSession;
import model.Term;
import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.MiscUtil;

/**
 *
 * @author suresh
 */
public class UserManager {

    private static Logger logger = LoggerFactory.getLogger(UserManager.class);
	private ArrayList<Role> allRoles = new ArrayList<Role>();
	
	public UserManager() {
		populateAllRoles();
	}
	
	//Adding all the roles in the list in decreasing order of importance/power
	private void populateAllRoles() {
		allRoles.add(Role.ADMINISTRATOR);
		allRoles.add(Role.COURSE_COORDINATOR);
		allRoles.add(Role.FACULTY);
		allRoles.add(Role.STUDENT);
		allRoles.add(Role.TA);
		allRoles.add(Role.GUEST);
	}
	
	//Choosing the user object with the least important/powerful role
	private User chooseRole(ArrayList<User> users) {
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
	public void initializeUser(EntityManager em, HttpSession session, String username, String fullName, Term activeTerm) {
		ArrayList<User> users = findActiveRolesByUsername (em, username, activeTerm);

		if (users.isEmpty()) {
			User tempUser = new User(username, fullName, null, Role.GUEST, activeTerm);
			session.setAttribute("user", tempUser);
			session.setAttribute("activeRole", tempUser.getRole());
		} else {
			User chosenRole = chooseRole(users); //Choosing the default role to begin with
			session.setAttribute("user", getUser(chosenRole));
			session.setAttribute("activeRole", chosenRole.getRole());
			users.remove(chosenRole); //Removing the chosen object from the list of users
		}

		session.setAttribute("userRoles", users);
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
		return (User) q.getSingleResult();
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
}
