/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package userAction;

import au.com.bytecode.opencsv.CSVReader;
import static com.opensymphony.xwork2.Action.SUCCESS;
import com.opensymphony.xwork2.ActionSupport;
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
import java.util.HashSet;
import java.util.List;
import javax.servlet.ServletContext;
import manager.TermManager;
import manager.UserManager;
import model.Team;
import model.Term;
import model.User;
import model.role.Faculty;
import model.role.Student;
import model.role.TA;
import org.apache.struts2.util.ServletContextAware;
/**
 *
 * @author Prakhar
 */
public class UploadFileAction extends ActionSupport implements ServletContextAware {

    private HttpServletRequest request;
    private static Logger logger = LoggerFactory.getLogger(UploadFileAction.class);
    private ArrayList<HashMap<String, Object>> data = new ArrayList<HashMap<String, Object>>();
    private HashMap<String, Object> json = new HashMap<String, Object>();
	private File file;
    private String fileContentType;
    private String fileFileName;
    private String filesPath;
    private ServletContext context;
	
    @Override
    public String execute() throws Exception {
		EntityManager em = null;
		try {
			em = MiscUtil.getEntityManagerInstance();
			
//			Getting the Term
//			long termId = Long.parseLong(request.getParameter("termChosen"));
//			Term term = em.find(Term.class, termId);
			
			//Getting the file
			File csvFile = getFile();
			try {
				logger.info("Extracting data from CSV File started");
//				em.getTransaction().begin();
				csvUpload(csvFile, em);
//				em.getTransaction().commit();
				logger.info("Extracting data from CSV completed");
			} catch (Exception e) {
				logger.error("CSV Parsing Error:");
				logger.error(e.getMessage());
				for (StackTraceElement s : e.getStackTrace()) {
					logger.debug(s.toString());
				}
				em.getTransaction().rollback();
			} finally {
				if (em != null && em.getTransaction().isActive()) {
					em.getTransaction().rollback();
				}
				if (em != null && em.isOpen()) {
					em.close();
				}
			}
		} catch (Exception e) {
			logger.error("Exception caught: " + e.getMessage());
			if (MiscUtil.DEV_MODE) {
			   for (StackTraceElement s : e.getStackTrace()) {
				   logger.debug(s.toString());
			   }
			}
//			json.put("exception", true);
//			json.put("message", "Error with UploadFileAction: Escalate to developers!");
        } finally {
			if (em != null && em.getTransaction().isActive()) em.getTransaction().rollback();
			if (em != null && em.isOpen()) em.close();
		}
		return SUCCESS;
	} //end of execute function
			
	private static void csvUpload(File csvFile, EntityManager em) {
		try {
			CSVReader reader = new CSVReader(new FileReader(csvFile));

			//<--------------------Validation checks for the csv file---------------------->
			//1. Validate that every user has a username or a "-"
			logger.info("Validating usernames");
			boolean errorInUsername = validateUsernames(csvFile);
			if (errorInUsername) {
				System.out.println("Error with Usernames! If a username doesnt exist, please put a '-' symbol");
			}
			
			//2. Validate roles of each user
			logger.info("Validating user roles");
			boolean errorInRole = validateRoles(csvFile);
			if (errorInRole) {
				System.out.println("Error with User Roles! Role can only be Administrator, Course Coordinator, " +
						"TA, Student, Supervisor, Reviewer 1, Reviewer 2");
			}
			
			//3. Validate for team names
			logger.info("Validating team names");
			boolean errorInTeamName = validateTeamNames(csvFile);
			if (errorInTeamName) {
				System.out.println("Error with Team Name! For Administrator, Course Coordinator and TA, please"
						+ " put a '-'. For other roles, put the team name");
			}
			
			//4. Validate that Admin, CC and TA's are at the start of the file
			logger.info("Validating order of roles (Admin, cc and TA)");
			boolean errorInOrderOfRoles = validateOrderOfRoles(csvFile);
			if (errorInOrderOfRoles) {
				System.out.println("Error with the order of roles! Administrator, Course Coordinator, TA's "
						+ "should be placed first in the file");
			} 			
			
			//5. Validate for term names (should be the same throughout the file)
			logger.info("Validating term names");
			String displayName = validateTermNames(csvFile);
			if (displayName == null) {
				System.out.println("Error with Term Names! The Academic Year and Semester should be same for all entries");
			} 			
			Term term = null;
			term = TermManager.getTermByDisplayName(em, displayName);
			
			
			// <------------------------Start Parsing the File to populate DB--------------------------->
			//1st Part: Creating unique user objects (Except for CC)
			List<User> usersList = createUsers(csvFile, term, em);
			
			//2nd Part: Creating unique team objects
			List<Team> teamsList = createTeams(csvFile, term, em);
			
			//3rd Part: Assigning users (Students & Faculty) to the teams
			assignUsersToTeams(csvFile, usersList, teamsList, em);
			
		} catch (FileNotFoundException e) {
			logger.error("Exception caught: " + e.getMessage());
			if (MiscUtil.DEV_MODE) {
			   for (StackTraceElement s : e.getStackTrace()) {
				   logger.debug(s.toString());
			   }
			}
		} catch (IOException e) {
			logger.error("Exception caught: " + e.getMessage());
			if (MiscUtil.DEV_MODE) {
			   for (StackTraceElement s : e.getStackTrace()) {
				   logger.debug(s.toString());
			   }
			}
		}
	} //end of csvUpload function
	
	private static List<User> createUsers(File csvFile, Term term, EntityManager em) {
		try {
			//------------------------Creating user objects------------------------
			CSVReader reader = new CSVReader(new FileReader(csvFile));
			String[] nextLineUsers;
			int lineNo = 0;
			List<User> usersList = new ArrayList<User>();
			boolean isCoordinator = false;
			boolean ccHasBeenAdded = false;
			//Read one line at a time
			logger.info("Parsing csv file for users");
			while ((nextLineUsers = reader.readNext()) != null) {
				lineNo++;
				//Not reading the 1st line
				if (lineNo != 1) {
					//If there is no user or no team assigned to the user skip the user
					if (!nextLineUsers[4].equalsIgnoreCase("") && !nextLineUsers[4].equalsIgnoreCase("-")) {
						boolean userExists = false;
						if (usersList.size() > 0) {
							for (User userCreated: usersList) {
								if (userCreated.getUsername().equalsIgnoreCase(nextLineUsers[4])) {
									//Only a user who is a course coordinator can also be a faculty
									User ccObj = UserManager.getCourseCoordinator(em);
									//If cc doesnt exist in db, getting cc from the csv file
									if (ccObj == null) {
										for (User cc : usersList) {
											if (cc.getRole().equals(Role.COURSE_COORDINATOR)) {
												ccObj = new User(cc.getUsername(), cc.getFullName(), null, Role.COURSE_COORDINATOR, term);
												break;
											}
										}
									}
									if (!ccObj.getUsername().equalsIgnoreCase(userCreated.getUsername())) {
										userExists = true;
										break;
									} else {
										if (!isCoordinator) {
											//Creating course coordinator object
											if (nextLineUsers[5].equalsIgnoreCase("Supervisor") || nextLineUsers[5].equalsIgnoreCase("Reviewer 1")
													|| nextLineUsers[5].equalsIgnoreCase("Reviewer 2")) {
												//Creating faculty object
												Faculty faculty = new Faculty(nextLineUsers[4], nextLineUsers[3], null, term);
												usersList.add(faculty);
											} else {
												//When user as faculty has been added first and then user as cc is added
												//First check whether user as cc has already been added or not
												if (ccHasBeenAdded == false) {
													User cc = new User(nextLineUsers[4], nextLineUsers[3], null, Role.COURSE_COORDINATOR, term);
													usersList.add(cc);
												}
											}
											//This variable is true after the course coordinator has been added twice
											userExists = true;
											isCoordinator = true;
											break;
										} else {
											//This will take place when a user has been added twice as cc and faculty
											userExists = true;
											break;
										}
									}
								}
							}
							if (userExists == true) {
								continue;
							}
						}
						if (nextLineUsers[5].equalsIgnoreCase("Student")) {
							//Creating student object
							Student student = new Student(nextLineUsers[4], nextLineUsers[3], null, term);
							usersList.add(student);
						} else if (nextLineUsers[5].equalsIgnoreCase("Supervisor") || nextLineUsers[5].equalsIgnoreCase("Reviewer 1")
								|| nextLineUsers[5].equalsIgnoreCase("Reviewer 2")) {
							//Creating faculty object
							Faculty faculty = new Faculty(nextLineUsers[4], nextLineUsers[3], null, term);
							usersList.add(faculty);
						} else if (nextLineUsers[5].equalsIgnoreCase("TA")) {
							//Creating TA object
							TA ta = new TA(nextLineUsers[4], nextLineUsers[3], null, term);
							usersList.add(ta);
						} else if (nextLineUsers[5].equalsIgnoreCase("Course Coordinator")) {
							//Creating course coordinator object
							User cc = new User(nextLineUsers[4], nextLineUsers[3], null, Role.COURSE_COORDINATOR, term);
							usersList.add(cc);
							ccHasBeenAdded = true;
						} else if (nextLineUsers[5].equalsIgnoreCase("Administrator")) {
							//Creating administrator object
							User admin = new User(nextLineUsers[4], nextLineUsers[3], null, Role.ADMINISTRATOR, term);
							usersList.add(admin);
						}
					}
				}
			}
			//Persisting the user objects
//			logger.info("Persisting users");
//			for (User userObj: usersList) {
//				em.persist(userObj);
//			}
			reader.close();
			return usersList;
		} catch (FileNotFoundException e) {
			logger.error("Exception caught: " + e.getMessage());
			if (MiscUtil.DEV_MODE) {
			   for (StackTraceElement s : e.getStackTrace()) {
				   logger.debug(s.toString());
			   }
			}
		} catch (IOException e) {
			logger.error("Exception caught: " + e.getMessage());
			if (MiscUtil.DEV_MODE) {
			   for (StackTraceElement s : e.getStackTrace()) {
				   logger.debug(s.toString());
			   }
			}
		}
		return null;
	}
	
	private static List<Team> createTeams(File csvFile, Term term, EntityManager em) {
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
					if (!nextLineTeams[2].equalsIgnoreCase("-") && !nextLineTeams[2].equalsIgnoreCase("")) {
						//Checking whether there is already a team with the same name in the teams list 
						boolean teamExists = false;
						if (teamsList.size() > 0) {
							for (Team teamCreated: teamsList) {
								if (teamCreated.getTeamName().equalsIgnoreCase(nextLineTeams[2])) {
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
						team.setTeamName(nextLineTeams[2]);
						team.setTerm(term);
						teamsList.add(team);
					}
				}
			}
			reader.close();
			return teamsList;
		} catch (FileNotFoundException e) {
			logger.error("Exception caught: " + e.getMessage());
			if (MiscUtil.DEV_MODE) {
			   for (StackTraceElement s : e.getStackTrace()) {
				   logger.debug(s.toString());
			   }
			}
		} catch (IOException e) {
			logger.error("Exception caught: " + e.getMessage());
			if (MiscUtil.DEV_MODE) {
			   for (StackTraceElement s : e.getStackTrace()) {
				   logger.debug(s.toString());
			   }
			}
		}
		return null;
	}
		
	private static void assignUsersToTeams(File csvFile, List<User> usersList, List<Team> teamsList, EntityManager em) { 
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
					if (!nextLine[2].equalsIgnoreCase("-") && !nextLine[2].equalsIgnoreCase("")) {
						for (Team team: teamsList) {
							//Getting the team object
							if (nextLine[2].equalsIgnoreCase(team.getTeamName())) {
								//Getting the user objects for the team
								if (nextLine[5].equalsIgnoreCase("Student")) {
									for (Student student: studentsList) {
										//Adding all the students for a team in a hash set
										if (nextLine[4].equalsIgnoreCase(student.getUsername())) {
											students.add(student);
											break;
										}
									}
								} else if (nextLine[5].equalsIgnoreCase("Supervisor")) {
									//Setting the supervisor for the team
									for (Faculty faculty: facultyList) {
										if (nextLine[4].equalsIgnoreCase(faculty.getUsername())) {
											team.setSupervisor(faculty);
											break;
										}
									}
									//After setting the supervisor, we can set the students 
									if (students.size() > 0) {
										team.setMembers(students);
										//Clearing the list of students
										students.clear();
									}
								} else if (nextLine[5].equalsIgnoreCase("Reviewer 1") || nextLine[5].equalsIgnoreCase("R1")) {
									//Setting the reviewer 1 for the team
									for (Faculty faculty: facultyList) {
										if (nextLine[4].equalsIgnoreCase(faculty.getUsername())) {
											team.setReviewer1(faculty);
											break;
										}
									}
									//After setting R1, we can set the students 
									if (students.size() > 0) {
										team.setMembers((HashSet<Student>)students);
										//Clearing the list of students
										students.clear();
									}
								} else if (nextLine[5].equalsIgnoreCase("Reviewer 2") || nextLine[5].equalsIgnoreCase("R2")) {
									//Setting the reviewer 2 for the team
									for (Faculty faculty: facultyList) {
										if (nextLine[4].equalsIgnoreCase(faculty.getUsername())) {
											team.setReviewer2(faculty);
											break;
										}
									}
									//After setting R2, we can set the students 
									if (students.size() > 0) {
										team.setMembers((HashSet<Student>)students);
										//Clearing the list of students
										students.clear();
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
//			logger.info("Persisting teams");
//			for (Team teamObj: teamsList) {
//				em.persist(teamObj);
//			}
			for (Team team: teamsList) {
				System.out.println(team.getTeamName() + ":");
				HashSet<Student> members = (HashSet<Student>) team.getMembers();
				for (Student stu: members) {
					System.out.println(stu.getFullName());
				}
				System.out.println("Supervisor:" + team.getSupervisor().getFullName());
				System.out.println("Reviewer 1:" + team.getReviewer1().getFullName());
				System.out.println("Reviewer 2:" + team.getReviewer2().getFullName());
				System.out.println();
			}
		} catch (FileNotFoundException e) {
			logger.error("Exception caught: " + e.getMessage());
			if (MiscUtil.DEV_MODE) {
			   for (StackTraceElement s : e.getStackTrace()) {
				   logger.debug(s.toString());
			   }
			}
		} catch (IOException e) {
			logger.error("Exception caught: " + e.getMessage());
			if (MiscUtil.DEV_MODE) {
			   for (StackTraceElement s : e.getStackTrace()) {
				   logger.debug(s.toString());
			   }
			}
		}
	}
	
	
	//Validating usernames
	private static boolean validateUsernames(File csvFile) {
		boolean errorInUsername = false;
		try {
			CSVReader reader = new CSVReader(new FileReader(csvFile));
			int lineNo = 0;
			String[] nextLine;
			while ((nextLine = reader.readNext()) != null) {
				lineNo++;
				if (lineNo != 1) {
					if (!(nextLine[4].length() > 0)) {
						errorInUsername = true;
						return errorInUsername;
					}
				}
			}
			reader.close();
		} catch (FileNotFoundException e) {
			logger.error("Exception caught: " + e.getMessage());
			if (MiscUtil.DEV_MODE) {
			   for (StackTraceElement s : e.getStackTrace()) {
				   logger.debug(s.toString());
			   }
			}
		} catch (IOException e) {
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
	private static boolean validateRoles(File csvFile) {
		boolean errorInRole = false;
		try {
			CSVReader reader = new CSVReader(new FileReader(csvFile));
			int lineNo = 0;
			String[] nextLine;
			while ((nextLine = reader.readNext()) != null) {
				lineNo++;
				if (lineNo != 1) {
					//Provided there is a username
					if (nextLine[4].length() > 0) {
						if (!nextLine[5].equalsIgnoreCase("Administrator") && !nextLine[5].equalsIgnoreCase("Course Coordinator")
							&& !nextLine[5].equalsIgnoreCase("TA") && !nextLine[5].equalsIgnoreCase("Student")
							&& !nextLine[5].equalsIgnoreCase("Supervisor") && !nextLine[5].equalsIgnoreCase("Reviewer 1") 
							&& !nextLine[5].equalsIgnoreCase("Reviewer 2")) {
								errorInRole = true;
								return errorInRole;
						}
					}
				}
			}
			reader.close();
		} catch (FileNotFoundException e) {
			logger.error("Exception caught: " + e.getMessage());
			if (MiscUtil.DEV_MODE) {
			   for (StackTraceElement s : e.getStackTrace()) {
				   logger.debug(s.toString());
			   }
			}
		} catch (IOException e) {
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
	private static boolean validateTeamNames(File csvFile) {
		boolean errorInTeamName = false;
		try {
			CSVReader reader = new CSVReader(new FileReader(csvFile));
			int lineNo = 0;
			String[] nextLine;
			while ((nextLine = reader.readNext()) != null) {
				lineNo++;
				if (lineNo != 1) {
					if (nextLine[5].equalsIgnoreCase("Administrator") || nextLine[5].equalsIgnoreCase("Course Coordinator") 
							|| nextLine[5].equalsIgnoreCase("TA")) {
						if (!nextLine[2].equalsIgnoreCase("-")) {
							errorInTeamName = true;
							return errorInTeamName;
						}
					} else {
						if (nextLine[2].equalsIgnoreCase("") || nextLine[2].equalsIgnoreCase("-")) {
							errorInTeamName = true;
							return errorInTeamName;
						}
					}
				}
			}
			reader.close();
		} catch (FileNotFoundException e) {
			logger.error("Exception caught: " + e.getMessage());
			if (MiscUtil.DEV_MODE) {
			   for (StackTraceElement s : e.getStackTrace()) {
				   logger.debug(s.toString());
			   }
			}
		} catch (IOException e) {
			logger.error("Exception caught: " + e.getMessage());
			if (MiscUtil.DEV_MODE) {
			   for (StackTraceElement s : e.getStackTrace()) {
				   logger.debug(s.toString());
			   }
			}
		}
		return errorInTeamName;
	}
	
	//Validating order of roles
	private static boolean validateOrderOfRoles(File csvFile) {
		boolean errorInOrderOfRoles = false;
		try {
			CSVReader reader = new CSVReader(new FileReader(csvFile));
			int lineNo = 0;
			String[] nextLine;
			int adminCount = 0, ccCount = 0, taCount = 0;
			//Getting the total number of admin, cc and ta's
			while ((nextLine = reader.readNext()) != null) {
				lineNo++;
				if (lineNo != 1) {
					if (nextLine[5].equalsIgnoreCase("Administrator")) {
						adminCount++;
					} else if (nextLine[5].equalsIgnoreCase("Course Coordinator")) {
						ccCount++;
					} else if (nextLine[5].equalsIgnoreCase("TA")) {
						taCount++;
					}
				}
			}
			reader.close();
			
			//Now validating order of roles
			reader = new CSVReader(new FileReader(csvFile));
			int tillLineNo = adminCount + ccCount + taCount;
			lineNo = 0;
			int i = 0;
			while (((nextLine = reader.readNext()) != null) && (i < tillLineNo)) {
				lineNo++;
				if (lineNo != 1) {
					if (!nextLine[5].equalsIgnoreCase("Administrator") && !nextLine[5].equalsIgnoreCase("Course Coordinator")
						&& !nextLine[5].equalsIgnoreCase("TA")) {
						errorInOrderOfRoles = true;
						return errorInOrderOfRoles;
					}
					i++;
				}
			}
			reader.close();
		} catch (FileNotFoundException e) {
			logger.error("Exception caught: " + e.getMessage());
			if (MiscUtil.DEV_MODE) {
			   for (StackTraceElement s : e.getStackTrace()) {
				   logger.debug(s.toString());
			   }
			}
		} catch (IOException e) {
			logger.error("Exception caught: " + e.getMessage());
			if (MiscUtil.DEV_MODE) {
			   for (StackTraceElement s : e.getStackTrace()) {
				   logger.debug(s.toString());
			   }
			}
		}
		return errorInOrderOfRoles;
	}
	
	//Validating term names
	private static String validateTermNames(File csvFile) {
		try {
			CSVReader reader = new CSVReader(new FileReader(csvFile));
			int lineNo = 0;
			String[] nextLineTerm;
			String displayName = "";
			boolean termInvalid = false;
			while ((nextLineTerm = reader.readNext()) != null) {
				lineNo++;
				if (lineNo != 1) {
					if (lineNo == 2) {
						displayName = nextLineTerm[0] + " " + nextLineTerm[1];
					} else {
						String name = nextLineTerm[0] + " " + nextLineTerm[1];
						if (!name.equalsIgnoreCase(displayName)) {
							termInvalid = true;
							return null;
						}
					}
				}
			}
			return displayName;
		} catch (FileNotFoundException e) {
			logger.error("Exception caught: " + e.getMessage());
			if (MiscUtil.DEV_MODE) {
			   for (StackTraceElement s : e.getStackTrace()) {
				   logger.debug(s.toString());
			   }
			}
		} catch (IOException e) {
			logger.error("Exception caught: " + e.getMessage());
			if (MiscUtil.DEV_MODE) {
			   for (StackTraceElement s : e.getStackTrace()) {
				   logger.debug(s.toString());
			   }
			}
		}
		return null;
	}
	
	
	
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
} //end of class