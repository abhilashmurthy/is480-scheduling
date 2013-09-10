///*
// * To change this template, choose Tools | Templates
// * and open the template in the editor.
// */
//package util;
//
//import com.google.gson.Gson;
//import constant.Role;
//import java.sql.Connection;
//import java.sql.DriverManager;
//import java.sql.SQLException;
//import java.sql.Statement;
//import java.sql.Timestamp;
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.HashSet;
//import java.util.logging.Level;
//import javax.persistence.EntityManager;
//import javax.persistence.Persistence;
//import model.Milestone;
//import model.Schedule;
//import model.Settings;
//import model.Team;
//import model.Term;
//import model.Timeslot;
//import model.User;
//import model.role.Faculty;
//import model.role.Student;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
///**
// * Contains method to initialize data in a new database.
// * WARNING! Please run this file only on a blank database!
// * @author suresh
// */
//public class UserTestingDB {
//	
//	static Logger logger = LoggerFactory.getLogger(DBInitUtil.class);
//        static {
//            try {
//		logger.info("DB Creation started");     
//                resetDB();
//                logger.info("DB Creation complete");
//            } catch (Exception ex) {
//                java.util.logging.Logger.getLogger(DBInitUtil.class.getName()).log(Level.SEVERE, null, ex);
//            }
//        }
//	
//	/**
//	 * Method to initialize data in a new database.
//	 * WARNING! Please run this file only on a blank database!
//	 */
//	public static void main(String[] args) {
//		EntityManager em = Persistence.createEntityManagerFactory("scheduler").createEntityManager();
//		try {
//			logger.info("DB Initialization started");   
//			em.getTransaction().begin();
//			initDB(em);
//			em.getTransaction().commit();
//			logger.info("DB Initialization complete");
//		} catch (Exception e) {
//			logger.error("DB Initialization Error:");
//			logger.error(e.getMessage());
//			for (StackTraceElement s : e.getStackTrace()) {
//				logger.debug(s.toString());
//			}
//			em.getTransaction().rollback();
//		}
//		
//	}
//        
//        private static void resetDB() throws Exception {
//            String url = "jdbc:mysql://localhost:3306/";
//            String username = "root";
//            String password = null;
//            String dbName = "is480-scheduling";
//            Connection conn = null;
//            Statement stmt = null;
//            try {
//                Class.forName("com.mysql.jdbc.Driver");
//                logger.debug("Connecting to phpmyadmin..");
//                conn = DriverManager.getConnection(url, username, password);
//                stmt = conn.createStatement();
//                try {
//                    stmt.executeUpdate("CREATE DATABASE `" + dbName + "`");
//                } catch (SQLException s) {
//                    logger.debug("Database exists. Dropping and creating again.");
//                    stmt.executeUpdate("DROP DATABASE `" + dbName + "`");
//                    stmt.executeUpdate("CREATE DATABASE `" + dbName + "`");
//                }
//                logger.debug("Database created successfully");
//            } catch (Exception e) {
//                logger.error("Exception caught! " + e.getMessage());
//            } finally {
//                if (stmt != null) {
//                    stmt.close();
//                    conn.close();
//                }
//            }
//        }
//	
//	private static void initDB(EntityManager em) throws Exception {
//		
//		/*
//		 * TERM TABLE POPULATION
//		 */		
//		Term term22013 = new Term(2013,"Term 2");
//		
//		// Persistence
//		em.persist(term22013);
//		logger.info("Terms persisted");
//		
//		/*
//		 * MILESTONE TABLE POPULATION
//		 */
//		Milestone acceptance = new Milestone();
//		acceptance.setName("Acceptance");
//		acceptance.setSlotDuration(60);
//		acceptance.setTerm(term22013);
//		
//		Milestone midterm = new Milestone();
//		midterm.setName("Midterm");
//		midterm.setSlotDuration(90);
//		midterm.setTerm(term22013);
//		
//		Milestone finalMilestone = new Milestone();
//		finalMilestone.setName("Final");
//		finalMilestone.setSlotDuration(90);
//		finalMilestone.setTerm(term22013);
//		
//		// Persistence
//		em.persist(acceptance);
//		em.persist(midterm);
//		em.persist(finalMilestone);
//		logger.info("Milestones persisted");
//		
//		/*
//		 * SCHEDULE TABLE POPULATION
//		 */
//		Schedule acceptance12013 = new Schedule();
//		acceptance12013.setMilestone(acceptance);
//		//acceptance12013.setStartDate(new Timestamp(2013 - 1900, 7, 7, 0, 0, 0, 0));
//		//acceptance12013.setEndDate(new Timestamp(2013 - 1900, 7, 20, 0, 0, 0, 0));
//                acceptance12013.setStartDate(new Timestamp(2013 - 1900, 10, 5, 0, 0, 0, 0));
//		acceptance12013.setEndDate(new Timestamp(2013 - 1900, 10, 8, 0, 0, 0, 0));
//		/*
//		 Acceptance Dates for Term 2
//		 5 Nov Tue: 1pm, 2pm, 3pm, 4pm, 5pm, 6pm
//		 6 Nov Wed: 10am, 11am, 1pm, 2pm, 3pm, 4pm, 5pm, 6pm
//		 7 Nov Thurs: 10am, 11am, 1pm, 2pm, 3pm, 4pm, 5pm, 6pm
//		 8 Nov Fri: 10am, 11am, 1pm, 2pm
//		*/
//		 
//		
//		Schedule midterm22013 = new Schedule();
//		midterm22013.setMilestone(midterm);
//		//midterm22013.setStartDate(new Timestamp(2013 - 1900, 9, 19, 0, 0, 0, 0));
//		//midterm22013.setEndDate(new Timestamp(2013 - 1900, 9, 30, 0, 0, 0, 0));
//                midterm22013.setStartDate(new Timestamp(2014 - 1900, 1, 17, 0, 0, 0, 0));
//		midterm22013.setEndDate(new Timestamp(2014 - 1900, 1, 26, 0, 0, 0, 0));
//        /*
//		 Mid Term Dates for Term 2
//		 17 Feb Mon: 9.30am, 11am, 1.30pm, 3pm, 4.30pm, 6pm
//		 18 Feb Tue: 9.30am, 11am, 1.30pm, 3pm, 4.30pm, 6pm
//		 19 Feb Wed: 9.30am, 11am, 1.30pm, 3pm, 4.30pm, 6pm
//		 20 Feb Thurs: 9.30am, 11am, 1.30pm, 3pm, 4.30pm, 6pm
//		 21 Feb Fri: 9.30am, 11am, 1.30pm, 3pm, 4.30pm, 6pm
//		 24 Feb Mon: 9.30am, 11am, 1.30pm, 3pm, 4.30pm, 6pm
//		 25 Feb Tues: 9.30am, 11am, 1.30pm, 3pm, 4.30pm, 6pm
//		 26 Feb Wed: 9.30am, 11am, 1.30pm, 3pm, 4.30pm, 6pm
//		*/
//        
//		Schedule final12013 = new Schedule();
//		final12013.setMilestone(finalMilestone);
//		final12013.setStartDate(new Timestamp(2014 - 1900, 3, 14, 0, 0, 0, 0));
//		final12013.setEndDate(new Timestamp(2014 - 1900, 3, 23, 0, 0, 0, 0));
//		/*
//		 Final Dates for Term 2
//		 14 Apr Mon: 9.30am, 11am, 1.30pm, 3pm, 4.30pm, 6pm
//		 15 Apr Tues: 9.30am, 11am, 1.30pm, 3pm, 4.30pm, 6pm
//		 16 Apr Wed: 9.30am, 11am, 1.30pm, 3pm, 4.30pm, 6pm
//		 17 Apr Thurs: 9.30am, 11am, 1.30pm, 3pm, 4.30pm, 6pm
//		 18 Apr Fri: 9.30am, 11am, 1.30pm, 3pm, 4.30pm, 6pm
//		 21 Apr Mon: 9.30am, 11am, 1.30pm, 3pm, 4.30pm, 6pm
//		 22 Apr Tues: 9.30am, 11am, 1.30pm, 3pm, 4.30pm, 6pm
//		 23 Apr Wed: 9.30am, 11am, 1.30pm, 3pm, 4.30pm, 6pm
//		 
//		*/
//		// Persistence
//		em.persist(acceptance12013);
//		em.persist(midterm22013);
//		em.persist(final12013);
//		logger.info("Schedule persisted");
//		
//		/*
//		 * TIMESLOT TABLE POPULATION
//		 */
//                //Acceptance
//		for (int a = 5; a <= 8; a++) {
//			// Skipping weekends
//			/*if (a == 10 || a == 11 || a == 17 || a == 18) {
//				continue;
//			}*/
//			
//			for (int b = 9; b <= 18; b++) {
//				Timeslot t = new Timeslot();
//                                if(a==5 && (b == 13 || b == 14 || b== 15 || b==16 || b==17 || b == 18)){
//                                    t.setStartTime(new Timestamp(2013 - 1900, 10, a, b, 0, 0, 0));
//                                    t.setEndTime(new Timestamp(2013 - 1900, 10, a, b + 1, 0, 0, 0));
//                                    t.setVenue("SIS Seminar Room 2-1");
//                                    t.setSchedule(acceptance12013);
//                                    em.persist(t);
//                                    
//                                }else if(a==6 && (b == 10 || b == 11 || b== 13 || b==14 || b==15 || b == 16 || b==17 || b==18)){
//                                    t.setStartTime(new Timestamp(2013 - 1900, 10, a, b, 0, 0, 0));
//                                    t.setEndTime(new Timestamp(2013 - 1900, 10, a, b + 1, 0, 0, 0));
//                                    t.setVenue("SIS Seminar Room 2-1");
//                                    t.setSchedule(acceptance12013);
//                                    em.persist(t);
//                                }else if(a==7 && (b == 10 || b == 11 || b== 13 || b==14 || b==15 || b == 16 || b==17 || b==18) ){
//                                    t.setStartTime(new Timestamp(2013 - 1900, 10, a, b, 0, 0, 0));
//                                    t.setEndTime(new Timestamp(2013 - 1900, 10, a, b + 1, 0, 0, 0));
//                                    t.setVenue("SIS Seminar Room 2-1");
//                                    t.setSchedule(acceptance12013);
//                                    em.persist(t);
//                                }else if(a==8 && (b == 10 || b == 11 || b== 13 || b==14)){
//                                    t.setStartTime(new Timestamp(2013 - 1900, 10, a, b, 0, 0, 0));
//                                    t.setEndTime(new Timestamp(2013 - 1900, 10, a, b + 1, 0, 0, 0));
//                                    t.setVenue("SIS Seminar Room 2-1");
//                                    t.setSchedule(acceptance12013);
//                                    em.persist(t);
//                                }
//                                
//				
//			}	
//		}
//                
//		//Midterm
//		for (int a = 17; a <= 26; a++) {
//			// Skipping weekends
//			if (a == 22 || a == 23) {
//				continue;
//			}
//			
//                        boolean change = true;
//			for (int b = 9; b <= 18; b++) {
//				Timeslot t = new Timeslot();
//                               
//                                    if((b == 9)){
//                                        t.setStartTime(new Timestamp(2014 - 1900, 1, a, b, 30, 0, 0));
//                                        t.setEndTime(new Timestamp(2014 - 1900, 1, a, b + 2, 0, 0, 0));
//                                        t.setVenue("SIS Seminar Room 2-1");
//                                        t.setSchedule(midterm22013);
//                                        em.persist(t);
//                                    }
//                               
//                                    if((b == 11 || b==15 || b==18)){
//                                        t.setStartTime(new Timestamp(2014 - 1900, 1, a, b, 00, 0, 0));
//                                        t.setEndTime(new Timestamp(2014 - 1900, 1, a, b +1, 30, 0, 0));
//                                        t.setVenue("SIS Seminar Room 2-1");
//                                        t.setSchedule(midterm22013);
//                                        em.persist(t);
//                                        //b++;
//                                    }else if((b == 13 || b == 16)){
//                                        t.setStartTime(new Timestamp(2014 - 1900, 1, a, b, 30, 0, 0));
//                                        t.setEndTime(new Timestamp(2014 - 1900, 1, a, b + 2, 0, 0, 0));
//                                        t.setVenue("SIS Seminar Room 2-1");
//                                        t.setSchedule(midterm22013);
//                                        em.persist(t);
//                                        //b++;
//                                    }
//                                
//                        }
//                                
//		}
//                
//		//Final
//		for (int a = 14; a <= 23; a++) {
//			// Skipping weekends
//			if (a == 19 || a == 20) {
//				continue;
//			}
//			
//                        boolean change = true;
//			for (int b = 9; b <= 18; b++) {
//				Timeslot t = new Timeslot();
//                                
//                                 if((b == 9)){
//                                        t.setStartTime(new Timestamp(2014 - 1900, 3, a, b, 30, 0, 0));
//                                        t.setEndTime(new Timestamp(2014 - 1900, 3, a, b + 2, 0, 0, 0));
//                                        t.setVenue("SIS Seminar Room 2-1");
//                                        t.setSchedule(final12013);
//                                        em.persist(t);
//                                    }
//                               
//                                    if((b == 11 || b==15 || b==18)){
//                                        t.setStartTime(new Timestamp(2014 - 1900, 3, a, b, 00, 0, 0));
//                                        t.setEndTime(new Timestamp(2014 - 1900, 3, a, b +1, 30, 0, 0));
//                                        t.setVenue("SIS Seminar Room 2-1");
//                                        t.setSchedule(final12013);
//                                        em.persist(t);
//                                        //b++;
//                                    }else if((b == 13 || b == 16)){
//                                        t.setStartTime(new Timestamp(2014 - 1900, 3, a, b, 30, 0, 0));
//                                        t.setEndTime(new Timestamp(2014 - 1900, 3, a, b + 2, 0, 0, 0));
//                                        t.setVenue("SIS Seminar Room 2-1");
//                                        t.setSchedule(final12013);
//                                        em.persist(t);
//                                        //b++;
//                                    }
//                                
//			}	
//		}
//		/*
//		 * USER TABLE POPULATION
//		 */
//		User uAdmin = new User("fionalee", "Fiona LEE", Role.ADMINISTRATOR, null);
//		
//		User uCourseCoordinator = new User("benjamingan", "Benjamin GAN Kok Siew", Role.COURSE_COORDINATOR, null);
//		
//		Faculty u1 = new Faculty("rcdavis", "Richard C. DAVIS", term22013);
//		
//		Faculty u2 = new Faculty("yskim", "Youngsoo KIM", term22013);
//		
//		Faculty u3 = new Faculty("laiteecheok", "CHEOK Lai-Tee", term22013);
//		
//		Faculty u4 = new Faculty("benjamingan", "Benjamin GAN Kok Siew", term22013);
//		
//		Faculty u5 = new Faculty("cboesch", "Chris BOESCH", term22013);
//		
//		Faculty u6 = new Faculty ("alanmegargel","Alan MEGARGEL", term22013);
//		
//		Faculty u7 = new Faculty ("davidlo","David LO", term22013);
//		
//		Faculty u8 = new Faculty ("lxjiang","JIANG Lingxiao", term22013);
//		
//		Faculty u9 = new Faculty("seemac","Seema CHOKSHI", term22013);
//		
//		Student u10 = new Student("lilong.lim.2011", "LIM Li Long", term22013);
//		
//		Student u11 = new Student("kimberlylek.2011", "Kimberly LEK Hui Lin", term22013);
//		
//		Student u12 = new Student("zhiyue.tay.2011", "TAY Zhi Yue", term22013);
//		
//		Student u13 = new Student("jy.ngoo.2011", "NGOO Jing Yong", term22013);
//		
//		Student u14 = new Student("zylim.2011", "LIM Zui Young", term22013);
//		
//		Student u15 = new Student("wl.chung.2011", "William CHUNG Wui Lun", term22013);
//		
//		Student u16 = new Student("jen.low.2011", "Jen LOW Ren Jie", term22013);
//		
//		Student u17 = new Student("quanhenglim.2011", "LIM Quan Heng", term22013);
//		
//		Student u18 = new Student("kr.tan.2011", "Benjamin TAN Kei Rong", term22013);
//		
//		Student u19 = new Student("amhollen.2011", "Anthony Marlius HOLLEN", term22013);
//		
//		Student u20 = new Student("huishia.tay.2011", "TAY Hui Shia", term22013);
//		
//		Student u21 = new Student("wctan.2011", "TAN Wei Chong", term22013);
//		
//		Student u22 = new Student("tommy.soh.2011", "Tommy SOH Jun Kui", term22013);
//		
//		Student u23 = new Student("hector.sim.2011", "Hector SIM Yiu Shin", term22013);
//		
//		Student u24 = new Student("jl.tan.2010", "TAN Jun Liang", term22013);
//		
//		Student u25 = new Student("leslie.leow.2011", "Leslie LEOW Jun Qiang", term22013);
//		
//		Student u26 = new Student("flwang.2011", "WANG Fenglin", term22013);
//		
//		Student u27 = new Student("chgoh.2011", "GOH Chin Hong", term22013);
//		
//		Student u28 = new Student("wyner.lim.2011", "Wyner LIM Wei Han", term22013);
//		
//		Student u29 = new Student("fuhua.shih.2011", "SHIH Fu Hua", term22013);
//		
//		Student u30 = new Student("chloechiang.2011", "Chloe CHIANG Foon Hui", term22013);
//		
//		Student u31 = new Student("danial.koh.2011", "Danial Asri KOH", term22013);
//		
//		Student u32 = new Student("ralewis.2011", "Rachelle Amanda LEWIS", term22013);
//		
//		Student u33 = new Student("weeta.lim.2011", "LIM Wee Ta", term22013);
//		
//		Student u34 = new Student("tianwen.tan.2011", "TAN Tian Wen", term22013);
//		
//		Student u35 = new Student("arethachang.2011", "Aretha CHANG Shu Hui", term22013);
//		
//		Student u36 = new Student("zhenzhi.yeo.2011", "YEO Zhen Zhi", term22013);
//		
//		Student u37 = new Student("spteo.2011", "TEO Siok Ping", term22013);
//		
//		Student u38 = new Student("qian.ye.2011", "YE Qian", term22013);
//		
//		Student u39 = new Student("ec.cheong.2011", "Michael CHEONG Ee Chien", term22013);
//		
//		Student u40 = new Student("willie.toh.2011", "TOH Willie", term22013);
//		
//		Student u41 = new Student("jolie.lee.2010", "Jolie LEE Jia Ling", term22013);
//
//		Student u42 = new Student("radeyap.2010", "Radeya PARVEEN", term22013);
//
//		Student u43 = new Student("rosalind.ng.2010", "Rosalind NG Hsiu Zhen", term22013);
//
//		Student u44 = new Student("sitiz.k.2010", "Siti Zulaiha BTE KAMARUDIN", term22013);
//
//		Student u45 = new Student("lionel.koh.2010", "Lionel KOH Wee Heng", term22013);
//
//		Student u46 = new Student("xinyi.song.2010", "SONG Xinyi", term22013);
//		
//		Student u47 = new Student("yh.koon.2010", "Geraldine KOON Yuhua", term22013);
//
//		Student u48 = new Student("juntao.zhu.2010", "ZHU Juntao", term22013);
//
//		Student u49 = new Student("yg.tan.2010", "TAN Yao Guang", term22013);
//
//		Student u50 = new Student("james.lim.2010", "James LIM Xing Yan", term22013);
//
//		Student u51 = new Student("kevin.ng.2010", "Kevin NG Ying Yi", term22013);
//
//		Student u52 = new Student("jonathan.ho.2010", "Jonathan HO Jian Wei", term22013);
//		
//		Student u53 = new Student("wahchun.ng.2011", "NG Wah Chun", term22013);
//		
//		Student u54 = new Student("glen.wong.2011", "Glen WONG Kee Siang", term22013);
//		
//		Student u55 = new Student("mohamedsh.2011", "Mohamed Yousof Bin SHAMSUL HAMEED", term22013);
//		
//		Student u56 = new Student("fariq.said.2011", "Fariq Bin SAID", term22013);
//		
//		Student u57 = new Student("shanaazmk.2011", "Shanaaz Do Musthafa MUSTHAFA KAMAL", term22013);
//		
//		Student u58 = new Student("engsen.kee.2011", "KEE Eng Sen", term22013);
//		
//		Student u59 = new Student("lynnettelim.2011", "Lynnette LIM Wen Zheng", term22013);
//		
//		Student u60 = new Student("scong.2011", "ONG Sen Chian", term22013);
//		
//		Student u61 = new Student("wq.siah.2011", "SIAH Wei Qiang", term22013);
//		
//		Student u62 = new Student("jinglong.wu.2011", "WU Jinglong", term22013);
//		
//		Student u63 = new Student("kaifeng.2011", "FENG Kai", term22013);
//		
//		Student u64 = new Student("eytanojo.2011","Edward Young TANOJO",term22013);
//		
//		Student u65 = new Student("akkustedjo.2011","Alfred Kusuma KUSTEDJO",term22013);
//		
//		Student u66 = new Student("antonysalim.2011","Antony SALIM",term22013);
//		
//		Student u67 = new Student("xi.chen.2011","CHEN Xi",term22013);
//		
//		Student u68 = new Student("dewi.zana.2011","Dewi ZANA",term22013);
//                
//                Student u69 = new Student("weizhenglai.2011","LAI Weizheng",term22013);
//		
//		Student u70 = new Student("robyn.cheng.2011","Robyn  CHENG King",term22013);
//		
//		Student u71 = new Student("jingyi.lim.2011","LIM Jing Yi",term22013);
//		
//		Student u72 = new Student("hanhui.koh.2011","KOH Han Hui",term22013);
//		
//		Student u73 = new Student("chelsea.toh.2011","Chelsea TOH Miaoxing",term22013);
//
//
//		// Persistence
//		em.persist(uAdmin);
//		em.persist(uCourseCoordinator);
//		em.persist(u1);
//		em.persist(u2);
//		em.persist(u3);
//		em.persist(u4);
//		em.persist(u5);
//		em.persist(u6);
//		em.persist(u7);
//		em.persist(u8);
//		em.persist(u9);
//		em.persist(u10);
//		em.persist(u11);
//		em.persist(u12);
//		em.persist(u13);
//		em.persist(u14);
//		em.persist(u15);
//		em.persist(u16);
//		em.persist(u17);
//		em.persist(u18);
//		em.persist(u19);
//		em.persist(u20);
//		em.persist(u21);
//		em.persist(u22);
//		em.persist(u23);
//		em.persist(u24);
//		em.persist(u25);
//		em.persist(u26);
//		em.persist(u27);
//		em.persist(u28);
//		em.persist(u29);
//		em.persist(u30);
//		em.persist(u31);
//		em.persist(u32);
//		em.persist(u33);
//		em.persist(u34);
//		em.persist(u35);
//		em.persist(u36);
//		em.persist(u37);
//		em.persist(u38);
//		em.persist(u39);
//		em.persist(u40);
//		em.persist(u41);
//		em.persist(u42);
//		em.persist(u43);
//		em.persist(u44);
//		em.persist(u45);
//		em.persist(u46);
//		em.persist(u47);
//		em.persist(u48);
//		em.persist(u49);
//		em.persist(u50);
//		em.persist(u51);
//		em.persist(u52);
//		em.persist(u53);
//		em.persist(u54);
//		em.persist(u55);
//		em.persist(u56);
//		em.persist(u57);
//		em.persist(u58);
//		em.persist(u59);
//		em.persist(u60);
//		em.persist(u61);
//		em.persist(u62);
//		em.persist(u63);
//		em.persist(u64);
//		em.persist(u65);
//		em.persist(u66);
//		em.persist(u67);
//		em.persist(u68);
//                em.persist(u69);
//		em.persist(u70);
//		em.persist(u71);
//		em.persist(u72);
//		em.persist(u73);
//		
//		logger.info("Users persisted");
//		
//		/*
//		 * TEAM TABLE POPULATION
//		 */
//		Team t1 = new Team();
//		t1.setTerm(term22013);
//		t1.setTeamName("SixDotz");
//		t1.setSupervisor(u4);
//		t1.setReviewer1(u5);
//		t1.setReviewer2(u6);
//		HashSet<Student> members = new HashSet<Student>();
//		members.add(u10);
//		members.add(u11);
//		members.add(u12);
//		members.add(u13);
//		members.add(u14);
//		t1.setMembers(members);
//                
//                
//		Team t2 = new Team();
//		t2.setTerm(term22013);
//		t2.setTeamName("Invenio");
//		t2.setSupervisor(u8);
//		t2.setReviewer1(u4);
//		t2.setReviewer2(u5);
//		HashSet<Student> t2members = new HashSet<Student>();
//		t2members.add(u15);
//		t2members.add(u16);
//		t2members.add(u17);
//		t2members.add(u18);
//		t2.setMembers(t2members);
//		
//		Team t3 = new Team();
//		t3.setTerm(term22013);
//		t3.setTeamName("AnthonyTeam");
//		t3.setSupervisor(u4);
//		t3.setReviewer1(u7);
//		t3.setReviewer2(u8);
//		HashSet<Student> t3members = new HashSet<Student>();
//		t3members.add(u19);
//		t3members.add(u20);
//		t3members.add(u21);
//		t3members.add(u22);
//		t3members.add(u23);
//		t3members.add(u24);
//		t3.setMembers(t3members);
//                
//		Team t4 = new Team();
//		t4.setTerm(term22013);
//		t4.setTeamName("FuhuaTeam");
//		t4.setSupervisor(u7);
//		t4.setReviewer1(u4);
//		t4.setReviewer2(u5);
//		HashSet<Student> t4members = new HashSet<Student>();
//		t4members.add(u25);
//		t4members.add(u26);
//		t4members.add(u27);
//		t4members.add(u28);
//		t4members.add(u29);
//		t4.setMembers(t4members);
//		
//		Team t5 = new Team();
//		t5.setTerm(term22013);
//		t5.setTeamName("LightningStrike");
//		t5.setSupervisor(u5);
//		t5.setReviewer1(u4);
//		t5.setReviewer2(u1);
//		HashSet<Student> t5members = new HashSet<Student>();
//		t5members.add(u30);
//		t5members.add(u31);
//		t5members.add(u32);
//		t5members.add(u33);
//		t5members.add(u34);
//		t5members.add(u35);
//		t5.setMembers(t5members);
//                
//		Team t6 = new Team();
//		t6.setTerm(term22013);
//		t6.setTeamName("LittleTeam");
//		t6.setSupervisor(u7);
//		t6.setReviewer1(u6);
//		t6.setReviewer2(u8);
//		HashSet<Student> t6members = new HashSet<Student>();
//		t6members.add(u36);
//		t6members.add(u37);
//		t6members.add(u38);
//		t6members.add(u39);
//		t6members.add(u40);
//		t6.setMembers(t6members);
//		
//		Team t7 = new Team();
//		t7.setTerm(term22013);
//		t7.setTeamName("La Buena Vida");
//		t7.setSupervisor(u2);
//		t7.setReviewer1(u1);
//		t7.setReviewer2(u4);
//		HashSet<Student> t7members = new HashSet<Student>();
//		t7members.add(u41);
//		t7members.add(u42);
//		t7members.add(u43);
//		t7members.add(u44);
//		t7members.add(u45);
//		t7members.add(u46);
//		t7.setMembers(t7members);
//		
//		Team t8 = new Team();
//		t8.setTerm(term22013);
//		t8.setTeamName("Kungfu Panda");
//		t8.setSupervisor(u1);
//		t8.setReviewer1(u5);
//		t8.setReviewer2(u2);
//		HashSet<Student> t8members = new HashSet<Student>();
//		t8members.add(u47);
//		t8members.add(u48);
//		t8members.add(u49);
//		t8members.add(u50);
//		t8members.add(u51);
//		t8members.add(u52);
//		t8.setMembers(t8members);
//		
//		
//		Team t9 = new Team();
//		t9.setTerm(term22013);
//		t9.setTeamName("GENShYFT");
//		t9.setSupervisor(u4);
//		t9.setReviewer1(u6);
//		t9.setReviewer2(u7);
//		HashSet<Student> t9members = new HashSet<Student>();
//		t9members.add(u53);
//		t9members.add(u54);
//		t9members.add(u55);
//		t9members.add(u56);
//		t9members.add(u57);
//		t9members.add(u58);
//		t9.setMembers(t9members);
//		
//		
//		Team t10 = new Team();
//		t10.setTerm(term22013);
//		t10.setTeamName("iChallenge");
//		t10.setSupervisor(u4);
//		t10.setReviewer1(u8);
//		t10.setReviewer2(u5);
//		HashSet<Student> t10members = new HashSet<Student>();
//		t10members.add(u59);
//		t10members.add(u60);
//		t10members.add(u61);
//		t10members.add(u62);
//		t10members.add(u63);
//		t10.setMembers(t10members);
//		
//		
//		Team t11 = new Team();
//		t11.setTerm(term22013);
//		t11.setTeamName("Bisa");
//		t11.setSupervisor(u1);
//		t11.setReviewer1(u7);
//		t11.setReviewer2(u8);
//		HashSet<Student> t11members = new HashSet<Student>();
//		t11members.add(u64);
//		t11members.add(u65);
//		t11members.add(u66);
//		t11members.add(u67);
//		t11members.add(u68);
//		t11.setMembers(t11members);
//                
//                Team t12 = new Team();
//		t12.setTerm(term22013);
//		t12.setTeamName("The Partners");
//		t12.setSupervisor(u7);
//		t12.setReviewer1(u6);
//		t12.setReviewer2(u8);
//		HashSet<Student> t12members = new HashSet<Student>();
//		t12members.add(u69);
//		t12members.add(u70);
//		t12members.add(u71);
//		t12members.add(u72);
//		t12members.add(u73);
//		t12.setMembers(t12members);
//                
//		// Persistence
//		em.persist(t1);
//		em.persist(t2);
//		em.persist(t3);
//		em.persist(t4);
//		em.persist(t5);
//		em.persist(t6);
//		em.persist(t7);
//		em.persist(t8);
//		em.persist(t9);
//		em.persist(t10);
//		em.persist(t11);
//                em.persist(t12);
//		logger.info("Teams persisted");
//		
//		/*
//		 * LINKING USERS AND TEAMS
//		 */
//		u10.setTeam(t1);
//		u11.setTeam(t1);
//		u12.setTeam(t1);
//		u13.setTeam(t1);
//		u14.setTeam(t1);
//                
//		u15.setTeam(t2);
//		u16.setTeam(t2);
//		u17.setTeam(t2);
//		u18.setTeam(t2);
//
//		u19.setTeam(t3);
//		u20.setTeam(t3);
//		u21.setTeam(t3);
//		u22.setTeam(t3);
//		u23.setTeam(t3);
//		u24.setTeam(t3);
//                
//		u25.setTeam(t4);
//		u26.setTeam(t4);
//		u27.setTeam(t4);
//		u28.setTeam(t4);
//		u29.setTeam(t4);
//		
//		u30.setTeam(t5);
//		u31.setTeam(t5);
//		u32.setTeam(t5);
//		u33.setTeam(t5);
//		u34.setTeam(t5);
//		u35.setTeam(t5);
//		
//		u36.setTeam(t6);
//		u37.setTeam(t6);
//		u38.setTeam(t6);
//		u39.setTeam(t6);
//		u40.setTeam(t6);
//		
//		u41.setTeam(t7);
//		u42.setTeam(t7);
//		u43.setTeam(t7);
//		u44.setTeam(t7);
//		u45.setTeam(t7);
//		u46.setTeam(t7);
//		
//		u47.setTeam(t8);
//		u48.setTeam(t8);
//		u49.setTeam(t8);
//		u50.setTeam(t8);
//		u51.setTeam(t8);
//		u52.setTeam(t8);
//		
//		u53.setTeam(t9);
//		u54.setTeam(t9);
//		u55.setTeam(t9);
//		u56.setTeam(t9);
//		u57.setTeam(t9);
//		u58.setTeam(t9);
//		
//		u59.setTeam(t10);
//		u60.setTeam(t10);
//		u61.setTeam(t10);
//		u62.setTeam(t10);
//		u63.setTeam(t10);
//		
//		u64.setTeam(t11);
//		u65.setTeam(t11);
//		u66.setTeam(t11);
//		u67.setTeam(t11);
//		u68.setTeam(t11);
//                
//                u69.setTeam(t12);
//		u70.setTeam(t12);
//		u71.setTeam(t12);
//		u72.setTeam(t12);
//		u73.setTeam(t12);
//		
//		
//		em.persist(u1);
//		em.persist(u2);
//		em.persist(u3);
//		em.persist(u4);
//		em.persist(u5);
//		em.persist(u9);
//		em.persist(u10);
//		em.persist(u11);
//		em.persist(u12);
//		em.persist(u13);
//		em.persist(u14);
//		em.persist(u15);
//		em.persist(u16);
//		em.persist(u17);
//		em.persist(u18);
//		em.persist(u19);
//		em.persist(u20);
//		em.persist(u21);
//		em.persist(u22);
//		em.persist(u23);
//		em.persist(u24);
//		em.persist(u25);
//		em.persist(u26);
//		em.persist(u27);
//		em.persist(u28);
//		em.persist(u29);
//		em.persist(u30);
//		em.persist(u31);
//		em.persist(u32);
//		em.persist(u33);
//		em.persist(u34);
//		em.persist(u35);
//		em.persist(u36);
//		em.persist(u37);
//		em.persist(u38);
//		em.persist(u39);
//		em.persist(u40);
//		em.persist(u41);
//		em.persist(u42);
//		em.persist(u43);
//		em.persist(u44);
//		em.persist(u45);
//		em.persist(u46);
//		em.persist(u47);
//		em.persist(u48);
//		em.persist(u49);
//		em.persist(u50);
//		em.persist(u51);
//		em.persist(u52);
//		em.persist(u53);
//		em.persist(u54);
//		em.persist(u55);
//		em.persist(u56);
//		em.persist(u57);
//		em.persist(u58);
//		em.persist(u59);
//		em.persist(u60);
//		em.persist(u61);
//		em.persist(u62);
//		em.persist(u63);
//		em.persist(u64);
//		em.persist(u65);
//		em.persist(u66);
//		em.persist(u67);
//		em.persist(u68);
//                em.persist(u69);
//		em.persist(u70);
//		em.persist(u71);
//		em.persist(u72);
//		em.persist(u73);
//		logger.info("User --> Team links persisted");
//		
//		/*
//		 * INITIALIZING SETTINGS
//		 */
//		Settings activeTerms = new Settings();
//		activeTerms.setName("activeTerms");
//		ArrayList<Long> activeTermIds = new ArrayList<Long>();
//		activeTermIds.add(term22013.getId());
//		activeTerms.setValue(new Gson().toJson(activeTermIds));
//		
//		Settings defaultTerm = new Settings();
//		defaultTerm.setName("defaultTerm");
//		defaultTerm.setValue(term22013.getId().toString());
//		
//		Settings milestones = new Settings();
//		milestones.setName("milestones");
//		ArrayList<HashMap<String,Object>> milestoneList = new ArrayList<HashMap<String, Object>>();
//		HashMap<String,Object> accMap = new HashMap<String, Object>();
//		accMap.put("order", 1);
//		accMap.put("milestone", "Acceptance");
//		accMap.put("duration", 60);
//		String[] accReqList = {"Supervisor"};
//		accMap.put("attendees", accReqList);
//		milestoneList.add(accMap);
//		HashMap<String,Object> midMap = new HashMap<String, Object>();
//		midMap.put("order", 2);
//		midMap.put("milestone", "Midterm");
//		midMap.put("duration", 90);
//		String[] midReqList = {"Reviewer1", "Reviewer2"};
//		midMap.put("attendees", midReqList);
//		milestoneList.add(midMap);
//		HashMap<String,Object> finMap = new HashMap<String, Object>();
//		finMap.put("order", 3);
//		finMap.put("milestone", "Final");
//		finMap.put("duration", 90);
//		String[] finReqList = {"Supervisor", "Reviewer1"};
//		finMap.put("attendees", finReqList);
//		milestoneList.add(finMap);
//		milestones.setValue(new Gson().toJson(milestoneList));
//		
//		//Persistence
//		em.persist(activeTerms);
//		em.persist(defaultTerm);
//		em.persist(milestones);
//	}
//}