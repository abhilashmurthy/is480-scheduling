/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package userAction;

import au.com.bytecode.opencsv.CSVReader;
import static com.opensymphony.xwork2.Action.ERROR;
import static com.opensymphony.xwork2.Action.SUCCESS;
import com.opensymphony.xwork2.ActionSupport;
import constant.PresentationType;
import constant.Role;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import javax.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.HashMap;
import javax.persistence.EntityManager;
import util.MiscUtil;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import javax.persistence.EntityTransaction;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import manager.UserManager;
import model.SystemActivityLog;
import model.Team;
import model.Term;
import model.User;
import model.role.Faculty;
import model.role.Student;
import model.role.TA;
import org.apache.struts2.interceptor.ServletRequestAware;
import org.apache.struts2.util.ServletContextAware;
/**
 *
 * @author Prakhar
 */
public class UploadFileAction extends ActionSupport implements ServletContextAware, ServletRequestAware {

    private static HttpServletRequest request;
    private static Logger logger = LoggerFactory.getLogger(UploadFileAction.class);
    private ArrayList<HashMap<String, Object>> data = new ArrayList<HashMap<String, Object>>();
    private HashMap<String, Object> json = new HashMap<String, Object>();
	private File file;
    private String fileContentType;
    private String fileFileName;
    private String filesPath;
    private ServletContext context;
	private String msg;
			
    @Override
    public String execute() throws Exception {
		HttpSession session = request.getSession();
		
		Calendar nowCal = Calendar.getInstance();
		Timestamp now = new Timestamp(nowCal.getTimeInMillis());
		
		SystemActivityLog logItem = new SystemActivityLog();
		logItem.setActivity("Administrator: Upload CSV File");
		logItem.setUser((User)session.getAttribute("user"));
		logItem.setRunTime(now);
		logItem.setSuccess(true);
		
		EntityManager em = null;
		try {
			User user = (User) session.getAttribute("user");
			
            Role activeRole = (Role) session.getAttribute("activeRole");
			if (activeRole.equals(Role.ADMINISTRATOR) || activeRole.equals(Role.COURSE_COORDINATOR)) {
				em = MiscUtil.getEntityManagerInstance();
				//Getting the file
				File csvFile = getFile();

				logger.info("Extracting data from CSV File started");

//				CSVReader reader = new CSVReader(new FileReader(csvFile));

				//<--------------------Validation checks for the csv file---------------------->
				//1. Validate that there are no empty cells in the csv file for the 4 columns
				logger.info("Validating for empty cells in the relevant part of the csv file");
				String[] errorWithEmptyCells = validateEmptyCells(csvFile, logItem);
				if (errorWithEmptyCells[0].equalsIgnoreCase("true")) {
					session.setAttribute("csvMsg", "Wrong Structure! There are blank cells in the csv file. Row Number: " + 
							errorWithEmptyCells[1]);
					logger.error("Error due to blank cells in csv file. Row Number: " + errorWithEmptyCells[1]);
					return SUCCESS;
				}
				
				//2. Validate that every user has a username or a "-"
				logger.info("Validating usernames");
				String[] errorInUsername = validateUsernames(csvFile, logItem);
				if (errorInUsername[0].equalsIgnoreCase("true")) {
					session.setAttribute("csvMsg", "Wrong Username! If a username doesnt exist, please put a '-' symbol."
							+ "Row Number: " + errorWithEmptyCells[1]);
					logger.error("Error with usernames in csv upload. Row Number: " + errorWithEmptyCells[1]);
					return SUCCESS;
				}

				//3. Validate roles of each user
				logger.info("Validating user roles");
				String[] errorInRole = validateRoles(csvFile, logItem);
				if (errorInRole[0].equalsIgnoreCase("true")) {
					session.setAttribute("csvMsg", "Wrong User Roles! Role can only be - TA, Student, Supervisor, Reviewer 1, "
							+ "Reviewer 2. Row Number: " + errorInRole[1]);
					logger.error("Error with user roles in csv upload. Row Number: " + errorInRole[1]);
					return SUCCESS;
				}
				
				//4. Validate for team names
				logger.info("Validating team names");
				String[] errorInTeamName = validateTeamNames(csvFile, logItem);
				if (errorInTeamName[0].equalsIgnoreCase("true")) {
					session.setAttribute("csvMsg", "Wrong Team Name! For TA, please put a '-'. For other roles, put the "
							+ "team name. Row Number: " + errorInTeamName[1]);
					logger.error("Error with team names in csv upload. Row Number: " + errorInTeamName[1]);
					return SUCCESS;
				}
				
				//5. Validate for team presentation type (private, internal, public)
				logger.info("Validating team presentation type (private, internal, public)");
				String[] errorInPresentationType = validatePresentationTypes(csvFile, logItem);
				if (errorInPresentationType[0].equalsIgnoreCase("true")) {
					session.setAttribute("csvMsg", "Wrong Presentation Type! Presentation types can only be Private, Internal "
							+ "or Public (For TA, please put a '-') Row Number: " + errorInPresentationType[1]);
					logger.error("Error with presentation types in csv upload. Row Number: " + errorInPresentationType[1]);
					return SUCCESS;
				}
				
				//6. Validate that TA's are at the start of the file
//				logger.info("Validating order of roles for TA");
//				boolean errorInOrderOfRoles = validateOrderOfRoles(csvFile, logItem);
//				if (errorInOrderOfRoles) {
//					session.setAttribute("csvMsg", "Wrong order of roles! TA's should be placed first in the file");
//					logger.error("Error with order of roles in csv upload");
//					return SUCCESS;
//				} 			

				//5. Validate for term names (should be the same throughout the file)
//				logger.info("Validating term names");
//				String displayName = validateTermNames(csvFile, logItem);
//				if (displayName == null) {
//					msg = "Wrong Term Names! The Academic Year and Semester should be same for all entries";
//					logger.error("Error with term names in csv upload");
//					return SUCCESS;
//				} 	
//				//e.g. If display name is 2013-2014 Term 1
//				if (displayName.length() == 16) {
//					String firstHalf = displayName.substring(0, 5);
//					String secondHalf = displayName.substring(7, 16);
//					displayName = firstHalf + secondHalf;
//				}
//				Term term = null;
//				term = TermManager.getTermByDisplayName(em, displayName);
				
				//Getting the term from the session
				Term tempterm = (Term) session.getAttribute("currentActiveTerm");
				//Getting the term object again from the db (To cater to Detached Entity error)
				Term term = em.find(Term.class, tempterm.getId());
				
				// <------------------------Start Parsing the File to populate DB--------------------------->
				em.getTransaction().begin();

				//1st Part: Creating unique user objects (Except for CC)
				List<User> usersList = createUsers(csvFile, term, em, logItem);
				//If error
				if (usersList == null) {
					msg = "Error with Upload File (Create Users): Escalate to developers!";
					logger.error("Error with Upload File (Create Users)");
					request.setAttribute("error", msg);
					return ERROR;
				}

				//2nd Part: Creating unique team objects
				List<Team> teamsList = createTeams(csvFile, term, em, logItem);
				//If error
				if (teamsList == null) {
					msg = "Error with Upload File (Create Teams): Escalate to developers!";
					logger.error("Error with Upload File (Create Teams)");
					request.setAttribute("error", msg);
					return ERROR;
				}

				//3rd Part: Assigning users (Students & Faculty) to the teams
				boolean result = assignUsersToTeams(csvFile, usersList, teamsList, em, logItem);
				if (!result) {
					msg = "Error with Upload File (Assigning Users to Teams): Escalate to developers!";
					logger.error("Error with Upload File (Assigning Users to Teams)");
					request.setAttribute("error", msg);
					return ERROR;
				}

				EntityTransaction tr = em.getTransaction();
				tr.commit();
				logger.info("Extracting data from CSV completed");
				session.setAttribute("csvMsg", "Success! File has been uploaded!");
				
				logItem.setMessage("CSV File was uploaded successfully");
				
				return SUCCESS;
			} else {
				request.setAttribute("error", "Oops. You're not authorized to access this page!");
				logger.error("User cannot access this page");
				return ERROR;
			}
		} catch (Exception e) {
			logItem.setSuccess(false);
			User userForLog = (User) session.getAttribute("user");
			logItem.setUser(userForLog);
			logItem.setMessage("Error: " + e.getMessage());
			
			logger.error("Exception caught: " + e.getMessage());
			if (MiscUtil.DEV_MODE) {
			   for (StackTraceElement s : e.getStackTrace()) {
				   logger.debug(s.toString());
			   }
			}
			request.setAttribute("error", "Error with Upload File: Escalate to Developers");
			return ERROR;
		} finally {
			if (em != null) {
				if (em.getTransaction().isActive()) em.getTransaction().rollback();
                //Saving job log in database
                if (!em.getTransaction().isActive()) em.getTransaction().begin();
                em.persist(logItem);
                em.getTransaction().commit();
                if (em.isOpen()) em.close();
			}
		}
	}
			
	private static List<User> createUsers(File csvFile, Term term, EntityManager em, SystemActivityLog logItem) {
		HttpSession sessionForLog = request.getSession();
		try {
			//------------------------Creating user objects------------------------
			CSVReader reader = new CSVReader(new FileReader(csvFile));
			String[] nextLineUsers;
			int lineNo = 0;
			List<User> usersList = new ArrayList<User>();
			List<Faculty> facultyList = new ArrayList<Faculty>();
			List<Student> studentList = new ArrayList<Student>();
			List<TA> taList = new ArrayList<TA>();
			//Read one line at a time
			logger.info("Parsing csv file for users");
			while ((nextLineUsers = reader.readNext()) != null) {
				lineNo++;
				//Not reading the 1st line
				if (lineNo != 1) {
					//If there is no user or no team assigned to the user skip the user
					if (!nextLineUsers[2].equalsIgnoreCase("") && !nextLineUsers[2].equalsIgnoreCase("-")) {
						boolean userExists = false;
						if (usersList.size() > 0) {
							for (User userCreated: usersList) {
								if (userCreated.getUsername().equalsIgnoreCase(nextLineUsers[2])) {
									userExists = true;
									break;
								} 
							}
							if (userExists == true) {
								continue;
							}
						}
						if (nextLineUsers[3].equalsIgnoreCase("Student")) {
							//Creating student object
							Student student = new Student(nextLineUsers[2], nextLineUsers[1], null, term);
							studentList.add(student);
							usersList.add(student);
						} else if (nextLineUsers[3].equalsIgnoreCase("Supervisor") || nextLineUsers[3].equalsIgnoreCase("Reviewer 1")
								|| nextLineUsers[3].equalsIgnoreCase("Reviewer 2")) {
							//Creating faculty object
							Faculty faculty = new Faculty(nextLineUsers[2], nextLineUsers[1], null, term);
							facultyList.add(faculty);
							usersList.add(faculty);
						} else if (nextLineUsers[3].equalsIgnoreCase("TA")) {
							//Creating TA object
							TA ta = new TA(nextLineUsers[2], nextLineUsers[1], null, term);
							taList.add(ta);
							usersList.add(ta);
						} 
//						else if (nextLineUsers[3].equalsIgnoreCase("Course Coordinator")) {
//							//Creating course coordinator object
//							User cc = new User(nextLineUsers[2], nextLineUsers[1], null, Role.COURSE_COORDINATOR, term);
//							usersList.add(cc);
//							ccHasBeenAdded = true;
//						} else if (nextLineUsers[3].equalsIgnoreCase("Administrator")) {
//							//Creating administrator object
//							User admin = new User(nextLineUsers[2], nextLineUsers[1], null, Role.ADMINISTRATOR, term);
//							usersList.add(admin);
//						}
					}
				}
			}
			//Persisting the user objects
			logger.info("Persisting users");
			//Persisting students
			if (studentList.size() > 0) {
				for (Student studentObj: studentList) {
					em.persist(studentObj);
				}
			}
			//Persisting faculty
			if (facultyList.size() > 0) {
				for (Faculty facultyObj: facultyList) {
					em.persist(facultyObj);
				}
			}
			//Persisting TA's
			if (taList.size() > 0) {
				for (TA taObj: taList) {
					em.persist(taObj);
				}
			}
			reader.close();
			return usersList;
		} catch (FileNotFoundException e) {
			logItem.setSuccess(false);
			User userForLog = (User) sessionForLog.getAttribute("user");
			logItem.setUser(userForLog);
			logItem.setMessage("Error: " + e.getMessage());
			
			logger.error("Exception caught: " + e.getMessage());
			if (MiscUtil.DEV_MODE) {
			   for (StackTraceElement s : e.getStackTrace()) {
				   logger.debug(s.toString());
			   }
			}
		} catch (IOException e) {
			logItem.setSuccess(false);
			User userForLog = (User) sessionForLog.getAttribute("user");
			logItem.setUser(userForLog);
			logItem.setMessage("Error: " + e.getMessage());
			
			logger.error("Exception caught: " + e.getMessage());
			if (MiscUtil.DEV_MODE) {
			   for (StackTraceElement s : e.getStackTrace()) {
				   logger.debug(s.toString());
			   }
			}
		} catch (Exception e) {
			logItem.setSuccess(false);
			User userForLog = (User) sessionForLog.getAttribute("user");
			logItem.setUser(userForLog);
			logItem.setMessage("Error: " + e.getMessage());
			
			logger.error("Exception caught: " + e.getMessage());
			if (MiscUtil.DEV_MODE) {
			   for (StackTraceElement s : e.getStackTrace()) {
				   logger.debug(s.toString());
			   }
			}
		}
		return null;
	}
	
	private static List<Team> createTeams(File csvFile, Term term, EntityManager em, SystemActivityLog logItem) {
		HttpSession sessionForLog = request.getSession();
		try {
			//-------------------------Creating the teams---------------------------
			CSVReader reader = new CSVReader(new FileReader(csvFile));
			List<Team> teamsList = new ArrayList<Team>();
			String nextLineTeams[];
			int lineNo = 0;
			//Read one line at a time
			logger.info("Parsing csv file for teams");
			while ((nextLineTeams = reader.readNext()) != null) {
				lineNo++;
				//Not reading the 1st line
				if (lineNo != 1) {
					if (!nextLineTeams[0].equalsIgnoreCase("-") && !nextLineTeams[0].equalsIgnoreCase("")) {
						//Checking whether there is already a team with the same name in the teams list 
						boolean teamExists = false;
						if (teamsList.size() > 0) {
							for (Team teamCreated: teamsList) {
								if (teamCreated.getTeamName().equalsIgnoreCase(nextLineTeams[0])) {
									teamExists = true;
									break;
								}
							}
							if (teamExists == true) {
								continue;
							}
						}
						//Creating new team object
						Team team = new Team();
						team.setTeamName(nextLineTeams[0]);
						team.setTerm(term);
						//Get the presentation type of the team
						if (nextLineTeams[4].equalsIgnoreCase("Private")) {
							team.setPresentationType(PresentationType.PRIVATE);
						} else if (nextLineTeams[4].equalsIgnoreCase("Public")) {
							team.setPresentationType(PresentationType.PUBLIC);
						} else if (nextLineTeams[4].equalsIgnoreCase("Internal")) {
							team.setPresentationType(PresentationType.INTERNAL);
						}
						teamsList.add(team);
					}
				}
			}
			reader.close();
			return teamsList;
		} catch (FileNotFoundException e) {
			logItem.setSuccess(false);
			User userForLog = (User) sessionForLog.getAttribute("user");
			logItem.setUser(userForLog);
			logItem.setMessage("Error: " + e.getMessage());
			
			logger.error("Exception caught: " + e.getMessage());
			if (MiscUtil.DEV_MODE) {
			   for (StackTraceElement s : e.getStackTrace()) {
				   logger.debug(s.toString());
			   }
			}
		} catch (IOException e) {
			logItem.setSuccess(false);
			User userForLog = (User) sessionForLog.getAttribute("user");
			logItem.setUser(userForLog);
			logItem.setMessage("Error: " + e.getMessage());
			
			logger.error("Exception caught: " + e.getMessage());
			if (MiscUtil.DEV_MODE) {
			   for (StackTraceElement s : e.getStackTrace()) {
				   logger.debug(s.toString());
			   }
			}
		} catch (Exception e) {
			logItem.setSuccess(false);
			User userForLog = (User) sessionForLog.getAttribute("user");
			logItem.setUser(userForLog);
			logItem.setMessage("Error: " + e.getMessage());
			
			logger.error("Exception caught: " + e.getMessage());
			if (MiscUtil.DEV_MODE) {
			   for (StackTraceElement s : e.getStackTrace()) {
				   logger.debug(s.toString());
			   }
			}
		}
		return null;
	}
		
	private static boolean assignUsersToTeams(File csvFile, List<User> usersList, List<Team> teamsList, EntityManager em, SystemActivityLog logItem) { 
		HttpSession sessionForLog = request.getSession();
		boolean result = false;
		try {
			//-----------------------Assigning users to teams-------------------------
			//Making a students list and a faculty list from the users list
			List<Student> studentsList = new ArrayList<Student>();
			List<Faculty> facultyList = new ArrayList<Faculty>();
			for (User user: usersList) {
				if (user.getRole() == Role.STUDENT) {
					studentsList.add((Student)user);
				} else if (user.getRole() == Role.FACULTY) {
					facultyList.add((Faculty)user);
				}
			}
			
			CSVReader reader = new CSVReader(new FileReader(csvFile));
			String nextLine[];
			int lineNo = 0;
			logger.info("Parsing csv file to assign users to teams");
			HashSet<Student> students = new HashSet<Student>();
			//Read one line at a time
			while ((nextLine = reader.readNext()) != null) {
				lineNo++;
				//Not reading the 1st line
				if (lineNo != 1) {
					if (!nextLine[0].equalsIgnoreCase("-") && !nextLine[0].equalsIgnoreCase("")) {
						for (Team team: teamsList) {
							//Getting the team object
							if (nextLine[0].equalsIgnoreCase(team.getTeamName())) {
								//Getting the user objects for the team
								if (nextLine[3].equalsIgnoreCase("Student")) {
									for (Student student: studentsList) {
										//Adding all the students for a team in a hash set
										if (nextLine[2].equalsIgnoreCase(student.getUsername())) {
											students.add(student);
											break;
										}
									}
								} else if (nextLine[3].equalsIgnoreCase("Supervisor")) {
									//Setting the supervisor for the team
									//When Supervisor for team has not been decided yet
									if (nextLine[2].equalsIgnoreCase("-") || nextLine[2].equalsIgnoreCase("")) {
										team.setSupervisor(getCC(em, facultyList));
									} else { 
										for (Faculty faculty: facultyList) {
											if (nextLine[2].equalsIgnoreCase(faculty.getUsername())) {
												team.setSupervisor(faculty);
												break;
											}
										}
									}
									//After setting the supervisor, we can set the students 
									if (students.size() > 0) {
										team.setMembers(students);
										//Clearing the list of students
										students = new HashSet<Student>();
									}
								} else if (nextLine[3].equalsIgnoreCase("Reviewer 1") || nextLine[3].equalsIgnoreCase("R1")) {
									//Setting the reviewer 1 for the team
									//When Reviewer 1 for team has not been decided yet
									if (nextLine[2].equalsIgnoreCase("-") || nextLine[2].equalsIgnoreCase("")) {
										team.setReviewer1(getCC(em, facultyList));
									} else {
										for (Faculty faculty: facultyList) {
											if (nextLine[2].equalsIgnoreCase(faculty.getUsername())) {
												team.setReviewer1(faculty);
												break;
											}
										}
									}
									//After setting R1, we can set the students 
									if (students.size() > 0) {
										team.setMembers((HashSet<Student>)students);
										//Clearing the list of students
										students = new HashSet<Student>();
									}
								} else if (nextLine[3].equalsIgnoreCase("Reviewer 2") || nextLine[3].equalsIgnoreCase("R2")) {
									//Setting the reviewer 2 for the team
									//When Reviewer 2 for team has not been decided yet
									if (nextLine[2].equalsIgnoreCase("-") || nextLine[2].equalsIgnoreCase("")) {
										team.setReviewer2(getCC(em, facultyList));
									} else { 
										for (Faculty faculty: facultyList) {
											if (nextLine[2].equalsIgnoreCase(faculty.getUsername())) {
												team.setReviewer2(faculty);
												break;
											}
										}
									}
									//After setting R2, we can set the students 
									if (students.size() > 0) {
										team.setMembers((HashSet<Student>)students);
										//Clearing the list of students
										students = new HashSet<Student>();
									}
								}
								break;
								//Setting the students for the team
							}
						}
					}
				}
			}
			reader.close();
			//Persisting the team objects
			logger.info("Persisting teams");
			for (Team teamObj: teamsList) {
				em.persist(teamObj);
			}
			
			logger.info("Parsing csv file to assign teams to students");
			reader = new CSVReader(new FileReader(csvFile));
			String nextLineStudents[];
			lineNo = 0;
			//Read one line at a time
			while ((nextLineStudents = reader.readNext()) != null) {
				lineNo++;
				//Not reading the 1st line
				if (lineNo != 1) {
					if (nextLineStudents[3].equalsIgnoreCase("Student")) {
						if (!nextLineStudents[2].equalsIgnoreCase("-") && !nextLineStudents[2].equalsIgnoreCase("")) {
							if (!nextLineStudents[0].equalsIgnoreCase("-") && !nextLineStudents[0].equalsIgnoreCase("")) {
								boolean found = false;
								for (Student student: studentsList) {
									if (student.getUsername().equalsIgnoreCase(nextLineStudents[2])) {
										for (Team team: teamsList) {
											if (team.getTeamName().equalsIgnoreCase(nextLineStudents[0])) {
												student.setTeam(team);
												found = true;
												break;
											}
										}
										if (found == true) {
											break;
										}
									}
								}
							}
						}
					}
				}
			}
			
			//Persisting student objects and their respective teams
			logger.info("Persisting students with their respective teams");
			for (Student studentObj: studentsList) {
				em.merge(studentObj);
			}
			
			result = true;
			return result;
		} catch (FileNotFoundException e) {
			logItem.setSuccess(false);
			User userForLog = (User) sessionForLog.getAttribute("user");
			logItem.setUser(userForLog);
			logItem.setMessage("Error: " + e.getMessage());
			
			logger.error("Exception caught: " + e.getMessage());
			if (MiscUtil.DEV_MODE) {
			   for (StackTraceElement s : e.getStackTrace()) {
				   logger.debug(s.toString());
			   }
			}
		} catch (IOException e) {
			logItem.setSuccess(false);
			User userForLog = (User) sessionForLog.getAttribute("user");
			logItem.setUser(userForLog);
			logItem.setMessage("Error: " + e.getMessage());
			
			logger.error("Exception caught: " + e.getMessage());
			if (MiscUtil.DEV_MODE) {
			   for (StackTraceElement s : e.getStackTrace()) {
				   logger.debug(s.toString());
			   }
			}
		} catch (Exception e) {
			logItem.setSuccess(false);
			User userForLog = (User) sessionForLog.getAttribute("user");
			logItem.setUser(userForLog);
			logItem.setMessage("Error: " + e.getMessage());
			
			logger.error("Exception caught: " + e.getMessage());
			if (MiscUtil.DEV_MODE) {
			   for (StackTraceElement s : e.getStackTrace()) {
				   logger.debug(s.toString());
			   }
			}
		}
		return result;
	}
	
	private static Faculty getCC(EntityManager em, List<Faculty> facultyList) {
		//Getting the cc from the db to assign it to teams in case a Sup/Rev1/Rev2 has not been assigned to a team
		User ccUser = UserManager.getCourseCoordinator(em);
		for (Faculty fac: facultyList) {
			if (ccUser.getUsername().equalsIgnoreCase(fac.getUsername())) {
				 return fac;
			}
		}
		return null;
	}
	
	//Validating for empty cells in the csv file
	private static String[] validateEmptyCells(File csvFile, SystemActivityLog logItem) {
		HttpSession sessionForLog = request.getSession();
		String[] errorWithEmptyCells = new String[2];
		errorWithEmptyCells[0] = "false";
		errorWithEmptyCells[0] = "0";
		int lineNo = 0;
		try {
			CSVReader reader = new CSVReader(new FileReader(csvFile));
//			int lineNo = 0;
			String[] nextLine;
			while ((nextLine = reader.readNext()) != null) {
				lineNo++;
				if (lineNo != 1) {
					if (!(nextLine[0].length() > 0) || !(nextLine[1].length() > 0) || !(nextLine[2].length() > 0) 
						|| !(nextLine[3].length() > 0) || !(nextLine[4].length() > 0)) {
						errorWithEmptyCells[0] = "true";
						errorWithEmptyCells[1] = String.valueOf(lineNo);
						return errorWithEmptyCells;
					} 
				}
			}
			reader.close();
		} catch (FileNotFoundException e) {
			logItem.setSuccess(false);
			User userForLog = (User) sessionForLog.getAttribute("user");
			logItem.setUser(userForLog);
			logItem.setMessage("Error: " + e.getMessage());
			
			errorWithEmptyCells[0] = "true";
			logger.error("Exception caught: " + e.getMessage());
			if (MiscUtil.DEV_MODE) {
			   for (StackTraceElement s : e.getStackTrace()) {
				   logger.debug(s.toString());
			   }
			}
		} catch (IOException e) {
			logItem.setSuccess(false);
			User userForLog = (User) sessionForLog.getAttribute("user");
			logItem.setUser(userForLog);
			logItem.setMessage("Error: " + e.getMessage());
			
			errorWithEmptyCells[0] = "true";
			logger.error("Exception caught: " + e.getMessage());
			if (MiscUtil.DEV_MODE) {
			   for (StackTraceElement s : e.getStackTrace()) {
				   logger.debug(s.toString());
			   }
			}
		} catch (Exception e) {
			logItem.setSuccess(false);
			User userForLog = (User) sessionForLog.getAttribute("user");
			logItem.setUser(userForLog);
			logItem.setMessage("Error: " + e.getMessage());
			
			errorWithEmptyCells[0] = "true";
			errorWithEmptyCells[1] = String.valueOf(lineNo);
			logger.error("Exception caught: " + e.getMessage());
			if (MiscUtil.DEV_MODE) {
			   for (StackTraceElement s : e.getStackTrace()) {
				   logger.debug(s.toString());
			   }
			}
		}
		return errorWithEmptyCells;
	}
	
	
	//Validating usernames
	private static String[] validateUsernames(File csvFile, SystemActivityLog logItem) {
		HttpSession sessionForLog = request.getSession();
		String[] errorInUsername = new String[2];
		errorInUsername[0] = "false";
		errorInUsername[0] = "0";
		int lineNo = 0;
		try {
			CSVReader reader = new CSVReader(new FileReader(csvFile));
			String[] nextLine;
			while ((nextLine = reader.readNext()) != null) {
				lineNo++;
				if (lineNo != 1) {
					if (!(nextLine[2].length() > 0)) {
						errorInUsername[0] = "true";
						errorInUsername[1] = String.valueOf(lineNo);
						return errorInUsername;
					}
				}
			}
			reader.close();
		} catch (FileNotFoundException e) {
			logItem.setSuccess(false);
			User userForLog = (User) sessionForLog.getAttribute("user");
			logItem.setUser(userForLog);
			logItem.setMessage("Error: " + e.getMessage());
			
			errorInUsername[0] = "true";
			logger.error("Exception caught: " + e.getMessage());
			if (MiscUtil.DEV_MODE) {
			   for (StackTraceElement s : e.getStackTrace()) {
				   logger.debug(s.toString());
			   }
			}
		} catch (IOException e) {
			logItem.setSuccess(false);
			User userForLog = (User) sessionForLog.getAttribute("user");
			logItem.setUser(userForLog);
			logItem.setMessage("Error: " + e.getMessage());
			
			errorInUsername[0] = "true";
			logger.error("Exception caught: " + e.getMessage());
			if (MiscUtil.DEV_MODE) {
			   for (StackTraceElement s : e.getStackTrace()) {
				   logger.debug(s.toString());
			   }
			}
		} catch (Exception e) {
			logItem.setSuccess(false);
			User userForLog = (User) sessionForLog.getAttribute("user");
			logItem.setUser(userForLog);
			logItem.setMessage("Error: " + e.getMessage());
			
			errorInUsername[0] = "true";
			errorInUsername[1] = String.valueOf(lineNo);
			logger.error("Exception caught: " + e.getMessage());
			if (MiscUtil.DEV_MODE) {
			   for (StackTraceElement s : e.getStackTrace()) {
				   logger.debug(s.toString());
			   }
			}
		}
		return errorInUsername;
	}
	
	
	//Validating user roles
	private static String[] validateRoles(File csvFile, SystemActivityLog logItem) {
		HttpSession sessionForLog = request.getSession();
		String[] errorInRole = new String[2];
		errorInRole[0] = "false";
		errorInRole[1] = "0";
		int lineNo = 0;
		try {
			CSVReader reader = new CSVReader(new FileReader(csvFile));
			String[] nextLine;
			while ((nextLine = reader.readNext()) != null) {
				lineNo++;
				if (lineNo != 1) {
					//Provided there is a username
					if (nextLine[2].length() > 0) {
						if (!nextLine[3].equalsIgnoreCase("TA") && !nextLine[3].equalsIgnoreCase("Student")
							&& !nextLine[3].equalsIgnoreCase("Supervisor") && !nextLine[3].equalsIgnoreCase("Reviewer 1") 
							&& !nextLine[3].equalsIgnoreCase("Reviewer 2")) {
								errorInRole[0] = "true";
								errorInRole[1] = String.valueOf(lineNo);
								return errorInRole;
						}
					}
				}
			}
			reader.close();
		} catch (FileNotFoundException e) {
			logItem.setSuccess(false);
			User userForLog = (User) sessionForLog.getAttribute("user");
			logItem.setUser(userForLog);
			logItem.setMessage("Error: " + e.getMessage());
			
			errorInRole[0] = "true";
			logger.error("Exception caught: " + e.getMessage());
			if (MiscUtil.DEV_MODE) {
			   for (StackTraceElement s : e.getStackTrace()) {
				   logger.debug(s.toString());
			   }
			}
		} catch (IOException e) {
			logItem.setSuccess(false);
			User userForLog = (User) sessionForLog.getAttribute("user");
			logItem.setUser(userForLog);
			logItem.setMessage("Error: " + e.getMessage());
			
			errorInRole[0] = "true";
			logger.error("Exception caught: " + e.getMessage());
			if (MiscUtil.DEV_MODE) {
			   for (StackTraceElement s : e.getStackTrace()) {
				   logger.debug(s.toString());
			   }
			}
		} catch (Exception e) {
			logItem.setSuccess(false);
			User userForLog = (User) sessionForLog.getAttribute("user");
			logItem.setUser(userForLog);
			logItem.setMessage("Error: " + e.getMessage());
			
			errorInRole[0] = "true";
			errorInRole[1] = String.valueOf(lineNo);
			logger.error("Exception caught: " + e.getMessage());
			if (MiscUtil.DEV_MODE) {
			   for (StackTraceElement s : e.getStackTrace()) {
				   logger.debug(s.toString());
			   }
			}
		}
		return errorInRole;
	}
	
	//Validating team names
	private static String[] validateTeamNames(File csvFile, SystemActivityLog logItem) {
		HttpSession sessionForLog = request.getSession();
		String[] errorInTeamName = new String[2];
		errorInTeamName[0] = "false";
		errorInTeamName[1] = "0";
		int lineNo = 0;
		try {
			CSVReader reader = new CSVReader(new FileReader(csvFile));
			String[] nextLine;
			while ((nextLine = reader.readNext()) != null) {
				lineNo++;
				if (lineNo != 1) {
					if (nextLine[3].equalsIgnoreCase("TA")) {
						if (!nextLine[0].equalsIgnoreCase("-")) {
							errorInTeamName[0] = "true";
							errorInTeamName[1] = String.valueOf(lineNo);
							return errorInTeamName;
						}
					} else {
						if (nextLine[0].equalsIgnoreCase("") || nextLine[0].equalsIgnoreCase("-")) {
							errorInTeamName[0] = "true";
							errorInTeamName[1] = String.valueOf(lineNo);
							return errorInTeamName;
						}
					}
				}
			}
			reader.close();
		} catch (FileNotFoundException e) {
			logItem.setSuccess(false);
			User userForLog = (User) sessionForLog.getAttribute("user");
			logItem.setUser(userForLog);
			logItem.setMessage("Error: " + e.getMessage());
			
			errorInTeamName[0] = "true";
			logger.error("Exception caught: " + e.getMessage());
			if (MiscUtil.DEV_MODE) {
			   for (StackTraceElement s : e.getStackTrace()) {
				   logger.debug(s.toString());
			   }
			}
		} catch (IOException e) {
			logItem.setSuccess(false);
			User userForLog = (User) sessionForLog.getAttribute("user");
			logItem.setUser(userForLog);
			logItem.setMessage("Error: " + e.getMessage());
			
			errorInTeamName[0] = "true";
			logger.error("Exception caught: " + e.getMessage());
			if (MiscUtil.DEV_MODE) {
			   for (StackTraceElement s : e.getStackTrace()) {
				   logger.debug(s.toString());
			   }
			}
		} catch (Exception e) {
			logItem.setSuccess(false);
			User userForLog = (User) sessionForLog.getAttribute("user");
			logItem.setUser(userForLog);
			logItem.setMessage("Error: " + e.getMessage());
			
			errorInTeamName[0] = "true";
			errorInTeamName[1] = String.valueOf(lineNo);
			logger.error("Exception caught: " + e.getMessage());
			if (MiscUtil.DEV_MODE) {
			   for (StackTraceElement s : e.getStackTrace()) {
				   logger.debug(s.toString());
			   }
			}
		}
		return errorInTeamName;
	}
	
	//Validating presentation types (private, internal or public)
	private static String[] validatePresentationTypes(File csvFile, SystemActivityLog logItem) {
		HttpSession sessionForLog = request.getSession();
		String[] errorInPresentationType = new String[2];
		errorInPresentationType[0] = "false";
		errorInPresentationType[1] = "0";
		int lineNo = 0;
		try {
			CSVReader reader = new CSVReader(new FileReader(csvFile));
			String[] nextLine;
			while ((nextLine = reader.readNext()) != null) {
				lineNo++;
				if (lineNo != 1) {
					if (nextLine[3].equalsIgnoreCase("TA")) {
						if (!nextLine[4].equalsIgnoreCase("-")) {
							errorInPresentationType[0] = "true";
							errorInPresentationType[1] = String.valueOf(lineNo);
							return errorInPresentationType;
						}
					} else {
						if (!nextLine[4].equalsIgnoreCase("Private") && !nextLine[4].equalsIgnoreCase("Internal") &&
								!nextLine[4].equalsIgnoreCase("Public")) {
							errorInPresentationType[0] = "true";
							errorInPresentationType[1] = String.valueOf(lineNo);
							return errorInPresentationType;
						}
					}
				}
			}
			reader.close();
		} catch (FileNotFoundException e) {
			logItem.setSuccess(false);
			User userForLog = (User) sessionForLog.getAttribute("user");
			logItem.setUser(userForLog);
			logItem.setMessage("Error: " + e.getMessage());
			
			errorInPresentationType[0] = "true";
			logger.error("Exception caught: " + e.getMessage());
			if (MiscUtil.DEV_MODE) {
			   for (StackTraceElement s : e.getStackTrace()) {
				   logger.debug(s.toString());
			   }
			}
		} catch (IOException e) {
			logItem.setSuccess(false);
			User userForLog = (User) sessionForLog.getAttribute("user");
			logItem.setUser(userForLog);
			logItem.setMessage("Error: " + e.getMessage());
			
			errorInPresentationType[0] = "true";
			logger.error("Exception caught: " + e.getMessage());
			if (MiscUtil.DEV_MODE) {
			   for (StackTraceElement s : e.getStackTrace()) {
				   logger.debug(s.toString());
			   }
			}
		} catch (Exception e) {
			logItem.setSuccess(false);
			User userForLog = (User) sessionForLog.getAttribute("user");
			logItem.setUser(userForLog);
			logItem.setMessage("Error: " + e.getMessage());
			
			errorInPresentationType[0] = "true";
			errorInPresentationType[1] = String.valueOf(lineNo);
			logger.error("Exception caught: " + e.getMessage());
			if (MiscUtil.DEV_MODE) {
			   for (StackTraceElement s : e.getStackTrace()) {
				   logger.debug(s.toString());
			   }
			}
		}
		return errorInPresentationType;
	}
	
	//Validating order of roles
//	private static boolean validateOrderOfRoles(File csvFile, SystemActivityLog logItem) {
//		HttpSession sessionForLog = request.getSession();
//		boolean errorInOrderOfRoles = false;
//		try {
//			CSVReader reader = new CSVReader(new FileReader(csvFile));
//			int lineNo = 0;
//			String[] nextLine;
//			int taCount = 0;
//			//Getting the total number of admin, cc and ta's
//			while ((nextLine = reader.readNext()) != null) {
//				lineNo++;
//				if (lineNo != 1) {
//					if (nextLine[3].equalsIgnoreCase("TA")) {
//						taCount++;
//					}
//				}
//			}
//			reader.close();
//			
//			//Now validating order of roles
//			reader = new CSVReader(new FileReader(csvFile));
//			lineNo = 0;
//			int i = 0;
//			while (((nextLine = reader.readNext()) != null) && (i < taCount)) {
//				lineNo++;
//				if (lineNo != 1) {
//					if (!nextLine[3].equalsIgnoreCase("Administrator") && !nextLine[3].equalsIgnoreCase("Course Coordinator")
//						&& !nextLine[3].equalsIgnoreCase("TA")) {
//						errorInOrderOfRoles = true;
//						return errorInOrderOfRoles;
//					}
//					i++;
//				}
//			}
//			reader.close();
//		} catch (FileNotFoundException e) {
//			logItem.setSuccess(false);
//			User userForLog = (User) sessionForLog.getAttribute("user");
//			logItem.setUser(userForLog);
//			logItem.setMessage("Error: " + e.getMessage());
			
//			errorInOrderOfRoles = true;
//			logger.error("Exception caught: " + e.getMessage());
//			if (MiscUtil.DEV_MODE) {
//			   for (StackTraceElement s : e.getStackTrace()) {
//				   logger.debug(s.toString());
//			   }
//			}
//		} catch (IOException e) {
//			logItem.setSuccess(false);
//			User userForLog = (User) sessionForLog.getAttribute("user");
//			logItem.setUser(userForLog);
//			logItem.setMessage("Error: " + e.getMessage());
	
//			errorInOrderOfRoles = true;
//			logger.error("Exception caught: " + e.getMessage());
//			if (MiscUtil.DEV_MODE) {
//			   for (StackTraceElement s : e.getStackTrace()) {
//				   logger.debug(s.toString());
//			   }
//			}
//		} catch (Exception e) {
//			logItem.setSuccess(false);
//			User userForLog = (User) sessionForLog.getAttribute("user");
//			logItem.setUser(userForLog);
//			logItem.setMessage("Error: " + e.getMessage());
//			
//			errorInOrderOfRoles = true;
//			logger.error("Exception caught: " + e.getMessage());
//			if (MiscUtil.DEV_MODE) {
//			   for (StackTraceElement s : e.getStackTrace()) {
//				   logger.debug(s.toString());
//			   }
//			}
//		}
//		return errorInOrderOfRoles;
//	}
	
	//Validating term names
//	private static String validateTermNames(File csvFile, SystemActivityLog logItem) {
//		HttpSession sessionForLog = request.getSession();
//		try {
//			CSVReader reader = new CSVReader(new FileReader(csvFile));
//			int lineNo = 0;
//			String[] nextLineTerm;
//			String displayName = "";
//			boolean termInvalid = false;
//			while ((nextLineTerm = reader.readNext()) != null) {
//				lineNo++;
//				if (lineNo != 1) {
//					if (lineNo == 2) {
//						displayName = nextLineTerm[0] + " " + nextLineTerm[1];
//					} else {
//						String name = nextLineTerm[0] + " " + nextLineTerm[1];
//						if (!name.equalsIgnoreCase(displayName)) {
//							termInvalid = true;
//							return null;
//						}
//					}
//				}
//			}
//			return displayName;
//		} catch (FileNotFoundException e) {
//			logItem.setSuccess(false);
//			User userForLog = (User) sessionForLog.getAttribute("user");
//			logItem.setUser(userForLog);
//			logItem.setMessage("Error: " + e.getMessage());
//			
//			logger.error("Exception caught: " + e.getMessage());
//			if (MiscUtil.DEV_MODE) {
//			   for (StackTraceElement s : e.getStackTrace()) {
//				   logger.debug(s.toString());
//			   }
//			}
//		} catch (IOException e) {
//			logItem.setSuccess(false);
//			User userForLog = (User) sessionForLog.getAttribute("user");
//			logItem.setUser(userForLog);
//			logItem.setMessage("Error: " + e.getMessage());
//			
//			logger.error("Exception caught: " + e.getMessage());
//			if (MiscUtil.DEV_MODE) {
//			   for (StackTraceElement s : e.getStackTrace()) {
//				   logger.debug(s.toString());
//			   }
//			}
//		} catch (Exception e) {
//			logItem.setSuccess(false);
//			User userForLog = (User) sessionForLog.getAttribute("user");
//			logItem.setUser(userForLog);
//			logItem.setMessage("Error: " + e.getMessage());
//			
//			logger.error("Exception caught: " + e.getMessage());
//			if (MiscUtil.DEV_MODE) {
//			   for (StackTraceElement s : e.getStackTrace()) {
//				   logger.debug(s.toString());
//			   }
//			}
//		}
//		return null;
//	}
	
	
	//Getter and Setter Methods
    public ArrayList<HashMap<String, Object>> getData() {
        return data;
    }

    public void setData(ArrayList<HashMap<String, Object>> data) {
        this.data = data;
    }

    public HashMap<String, Object> getJson() {
        return json;
    }

    public void setJson(HashMap<String, Object> json) {
        this.json = json;
    }

    public HttpServletRequest getRequest() {
        return request;
    }

    public void setServletRequest(HttpServletRequest hsr) {
        this.request = hsr;
    }
	
	public File getFile() {
        return file;
    }
 
    public void setFile(File file) {
        this.file = file;
    }
 
    public String getFileContentType() {
        return fileContentType;
    }
 
    public void setFileContentType(String fileContentType) {
        this.fileContentType = fileContentType;
    }
 
    public String getFileFileName() {
        return fileFileName;
    }
 
    public void setFileFileName(String fileFileName) {
        this.fileFileName = fileFileName;
    }
 
    public void setFilesPath(String filesPath) {
        this.filesPath = filesPath;
    }
 
    @Override
    public void setServletContext(ServletContext ctx) {
        this.context=ctx;
    }

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}
} //end of class