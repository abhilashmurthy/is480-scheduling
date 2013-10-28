/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package systemAction;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.opensymphony.xwork2.ActionSupport;
import constant.Role;
import java.text.SimpleDateFormat;
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
import manager.TermManager;
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
	
	private long selectedTermId;
	private ArrayList<HashMap<String, Object>> adminData = new ArrayList<HashMap<String, Object>>();
	private ArrayList<HashMap<String, Object>> ccData = new ArrayList<HashMap<String, Object>>();
	private ArrayList<HashMap<String, Object>> teamData = new ArrayList<HashMap<String, Object>>();
	private ArrayList<HashMap<String, Object>> studentData = new ArrayList<HashMap<String, Object>>();
	private ArrayList<HashMap<String, Object>> facultyData = new ArrayList<HashMap<String, Object>>();
	private ArrayList<HashMap<String, Object>> taData = new ArrayList<HashMap<String, Object>>();
	private ArrayList<HashMap<String, Object>> termData = new ArrayList<HashMap<String, Object>>();
	private String adminJson;
	private String ccJson;
	private String teamJson;
	private String studentJson;
	private String facultyJson;
	private String taJson;
	
    private HttpServletRequest request;
    private static Logger logger = LoggerFactory.getLogger(PrepareManageUsersAction.class);
	
	@Override
    public String execute() {
		EntityManager em = null;
		Gson gson = new GsonBuilder().disableHtmlEscaping().create();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		
        try {
            em = MiscUtil.getEntityManagerInstance();
			HttpSession session = request.getSession();
			
			User user = (User) session.getAttribute("user");
			if (user.getRole().equals(Role.ADMINISTRATOR) || user.getRole().equals(Role.COURSE_COORDINATOR)) {
				em.getTransaction().begin();
				
				//TERM MANAGEMENT
				Term term = (Term) session.getAttribute("currentActiveTerm");
				if (selectedTermId != 0) {
					term = TermManager.findTermById(em, selectedTermId);
					UserManager.initializeUser(em, session, user.getUsername(), user.getFullName(), term); //SURESH PLEASE TAKE A LOOK - fionalee becoming guest for new term
				}
				ArrayList<Term> activeTerms = SettingsManager.getActiveTerms(em);
				for (Term activeTerm : activeTerms) {
					HashMap<String, Object> termMap = new HashMap<String, Object>();
					termMap.put("termName", activeTerm.getDisplayName());
					termMap.put("termId", activeTerm.getId());
					termData.add(termMap);
				}
				
				//ADMIN MANAGEMENT
				List<User> adminUsers = UserManager.findByTermAndRole(em, null, Role.ADMINISTRATOR);
				for (User adminUser : adminUsers) {
					HashMap<String, Object> adminMap = new HashMap<String, Object>();
					adminMap.put("id", adminUser.getId());
					adminMap.put("name", adminUser.getFullName());
					adminMap.put("username", adminUser.getUsername());
					adminMap.put("mobileNumber", (adminUser.getMobileNumber() != null?adminUser.getMobileNumber():"-"));
					adminData.add(adminMap);
				}
				
				//COURSE COORDINATOR MANAGEMENT
				List<User> ccUsers = UserManager.findByTermAndRole(em, null, Role.COURSE_COORDINATOR);
				for (User ccUser : ccUsers) {
					HashMap<String, Object> ccMap = new HashMap<String, Object>();
					ccMap.put("id", ccUser.getId());
					ccMap.put("name", ccUser.getFullName());
					ccMap.put("username", ccUser.getUsername());
					ccMap.put("mobileNumber", (ccUser.getMobileNumber() != null?ccUser.getMobileNumber():"-"));
					ccData.add(ccMap);
				}
				
				//TEAM MANAGEMENT
				List<Team> teams = UserManager.getTeamsByTerm(em, term);
				for (Team team : teams) {
					HashMap<String, Object> teamMap = new HashMap<String, Object>();
					teamMap.put("id", team.getId());
					teamMap.put("teamName", team.getTeamName());
					teamMap.put("wiki", team.getWiki());
					Set<Student> students = team.getMembers();
					List<HashMap<String, Object>> memberList = new ArrayList<HashMap<String, Object>>();
					for (Student student : students) {
						HashMap<String, Object> memberMap = new HashMap<String, Object>();
						memberMap.put("id", student.getId());
						memberMap.put("name", student.getFullName());
						memberMap.put("username", student.getUsername());
						memberList.add(memberMap);
					}
					teamMap.put("members", memberList);
					HashMap<String, Object> supervisorMap = new HashMap<String, Object>();
					supervisorMap.put("id", team.getSupervisor().getId());
					supervisorMap.put("name", team.getSupervisor().getFullName());
					supervisorMap.put("username", team.getSupervisor().getUsername());
					teamMap.put("supervisor", supervisorMap);
					HashMap<String, Object> reviewer1Map = new HashMap<String, Object>();
					reviewer1Map.put("id", team.getReviewer1().getId());
					reviewer1Map.put("name", team.getReviewer1().getFullName());
					reviewer1Map.put("username", team.getReviewer1().getUsername());
					teamMap.put("reviewer1", reviewer1Map);
					HashMap<String, Object> reviewer2Map = new HashMap<String, Object>();
					reviewer2Map.put("id", team.getReviewer2().getId());
					reviewer2Map.put("name", team.getReviewer2().getFullName());
					reviewer2Map.put("username", team.getReviewer2().getUsername());
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
					studentMap.put("mobileNumber", (student.getMobileNumber() != null?student.getMobileNumber():"-"));
					if (student.getTeam() != null) {
						studentMap.put("teamId", student.getTeam().getId());
						studentMap.put("teamName", student.getTeam().getTeamName());
					}
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
					facultyMap.put("mobileNumber", (faculty.getMobileNumber() != null?faculty.getMobileNumber():"-"));
					List<HashMap<String, Object>> supervisorTeamsList = new ArrayList<HashMap<String, Object>>();
					List<HashMap<String, Object>> reviewer1TeamsList = new ArrayList<HashMap<String, Object>>();
					List<HashMap<String, Object>> reviewer2TeamsList = new ArrayList<HashMap<String, Object>>();
					for (Team team : teams) {
						HashMap<String, Object> teamMap = new HashMap<String, Object>();
						teamMap.put("teamId", team.getId());
						teamMap.put("teamName", team.getTeamName());
						if (faculty.equals(team.getSupervisor())) supervisorTeamsList.add(teamMap);
						if (faculty.equals(team.getReviewer1())) reviewer1TeamsList.add(teamMap);
						if (faculty.equals(team.getReviewer2())) reviewer2TeamsList.add(teamMap);
					}
					facultyMap.put("supervisorTeams", supervisorTeamsList);
					facultyMap.put("reviewer1Teams", reviewer1TeamsList);
					facultyMap.put("reviewer2Teams", reviewer2TeamsList);
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
					taMap.put("mobileNumber", (ta.getMobileNumber() != null?ta.getMobileNumber():"-"));
					List<Schedule> schedules = ScheduleManager.findByTerm(em, term);
					List<HashMap<String, Object>> mySignupList = new ArrayList<HashMap<String, Object>>();
					for (Schedule schedule : schedules) {
						for (Timeslot timeslot : schedule.getTimeslots()) {
							if (timeslot.getTA() != null && timeslot.getTA().equals(ta)) {
								HashMap<String, Object> signupMap = new HashMap<String, Object>();
								signupMap.put("datetime", sdf.format(timeslot.getStartTime()));
								signupMap.put("milestone", schedule.getMilestone().getName());
								if (timeslot.getCurrentBooking() != null) signupMap.put("teamName", timeslot.getCurrentBooking().getTeam().getTeamName());
								mySignupList.add(signupMap);
							}
						}
					}
					taMap.put("mySignups", mySignupList);
					taData.add(taMap);
				}
				
				logger.debug("adminData: " + adminData.size());
				logger.debug("ccData: " + ccData.size());
				logger.debug("teamData: " + teamData.size());
				logger.debug("studentData: " + studentData.size());
				logger.debug("facultyData: " + facultyData.size());
				logger.debug("taData: " + taData.size());
				
				adminJson = gson.toJson(adminData);
				ccJson = gson.toJson(ccData);
				teamJson = gson.toJson(teamData);
				studentJson = gson.toJson(studentData);
				facultyJson = gson.toJson(facultyData);
				taJson = gson.toJson(taData);
				
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
	
	public long getSelectedTermId() {
		return selectedTermId;
	}

	public void setSelectedTermId(long selectedTermId) {
		this.selectedTermId = selectedTermId;
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
	
	public String getAdminJson() {
		return adminJson;
	}

	public void setAdminJson(String adminJson) {
		this.adminJson = adminJson;
	}

	public String getCcJson() {
		return ccJson;
	}

	public void setCcJson(String ccJson) {
		this.ccJson = ccJson;
	}

	public String getTeamJson() {
		return teamJson;
	}

	public void setTeamJson(String teamJson) {
		this.teamJson = teamJson;
	}

	public String getStudentJson() {
		return studentJson;
	}

	public void setStudentJson(String studentJson) {
		this.studentJson = studentJson;
	}

	public String getFacultyJson() {
		return facultyJson;
	}

	public void setFacultyJson(String facultyJson) {
		this.facultyJson = facultyJson;
	}

	public String getTaJson() {
		return taJson;
	}

	public void setTaJson(String taJson) {
		this.taJson = taJson;
	}
	
}
