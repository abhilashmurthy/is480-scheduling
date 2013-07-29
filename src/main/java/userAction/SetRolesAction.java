/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package userAction;

import com.opensymphony.xwork2.ActionSupport;
import constant.Status;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.EntityManager;
import javax.persistence.Persistence;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import manager.MilestoneManager;
import manager.ScheduleManager;
import manager.TermManager;
import model.Milestone;
import model.Schedule;
import model.Team;
import model.Term;
import model.Timeslot;
import model.User;
import org.apache.struts2.interceptor.ServletRequestAware;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.MiscUtil;

/**
 *
 * @author Prakhar
 */
public class SetRolesAction extends ActionSupport implements ServletRequestAware {
	
	private HttpServletRequest request;
	private String roleParam;
	
	@Override
	public String execute() throws Exception {
		EntityManager em = Persistence.createEntityManagerFactory(MiscUtil.PERSISTENCE_UNIT).createEntityManager();
//		String roleParam = request.getParameter("role");
		
		
		
		
		
		
		
		return SUCCESS;
	}

	public void setServletRequest(HttpServletRequest hsr) {
		this.request = hsr;
	}

	public String getRoleParam() {
		return roleParam;
	}

	public void setRoleParam(String roleParam) {
		this.roleParam = roleParam;
	}
	
} //end of class