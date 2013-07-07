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
import model.Schedule;
import model.Term;
import model.Timeslot;
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
	public static void main(String[] args) {
		Logger logger = LoggerFactory.getLogger(DBInitUtil.class);
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
		// Creation
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
		
		/*
		 * TERM TABLE POPULATION
		 */		
		// Creation
		Term term12013 = new Term();
		term12013.setAcademicYear(2013);
		term12013.setSemester(1);
		
		Term term22013 = new Term();
		term22013.setAcademicYear(2013);
		term22013.setSemester(2);
		
		// Persistence
		em.persist(term12013);
		em.persist(term12013);
		
		/*
		 * SCHEDULE TABLE POPULATION
		 */
		// Creation
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
		
		// Persistence
		em.persist(acceptance12013);
	}
}
