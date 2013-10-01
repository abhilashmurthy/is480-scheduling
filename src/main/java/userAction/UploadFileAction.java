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
//import util.FilesUtil;
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
				em.getTransaction().begin();
				csvUpload(csvFile, em);
				em.getTransaction().commit();
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
	}
			
	private static void csvUpload(File csvFile, EntityManager em) {
		// <------------------------Start Parsing the File --------------------------->
		try {
			CSVReader reader = new CSVReader(new FileReader(csvFile));

			//Getting the term and checking whether all term names are the same
			logger.info("Parsing csv file for term");
			int lineNo = 0;
			String[] nextLineTerm;
			String displayName = "";
			boolean termsInvalid = false;
			while ((nextLineTerm = reader.readNext()) != null) {
				lineNo++;
				if (lineNo != 1) {
					if (lineNo == 2) {
						displayName = nextLineTerm[0] + " " + nextLineTerm[1];
					} else {
						String name = nextLineTerm[0] + " " + nextLineTerm[1];
						if (!name.equalsIgnoreCase(displayName)) {
							termsInvalid = true;
							break;
						}
					}
				}
			}
			Term term = null;
			if (termsInvalid == true) {
				System.out.println("Error: Term names are invalid!");
			} else {
				term = TermManager.getTermByDisplayName(em, displayName);
			}
			reader.close();
			
			
			//------------------------Creating user objects------------------------
			reader = new CSVReader(new FileReader(csvFile));
			String[] nextLineUsers;
			lineNo = 0;
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
			for (User userObj: usersList) {
				System.out.println(userObj.getFullName());
			}
			
			//-------------------------Creating the teams---------------------------
			reader = new CSVReader(new FileReader(csvFile));
			List<Team> teamsList = new ArrayList<Team>();
			String nextLineTeams[];
			lineNo = 0;
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
			for (Team teamObj : teamsList) {
				System.out.println(teamObj.getTeamName());
			}
			
			//-----------------------Assigning users to teams-------------------------
//			reader = new CSVReader(new FileReader(csvFile));
//			String nextLine[];
//			lineNo = 0;
//			logger.info("Parsing csv file to assign users to teams");
//			HashSet<Student> students = new HashSet<Student>();
//			//Read one line at a time
//			while ((nextLine = reader.readNext()) != null) {
//				lineNo++;
//				//Not reading the 1st line
//				if (lineNo != 1) {
//					if (!(nextLine[7].equalsIgnoreCase("-") && nextLine[7].equalsIgnoreCase(""))) {
//						for (Team team: teamsList) {
//							boolean teamFound = false;
//							//Getting the team object
//							if (nextLine[7].equalsIgnoreCase(team.getTeamName())) {
//								teamFound = true;
//								//Getting the user objects for the team
//								for (User user: usersList) {
//									if (nextLine[0].equalsIgnoreCase(user.getFullName())) {
//										Student student = (Student) user;
//										students.add(student);
//									}
//									if (nextLine[2].equalsIgnoreCase(user.getFullName())) {
//										//Setting the supervisor for the team
//										Faculty faculty = (Faculty) user;
//										team.setSupervisor(faculty);
//									} 
//									if (nextLine[3].equalsIgnoreCase(user.getFullName())) {
//										//Setting the reviewer 1 for the team
//										Faculty faculty = (Faculty) user;
//										team.setReviewer1(faculty);
//									}
//									if (nextLine[4].equalsIgnoreCase(user.getFullName())) {
//										//Setting the reviewer 2 for the team
//										Faculty faculty = (Faculty) user;
//										team.setReviewer2(faculty);
//									} 
//								}
//								//Setting the students for the team
//							}
//							if (teamFound == true) { break; }
//						}
//					}
//				}
//			}
//			//Persisting the team objects
//			logger.info("Persisting teams");
////			for (Team team: ) {
////				em.persist(userObj);
////			}
//			reader.close();
			
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