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
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import javax.persistence.Query;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import manager.TermManager;
import model.Team;
import model.Term;
import org.apache.struts2.interceptor.ServletRequestAware;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.MiscUtil;

/**
 *
 * @author suresh
 */
public class GetTeamsAction extends ActionSupport implements ServletRequestAware {
    
    private HttpServletRequest request;
    private static Logger logger = LoggerFactory.getLogger(GetTeamsAction.class);
    private String milestoneString;
    private String academicYearString;
    private HashMap<String, Object> json = new HashMap<String, Object>();
    
    public HttpServletRequest getRequest() {
        return request;
    }

    public void setRequest(HttpServletRequest request) {
        this.request = request;
    }
    
    public String getAcademicYearString() {
        return academicYearString;
    }
    
    public void setAcademicYearString(String academicYearString) {
        this.academicYearString = academicYearString;
    }
    
    public String getSemesterString() {
        return semesterString;
    }
    
    public void setSemesterString(String semesterString) {
        this.semesterString = semesterString;
    }
    private String semesterString;
    
    public String getMilestoneString() {
        return milestoneString;
    }
    
    public void setMilestoneString(String milestoneString) {
        this.milestoneString = milestoneString;
    }
    
    @Override
    public String execute() throws ServletException, IOException {
        try {
            EntityManager em = Persistence.createEntityManagerFactory(MiscUtil.PERSISTENCE_UNIT).createEntityManager();
            
            //Get term
            Term term = TermManager.findByYearAndSemester(em, Integer.parseInt(academicYearString), semesterString);
            
            logger.info("Getting all teams for term: " + term.getAcademicYear() + " " + term.getSemester());
            List<Team> teamList = null;
            EntityTransaction transaction = em.getTransaction();
            try {
                transaction.begin();
                Query q = em.createQuery("Select t from Team t where term = :term")
                        .setParameter("term", term);
                teamList = q.getResultList();
                transaction.commit();
            } catch (Exception e) {
                logger.error("Database Operation Error");
            }
            
            ArrayList<HashMap<String, Object>> teamJsonList = new ArrayList<HashMap<String, Object>>();
            for (Team t : teamList) {
                HashMap<String, Object> team = new HashMap<String, Object>();
                team.put("teamName", t.getTeamName());
                teamJsonList.add(team);
            }
            
            json.put("teamList", teamJsonList);
            json.put("success", true);
        } catch (Exception e) {
            logger.error("Exception caught: " + e.getMessage());
            if (MiscUtil.DEV_MODE) {
                for (StackTraceElement s : e.getStackTrace()) {
                    logger.debug(s.toString());
                }
            }
            json.put("success", false);
            json.put("message", "Error with GetSchedule: Escalate to developers!");
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
