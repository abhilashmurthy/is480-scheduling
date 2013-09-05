/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package userAction;

import static com.opensymphony.xwork2.Action.SUCCESS;
import com.opensymphony.xwork2.ActionSupport;
import java.util.HashMap;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.servlet.http.HttpServletRequest;
import model.Term;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.persistence.Persistence;
import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpSession;
import manager.MilestoneManager;
import manager.TermManager;
import org.apache.struts2.interceptor.ServletRequestAware;
import org.json.JSONObject;
import util.MiscUtil;

/**
 *
 * @author Prakhar
 */
public class UpdateTermAction extends ActionSupport implements ServletRequestAware {

    private HttpServletRequest request;
    static final Logger logger = LoggerFactory.getLogger(UpdateTermAction.class);
    private HashMap<String, Object> json = new HashMap<String, Object>();

    public HashMap<String, Object> getJson() {
        return json;
    }

    public void setJson(HashMap<String, Object> json) {
        this.json = json;
    }

    @Override
    public String execute() throws Exception {
        EntityManager em = null;
        try {
            json.put("exception", false);
            em = Persistence.createEntityManagerFactory(MiscUtil.PERSISTENCE_UNIT).createEntityManager();

            JSONObject inputData = new JSONObject(request.getParameter("jsonData"));
            int year = inputData.getInt("year");
            String semester = inputData.getString("semester");
            Term currentTerm = (Term) request.getSession().getAttribute("currentActiveTerm");
            int activeYear = currentTerm.getAcademicYear();
            String activeSemester = currentTerm.getSemester();

            //To check if this term already exists
            logger.info("Active year: " + activeYear + ", Active semester: " + activeSemester);
            //Check if there is a change
            if (year == activeYear && semester.equals(activeSemester)) {
                json.put("success", false);
                json.put("message", "No change was made..");
                return SUCCESS;
            }

            Term newTerm = TermManager.findByYearAndSemester(em, year, semester);
            if (newTerm != null) {
                json.put("success", false);
                json.put("message", "Term already exists..");
                return SUCCESS;
            }

            //Update Term in DB
            EntityTransaction transaction = null;
            currentTerm = new Term(year, semester);
            TermManager.update(em, currentTerm, transaction);

            //Update Term in session
            HttpSession session = request.getSession();
            session.setAttribute("currentActiveTerm", currentTerm);

            json.put("success", true);
            json.put("message", "Updated Term");
            return SUCCESS;
        } catch (Exception e) {
            logger.error("Exception caught: " + e.getMessage());
            if (MiscUtil.DEV_MODE) {
                for (StackTraceElement s : e.getStackTrace()) {
                    logger.debug(s.toString());
                }
            }
            json.put("exception", true);
            json.put("message", "Error with UpdateTerm: Escalate to developers!");
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
}
