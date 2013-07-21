/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package manager;

import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.Persistence;
import javax.persistence.Query;
import model.Milestone;
import model.Role;
import model.Term;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Prakhar
 */
public class RoleManager {
	private static EntityManager em = Persistence.createEntityManagerFactory("scheduler").createEntityManager();
	private static Logger logger = LoggerFactory.getLogger(MilestoneManager.class);
	
	public static List<Role> getAllRolesByTerm (Term term) {
		logger.info("Getting all roles for a particular term");
		List<Role> rolesList = null;
		long termId = term.getId();
		try {
			em.getTransaction().begin();
			Query q = em.createNativeQuery("Select * from Role where term_id = :termId", Role.class);
			q.setParameter("termId", termId);
			rolesList = q.getResultList();
			em.getTransaction().commit();	
		} catch (Exception e) {
			e.printStackTrace();
			em.getTransaction().rollback();
		}
		return rolesList;
	}
}
