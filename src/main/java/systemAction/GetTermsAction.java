/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package systemAction;

import static com.opensymphony.xwork2.Action.SUCCESS;
import com.opensymphony.xwork2.ActionSupport;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.Persistence;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import manager.MilestoneManager;
import manager.TermManager;
import model.Milestone;
import model.Term;
import org.apache.struts2.interceptor.ServletRequestAware;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.MiscUtil;

/**
 *
 * @author Prakhar
 */
public class GetTermsAction extends ActionSupport implements ServletRequestAware {

    private List<Term> listTerms;
    private List<HashMap<String, String>> dataList;
    private List<HashMap<String, String>> dataListMilestones;
    private HttpServletRequest request;
    static final Logger logger = LoggerFactory.getLogger(GetTermsAction.class);

    @Override
    public String execute() throws ServletException, IOException {
		EntityManager em = null;
        try {
            em = MiscUtil.getEntityManagerInstance();

            //Getting all the term objects
            listTerms = TermManager.getAllTerms(em);
            if (listTerms != null) {
				dataList = new ArrayList<HashMap<String, String>>();
				dataListMilestones = new ArrayList<HashMap<String, String>>();
				//Getting term specific details
                for (Term term : listTerms) {
					HashMap<String, String> map = new HashMap<String, String>();
					//Getting term specific details
                    long termId = term.getId();
                    String termName = term.getDisplayName();
					map.put("termName", termName);
                    map.put("termId", Long.toString(termId));
					dataList.add(map);
				
					//Getting milestone specific details
					HashMap<String, String> mapMilestone = new HashMap<String, String>();
					mapMilestone.put("termName", termName);
					mapMilestone.put("termId", Long.toString(termId));
					String mStone = "";
					List<Milestone> milestones = MilestoneManager.findByTerm(em, term);
					for (Milestone milestone: milestones) {
						//for every milestone, add it to the map
						mStone += milestone.getName() + ",";
						mapMilestone.put("milestone", mStone);
						dataListMilestones.add(mapMilestone);
					}
				}
            } else {
                request.setAttribute("error", "You cannot create a schedule right now. Please create a term first!");
                logger.error("User cannot create schedule before creating term.");
                return ERROR;
            }
        } catch (Exception e) {
            logger.error("Exception caught: " + e.getMessage());
            if (MiscUtil.DEV_MODE) {
                for (StackTraceElement s : e.getStackTrace()) {
                    logger.debug(s.toString());
                }
            }
            request.setAttribute("error", "Error with GetTerms: Escalate to developers!");
            return ERROR;
        } finally {
			if (em != null && em.getTransaction().isActive()) em.getTransaction().rollback();
			if (em != null && em.isOpen()) em.close();
		}
        return SUCCESS;
    }

	public List<HashMap<String, String>> getDataList() {
		return dataList;
	}

	public void setDataList(List<HashMap<String, String>> dataList) {
		this.dataList = dataList;
	}

	public List<HashMap<String, String>> getDataListMilestones() {
		return dataListMilestones;
	}

	public void setDataListMilestones(List<HashMap<String, String>> dataListMilestones) {
		this.dataListMilestones = dataListMilestones;
	}

    public void setServletRequest(HttpServletRequest hsr) {
        request = hsr;
    }
}
