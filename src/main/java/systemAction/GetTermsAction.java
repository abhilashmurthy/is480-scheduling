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
import manager.TermManager;
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
    private HttpServletRequest request;
    static final Logger logger = LoggerFactory.getLogger(GetTermsAction.class);

    @Override
    public String execute() throws ServletException, IOException {
		EntityManager em = null;
        try {
            em = Persistence.createEntityManagerFactory(MiscUtil.PERSISTENCE_UNIT).createEntityManager();

            //Getting all the term objects
            listTerms = TermManager.getAllTerms(em);
            if (listTerms != null) {
                dataList = new ArrayList<HashMap<String, String>>();
                HashMap<String, String> map = null;
                for (Term term : listTerms) {
                    map = new HashMap<String, String>();
                    String semester = term.getSemester();  //e.g. 1
                    int year = term.getAcademicYear();  //e.g. 2013
                    long termId = term.getId();
                    //e.g. 2013-2014
                    String academicYear = Integer.toString(year) + "-" + Integer.toString(year + 1);
                    //e.g. Term 1
                    String termName = academicYear + semester;
                    map.put("termName", termName);
                    map.put("termId", Long.toString(termId));

                    dataList.add(map);
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

    public void setDataList(ArrayList<HashMap<String, String>> dataList) {
        this.dataList = dataList;
    }

    public void setServletRequest(HttpServletRequest hsr) {
        request = hsr;
    }
}
