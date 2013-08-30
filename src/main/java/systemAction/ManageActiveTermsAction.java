/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package systemAction;

import static com.opensymphony.xwork2.Action.SUCCESS;
import com.opensymphony.xwork2.ActionSupport;
import java.io.IOException;
import java.util.ArrayList;
import javax.persistence.EntityManager;
import javax.persistence.Persistence;
import javax.persistence.Query;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import manager.SettingsManager;
import model.Term;
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
	private ArrayList<Long> activeTerms = new ArrayList<Long>();

    @Override
    public String execute() throws ServletException, IOException {
		EntityManager em = null;
        try {
            em = Persistence.createEntityManagerFactory(MiscUtil.PERSISTENCE_UNIT).createEntityManager();
			Query q = em.createQuery("SELECT t FROM Term t");
			allTerms = (ArrayList<Term>) q.getResultList();
			ArrayList<Term> activeTermObjects = SettingsManager.getActiveTerms(em);
			for (Term t : activeTermObjects) {
				activeTerms.add(t.getId());
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

	public ArrayList<Long> getActiveTerms() {
		return activeTerms;
	}

	public void setActiveTerms(ArrayList<Long> activeTerms) {
		this.activeTerms = activeTerms;
	}
	
    public void setServletRequest(HttpServletRequest hsr) {
        request = hsr;
    }
}
