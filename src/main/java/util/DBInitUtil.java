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
import java.util.Calendar;
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
import model.role.TA;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Contains method to initialize data in a new database. WARNING! Please run
 * this file only on a blank database!
 *
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
     * Method to initialize data in a new database. WARNING! Please run this
     * file only on a blank database!
     */
    public static void main(String[] args) {
        EntityManager em = Persistence.createEntityManagerFactory("scheduler").createEntityManager();
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
        } finally {
            if (em != null && em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            if (em != null && em.isOpen()) {
                em.close();
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
		ArrayList<String> acceptanceAttendees = new ArrayList<String>();
		acceptanceAttendees.add("Supervisor");
        Milestone acceptance = new Milestone(1, "Acceptance", 60, term12013, acceptanceAttendees);

		ArrayList<String> midtermAttendees = new ArrayList<String>();
		midtermAttendees.add("Reviewer1");
		midtermAttendees.add("Reviewer2");
        Milestone midterm = new Milestone(2, "Midterm", 90, term12013, midtermAttendees);

		ArrayList<String> finalAttendees = new ArrayList<String>();
		finalAttendees.add("Supervisor");
		finalAttendees.add("Reviewer1");
        Milestone finalMilestone = new Milestone(3, "Final", 90, term12013, finalAttendees);

        // Persistence
        em.persist(acceptance);
        em.persist(midterm);
        em.persist(finalMilestone);
        logger.info("Milestones persisted");

        /*
         * SCHEDULE TABLE POPULATION
         */
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DAY_OF_MONTH, 7); //Adding a week
        Schedule acceptance12013 = new Schedule();
        acceptance12013.setMilestone(acceptance);
		acceptance12013.setBookable(true);
		acceptance12013.setStartDate(new Timestamp(cal.get(Calendar.YEAR) - 1900, cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH), 0, 0, 0, 0));
		cal.add(Calendar.DAY_OF_MONTH, 10); //Schedule is 10 days long
		Timestamp accEndTimestamp = new Timestamp(cal.get(Calendar.YEAR) - 1900, cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH), 0, 0, 0, 0);
		//Calculating the real end of the schedule
		Calendar endCal = Calendar.getInstance();
		endCal.setTimeInMillis(accEndTimestamp.getTime());
		endCal.add(Calendar.DAY_OF_MONTH, 1); //Adding a day
		endCal.add(Calendar.MINUTE, - (acceptance.getSlotDuration())); //Subtracting the slot duration
		accEndTimestamp.setTime(endCal.getTimeInMillis());
        acceptance12013.setEndDate(accEndTimestamp);
        acceptance12013.setDayStartTime(9);
        acceptance12013.setDayEndTime(19);

        Schedule midterm12013 = new Schedule();
        midterm12013.setMilestone(midterm);
		midterm12013.setBookable(true);
		cal.add(Calendar.MONTH, 1); //Midterm is after another month
		midterm12013.setStartDate(new Timestamp(cal.get(Calendar.YEAR) - 1900, cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH), 0, 0, 0, 0));
		cal.add(Calendar.DAY_OF_MONTH, 10); //Schedule is 10 days long
        Timestamp midEndTimestamp = new Timestamp(cal.get(Calendar.YEAR) - 1900, cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH), 0, 0, 0, 0);
		//Calculating the real end of the schedule
		endCal.setTimeInMillis(midEndTimestamp.getTime());
		endCal.add(Calendar.DAY_OF_MONTH, 1); //Adding a day
		endCal.add(Calendar.MINUTE, - (midterm.getSlotDuration())); //Subtracting the slot duration
		midEndTimestamp.setTime(endCal.getTimeInMillis());
        midterm12013.setEndDate(midEndTimestamp);
        midterm12013.setDayStartTime(9);
        midterm12013.setDayEndTime(19);

        Schedule final12013 = new Schedule();
        final12013.setMilestone(finalMilestone);
		final12013.setBookable(true);
		cal.add(Calendar.MONTH, 1); //Final is after another month
		final12013.setStartDate(new Timestamp(cal.get(Calendar.YEAR) - 1900, cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH), 0, 0, 0, 0));
		cal.add(Calendar.DAY_OF_MONTH, 10); //Schedule is 10 days long
        Timestamp finEndTimestamp = new Timestamp(cal.get(Calendar.YEAR) - 1900, cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH), 0, 0, 0, 0);
		//Calculating the real end of the schedule
		endCal.setTimeInMillis(finEndTimestamp.getTime());
		endCal.add(Calendar.DAY_OF_MONTH, 1); //Adding a day
		endCal.add(Calendar.MINUTE, - (finalMilestone.getSlotDuration())); //Subtracting the slot duration
		finEndTimestamp.setTime(endCal.getTimeInMillis());
        final12013.setEndDate(finEndTimestamp);
        final12013.setDayStartTime(9);
        final12013.setDayEndTime(19);

        // Persistence
        em.persist(acceptance12013);
        em.persist(midterm12013);
        em.persist(final12013);
        logger.info("Schedule persisted");

        /*
         * TIMESLOT TABLE POPULATION
         */
        //Acceptance
		Calendar accStart = Calendar.getInstance(); Calendar accEnd = Calendar.getInstance();
		accStart.setTime(acceptance12013.getStartDate());
		accEnd.setTime(acceptance12013.getEndDate());
        while (accEnd.after(accStart) || accEnd.compareTo(accStart) == 0) {
            // Skipping weekends
            if (accStart.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY
					|| accStart.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
				accStart.add(Calendar.DAY_OF_MONTH, 1);
                continue;
            }

            for (int b = 9; b <= 18; b++) {
                Timeslot t = new Timeslot();
                t.setStartTime(new Timestamp(accStart.get(Calendar.YEAR) - 1900, accStart.get(Calendar.MONTH), accStart.get(Calendar.DAY_OF_MONTH), b, 0, 0, 0));
                t.setEndTime(new Timestamp(accStart.get(Calendar.YEAR) - 1900, accStart.get(Calendar.MONTH), accStart.get(Calendar.DAY_OF_MONTH), b + 1, 0, 0, 0));
                t.setVenue("SIS Seminar Room 2-1");
                t.setSchedule(acceptance12013);
                em.persist(t);
            }
			accStart.add(Calendar.DAY_OF_MONTH, 1); //Loop increment. 1 day
        }

        //Midterm
		Calendar midStart = Calendar.getInstance(); Calendar midEnd = Calendar.getInstance();
		midStart.setTime(midterm12013.getStartDate());
		midEnd.setTime(midterm12013.getEndDate());
        while (midEnd.after(midStart) || midEnd.compareTo(midStart) == 0) {
            // Skipping weekends
            if (midStart.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY
					|| midStart.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
				midStart.add(Calendar.DAY_OF_MONTH, 1);
                continue;
            }

            boolean change = true;
            for (int b = 9; (b + 2) <= 18; b++) {
                Timeslot t = new Timeslot();
                if (change) {
                    t.setStartTime(new Timestamp(midStart.get(Calendar.YEAR) - 1900, midStart.get(Calendar.MONTH), midStart.get(Calendar.DAY_OF_MONTH), b, 0, 0, 0));
                    t.setEndTime(new Timestamp(midStart.get(Calendar.YEAR) - 1900, midStart.get(Calendar.MONTH), midStart.get(Calendar.DAY_OF_MONTH), b + 1, 30, 0, 0));
                } else {
                    t.setStartTime(new Timestamp(midStart.get(Calendar.YEAR) - 1900, midStart.get(Calendar.MONTH), midStart.get(Calendar.DAY_OF_MONTH), b, 30, 0, 0));
                    t.setEndTime(new Timestamp(midStart.get(Calendar.YEAR) - 1900, midStart.get(Calendar.MONTH), midStart.get(Calendar.DAY_OF_MONTH), b + 2, 00, 0, 0));
                    b++;
                }
                t.setVenue("SIS Seminar Room 2-1");
                t.setSchedule(midterm12013);
                em.persist(t);
                change = !change;
            }
			midStart.add(Calendar.DAY_OF_MONTH, 1); //Loop increment. 1 day
        }

        //Final
        Calendar finStart = Calendar.getInstance(); Calendar finEnd = Calendar.getInstance();
		finStart.setTime(final12013.getStartDate());
		finEnd.setTime(final12013.getEndDate());
        while (finEnd.after(finStart) || finEnd.compareTo(finStart) == 0) {
            // Skipping weekends
            if (finStart.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY
					|| finStart.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
				finStart.add(Calendar.DAY_OF_MONTH, 1);
                continue;
            }

            boolean change = true;
            for (int b = 9; (b + 2) <= 18; b++) {
                Timeslot t = new Timeslot();
                if (change) {
                    t.setStartTime(new Timestamp(finStart.get(Calendar.YEAR) - 1900, finStart.get(Calendar.MONTH), finStart.get(Calendar.DAY_OF_MONTH), b, 0, 0, 0));
                    t.setEndTime(new Timestamp(finStart.get(Calendar.YEAR) - 1900, finStart.get(Calendar.MONTH), finStart.get(Calendar.DAY_OF_MONTH), b + 1, 30, 0, 0));
                } else {
                    t.setStartTime(new Timestamp(finStart.get(Calendar.YEAR) - 1900, finStart.get(Calendar.MONTH), finStart.get(Calendar.DAY_OF_MONTH), b, 30, 0, 0));
                    t.setEndTime(new Timestamp(finStart.get(Calendar.YEAR) - 1900, finStart.get(Calendar.MONTH), finStart.get(Calendar.DAY_OF_MONTH), b + 2, 00, 0, 0));
                    b++;
                }
                t.setVenue("SIS Seminar Room 2-1");
                t.setSchedule(final12013);
                em.persist(t);
                change = !change;
            }
			finStart.add(Calendar.DAY_OF_MONTH, 1); //Loop increment. 1 day
        }
		
        /*
         * USER TABLE POPULATION
         */
        User uAdmin = new User("fionalee", "Fiona LEE", null, Role.ADMINISTRATOR, null);

        User uCourseCoordinator = new User("benjamingan", "Benjamin GAN Kok Siew", null, Role.COURSE_COORDINATOR, null);

        TA ta1 = new TA("kjsebastian.2011", "SEBASTIAN Kiran Joy", null, term12013);
        TA ta2 = new TA("dudeTA", "Dude TA", null, term12013);

        Student u1 = new Student("suresh.s.2010", "Suresh SUBRAMANIAM", null, term12013);

        Student u2 = new Student("abhilashm.2010", "Abhilash MURTHY", null, term12013);

        Student u3 = new Student("tsgill.ps.2010", "Tarlochan Singh GILL S/O P S", null, term12013);

        Student u4 = new Student("prakhara.2010", "Prakhar AGARWAL", null, term12013);

        Student u5 = new Student("xuling.dai.2010", "DAI Xuling", null, term12013);

        Faculty u6 = new Faculty("rcdavis", "Richard C. DAVIS", null, term12013);

        Faculty u7 = new Faculty("yskim", "Youngsoo KIM", null, term12013);

        Faculty u8 = new Faculty("laiteecheok", "CHEOK Lai-Tee", null, term12013);

        Student u9 = new Student("henry.tang.2011", "Henry TANG Ji Rui", null, term12013);

        Student u10 = new Student("ian.chan.2011", "Ian Clarence CHAN", null, term12013);

        Student u11 = new Student("jeremyzhong.2011", "Jeremy ZHONG Jiahao", null, term12013);

        Student u12 = new Student("xrlee.2011", "LEE Xiang Rui", null, term12013);

        Student u13 = new Student("vivian.lai.2011", "Vivian LAI Wan Yin", null, term12013);

        Student u14 = new Student("alvin.soh.2011", "Alvin SOH Wei Sheng", null, term12013);

        Faculty u15 = new Faculty("benjamingan", "Benjamin GAN Kok Siew", null, term12013);

        Student u16 = new Student("mfaizal.s.2010", "Muhammad Faizal SUKIM", null, term12013);

        Student u17 = new Student("huimin.hong.2011", "HONG Huimin", null, term12013);

        Student u18 = new Student("shaorui.lei.2011", "LEI Shaorui", null, term12013);

        Student u19 = new Student("zhuoran.li.2011", "LI Zhuoran", null, term12013);

        Student u20 = new Student("jz.peng.2011", "PENG Jian Zhang", null, term12013);

        Faculty u21 = new Faculty("cboesch", "Chris BOESCH", null, term12013);

        Student u22 = new Student("yt.ning.2011", "NING Yuting", null, term12013);

        Student u23 = new Student("duo.li.2011", "LI Duo", null, term12013);

        Student u24 = new Student("haryono.2011", "HARYONO", null, term12013);

        Student u25 = new Student("yufu.2011", "FU Yu", null, term12013);

        Student u26 = new Student("canwang.2011", "WANG Can", null, term12013);

        Student u27 = new Student("fzsun.2011", "SUN Fangzhou", null, term12013);

        Student u28 = new Student("lu.yang.2011", "YANG Lu", null, term12013);

        Student u29 = new Student("wenxuan.he.2011", "HE Wenxuan", null, term12013);

        Student u30 = new Student("jifei.zhang.2010", "ZHANG Jifei", null, term12013);

        Student u31 = new Student("tao.liang.2011", "LIANG Tao", null, term12013);

        Student u32 = new Student("miao.gao.2010", "GAO Miao", null, term12013);

        Student u33 = new Student("joelbb.p.2010", "PEREIRA Joel Bernardo Bosco", null, term12013);

        Student u34 = new Student("bixia.ang.2010", "ANG Bi Xia", null, term12013);

        Student u35 = new Student("yiying.tan.2010", "TAN Yi Ying", null, term12013);

        Student u36 = new Student("suansen.yeo.2010", "YEO Suan Sen", null, term12013);

        Student u37 = new Student("iadarmawan.2010", "Indra Adam DARMAWAN", null, term12013);

        Student u38 = new Student("lynetteseah.2010", "Lynette SEAH Pei Jie", null, term12013);

        Student u39 = new Student("jane.lee.2011", "Jane LEE Xue li", null, term12013);

        Student u40 = new Student("shena.ong.2011", "Shena ONG Wei Ting", null, term12013);

        Student u41 = new Student("edmund.gair.2010", "Edmund GAIR Jun Jie", null, term12013);

        Student u42 = new Student("kaicong.loh.2011", "LOH Kai Cong", null, term12013);

        Student u43 = new Student("yanjun.tan.2011", "TAN Yan Jun", null, term12013);

        Student u44 = new Student("weiyang.sim.2011", "SIM Wei Yang", null, term12013);

        Student u45 = new Student("sy.chia.2011", "CHIA Sheng Yang", null, term12013);

        Student u46 = new Student("junkiat.koh.2011", "KOH Jun Kiat", null, term12013);

        Student u47 = new Student("billy.lam.2011", "Billy LAM Wai Loon", null, term12013);

        Student u48 = new Student("rosannechoo.2011", "Rosanne CHOO Sweet Cin", null, term12013);

        Student u49 = new Student("wjwee.2011", "WEE Wei Jian", null, term12013);

        Student u50 = new Student("yh.koon.2010", "Geraldine KOON Yuhua", null, term12013);

        Student u51 = new Student("juntao.zhu.2010", "ZHU Juntao", null, term12013);

        Student u52 = new Student("yg.tan.2010", "TAN Yao Guang", null, term12013);

        Student u53 = new Student("james.lim.2010", "James LIM Xing Yan", null, term12013);

        Student u54 = new Student("kevin.ng.2010", "Kevin NG Ying Yi", null, term12013);

        Student u55 = new Student("jonathan.ho.2010", "Jonathan HO Jian Wei", null, term12013);

        Student u56 = new Student("jolie.lee.2010", "Jolie LEE Jia Ling", null, term12013);

        Student u57 = new Student("radeyap.2010", "Radeya PARVEEN", null, term12013);

        Student u58 = new Student("rosalind.ng.2010", "Rosalind NG Hsiu Zhen", null, term12013);

        Student u59 = new Student("sitiz.k.2010", "Siti Zulaiha BTE KAMARUDIN", null, term12013);

        Student u60 = new Student("lionel.koh.2010", "Lionel KOH Wee Heng", null, term12013);

        Student u61 = new Student("xinyi.song.2010", "SONG Xinyi", null, term12013);

        // Persistence
        em.persist(uAdmin);
        em.persist(uCourseCoordinator);
        em.persist(ta1);
        em.persist(ta2);
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
		Settings bypassPassword = new Settings();
		bypassPassword.setName("bypassPassword");
		bypassPassword.setValue("default");
		
        Settings activeTerms = new Settings();
        activeTerms.setName("activeTerms");
        ArrayList<Long> activeTermIds = new ArrayList<Long>();
        activeTermIds.add(term12013.getId());
        activeTerms.setValue(new Gson().toJson(activeTermIds));

        Settings milestones = new Settings();
        milestones.setName("milestones");
        ArrayList<HashMap<String, Object>> milestoneList = new ArrayList<HashMap<String, Object>>();
        HashMap<String, Object> accMap = new HashMap<String, Object>();
        accMap.put("order", 1);
        accMap.put("milestone", "Acceptance");
        accMap.put("duration", 60);
        String[] accReqList = {"Supervisor"};
        accMap.put("attendees", accReqList);
        milestoneList.add(accMap);
        HashMap<String, Object> midMap = new HashMap<String, Object>();
        midMap.put("order", 2);
        midMap.put("milestone", "Midterm");
        midMap.put("duration", 90);
        String[] midReqList = {"Reviewer1", "Reviewer2"};
        midMap.put("attendees", midReqList);
        milestoneList.add(midMap);
        HashMap<String, Object> finMap = new HashMap<String, Object>();
        finMap.put("order", 3);
        finMap.put("milestone", "Final");
        finMap.put("duration", 90);
        String[] finReqList = {"Supervisor", "Reviewer1"};
        finMap.put("attendees", finReqList);
        milestoneList.add(finMap);
        milestones.setValue(new Gson().toJson(milestoneList));
		
		Settings manageNotifications = new Settings();
		manageNotifications.setName("manageNotifications");
		
		JSONArray notificationArray = new JSONArray();

		JSONObject email = new JSONObject();
		JSONObject sms = new JSONObject();
		JSONObject clearEmail = new JSONObject();
		
		email.put("emailStatus","On");
		email.put("emailFrequency","1");
		notificationArray.put(0,email);


		sms.put("smsStatus","On");
		sms.put("smsFrequency","24");
		notificationArray.put(1,sms);
		
		clearEmail.put("emailClearStatus","On");
		clearEmail.put("emailClearFrequency","2");
		notificationArray.put(2,clearEmail);
			
		manageNotifications.setValue(notificationArray.toString());
		
		
        //Persistence
		em.persist(bypassPassword);
        em.persist(activeTerms);
        em.persist(milestones);
		em.persist(manageNotifications);
    }
}