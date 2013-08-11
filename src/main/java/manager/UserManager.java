/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package manager;

import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.Query;
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
    
    public static User findByUsername(EntityManager em, String username) {
        logger.info("Finding user: " + username);
        User user = null;
        try {
            em.getTransaction().begin();
            Query q = em.createQuery("select o from User o where o.username = :username")
                    .setParameter("username", username);
            user = (User) q.getSingleResult();
            em.getTransaction().commit();
        } catch (Exception e) {
            logger.error("Database Operation Error");
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
            Query q = em.createQuery("select o from User o inner join o.roles r where r.name = :name")
                    .setParameter("name", "Course Coordinator");
            user = (User) q.getSingleResult();
            em.getTransaction().commit();
        } catch (Exception e) {
            logger.error("Database Operation Error");
            em.getTransaction().rollback();
        }
        return user;
    }
}
