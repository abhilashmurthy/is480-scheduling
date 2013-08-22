/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package manager;

import constant.Role;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import model.Term;
import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author suresh
 */
public class UserManager {

    private static Logger logger = LoggerFactory.getLogger(UserManager.class);
    
    public static void save(EntityManager em, User user) {
        logger.info("Saving user: " + user.getFullName());
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
	 * Method to find all the active roles for a user. Includes roles for the current active term and permanent roles.
	 * @param em
	 * @param username
	 * @param activeTerm
	 * @return List of User objects
	 */
    public static ArrayList<User> findActiveRolesByUsername(EntityManager em, String username, Term activeTerm) {
        logger.info("Finding active roles for: " + username);
        ArrayList<User> users = new ArrayList<User>();
        try {
            em.getTransaction().begin();
            Query q = em.createQuery("select o from User o where o.username = :username and (term.id = :termId or term is null)")
                    .setParameter("termId", activeTerm.getId());
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
	 * @param em
	 * @param role
	 * @param term
	 * @param username
	 * @return Single User object satisfying all criteria
	 */
	public static User findByRoleTermUsername(EntityManager em, Role role, Term term, String username) {
		logger.info("Getting specific user object");
        User user = null;
        try {
            em.getTransaction().begin();
            Query q = em.createQuery("select o from User o where role = :role and term = :term and username = :username");
			q.setParameter("role", role);
			q.setParameter("term", term);
			q.setParameter("username", username);
            user = (User) q.getSingleResult();
            em.getTransaction().commit();
        } catch (Exception e) {
            logger.error(e.getMessage());
            em.getTransaction().rollback();
        }
        return user;
	}

    public static List<User> getAllUsers(EntityManager em) {
        logger.info("Getting all users");
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
        logger.info("Getting Course Coordinator");
        User user = null;
        try {
            em.getTransaction().begin();
            Query q = em.createQuery("select o from User o where role = :role")
                    .setParameter("role", Role.COURSE_COORDINATOR);
            user = (User) q.getSingleResult();
            em.getTransaction().commit();
        } catch (Exception e) {
            logger.error("Database Operation Error");
            em.getTransaction().rollback();
        }
        return user;
    }
}
