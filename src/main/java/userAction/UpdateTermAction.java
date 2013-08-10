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
import util.MiscUtil;

/**
 *
 * @author Prakhar
 */
public class UpdateTermAction extends ActionSupport implements ServletRequestAware {

    private HttpServletRequest request;
    static final Logger logger = LoggerFactory.getLogger(UpdateTermAction.class);
    private int year;
    private String semester;
    private int activeYear;

    public int getActiveYear() {
        return activeYear;
    }

    public void setActiveYear(int activeYear) {
        this.activeYear = activeYear;
    }

    public String getActiveSemester() {
        return activeSemester;
    }

    public void setActiveSemester(String activeSemester) {
        this.activeSemester = activeSemester;
    }
    private String activeSemester;
    private boolean canAdd;
    private HashMap<String, Object> json = new HashMap<String, Object>();

    public HashMap<String, Object> getJson() {
        return json;
    }

    public void setJson(HashMap<String, Object> json) {
        this.json = json;
    }

    public boolean isHasBeenAdded() {
        return canAdd;
    }

    public void setHasBeenAdded(boolean canAdd) {
        this.canAdd = canAdd;
    }

    @Override
    public String execute() throws Exception {
        try {
            json.put("exception", false);
            EntityManager em = Persistence.createEntityManagerFactory(MiscUtil.PERSISTENCE_UNIT).createEntityManager();
            
            json.put("year", year);
            json.put("semester", semester);

            //To check if this term already exists
            logger.info("Active year: " + activeYear + ", Active semester: " + activeSemester);
            Term existingTerm = TermManager.findByYearAndSemester(em, activeYear, activeSemester);
            if (existingTerm != null) {                
                //Check if there is a change
                if (year == activeYear && semester.equals(activeSemester)) {
                    json.put("success", false);
                    json.put("message", "No change was made");
                    return SUCCESS;
                }
                
                Term newTerm = TermManager.findByYearAndSemester(em, year, semester);
                if (newTerm != null) {
                    json.put("success", false);
                    json.put("message", "Term already exists...");
                    return SUCCESS;
                }
                
                 //Update Term in DB
                EntityTransaction transaction = null;
                existingTerm.setAcademicYear(year);
                existingTerm.setSemester(semester);
                TermManager.update(em, existingTerm, transaction);
                
                //Update Term in session
                HttpSession session = request.getSession();
                session.setAttribute("currentActiveTerm", existingTerm);
                
                json.put("success", true);
                json.put("message", "Updated Term");
                json.put("canAdd", true);
            } else {
                logger.error("Existing term doesn't exists!");
                json.put("message", "Term doesn't exist");
                json.put("success", false);
            }
            return SUCCESS;
        } catch (Exception e) {
            logger.error("Exception caught: " + e.getMessage());
            if (MiscUtil.DEV_MODE) {
                for (StackTraceElement s : e.getStackTrace()) {
                    logger.debug(s.toString());
                }
            }
            json.put("exception", true);
            json.put("message", "Error with CheckTerm: Escalate to developers!");
        }
        return SUCCESS;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public String getSemester() {
        return semester;
    }

    public void setSemester(String semester) {
        this.semester = semester;
    }

    public void setServletRequest(HttpServletRequest hsr) {
        this.request = hsr;
    }
}
