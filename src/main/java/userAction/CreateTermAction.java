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
import util.MiscUtil;

/**
 *
 * @author Prakhar
 */
public class CreateTermAction extends ActionSupport implements ServletRequestAware {

    private HttpServletRequest request;
    static final Logger logger = LoggerFactory.getLogger(CreateTermAction.class);
    private int year;
    private String semester;
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
            //To check if this term already exists

            json.put("year", year);
            json.put("semester", semester);

            Term existingTerm = TermManager.findByYearAndSemester(em, year, semester);
            if (existingTerm != null) {
                logger.error("Term already exists");
                json.put("message", "Term already exists");
                json.put("canAdd", false);
                return SUCCESS;
            }
            
            //Save Term in DB
            EntityTransaction transaction = null;
            Term newTerm = new Term();
            newTerm.setAcademicYear(year);
            newTerm.setSemester(semester);
            TermManager.save(em, newTerm, transaction);
            json.put("message", "Term added");
            json.put("canAdd", true);
            
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
