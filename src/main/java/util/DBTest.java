/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package util;

import static com.opensymphony.xwork2.Action.SUCCESS;
import constant.Response;
import constant.Role;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.logging.Level;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import javax.persistence.Query;
import javax.servlet.http.HttpSession;
import manager.BookingManager;
import manager.UserManager;
import model.Booking;
import model.Milestone;
import model.Schedule;
import model.Team;
import model.Term;
import model.Timeslot;
import model.User;
import model.role.Faculty;
import model.role.Student;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Contains method to initialize data in a new database.
 * WARNING! Please run this file only on a blank database!
 * @author suresh
 */
public class DBTest {
	
	static Logger logger = LoggerFactory.getLogger(DBTest.class);
        static {
            try {
//		logger.info("DB Creation started");     
//                resetDB();
//                logger.info("DB Creation complete");
            } catch (Exception ex) {
                java.util.logging.Logger.getLogger(DBInitUtil.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
	
	/**
	 * Method to initialize data in a new database.
	 * WARNING! Please run this file only on a blank database!
	 */
	public static void main(String[] args) {
		EntityManager em = Persistence.createEntityManagerFactory(MiscUtil.PERSISTENCE_UNIT).createEntityManager();
		try {
                        logger.info("DB Testing started");   
//			em.getTransaction().begin();
			testDB(em);
//			em.getTransaction().commit();
			testDb(em);
			logger.info("DB Testing complete");
		} catch (Exception e) {
			logger.error("DB Testing Error:");
			logger.error(e.getMessage());
			for (StackTraceElement s : e.getStackTrace()) {
				logger.debug(s.toString());
			}
			if (em.getTransaction().isActive()) {
				em.getTransaction().rollback();
			}
		}
		
	}
        
        private static void resetDB() throws Exception {
            String url = "jdbc:mysql://localhost:3306/";
            String username = "root";
            String password = "root";
            String dbName = "is480-scheduling";
            Connection conn = null;
            Statement stmt = null;
            try {
                Class.forName("com.mysql.jdbc.Driver");
                logger.debug("Connecting to phpmyadmin..");
                conn = DriverManager.getConnection(url, username, password);
                stmt = conn.createStatement();
                try {
                    stmt.executeUpdate("CREATE DATABASE `" + dbName + "`");
                } catch (SQLException s) {
                    logger.debug("Database exists. Dropping and creating again.");
                    stmt.executeUpdate("DROP DATABASE `" + dbName + "`");
                    stmt.executeUpdate("CREATE DATABASE `" + dbName + "`");
                }
                logger.debug("Database created successfully");
            } catch (Exception e) {
                logger.error("Exception caught! " + e.getMessage());
            } finally {
                if (stmt != null) {
                    stmt.close();
                    conn.close();
                }
            }
        }
	
	private static void testDB(EntityManager em) throws Exception {
		Term currentTerm = em.find(Term.class, 1L);
		User loggedIn = UserManager.findByRoleTermUsername(em, Role.STUDENT, currentTerm, "suresh.s.2010");
		execute(em, loggedIn, Role.STUDENT, 1L);
	}
	
	public static void testDb(EntityManager em) {
		
	}
	
	private static HashMap<String, Object> json = new HashMap<String, Object>();
	
    public static String execute(EntityManager em, User user, Role activeRole, Long timeslotId) throws Exception {
        try {
            json.put("exception", false);
            Team team = null;

			//Retrieving the team information
            if (activeRole.equals(Role.STUDENT)) {
                Student s = em.find(Student.class, user.getId());
				team = s.getTeam();
            }
			
			//Retrieving the chosen timeslot
			Timeslot timeslot = null;
			if (timeslotId != null) {
				timeslot = em.find(Timeslot.class, timeslotId);
			}
			
            //Validating information provided by the front end
            if (!validateInformation(em, team, timeslot)) {
                    return SUCCESS;
            }

            try {
				
				em.getTransaction().begin();
				
				Booking booking = new Booking();
				
				//Assign information to booking
				booking.setTimeslot(timeslot);
                booking.setTeam(team);
				booking.setCreatedAt(new Timestamp(Calendar.getInstance().getTimeInMillis()));

                //Add team members to attendees
                HashSet<User> reqAttendees = new HashSet<User>();
                reqAttendees.addAll(team.getMembers());

                //Create booking response entries based on milestone
				//TODO Remove hardcoding after milestone management is implemented
                HashMap<User, Response> responseList = new HashMap<User, Response>();
				Milestone milestone = timeslot.getSchedule().getMilestone();
                if (milestone.getName().equalsIgnoreCase("acceptance")) {
                    responseList.put(team.getSupervisor(), Response.PENDING);
                    reqAttendees.add(team.getSupervisor());
                } else if (milestone.getName().equalsIgnoreCase("midterm")) {
                    responseList.put(team.getReviewer1(), Response.PENDING);
                    reqAttendees.add(team.getReviewer1());
					responseList.put(team.getReviewer2(), Response.PENDING);
                    reqAttendees.add(team.getReviewer2());
                } else if (milestone.getName().equalsIgnoreCase("final")) {
					responseList.put(team.getSupervisor(), Response.PENDING);
                    reqAttendees.add(team.getSupervisor());
                    responseList.put(team.getReviewer1(), Response.PENDING);
                    reqAttendees.add(team.getReviewer1());
                } else {
                    logger.error("FATAL ERROR: Code not to be reached!");
                    throw new Exception();
                }

                booking.setResponseList(responseList);
                booking.setRequiredAttendees(reqAttendees);
//                NewBookingEmail newEmail = new NewBookingEmail(bookingSlot);
//				RespondToBookingEmail responseEmail = new RespondToBookingEmail(bookingSlot);
//                newEmail.sendEmail();
//				responseEmail.sendEmail();
                em.persist(booking);
				
				//Setting the current active booking in the timeslot object
				timeslot.setCurrentBooking(booking);
				em.persist(timeslot);
				
                em.getTransaction().commit();
            } catch (Exception e) {
                //Rolling back write operations
                em.getTransaction().rollback();
                logger.error("FATAL ERROR: Database Write Error. Code not to be reached!");
                json.put("success", false);
                json.put("message", "Oops. Something went wrong on our end. Please try again!");
                return SUCCESS;
            }

            json.put("success", true);
            json.put("message", "Booking created successfully! Confirmation email has been sent to all attendees.");
        } catch (Exception e) {
            logger.error("Exception caught: " + e.getMessage());
            if (MiscUtil.DEV_MODE) {
                for (StackTraceElement s : e.getStackTrace()) {
                    logger.debug(s.toString());
                }
            }
            json.put("success", false);
            json.put("exception", true);
            json.put("message", "Error with CreateBooking: Escalate to developers!");
        }
        return SUCCESS;
    }
	
	private static boolean validateInformation(EntityManager em, Team team, Timeslot timeslot) {
		// Checking if team information is found
		if (team == null) {
			logger.error("Team information not found or unauthorized user role");
			json.put("success", false);
			json.put("message", "Team not identified or you do not have required"
					+ " permissions to make a booking.");
			return false;
		}

		//Check if the timeslot is found
		if (timeslot == null) {
			logger.error("Timeslot not found");
			json.put("success", false);
			json.put("message", "Timeslot not found. Please check the ID provided!");
			return false;
		}

		//Check if the timeslot is free
		if (timeslot.getCurrentBooking() != null) { //Slot is full
			json.put("success", false);
			json.put("message", "Oops. This timeslot is already taken."
					+ " Please book another slot!");
			return false;
		}
		
		//Check if the team has already made a booking for the current schedule
		ArrayList<Booking> activeBookings = BookingManager.getActiveByTeamAndSchedule(em, team, timeslot.getSchedule());
		if (activeBookings == null) {
			logger.error("Error in database query");
			json.put("success", false);
			json.put("message", "Oops. Something went wrong");
			return false;
		}
		
		if (!activeBookings.isEmpty()) {
			json.put("success", false);
			json.put("message", "Team already has a booking in the current schedule");
			return false;	
		}
		
		return true;
	}
}
