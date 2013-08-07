/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package util;

import com.google.gson.Gson;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import javax.persistence.EntityManager;
import javax.persistence.Persistence;
import model.Milestone;
import model.Role;
import model.Schedule;
import model.Settings;
import model.Team;
import model.Term;
import model.Timeslot;
import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Contains method to initialize data in a new database.
 * WARNING! Please run this file only on a blank database!
 * @author suresh
 */
public class DBInitUtil {
	
	static Logger logger = LoggerFactory.getLogger(DBInitUtil.class);
	
	/**
	 * Method to initialize data in a new database.
	 * WARNING! Please run this file only on a blank database!
	 */
	public static void main(String[] args) {
		
		EntityManager em = Persistence.createEntityManagerFactory(MiscUtil.PERSISTENCE_UNIT).createEntityManager();
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
		midterm.setSlotDuration(90);
		
		Milestone finalMilestone = new Milestone();
		finalMilestone.setName("Final");
		finalMilestone.setSlotDuration(90);
		
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
		
		Role admin = new Role();
		admin.setName("Administrator");
		
		Role courseCoordinator = new Role();
		courseCoordinator.setName("Course Coordinator");
		
		//Persistence
		em.persist(student);
		em.persist(supervisor);
		em.persist(reviewer);
		em.persist(ta);
		em.persist(admin);
		em.persist(courseCoordinator);
		logger.info("Roles persisted");
		
		/*
		 * SCHEDULE TABLE POPULATION
		 */
		Schedule acceptance12013 = new Schedule();
		acceptance12013.setTerm(term12013);
		acceptance12013.setMilestone(acceptance);
		acceptance12013.setStartDate(new Timestamp(2013 - 1900, 7, 7, 0, 0, 0, 0));
		acceptance12013.setEndDate(new Timestamp(2013 - 1900, 7, 20, 0, 0, 0, 0));
		
		Schedule midterm12013 = new Schedule();
		midterm12013.setTerm(term12013);
		midterm12013.setMilestone(midterm);
		midterm12013.setStartDate(new Timestamp(2013 - 1900, 9, 19, 0, 0, 0, 0));
		midterm12013.setEndDate(new Timestamp(2013 - 1900, 10, 30, 0, 0, 0, 0));
		
		// Persistence
		em.persist(acceptance12013);
		em.persist(midterm12013);
		logger.info("Schedule persisted");
		
		/*
		 * TIMESLOT TABLE POPULATION
		 */
		for (int a = 7; a <= 20; a++) {
			// Skipping weekends
			if (a == 10 || a == 11 || a == 17 || a == 18) {
				continue;
			}
			
			for (int b = 9; b <= 18; b++) {
				Timeslot t = new Timeslot();
				t.setStartTime(new Timestamp(2013 - 1900, 7, a, b, 0, 0, 0));
				t.setEndTime(new Timestamp(2013 - 1900, 7, a, b + 1, 0, 0, 0));
				t.setVenue("SIS Seminar Room 2-1");
				t.setSchedule(acceptance12013);
				em.persist(t);
			}	
		}
		
		for (int a = 19; a <= 30; a++) {
			// Skipping weekends
			if (a == 20 || a == 21 || a == 27 || a == 28) {
				continue;
			}
			
			for (int b = 9; b <= 18; b++) {
				Timeslot t = new Timeslot();
				t.setStartTime(new Timestamp(2013 - 1900, 7, a, b, 0, 0, 0));
				t.setEndTime(new Timestamp(2013 - 1900, 7, a, b + 1, 0, 0, 0));
				t.setVenue("SIS Seminar Room 2-1");
				t.setSchedule(midterm12013);
				em.persist(t);
			}	
		}
		/*
		 * USER TABLE POPULATION
		 */
		User u0 = new User();
		u0.setUsername("fionalee");
		u0.setFullName("Fiona LEE");
		u0.addRole(admin);
		
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
                
		User u9 = new User();
		u9.setUsername("henry.tang.2011");
		u9.setFullName("Henry TANG Ji Rui");
		u9.addRole(student);
                
		User u10 = new User();
		u10.setUsername("ian.chan.2011");
		u10.setFullName("Ian Clarence CHAN");
		u10.addRole(student);
                
		User u11 = new User();
		u11.setUsername("jeremyzhong.2011");
		u11.setFullName("Jeremy ZHONG Jiahao");
		u11.addRole(student);
		
		User u12 = new User();
		u12.setUsername("xrlee.2011");
		u12.setFullName("LEE Xiang Rui");
		u12.addRole(student);
                
		User u13 = new User();
		u13.setUsername("vivian.lai.2011");
		u13.setFullName("Vivian LAI Wan Yin");
		u13.addRole(student);
                
		User u14 = new User();
		u14.setUsername("alvin.soh.2011");
		u14.setFullName("Alvin SOH Wei Sheng");
		u14.addRole(student);
               
		User u15 = new User();
		u15.setUsername("benjamingan");
		u15.setFullName("Benjamin GAN Kok Siew");
		u15.addRole(supervisor);
		u15.addRole(courseCoordinator);
                
		User u16 = new User();
		u16.setUsername("mfaizal.s.2010");
		u16.setFullName("Muhammad Faizal SUKIM");
		u16.addRole(student);
                
		User u17 = new User();
		u17.setUsername("huimin.hong.2011");
		u17.setFullName("HONG Huimin");
		u17.addRole(student);
                
		User u18 = new User();
		u18.setUsername("shaorui.lei.2011");
		u18.setFullName("LEI Shaorui");
		u18.addRole(student);
                
		User u19 = new User();
		u19.setUsername("zhuoran.li.2011");
		u19.setFullName("LI Zhuoran");
		u19.addRole(student);
                
		User u20 = new User();
		u20.setUsername("jz.peng.2011");
		u20.setFullName("PENG Jian Zhang");
		u20.addRole(student);
                
		User u21 = new User();
		u21.setUsername("cboesch");
		u21.setFullName("Chris BOESCH");
		u21.addRole(supervisor);
		
		// Persistence
		em.persist(u0);
		em.persist(u1);
		em.persist(u2);
		em.persist(u3);
		em.persist(u4);
		em.persist(u5);
		em.persist(u6);
		em.persist(u7);
		em.persist(u8);
		em.persist(u9);
		em.persist(u10);
		em.persist(u11);
		em.persist(u12);
		em.persist(u13);
		em.persist(u14);
		em.persist(u15);
		em.persist(u16);
		em.persist(u17);
		em.persist(u18);
		em.persist(u19);
		em.persist(u20);
		em.persist(u21);
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
                
                
		Team t2 = new Team();
		t2.setTerm(term12013);
		t2.setTeamName("Acellence");
		t2.setSupervisor(u15);
		t2.setReviewer1(u8);
		t2.setReviewer2(u6);
		HashSet<User> t2members = new HashSet<User>();
		t2members.add(u9);
		t2members.add(u10);
		t2members.add(u11);
		t2members.add(u12);
		t2members.add(u13);
		t2members.add(u14);
		t2.setMembers(t2members);
                
		Team t3 = new Team();
		t3.setTerm(term12013);
		t3.setTeamName("ironMEN");
		t3.setSupervisor(u21);
		t3.setReviewer1(u15);
		t3.setReviewer2(u7);
		HashSet<User> t3members = new HashSet<User>();
		t3members.add(u16);
		t3members.add(u17);
		t3members.add(u18);
		t3members.add(u19);
		t3members.add(u20);
		t3.setMembers(t3members);
		
		// Persistence
		em.persist(t1);
		em.persist(t2);
		em.persist(t3);
		logger.info("Teams persisted");
		
		/*
		 * LINKING USERS AND TEAMS
		 */
		u1.setTeam(t1);
		u2.setTeam(t1);
		u3.setTeam(t1);
		u4.setTeam(t1);
		u5.setTeam(t1);
                
		u9.setTeam(t2);
		u10.setTeam(t2);
		u11.setTeam(t2);
		u12.setTeam(t2);
		u13.setTeam(t2);
		u14.setTeam(t2);

		u16.setTeam(t3);
		u17.setTeam(t3);
		u18.setTeam(t3);
		u19.setTeam(t3);
		u20.setTeam(t3);
		
		em.persist(u1);
		em.persist(u2);
		em.persist(u3);
		em.persist(u4);
		em.persist(u5);
		em.persist(u9);
		em.persist(u10);
		em.persist(u11);
		em.persist(u12);
		em.persist(u13);
		em.persist(u14);
		em.persist(u15);
		em.persist(u16);
		em.persist(u17);
		em.persist(u18);
		em.persist(u19);
		em.persist(u20);
		em.persist(u21);
		logger.info("User --> Team links persisted");
		
		/*
		 * INITIALIZING SETTINGS
		 */
		Settings activeTerms = new Settings();
		activeTerms.setName("activeTerms");
		ArrayList<Long> activeTermIds = new ArrayList<Long>();
		activeTermIds.add(term12013.getId());
		activeTerms.setValue(new Gson().toJson(activeTermIds));
		
		Settings milestones = new Settings();
		milestones.setName("milestones");
		ArrayList<HashMap<String,Object>> milestoneList = new ArrayList<HashMap<String, Object>>();
		HashMap<String,Object> accMap = new HashMap<String, Object>();
		accMap.put("name", "Acceptance");
		accMap.put("duration", 60);
		milestoneList.add(accMap);
		HashMap<String,Object> midMap = new HashMap<String, Object>();
		midMap.put("name", "Midterm");
		midMap.put("duration", 90);
		milestoneList.add(midMap);
		HashMap<String,Object> finMap = new HashMap<String, Object>();
		finMap.put("name", "Final");
		finMap.put("duration", 90);
		milestoneList.add(finMap);
		milestones.setValue(new Gson().toJson(milestoneList));
		
		//Persistence
		em.persist(activeTerms);
		em.persist(milestones);
	}
}
