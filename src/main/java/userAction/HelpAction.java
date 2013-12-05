/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package userAction;

import com.opensymphony.xwork2.ActionSupport;
import javax.persistence.EntityManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.MiscUtil;

/**
 *
 * @author suresh
 */
public class HelpAction extends ActionSupport {
	private Logger logger = LoggerFactory.getLogger(HelpAction.class);

	@Override
	public String execute() throws Exception {
		EntityManager em = MiscUtil.getEntityManagerInstance();
		
		try {
			
		} catch (Exception e) {
			logger.error("Exception caught: " + e.getMessage());
			for (StackTraceElement s : e.getStackTrace()) {
				logger.error(s.toString());
			}
		} finally {
			if (em.getTransaction().isActive()) em.getTransaction().rollback();
			if (em.isOpen()) em.close();
		}
		
		return SUCCESS;
	}
	
}
