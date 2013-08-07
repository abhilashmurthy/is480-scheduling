/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package manager;

import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.Persistence;
import javax.persistence.Query;
import model.Role;
import model.Term;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.MiscUtil;

/**
 *
 * @author Prakhar
 */
public class RoleManager {
	private static Logger logger = LoggerFactory.getLogger(MilestoneManager.class);
	
	public static List<Role> getAllRolesByTerm (EntityManager em, Term term) {
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
	
	/**
	 * Method to get roles not linked to any term. Eg. admin, course coordinator.
	 * @param em
	 * @return 
	 */
	public static List<Role> getNonTermRoles (EntityManager em) {
		logger.info("Getting all roles not linked to any term");
		List<Role> rolesList = null;
		try {
			em.getTransaction().begin();
			Query q = em.createNativeQuery("Select * from Role where term_id IS NULL", Role.class);
			rolesList = q.getResultList();
			em.getTransaction().commit();
		} catch (Exception e) {
			e.printStackTrace();
			em.getTransaction().rollback();
		}
		return rolesList;
	}
}
