/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package util;

import java.sql.Timestamp;
import java.util.HashSet;
import javax.persistence.EntityManager;
import javax.persistence.Persistence;
import model.Milestone;
import model.Role;
import model.Schedule;
import model.Team;
import model.Term;
import model.Timeslot;
import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author suresh
 */
public class DBInitUtil {
	/**
	 * Methods to initialize data in a new database.
	 * WARNING! Please run this file only on a blank database!
	 */
	static Logger logger = LoggerFactory.getLogger(DBInitUtil.class);
	
	public static void main(String[] args) {
		
		EntityManager em = Persistence.createEntityManagerFactory("scheduler").createEntityManager();
		try {
			logger.info("DB Initialization started");
			em.getTransaction().begin();
			initDB(em);
			em.getTransaction().commit();
			logger.info("DB Initialization complete");
		} catch (Exception e) {
			logger.error("DB Initialization Error");
			em.getTransaction().rollback();
		}
		
	}
	
	private static void initDB(EntityManager em) throws Exception {
		/*
		 * MILESTONE TABLE POPULATION
		 */
		Milestone acceptance = new Milestone();
		acceptance.setName("Acceptance");
		acceptance.setSlotDuration(60);
		
		Milestone midterm = new Milestone();
		midterm.setName("Midterm");
		midterm.setSlotDuration(60);
		
		Milestone finalMilestone = new Milestone();
		finalMilestone.setName("Final");
		finalMilestone.setSlotDuration(60);
		
		// Persistence
		em.persist(acceptance);
		em.persist(midterm);
		em.persist(finalMilestone);
		logger.info("Milestones persisted");
		
		/*
		 * TERM TABLE POPULATION
		 */		
		Term term12013 = new Term();
		term12013.setAcademicYear(2013);
		term12013.setSemester("Term 1");
		
		Term term22013 = new Term();
		term22013.setAcademicYear(2013);
		term22013.setSemester("Term 2");
		
		// Persistence
		em.persist(term12013);
		em.persist(term22013);
		logger.info("Terms persisted");
		
		/*
		 * ROLE TABLE POPULATION
		 */
		Role student = new Role();
		student.setName("Student");
		student.setTerm(term12013);
		
		Role supervisor = new Role();
		supervisor.setName("Supervisor");
		supervisor.setTerm(term12013);
		
		Role reviewer = new Role();
		reviewer.setName("Reviewer");
		reviewer.setTerm(term12013);
		
		Role ta = new Role();
		ta.setName("TA");
		ta.setTerm(term12013);
		
		//Persistence
		em.persist(student);
		em.persist(supervisor);
		em.persist(reviewer);
		em.persist(ta);
		logger.info("Roles persisted");
		
		/*
		 * SCHEDULE TABLE POPULATION
		 */
		Schedule acceptance12013 = new Schedule();
		acceptance12013.setTerm(term12013);
		acceptance12013.setMilestone(acceptance);
		acceptance12013.setStartDate(new Timestamp(2013 - 1900, 7, 7, 0, 0, 0, 0));
		acceptance12013.setEndDate(new Timestamp(2013 - 1900, 7, 20, 0, 0, 0, 0));
		HashSet<Timeslot> timeslots = new HashSet<Timeslot>();
		for (int a = 7; a <= 20; a++) {
			// Skipping weekends
			if (a == 11 || a == 12 || a == 18 || a == 19) {
				continue;
			}
			
			for (int b = 9; b <= 18; b++) {
				Timeslot t = new Timeslot();
				t.setStartTime(new Timestamp(2013 - 1900, 7, a, b, 0, 0, 0));
				t.setEndTime(new Timestamp(2013 - 1900, 7, a, b + 1, 0, 0, 0));
				t.setVenue("SIS Seminar Room 2-1");
				em.persist(t);
				timeslots.add(t);
			}	
		}
		acceptance12013.setTimeslots(timeslots);
		
		Schedule midterm12013 = new Schedule();
		midterm12013.setTerm(term12013);
		midterm12013.setMilestone(midterm);
		midterm12013.setStartDate(new Timestamp(2013 - 1900, 9, 19, 0, 0, 0, 0));
		midterm12013.setEndDate(new Timestamp(2013 - 1900, 10, 30, 0, 0, 0, 0));
		HashSet<Timeslot> midtermTimeslots = new HashSet<Timeslot>();
		for (int a = 19; a <= 30; a++) {
			// Skipping weekends
			if (a == 21 || a == 22 || a == 28 || a == 29) {
				continue;
			}
			
			for (int b = 9; b <= 18; b++) {
				Timeslot t = new Timeslot();
				t.setStartTime(new Timestamp(2013 - 1900, 7, a, b, 0, 0, 0));
				t.setEndTime(new Timestamp(2013 - 1900, 7, a, b + 1, 0, 0, 0));
				t.setVenue("SIS Seminar Room 2-1");
				em.persist(t);
				midtermTimeslots.add(t);
			}	
		}
		midterm12013.setTimeslots(midtermTimeslots);
		
		// Persistence
		em.persist(acceptance12013);
		em.persist(midterm12013);
		logger.info("Schedule and timeslots persisted");
		
		/*
		 * USER TABLE POPULATION
		 */
		User u1 = new User();
		u1.setUsername("suresh.s.2010");
		u1.setFullName("Suresh SUBRAMANIAM");
		u1.addRole(student);
		
		User u2 = new User();
		u2.setUsername("abhilashm.2010");
		u2.setFullName("Abhilash MURTHY");
		u2.addRole(student);
		
		User u3 = new User();
		u3.setUsername("tsgill.ps.2010");
		u3.setFullName("Tarlochan Singh GILL S/O P S");
		u3.addRole(student);
		
		User u4 = new User();
		u4.setUsername("prakhara.2010");
		u4.setFullName("Prakhar AGARWAL");
		u4.addRole(student);
		
		User u5 = new User();
		u5.setUsername("xuling.dai.2010");
		u5.setFullName("DAI Xuling");
		u5.addRole(student);
		
		User u6 = new User();
		u6.setUsername("rcdavis");
		u6.setFullName("Richard C. DAVIS");
		u6.addRole(supervisor);
		
		User u7 = new User();
		u7.setUsername("yskim");
		u7.setFullName("Youngsoo KIM");
		u7.addRole(reviewer);
		
		User u8 = new User();
		u8.setUsername("laiteecheok");
		u8.setFullName("CHEOK Lai-Tee");
		u8.addRole(reviewer);
		
		// Persistence
		em.persist(u1);
		em.persist(u2);
		em.persist(u3);
		em.persist(u4);
		em.persist(u5);
		em.persist(u6);
		em.persist(u7);
		em.persist(u8);
		logger.info("Users persisted");
		
		/*
		 * TEAM TABLE POPULATION
		 */
		Team t1 = new Team();
		t1.setTerm(term12013);
		t1.setTeamName("Thunderbolt");
		t1.setSupervisor(u6);
		t1.setReviewer1(u7);
		t1.setReviewer2(u8);
		HashSet<User> members = new HashSet<User>();
		members.add(u1);
		members.add(u2);
		members.add(u3);
		members.add(u4);
		members.add(u5);
		t1.setMembers(members);
		
		// Persistence
		em.persist(t1);
		logger.info("Teams persisted");
		
		/*
		 * LINKING USERS AND TEAMS
		 */
		u1.setTeam(t1);
		u2.setTeam(t1);
		u3.setTeam(t1);
		u4.setTeam(t1);
		u5.setTeam(t1);
		
		em.persist(u1);
		em.persist(u2);
		em.persist(u3);
		em.persist(u4);
		em.persist(u5);
		logger.info("User --> Team links persisted");
	}
}
