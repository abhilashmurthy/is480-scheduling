/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package systemAction;

import com.opensymphony.xwork2.ActionSupport;
import constant.Role;
import java.util.ArrayList;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.MiscUtil;

/**
 *
 * @author suresh
 */
public class HelpAction extends ActionSupport {
	private Logger logger = LoggerFactory.getLogger(HelpAction.class);
	private ArrayList<String> adminEmails = new ArrayList<String>();

	@Override
	public String execute() throws Exception {
		EntityManager em = null;
		try {
			em = MiscUtil.getEntityManagerInstance();
			
			Query q = em.createQuery("SELECT u FROM User u WHERE u.role = :role");
			q.setParameter("role", Role.ADMINISTRATOR);
			ArrayList<User> admins = (ArrayList<User>) q.getResultList();
			
			for (User u : admins) {
				adminEmails.add(u.getEmail());
			}
		} catch (Exception e) {
			logger.error("Exception caught: " + e.getMessage());
			for (StackTraceElement s : e.getStackTrace()) {
				logger.error(s.toString());
			}
		} finally {
			if (em != null) {
				if (em.getTransaction().isActive()) em.getTransaction().rollback();
				if (em.isOpen()) em.close();	
			}
		}
		
		return SUCCESS;
	}
	
	public ArrayList<String> getAdminEmails() {
		return adminEmails;
	}

	public void setAdminEmails(ArrayList<String> adminEmails) {
		this.adminEmails = adminEmails;
	}

}
