/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package systemAction;

import static com.opensymphony.xwork2.Action.ERROR;
import static com.opensymphony.xwork2.Action.SUCCESS;
import com.opensymphony.xwork2.ActionSupport;
import constant.Role;
import java.io.IOException;
import java.util.ArrayList;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import manager.SettingsManager;
import model.Term;
import model.User;
import org.apache.struts2.interceptor.ServletRequestAware;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.MiscUtil;

/**
 *
 * @author Suresh
 */
public class ManageActiveTermsAction extends ActionSupport implements ServletRequestAware {

    private HttpServletRequest request;
    static final Logger logger = LoggerFactory.getLogger(ManageActiveTermsAction.class);
	private ArrayList<Term> allTerms = new ArrayList<Term>();
	private ArrayList<Long> activeTermIds = new ArrayList<Long>();
	private ArrayList<Term> activeTermObjects = new ArrayList<Term>();
	private Long defaultTerm;

    @Override
    public String execute() throws ServletException, IOException {
		EntityManager em = null;
        try {
			em = MiscUtil.getEntityManagerInstance();
			HttpSession session = request.getSession();
			User user = (User) session.getAttribute("user");
			Role activeRole = (Role) session.getAttribute("activeRole");
			
			if (activeRole.equals(Role.ADMINISTRATOR) || activeRole.equals(Role.COURSE_COORDINATOR)) {
				Query q = em.createQuery("SELECT t FROM Term t");
				allTerms = (ArrayList<Term>) q.getResultList();
				activeTermObjects = SettingsManager.getActiveTerms(em);
				for (Term t : activeTermObjects) {
					activeTermIds.add(t.getId());
				}
				
				defaultTerm = SettingsManager.getDefaultTerm(em).getId();
				
			} else {
				request.setAttribute("error", "Oops. You're not authorized to access this page!");
				MiscUtil.logActivity(logger, user, "User cannot access this page");
				return ERROR;
			}
			
			return SUCCESS;
        } catch (Exception e) {
            logger.error("Exception caught: " + e.getMessage());
            if (MiscUtil.DEV_MODE) {
                for (StackTraceElement s : e.getStackTrace()) {
                    logger.debug(s.toString());
                }
            }
            request.setAttribute("error", "Error with GetAllTerms: Escalate to developers!");
            return ERROR;
        } finally {
			if (em != null && em.getTransaction().isActive()) em.getTransaction().rollback();
			if (em != null && em.isOpen()) em.close();
		}
    }
	
	public ArrayList<Term> getAllTerms() {
		return allTerms;
	}

	public void setAllTerms(ArrayList<Term> allTerms) {
		this.allTerms = allTerms;
	}

	public ArrayList<Long> getActiveTermIds() {
		return activeTermIds;
	}

	public void setActiveTermIds(ArrayList<Long> activeTermIds) {
		this.activeTermIds = activeTermIds;
	}
	
	public ArrayList<Term> getActiveTermObjects() {
		return activeTermObjects;
	}

	public void setActiveTermObjects(ArrayList<Term> activeTermObjects) {
		this.activeTermObjects = activeTermObjects;
	}

	public Long getDefaultTerm() {
		return defaultTerm;
	}

	public void setDefaultTerm(Long defaultTerm) {
		this.defaultTerm = defaultTerm;
	}

    public void setServletRequest(HttpServletRequest hsr) {
        request = hsr;
    }
}
