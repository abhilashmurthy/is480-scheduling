/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package util;

import java.io.IOException;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;
import model.Team;
import model.Term;
import model.User;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.LoggerFactory;

/**
 *
 * @author ABHILASHM.2010
 */
public class PopulatorUtil {

    //The wiki stuff
    Elements tables;
    Element term1;
    //Our own stuff
    static final org.slf4j.Logger logger = LoggerFactory.getLogger(PopulatorUtil.class);
    List<User> users = new ArrayList<User>();

    //Testing
    public static void main(String[] args) {
        PopulatorUtil p = new PopulatorUtil();
        p.init();
        p.populateTerms();
        p.populate();
    }

    public void init() {
        try {
            Document doc = Jsoup.connect("https://wiki.smu.edu.sg/is480/Main_Page").get();
            //Get base tables
            tables = doc.select("table");
            //Using Term 1 for dummy data
            term1 = tables.get(2);
        } catch (IOException ex) {
            logger.error("\nException caught: " + ex.getMessage());
        }
    }
    
    public void populateTerms() {
        Object[][] terms = {
            {2013, "Term 1"},
            {2013, "Term 2"},
            {2014, "Term 1"},
            {2014, "Term 2"}
        };
        for(Object[] term : terms) {
            Term newTerm = new Term();
            newTerm.setAcademicYear((Integer) term[0]);
            newTerm.setSemester((String) term[1]);
            //Add into DB
        }
    }

    public void populate() {
        Elements rows = term1.getElementsByTag("tr");
        rows.remove(0); //Removes header row
        for (Element row : rows) {
            //Getting the users
            Elements cells = row.getElementsByTag("td");
            String supervisor = cells.get(0).ownText();
            String[] reviewers = cells.get(1).ownText().split("&");
            String teamName = cells.get(2).select("a").first().attr("name");
            Element membersCell = cells.get(4);
            Elements members = membersCell.getElementsByTag("li");
            ArrayList<String> memberList = new ArrayList<String>();
            for (Element member : members) {
                memberList.add(member.ownText());
            }
            String client = cells.get(5).ownText();
            
            //Storing the users
            User supervisorUser = new User();
            supervisorUser.setFullName(supervisor);
            
            User reviewer1User = new User();
            reviewer1User.setFullName(reviewers[0]);
            
            User reviewer2User = new User();
            reviewer2User.setFullName(reviewers[1]);
            
            //Storing the teams
            Team team = new Team();
            team.setTeamName(teamName);
            team.setSupervisor(supervisorUser);
            team.setReviewer1(reviewer1User);
            team.setReviewer2(reviewer2User);
            
            for (String member : memberList) {
                User memberUser = new User();
                memberUser.setFullName(member);
            }
        }
    }
}
