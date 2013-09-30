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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.ServletContext;
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
			//Getting the Term
			long termId = Long.parseLong(request.getParameter("termChosen"));
			Term term = em.find(Term.class, termId);
//			String termName = term.getDisplayName();
			//Getting the file
			File csvFile = getFile();
			try {
				logger.info("Extracting data from CSV File started");
				em.getTransaction().begin();
				csvUpload(csvFile, term, em);
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
			
	private static void csvUpload(File csvFile, Term term, EntityManager em) {
		// <------------------------Start Parsing the File --------------------------->
		try {
			CSVReader reader = new CSVReader(new FileReader(csvFile));

			//------------------------Creating user objects------------------------
			String[] nextLineUsers;
			int lineNo = 0;
			List<User> usersList = new ArrayList<User>();
			//Read one line at a time
			logger.info("Parsing csv file for users");
			while ((nextLineUsers = reader.readNext()) != null) {
				lineNo++;
				//Not reading the 1st line
				if (lineNo != 1) {
	//					for(String token : nextLine) {
						if (nextLineUsers[1].equals("0")) {
							//Creating student object
							//Extracting the email address from between the brackets
							Pattern pattern = Pattern.compile("<(.*?)@");
							Matcher matcher = pattern.matcher(nextLineUsers[5]);
							if (matcher.find()) {
								Student student = new Student(nextLineUsers[0], matcher.group(1), null, term);
								usersList.add(student);
							} else {
								//Incorrect email address
	//								Student student = new Student(nextLine[0], null, null, term);
							}
						} else if (nextLineUsers[1].equals("1")) {
							//Creating faculty object
							Faculty faculty = new Faculty(nextLineUsers[0], nextLineUsers[5], null, term);
							usersList.add(faculty);
						} else if (nextLineUsers[1].equals("2")) {
							//Creating TA object
							TA ta = new TA(nextLineUsers[0], nextLineUsers[5], null, term);
							usersList.add(ta);
						} else if (nextLineUsers[1].equals("3")) {
							//Creating admin object
							User admin = new User(nextLineUsers[0], nextLineUsers[5], null, Role.ADMINISTRATOR, term);
							usersList.add(admin);
						} else if (nextLineUsers[1].equals("4")) {
							//Creating course coordinator object
							User cc = new User(nextLineUsers[0], nextLineUsers[5], null, Role.COURSE_COORDINATOR, term);
							usersList.add(cc);
						}
	//					}
				}
			}
			//Persisting the user objects
			logger.info("Persisting users");
			for (User userObj: usersList) {
				em.persist(userObj);
			}
			reader.close();

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
					if (!(nextLineTeams[7].equalsIgnoreCase("-") && nextLineTeams[7].equalsIgnoreCase(""))) {
						//Checking whether there is already a team with the same name in the teams list 
						boolean teamExists = false;
						if (teamsList.size() > 0) {
							for (Team teamCreated: teamsList) {
								if (teamCreated.getTeamName().equalsIgnoreCase(nextLineTeams[7])) {
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
						team.setTeamName(nextLineTeams[7]);
						team.setTerm(term);
						teamsList.add(team);
					}
				}
			}
			reader.close();
			
			//-----------------------Assigning users to teams-------------------------
			reader = new CSVReader(new FileReader(csvFile));
			String nextLine[];
			lineNo = 0;
			logger.info("Parsing csv file to assign users to teams");
			HashSet<Student> students = new HashSet<Student>();
			//Read one line at a time
			while ((nextLine = reader.readNext()) != null) {
				lineNo++;
				//Not reading the 1st line
				if (lineNo != 1) {
					if (!(nextLine[7].equalsIgnoreCase("-") && nextLine[7].equalsIgnoreCase(""))) {
						for (Team team: teamsList) {
							boolean teamFound = false;
							//Getting the team object
							if (nextLine[7].equalsIgnoreCase(team.getTeamName())) {
								teamFound = true;
								//Getting the user objects for the team
								for (User user: usersList) {
									if (nextLine[0].equalsIgnoreCase(user.getFullName())) {
										Student student = (Student) user;
										students.add(student);
									}
									if (nextLine[2].equalsIgnoreCase(user.getFullName())) {
										//Setting the supervisor for the team
										Faculty faculty = (Faculty) user;
										team.setSupervisor(faculty);
									} 
									if (nextLine[3].equalsIgnoreCase(user.getFullName())) {
										//Setting the reviewer 1 for the team
										Faculty faculty = (Faculty) user;
										team.setReviewer1(faculty);
									}
									if (nextLine[4].equalsIgnoreCase(user.getFullName())) {
										//Setting the reviewer 2 for the team
										Faculty faculty = (Faculty) user;
										team.setReviewer2(faculty);
									} 
								}
								//Setting the students for the team
							}
							if (teamFound == true) { break; }
						}
					}
				}
			}
			//Persisting the team objects
			logger.info("Persisting teams");
//			for (Team team: ) {
//				em.persist(userObj);
//			}
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