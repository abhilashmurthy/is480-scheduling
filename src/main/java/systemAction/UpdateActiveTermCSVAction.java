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
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import manager.SettingsManager;
import manager.UserManager;
import model.Term;
import model.User;
import org.apache.struts2.interceptor.ServletRequestAware;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.MiscUtil;

/**
 *
 * @author Prakhar
 */
public class UpdateActiveTermCSVAction extends ActionSupport implements ServletRequestAware {

    private HttpServletRequest request;
    static final Logger logger = LoggerFactory.getLogger(UpdateActiveTermCSVAction.class);

    @Override
    public String execute() throws ServletException, IOException {
		EntityManager em = null;
        try {
            em = MiscUtil.getEntityManagerInstance();
			User user = (User) request.getSession().getAttribute("user");
			JSONObject inputObject = (JSONObject) new JSONObject(request.getParameter("jsonData"));
			long activeTermId = Long.valueOf(inputObject.getString("termId"));
			//Changing active term
            if (activeTermId != 0) {
                ArrayList<Term> activeTerms = SettingsManager.getActiveTerms(em);
                for (Term term : activeTerms) {
                    if (term.getId() == activeTermId) {
                        request.getSession().setAttribute("currentActiveTerm", term);
						//Refreshing the user object in the session based on the new term selected
						new UserManager().initializeUser(em, request.getSession(), user.getUsername(), user.getFullName(), term);
                    }
                }
            } 
        } catch (Exception e) {
            logger.error("Exception caught: " + e.getMessage());
            if (MiscUtil.DEV_MODE) {
                for (StackTraceElement s : e.getStackTrace()) {
                    logger.debug(s.toString());
                }
            }
            request.setAttribute("error", "Error with UpdateActiveTermCSVAction: Escalate to developers!");
            return ERROR;
        } finally {
			if (em != null && em.getTransaction().isActive()) em.getTransaction().rollback();
			if (em != null && em.isOpen()) em.close();
		}
        return SUCCESS;
    }

    public void setServletRequest(HttpServletRequest hsr) {
        request = hsr;
    }
}
