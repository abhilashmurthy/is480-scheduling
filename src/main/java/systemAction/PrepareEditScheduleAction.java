/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package systemAction;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import static com.opensymphony.xwork2.Action.SUCCESS;
import com.opensymphony.xwork2.ActionSupport;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.servlet.http.HttpServletRequest;
import manager.MilestoneManager;
import manager.ScheduleManager;
import manager.TimeslotManager;
import model.Milestone;
import model.Schedule;
import model.Term;
import org.apache.struts2.interceptor.ServletRequestAware;
import org.json.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.MiscUtil;

/**
 *
 * @author Prakhar
 */
public class PrepareEditScheduleAction extends ActionSupport implements ServletRequestAware {
    private HttpServletRequest request;
    static final Logger logger = LoggerFactory.getLogger(PrepareEditScheduleAction.class);
	
	//Struts variables
	String scheduleJson;
	String termNameJson;

    @Override
    public String execute() throws Exception {
        EntityManager em = null;
		Gson gson = new GsonBuilder().disableHtmlEscaping().create();
        try {
            em = MiscUtil.getEntityManagerInstance();
			Term activeTerm = (Term) request.getSession().getAttribute("currentActiveTerm");
			
			//Get milestones and schedules
			List<Milestone> milestones = MilestoneManager.findByTerm(em, activeTerm);
			ArrayList<HashMap<String, Object>> scheduleList = new ArrayList<HashMap<String, Object>>();
			for (Milestone m : milestones) {
				HashMap<String, Object> scheduleInfo = new HashMap<String, Object>();
				Schedule s = ScheduleManager.findByMilestone(em, m);
				scheduleInfo.put("scheduleId", s.getId());
				scheduleInfo.put("milestoneName", m.getName());
				scheduleInfo.put("dayStartTime", s.getDayStartTime());
				scheduleInfo.put("dayEndTime", s.getDayEndTime());
				scheduleInfo.put("milestoneOrder", m.getMilestoneOrder());
				scheduleInfo.put("dates", TimeslotManager.getUniqueDatesForSchedule(em, s));
				scheduleList.add(scheduleInfo);
			}
			scheduleJson = gson.toJson(scheduleList);
			
			//Get Term Names
			Query q = em.createQuery("select t from Term t where t NOT IN (select t2 from Term t2 where t2 = :term)").setParameter("term", activeTerm);
			List<Term> terms = q.getResultList();
			List<HashMap<String, Object>> termsMap = new ArrayList<HashMap<String, Object>>();
			for (Term t : terms) {
				HashMap termMap = new HashMap();
				termMap.put("year", t.getAcademicYear());
				termMap.put("term", t.getSemester());
				termsMap.add(termMap);
			}
			termNameJson = gson.toJson(termsMap);
			
			return SUCCESS;
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
	
	public String getScheduleJson() {
		return scheduleJson;
	}

	public void setScheduleJson(String scheduleJson) {
		this.scheduleJson = scheduleJson;
	}

	public String getTermNameJson() {
		return termNameJson;
	}

	public void setTermNameJson(String termNameJson) {
		this.termNameJson = termNameJson;
	}

    public void setServletRequest(HttpServletRequest hsr) {
        request = hsr;
    }
}
