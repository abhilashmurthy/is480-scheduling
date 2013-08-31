/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package util;

import com.google.gson.Gson;
import constant.Role;
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
import model.Schedule;
import model.Settings;
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
			logger.error("DB Initialization Error:");
			logger.error(e.getMessage());
			for (StackTraceElement s : e.getStackTrace()) {
				logger.debug(s.toString());
			}
			em.getTransaction().rollback();
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
	
	private static void initDB(EntityManager em) throws Exception {
		
		/*
		 * TERM TABLE POPULATION
		 */		
		Term term12013 = new Term(2013, "Term 1");
		
		// Persistence
		em.persist(term12013);
		logger.info("Terms persisted");
		
		/*
		 * MILESTONE TABLE POPULATION
		 */
		Milestone acceptance = new Milestone();
		acceptance.setName("Acceptance");
		acceptance.setSlotDuration(60);
		acceptance.setTerm(term12013);
		
		Milestone midterm = new Milestone();
		midterm.setName("Midterm");
		midterm.setSlotDuration(90);
		midterm.setTerm(term12013);
		
		Milestone finalMilestone = new Milestone();
		finalMilestone.setName("Final");
		finalMilestone.setSlotDuration(90);
		finalMilestone.setTerm(term12013);
		
		// Persistence
		em.persist(acceptance);
		em.persist(midterm);
		em.persist(finalMilestone);
		logger.info("Milestones persisted");
		
		/*
		 * SCHEDULE TABLE POPULATION
		 */
		Schedule acceptance12013 = new Schedule();
		acceptance12013.setMilestone(acceptance);
		acceptance12013.setStartDate(new Timestamp(2013 - 1900, 7, 7, 0, 0, 0, 0));
		acceptance12013.setEndDate(new Timestamp(2013 - 1900, 7, 20, 0, 0, 0, 0));
		
		Schedule midterm12013 = new Schedule();
		midterm12013.setMilestone(midterm);
		midterm12013.setStartDate(new Timestamp(2013 - 1900, 9, 19, 0, 0, 0, 0));
		midterm12013.setEndDate(new Timestamp(2013 - 1900, 9, 30, 0, 0, 0, 0));
                
		Schedule final12013 = new Schedule();
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
		User uAdmin = new User("fionalee", "Fiona LEE", Role.ADMINISTRATOR, null);
		
		User uCourseCoordinator = new User("benjamingan", "Benjamin GAN Kok Siew", Role.COURSE_COORDINATOR, null);
		
		Student u1 = new Student("suresh.s.2010", "Suresh SUBRAMANIAM", term12013);
		
		Student u2 = new Student("abhilashm.2010", "Abhilash MURTHY", term12013);
		
		Student u3 = new Student("tsgill.ps.2010", "Tarlochan Singh GILL S/O P S", term12013);
		
		Student u4 = new Student("prakhara.2010", "Prakhar AGARWAL", term12013);
		
		Student u5 = new Student("xuling.dai.2010", "DAI Xuling", term12013);
		
		Faculty u6 = new Faculty("rcdavis", "Richard C. DAVIS", term12013);
		
		Faculty u7 = new Faculty("yskim", "Youngsoo KIM", term12013);
		
		Faculty u8 = new Faculty("laiteecheok", "CHEOK Lai-Tee", term12013);
                
		Student u9 = new Student("henry.tang.2011", "Henry TANG Ji Rui", term12013);
                
		Student u10 = new Student("ian.chan.2011", "Ian Clarence CHAN", term12013);
                
		Student u11 = new Student("jeremyzhong.2011", "Jeremy ZHONG Jiahao", term12013);
		
		Student u12 = new Student("xrlee.2011", "LEE Xiang Rui", term12013);
                
		Student u13 = new Student("vivian.lai.2011", "Vivian LAI Wan Yin", term12013);
                
		Student u14 = new Student("alvin.soh.2011", "Alvin SOH Wei Sheng", term12013);
               
		Faculty u15 = new Faculty("benjamingan", "Benjamin GAN Kok Siew", term12013);
                
		Student u16 = new Student("mfaizal.s.2010", "Muhammad Faizal SUKIM", term12013);
                
		Student u17 = new Student("huimin.hong.2011", "HONG Huimin", term12013);
                
		Student u18 = new Student("shaorui.lei.2011", "LEI Shaorui", term12013);
                
		Student u19 = new Student("zhuoran.li.2011", "LI Zhuoran", term12013);
                
		Student u20 = new Student("jz.peng.2011", "PENG Jian Zhang", term12013);
                
		Faculty u21 = new Faculty("cboesch", "Chris BOESCH", term12013);
		
		Student u22 = new Student("yt.ning.2011", "NING Yuting", term12013);

		Student u23 = new Student("duo.li.2011", "LI Duo", term12013);

		Student u24 = new Student("haryono.2011", "HARYONO", term12013);

		Student u25 = new Student("yufu.2011", "FU Yu", term12013);

		Student u26 = new Student("canwang.2011", "WANG Can", term12013);

		Student u27 = new Student("fzsun.2011", "SUN Fangzhou", term12013);

		Student u28 = new Student("lu.yang.2011", "YANG Lu", term12013);

		Student u29 = new Student("wenxuan.he.2011", "HE Wenxuan", term12013);

		Student u30 = new Student("jifei.zhang.2010", "ZHANG Jifei", term12013);

		Student u31 = new Student("tao.liang.2011", "LIANG Tao", term12013);

		Student u32 = new Student("miao.gao.2010", "GAO Miao", term12013);

		Student u33 = new Student("joelbb.p.2010", "PEREIRA Joel Bernardo Bosco", term12013);

		Student u34 = new Student("bixia.ang.2010", "ANG Bi Xia", term12013);

		Student u35 = new Student("yiying.tan.2010", "TAN Yi Ying", term12013);

		Student u36 = new Student("suansen.yeo.2010", "YEO Suan Sen", term12013);

		Student u37 = new Student("iadarmawan.2010", "Indra Adam DARMAWAN", term12013);

		Student u38 = new Student("lynetteseah.2010", "Lynette SEAH Pei Jie", term12013);

		Student u39 = new Student("jane.lee.2011", "Jane LEE Xue li", term12013);

		Student u40 = new Student("shena.ong.2011", "Shena ONG Wei Ting", term12013);

		Student u41 = new Student("edmund.gair.2010", "Edmund GAIR Jun Jie", term12013);

		Student u42 = new Student("kaicong.loh.2011", "LOH Kai Cong", term12013);

		Student u43 = new Student("yanjun.tan.2011", "TAN Yan Jun", term12013);

		Student u44 = new Student("weiyang.sim.2011", "SIM Wei Yang", term12013);

		Student u45 = new Student("sy.chia.2011", "CHIA Sheng Yang", term12013);

		Student u46 = new Student("junkiat.koh.2011", "KOH Jun Kiat", term12013);

		Student u47 = new Student("billy.lam.2011", "Billy LAM Wai Loon", term12013);

		Student u48 = new Student("rosannechoo.2011", "Rosanne CHOO Sweet Cin", term12013);

		Student u49 = new Student("wjwee.2011", "WEE Wei Jian", term12013);

		Student u50 = new Student("yh.koon.2010", "Geraldine KOON Yuhua", term12013);

		Student u51 = new Student("juntao.zhu.2010", "ZHU Juntao", term12013);

		Student u52 = new Student("yg.tan.2010", "TAN Yao Guang", term12013);

		Student u53 = new Student("james.lim.2010", "James LIM Xing Yan", term12013);

		Student u54 = new Student("kevin.ng.2010", "Kevin NG Ying Yi", term12013);

		Student u55 = new Student("jonathan.ho.2010", "Jonathan HO Jian Wei", term12013);

		Student u56 = new Student("jolie.lee.2010", "Jolie LEE Jia Ling", term12013);

		Student u57 = new Student("radeyap.2010", "Radeya PARVEEN", term12013);

		Student u58 = new Student("rosalind.ng.2010", "Rosalind NG Hsiu Zhen", term12013);

		Student u59 = new Student("sitiz.k.2010", "Siti Zulaiha BTE KAMARUDIN", term12013);

		Student u60 = new Student("lionel.koh.2010", "Lionel KOH Wee Heng", term12013);

		Student u61 = new Student("xinyi.song.2010", "SONG Xinyi", term12013);

		// Persistence
		em.persist(uAdmin);
		em.persist(uCourseCoordinator);
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
		t1.setReviewer1(u7);
		t1.setReviewer2(u8);
		HashSet<Student> members = new HashSet<Student>();
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
		HashSet<Student> t2members = new HashSet<Student>();
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
		HashSet<Student> t3members = new HashSet<Student>();
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
		HashSet<Student> t4members = new HashSet<Student>();
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
		HashSet<Student> t5members = new HashSet<Student>();
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
		HashSet<Student> t6members = new HashSet<Student>();
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
		HashSet<Student> t7members = new HashSet<Student>();
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
		HashSet<Student> t8members = new HashSet<Student>();
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
		HashSet<Student> t9members = new HashSet<Student>();
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
		HashSet<Student> t10members = new HashSet<Student>();
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
		
		Settings defaultTerm = new Settings();
		defaultTerm.setName("defaultTerm");
		defaultTerm.setValue(term12013.getId().toString());
		
		Settings milestones = new Settings();
		milestones.setName("milestones");
		ArrayList<HashMap<String,Object>> milestoneList = new ArrayList<HashMap<String, Object>>();
		HashMap<String,Object> accMap = new HashMap<String, Object>();
		accMap.put("order", 1);
		accMap.put("milestone", "Acceptance");
		accMap.put("duration", 60);
		String[] accReqList = {"Supervisor"};
		accMap.put("attendees", accReqList);
		milestoneList.add(accMap);
		HashMap<String,Object> midMap = new HashMap<String, Object>();
		midMap.put("order", 2);
		midMap.put("milestone", "Midterm");
		midMap.put("duration", 90);
		String[] midReqList = {"Reviewer1", "Reviewer2"};
		midMap.put("attendees", midReqList);
		milestoneList.add(midMap);
		HashMap<String,Object> finMap = new HashMap<String, Object>();
		finMap.put("order", 3);
		finMap.put("milestone", "Final");
		finMap.put("duration", 90);
		String[] finReqList = {"Supervisor", "Reviewer1"};
		finMap.put("attendees", finReqList);
		milestoneList.add(finMap);
		milestones.setValue(new Gson().toJson(milestoneList));
		
		//Persistence
		em.persist(activeTerms);
		em.persist(defaultTerm);
		em.persist(milestones);
	}
}