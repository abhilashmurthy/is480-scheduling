/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package systemAction;

import static com.opensymphony.xwork2.Action.SUCCESS;
import com.opensymphony.xwork2.ActionSupport;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.xml.bind.DatatypeConverter;
import manager.SettingsManager;
import manager.TermManager;
import manager.UserManager;
import model.Term;
import org.apache.struts2.interceptor.ServletRequestAware;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.MiscUtil;

/**
 *
 * @author Abhilash
 */
public class WelcomeAction extends ActionSupport implements ServletRequestAware {

    private long selectedTermId;   //To get the active term id user chooses
	private String t;
    private ArrayList<HashMap<String, Object>> termData = new ArrayList<HashMap<String, Object>>();
    private HttpServletRequest request;
    private static Logger logger = LoggerFactory.getLogger(WelcomeAction.class);

    @Override
    public String execute() throws Exception {
        EntityManager em = null;
        try {
            em = MiscUtil.getEntityManagerInstance();
			em.getTransaction().begin();
            HttpSession session = request.getSession();
			
			if (t != null) {
				selectedTermId = Long.parseLong(new String(DatatypeConverter.parseBase64Binary(t)));
			}

			//TERM MANAGEMENT
			Term term = null;
			if (selectedTermId != 0) {
				term = TermManager.findTermById(em, selectedTermId);
			} else {
				term = TermManager.getDefaultActiveTerm(em);
				selectedTermId = term.getId();
			}
			
			ArrayList<Term> activeTerms = SettingsManager.getActiveTerms(em);
			for (Term activeTerm : activeTerms) {
				HashMap<String, Object> termMap = new HashMap<String, Object>();
				termMap.put("termName", activeTerm.getDisplayName());
				termMap.put("termId", activeTerm.getId());
				termData.add(termMap);
			}
			Collections.sort(termData, new Comparator<HashMap<String, Object>>(){
				public int compare(HashMap<String, Object> o1, HashMap<String, Object> o2) {
					return String.valueOf(o1.get("termName")).compareToIgnoreCase(String.valueOf(o2.get("termName")));
				}
			});
			session.setAttribute("currentActiveTerm", term);
			
			UserManager.initializeUser(em, session, "_", "_", term);
			
			em.getTransaction().commit();
        } catch (Exception e) {
            logger.error("Exception caught: " + e.getMessage());
            if (MiscUtil.DEV_MODE) {
                for (StackTraceElement s : e.getStackTrace()) {
                    logger.debug(s.toString());
                }
            }
        } finally {
            if (em != null && em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            if (em != null && em.isOpen()) {
                em.close();
            }
        }
        return SUCCESS;
    }

	public void setServletRequest(HttpServletRequest hsr) {
		this.request = hsr;
	}
	
	public HttpServletRequest getServletRequest() {
		return request;
	}
	
	public long getSelectedTermId() {
		return selectedTermId;
	}

	public void setSelectedTermId(long selectedTermId) {
		this.selectedTermId = selectedTermId;
	}

	public ArrayList<HashMap<String, Object>> getTermData() {
		return termData;
	}

	public void setTermData(ArrayList<HashMap<String, Object>> termData) {
		this.termData = termData;
	}
	
	public String getT() {
		return t;
	}

	public void setT(String t) {
		this.t = t;
	}
	
}
