/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package manager;

import constant.Role;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.Persistence;
import javax.persistence.Query;
import model.Term;
import model.User;
import model.role.Faculty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.MiscUtil;

/**
 *
 * @author suresh
 */
public class UserManager {

    private static Logger logger = LoggerFactory.getLogger(UserManager.class);

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

    public static <T extends User> T getUser(long id, Class<T> type) {
        logger.trace("Getting User: " + id + ", by class: " + type);
        EntityManager em = null;
        T user = null;
        try {
            em = Persistence.createEntityManagerFactory(MiscUtil.PERSISTENCE_UNIT).createEntityManager();
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
