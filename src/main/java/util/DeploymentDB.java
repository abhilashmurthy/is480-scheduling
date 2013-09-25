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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Contains method to initialize data in a new database. WARNING! Please run
 * this file only on a blank database!
 *
 * @author suresh
 */
public class DeploymentDB {

    static Logger logger = LoggerFactory.getLogger(DeploymentDB.class);

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

    private static void initDB(EntityManager em) throws Exception {
		
		Term term12013 = em.find(Term.class, 3L);
		
        /*
         * USER TABLE POPULATION
         */
        
        TA ta1 = new TA("justin.lim.2010", "Justin LIM Ding Yang", null, term12013);
        
        TA ta2 = new TA("kxleong.2010", "Michelle LEONG Kai Xin", null, term12013);
                
        Faculty u1 = new Faculty ("alanmegargel", "Alan MEGARGEL", null, term12013);
        
        Faculty u2 = new Faculty ("benjamingan", "Benjamin GAN Kok Siew", null, term12013);
        
        Faculty u3 = new Faculty ("cboesch", "Chris BOESCH", null, term12013);
        
        Faculty u4 = new Faculty ("davidlo", "David LO ", null, term12013);
        
        Faculty u5 = new Faculty ("lxjiang", "JIANG Lingxiao", null, term12013);
        
        Faculty u6 = new Faculty ("seemac", "Seema CHOKSHI", null, term12013);
        
        Student u7 = new Student ("bh.hoe.2011", "HOE Bing Huan", null, term12013);
        
        Student u8 = new Student ("zoey.teo.2011", "Zoey TEO Kai Ying", null, term12013);
        
        Student u9 = new Student ("meihui.chee.2011", "CHEE Mei Hui", null, term12013);
        
        Student u10 = new Student ("cp.pooi.2011", "POOI Ching Png", null, term12013);
        
        Student u11 = new Student ("elmaliqueg.2011", "El-malique Bin Md GHAZALI", null, term12013);
        
        Student u12 = new Student ("eugene.low.2011", "Eugene LOW Foo Mun", null, term12013);
        
        Student u13 = new Student ("eehian.tay.2011", "TAY Ee Hian", null, term12013);
        
        Student u14 = new Student ("cheeyonglim.2011", "LIM Chee Yong", null, term12013);
        
        Student u15 = new Student ("joannaphoon.2011", "Joanna PHOON Mu En", null, term12013);
        
        Student u16 = new Student ("fennie.too.2011", "Fennie TOO Jingyan", null, term12013);
        
        Student u17 = new Student ("matthew.c.2011", "Matthew CHEAH Kheng Boon", null, term12013);
        
        Student u18 = new Student ("haobo.wang.2011", "WANG Hao Bo", null, term12013);
        
        Student u19 = new Student ("clarissagoh.2011", "Clarissa GOH Shu Yi", null, term12013);
        
        Student u20 = new Student ("khcheng.2011", "CHENG Kam Him", null, term12013);
        
        Student u21 = new Student ("paige.lim.2011", "Paige Isabela Quiambao LIM", null, term12013);
        
        Student u22 = new Student ("muhammadmf.2011", "Muhammad Mustaqim Bin FADILLAH", null, term12013);
        
        Student u23 = new Student ("wahchun.ng.2011", "NG Wah Chun", null, term12013);
        
        Student u24 = new Student ("glen.wong.2011", "Glen WONG Kee Siang", null, term12013);
        
        Student u25 = new Student ("mohamedsh.2011", "Mohamed Yousof Bin SHAMSUL HAMEED", null, term12013);
        
        Student u26 = new Student ("fariq.said.2011", "Fariq Bin SAID", null, term12013);
        
        Student u27 = new Student ("shanaazmk.2011", "Shanaaz Do Musthafa MUSTHAFA KAMAL", null, term12013);
        
        Student u28 = new Student ("engsen.kee.2011", "KEE Eng Sen", null, term12013);
        
        Student u29 = new Student ("lynnettelim.2011", "Lynnette LIM Wen Zheng", null, term12013);
        
        Student u30 = new Student ("scong.2011", "ONG Sen Chian", null, term12013);
        
        Student u31 = new Student ("wq.siah.2011", "SIAH Wei Qiang", null, term12013);
        
        Student u32 = new Student ("jinglong.wu.2011", "WU Jinglong", null, term12013);
        
        Student u33 = new Student ("kaifeng.2011", "FENG Kai", null, term12013);
        
        Student u34 = new Student ("lilong.lim.2011", "LIM Li Long", null, term12013);
         
        Student u35 = new Student ("kimberlylek.2011", "Kimberly LEK Hui Lin", null, term12013);
          
        Student u36 = new Student ("zhiyue.tay.2011", "TAY Zhi Yue", null, term12013);
        
        Student u37 = new Student ("jy.ngoo.2011", "NGOO Jing Yong", null, term12013);
         
        Student u38 = new Student ("zylim.2011", "LIM Zui Young", null, term12013);
        
        Student u39 = new Student ("viryap.2011", "Virya PARAMITA", null, term12013);
        
        Student u40 = new Student ("gabrielbong.2011", "Gabriel BONG Zhi Siong", null, term12013);
        
        Student u41 = new Student ("simon.ang.2011", "Simon ANG Hock Leng", null, term12013);
        
        Student u42 = new Student ("jerrold.wee.2011", "Jerrold WEE Jia Wei", null, term12013);
        
        Student u43 = new Student ("tv.nguyen.2011", "NGUYEN Tuan Viet", null, term12013);
        
        Student u44 = new Student ("melvrickgoh.2011", "Melvrick GOH Aik Chun", null, term12013);
        
        Student u45 = new Student ("ygchan.2011", "CHAN Yong Guang", null, term12013);
        
        Student u46 = new Student ("yy.chan.2011", "CHAN Yeong Yuan @zeng Deyuan", null, term12013);
        
        Student u47 = new Student ("snseah.2011", "SEAH Siu Ngee", null, term12013);
        
        Student u48 = new Student ("sxlaw.2011", "LAW Sheng Xun", null, term12013);
        
        Student u49 = new Student ("qllee.2011", "LEE Quee Leong", null, term12013);
        
        Student u50 = new Student ("geoffreygoh.2011", "Geoffrey GOH Koon Hui", null, term12013);
        
        Student u51 = new Student ("sean.chua.2011", "Sean CHUA Kian Shun", null, term12013);
        
        Student u52 = new Student ("jieting.teo.2011", "TEO Jie Ting", null, term12013);
        
        Student u53 = new Student ("nicholaslee.2011", "Nicholas LEE Desheng", null, term12013);
        
        Student u54 = new Student ("bh.chua.2011", "CHUA Bing Huan", null, term12013);
        
        Student u55 = new Student ("zhenzhi.yeo.2011", "YEO Zhen Zhi", null, term12013);
        
        Student u56 = new Student ("spteo.2011", "TEO Siok Ping", null, term12013);
        
        Student u57 = new Student ("huiwenjiang.2011", "JIANG Huiwen", null, term12013);
        
        Student u58 = new Student ("qian.ye.2011", "YE Qian", null, term12013);
        
        Student u59 = new Student ("ec.cheong.2011", "Michael CHEONG Ee Chien", null, term12013);
        
        Student u60 = new Student ("willie.toh.2011", "TOH Willie", null, term12013);
        
        Student u61 = new Student ("weizhenglai.2011", "LAI Weizheng", null, term12013);
        
        Student u62 = new Student ("robyn.cheng.2011", "Robyn CHENG King", null, term12013);
        
        Student u63 = new Student ("jingyi.lim.2011", "LIM Jing Yi", null, term12013);
        
        Student u64 = new Student ("hanhui.koh.2011", "KOH Han Hui", null, term12013);
        
        Student u65 = new Student ("chelsea.toh.2011", "Chelsea TOH Miaoxing", null, term12013);
        
        Student u66 = new Student ("fiona.woo.2011", "Fiona WOO Yu Mei", null, term12013);
        
        Student u67 = new Student ("meizhen.tan.2011", "TAN Mei Zhen", null, term12013);
        
        Student u68 = new Student ("kenkhoonlim.2011", "LIM Ken Khoon", null, term12013);
        
        Student u69 = new Student ("darrylleong.2011", "Darryl LEONG Jun Hun", null, term12013);
        
        Student u70 = new Student ("yixian.lee.2011", "LEE Yi Xian", null, term12013);
        
        Student u71 = new Student ("yunxi.tan.2011", "TAN Yun Xi", null, term12013);
        
        Student u72 = new Student ("jeremylim.2011", "Jeremy LIM Jie Ming", null, term12013);
        
        Student u73 = new Student ("cfchan.2011", "CHAN Chun Fatt", null, term12013);
        
        Student u74 = new Student ("leonard.ong.2011", "Leonard ONG Guang Shan", null, term12013);
        
        Student u75 = new Student ("wjlee.2011", "LEE Wen Jie", null, term12013);
        
        Student u76 = new Student ("shuhan.tan.2011", "TAN Shu Han", null, term12013);
       
        Student u77 = new Student ("lx.chuah.2011", "CHUAH Li Xian", null, term12013);
        
        Student u78 = new Student ("eytanojo.2011", "Edward Young TANOJO", null, term12013);
        
        Student u79 = new Student ("akkustedjo.2011", "Alfred Kusuma KUSTEDJO", null, term12013);
        
        Student u80 = new Student ("antonysalim.2011", "Antony SALIM", null, term12013);
        
        Student u81 = new Student ("xi.chen.2011", "CHEN Xi", null, term12013);
        
        Student u82 = new Student ("dewi.zana.2011", "Dewi ZANA", null, term12013);
        
        Student u83 = new Student ("huiqi.ang.2011", "ANG Hui Qi", null, term12013);
        
        Student u84 = new Student ("zhang.zy.2011", "ZHANG Zhongyun", null, term12013);
        
        Student u85 = new Student ("qilinjia.2011", "JIA Qilin", null, term12013);
        
        Student u86 = new Student ("xinlin.tan.2011", "TAN Xin Lin", null, term12013);
        
        Student u87 = new Student ("ehtan.2011", "Nicholas TAN Eng Howe", null, term12013);
        
        Student u88 = new Student ("rachel.quah.2011", "Rachel QUAH Shu Ting", null, term12013);
        
        Student u89 = new Student ("btjoseph.2011", "Benjos Antony THAREPARAMBIL JOSEPH", null, term12013);
        
        Student u90 = new Student ("ktchan.2011", "CHAN Keng Theng", null, term12013);
        
        Student u91 = new Student ("qianbi.foo.2011", "FOO Qian Bi", null, term12013);
        
        Student u92 = new Student ("kean.kwok.2011", "Kean KWOK Jin", null, term12013);
        
        Student u93 = new Student ("wl.chung.2011", "William CHUNG Wui Lun", null, term12013);
        
        Student u94 = new Student ("jen.low.2011", "Jen LOW Ren Jie", null, term12013);
        
        Student u95 = new Student ("quanhenglim.2011", "LIM Quan Heng", null, term12013);
        
        Student u96 = new Student ("kr.tan.2011", "Benjamin TAN Kei Rong", null, term12013);
        
        Student u97 = new Student ("pearlyn.neo.2010", "Pearlyn NEO Hui Min", null, term12013);
        
        Student u98 = new Student ("ruth.liem.2011", "Priscilla Ruth LIEM", null, term12013);
        
        Student u99 = new Student ("rachel.wang.2011", "Rachel WANG Yee Lin", null, term12013);
        
        Student u100 = new Student ("lnugroho.2011@sis.smu.edu.sg", "Lydia NUGROHO", null, term12013);
        
        Student u101 = new Student ("m.shahrain.2011", "Muhammad Shahrain Bin SARDON", null, term12013);
        
        Student u102 = new Student ("mytan.2011", "TAN Meng Yew", null, term12013);
        
        Student u103 = new Student ("kjsebastian.2011", "SEBASTIAN Kiran Joy", null, term12013);
        
        Student u104 = new Student ("wenguanglow.2011", "LOW Wen Guang", null, term12013);
        
        Student u105 = new Student ("noelle.tan.2011", "Noelle TAN Rui Jia", null, term12013);
        
        Student u106 = new Student ("victor.lee.2011", "Victor LEE Hong Zhi", null, term12013);
        
        Student u107 = new Student ("yaqing.bie.2011", "BIE Ya Qing", null, term12013);
        
        Student u108 = new Student ("guohao.tan.2011", "TAN Guo Hao", null, term12013);
        
        Student u109 = new Student ("pschua.2011", "CHUA Pei Shan", null, term12013);
        
        Student u110 = new Student ("zhenyuan.ng.2011", "NG Zhenyuan", null, term12013);
        
        Student u111 = new Student ("shemin.ang.2011", "Shemin ANG Qiao Ni", null, term12013);
        
        Student u112 = new Student ("th.tan.2011", "Gwendolin TAN Tiong Hui", null, term12013);
        
        Student u113 = new Student ("xinyi.lim.2011", "LIM Xin Yi", null, term12013);
        
        Student u114 = new Student ("yvonne.phui.2011", "Yvonne PHUI Rui Qi", null, term12013);
        
        Student u115 = new Student ("yiling.ong.2011", "ONG Yi Ling", null, term12013);
        
        Student u116 = new Student ("cs.tan.2011", "TAN Chit Sian", null, term12013);
        
        Student u117 = new Student ("kelvin.wong.2011", "Kelvin WONG Wei Hong", null, term12013);
        
        Student u118 = new Student ("rayner.koh.2011", "Rayner KOH Yok How", null, term12013);
        
        Student u119 = new Student ("junzhi.seah.2011", "SEAH Jun Zhi", null, term12013);
        
        Student u120 = new Student ("leslie.leow.2011", "Leslie LEOW Jun Qiang", null, term12013);
        
        Student u121 = new Student ("flwang.2011", "WANG Fenglin", null, term12013);
        
        Student u122 = new Student ("chgoh.2011", "GOH Chin Hong", null, term12013);
        
        Student u123 = new Student ("wyner.lim.2011", "Wyner LIM Wei Han", null, term12013);
        
        Student u124 = new Student ("fuhua.shih.2011", "SHIH Fu Hua", null, term12013);
        
        Student u125 = new Student ("agurz.leong.2011", "Agurz Gary LEONG Kwok Wai", null, term12013);
        
        Student u126 = new Student ("denise.wong.2011", "Denise WONG Kar Lin", null, term12013);
        
        Student u127 = new Student ("nicholas.li.2011", "Nicholas LI Jiacheng", null, term12013);
        
        Student u128 = new Student ("yisong.loh.2011", "LOH Yi Song", null, term12013);
        
        Student u129 = new Student ("eleazar.lim.2011", "Eleazar LIM Wei Ji", null, term12013);
        
        Student u130 = new Student ("janan.tan.2011", "Janan TAN", null, term12013);
        
        Student u131 = new Student ("pvttran.2011", "TRAN Pham Viet Thao", null, term12013);
        
        Student u132 = new Student ("mriingen.2011", "Miguel Nicholas Zamora RIINGEN", null, term12013);
        
        Student u133 = new Student ("edric.2011", "EDRIC", null, term12013);
        
        Student u134 = new Student ("bnaguiat.2011", "NAGUIAT Beatriz Camille Abijay", null, term12013);
        
        Student u135 = new Student ("hungle.2011", "LE Hung", null, term12013);
        
        Student u136 = new Student ("jerrell.che.2010", "Jerrell CHE Shao Xian", null, term12013);
        
        Student u137 = new Student ("sherman.tan.2010", "Sherman TAN Thuan Leng", null, term12013);
        
        Student u138 = new Student ("gladys.lim.2010", "Gladys LIM Zhen Rong", null, term12013);
        
        Student u139 = new Student ("geoklin.tay.2011", "TAY Geok Lin", null, term12013);
        
        Student u140 = new Student ("racheltan.2011", "Rachel TAN Pei Ying", null, term12013);
        
        Student u141 = new Student ("htle.2011", "LE Hoang Trinh", null, term12013);
        
        Student u142 = new Student ("xiuhao.kuek.2011", "KUEK Xiu Hao", null, term12013);
        
        Student u143 = new Student ("ktv.nguyen.2011", "NGUYEN Khac Thien Vu", null, term12013);
        
        Student u144 = new Student ("mtphung.2011", "PHUNG Minh Thu", null, term12013);
        
        Student u145 = new Student ("huishia.tay.2011", "TAY Hui Shia", null, term12013);
        
        Student u146 = new Student ("tommy.soh.2011", "Tommy SOH Jun Kui", null, term12013);
        
        Student u147 = new Student ("jl.tan.2010", "TAN Jun Liang", null, term12013);
        
        Student u148 = new Student ("hector.sim.2011", "Hector SIM Yiu Shin", null, term12013);
        
        Student u149 = new Student ("wctan.2011", "TAN Wei Chong", null, term12013);
        
        Student u150 = new Student ("amhollen.2011", "Anthony Marlius HOLLEN", null, term12013);
        
        Student u151 = new Student ("wenxuan.he.2011", "HE Wenxuan", null, term12013);
        
        Student u152 = new Student ("fzsun.2011", "SUN Fangzhou", null, term12013);
        
        Student u153 = new Student ("zeyaoliu.2011", "LIU Zeyao", null, term12013);
        
        Student u154 = new Student ("jifei.zhang.2010", "ZHANG Jifei", null, term12013);
        
        Student u155 = new Student ("xinxia.2009", "XIA Xin", null, term12013);
        
        Student u156 = new Student ("lu.yang.2011", "YANG Lu", null, term12013);
        
        Student u157 = new Student ("yangewang.2011", "WANG Yange", null, term12013);
        
        Student u158 = new Student ("yidongwang.2011", "WANG Yidong", null, term12013);
        
        Student u159 = new Student ("tao.liang.2011", "LIANG Tao", null, term12013);
        
        Student u160 = new Student ("mlgoh.2009", "Alexander GOH Mingliang", null, term12013);
        
        Student u161 = new Student ("nhyupa.2010", "YUPA Nyein Htoon", null, term12013);
        
        Student u162 = new Student ("george.ng.2010", "George NG Xuan Long", null, term12013);
        
        Student u163 = new Student ("samuel.phua.2011", "Samuel PHUA Wee Kiat", null, term12013);
        
        Student u164 = new Student ("bryan.neo.2011", "Bryan NEO Han Sheng", null, term12013);
        
        Student u165 = new Student ("ayeayesoe.2011", "Aye Aye SOE", null, term12013);
        
        Student u166 = new Student ("junkiat.yeo.2011", "YEO Jun Kiat", null, term12013);
        
        Student u167 = new Student ("rui.wang.2011", "WANG Rui", null, term12013);
        
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
        em.persist(u62);
        em.persist(u63);
        em.persist(u64);
        em.persist(u65);
        em.persist(u66);
        em.persist(u67);
        em.persist(u68);
        em.persist(u69);
        em.persist(u70);
        em.persist(u71);
        em.persist(u72);
        em.persist(u73);
        em.persist(u74);
        em.persist(u75);
        em.persist(u76);
        em.persist(u77);
        em.persist(u78);
        em.persist(u79);
        em.persist(u80);
        em.persist(u81);
        em.persist(u82);
        em.persist(u83);
        em.persist(u84);
        em.persist(u85);
        em.persist(u86);
        em.persist(u87);
        em.persist(u88);
        em.persist(u89);
        em.persist(u90);
        em.persist(u91);
        em.persist(u92);
        em.persist(u93);
        em.persist(u94);
        em.persist(u95);
        em.persist(u96);
        em.persist(u97);
        em.persist(u98);
        em.persist(u99);
        em.persist(u100);
        em.persist(u101);
        em.persist(u102);
        em.persist(u103);
        em.persist(u104);
        em.persist(u105);
        em.persist(u106);
        em.persist(u107);
        em.persist(u108);
        em.persist(u109);
        em.persist(u110);
        em.persist(u111);
        em.persist(u112);
        em.persist(u113);
        em.persist(u114);
        em.persist(u115);
        em.persist(u116);
        em.persist(u117);
        em.persist(u118);
        em.persist(u119);
        em.persist(u120);
        em.persist(u121);
        em.persist(u122);
        em.persist(u123);
        em.persist(u124);
        em.persist(u125);
        em.persist(u126);
        em.persist(u127);
        em.persist(u128);
        em.persist(u129);
        em.persist(u130);
        em.persist(u131);
        em.persist(u132);
        em.persist(u133);
        em.persist(u134);
        em.persist(u135);
        em.persist(u136);
        em.persist(u137);
        em.persist(u138);
        em.persist(u139);
        em.persist(u140);
        em.persist(u141);
        em.persist(u142);
        em.persist(u143);
        em.persist(u144);
        em.persist(u145);
        em.persist(u146);
        em.persist(u147);
        em.persist(u148);
        em.persist(u149);
        em.persist(u150);
        em.persist(u151);
        em.persist(u152);
        em.persist(u153);
        em.persist(u154);
        em.persist(u155);
        em.persist(u156);
        em.persist(u157);
        em.persist(u158);
        em.persist(u159);
        em.persist(u160);
        em.persist(u161);
        em.persist(u162);
        em.persist(u163);
        em.persist(u164);
        em.persist(u165);
        em.persist(u166);
        em.persist(u167);
        logger.info("Users persisted");

        /*
         * TEAM TABLE POPULATION
         */
        Team t1 = new Team();
        t1.setTerm(term12013);
        t1.setTeamName("D’PENZ");
        t1.setSupervisor(u1);
        t1.setReviewer1(u2);
        t1.setReviewer2(u3);
        HashSet<Student> members = new HashSet<Student>();
        members.add(u7);
        members.add(u8);
        members.add(u9);
        members.add(u10);
        members.add(u11);
        t1.setMembers(members);


        Team t2 = new Team();
        t2.setTerm(term12013);
        t2.setTeamName("KUMUS");
        t2.setSupervisor(u1);
        //t2.setReviewer1(u8);
        //t2.setReviewer2(u6);
        HashSet<Student> t2members = new HashSet<Student>();
        t2members.add(u12);
        t2members.add(u13);
        t2members.add(u14);
        t2members.add(u15);
        t2members.add(u16);
        t2members.add(u17);
        t2.setMembers(t2members);

        Team t3 = new Team();
        t3.setTerm(term12013);
        t3.setTeamName("Techne Logos");
        t3.setSupervisor(u1);
        //t3.setReviewer1(u15);
        //t3.setReviewer2(u7);
        HashSet<Student> t3members = new HashSet<Student>();
        t3members.add(u18);
        t3members.add(u19);
        t3members.add(u20);
        t3members.add(u21);
        t3members.add(u22);
        t3.setMembers(t3members);

        Team t4 = new Team();
        t4.setTerm(term12013);
        t4.setTeamName("GENShYFT");
        t4.setSupervisor(u2);
        //t4.setReviewer1(u7);
        //t4.setReviewer2(u8);
        HashSet<Student> t4members = new HashSet<Student>();
        t4members.add(u23);
        t4members.add(u24);
        t4members.add(u25);
        t4members.add(u26);
        t4members.add(u27);
        t4members.add(u28);
        t4.setMembers(t4members);

        Team t5 = new Team();
        t5.setTerm(term12013);
        t5.setTeamName("iChallenge");
        t5.setSupervisor(u2);
        //t5.setReviewer1(u21);
        //t5.setReviewer2(u6);
        HashSet<Student> t5members = new HashSet<Student>();
        t5members.add(u29);
        t5members.add(u30);
        t5members.add(u31);
        t5members.add(u32);
        t5members.add(u33);
        t5.setMembers(t5members);

        Team t6 = new Team();
        t6.setTerm(term12013);
        t6.setTeamName("SixDotz");
        t6.setSupervisor(u2);
        //t6.setReviewer1(u8);
        //t6.setReviewer2(u6);
        HashSet<Student> t6members = new HashSet<Student>();
        t6members.add(u34);
        t6members.add(u35);
        t6members.add(u36);
        t6members.add(u37);
        t6members.add(u38);
        t6.setMembers(t6members);

        Team t7 = new Team();
        t7.setTerm(term12013);
        t7.setTeamName("AAN");
        t7.setSupervisor(u3);
        //t7.setReviewer1(u6);
        //t7.setReviewer2(u15);
        HashSet<Student> t7members = new HashSet<Student>();
        t7members.add(u39);
        t7members.add(u40);
        t7members.add(u41);
        t7members.add(u42);
        t7members.add(u43);
        t7.setMembers(t7members);

        Team t8 = new Team();
        t8.setTerm(term12013);
        t8.setTeamName("Five&AHalfMen");
        t8.setSupervisor(u3);
        //t8.setReviewer1(u15);
        //t8.setReviewer2(u21);
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
        t9.setTeamName("RubberDuck Studios");
        t9.setSupervisor(u3);
       // t9.setReviewer1(u15);
        //t9.setReviewer2(u7);
        HashSet<Student> t9members = new HashSet<Student>();
        t9members.add(u50);
        t9members.add(u51);
        t9members.add(u52);
        t9members.add(u53);
        t9members.add(u54);
        t9.setMembers(t9members);

        Team t10 = new Team();
        t10.setTerm(term12013);
        t10.setTeamName("LittleTeam");
        t10.setSupervisor(u4);
        //t10.setReviewer1(u6);
        //t10.setReviewer2(u15);
        HashSet<Student> t10members = new HashSet<Student>();
        t10members.add(u55);
        t10members.add(u56);
        t10members.add(u57);
        t10members.add(u58);
        t10members.add(u59);
        t10members.add(u60);
        t10.setMembers(t10members);
        
        Team t11 = new Team();
        t11.setTerm(term12013);
        t11.setTeamName("The Partners");
        t11.setSupervisor(u4);
        //t11.setReviewer1(u6);
        //t11.setReviewer2(u15);
        HashSet<Student> t11members = new HashSet<Student>();
        t11members.add(u61);
        t11members.add(u62);
        t11members.add(u63);
        t11members.add(u64);
        t11members.add(u65);
        t11.setMembers(t11members);
        
        Team t12 = new Team();
        t12.setTerm(term12013);
        t12.setTeamName("Zora");
        t12.setSupervisor(u4);
        //t12.setReviewer1(u6);
        //t12.setReviewer2(u15);
        HashSet<Student> t12members = new HashSet<Student>();
        t12members.add(u66);
        t12members.add(u67);
        t12members.add(u68);
        t12members.add(u69);
        t12members.add(u70);
        t12members.add(u71);
        t12.setMembers(t12members);
        
        Team t13 = new Team();
        t13.setTerm(term12013);
        t13.setTeamName("Silicon Geeks");
        t13.setSupervisor(u4);
        //t13.setReviewer1(u6);
        //t13.setReviewer2(u15);
        HashSet<Student> t13members = new HashSet<Student>();
        t13members.add(u72);
        t13members.add(u73);
        t13members.add(u74);
        t13members.add(u75);
        t13members.add(u76);
        t13members.add(u77);
        t13.setMembers(t13members);
        
        Team t14 = new Team();
        t14.setTerm(term12013);
        t14.setTeamName("Bisa");
        t14.setSupervisor(u5);
        //t14.setReviewer1(u6);
        //t14.setReviewer2(u15);
        HashSet<Student> t14members = new HashSet<Student>();
        t14members.add(u78);
        t14members.add(u79);
        t14members.add(u80);
        t14members.add(u81);
        t14members.add(u82);
        t14.setMembers(t14members);
        
        Team t15 = new Team();
        t15.setTerm(term12013);
        t15.setTeamName("Carpe Diem");
        t15.setSupervisor(u5);
        //t15.setReviewer1(u6);
        //t15.setReviewer2(u15);
        HashSet<Student> t15members = new HashSet<Student>();
        t15members.add(u83);
        t15members.add(u84);
        t15members.add(u85);
        t15members.add(u86);
        t15members.add(u87);
        t15.setMembers(t15members);
        
        Team t16 = new Team();
        t16.setTerm(term12013);
        t16.setTeamName("Change-Makers");
        t16.setSupervisor(u5);
        //t16.setReviewer1(u6);
        //t16.setReviewer2(u15);
        HashSet<Student> t16members = new HashSet<Student>();
        t16members.add(u88);
        t16members.add(u89);
        t16members.add(u90);
        t16members.add(u91);
        t16members.add(u92);
        t16.setMembers(t16members);
        
        Team t17 = new Team();
        t17.setTerm(term12013);
        t17.setTeamName("Invenio");
        t17.setSupervisor(u5);
        //t17.setReviewer1(u6);
        //t17.setReviewer2(u15);
        HashSet<Student> t17members = new HashSet<Student>();
        t17members.add(u93);
        t17members.add(u94);
        t17members.add(u95);
        t17members.add(u96);
        t17members.add(u97);
        t17.setMembers(t17members);
        
        Team t18 = new Team();
        t18.setTerm(term12013);
        t18.setTeamName("JavaChips");
        //t18.setSupervisor(u5);
        //t18.setReviewer1(u6);
        //t18.setReviewer2(u15);
        HashSet<Student> t18members = new HashSet<Student>();
        t18members.add(u98);
        t18members.add(u99);
        t18members.add(u100);
        t18members.add(u101);
        t18members.add(u102);
        t18.setMembers(t18members);
        
        Team t19 = new Team();
        t19.setTerm(term12013);
        t19.setTeamName("Excelente");
        //t19.setSupervisor(u5);
        //t19.setReviewer1(u6);
        //t19.setReviewer2(u15);
        HashSet<Student> t19members = new HashSet<Student>();
        t19members.add(u103);
        t19members.add(u104);
        t19members.add(u105);
        t19members.add(u106);
        t19members.add(u107);
        t19members.add(u108);
        t19.setMembers(t19members);
        
        Team t20 = new Team();
        t20.setTerm(term12013);
        t20.setTeamName("#OneRevolution");
        t20.setSupervisor(u1);
        //t20.setReviewer1(u6);
        //t20.setReviewer2(u15);
        HashSet<Student> t20members = new HashSet<Student>();
        t20members.add(u109);
        t20members.add(u110);
        t20members.add(u111);
        t20members.add(u112);
        t20members.add(u113);
        t20.setMembers(t20members);
        
        Team t21 = new Team();
        t21.setTerm(term12013);
        t21.setTeamName("Beedoh");
        //t21.setSupervisor(u1);
        //t21.setReviewer1(u6);
        //t21.setReviewer2(u15);
        HashSet<Student> t21members = new HashSet<Student>();
        t21members.add(u114);
        t21members.add(u115);
        t21members.add(u116);
        t21members.add(u117);
        t21members.add(u118);
        t21members.add(u119);
        t21.setMembers(t21members);
        
        Team t22 = new Team();
        t22.setTerm(term12013);
        t22.setTeamName("golf.(y).peers");
        //t22.setSupervisor(u1);
        //t22.setReviewer1(u6);
        //t22.setReviewer2(u15);
        HashSet<Student> t22members = new HashSet<Student>();
        t22members.add(u120);
        t22members.add(u121);
        t22members.add(u122);
        t22members.add(u123);
        t22members.add(u124);
        t22.setMembers(t22members);
        
        Team t23 = new Team();
        t23.setTerm(term12013);
        t23.setTeamName("Dr.Jean");
        //t23.setSupervisor(u1);
        //t23.setReviewer1(u6);
        //t23.setReviewer2(u15);
        HashSet<Student> t23members = new HashSet<Student>();
        t23members.add(u125);
        t23members.add(u126);
        t23members.add(u127);
        t23members.add(u128);
        t23members.add(u129);
        t23members.add(u130);
        t23.setMembers(t23members);
        
        Team t24 = new Team();
        t24.setTerm(term12013);
        t24.setTeamName("Team 1");
        //t24.setSupervisor(u1);
        //t24.setReviewer1(u6);
        //t24.setReviewer2(u15);
        HashSet<Student> t24members = new HashSet<Student>();
        t24members.add(u131);
        t24members.add(u132);
        t24members.add(u133);
        t24members.add(u134);
        t24members.add(u135);
        t24.setMembers(t24members);
        
        Team t25 = new Team();
        t25.setTerm(term12013);
        t25.setTeamName("Rubix");
        //t25.setSupervisor(u1);
        //t25.setReviewer1(u6);
        //t25.setReviewer2(u15);
        HashSet<Student> t25members = new HashSet<Student>();
        t25members.add(u136);
        t25members.add(u137);
        t25members.add(u138);
        t25members.add(u139);
        t25members.add(u140);
        t25.setMembers(t25members);
        
        Team t26 = new Team();
        t26.setTerm(term12013);
        t26.setTeamName("Tappers");
        t26.setSupervisor(u6);
        //t26.setReviewer1(u6);
        //t26.setReviewer2(u15);
        HashSet<Student> t26members = new HashSet<Student>();
        t26members.add(u141);
        t26members.add(u142);
        t26members.add(u143);
        t26members.add(u144);
        t26.setMembers(t26members);
        
        Team t27 = new Team();
        t27.setTerm(term12013);
        t27.setTeamName("The Codefather");
        //t27.setSupervisor(u1);
        //t27.setReviewer1(u6);
        //t27.setReviewer2(u15);
        HashSet<Student> t27members = new HashSet<Student>();
        t27members.add(u145);
        t27members.add(u146);
        t27members.add(u147);
        t27members.add(u148);
        t27members.add(u149);
        t27members.add(u150);
        t27.setMembers(t27members);
        
        Team t28 = new Team();
        t28.setTerm(term12013);
        t28.setTeamName("EINE");
        t28.setSupervisor(u6);
        //t28.setReviewer1(u6);
        //t28.setReviewer2(u15);
        HashSet<Student> t28members = new HashSet<Student>();
        t28members.add(u151);
        t28members.add(u152);
        t28members.add(u153);
        t28members.add(u154);
        t28.setMembers(t28members);
        
        Team t29 = new Team();
        t29.setTerm(term12013);
        t29.setTeamName("Newbility");
        t29.setSupervisor(u6);
        //t29.setReviewer1(u6);
        //t29.setReviewer2(u15);
        HashSet<Student> t29members = new HashSet<Student>();
        t29members.add(u155);
        t29members.add(u156);
        t29members.add(u157);
        t29members.add(u158);
        t29members.add(u159);
        t29.setMembers(t29members);
        
        Team t30 = new Team();
        t30.setTerm(term12013);
        t30.setTeamName("+ve Impressions");
        //t30.setSupervisor(u6);
        //t30.setReviewer1(u6);
        //t30.setReviewer2(u15);
        HashSet<Student> t30members = new HashSet<Student>();
        t30members.add(u160);
        t30members.add(u161);
        t30members.add(u162);
        t30members.add(u163);
        t30.setMembers(t30members);
        
        Team t31 = new Team();
        t31.setTerm(term12013);
        t31.setTeamName("Team Santé");
        //t31.setSupervisor(u6);
        //t31.setReviewer1(u6);
        //t31.setReviewer2(u15);
        HashSet<Student> t31members = new HashSet<Student>();
        t31members.add(u164);
        t31members.add(u165);
        t31members.add(u166);
        t31members.add(u167);
        t31.setMembers(t31members);

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
        em.persist(t11);
        em.persist(t12);
        em.persist(t13);
        em.persist(t14);
        em.persist(t15);
        em.persist(t16);
        em.persist(t17);
        em.persist(t18);
        em.persist(t19);
        em.persist(t20);
        em.persist(t21);
        em.persist(t22);
        em.persist(t23);
        em.persist(t24);
        em.persist(t25);
        em.persist(t26);
        em.persist(t27);
        em.persist(t28);
        em.persist(t29);
        em.persist(t30);
        em.persist(t31);
        logger.info("Teams persisted");

        /*
         * LINKING USERS AND TEAMS
         */
        u7.setTeam(t1);
        u8.setTeam(t1);
        u9.setTeam(t1);
        u10.setTeam(t1);
        u11.setTeam(t1);

        u12.setTeam(t2);
        u13.setTeam(t2);
        u14.setTeam(t2);
        u15.setTeam(t2);
        u16.setTeam(t2);
        u17.setTeam(t2);

        u18.setTeam(t3);
        u19.setTeam(t3);
        u20.setTeam(t3);
        u21.setTeam(t3);
        u22.setTeam(t3);

        u23.setTeam(t4);
        u24.setTeam(t4);
        u25.setTeam(t4);
        u26.setTeam(t4);
        u27.setTeam(t4);
        u28.setTeam(t4);

        u29.setTeam(t5);
        u30.setTeam(t5);
        u31.setTeam(t5);
        u32.setTeam(t5);
        u33.setTeam(t5);

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

        u55.setTeam(t10);
        u56.setTeam(t10);
        u57.setTeam(t10);
        u58.setTeam(t10);
        u59.setTeam(t10);
        u60.setTeam(t10);
        
        u61.setTeam(t11);
        u62.setTeam(t11);
        u63.setTeam(t11);
        u64.setTeam(t11);
        u65.setTeam(t11);
        
        u66.setTeam(t12);
        u67.setTeam(t12);
        u68.setTeam(t12);
        u69.setTeam(t12);
        u70.setTeam(t12);
        u71.setTeam(t12);
        
        u72.setTeam(t13);
        u73.setTeam(t13);
        u74.setTeam(t13);
        u75.setTeam(t13);
        u76.setTeam(t13);
        u77.setTeam(t13);
        
        u78.setTeam(t14);
        u79.setTeam(t14);
        u80.setTeam(t14);
        u81.setTeam(t14);
        u82.setTeam(t14);
        
        u83.setTeam(t15);
        u84.setTeam(t15);
        u85.setTeam(t15);
        u86.setTeam(t15);
        u87.setTeam(t15);
        
        u88.setTeam(t16);
        u89.setTeam(t16);
        u90.setTeam(t16);
        u91.setTeam(t16);
        u92.setTeam(t16);
        
        u93.setTeam(t17);
        u94.setTeam(t17);
        u95.setTeam(t17);
        u96.setTeam(t17);
        u97.setTeam(t17);
        
        u98.setTeam(t18);
        u99.setTeam(t18);
        u100.setTeam(t18);
        u101.setTeam(t18);
        u102.setTeam(t18);
        
        u103.setTeam(t19);
        u104.setTeam(t19);
        u105.setTeam(t19);
        u106.setTeam(t19);
        u107.setTeam(t19);
        u108.setTeam(t19);
        
        u109.setTeam(t20);
        u110.setTeam(t20);
        u111.setTeam(t20);
        u112.setTeam(t20);
        u113.setTeam(t20);
        
        u114.setTeam(t21);
        u115.setTeam(t21);
        u116.setTeam(t21);
        u117.setTeam(t21);
        u118.setTeam(t21);
        u119.setTeam(t21);
        
        u120.setTeam(t22);
        u121.setTeam(t22);
        u122.setTeam(t22);
        u123.setTeam(t22);
        u124.setTeam(t22);
        
        u125.setTeam(t23);
        u126.setTeam(t23);
        u127.setTeam(t23);
        u128.setTeam(t23);
        u129.setTeam(t23);
        u130.setTeam(t23);
        
        u131.setTeam(t24);
        u132.setTeam(t24);
        u133.setTeam(t24);
        u134.setTeam(t24);
        u135.setTeam(t24);

        u136.setTeam(t25);
        u137.setTeam(t25);
        u138.setTeam(t25);
        u139.setTeam(t25);
        u140.setTeam(t25);
        
        u141.setTeam(t26);
        u142.setTeam(t26);
        u143.setTeam(t26);
        u144.setTeam(t26);
        
        u145.setTeam(t27);
        u146.setTeam(t27);
        u147.setTeam(t27);
        u148.setTeam(t27);
        u149.setTeam(t27);
        u150.setTeam(t27);
        
        u151.setTeam(t28);
        u152.setTeam(t28);
        u153.setTeam(t28);
        u154.setTeam(t28);
        
        u155.setTeam(t29);
        u156.setTeam(t29);
        u157.setTeam(t29);
        u158.setTeam(t29);
        u159.setTeam(t29);
        
        u160.setTeam(t30);
        u161.setTeam(t30);
        u162.setTeam(t30);
        u163.setTeam(t30);
        
        u164.setTeam(t31);
        u165.setTeam(t31);
        u166.setTeam(t31);
        u167.setTeam(t31);

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
        em.persist(u62);
        em.persist(u63);
        em.persist(u64);
        em.persist(u65);
        em.persist(u66);
        em.persist(u67);
        em.persist(u68);
        em.persist(u69);
        em.persist(u70);
        em.persist(u71);
        em.persist(u72);
        em.persist(u73);
        em.persist(u74);
        em.persist(u75);
        em.persist(u76);
        em.persist(u77);
        em.persist(u78);
        em.persist(u79);
        em.persist(u80);
        em.persist(u81);
        em.persist(u82);
        em.persist(u83);
        em.persist(u84);
        em.persist(u85);
        em.persist(u86);
        em.persist(u87);
        em.persist(u88);
        em.persist(u89);
        em.persist(u90);
        em.persist(u91);
        em.persist(u92);
        em.persist(u93);
        em.persist(u94);
        em.persist(u95);
        em.persist(u96);
        em.persist(u97);
        em.persist(u98);
        em.persist(u99);
        em.persist(u100);
        em.persist(u101);
        em.persist(u102);
        em.persist(u103);
        em.persist(u104);
        em.persist(u105);
        em.persist(u106);
        em.persist(u107);
        em.persist(u108);
        em.persist(u109);
        em.persist(u110);
        em.persist(u111);
        em.persist(u112);
        em.persist(u113);
        em.persist(u114);
        em.persist(u115);
        em.persist(u116);
        em.persist(u117);
        em.persist(u118);
        em.persist(u119);
        em.persist(u120);
        em.persist(u121);
        em.persist(u122);
        em.persist(u123);
        em.persist(u124);
        em.persist(u125);
        em.persist(u126);
        em.persist(u127);
        em.persist(u128);
        em.persist(u129);
        em.persist(u130);
        em.persist(u131);
        em.persist(u132);
        em.persist(u133);
        em.persist(u134);
        em.persist(u135);
        em.persist(u136);
        em.persist(u137);
        em.persist(u138);
        em.persist(u139);
        em.persist(u140);
        em.persist(u141);
        em.persist(u142);
        em.persist(u143);
        em.persist(u144);
        em.persist(u145);
        em.persist(u146);
        em.persist(u147);
        em.persist(u148);
        em.persist(u149);
        em.persist(u150);
        em.persist(u151);
        em.persist(u152);
        em.persist(u153);
        em.persist(u154);
        em.persist(u155);
        em.persist(u156);
        em.persist(u157);
        em.persist(u158);
        em.persist(u159);
        em.persist(u160);
        em.persist(u161);
        em.persist(u162);
        em.persist(u163);
        em.persist(u164);
        em.persist(u165);
        em.persist(u166);
        em.persist(u167);
        logger.info("User --> Team links persisted");
    }
}