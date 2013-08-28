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
import manager.MilestoneManager;
import manager.TermManager;
import org.apache.struts2.interceptor.ServletRequestAware;
import org.json.JSONObject;
import util.MiscUtil;

/**
 *
 * @author Prakhar
 */
public class CreateTermAction extends ActionSupport implements ServletRequestAware {

    private HttpServletRequest request;
    static final Logger logger = LoggerFactory.getLogger(CreateTermAction.class);
    private HashMap<String, Object> json = new HashMap<String, Object>();

    @Override
    public String execute() throws Exception {
		EntityManager em = null;
        try {
            json.put("exception", false);
            em = Persistence.createEntityManagerFactory(MiscUtil.PERSISTENCE_UNIT).createEntityManager();
			
			//Getting input data
			JSONObject inputData = new JSONObject(request.getParameter("jsonData"));
			int year = inputData.getInt("year");
			String semester = inputData.getString("semester");
			
			//Checking if the term already exists
            Term existingTerm = TermManager.findByYearAndSemester(em, year, semester);
            if (existingTerm != null) {
                logger.error("Term already exists");
                json.put("message", "Term already exists");
				json.put("success", false);
                return SUCCESS;
            }
            
            if (year != 0 && semester != null && !semester.equals("")) {
                //Save Term in DB
                EntityTransaction transaction = null;
                Term newTerm = new Term();
                newTerm.setAcademicYear(year);
                newTerm.setSemester(semester);
                TermManager.save(em, newTerm, transaction);
                json.put("message", "Term added");
                json.put("success", true);
            } else {
                json.put("message", "Term information provided incorrect.");
                json.put("success", false);
            }
            
        } catch (Exception e) {
            logger.error("Exception caught: " + e.getMessage());
            if (MiscUtil.DEV_MODE) {
                for (StackTraceElement s : e.getStackTrace()) {
                    logger.debug(s.toString());
                }
            }
            json.put("exception", true);
            json.put("message", "Error with CheckTerm: Escalate to developers!");
        } finally {
			if (em != null && em.isOpen()) em.close();
		}
        return SUCCESS;
    }

    public HashMap<String, Object> getJson() {
        return json;
    }

    public void setJson(HashMap<String, Object> json) {
        this.json = json;
    }
	
    public void setServletRequest(HttpServletRequest hsr) {
        this.request = hsr;
    }
}
