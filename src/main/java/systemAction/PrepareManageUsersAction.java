/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package systemAction;

import com.google.gson.Gson;
import com.opensymphony.xwork2.ActionSupport;
import constant.Role;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import javax.persistence.EntityManager;
import javax.persistence.Persistence;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import manager.ScheduleManager;
import manager.SettingsManager;
import manager.UserManager;
import model.Schedule;
import model.Settings;
import model.Team;
import model.Term;
import model.Timeslot;
import model.User;
import model.role.Faculty;
import model.role.Student;
import model.role.TA;
import org.apache.struts2.interceptor.ServletRequestAware;
import org.hibernate.Hibernate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.MiscUtil;

/**
 *
 * @author Prakhar
 */
public class PrepareManageUsersAction extends ActionSupport implements ServletRequestAware {
	
	private long termId;
	private ArrayList<HashMap<String, Object>> adminData = new ArrayList<HashMap<String, Object>>();
	private ArrayList<HashMap<String, Object>> ccData = new ArrayList<HashMap<String, Object>>();
	private ArrayList<HashMap<String, Object>> studentData = new ArrayList<HashMap<String, Object>>();
	private ArrayList<HashMap<String, Object>> facultyData = new ArrayList<HashMap<String, Object>>();
	private ArrayList<HashMap<String, Object>> taData = new ArrayList<HashMap<String, Object>>();
	private ArrayList<HashMap<String, Object>> teamData = new ArrayList<HashMap<String, Object>>();
	private ArrayList<HashMap<String, Object>> termData = new ArrayList<HashMap<String, Object>>();
    private HttpServletRequest request;
    private static Logger logger = LoggerFactory.getLogger(PrepareManageUsersAction.class);
	
	@Override
    public String execute() {
		EntityManager em = null;
        try {
            em = MiscUtil.getEntityManagerInstance();
			HttpSession session = request.getSession();
			
			User user = (User) session.getAttribute("user");
			if (user.getRole().equals(Role.ADMINISTRATOR) || user.getRole().equals(Role.COURSE_COORDINATOR)) {
				em.getTransaction().begin();
				
				//TERM MANAGEMENT
				Term currentActiveTerm = (Term) session.getAttribute("currentActiveTerm");
				Term term = (Term) session.getAttribute("currentActiveTerm");
				if (termId != 0) {
					ArrayList<Term> activeTerms = SettingsManager.getActiveTerms(em);
					for (Term activeTerm : activeTerms) {
						if (term.getId() == termId) {
							term = activeTerm;
							UserManager.initializeUser(em, session, user.getUsername(), user.getFullName(), term);
							break;
						}
					}
				}
				ArrayList<Term> allActiveTerms = SettingsManager.getActiveTerms(em);
				allActiveTerms.remove(currentActiveTerm);
				if (allActiveTerms.size() > 0) {
					for (Term activeTerm : allActiveTerms) {
						HashMap<String, Object> map = new HashMap<String, Object>();
						map.put("termName", term.getDisplayName());
						map.put("termId", String.valueOf(term.getId()));
						termData.add(map);
					}
				}
				
				//ADMIN MANAGEMENT
				List<User> adminUsers = UserManager.findByTermAndRole(em, null, Role.ADMINISTRATOR);
				for (User adminUser : adminUsers) {
					HashMap<String, Object> adminMap = new HashMap<String, Object>();
					adminMap.put("id", adminUser.getId());
					adminMap.put("name", adminUser.getFullName());
					adminMap.put("username", adminUser.getUsername());
					adminMap.put("mobileNumber", adminUser.getMobileNumber());
					adminData.add(adminMap);
				}
				
				//COURSE COORDINATOR MANAGEMENT
				List<User> ccUsers = UserManager.findByTermAndRole(em, null, Role.COURSE_COORDINATOR);
				for (User ccUser : ccUsers) {
					HashMap<String, Object> ccMap = new HashMap<String, Object>();
					ccMap.put("id", ccUser.getId());
					ccMap.put("name", ccUser.getFullName());
					ccMap.put("username", ccUser.getUsername());
					ccMap.put("mobileNumber", ccUser.getMobileNumber());
					ccData.add(ccMap);
				}
				
				//TEAM MANAGEMENT
				List<Team> teams = UserManager.getTeamsByTerm(em, term);
				for (Team team : teams) {
					HashMap<String, Object> teamMap = new HashMap<String, Object>();
					teamMap.put("id", team.getId());
					teamMap.put("name", team.getTeamName());
					Set<Student> students = team.getMembers();
					HashMap<String, Object> memberMap = new HashMap<String, Object>();
					for (Student student : students) {
						memberMap.put("id", student.getId());
						memberMap.put("name", student.getFullName());
						memberMap.put("username", student.getUsername());
					}
					teamMap.put("members", memberMap);
					HashMap<String, Object> supervisorMap = new HashMap<String, Object>();
					supervisorMap.put("id", team.getSupervisor().getId());
					supervisorMap.put("name", team.getSupervisor().getFullName());
					supervisorMap.put("username", team.getSupervisor().getUsername());
					teamMap.put("supervisor", supervisorMap);
					HashMap<String, Object> reviewer1Map = new HashMap<String, Object>();
					supervisorMap.put("id", team.getReviewer1().getId());
					supervisorMap.put("name", team.getReviewer1().getFullName());
					supervisorMap.put("username", team.getReviewer1().getUsername());
					teamMap.put("reviewer1", reviewer1Map);
					HashMap<String, Object> reviewer2Map = new HashMap<String, Object>();
					supervisorMap.put("id", team.getReviewer2().getId());
					supervisorMap.put("name", team.getReviewer2().getFullName());
					supervisorMap.put("username", team.getReviewer2().getUsername());
					teamMap.put("reviewer2", reviewer2Map);
					teamData.add(teamMap);
				}
				
				//STUDENT MANAGEMENT
				List<User> studentUsers = UserManager.findByTermAndRole(em, term, Role.STUDENT);
				for (User studentUser : studentUsers) {
					Student student = (Student) studentUser;
					HashMap<String, Object> studentMap = new HashMap<String, Object>();
					studentMap.put("id", student.getId());
					studentMap.put("name", student.getFullName());
					studentMap.put("username", student.getUsername());
					studentMap.put("mobileNumber", student.getMobileNumber());
					studentMap.put("teamId", student.getTeam().getId());
					studentMap.put("teamName", student.getTeam().getTeamName());
					studentData.add(studentMap);
				}
				
				//FACULTY MANAGEMENT
				List<User> facultyUsers = UserManager.findByTermAndRole(em, term, Role.FACULTY);
				for (User facultyUser : facultyUsers) {
					Faculty faculty = (Faculty) facultyUser;
					HashMap<String, Object> facultyMap = new HashMap<String, Object>();
					facultyMap.put("id", faculty.getId());
					facultyMap.put("name", faculty.getFullName());
					facultyMap.put("username", faculty.getUsername());
					facultyMap.put("mobileNumber", faculty.getMobileNumber());
					HashMap<String, Object> myTeamsMap = new HashMap<String, Object>();
					for (Team team : teams) {
						List<String> myRoles = new ArrayList<String>();
						HashMap<String, Object> teamMap = new HashMap<String, Object>();
						teamMap.put("id", team.getId());
						teamMap.put("name", team.getTeamName());
						if (team.getSupervisor().equals(faculty)) {
							myRoles.add("Supervisor");
						}
						else if (team.getReviewer1().equals(faculty)) {
							myRoles.add("Reviewer1");
						}
						else if (team.getReviewer2().equals(faculty)) {
							myRoles.add("Reviewer2");
						}
						if (myRoles.size() > 0) {
							teamMap.put("myRoles", myRoles);
							myTeamsMap.put(team.getTeamName(), teamMap);
						}
					}
					facultyMap.put("myTeams", myTeamsMap);
					facultyData.add(facultyMap);
				}
				
				//TA MANAGEMENT
				List<User> taUsers = UserManager.findByTermAndRole(em, term, Role.TA);
				for (User taUser : taUsers) {
					TA ta = (TA) taUser;
					HashMap<String, Object> taMap = new HashMap<String, Object>();
					taMap.put("id", ta.getId());
					taMap.put("name", ta.getFullName());
					taMap.put("username", ta.getUsername());
					taMap.put("mobileNumber", ta.getMobileNumber());
					List<Schedule> schedules = ScheduleManager.findByTerm(em, term);
					HashMap<String, Object> myScheduleMap = new HashMap<String, Object>();
					for (Schedule schedule : schedules) {
						List<String> mySignups = new ArrayList<String>();
						HashMap<String, Object> scheduleMap = new HashMap<String, Object>();
						scheduleMap.put("name", schedule.getMilestone().getName());
						for (Timeslot timeslot : schedule.getTimeslots()) {
							if (timeslot.getTA() != null && timeslot.getTA().equals(ta)) {
								if (timeslot.getCurrentBooking() != null) mySignups.add(timeslot.getCurrentBooking().getTeam().getTeamName() + " | " + timeslot.getStartTime().toString());
								else mySignups.add(timeslot.getStartTime().toString());
							}
						}
						if (mySignups.size() > 0) scheduleMap.put("signups", mySignups);
						myScheduleMap.put(schedule.getMilestone().getName(), scheduleMap);
					}
					taMap.put("mySchedules", myScheduleMap);
					taData.add(taMap);
				}
				
				logger.debug("adminData: " + adminData.size());
				logger.debug("ccData: " + ccData.size());
				logger.debug("studentData: " + studentData.size());
				logger.debug("facultyData: " + facultyData.size());
				logger.debug("taData: " + taData.size());
				em.getTransaction().commit();
			}
		} catch (Exception e) {
			logger.error("Exception caught: " + e.getMessage());
            if (MiscUtil.DEV_MODE) {
                for (StackTraceElement s : e.getStackTrace()) {
                    logger.debug(s.toString());
                }
            }
        } finally {
			if (em != null && em.getTransaction().isActive()) em.getTransaction().rollback();
			if (em != null && em.isOpen()) em.close();
		}
        return SUCCESS;
	 }
	 
	public HttpServletRequest getRequest() {
        return request;
    }

    public void setRequest(HttpServletRequest request) {
        this.request = request;
    }
	
	public void setServletRequest(HttpServletRequest hsr) {
        this.request = hsr;
    }
	
	public long getTermId() {
		return termId;
	}

	public void setTermId(long termId) {
		this.termId = termId;
	}
	
	public ArrayList<HashMap<String, Object>> getAdminData() {
		return adminData;
	}

	public void setAdminData(ArrayList<HashMap<String, Object>> adminData) {
		this.adminData = adminData;
	}

	public ArrayList<HashMap<String, Object>> getCcData() {
		return ccData;
	}

	public void setCcData(ArrayList<HashMap<String, Object>> ccData) {
		this.ccData = ccData;
	}

	public ArrayList<HashMap<String, Object>> getStudentData() {
		return studentData;
	}

	public void setStudentData(ArrayList<HashMap<String, Object>> studentData) {
		this.studentData = studentData;
	}

	public ArrayList<HashMap<String, Object>> getFacultyData() {
		return facultyData;
	}

	public void setFacultyData(ArrayList<HashMap<String, Object>> facultyData) {
		this.facultyData = facultyData;
	}

	public ArrayList<HashMap<String, Object>> getTaData() {
		return taData;
	}

	public void setTaData(ArrayList<HashMap<String, Object>> taData) {
		this.taData = taData;
	}

	public ArrayList<HashMap<String, Object>> getTeamData() {
		return teamData;
	}

	public void setTeamData(ArrayList<HashMap<String, Object>> teamData) {
		this.teamData = teamData;
	}
	
	public ArrayList<HashMap<String, Object>> getTermData() {
		return termData;
	}

	public void setTermData(ArrayList<HashMap<String, Object>> termData) {
		this.termData = termData;
	}
	
}