/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package util;

import com.google.gson.Gson;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.logging.Level;
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
        static {
            try {
		logger.info("DB Creation started");     
                resetDB();
                logger.info("DB Creation complete");
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
        
        private static void resetDB() throws Exception {
            String url = "jdbc:mysql://localhost:3306/";
            String username = "root";
            String password = null;
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
		midterm12013.setEndDate(new Timestamp(2013 - 1900, 9, 30, 0, 0, 0, 0));
                
		Schedule final12013 = new Schedule();
		final12013.setTerm(term12013);
		final12013.setMilestone(finalMilestone);
		final12013.setStartDate(new Timestamp(2013 - 1900, 11, 1, 0, 0, 0, 0));
		final12013.setEndDate(new Timestamp(2013 - 1900, 11, 15, 0, 0, 0, 0));
		
		// Persistence
		em.persist(acceptance12013);
		em.persist(midterm12013);
		em.persist(final12013);
		logger.info("Schedule persisted");
		
		/*
		 * TIMESLOT TABLE POPULATION
		 */
                //Acceptance
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
                
		//Midterm
		for (int a = 19; a <= 30; a++) {
			// Skipping weekends
			if (a == 19 || a == 20 || a == 26 || a == 27) {
				continue;
			}
			
                        boolean change = true;
			for (int b = 9; (b + 2) <= 18; b++) {
				Timeslot t = new Timeslot();
                                if (change) {
                                    t.setStartTime(new Timestamp(2013 - 1900, 9, a, b, 0, 0, 0));
                                    t.setEndTime(new Timestamp(2013 - 1900, 9, a, b + 1, 30, 0, 0));
                                } else {
                                    t.setStartTime(new Timestamp(2013 - 1900, 9, a, b, 30, 0, 0));
                                    t.setEndTime(new Timestamp(2013 - 1900, 9, a, b + 2, 00, 0, 0));
                                    b++;
                                }
				t.setVenue("SIS Seminar Room 2-1");
				t.setSchedule(midterm12013);
				em.persist(t);
                                change = !change;
			}	
		}
                
		//Final
		for (int a = 1; a <= 15; a++) {
			// Skipping weekends
			if (a == 1 || a == 7 || a == 8 || a == 14 || a == 15) {
				continue;
			}
			
                        boolean change = true;
			for (int b = 9; (b + 2) <= 18; b++) {
				Timeslot t = new Timeslot();
                                if (change) {
                                    t.setStartTime(new Timestamp(2013 - 1900, 11, a, b, 0, 0, 0));
                                    t.setEndTime(new Timestamp(2013 - 1900, 11, a, b + 1, 30, 0, 0));
                                } else {
                                    t.setStartTime(new Timestamp(2013 - 1900, 11, a, b, 30, 0, 0));
                                    t.setEndTime(new Timestamp(2013 - 1900, 11, a, b + 2, 00, 0, 0));
                                    b++;
                                }
				t.setVenue("SIS Seminar Room 2-1");
				t.setSchedule(final12013);
				em.persist(t);
                                change = !change;
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
		
		User u22 = new User();
		u22.setUsername("yt.ning.2011");
		u22.setFullName("NING Yuting");
		u22.addRole(student);

		User u23 = new User();
		u23.setUsername("duo.li.2011");
		u23.setFullName("LI Duo");
		u23.addRole(student);

		User u24 = new User();
		u24.setUsername("haryono.2011");
		u24.setFullName("HARYONO");
		u24.addRole(student);

		User u25 = new User();
		u25.setUsername("yufu.2011");
		u25.setFullName("FU Yu");
		u25.addRole(student);

		User u26 = new User();
		u26.setUsername("canwang.2011");
		u26.setFullName("WANG Can");
		u26.addRole(student);

		User u27 = new User();
		u27.setUsername("fzsun.2011");
		u27.setFullName("SUN Fangzhou");
		u27.addRole(student);

		User u28 = new User();
		u28.setUsername("lu.yang.2011");
		u28.setFullName("YANG Lu");
		u28.addRole(student);

		User u29 = new User();
		u29.setUsername("wenxuan.he.2011");
		u29.setFullName("HE Wenxuan");
		u29.addRole(student);

		User u30 = new User();
		u30.setUsername("jifei.zhang.2010");
		u30.setFullName("ZHANG Jifei");
		u30.addRole(student);

		User u31 = new User();
		u31.setUsername("tao.liang.2011");
		u31.setFullName("LIANG Tao");
		u31.addRole(student);

		User u32 = new User();
		u32.setUsername("miao.gao.2010");
		u32.setFullName("GAO Miao");
		u32.addRole(student);

		User u33 = new User();
		u33.setUsername("joelbb.p.2010");
		u33.setFullName("PEREIRA Joel Bernardo Bosco");
		u33.addRole(student);

		User u34 = new User();
		u34.setUsername("bixia.ang.2010");
		u34.setFullName("ANG Bi Xia");
		u34.addRole(student);

		User u35 = new User();
		u35.setUsername("yiying.tan.2010");
		u35.setFullName("TAN Yi Ying");
		u35.addRole(student);

		User u36 = new User();
		u36.setUsername("suansen.yeo.2010");
		u36.setFullName("YEO Suan Sen");
		u36.addRole(student);


		User u37 = new User();
		u37.setUsername("iadarmawan.2010");
		u37.setFullName("Indra Adam DARMAWAN");
		u37.addRole(student);

		User u38 = new User();
		u38.setUsername("lynetteseah.2010");
		u38.setFullName("Lynette SEAH Pei Jie");
		u38.addRole(student);

		User u39 = new User();
		u39.setUsername("jane.lee.2011");
		u39.setFullName("Jane LEE Xue li");
		u39.addRole(student);

		User u40 = new User();
		u40.setUsername("shena.ong.2011");
		u40.setFullName("Shena ONG Wei Ting");
		u40.addRole(student);

		User u41 = new User();
		u41.setUsername("edmund.gair.2010");
		u41.setFullName("Edmund GAIR Jun Jie");
		u41.addRole(student);

		User u42 = new User();
		u42.setUsername("kaicong.loh.2011");
		u42.setFullName("LOH Kai Cong");
		u42.addRole(student);

		User u43 = new User();
		u43.setUsername("yanjun.tan.2011");
		u43.setFullName("TAN Yan Jun");
		u43.addRole(student);

		User u44 = new User();
		u44.setUsername("weiyang.sim.2011");
		u44.setFullName("SIM Wei Yang");
		u44.addRole(student);

		User u45 = new User();
		u45.setUsername("sy.chia.2011");
		u45.setFullName("CHIA Sheng Yang");
		u45.addRole(student);

		User u46 = new User();
		u46.setUsername("junkiat.koh.2011");
		u46.setFullName("KOH Jun Kiat");
		u46.addRole(student);

		User u47 = new User();
		u47.setUsername("billy.lam.2011");
		u47.setFullName("Billy LAM Wai Loon");
		u47.addRole(student);

		User u48 = new User();
		u48.setUsername("rosannechoo.2011");
		u48.setFullName("Rosanne CHOO Sweet Cin");
		u48.addRole(student);

		User u49 = new User();
		u49.setUsername("wjwee.2011");
		u49.setFullName("WEE Wei Jian");
		u49.addRole(student);

		User u50 = new User();
		u50.setUsername("yh.koon.2010");
		u50.setFullName("Geraldine KOON Yuhua");
		u50.addRole(student);

		User u51 = new User();
		u51.setUsername("juntao.zhu.2010");
		u51.setFullName("ZHU Juntao");
		u51.addRole(student);

		User u52 = new User();
		u52.setUsername("yg.tan.2010");
		u52.setFullName("TAN Yao Guang");
		u52.addRole(student);

		User u53 = new User();
		u53.setUsername("james.lim.2010");
		u53.setFullName("James LIM Xing Yan");
		u53.addRole(student);

		User u54 = new User();
		u54.setUsername("kevin.ng.2010");
		u54.setFullName("Kevin NG Ying Yi");
		u54.addRole(student);

		User u55 = new User();
		u55.setUsername("jonathan.ho.2010");
		u55.setFullName("Jonathan HO Jian Wei");
		u55.addRole(student);

		User u56 = new User();
		u56.setUsername("jolie.lee.2010");
		u56.setFullName("Jolie LEE Jia Ling");
		u56.addRole(student);

		User u57 = new User();
		u57.setUsername("radeyap.2010");
		u57.setFullName("Radeya PARVEEN");
		u57.addRole(student);

		User u58 = new User();
		u58.setUsername("rosalind.ng.2010");
		u58.setFullName("Rosalind NG Hsiu Zhen");
		u58.addRole(student);

		User u59 = new User();
		u59.setUsername("sitiz.k.2010");
		u59.setFullName("Siti Zulaiha BTE KAMARUDIN");
		u59.addRole(student);

		User u60 = new User();
		u60.setUsername("lionel.koh.2010");
		u60.setFullName("Lionel KOH Wee Heng");
		u60.addRole(student);

		User u61 = new User();
		u61.setUsername("xinyi.song.2010");
		u61.setFullName("SONG Xinyi");
		u61.addRole(student);

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
		em.persist(u22);
		em.persist(u23);
		em.persist(u24);
		em.persist(u25);
		em.persist(u26);
		em.persist(u27);
		em.persist(u28);
		em.persist(u29);
		em.persist(u30);
		em.persist(u31);
		em.persist(u32);
		em.persist(u33);
		em.persist(u34);
		em.persist(u35);
		em.persist(u36);
		em.persist(u37);
		em.persist(u38);
		em.persist(u39);
		em.persist(u40);
		em.persist(u41);
		em.persist(u42);
		em.persist(u43);
		em.persist(u44);
		em.persist(u45);
		em.persist(u46);
		em.persist(u47);
		em.persist(u48);
		em.persist(u49);
		em.persist(u50);
		em.persist(u51);
		em.persist(u52);
		em.persist(u53);
		em.persist(u54);
		em.persist(u55);
		em.persist(u56);
		em.persist(u57);
		em.persist(u58);
		em.persist(u59);
		em.persist(u60);
		em.persist(u61);
		logger.info("Users persisted");
		
		/*
		 * TEAM TABLE POPULATION
		 */
		Team t1 = new Team();
		t1.setTerm(term12013);
		t1.setTeamName("Thunderbolt");
		t1.setSupervisor(u6);
		t1.setReviewer1(u15);
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
		
		Team t4 = new Team();
		t4.setTerm(term12013);
		t4.setTeamName("CARTES");
		t4.setSupervisor(u15);
		t4.setReviewer1(u7);
		t4.setReviewer2(u8);
		HashSet<User> t4members = new HashSet<User>();
		t4members.add(u22);
		t4members.add(u23);
		t4members.add(u24);
		t4members.add(u25);
		t4members.add(u26);
		t4.setMembers(t4members);
                
		Team t5 = new Team();
		t5.setTerm(term12013);
		t5.setTeamName("Creagend");
		t5.setSupervisor(u15);
		t5.setReviewer1(u21);
		t5.setReviewer2(u6);
		HashSet<User> t5members = new HashSet<User>();
		t5members.add(u27);
		t5members.add(u28);
		t5members.add(u29);
		t5members.add(u30);
		t5members.add(u31);
		t5members.add(u32);
		t5.setMembers(t5members);
                
		Team t6 = new Team();
		t6.setTerm(term12013);
		t6.setTeamName("Funktional");
		t6.setSupervisor(u15);
		t6.setReviewer1(u8);
		t6.setReviewer2(u6);
		HashSet<User> t6members = new HashSet<User>();
		t6members.add(u33);
		t6members.add(u34);
		t6members.add(u35);
		t6members.add(u36);
		t6members.add(u37);
		t6members.add(u38);
		t6.setMembers(t6members);
                
		Team t7 = new Team();
		t7.setTerm(term12013);
		t7.setTeamName("Jskey");
		t7.setSupervisor(u8);
		t7.setReviewer1(u6);
		t7.setReviewer2(u15);
		HashSet<User> t7members = new HashSet<User>();
		t7members.add(u39);
		t7members.add(u40);
		t7members.add(u41);
		t7members.add(u42);
		t7members.add(u43);
		t7.setMembers(t7members);
                
		Team t8 = new Team();
		t8.setTerm(term12013);
		t8.setTeamName("UniCode");
		t8.setSupervisor(u8);
		t8.setReviewer1(u15);
		t8.setReviewer2(u21);
		HashSet<User> t8members = new HashSet<User>();
		t8members.add(u44);
		t8members.add(u45);
		t8members.add(u46);
		t8members.add(u47);
		t8members.add(u48);
		t8members.add(u49);
		t8.setMembers(t8members);
                
		Team t9 = new Team();
		t9.setTerm(term12013);
		t9.setTeamName("Kungfu Panda");
		t9.setSupervisor(u6);
		t9.setReviewer1(u15);
		t9.setReviewer2(u7);
		HashSet<User> t9members = new HashSet<User>();
		t9members.add(u50);
		t9members.add(u51);
		t9members.add(u52);
		t9members.add(u53);
		t9members.add(u54);
		t9members.add(u55);
		t9.setMembers(t9members);
                
		Team t10 = new Team();
		t10.setTerm(term12013);
		t10.setTeamName("La Buena Vida");
		t10.setSupervisor(u7);
		t10.setReviewer1(u6);
		t10.setReviewer2(u15);
		HashSet<User> t10members = new HashSet<User>();
		t10members.add(u56);
		t10members.add(u57);
		t10members.add(u58);
		t10members.add(u59);
		t10members.add(u60);
		t10members.add(u61);
		t10.setMembers(t10members);
                
		// Persistence
		em.persist(t1);
		em.persist(t2);
		em.persist(t3);
		em.persist(t4);
		em.persist(t5);
		em.persist(t6);
		em.persist(t7);
		em.persist(t8);
		em.persist(t9);
		em.persist(t10);
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
                
		u22.setTeam(t4);
		u23.setTeam(t4);
		u24.setTeam(t4);
		u25.setTeam(t4);
		u26.setTeam(t4);
		
		u27.setTeam(t5);
		u28.setTeam(t5);
		u29.setTeam(t5);
		u30.setTeam(t5);
		u31.setTeam(t5);
		u32.setTeam(t5);
		
		u33.setTeam(t6);
		u34.setTeam(t6);
		u35.setTeam(t6);
		u36.setTeam(t6);
		u37.setTeam(t6);
		u38.setTeam(t6);
		
		u39.setTeam(t7);
		u40.setTeam(t7);
		u41.setTeam(t7);
		u42.setTeam(t7);
		u43.setTeam(t7);
		
		u44.setTeam(t8);
		u45.setTeam(t8);
		u46.setTeam(t8);
		u47.setTeam(t8);
		u48.setTeam(t8);
		u49.setTeam(t8);
		
		u50.setTeam(t9);
		u51.setTeam(t9);
		u52.setTeam(t9);
		u53.setTeam(t9);
		u54.setTeam(t9);
		u55.setTeam(t9);

		u56.setTeam(t10);
		u57.setTeam(t10);
		u58.setTeam(t10);
		u59.setTeam(t10);
		u60.setTeam(t10);
		u61.setTeam(t10);
		
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
		em.persist(u22);
		em.persist(u23);
		em.persist(u24);
		em.persist(u25);
		em.persist(u26);
		em.persist(u27);
		em.persist(u28);
		em.persist(u29);
		em.persist(u30);
		em.persist(u31);
		em.persist(u32);
		em.persist(u33);
		em.persist(u34);
		em.persist(u35);
		em.persist(u36);
		em.persist(u37);
		em.persist(u38);
		em.persist(u39);
		em.persist(u40);
		em.persist(u41);
		em.persist(u42);
		em.persist(u43);
		em.persist(u44);
		em.persist(u45);
		em.persist(u46);
		em.persist(u47);
		em.persist(u48);
		em.persist(u49);
		em.persist(u50);
		em.persist(u51);
		em.persist(u52);
		em.persist(u53);
		em.persist(u54);
		em.persist(u55);
		em.persist(u56);
		em.persist(u57);
		em.persist(u58);
		em.persist(u59);
		em.persist(u60);
		em.persist(u61);
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
